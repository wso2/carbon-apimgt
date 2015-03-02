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

package org.apache.synapse.mediators.builtin;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.MediatorProperty;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Logs the specified message into the configured logger. The log levels specify
 * which attributes would be logged, and is configurable. Additionally custom
 * properties may be defined to the logger, where literal values or expressions
 * could be specified for logging. The custom properties are printed into the log
 * using the defined separator (\n, "," etc)
 */
public class LogMediator extends AbstractMediator {

    /** Only properties specified to the Log mediator */
    public static final int CUSTOM  = 0;
    /** To, From, WSAction, SOAPAction, ReplyTo, MessageID and any properties */
    public static final int SIMPLE  = 1;
    /** All SOAP header blocks and any properties */
    public static final int HEADERS = 2;
    /** all attributes of level 'simple' and the SOAP envelope and any properties */
    public static final int FULL    = 3;

    public static final int CATEGORY_INFO = 0;
    public static final int CATEGORY_DEBUG = 1;
    public static final int CATEGORY_TRACE = 2;
    public static final int CATEGORY_WARN = 3;
    public static final int CATEGORY_ERROR = 4;
    public static final int CATEGORY_FATAL = 5;

    public static final String DEFAULT_SEP = ", ";

    /** The default log level is set to SIMPLE */
    private int logLevel = SIMPLE;
    /** The separator for which used to separate logging information */
    private String separator = DEFAULT_SEP;
    /** Category of the log statement */
    private int category = CATEGORY_INFO;
    /** The holder for the custom properties */
    private final List<MediatorProperty> properties = new ArrayList<MediatorProperty>();

    /**
     * Logs the current message according to the supplied semantics
     *
     * @param synCtx (current) message to be logged
     * @return true always
     */
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Log mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        switch (category) {
            case CATEGORY_INFO :
                synLog.auditLog(getLogMessage(synCtx));
                break;
            case CATEGORY_TRACE :
                synLog.auditTrace(getLogMessage(synCtx));
                break;
            case CATEGORY_DEBUG :
                synLog.auditDebug(getLogMessage(synCtx));
                break;
            case CATEGORY_WARN :
                synLog.auditWarn(getLogMessage(synCtx));
                break;
            case CATEGORY_ERROR :
                synLog.auditError(getLogMessage(synCtx));
                break;
            case CATEGORY_FATAL :
                synLog.auditFatal(getLogMessage(synCtx));
                break;
        }

        synLog.traceOrDebug("End : Log mediator");
        return true;
    }

    private String getLogMessage(MessageContext synCtx) {
        switch (logLevel) {
            case CUSTOM:
                return getCustomLogMessage(synCtx);
            case SIMPLE:
                return getSimpleLogMessage(synCtx);
            case HEADERS:
                return getHeadersLogMessage(synCtx);
            case FULL:
                return getFullLogMessage(synCtx);
            default:
                return "Invalid log level specified";
        }
    }

    private String getCustomLogMessage(MessageContext synCtx) {
        StringBuffer sb = new StringBuffer();
        setCustomProperties(sb, synCtx);
        return trimLeadingSeparator(sb);
    }

    private String getSimpleLogMessage(MessageContext synCtx) {
        StringBuffer sb = new StringBuffer();
        if (synCtx.getTo() != null)
            sb.append("To: ").append(synCtx.getTo().getAddress());
        else
            sb.append("To: ");
        if (synCtx.getFrom() != null)
            sb.append(separator).append("From: ").append(synCtx.getFrom().getAddress());
        if (synCtx.getWSAAction() != null)
            sb.append(separator).append("WSAction: ").append(synCtx.getWSAAction());
        if (synCtx.getSoapAction() != null)
            sb.append(separator).append("SOAPAction: ").append(synCtx.getSoapAction());
        if (synCtx.getReplyTo() != null)
            sb.append(separator).append("ReplyTo: ").append(synCtx.getReplyTo().getAddress());
        if (synCtx.getMessageID() != null)
            sb.append(separator).append("MessageID: ").append(synCtx.getMessageID());
        sb.append(separator).append("Direction: ").append(
                synCtx.isResponse() ? "response" : "request");
        setCustomProperties(sb, synCtx);
        return trimLeadingSeparator(sb);
    }

    private String getHeadersLogMessage(MessageContext synCtx) {
        StringBuffer sb = new StringBuffer();
        if (synCtx.getEnvelope() != null) {
            SOAPHeader header = synCtx.getEnvelope().getHeader();
            if (header != null) {
                for (Iterator iter = header.examineAllHeaderBlocks(); iter.hasNext();) {
                    Object o = iter.next();
                    if (o instanceof SOAPHeaderBlock) {
                        SOAPHeaderBlock headerBlk = (SOAPHeaderBlock) o;
                        sb.append(separator).append(headerBlk.getLocalName()).
                                append(" : ").append(headerBlk.getText());
                    } else if (o instanceof OMElement) {
                        OMElement headerElem = (OMElement) o;
                        sb.append(separator).append(headerElem.getLocalName()).
                                append(" : ").append(headerElem.getText());
                    }
                }
            }
        }
        setCustomProperties(sb, synCtx);
        return trimLeadingSeparator(sb);
    }

    private String getFullLogMessage(MessageContext synCtx) {
        StringBuffer sb = new StringBuffer();
        sb.append(getSimpleLogMessage(synCtx));
        if (synCtx.getEnvelope() != null)
            sb.append(separator).append("Envelope: ").append(synCtx.getEnvelope());
        return trimLeadingSeparator(sb);
    }

    private void setCustomProperties(StringBuffer sb, MessageContext synCtx) {
        if (properties != null && !properties.isEmpty()) {
            for (MediatorProperty property : properties) {
                if(property != null){
                sb.append(separator).append(property.getName()).append(" = ").append(property.getValue()
                        != null ? property.getValue() :
                        property.getEvaluatedExpression(synCtx));
                }
            }
        }
    }

    public int getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public void addProperty(MediatorProperty p) {
        properties.add(p);
    }

    public void addAllProperties(List<MediatorProperty> list) {
        properties.addAll(list);
    }

    public List<MediatorProperty> getProperties() {
        return properties;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        if (category > 0 && category <= 5) {
            this.category = category;
        } else {
            
        }
    }

    private String trimLeadingSeparator(StringBuffer sb) {
        String retStr = sb.toString();
        if (retStr.startsWith(separator)) {
            return retStr.substring(separator.length());
        } else {
            return retStr;
        }
    }

    @Override
    public boolean isContentAware() {
        if (logLevel == CUSTOM) {
            for (MediatorProperty property : properties) {
                if (property.getExpression() != null && property.getExpression().isContentAware()) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }
}
