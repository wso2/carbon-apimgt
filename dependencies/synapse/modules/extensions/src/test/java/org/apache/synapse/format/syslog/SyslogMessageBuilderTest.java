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
package org.apache.synapse.format.syslog;

import java.io.ByteArrayInputStream;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.jaxp.OMSource;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.lang.ObjectUtils;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class SyslogMessageBuilderTest extends TestCase {
    private class SyslogMessage {
        private final String facility;
        private final String severity;
        private final String tag;
        private final int pid;
        private final String content;
        
        public SyslogMessage(String facility, String severity, String tag,
                int pid, String content) {
            this.facility = facility;
            this.severity = severity;
            this.tag = tag;
            this.pid = pid;
            this.content = content;
        }
        
        @Override
        public String toString() {
            return "[pri=" + facility + "." + severity + " tag=" + tag + " pid=" + pid + ": " + content + "]";
        }

        @Override
        public boolean equals(Object _obj) {
            if (_obj == null || !(_obj instanceof SyslogMessage)) {
                return false;
            } else {
                SyslogMessage obj = (SyslogMessage)_obj;
                return ObjectUtils.equals(facility, obj.facility) &&
                       ObjectUtils.equals(severity, obj.severity) &&
                       ObjectUtils.equals(tag, obj.tag) &&
                       pid == obj.pid &&
                       ObjectUtils.equals(content, obj.content);
            }
        }
    }
    
    private SyslogMessage test(String message) throws Exception {
        MessageContext msgContext = new MessageContext();
        ByteArrayInputStream in = new ByteArrayInputStream(message.getBytes("us-ascii"));
        OMElement element = new SyslogMessageBuilder().processDocument(in, null, msgContext);
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(SyslogMessageBuilderTest.class.getResource("schema.xsd").toExternalForm()));
        Validator validator = schema.newValidator();
        validator.setErrorHandler(new ErrorHandler() {
            public void error(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void fatalError(SAXParseException exception) throws SAXException {
                throw exception;
            }

            public void warning(SAXParseException exception) throws SAXException {
                throw exception;
            }
        });
        validator.validate(new OMSource(element));
        String pidString = element.getAttributeValue(new QName(SyslogConstants.PID));
        return new SyslogMessage(element.getAttributeValue(new QName(SyslogConstants.FACILITY)),
                                 element.getAttributeValue(new QName(SyslogConstants.SEVERITY)),
                                 element.getAttributeValue(new QName(SyslogConstants.TAG)),
                                 pidString == null ? -1 : Integer.parseInt(pidString),
                                 element.getText());
    }
    
    public void testTagPidContent() throws Exception {
        assertEquals(new SyslogMessage("mail", "info", "fetchmail", 8928, "awakened at Sun 04 May 2008 08:04:56 PM CEST"),
                     test("<22>fetchmail[8928]: awakened at Sun 04 May 2008 08:04:56 PM CEST\n"));
    }
    
    public void testTagContent() throws Exception {
        assertEquals(new SyslogMessage("local3", "info", "logger", -1, "test"),
                     test("<158>logger: test\n"));
    }
    
    public void testContent() throws Exception {
        assertEquals(new SyslogMessage("syslog", "info", null, -1, "exiting on signal 15."),
                     test("<46>exiting on signal 15.\n"));
    }
}
