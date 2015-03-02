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

package org.apache.synapse.commons.json;

import org.apache.axiom.om.OMDataSource;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.impl.llom.soap11.SOAP11Factory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;
import org.springframework.core.io.ClassPathResource;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Util {
    private static final String[] files = {
            "json/dataSource_0.json",
            "json/twitter_huge.json"
    };

    private static final SOAPFactory factory = new SOAP11Factory();


    public static InputStream getJson(int file) throws FileNotFoundException {
        String path = null;
        try {
            path = new ClassPathResource(files[file]).getFile().getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
        FileInputStream fileInputStream = new FileInputStream(path);
        return fileInputStream;
    }

    public static InputStream getXml(int file) throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(files[file]);
        return fileInputStream;
    }

    public static SOAPEnvelope newEnvelop() {
        return null;
    }

    public static MessageContext newMessageContext() throws AxisFault {
        MessageContext messageContext = new MessageContext();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        factory.createSOAPBody(envelope);
        messageContext.setEnvelope(envelope);
        return messageContext;
    }

    public static MessageContext newMessageContext(String xmlpayload)
            throws AxisFault, XMLStreamException {
        MessageContext messageContext = new MessageContext();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        factory.createSOAPBody(envelope);
        envelope.getBody().addChild(AXIOMUtil.stringToOM(xmlpayload));
        messageContext.setEnvelope(envelope);
        return messageContext;
    }

    public static InputStream newInputStream(byte[] input) {
        return new ByteArrayInputStream(input);
    }

    public static OutputStream newOutputStream() {
        return new ByteArrayOutputStream();
    }

    public static void setAsChild(MessageContext messageContext, String xmlInput)
            throws XMLStreamException {
        messageContext.getEnvelope().getBody().addChild(AXIOMUtil.stringToOM(xmlInput));
    }

    public static MessageFormatter newJsonFormatter() {
        return new JsonFormatter();
    }

    public static MessageFormatter newJsonStreamFormatter() {
        return new JsonStreamFormatter();
    }

    public static Builder newJsonBuilder() {
        return new JsonBuilder();
    }

    public static Builder newJsonStreamBuilder() {
        return new JsonStreamBuilder();
    }

    public static OMDataSource newJsonDataSource(InputStream inputStream) {
        return new JsonDataSourceImpl(inputStream);
    }
}
