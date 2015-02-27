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
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.AbstractEndpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.DefaultEndpoint;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.message.senders.blocking.BlockingMsgSender;
import org.apache.synapse.util.MessageHelper;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <callout serviceURL="string" | endpointKey="string" [action="string"] [initAxis2ClientOptions="boolean"]>
 *      <configuration [axis2xml="string"] [repository="string"]/>?
 *      <endpoint/>?
 *      <source xpath="expression" | key="string" | type="envelope">? <!-- key can be a MC property or entry key -->
 *      <target xpath="expression" | key="string"/>?
 *      <enableSec policy="string" | outboundPolicy="String" | inboundPolicy="String"/>?
 * </callout>
 */
public class CalloutMediator extends AbstractMediator implements ManagedLifecycle {

    private ConfigurationContext configCtx = null;
    private String serviceURL = null;
    private String action = null;
    private String requestKey = null;
    private SynapseXPath requestXPath = null;
    private SynapseXPath targetXPath = null;
    private String targetKey = null;
    private String clientRepository = null;
    private String axis2xml = null;
    private String useServerConfig = null;
    private boolean initClientOptions = true;
    private Endpoint endpoint;
    private String endpointKey = null;
    private boolean useEnvelopeAsSource = false;
    private boolean securityOn = false;  //Should messages be sent using WS-Security?
    private String wsSecPolicyKey = null;
    private String inboundWsSecPolicyKey = null;
    private String outboundWsSecPolicyKey = null;
    public final static String DEFAULT_CLIENT_REPO = "./repository/deployment/client";
    public final static String DEFAULT_AXIS2_XML = "./repository/conf/axis2/axis2_blocking_client.xml";
    private boolean isWrappingEndpointCreated = false;

