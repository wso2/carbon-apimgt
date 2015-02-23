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

package org.apache.synapse.core.axis2;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.RelatesTo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.template.InvokeMediator;
import org.apache.synapse.mediators.template.TemplateMediator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * This is the MessageContext implementation that synapse uses almost all the time because Synapse
 * is implemented on top of the Axis2
 */
public class Axis2MessageContext implements MessageContext {

    /**
     * Holds the reference to the Synapse Message Context
     */
    private SynapseConfiguration synCfg = null;

    /**
     * Holds the environment on which synapse operates
     */
    private SynapseEnvironment synEnv = null;

    /**
     * Synapse Message Context properties
     */
    private final Map<String, Object> properties = new HashMap<String, Object>();

    /**
     * Local entries fetched from the configuration or from the registry for the transactional
     * resource access
     */
    private final Map<String, Object> localEntries = new HashMap<String, Object>();

    /**
     * Fault Handler stack which will be popped and called the handleFault in error states
     */
    private final Stack<FaultHandler> faultStack = new Stack<FaultHandler>();

    /**
     * ContinuationState stack which is used to store ContinuationStates of mediation flow
     */
    private final Stack<ContinuationState> continuationStateStack = new Stack<ContinuationState>();

    /**
     * The Axis2 MessageContext reference
     */
    private org.apache.axis2.context.MessageContext axis2MessageContext = null;

    /**
     * Attribute of the MC specifying whether this is a response or not
     */
    private boolean response = false;

    /**
     * Attribute specifying whether this MC corresponds to fault response or not
     */
    private boolean faultResponse = false;

    /**
     * Attribute of MC stating the tracing state of the message
     */
    private int tracingState = SynapseConstants.TRACING_UNSET;

    /**
     * The service log for this message
     */
    private Log serviceLog = null;

    /**
     * SequenceCallStack is enabled/disabled for this message
     */
    private boolean continuationEnabled = false;

    /**
     * Position of the current mediator in execution in the sequence flow
     */
    private int mediatorPosition = 0;

    public SynapseConfiguration getConfiguration() {
        return synCfg;
    }

    public void setConfiguration(SynapseConfiguration synCfg) {
        this.synCfg = synCfg;
    }

    public SynapseEnvironment getEnvironment() {
        return synEnv;
    }

    public void setEnvironment(SynapseEnvironment synEnv) {
        this.synEnv = synEnv;
    }

    public Map<String, Object> getContextEntries() {
        return localEntries;
    }

    public void setContextEntries(Map<String, Object> entries) {
        this.localEntries.putAll(entries);
    }

    public Mediator getMainSequence() {
        Object o = localEntries.get(SynapseConstants.MAIN_SEQUENCE_KEY);
        if (o != null && o instanceof Mediator) {
            return (Mediator) o;
        } else {
            Mediator main = getConfiguration().getMainSequence();
            localEntries.put(SynapseConstants.MAIN_SEQUENCE_KEY, main);
            return main;
        }
    }

    public Mediator getFaultSequence() {
        Object o = localEntries.get(SynapseConstants.FAULT_SEQUENCE_KEY);
        if (o != null && o instanceof Mediator) {
            return (Mediator) o;
        } else {
            Mediator fault = getConfiguration().getFaultSequence();
            localEntries.put(SynapseConstants.FAULT_SEQUENCE_KEY, fault);
            return fault;
        }
    }

    public Mediator getSequence(String key) {
        Object o = localEntries.get(key);
        if (o != null && o instanceof Mediator) {
            return (Mediator) o;
        } else {
            Mediator m = getConfiguration().getSequence(key);
            if (m instanceof SequenceMediator) {
                SequenceMediator seqMediator = (SequenceMediator) m;
                synchronized (m) {
                    if (!seqMediator.isInitialized()) {
                        seqMediator.init(synEnv);
                    }
                }
            }
            localEntries.put(key, m);
            return m;
        }
    }
    
