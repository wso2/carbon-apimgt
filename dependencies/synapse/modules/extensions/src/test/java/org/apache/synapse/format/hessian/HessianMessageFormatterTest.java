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

package org.apache.synapse.format.hessian;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.SynapseConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

/**
 * Test the HessianMessageFormatter with a test message build using
 * the HessianMessageBuilder as well as a dummy SOAP fault written out,
 * read with HessianMessageBuilder and marked as a fault.
 */
public class HessianMessageFormatterTest extends TestCase {

    public void testWriteToWithMessage() throws IOException {

        String testMessageName = HessianTestHelper.HESSIAN_DUMMY_REQUEST;
        HessianTestHelper testHelper = new HessianTestHelper();
        MessageContext msgContext = testHelper.createAxis2MessageContext(null);
        OMElement element = testHelper.buildHessianTestMessage(testMessageName, msgContext);
        testHelper.addBodyToMessageContext(msgContext, element);
        byte[] originalBytes = testHelper.getTestMessageAsBytes(testMessageName);
        byte[] formatterBytes = writeTo(msgContext);

        assertEquals(originalBytes.length, formatterBytes.length);
        assertTrue(Arrays.equals(originalBytes, formatterBytes));
    }

    public void testWriteToWithSoapFault() throws IOException {

        HessianTestHelper testHelper = new HessianTestHelper();
        MessageContext faultMsgContext = testHelper.createAxis2MessageContext(null);
        testHelper.addSoapFaultToMessageContext(faultMsgContext, "500", "test", "testDetail");
        byte[] formatterBytes = writeTo(faultMsgContext);
        HessianMessageBuilder messageBuilder = new HessianMessageBuilder();
        MessageContext msgContext = testHelper.createAxis2MessageContext(null);
        messageBuilder.processDocument(
            IOUtils.toInputStream(new String(formatterBytes, HessianTestHelper.CHARSET_ENCODING)), 
            HessianConstants.HESSIAN_CONTENT_TYPE, msgContext);
        assertTrue(formatterBytes.length > 0);
        assertEquals(SynapseConstants.TRUE, msgContext.getProperty(BaseConstants.FAULT_MESSAGE));
        
    }

    public void testGetBytes() {

        try {
            HessianMessageFormatter formatter = new HessianMessageFormatter();
            OMOutputFormat format = new OMOutputFormat();
            HessianTestHelper testHelper = new HessianTestHelper();
            MessageContext msgContext = testHelper.createAxis2MessageContext(null);
            formatter.getBytes(msgContext, format);
            fail("getBytes() should have thrown an AxisFault!");
        } catch (AxisFault fault) {
            assertTrue(fault.getMessage().length() > 0);
        }
    }

    public void testGetContentType() throws AxisFault {
        HessianMessageFormatter formatter = new HessianMessageFormatter();
        OMOutputFormat format = new OMOutputFormat();
        String soapActionString = "soapAction";
        HessianTestHelper testHelper = new HessianTestHelper();
        MessageContext msgContext = testHelper.createAxis2MessageContext(null);
        String contentType = formatter.getContentType(msgContext, format, soapActionString);
        assertEquals(HessianConstants.HESSIAN_CONTENT_TYPE, contentType);

        msgContext.setProperty(Constants.Configuration.CONTENT_TYPE,
            HessianConstants.HESSIAN_CONTENT_TYPE);
        format.setCharSetEncoding(HessianTestHelper.CHARSET_ENCODING);
        contentType = formatter.getContentType(msgContext, format, soapActionString);
        String expectedContentType = HessianConstants.HESSIAN_CONTENT_TYPE + "; charset="
            + HessianTestHelper.CHARSET_ENCODING;
        assertEquals(expectedContentType, contentType);
    }

    private byte[] writeTo(MessageContext msgContext) throws IOException {

        HessianMessageFormatter formatter = new HessianMessageFormatter();
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        formatter.writeTo(msgContext, null, out, false);

        return out.toByteArray();
    }

}
