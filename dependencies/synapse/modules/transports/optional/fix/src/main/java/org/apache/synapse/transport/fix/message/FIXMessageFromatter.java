/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.synapse.transport.fix.message;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.fix.FIXUtils;

/**
 * Reads the incoming message context and convert them back to the fix raw
 * message
 * 
 */
public class FIXMessageFromatter implements MessageFormatter {

	private Log log = LogFactory.getLog(FIXMessageFromatter.class);

	public String formatSOAPAction(MessageContext arg0, OMOutputFormat arg1, String arg2) {
		return null;
	}

	public byte[] getBytes(MessageContext arg0, OMOutputFormat arg1) throws AxisFault {

		return null;
	}

	public String getContentType(MessageContext msgCtxt, OMOutputFormat format, String soapActionString) {
		String contentType = (String) msgCtxt.getProperty(Constants.Configuration.CONTENT_TYPE);
		String encoding = format.getCharSetEncoding();
		if (contentType == null) {
			contentType = (String) msgCtxt.getProperty(Constants.Configuration.MESSAGE_TYPE);
		}
		if (encoding != null) {
			contentType += "; charset=" + encoding;
		}
		return contentType;
	}

	public URL getTargetAddress(MessageContext arg0, OMOutputFormat arg1, URL arg2) throws AxisFault {
		return null;
	}

	/**
	 * Read the FIX message payload and identify the namespace if exists
	 * 
	 * @param fixBody
	 *            FIX message payload
	 * @return namespace as a OMNamespace
	 */
	public static OMNamespace getNamespaceOfFIXPayload(SOAPBody fixBody) {
		return fixBody.getFirstElementNS();
	}

	
	public void writeTo(MessageContext msgCtx, OMOutputFormat format, OutputStream out, boolean arg3) throws AxisFault {

		FIXUtils fixUtils = new FIXUtils();

		try {
			quickfix.Message message = fixUtils.createFIXMessage(msgCtx);
			out.write(message.toString().getBytes());
		} catch (IOException e) {
			log.error("Error while formatting FIX SOAP message", e);
			throw new AxisFault(e.getMessage());
		}

	}

}
