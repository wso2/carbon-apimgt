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

import java.io.IOException;
import java.io.InputStream;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Message builder for syslog events.
 * <p>
 * See {@link org.apache.synapse.format.syslog} for a description of how to use
 * this class.
 */
public class SyslogMessageBuilder implements Builder {
    private static final Log log = LogFactory.getLog(SyslogMessageBuilder.class);
    
    private static final String[] facilityNames = {
        "kern", "user", "mail", "daemon", "auth", "syslog", "lpr", "news", "uucp", "cron",
        "authpriv", "ftp", "reserved0", "reserved1", "reserved2", "reserved3",
        "local0", "local1", "local2", "local3", "local4", "local5", "local6", "local7"
    };
    
    private static final String[] severityNames = {
        "emerg", "alert", "crit", "err", "warning", "notice", "info", "debug"
    };
    
    public OMElement processDocument(InputStream inputStream,
            String contentType, MessageContext messageContext) throws AxisFault {
        String facility = null;
        String severity = null;
        String tag = null;
        int pid = -1;
        String content = null;
        
        InputStreamConsumer in = new InputStreamConsumer(inputStream);
        try {
            StringBuilder buffer = new StringBuilder();
            
            in.consume('<');
            int priority = in.getInteger(3);
            if (priority > 199) {
                throw new ProtocolException("Invalid priority value (greater than 199)");
            }
            in.consume('>');
            
            severity = severityNames[priority & 7];
            facility = facilityNames[priority >> 3];
            
            int savedPosition = in.getPosition();
            try {
                outer: while (true) {
                    int next = in.next();
                    switch (next) {
                        case '[':
                            in.consume();
                            pid = in.getInteger(6);
                            in.consume(']');
                            in.expect(':');
                            // Fall through
                        case ':':
                            in.consume();
                            in.consume(' ');
                            tag = buffer.toString();
                            break outer;
                        case ' ':
                        case -1:
                            throw new ProtocolException("Unexpected end of tag");
                        default:
                            in.consume();
                            buffer.append((char)next);
                    }
                }
            }
            catch (ProtocolException ex) {
                in.setPosition(savedPosition);
            }
            
            buffer.setLength(0);
            while (true) {
                int next = in.next();
                if (next == '\n') {
                    in.consume();
                    in.consume(-1);
                    // Fall through
                } else if (next == -1) {
                    content = buffer.toString();
                    break;
                } else {
                    in.consume();
                    buffer.append((char)next);
                }
            }
        }
        catch (ProtocolException ex) {
            log.error("Protocol error: " + ex.getMessage() + " [pri=" + facility + "." +
                    severity + " tag=" + tag + " pid=" + pid + "]");
            throw new AxisFault("Protocol error", ex);
        }
        catch (IOException ex) {
            throw new AxisFault("I/O error", ex);
        }
        
        OMFactory factory = new OMLinkedListImplFactory();
        OMElement message = factory.createOMElement(SyslogConstants.MESSAGE);
        message.addAttribute(factory.createOMAttribute(SyslogConstants.FACILITY, null, facility));
        message.addAttribute(factory.createOMAttribute(SyslogConstants.SEVERITY, null, severity));
        if (tag != null) {
            message.addAttribute(factory.createOMAttribute(SyslogConstants.TAG, null, tag));
        }
        if (pid != -1) {
            message.addAttribute(factory.createOMAttribute(SyslogConstants.PID, null, String.valueOf(pid)));
        }
        message.setText(content);
        
        return message;
    }
}