    public Mediator getDefaultConfiguration(String key){
        Object o = localEntries.get(key);
        if (o != null && o instanceof Mediator) {
            return (Mediator) o;
        } else {
            Mediator m = getConfiguration().getDefaultConfiguration(key);
            localEntries.put(key, m);
            return m;
        }
    }
    

    public OMElement getFormat(String key) {

        Object o = localEntries.get(key);
        if (o != null && o instanceof OMElement) {
            return (OMElement) o;
        } else {
            OMElement result = getConfiguration().getFormat(key);
            localEntries.put(key, result);
            return result;
        }
    }


    public Mediator getSequenceTemplate(String key) {
        Object o = localEntries.get(key);
        if (o != null && o instanceof Mediator) {
            return (Mediator) o;
        } else {
            Mediator m = getConfiguration().getSequenceTemplate(key);
            if (m instanceof TemplateMediator) {
                TemplateMediator templateMediator = (TemplateMediator) m;
                synchronized (m) {
                    if (!templateMediator.isInitialized()) {
                        templateMediator.init(synEnv);
                    }
                }
            }
            localEntries.put(key, m);
            return m;
        }
    }

    public Endpoint getEndpoint(String key) {
        Object o = localEntries.get(key);
        if (o != null && o instanceof Endpoint) {
            return (Endpoint) o;
        } else {
            Endpoint e = getConfiguration().getEndpoint(key);
            if (e != null) {
                if (!e.isInitialized()) {
                    synchronized (e) {
                        if (!e.isInitialized()) {
                            e.init(synEnv);
                        }
                    }
                }
                localEntries.put(key, e);
            }
            return e;
        }
    }

    public Object getEntry(String key) {
        Object o = localEntries.get(key);
        if (o != null && o instanceof Entry) {
            return ((Entry) o).getValue();
        } else {
            Object e = getConfiguration().getEntry(key);
            if (e != null) {
                localEntries.put(key, e);
                return e;
            } else {
                getConfiguration().getEntryDefinition(key);
                return getConfiguration().getEntry(key);
            }
        }
    }

    /**
     * Get a read-only view of all the properties currently set on this
     * message context
     *
     * @return an unmodifiable map of message context properties
     */
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        if (value == null) {
            return;
        }

        properties.put(key, value);

