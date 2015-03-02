/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
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
package org.apache.synapse.transport.fix.message;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import quickfix.DataDictionary;
import quickfix.DefaultDataDictionaryProvider;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.InvalidMessage;
import quickfix.MessageUtils;
import quickfix.field.BeginString;

/**
 * Fix message builder prepares a payload based on the incoming raw fix message
 * read from the
 * destination,the implementation only focusing the building the message
 * context, there will be
 * limitations such as when build message there wont be fix session attribute
 * involved and the assumption
 * is that the fix client and executor has the responsibilities of managing fix
 * session accordingly
 * 
 */
public class FIXMessageBuilder implements Builder {

	private static final Log log = LogFactory.getLog(FIXMessageBuilder.class);

	public OMElement processDocument(InputStream inputStream, String contentType, MessageContext messageContext) throws AxisFault {
		Reader reader = null;
		StringBuilder messageString = new StringBuilder();
		quickfix.Message message = null;
		try {
			String charSetEncoding = (String) messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
			if (charSetEncoding == null) {
				charSetEncoding = MessageContext.DEFAULT_CHAR_SET_ENCODING;
			}
			reader = new InputStreamReader(inputStream, charSetEncoding);
			try {
				int data = reader.read();
				while (data != -1) {
					char dataChar = (char) data;
					data = reader.read();
					messageString.append(dataChar);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.error("Error In creating FIX SOAP envelope ...", e);
				throw new AxisFault(e.getMessage());
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("Error In creating FIX SOAP envelope ...", e);
			throw new AxisFault(e.getMessage());
		}

		try {
			DefaultDataDictionaryProvider dataDictionary = new DefaultDataDictionaryProvider();
			String beginString = MessageUtils.getStringField(messageString.toString(), BeginString.FIELD);
			DataDictionary dataDic = dataDictionary.getSessionDataDictionary(beginString);
			message = new quickfix.Message(messageString.toString(), null, false);
		} catch (InvalidMessage e) {
			// TODO Auto-generated catch block
			log.error("Error In creating FIX SOAP envelope ...", e);
			throw new AxisFault(e.getMessage());
		}

		if (log.isDebugEnabled()) {
			log.debug("Creating SOAP envelope for FIX message...");
		}

		SOAPFactory soapFactory = new SOAP11Factory();
		OMElement msg = soapFactory.createOMElement(FIXConstants.FIX_MESSAGE, null);
		msg.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_MESSAGE_INCOMING_SESSION, null, ""));
		msg.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_MESSAGE_COUNTER, null, String.valueOf("-1")));

		OMElement header = soapFactory.createOMElement(FIXConstants.FIX_HEADER, null);
		OMElement body = soapFactory.createOMElement(FIXConstants.FIX_BODY, null);
		OMElement trailer = soapFactory.createOMElement(FIXConstants.FIX_TRAILER, null);

		// process FIX header
		Iterator<quickfix.Field<?>> iter = message.getHeader().iterator();
		if (iter != null) {
			while (iter.hasNext()) {
				quickfix.Field<?> field = iter.next();
				OMElement msgField = soapFactory.createOMElement(FIXConstants.FIX_FIELD, null);
				msgField.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_FIELD_ID, null, String.valueOf(field.getTag())));
				Object value = field.getObject();

				if (value instanceof byte[]) {
					DataSource dataSource = new ByteArrayDataSource((byte[]) value);
					DataHandler dataHandler = new DataHandler(dataSource);
					String contentID = messageContext.addAttachment(dataHandler);
					OMElement binaryData = soapFactory.createOMElement(FIXConstants.FIX_BINARY_FIELD, null);
					String binaryCID = "cid:" + contentID;
					binaryData.addAttribute(FIXConstants.FIX_MESSAGE_REFERENCE, binaryCID, null);
					msgField.addChild(binaryData);
				} else {
					soapFactory.createOMText(msgField, value.toString(), OMElement.CDATA_SECTION_NODE);
				}
				header.addChild(msgField);
			}
		}
		// process FIX body
		convertFIXBodyToXML(message, body, soapFactory, messageContext);

		// process FIX trailer
		iter = message.getTrailer().iterator();
		if (iter != null) {
			while (iter.hasNext()) {
				quickfix.Field<?> field = iter.next();
				OMElement msgField = soapFactory.createOMElement(FIXConstants.FIX_FIELD, null);
				msgField.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_FIELD_ID, null, String.valueOf(field.getTag())));
				Object value = field.getObject();

				if (value instanceof byte[]) {
					DataSource dataSource = new ByteArrayDataSource((byte[]) value);
					DataHandler dataHandler = new DataHandler(dataSource);
					String contentID = messageContext.addAttachment(dataHandler);
					OMElement binaryData = soapFactory.createOMElement(FIXConstants.FIX_BINARY_FIELD, null);
					String binaryCID = "cid:" + contentID;
					binaryData.addAttribute(FIXConstants.FIX_MESSAGE_REFERENCE, binaryCID, null);
					msgField.addChild(binaryData);
				} else {
					soapFactory.createOMText(msgField, value.toString(), OMElement.CDATA_SECTION_NODE);
				}
				trailer.addChild(msgField);
			}
		}

		msg.addChild(header);
		msg.addChild(body);
		msg.addChild(trailer);
		SOAPEnvelope envelope = soapFactory.getDefaultEnvelope();
		envelope.getBody().addChild(msg);
		messageContext.setEnvelope(envelope);

		return msg;
	}

	/**
	 * Constructs the XML infoset for the FIX message body
	 * 
	 * @param message
	 *            the FIX message
	 * @param body
	 *            the body element of the XML infoset
	 * @param soapFactory
	 *            the SOAP factory to create XML elements
	 * @param msgCtx
	 *            the Axis2 Message context
	 * @throws AxisFault
	 *             on error
	 */
	private void convertFIXBodyToXML(FieldMap message, OMElement body, SOAPFactory soapFactory, MessageContext msgCtx) throws AxisFault {

		if (log.isDebugEnabled()) {
			log.debug("Generating FIX message body (Message ID: " + msgCtx.getMessageID() + ")");
		}

		Iterator<quickfix.Field<?>> iter = message.iterator();
		if (iter != null) {
			while (iter.hasNext()) {
				quickfix.Field<?> field = iter.next();
				OMElement msgField = soapFactory.createOMElement(FIXConstants.FIX_FIELD, null);
				msgField.addAttribute(soapFactory.createOMAttribute(FIXConstants.FIX_FIELD_ID, null, String.valueOf(field.getTag())));
				Object value = field.getObject();

				if (value instanceof byte[]) {
					DataSource dataSource = new ByteArrayDataSource((byte[]) value);
					DataHandler dataHandler = new DataHandler(dataSource);
					String contentID = msgCtx.addAttachment(dataHandler);
					OMElement binaryData = soapFactory.createOMElement(FIXConstants.FIX_BINARY_FIELD, null);
					String binaryCID = "cid:" + contentID;
					binaryData.addAttribute(FIXConstants.FIX_MESSAGE_REFERENCE, binaryCID, null);
					msgField.addChild(binaryData);
				} else {
					soapFactory.createOMText(msgField, value.toString(), OMElement.CDATA_SECTION_NODE);
				}

				body.addChild(msgField);
			}
		}

		// process FIX repeating groups
		Iterator<Integer> groupKeyItr = message.groupKeyIterator();
		if (groupKeyItr != null) {
			while (groupKeyItr.hasNext()) {
				int groupKey = groupKeyItr.next();
				OMElement groupsField = soapFactory.createOMElement(FIXConstants.FIX_GROUPS, null);
				groupsField.addAttribute(FIXConstants.FIX_FIELD_ID, String.valueOf(groupKey), null);
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

}
