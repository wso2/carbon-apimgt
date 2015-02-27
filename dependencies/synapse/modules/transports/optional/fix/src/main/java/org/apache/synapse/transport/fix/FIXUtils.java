/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.transport.fix;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import quickfix.*;
import quickfix.field.*;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;

public class FIXUtils {

    private static final Log log = LogFactory.getLog(FIXUtils.class);
    private static FIXUtils _instance = new FIXUtils();

    public static FIXUtils getInstance() {
        return _instance;
    }

    /**
     * FIX messages are non-XML. So convert them into XML using the AXIOM API.
     * Put the FIX message into an Axis2 MessageContext.The basic format of the
     * generated SOAP envelope;
     * <p/>
     * <soapEnvelope>
     * <soapBody>
     * <message>
     * <header> ....</header>
     * <body> .... </body>
     * <trailer> .... </trailer>
     * </message>
     * </soapBody>
     * </soapEnvelope>
     *
     * @param message   the FIX message
     * @param counter   application level sequence number of the message
     * @param sessionID the incoming session
     * @param msgCtx    the Axis2 MessageContext to hold the FIX message
     * @throws AxisFault the exception thrown when invalid soap envelopes are set to the msgCtx
     */
    public void setSOAPEnvelope(Message message, int counter, String sessionID,
                                MessageContext msgCtx) throws AxisFault {

        if (log.isDebugEnabled()) {
            log.debug("Creating SOAP envelope for FIX message...");
        }

        SOAPFactory soapFactory = new SOAP11Factory();
        OMElement msg = soapFactory.createOMElement(FIXConstants.FIX_MESSAGE, null);
        msg.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_MESSAGE_INCOMING_SESSION,
                null, sessionID));
        msg.addAttribute(soapFactory.createOMAttribute
                (FIXConstants.FIX_MESSAGE_COUNTER, null, String.valueOf(counter)));

        OMElement header = soapFactory.createOMElement(FIXConstants.FIX_HEADER, null);
        OMElement body = soapFactory.createOMElement(FIXConstants.FIX_BODY, null);
        OMElement trailer = soapFactory.createOMElement(FIXConstants.FIX_TRAILER, null);

        //process FIX header
        Iterator<Field<?>> iter = message.getHeader().iterator();
        if (iter != null) {
            while (iter.hasNext()) {
                Field<?> field = iter.next();
                OMElement msgField = soapFactory.createOMElement(FIXConstants.FIX_FIELD, null);
                msgField.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_FIELD_ID,
                        null, String.valueOf(field.getTag())));
                Object value = field.getObject();

                if (value instanceof byte[]) {
                    DataSource dataSource = new ByteArrayDataSource((byte[]) value);
                    DataHandler dataHandler = new DataHandler(dataSource);
                    String contentID = msgCtx.addAttachment(dataHandler);
                    OMElement binaryData = soapFactory.createOMElement(
                            FIXConstants.FIX_BINARY_FIELD, null);
                    String binaryCID = "cid:" + contentID;
                    binaryData.addAttribute(FIXConstants.FIX_MESSAGE_REFERENCE, binaryCID, null);
                    msgField.addChild(binaryData);
                } else {
                    createOMText(soapFactory, msgField, value.toString());
                }
                header.addChild(msgField);
            }
        }
        //process FIX body
        convertFIXBodyToXML(message, body, soapFactory, msgCtx);

        //process FIX trailer
        iter = message.getTrailer().iterator();
        if (iter != null) {
            while (iter.hasNext()) {
                Field<?> field = iter.next();
                OMElement msgField = soapFactory.createOMElement(FIXConstants.FIX_FIELD, null);
                msgField.addAttribute(soapFactory.
                        createOMAttribute(FIXConstants.FIX_FIELD_ID, null,
                        String.valueOf(field.getTag())));
                Object value = field.getObject();

                if (value instanceof byte[]) {
                    DataSource dataSource = new ByteArrayDataSource((byte[]) value);
                    DataHandler dataHandler = new DataHandler(dataSource);
                    String contentID = msgCtx.addAttachment(dataHandler);
                    OMElement binaryData = soapFactory.createOMElement(
                            FIXConstants.FIX_BINARY_FIELD, null);
                    String binaryCID = "cid:" + contentID;
                    binaryData.addAttribute(FIXConstants.FIX_MESSAGE_REFERENCE, binaryCID, null);
                    msgField.addChild(binaryData);
                } else {
                    createOMText(soapFactory, msgField, value.toString());
                }
                trailer.addChild(msgField);
            }
        }

        msg.addChild(header);
        msg.addChild(body);
        msg.addChild(trailer);
        SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
        envelope.getBody().addChild(msg);
        msgCtx.setEnvelope(envelope);
    }
    

    /**
     * Constructs the XML infoset for the FIX message body
     *
     * @param message the FIX message
     * @param body the body element of the XML infoset
     * @param soapFactory the SOAP factory to create XML elements
     * @param msgCtx the Axis2 Message context
     * @throws AxisFault on error
     */
    private void convertFIXBodyToXML(FieldMap message, OMElement body, SOAPFactory soapFactory,
                                        MessageContext msgCtx) throws AxisFault{

        if (log.isDebugEnabled()) {
            log.debug("Generating FIX message body (Message ID: " + msgCtx.getMessageID() + ")");
        }

        Iterator<Field<?>> iter = message.iterator();
        if (iter != null) {
             while (iter.hasNext()) {
                 Field<?> field = iter.next();
                 OMElement msgField = soapFactory.createOMElement(FIXConstants.FIX_FIELD, null);
                 msgField.addAttribute(soapFactory.
                         createOMAttribute(FIXConstants.FIX_FIELD_ID, null,
                         String.valueOf(field.getTag())));
                 Object value = field.getObject();

                 if (value instanceof byte[]) {
                     DataSource dataSource = new ByteArrayDataSource((byte[]) value);
                     DataHandler dataHandler = new DataHandler(dataSource);
                     String contentID = msgCtx.addAttachment(dataHandler);
                     OMElement binaryData = soapFactory.createOMElement(
                             FIXConstants.FIX_BINARY_FIELD, null);
                     String binaryCID = "cid:" + contentID;
                     binaryData.addAttribute(FIXConstants.FIX_MESSAGE_REFERENCE, binaryCID, null);
                     msgField.addChild(binaryData);
                 } else {
                     createOMText(soapFactory, msgField, value.toString());
                 }

                 body.addChild(msgField);
             }
        }
        
        //process FIX repeating groups
        Iterator<Integer> groupKeyItr = message.groupKeyIterator();
        if (groupKeyItr != null) {
            while (groupKeyItr.hasNext()) {
                int groupKey =  groupKeyItr.next();
                OMElement groupsField = soapFactory.createOMElement(FIXConstants.FIX_GROUPS,
                        null);
                groupsField.addAttribute(FIXConstants.FIX_FIELD_ID,
                        String.valueOf(groupKey),null);
                List<Group> groupList = message.getGroups(groupKey);
                Iterator<Group> groupIterator = groupList.iterator();

                while (groupIterator.hasNext()) {
                    Group msgGroup = groupIterator.next();
                    OMElement groupField = soapFactory.createOMElement(FIXConstants.FIX_GROUP, null);
                    // rec. call the method to process the repeating groups
                    convertFIXBodyToXML(msgGroup, groupField, soapFactory, msgCtx);
                    groupsField.addChild(groupField);
                }
                body.addChild(groupsField);
            }
        }
    }


    private void generateFIXBody(OMElement node, FieldMap message, MessageContext msgCtx,
                                 boolean withNs, String nsURI, String nsPrefix) throws IOException {

        Iterator bodyElements = node.getChildElements();
        while (bodyElements.hasNext()) {
            OMElement bodyNode = (OMElement) bodyElements.next();
            String nodeLocalName = bodyNode.getLocalName();

            //handle repeating groups
            if (nodeLocalName.equals(FIXConstants.FIX_GROUPS)){
                int groupsKey = Integer.parseInt(bodyNode.getAttributeValue(
                        new QName(FIXConstants.FIX_FIELD_ID)));
                Group group;
                Iterator groupElements = bodyNode.getChildElements();
                while (groupElements.hasNext()){
                    OMElement groupNode = (OMElement) groupElements.next();
                    Iterator groupFields = groupNode.getChildrenWithName(new QName(FIXConstants.FIX_FIELD));
                    List<Integer> idList = new ArrayList<Integer>();
                    while (groupFields.hasNext()) {
                        OMElement fieldNode = (OMElement) groupFields.next();
                        idList.add(Integer.parseInt(fieldNode.getAttributeValue(
                                new QName(FIXConstants.FIX_FIELD_ID))));
                    }

                    int[] order = new int[idList.size()];
                    for (int i = 0; i < order.length; i++) {
                        order[i] = idList.get(i);
                    }

                    group = new Group(groupsKey, order[0], order);
                    generateFIXBody(groupNode, group, msgCtx, withNs, nsURI, nsPrefix);
                    message.addGroup(group);
                }

            } else {
                String tag;
                if (withNs) {
                    tag = bodyNode.getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID,
                            nsPrefix));
                } else {
                    tag = bodyNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID));
                }

                 String value = null;
                 OMElement child = bodyNode.getFirstElement();
                 if (child != null) {
                     String href;
                     if (withNs) {
                         href = bodyNode.getFirstElement().
                                    getAttributeValue(new QName(nsURI, FIXConstants.FIX_FIELD_ID,
                                            nsPrefix)) ;
                     } else {
                         href = bodyNode.getFirstElement().
                                    getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_REFERENCE));
                     }

                     if (href != null) {
                         DataHandler binaryDataHandler = msgCtx.getAttachment(href.substring(4));
                         ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                         binaryDataHandler.writeTo(outputStream);
                         value = new String(outputStream.toByteArray());
                     }
                }
                else {
                     value = bodyNode.getText();
                }

                if (value != null) {
                    message.setString(Integer.parseInt(tag), value);
                }
            }
         }
    }


    /**
     * Extract the FIX message embedded in an Axis2 MessageContext
     *
     * @param msgCtx the Axis2 MessageContext
     * @return a FIX message
     * @throws java.io.IOException the exception thrown when handling erroneous binary content
     */
    public Message createFIXMessage(MessageContext msgCtx) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting FIX message from the message context (Message ID: " +
                    msgCtx.getMessageID() + ")");
        }

        boolean withNs = false;
        String nsPrefix = null;
        String nsURI = null;
        
        Message message = new Message();
        SOAPBody soapBody = msgCtx.getEnvelope().getBody();

        //find namespace information embedded in the FIX payload
        OMNamespace ns = getNamespaceOfFIXPayload(soapBody);
        if (ns != null) {
            withNs = true;
            nsPrefix = ns.getPrefix();
            nsURI = ns.getNamespaceURI();
        }

        OMElement messageNode;
        if (withNs) {
            messageNode = soapBody.getFirstChildWithName(new QName(nsURI, FIXConstants.FIX_MESSAGE,
                    nsPrefix));
        } else {
            messageNode = soapBody.getFirstChildWithName(new QName(FIXConstants.FIX_MESSAGE));
        }

        Iterator messageElements = messageNode.getChildElements();

        while (messageElements.hasNext()) {
            OMElement node = (OMElement) messageElements.next();
            //create FIX header
            if (node.getQName().getLocalPart().equals(FIXConstants.FIX_HEADER)) {
                Iterator headerElements = node.getChildElements();
                while (headerElements.hasNext()) {
                    OMElement headerNode = (OMElement) headerElements.next();
                    String tag;
                    if (withNs) {
                        tag = headerNode.getAttributeValue(new QName(nsURI,
                                FIXConstants.FIX_FIELD_ID, nsPrefix));
                    } else {
                        tag = headerNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID));
                    }
                    String value = null;

                    OMElement child = headerNode.getFirstElement();
                    if (child != null) {
                        String href;
                        if (withNs) {
                            href = headerNode.getFirstElement().getAttributeValue(
                                    new QName(nsURI, FIXConstants.FIX_MESSAGE_REFERENCE, nsPrefix));
                        } else {
                            href = headerNode.getFirstElement().
                                getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_REFERENCE));
                        }

                        if (href != null) {
                            DataHandler binaryDataHandler = msgCtx.getAttachment(href.substring(4));
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            binaryDataHandler.writeTo(outputStream);
                            value = new String(outputStream.toByteArray());
                        }
                    } else {
                        value = headerNode.getText();
                    }

                    if (value != null) {
                        message.getHeader().setString(Integer.parseInt(tag), value);
                    }
                }

            } else if (node.getQName().getLocalPart().equals(FIXConstants.FIX_BODY)) {
                //create FIX body
                generateFIXBody(node, message, msgCtx, withNs, nsURI, nsPrefix);
                
            } else if (node.getQName().getLocalPart().equals(FIXConstants.FIX_TRAILER)) {
                //create FIX trailer
                Iterator trailerElements = node.getChildElements();
                while (trailerElements.hasNext()) {
                    OMElement trailerNode = (OMElement) trailerElements.next();
                    String tag;
                    if (withNs) {
                        tag = trailerNode.getAttributeValue(new QName(nsURI,
                                FIXConstants.FIX_FIELD_ID, nsPrefix));
                    } else {
                        tag = trailerNode.getAttributeValue(new QName(FIXConstants.FIX_FIELD_ID));
                    }
                    String value = null;

                    OMElement child = trailerNode.getFirstElement();
                    if (child != null) {
                        String href;
                        if (withNs) {
                            href = trailerNode.getFirstElement().getAttributeValue(
                                    new QName(nsURI, FIXConstants.FIX_FIELD_ID, nsPrefix));
                        } else {
                             href = trailerNode.getFirstElement().
                                getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_REFERENCE));
                        }
                        if (href != null) {
                            DataHandler binaryDataHandler = msgCtx.getAttachment(href.substring(4));
                            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                            binaryDataHandler.writeTo(outputStream);
                            value = new String(outputStream.toByteArray());
                        }
                    } else {
                        value = trailerNode.getText();
                    }

                    if (value != null) {
                        message.getTrailer().setString(Integer.parseInt(tag), value);
                    }
                }
            }
        }
        return message;
    }


    /**
     * Generate EPRs for the specified FIX service. A FIX end point can be uniquely
     * identified by a <host(IP), port> pair. Add some additional FIX session details
     * so the EPRs are more self descriptive.
     * A FIX EPR generated here looks like;
     * fix://10.100.1.80:9898?BeginString=FIX.4.4&SenderCompID=BANZAI&TargetCompID=EXEC&
     * SessionQualifier=mySession&Serviec=StockQuoteProxy
     *
     * @param acceptor    the SocketAcceptor associated with the service
     * @param serviceName the name of the service
     * @param ip          the IP address of the host
     * @return an array of EPRs for the specified service in String format
     */
    public static String[] generateEPRs(SocketAcceptor acceptor, String serviceName, String ip) {
        //Get all the addresses associated with the acceptor
        Map<SessionID, SocketAddress> socketAddresses = acceptor.getAcceptorAddresses();
        //Get all the sessions (SessionIDs) associated with the acceptor
        ArrayList<SessionID> sessions = acceptor.getSessions();
        String[] EPRList = new String[sessions.size()];

        //Generate an EPR for each session/socket address
        for (int i = 0; i < sessions.size(); i++) {
            SessionID sessionID = sessions.get(i);
            InetSocketAddress socketAddress = (InetSocketAddress) socketAddresses.get(sessionID);
            EPRList[i] = FIXConstants.FIX_PREFIX + ip + ":" + socketAddress.getPort() +
                    "?" + FIXConstants.BEGIN_STRING + "=" + sessionID.getBeginString() +
                    "&" + FIXConstants.SENDER_COMP_ID + "=" + sessionID.getTargetCompID() +
                    "&" + FIXConstants.TARGET_COMP_ID + "=" + sessionID.getSenderCompID();

            String sessionQualifier = sessionID.getSessionQualifier();
            if (sessionQualifier != null && !sessionQualifier.equals("")) {
                EPRList[i] += "&" + FIXConstants.SESSION_QUALIFIER + "=" + sessionQualifier;
            }

            String senderSubID = sessionID.getSenderSubID();
            if (senderSubID != null && !senderSubID.equals("")) {
                EPRList[i] += "&" + FIXConstants.SENDER_SUB_ID + "=" + senderSubID;
            }

            String targetSubID = sessionID.getTargetSubID();
            if (targetSubID != null && !targetSubID.equals("")) {
                EPRList[i] += "&" + FIXConstants.TARGET_SUB_ID + "=" + targetSubID;
            }

            String senderLocationID = sessionID.getSenderLocationID();
            if (senderLocationID != null && !senderLocationID.equals("")) {
                EPRList[i] += "&" + FIXConstants.SENDER_LOCATION_ID + "=" + senderLocationID;
            }

            String targetLocationID = sessionID.getTargetLocationID();
            if (targetLocationID != null && !targetLocationID.equals("")) {
                EPRList[i] += "&" + FIXConstants.TARGET_LOCATION_ID + "=" + targetLocationID;
            }

            EPRList[i] += "&Service=" + serviceName;
        }
        return EPRList;
    }

    public static String[] getEPRs(SessionSettings settings) throws FieldConvertError, ConfigError {
        Iterator<SessionID> sessions = settings.sectionIterator();
        String[] EPRs = new String[settings.size()];
        int i = 0;
        while (sessions.hasNext()) {
            SessionID session = sessions.next();
            String EPR = FIXConstants.FIX_PREFIX;
            String paramValue;

            EPR += settings.getString(session, FIXConstants.SOCKET_CONNECT_HOST);
            EPR += ":" + settings.getString(session, FIXConstants.SOCKET_CONNECT_PORT);
            EPR += "?" + FIXConstants.BEGIN_STRING + "=";
            EPR += settings.getString(session, FIXConstants.BEGIN_STRING);
            EPR += "&" + FIXConstants.SENDER_COMP_ID + "=";
            EPR += settings.getString(session, FIXConstants.SENDER_COMP_ID);
            EPR += "&" + FIXConstants.TARGET_COMP_ID + "=";
            EPR += settings.getString(session, FIXConstants.TARGET_COMP_ID);

            try {
                paramValue = settings.getString(session, FIXConstants.SENDER_SUB_ID);
                if (paramValue != null) {
                   EPR += "&" + FIXConstants.SENDER_SUB_ID + "=";
                   EPR += paramValue;
                }
            }
            catch (ConfigError ignore) { }

            try {
                paramValue = settings.getString(session, FIXConstants.SENDER_LOCATION_ID);
                if (paramValue != null) {
                   EPR += "&" + FIXConstants.SENDER_LOCATION_ID + "=";
                   EPR += paramValue;
                }
            }
            catch (ConfigError ignore) { }

            try {
                paramValue = settings.getString(session, FIXConstants.TARGET_SUB_ID);
                if (paramValue != null) {
                   EPR += "&" + FIXConstants.TARGET_SUB_ID + "=";
                   EPR += paramValue;
                }
            }
            catch (ConfigError ignore) { }

            try {
                paramValue = settings.getString(session, FIXConstants.TARGET_LOCATION_ID);
                if (paramValue != null) {
                   EPR += "&" + FIXConstants.TARGET_LOCATION_ID + "=";
                   EPR += paramValue;
                }
            }
            catch (ConfigError ignore) { }

            EPRs[i] = EPR;
        }
        return EPRs;
    }

    /**
     * Compares two given FIX URL strings. The second URL is considered equal to the
     * first URL if all the properties in the first URL also exist in the second URL
     * and if they have equals values.
     *
     * @param url1 a FIX URL String
     * @param url2 a FIX URL String
     * @return a boolean value
     */
    public static boolean compareURLs(String url1, String url2) {
        if (!url1.substring(0, url1.indexOf("?")).equals(url2.substring(0, url2.indexOf("?")))) {
             return false;
        } else {
            Hashtable<String,String> properties1 = BaseUtils.getEPRProperties(url1);
            Hashtable<String, String> properties2 = BaseUtils.getEPRProperties(url2);
            for (Map.Entry<String,String> entry : properties1.entrySet()) {
                if (!properties2.containsKey(entry.getKey())) {
                    return false;
                } else if (!properties1.get(entry.getKey()).equals(entry.getValue())) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     * This is here because AXIOM does not support removing CDATA tags yet. Given a String embedded in
     * CDATA tags this method will return the String element only.
     *
     * @param str the String with CDATA tags
     * @return String with CDATA tags stripped
     *
    private static String removeCDATA(String str) {
        if (str.indexOf("<![CDATA[") != -1) {
            str = str.split("CDATA")[1].split("]></field>")[0];
		    str= str.substring(1, str.length()-1);
		    return str;
        } else {
            return str;
        }
    }*/

    /**
     * Extracts the fields related to message forwarding (third party routing) from
     * the FIX header.
     *
     * @param message the FIX message
     * @return a Map of forwarding parameters
     */
    public static Map<String, String> getMessageForwardingParameters(Message message) {

        Map<String, String> map = new HashMap<String, String>();
        String value = getHeaderFieldValue(message, BeginString.FIELD);
        map.put(FIXConstants.BEGIN_STRING, value);
        value = getHeaderFieldValue(message, SenderCompID.FIELD);
        map.put(FIXConstants.SENDER_COMP_ID, value);
        value = getHeaderFieldValue(message, SenderSubID.FIELD);
        map.put(FIXConstants.SENDER_SUB_ID, value);
        value = getHeaderFieldValue(message, SenderLocationID.FIELD);
        map.put(FIXConstants.SENDER_LOCATION_ID, value);
        value = getHeaderFieldValue(message, TargetCompID.FIELD);
        map.put(FIXConstants.TARGET_COMP_ID, value);
        value = getHeaderFieldValue(message, DeliverToCompID.FIELD);
        map.put(FIXConstants.DELIVER_TO_COMP_ID, value);
        value = getHeaderFieldValue(message, DeliverToSubID.FIELD);
        map.put(FIXConstants.DELIVER_TO_SUB_ID, value);
        value = getHeaderFieldValue(message, DeliverToLocationID.FIELD);
        map.put(FIXConstants.DELIVER_TO_LOCATION_ID, value);
        value = getHeaderFieldValue(message, OnBehalfOfCompID.FIELD);
        map.put(FIXConstants.ON_BEHALF_OF_COMP_ID, value);
        value = getHeaderFieldValue(message, OnBehalfOfSubID.FIELD);
        map.put(FIXConstants.ON_BEHALF_OF_SUB_ID, value);
        value = getHeaderFieldValue(message, OnBehalfOfLocationID.FIELD);
        map.put(FIXConstants.ON_BEHALF_OF_LOCATION_ID, value);
        return map;
    }

    private static String getHeaderFieldValue(Message message, int tag) {
        try {
            return message.getHeader().getString(tag);
        } catch (FieldNotFound fieldNotFound) {
            return null;
        }
    }

    /**
     * Extracts the name of the service which processed the message from the MessageContext
     *
     * @param msgCtx Axis2 MessageContext of a message
     * @return name of the AxisService
     * @throws org.apache.axis2.AxisFault on error
     */
    public static String getServiceName(MessageContext msgCtx) throws AxisFault {

        Object serviceParam = msgCtx.getProperty(FIXConstants.FIX_SERVICE_NAME);
        if (serviceParam != null) {
            String serviceName = serviceParam.toString();
            if (serviceName != null && !serviceName.equals("")) {
                return serviceName;
            }
        }

        Map trpHeaders = (Map) msgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
        //try to get the service from the transport headers
        if (trpHeaders != null) {
            String serviceName = (String) trpHeaders.get(FIXConstants.FIX_MESSAGE_SERVICE);
            if (serviceName != null) {
                return serviceName;
            }
        }
        throw new AxisFault("Unable to find a valid service for the message");
    }

    /**
     * Extracts the application type for the message from the message context
     *
     * @param msgCtx Axis2 Message Context
     * @return application type of the message
     */
    public static String getFixApplication(MessageContext msgCtx) {
        Map trpHeaders = (Map) msgCtx.getProperty(MessageContext.TRANSPORT_HEADERS);
        //try to get the application type from the transport headers
        String fixApplication = null;
        if (trpHeaders != null) {
            fixApplication = (String) trpHeaders.get(FIXConstants.FIX_MESSAGE_APPLICATION);
        }
        return fixApplication;
    }

    /**
     * Creates a Map of transport headers for a message
     *
     * @param serviceName    name of the service to which the message belongs to
     * @param fixApplication FIX application type
     * @return a Map of transport headers
     */
    public static Map<String, String> getTransportHeaders(String serviceName,
                                                          String fixApplication) {

        Map<String, String> trpHeaders = new HashMap<String, String>();
        trpHeaders.put(FIXConstants.FIX_MESSAGE_SERVICE, serviceName);
        trpHeaders.put(FIXConstants.FIX_MESSAGE_APPLICATION, fixApplication);
        return trpHeaders;
    }

    /**
     * Reads a FIX EPR and returns the host and port on a String array
     *
     * @param fixEPR a FIX EPR
     * @return an array of Strings containing addressing elements
     * @throws AxisFault on error
     */
    public static String[] getSocketAddressElements(String fixEPR) throws AxisFault {
        int propPos = fixEPR.indexOf("?");
        if (propPos != -1 && fixEPR.startsWith(FIXConstants.FIX_PREFIX)) {
            String address = fixEPR.substring(FIXConstants.FIX_PREFIX.length(), propPos);
            String[] socketAddressElemets = address.split(":");
            if (socketAddressElemets.length == 2) {
                return socketAddressElemets;
            }
        }
        throw new AxisFault("Malformed FIX EPR: " + fixEPR);
    }

    /**
     * Reads the SOAP body of a message and attempts to retreive the application level
     * sequence number
     *
     * @param msgCtx Axis2 MessageContext
     * @return application level sequence number or -1
     */
    public static int getSequenceNumber(MessageContext msgCtx) {
        int seqNum;
        SOAPBody body = msgCtx.getEnvelope().getBody();
        OMNamespace ns = getNamespaceOfFIXPayload(body);
        if (ns == null) {
            OMElement messageNode = body.getFirstChildWithName(new QName(FIXConstants.FIX_MESSAGE));
            String value = messageNode.getAttributeValue(new QName(FIXConstants.FIX_MESSAGE_COUNTER));
            if (value != null) {
                seqNum = Integer.parseInt(value);
            } else {
                seqNum = -1;
            }
        }
        else {
            seqNum = getSequenceNumber(body, ns);
        }
        return seqNum;
    }

    /**
     * Reads the SOAP body of a message and attempts to retreive the application level
     * sequence number
     *
     * @param body Body of the SOAP message
     * @param ns Namespace
     * @return application level sequence number or -1
     */
    private static int getSequenceNumber(SOAPBody body, OMNamespace ns) {
        OMElement messageNode = body.getFirstChildWithName(new QName(ns.getNamespaceURI(),
                FIXConstants.FIX_MESSAGE, ns.getPrefix()));
        String value = messageNode.getAttributeValue(new QName(ns.getNamespaceURI(),
                FIXConstants.FIX_MESSAGE_COUNTER, ns.getPrefix()));
        if (value != null) {
             return Integer.parseInt(value);
        } else {
             return -1;
        }
    }

    /**
     * Reads the SOAP body of a message and attempts to retreive the session identifier string
     *
     * @param msgCtx Axis2 MessageContext
     * @return a String uniquely identifying a session or null
     */
    public static String getSourceSession(MessageContext msgCtx) {
        String srcSession;
        SOAPBody body = msgCtx.getEnvelope().getBody();
        OMNamespace ns = getNamespaceOfFIXPayload(body);
        if (ns == null) {
            OMElement messageNode = body.getFirstChildWithName(new QName(FIXConstants.FIX_MESSAGE));
            srcSession = messageNode.getAttributeValue(new QName(
                    FIXConstants.FIX_MESSAGE_INCOMING_SESSION));
        } else {
            srcSession = getSourceSession(body, ns);
        }
        return srcSession;
    }

    /**
     * Reads the SOAP body of a message and attempts to retreive the session identifier string
     * with a namesapce
     *
     * @param body Body of the SOAP message
     * @param ns Namespace
     * @return a String uniquely identifying a session or null
     */
    private static String getSourceSession(SOAPBody body, OMNamespace ns) {
           OMElement messageNode = body.getFirstChildWithName(new QName(ns.getNamespaceURI(),
                   FIXConstants.FIX_MESSAGE, ns.getPrefix()));
           return messageNode.getAttributeValue(new QName(ns.getNamespaceURI(),
                   FIXConstants.FIX_MESSAGE_INCOMING_SESSION, ns.getPrefix()));
    }

    /**
     * Creates a text node within a CDATA section selectively by looking at the enclosing text.
     * @param soapFactory
     * @param field
     * @param text
     */
    private static void createOMText(SOAPFactory soapFactory, OMElement field, String text) {
        if (text == null) {
            return;
        }
        if (text.indexOf('<') == -1 && text.indexOf('&') == -1) {
            soapFactory.createOMText(field, text);
        } else {
            soapFactory.createOMText(field, text, OMElement.CDATA_SECTION_NODE);
        }
    }

    /**
     * Read the FIX message payload and identify the namespace if exists
     *
     * @param fixBody FIX message payload
     * @return  namespace as a OMNamespace
     */
    public static OMNamespace getNamespaceOfFIXPayload(SOAPBody fixBody){
        return fixBody.getFirstElementNS();
    }
}