        // do not commit response by default in the server process
        if (SynapseConstants.RESPONSE.equals(key) &&
            getAxis2MessageContext().getOperationContext() != null) {
            getAxis2MessageContext().getOperationContext().setProperty(
                    org.apache.axis2.Constants.RESPONSE_WRITTEN, "SKIP");
        }
    }

    public Set getPropertyKeySet() {
        return properties.keySet();
    }

    /**
     * Constructor for the Axis2MessageContext inside Synapse
     *
     * @param axisMsgCtx MessageContext representing the relevant Axis MC
     * @param synCfg     SynapseConfiguraion describing Synapse
     * @param synEnv     SynapseEnvironment describing the environment of Synapse
     */
    public Axis2MessageContext(org.apache.axis2.context.MessageContext axisMsgCtx,
                               SynapseConfiguration synCfg, SynapseEnvironment synEnv) {
        setAxis2MessageContext(axisMsgCtx);
        this.synCfg = synCfg;
        this.synEnv = synEnv;
        if (synEnv != null && synEnv.isContinuationEnabled()) {
            continuationEnabled = true;
        }
    }

    public EndpointReference getFaultTo() {
        return axis2MessageContext.getFaultTo();
    }

    public void setFaultTo(EndpointReference reference) {
        axis2MessageContext.setFaultTo(reference);
    }

    public EndpointReference getFrom() {
        return axis2MessageContext.getFrom();
    }

    public void setFrom(EndpointReference reference) {
        axis2MessageContext.setFrom(reference);
    }

    public SOAPEnvelope getEnvelope() {
        return axis2MessageContext.getEnvelope();
    }

    public void setEnvelope(SOAPEnvelope envelope) throws AxisFault {
        axis2MessageContext.setEnvelope(envelope);
    }

    public String getMessageID() {
        return axis2MessageContext.getMessageID();
    }

    public void setMessageID(String string) {
        axis2MessageContext.setMessageID(string);
    }

    public RelatesTo getRelatesTo() {
        return axis2MessageContext.getRelatesTo();
    }

    public void setRelatesTo(RelatesTo[] reference) {
        axis2MessageContext.setRelationships(reference);
    }

    public EndpointReference getReplyTo() {
        return axis2MessageContext.getReplyTo();
    }

    public void setReplyTo(EndpointReference reference) {
        axis2MessageContext.setReplyTo(reference);
    }

    public EndpointReference getTo() {
        return axis2MessageContext.getTo();
    }

    public void setTo(EndpointReference reference) {
        axis2MessageContext.setTo(reference);
    }

    public void setWSAAction(String actionURI) {
        axis2MessageContext.setWSAAction(actionURI);
    }

    public String getWSAAction() {
        return axis2MessageContext.getWSAAction();
    }

    public void setWSAMessageID(String messageID) {
        axis2MessageContext.setWSAMessageId(messageID);
    }

    public String getWSAMessageID() {
        return axis2MessageContext.getMessageID();
    }

    public String getSoapAction() {
        return axis2MessageContext.getSoapAction();
    }

    public void setSoapAction(String string) {
        axis2MessageContext.setSoapAction(string);
    }

    public boolean isDoingMTOM() {
        return axis2MessageContext.isDoingMTOM();
    }

    public boolean isDoingSWA() {
        return axis2MessageContext.isDoingSwA();
    }

    public void setDoingMTOM(boolean b) {
        axis2MessageContext.setDoingMTOM(b);
    }

    public void setDoingSWA(boolean b) {
        axis2MessageContext.setDoingSwA(b);
    }

    public boolean isDoingPOX() {
        return axis2MessageContext.isDoingREST();
    }

    public void setDoingPOX(boolean b) {
        axis2MessageContext.setDoingREST(b);
    }

    public boolean isDoingGET() {
        return Constants.Configuration.HTTP_METHOD_GET.equals(
                axis2MessageContext.getProperty(Constants.Configuration.HTTP_METHOD))
               && axis2MessageContext.isDoingREST();
    }

    public void setDoingGET(boolean b) {
        if (b) {
            axis2MessageContext.setDoingREST(b);
            axis2MessageContext.setProperty(Constants.Configuration.HTTP_METHOD,
                                            Constants.Configuration.HTTP_METHOD_GET);
        } else {
            axis2MessageContext.removeProperty(Constants.Configuration.HTTP_METHOD);
        }
    }

    public boolean isSOAP11() {
        return axis2MessageContext.isSOAP11();
    }

    public void setResponse(boolean b) {
        response = b;
        axis2MessageContext.setProperty(SynapseConstants.ISRESPONSE_PROPERTY, b);
    }

    public boolean isResponse() {
        Object o = properties.get(SynapseConstants.RESPONSE);
        return o != null && o instanceof String &&
               ((String) o).equalsIgnoreCase("true") || response;
    }

    public void setFaultResponse(boolean b) {
        this.faultResponse = b;
    }

    public boolean isFaultResponse() {
        return this.faultResponse;
    }

    public int getTracingState() {
        return tracingState;
    }

    public void setTracingState(int tracingState) {
        this.tracingState = tracingState;
    }

    public Stack<FaultHandler> getFaultStack() {
        return this.faultStack;
    }

    public void pushFaultHandler(FaultHandler fault) {
        this.faultStack.push(fault);
    }

    public void pushContinuationState(ContinuationState continuationState) {
        this.continuationStateStack.push(continuationState);
    }

    public Stack<ContinuationState> getContinuationStateStack() {
        return this.continuationStateStack;
    }

    /**
     * Return the service level Log for this message context or null
     *
     * @return the service level Log for the message
     */
    public Log getServiceLog() {

        if (serviceLog != null) {
            return serviceLog;
        } else {
            String serviceName = (String) getProperty(SynapseConstants.PROXY_SERVICE);
            if (serviceName != null && synCfg.getProxyService(serviceName) != null) {
                serviceLog = LogFactory.getLog(
                        SynapseConstants.SERVICE_LOGGER_PREFIX + serviceName);
                return serviceLog;
            } else {
                serviceLog = LogFactory.getLog(
                        SynapseConstants.SERVICE_LOGGER_PREFIX.substring(0,
                                                                         SynapseConstants.SERVICE_LOGGER_PREFIX.length() - 1));
                return serviceLog;
            }
        }
    }

    /**
     * Set the service log
     *
     * @param serviceLog log to be used on a per-service basis
     */
    public void setServiceLog(Log serviceLog) {
        this.serviceLog = serviceLog;
    }

    public org.apache.axis2.context.MessageContext getAxis2MessageContext() {
        return axis2MessageContext;
    }

    public void setAxis2MessageContext(org.apache.axis2.context.MessageContext axisMsgCtx) {
        this.axis2MessageContext = axisMsgCtx;
        Boolean resp = (Boolean) axisMsgCtx.getProperty(SynapseConstants.ISRESPONSE_PROPERTY);
        if (resp != null) {
            response = resp;
        }
    }

    public void setPaused(boolean value) {
        axis2MessageContext.setPaused(value);
    }

    public boolean isPaused() {
        return axis2MessageContext.isPaused();
    }

    public boolean isServerSide() {
        return axis2MessageContext.isServerSide();
    }

    public void setServerSide(boolean value) {
        axis2MessageContext.setServerSide(value);
    }

    public String toString() {

        StringBuffer sb = new StringBuffer();
        String separator = "\n";

        if (getTo() != null) {
            sb.append("To : ").append(getTo().getAddress());
        } else {
            sb.append("To : ");
        }

        if (getFrom() != null) {
            sb.append(separator).append("From : ").append(getFrom().getAddress());
        }

        if (getWSAAction() != null) {
            sb.append(separator).append("WSAction : ").append(getWSAAction());
        }

        if (getSoapAction() != null) {
            sb.append(separator).append("SOAPAction : ").append(getSoapAction());
        }

        if (getReplyTo() != null) {
            sb.append(separator).append("ReplyTo : ").append(getReplyTo().getAddress());
        }

        if (getMessageID() != null) {
            sb.append(separator).append("MessageID : ").append(getMessageID());
        }

        SOAPHeader soapHeader = getEnvelope().getHeader();
        if (soapHeader != null) {

            sb.append(separator).append("Headers : ");
            for (Iterator iter = soapHeader.examineAllHeaderBlocks(); iter.hasNext();) {

                Object o = iter.next();
                if (o instanceof SOAPHeaderBlock) {

                    SOAPHeaderBlock headerBlock = (SOAPHeaderBlock) o;
                    sb.append(separator).append("\t").append(
                            headerBlock.getLocalName()).append(" : ").append(headerBlock.getText());

                } else if (o instanceof OMElement) {

                    OMElement headerElem = (OMElement) o;
                    sb.append(separator).append("\t").append(
                            headerElem.getLocalName()).append(" : ").append(headerElem.getText());
                }
            }
        }

        SOAPBody soapBody = getEnvelope().getBody();
        if (soapBody != null) {
            sb.append(separator).append("Body : ").append(soapBody.toString());
        }

        return sb.toString();
    }

    public boolean isContinuationEnabled() {
        return continuationEnabled;
    }

    public void setContinuationEnabled(boolean continuationEnabled) {
        this.continuationEnabled = continuationEnabled;
    }

    public void setMediatorPosition(int mediatorPosition) {
         this.mediatorPosition = mediatorPosition;
    }

    public int getMediatorPosition() {
        return mediatorPosition;
    }
}