    BlockingMsgSender blockingMsgSender = null;

    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : Callout mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + synCtx.getEnvelope());
            }
        }

        try {

            if (!initClientOptions) {
                blockingMsgSender.setInitClientOptions(false);
            }

            if (endpointKey != null) {
                endpoint = synCtx.getEndpoint(endpointKey);
            }

            if (synLog.isTraceOrDebugEnabled()) {
                if (!isWrappingEndpointCreated) {
                    synLog.traceOrDebug("Using the defined endpoint : " + endpoint.getName());
                } else {
                    if (serviceURL != null) {
                        synLog.traceOrDebug("Using the serviceURL : " + serviceURL);
                    } else {
                        synLog.traceOrDebug("Using the To header as the EPR ");
                    }
                    if (securityOn) {
                        synLog.traceOrDebug("Security enabled within the Callout Mediator config");
                        if (wsSecPolicyKey != null) {
                            synLog.traceOrDebug("Using security policy key : " + wsSecPolicyKey);
                        } else {
                            if (inboundWsSecPolicyKey != null) {
                                synLog.traceOrDebug("Using inbound security policy key : " + inboundWsSecPolicyKey);
                            }
                            if (outboundWsSecPolicyKey != null) {
                                synLog.traceOrDebug("Using outbound security policy key : " + outboundWsSecPolicyKey);
                            }
                        }
                    }
                }
            }

            if (isWrappingEndpointCreated) {
                org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                if (Constants.VALUE_TRUE.equals(axis2MsgCtx.getProperty(Constants.Configuration.ENABLE_MTOM))) {
                    ((AbstractEndpoint) endpoint).getDefinition().setUseMTOM(true);
                }
            }

            MessageContext synapseOutMsgCtx = MessageHelper.cloneMessageContext(synCtx);
            if (!useEnvelopeAsSource
            		// if the payload is JSON, we do not consider the request (ie. source) path. Instead, we use the complete payload.
            		&& !JsonUtil.hasAJsonPayload(((Axis2MessageContext) synapseOutMsgCtx).getAxis2MessageContext())) {
                SOAPBody soapBody = synapseOutMsgCtx.getEnvelope().getBody();
                for (Iterator itr = soapBody.getChildElements(); itr.hasNext(); ) {
                    OMElement child = (OMElement) itr.next();
                    child.detach();
                }
                soapBody.addChild(getRequestPayload(synCtx));
            }

            if (action != null) {
                synapseOutMsgCtx.setWSAAction(action);
            }

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("About to invoke the service");
                if (synLog.isTraceTraceEnabled()) {
                    synLog.traceTrace("Request message payload : " + synapseOutMsgCtx.getEnvelope());
                }
            }

            MessageContext resultMsgCtx = null;
            try {
                if ("true".equals(synCtx.getProperty(SynapseConstants.OUT_ONLY))) {
                    blockingMsgSender.send(endpoint, synapseOutMsgCtx);
                } else {
                    resultMsgCtx = blockingMsgSender.send(endpoint, synapseOutMsgCtx);
                    if ("true".equals(resultMsgCtx.getProperty(SynapseConstants.BLOCKING_SENDER_ERROR))) {
                        handleFault(synCtx, (Exception) synCtx.getProperty(SynapseConstants.ERROR_EXCEPTION));
                    }
                }
            } catch (Exception ex) {
                handleFault(synCtx, ex);
            }

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Response payload received : " + resultMsgCtx.getEnvelope());
            }

            if (resultMsgCtx != null) {
                org.apache.axis2.context.MessageContext mc = ((Axis2MessageContext) resultMsgCtx).getAxis2MessageContext();
                if (JsonUtil.hasAJsonPayload(mc)) {
                    JsonUtil.cloneJsonPayload(mc, ((Axis2MessageContext) synCtx).getAxis2MessageContext());
                } else {
                    if (targetXPath != null) {
                        Object o = targetXPath.evaluate(synCtx);
                        OMElement result = resultMsgCtx.getEnvelope().getBody().getFirstElement();
                        if (o != null && o instanceof OMElement) {
                            OMNode tgtNode = (OMElement) o;
                            tgtNode.insertSiblingAfter(result);
                            tgtNode.detach();
                        } else if (o != null && o instanceof List && !((List) o).isEmpty()) {
                            // Always fetches *only* the first
                            OMNode tgtNode = (OMElement) ((List) o).get(0);
                            tgtNode.insertSiblingAfter(result);
                            tgtNode.detach();
                        } else {
                            handleException("Evaluation of target XPath expression : " +
                                    targetXPath.toString() + " did not yeild an OMNode", synCtx);
                        }
                    } else if (targetKey != null) {
                        OMElement result = resultMsgCtx.getEnvelope().getBody().getFirstElement();
                        synCtx.setProperty(targetKey, result);
                    } else {
                    	synCtx.setEnvelope(resultMsgCtx.getEnvelope());
                    }
                }
            } else {
                synLog.traceOrDebug("Service returned a null response");
            }

        } catch (AxisFault e) {
            handleException("Error invoking service : " + serviceURL +
                            (action != null ? " with action : " + action : ""), e, synCtx);
        } catch (JaxenException e) {
            handleException("Error while evaluating the XPath expression: " + targetXPath,
                            e, synCtx);
        }

        synLog.traceOrDebug("End : Callout mediator");
        return true;
    }

    private void handleFault(MessageContext synCtx, Exception ex) {
        synCtx.setProperty(SynapseConstants.SENDING_FAULT, Boolean.TRUE);

        if (ex instanceof AxisFault) {
            AxisFault axisFault = (AxisFault) ex;

            if (axisFault.getFaultCodeElement() != null) {
                synCtx.setProperty(SynapseConstants.ERROR_CODE,
                                   axisFault.getFaultCodeElement().getText());
            } else {
                synCtx.setProperty(SynapseConstants.ERROR_CODE,
                                   SynapseConstants.CALLOUT_OPERATION_FAILED);
            }

            if (axisFault.getMessage() != null) {
                synCtx.setProperty(SynapseConstants.ERROR_MESSAGE,
                                   axisFault.getMessage());
            } else {
                synCtx.setProperty(SynapseConstants.ERROR_MESSAGE, "Error while performing " +
                                                                   "the callout operation");
            }

            if (axisFault.getFaultDetailElement() != null) {
                if (axisFault.getFaultDetailElement().getFirstElement() != null) {
                    synCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                                       axisFault.getFaultDetailElement().getFirstElement());
                } else {
                    synCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                                       axisFault.getFaultDetailElement().getText());
                }
            }
        }

        synCtx.setProperty(SynapseConstants.ERROR_EXCEPTION, ex);
        throw new SynapseException("Error while performing the callout operation", ex);
    }

    private OMElement getRequestPayload(MessageContext synCtx) throws AxisFault {

        if (requestKey != null) {
            Object request = synCtx.getProperty(requestKey);
            if (request == null) {
                request = synCtx.getEntry(requestKey);
            }
            if (request != null && request instanceof OMElement) {
                return (OMElement) request;
            } else {
                handleException("The property : " + requestKey + " is not an OMElement", synCtx);
            }
        } else if (requestXPath != null) {
            try {
                Object o = requestXPath.evaluate(MessageHelper.cloneMessageContext(synCtx));

                if (o instanceof OMElement) {
                    return (OMElement) o;
                } else if (o instanceof List && !((List) o).isEmpty()) {
                    return (OMElement) ((List) o).get(0);  // Always fetches *only* the first
                } else {
                    handleException("The evaluation of the XPath expression : "
                                    + requestXPath.toString() + " did not result in an OMElement", synCtx);
                }
            } catch (JaxenException e) {
                handleException("Error evaluating XPath expression : "
                                + requestXPath.toString(), e, synCtx);
            }
        }
        return null;
    }

    public void init(SynapseEnvironment synEnv) {
        try {
            configCtx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                    clientRepository != null ? clientRepository : DEFAULT_CLIENT_REPO,
                    axis2xml != null ? axis2xml : DEFAULT_AXIS2_XML);
            if (serviceURL != null) {
                serviceURL = changeEndPointReference(serviceURL);
            }

            blockingMsgSender = new BlockingMsgSender();
            blockingMsgSender.setConfigurationContext(configCtx);
            blockingMsgSender.init();

            EndpointDefinition endpointDefinition = null;

            if (serviceURL != null) {
                // If Service URL is specified, it is given the highest priority
                endpoint = new AddressEndpoint();
                endpointDefinition = new EndpointDefinition();
                endpointDefinition.setAddress(serviceURL);
                ((AddressEndpoint) endpoint).setDefinition(endpointDefinition);
                isWrappingEndpointCreated = true;
            } else if (endpoint == null && endpointKey == null) {
                // Use a default endpoint in this case - i.e. the To header
                endpoint = new DefaultEndpoint();
                endpointDefinition = new EndpointDefinition();
                ((DefaultEndpoint) endpoint).setDefinition(endpointDefinition);
                isWrappingEndpointCreated = true;
            }
            // If the endpoint is specified, we'll look it up at mediation time.

            if (endpointDefinition != null && isSecurityOn()) {
                endpointDefinition.setSecurityOn(true);
                if (wsSecPolicyKey != null) {
                    endpointDefinition.setWsSecPolicyKey(wsSecPolicyKey);
                } else {
                    if (inboundWsSecPolicyKey != null) {
                        endpointDefinition.setInboundWsSecPolicyKey(inboundWsSecPolicyKey);
                    }
                    if (outboundWsSecPolicyKey != null) {
                        endpointDefinition.setOutboundWsSecPolicyKey(outboundWsSecPolicyKey);
                    }
                }
            }
        } catch (AxisFault e) {
            String msg = "Error initializing callout mediator : " + e.getMessage();
            log.error(msg, e);
            throw new SynapseException(msg, e);
        }
    }


    public void destroy() {
        try {
            configCtx.terminate();
        } catch (AxisFault ignore) {}
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getUseServerConfig() {
        return useServerConfig;
    }

    public void setUseServerConfig(String useServerConfig) {
        this.useServerConfig = useServerConfig;
    }

    public String getRequestKey() {
        return requestKey;
    }

    public void setRequestKey(String requestKey) {
        this.requestKey = requestKey;
    }

    public void setRequestXPath(SynapseXPath requestXPath) throws JaxenException {
        this.requestXPath = requestXPath;
    }

    public void setTargetXPath(SynapseXPath targetXPath) throws JaxenException {
        this.targetXPath = targetXPath;
    }

    public String getTargetKey() {
        return targetKey;
    }

    public void setTargetKey(String targetKey) {
        this.targetKey = targetKey;
    }

    public SynapseXPath getRequestXPath() {
        return requestXPath;
    }

    public SynapseXPath getTargetXPath() {
        return targetXPath;
    }

    public String getClientRepository() {
        return clientRepository;
    }

    public void setClientRepository(String clientRepository) {
        this.clientRepository = clientRepository;
    }

    public String getAxis2xml() {
        return axis2xml;
    }

    public void setAxis2xml(String axis2xml) {
        this.axis2xml = axis2xml;
    }

    public void setEndpointKey(String key) {
        this.endpointKey = key;
    }

    public String getEndpointKey() {
        return endpointKey;
    }

    public boolean getInitClientOptions() {
        return initClientOptions;
    }

    public void setInitClientOptions(boolean initClientOptions) {
        this.initClientOptions = initClientOptions;
    }

    public boolean isUseEnvelopeAsSource() {
        return useEnvelopeAsSource;
    }

    public void setUseEnvelopeAsSource(boolean useEnvelopeAsSource) {
        this.useEnvelopeAsSource = useEnvelopeAsSource;
    }

    /**
     * Is WS-Security turned on on this endpoint?
     *
     * @return true if on
     */
    public boolean isSecurityOn() {
        return securityOn;
    }

    /**
     * Request that WS-Sec be turned on/off on this endpoint
     *
     * @param securityOn  a boolean flag indicating security is on or not
     */
    public void setSecurityOn(boolean securityOn) {
        this.securityOn = securityOn;
    }

    /**
     * Return the Rampart Security configuration policys' 'key' to be used (See Rampart)
     *
     * @return the Rampart Security configuration policys' 'key' to be used (See Rampart)
     */
    public String getWsSecPolicyKey() {
        return wsSecPolicyKey;
    }

    /**
     * Set the Rampart Security configuration policys' 'key' to be used (See Rampart)
     *
     * @param wsSecPolicyKey the Rampart Security configuration policys' 'key' to be used
     */
    public void setWsSecPolicyKey(String wsSecPolicyKey) {
        this.wsSecPolicyKey = wsSecPolicyKey;
    }

    /**
     * Get the outbound security policy key. This is used when we specify different policies for
     * inbound and outbound.
     *
     * @return outbound security policy key
     */
    public String getOutboundWsSecPolicyKey() {
        return outboundWsSecPolicyKey;
    }

    /**
     * Set the outbound security policy key.This is used when we specify different policies for
     * inbound and outbound.
     *
     * @param outboundWsSecPolicyKey outbound security policy key.
     */
    public void setOutboundWsSecPolicyKey(String outboundWsSecPolicyKey) {
        this.outboundWsSecPolicyKey = outboundWsSecPolicyKey;
    }

    /**
     * Get the inbound security policy key. This is used when we specify different policies for
     * inbound and outbound.
     *
     * @return inbound security policy key
     */
    public String getInboundWsSecPolicyKey() {
        return inboundWsSecPolicyKey;
    }

    /**
     * Set the inbound security policy key. This is used when we specify different policies for
     * inbound and outbound.
     * @param inboundWsSecPolicyKey inbound security policy key.
     */
    public void setInboundWsSecPolicyKey(String inboundWsSecPolicyKey) {
        this.inboundWsSecPolicyKey = inboundWsSecPolicyKey;
    }


    /**
     * This method checks for dynamic url in callout medaitor and replace it with given system properties.
     * properties has to given as -D{parameter}={value}
     * @param epr end point url
     * @return fixed end point url
     */
    private String changeEndPointReference(String epr) {

        if (epr.toLowerCase().contains("system.prop")) {
            Pattern p = Pattern.compile("\\{(.*?)\\}");
            Matcher m = p.matcher(epr);
            Map<String, String> result = new HashMap<String, String>();
            while (m.find()) {
                result.put(m.group(1), "");
                String propName = System.getProperty(m.group(1).replace("system.prop.", ""));
                if (propName != null) {
                    epr = epr.replace("{" + m.group(1) + "}", propName);
                } else {
                    log.warn("System property is not initialized");
                }
            }
            log.info("Dynamic properties of url are replaced");
        }

        return epr;
    }

    /**
     * Get the defined endpoint
     *
     * @return endpoint
     */
    public Endpoint getEndpoint() {
        if (!isWrappingEndpointCreated) {
            return endpoint;
        }
        return null;
    }

    /**
     * Set the defined endpoint
     *
     * @param endpoint defined endpoint
     */
    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

}
