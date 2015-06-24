/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.message.store.impl.jms;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXBuilder;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.util.UUIDGenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public final class MessageConverter {
    private static final String ABSTRACT_MC_PROPERTIES = "ABSTRACT_MC_PROPERTIES";

    private static final String JMS_PRIORITY = "JMS_PRIORITY";

    //Prefix to identify a OMElemet type property
    private static final String OM_ELEMENT_PREFIX = "OM_ELEMENT_PREFIX_";

    private static final Log logger = LogFactory.getLog(MessageConverter.class.getName());

    private MessageConverter() {}

    /**
     * Converts a message read from the message store to a Synapse Message Context object.
     * @param message Message from the message store
     * @param axis2Ctx  Final Axis2 Message Context
     * @param synCtx Final Synapse message Context
     * @return Final Synapse Message Context
     */
    public static MessageContext toMessageContext(StorableMessage message,
                                                  org.apache.axis2.context.MessageContext axis2Ctx,
                                                  MessageContext synCtx) {
        if (message == null) {
            logger.error("Cannot create Message Context. Message is null.");
            return null;
        }

        AxisConfiguration axisConfig = axis2Ctx.getConfigurationContext().getAxisConfiguration();
        if (axisConfig == null) {
            logger.warn("Cannot create AxisConfiguration. AxisConfiguration is null.");
            return null;
        }
        Axis2Message axis2Msg = message.getAxis2message();
        try {
            SOAPEnvelope envelope = getSoapEnvelope(axis2Msg.getSoapEnvelope());
            axis2Ctx.setEnvelope(envelope);
            // set the RMSMessageDto properties
            axis2Ctx.getOptions().setAction(axis2Msg.getAction());
            if (axis2Msg.getRelatesToMessageId() != null) {
                axis2Ctx.addRelatesTo(new RelatesTo(axis2Msg.getRelatesToMessageId()));
            }
            axis2Ctx.setMessageID(axis2Msg.getMessageID());
            axis2Ctx.getOptions().setAction(axis2Msg.getAction());
            axis2Ctx.setDoingREST(axis2Msg.isDoingPOX());
            axis2Ctx.setDoingMTOM(axis2Msg.isDoingMTOM());
            axis2Ctx.setDoingSwA(axis2Msg.isDoingSWA());
            if (axis2Msg.getService() != null) {
                AxisService axisService = axisConfig.getServiceForActivation(axis2Msg.getService());
                AxisOperation axisOperation = axisService.getOperation(axis2Msg.getOperationName());
                axis2Ctx.setFLOW(axis2Msg.getFLOW());
                ArrayList executionChain = new ArrayList();
                if (axis2Msg.getFLOW() == org.apache.axis2.context.MessageContext.OUT_FLOW) {
                    executionChain.addAll(axisOperation.getPhasesOutFlow());
                    executionChain.addAll(axisConfig.getOutFlowPhases());
                } else if (axis2Msg.getFLOW() == org.apache.axis2.context.MessageContext.OUT_FAULT_FLOW) {
                    executionChain.addAll(axisOperation.getPhasesOutFaultFlow());
                    executionChain.addAll(axisConfig.getOutFlowPhases());
                }
                axis2Ctx.setExecutionChain(executionChain);
                ConfigurationContext configurationContext = axis2Ctx.getConfigurationContext();
                axis2Ctx.setAxisService(axisService);
                ServiceGroupContext serviceGroupContext = configurationContext
                        .createServiceGroupContext(axisService.getAxisServiceGroup());
                ServiceContext serviceContext = serviceGroupContext.getServiceContext(axisService);
                OperationContext operationContext = serviceContext
                        .createOperationContext(axis2Msg.getOperationName());
                axis2Ctx.setServiceContext(serviceContext);
                axis2Ctx.setOperationContext(operationContext);
                axis2Ctx.setAxisService(axisService);
                axis2Ctx.setAxisOperation(axisOperation);
            }
            if (axis2Msg.getReplyToAddress() != null) {
                axis2Ctx.setReplyTo(new EndpointReference(axis2Msg.getReplyToAddress().trim()));
            }
            if (axis2Msg.getFaultToAddress() != null) {
                axis2Ctx.setFaultTo(new EndpointReference(axis2Msg.getFaultToAddress().trim()));
            }
            if (axis2Msg.getFromAddress() != null) {
                axis2Ctx.setFrom(new EndpointReference(axis2Msg.getFromAddress().trim()));
            }
            if (axis2Msg.getToAddress() != null) {
                axis2Ctx.getOptions().setTo(new EndpointReference(axis2Msg.getToAddress().trim()));
            }
            Object map = axis2Msg.getProperties().get(ABSTRACT_MC_PROPERTIES);
            axis2Msg.getProperties().remove(ABSTRACT_MC_PROPERTIES);
            axis2Ctx.setProperties(axis2Msg.getProperties());
            axis2Ctx.setTransportIn(
                    axisConfig.getTransportIn(axis2Msg.getTransportInName()));
            axis2Ctx.setTransportOut(
                    axisConfig.getTransportOut(axis2Msg.getTransportOutName()));
            Object headers = axis2Msg.getProperties()
                    .get(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            if (headers instanceof Map) {
                setTransportHeaders(axis2Ctx, (Map) headers);
            }
            if (map instanceof Map) {
                Map<String, Object> abstractMCProperties = (Map) map;
                Iterator<String> properties = abstractMCProperties.keySet().iterator();
                while (properties.hasNext()) {
                    String property = properties.next();
                    Object value = abstractMCProperties.get(property);
                    axis2Ctx.setProperty(property, value);
                }
            }
            // axis2Ctx.setEnvelope(envelope);
            // XXX: always this section must come after the above step. ie. after applying Envelope.
            // That is to get the existing headers into the new envelope.
            if (axis2Msg.getJsonStream() != null) {
                JsonUtil.newJsonPayload(axis2Ctx,
                        new ByteArrayInputStream(axis2Msg.getJsonStream()), true, true);
            }
            SynapseMessage synMsg = message.getSynapseMessage();
            synCtx.setTracingState(synMsg.getTracingState());
            Iterator<String> properties = synMsg.getProperties().keySet().iterator();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = synMsg.getProperties().get(key);
                synCtx.setProperty(key, value);
            }
            Iterator<String> propertyObjects = synMsg.getPropertyObjects().keySet().iterator();
            while (propertyObjects.hasNext()) {
                String key = propertyObjects.next();
                Object value = synMsg.getPropertyObjects().get(key);
                if(key.startsWith(OM_ELEMENT_PREFIX)){
                    String originalKey = key.substring(OM_ELEMENT_PREFIX.length(),key.length());
                    ByteArrayInputStream is = new ByteArrayInputStream((byte[])value);
                    StAXOMBuilder builder = new StAXOMBuilder(is);
                    OMElement omElement = builder.getDocumentElement();
                    synCtx.setProperty(originalKey,omElement);
                }
            }

            synCtx.setFaultResponse(synMsg.isFaultResponse());
            synCtx.setResponse(synMsg.isResponse());
            return synCtx;
        } catch (Exception e) {
            logger.error("Cannot create Message Context. Error:" + e.getLocalizedMessage(), e);
            return null;
        }
    }

    /**
     * Converts a Synapse Message Context to a representation that can be stored in the
     * Message store queue.
     * @param synCtx Source Synapse message context.
     * @return Storable representation of the provided message context.
     */
    public static StorableMessage toStorableMessage(MessageContext synCtx) {
        StorableMessage message = new StorableMessage();
        Axis2Message axis2msg = new Axis2Message();
        SynapseMessage synMsg = new SynapseMessage();
        Axis2MessageContext axis2MessageContext;

        if (synCtx instanceof Axis2MessageContext) {
            axis2MessageContext = (Axis2MessageContext) synCtx;
            org.apache.axis2.context.MessageContext msgCtx =
                    axis2MessageContext.getAxis2MessageContext();
            axis2msg.setMessageID(UUIDGenerator.getUUID());
            if ( msgCtx.getAxisOperation() != null ){
                axis2msg.setOperationAction(msgCtx.getAxisOperation().getSoapAction());
                axis2msg.setOperationName(msgCtx.getAxisOperation().getName());
            }
            if (JsonUtil.hasAJsonPayload(msgCtx)) {
                axis2msg.setJsonStream(JsonUtil.jsonPayloadToByteArray(msgCtx));
            }
            axis2msg.setAction(msgCtx.getOptions().getAction());
            if (msgCtx.getAxisService() != null){
                axis2msg.setService(msgCtx.getAxisService().getName());
            }
            if (msgCtx.getRelatesTo() != null) {
                axis2msg.setRelatesToMessageId(msgCtx.getRelatesTo().getValue());
            }
            if (msgCtx.getReplyTo() != null) {
                axis2msg.setReplyToAddress(msgCtx.getReplyTo().getAddress());
            }
            if (msgCtx.getFaultTo() != null) {
                axis2msg.setFaultToAddress(msgCtx.getFaultTo().getAddress());
            }
            if (msgCtx.getTo() != null) {
                axis2msg.setToAddress(msgCtx.getTo().getAddress());
            }
            axis2msg.setDoingPOX(msgCtx.isDoingREST());
            axis2msg.setDoingMTOM(msgCtx.isDoingMTOM());
            axis2msg.setDoingSWA(msgCtx.isDoingSwA());
            String soapEnvelope = msgCtx.getEnvelope().toString();
            axis2msg.setSoapEnvelope(soapEnvelope);
            axis2msg.setFLOW(msgCtx.getFLOW());
            if (msgCtx.getTransportIn() != null) {
                axis2msg.setTransportInName(msgCtx.getTransportIn().getName());
            }
            if (msgCtx.getTransportOut() != null) {
                axis2msg.setTransportOutName(msgCtx.getTransportOut().getName());
            }
            Iterator<String> abstractMCProperties = msgCtx.getPropertyNames();
            Map<String, Object> copy = new HashMap<String, Object>(msgCtx.getProperties().size());
            while (abstractMCProperties.hasNext()) {
                String propertyName = abstractMCProperties.next();
                Object propertyValue = msgCtx.getProperty(propertyName);
                if (propertyValue instanceof String
                        || propertyValue instanceof Boolean
                        || propertyValue instanceof Integer
                        || propertyValue instanceof Double
                        || propertyValue instanceof Character) {
                    copy.put(propertyName, propertyValue);
                }
                if (JMS_PRIORITY.equals(propertyName)) {
                    if (propertyValue instanceof Integer) {
                        message.setPriority((Integer) propertyValue);
                    } else if (propertyValue instanceof  String) {
                        try {
                            int value = Integer.parseInt((String) propertyValue);
                            message.setPriority(value);
                        } catch (NumberFormatException e) {}
                    }
                }
            }
            axis2msg.addProperty(ABSTRACT_MC_PROPERTIES, copy);
            Map<String, String> transportHeaders = getTransportHeaders(msgCtx);
            axis2msg.addProperty(
                    org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
            Iterator<String> properties = msgCtx.getProperties().keySet().iterator();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = msgCtx.getProperty(key);
                if (value instanceof String) {
                    axis2msg.addProperty(key, value);
                }
            }
            message.setAxis2message(axis2msg);
            synMsg.setFaultResponse(synCtx.isFaultResponse());
            synMsg.setTracingState(synCtx.getTracingState());
            synMsg.setResponse(synCtx.isResponse());
            properties = synCtx.getPropertyKeySet().iterator();
            while (properties.hasNext()) {
                String key = properties.next();
                Object value = synCtx.getProperty(key);
                if (value instanceof String) {
                    synMsg.addProperty(key, (String) value);
                }
                if(value instanceof ArrayList && ((ArrayList)value).get(0) instanceof OMElement) {
                    OMElement elem = ((OMElement) ((ArrayList) value).get(0));
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {
                        elem.serialize(bos);
                        byte[] bytes = bos.toByteArray();
                        synMsg.addPropertyObject(OM_ELEMENT_PREFIX + key, bytes);
                    } catch (XMLStreamException e) {
                        logger.error("Error while converting OMElement to byte array", e);
                    }
                }
            }
            message.setSynapseMessage(synMsg);
        } else {
            throw new SynapseException("Cannot store message to store.");
        }
        return message;
    }

    private static SOAPEnvelope getSoapEnvelope(String soapEnvelpe) {
        try {
            //This is a temporary fix for ESBJAVA-1157 for Andes based(QPID) Client libraries
            //Thread.currentThread().setContextClassLoader(SynapseEnvironment.class.getClassLoader());
            XMLStreamReader xmlReader = StAXUtils
                    .createXMLStreamReader(new ByteArrayInputStream(getUTF8Bytes(soapEnvelpe)));
            StAXBuilder builder = new StAXSOAPModelBuilder(xmlReader);
            SOAPEnvelope soapEnvelope = (SOAPEnvelope) builder.getDocumentElement();
            soapEnvelope.build();
            String soapNamespace = soapEnvelope.getNamespace().getNamespaceURI();
            if (soapEnvelope.getHeader() == null) {
                SOAPFactory soapFactory;
                if (soapNamespace.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                    soapFactory = OMAbstractFactory.getSOAP12Factory();
                } else {
                    soapFactory = OMAbstractFactory.getSOAP11Factory();
                }
                soapFactory.createSOAPHeader(soapEnvelope);
            }
            return soapEnvelope;
        } catch (XMLStreamException e) {
            logger.error("Cannot create SOAP Envelop. Error:" + e.getLocalizedMessage(), e);
            return null;
        }
    }

	private static byte[] getUTF8Bytes(String soapEnvelpe) {
		byte[] bytes;
		try {
			bytes = soapEnvelpe.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Unable to extract bytes in UTF-8 encoding. "
					+ "Extracting bytes in the system default encoding"
					+ e.getMessage());
			bytes = soapEnvelpe.getBytes();
		}
		return bytes;
	}

    private static final class Replace {
        public String CHAR;
        public String STRING;

        public Replace(String c, String string) {
            CHAR = c;
            STRING = string;
        }
    }

    /** Replaced Strings */
    private static final Replace RS_HYPHEN = new Replace("-", "__HYPHEN__");
    private static final Replace RS_EQUAL = new Replace("=", "__EQUAL__");
    private static final Replace RS_SLASH = new Replace("/", "__SLASH__");
    private static final Replace RS_COMMA = new Replace(",", "__COMMA__");
    private static final Replace RS_SPACE = new Replace(" ", "__SPACE__");
    private static final Replace RS_COLON = new Replace(":", "__COLON__");
    private static final Replace RS_SEMICOLON = new Replace(";", "__SEMICOLON__");

    private static Map<String, String> getTransportHeaders(org.apache.axis2.context.MessageContext messageContext) {
        Object headers = messageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (!(headers instanceof Map)) {
            return Collections.emptyMap();
        }
        Map<String, String> httpHeaders = new TreeMap<String, String>();
        Iterator<Map.Entry> i = ((Map) headers).entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry headerStr = i.next();
            String fieldName = String.valueOf(headerStr.getKey());
            String fieldValue = String.valueOf(headerStr.getValue());
            fieldName = fieldName.replaceAll(RS_HYPHEN.CHAR, RS_HYPHEN.STRING);
            fieldValue = fieldValue.replaceAll(RS_HYPHEN.CHAR, RS_HYPHEN.STRING);
            fieldValue = fieldValue.replaceAll(RS_EQUAL.CHAR, RS_EQUAL.STRING);
            fieldValue = fieldValue.replaceAll(RS_SLASH.CHAR, RS_SLASH.STRING);
            fieldValue = fieldValue.replaceAll(RS_COMMA.CHAR, RS_COMMA.STRING);
            fieldValue = fieldValue.replaceAll(RS_SPACE.CHAR, RS_SPACE.STRING);
            fieldValue = fieldValue.replaceAll(RS_COLON.CHAR, RS_COLON.STRING);
            fieldValue = fieldValue.replaceAll(RS_SEMICOLON.CHAR, RS_SEMICOLON.STRING);
            httpHeaders.put(fieldName, fieldValue);
        }
        return httpHeaders;
    }

    private static void setTransportHeaders(org.apache.axis2.context.MessageContext msgCtx, Map headers) {
        if (headers == null || msgCtx == null) {
            return;
        }
        Map<String, String> httpHeaders = new TreeMap<String, String>();
        Iterator<Map.Entry> i = headers.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry headerEntry = i.next();
            String fieldName = (String) headerEntry.getKey();
            fieldName = fieldName.replaceAll(RS_HYPHEN.STRING, RS_HYPHEN.CHAR);
            String fieldValue = (String) headerEntry.getValue();
            fieldValue = fieldValue.replaceAll(RS_HYPHEN.STRING, RS_HYPHEN.CHAR);
            fieldValue = fieldValue.replaceAll(RS_EQUAL.STRING, RS_EQUAL.CHAR);
            fieldValue = fieldValue.replaceAll(RS_SLASH.STRING, RS_SLASH.CHAR);
            fieldValue = fieldValue.replaceAll(RS_COMMA.STRING, RS_COMMA.CHAR);
            fieldValue = fieldValue.replaceAll(RS_SPACE.STRING, RS_SPACE.CHAR);
            fieldValue = fieldValue.replaceAll(RS_COLON.STRING, RS_COLON.CHAR);
            fieldValue = fieldValue.replaceAll(RS_SEMICOLON.STRING, RS_SEMICOLON.CHAR);
            httpHeaders.put(fieldName, fieldValue);
        }
        msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, httpHeaders);
    }
}
