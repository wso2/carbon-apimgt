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
package org.apache.synapse.endpoints;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.LoadBalanceMembershipHandler;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.endpoints.algorithms.AlgorithmContext;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SALSessions;
import org.apache.synapse.endpoints.dispatch.SessionInformation;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

/**
 * Represents a dynamic load balance endpoint. The application membership is not static,
 * but discovered through some mechanism such as using a GCF
 */
public class DynamicLoadbalanceEndpoint extends LoadbalanceEndpoint {

    private static final Log log = LogFactory.getLog(DynamicLoadbalanceEndpoint.class);

    private static final String PORT_MAPPING_PREFIX = "port.mapping.";

    /**
     *  Flag to enable session affinity based load balancing.
     */
    protected boolean sessionAffinity = false;

    /**
     * Dispatcher used for session affinity.
     */
    protected Dispatcher dispatcher = null;

    /* Sessions time out interval*/
    protected long sessionTimeout = -1;

    /**
     * The algorithm context , place holder for keep any runtime states related to the load balance
     * algorithm
     */
    private AlgorithmContext algorithmContext;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        ConfigurationContext cc =
                ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext();
        if (!initialized) {
            super.init(synapseEnvironment);
            if (algorithmContext == null) {
                algorithmContext = new AlgorithmContext(isClusteringEnabled, cc, getName());
            }

            // Initialize the SAL Sessions if already has not been initialized.
            SALSessions salSessions = SALSessions.getInstance();
            if (!salSessions.isInitialized()) {
                salSessions.initialize(isClusteringEnabled, cc);
            }
        }
        log.info("Dynamic load balance endpoint initialized");
    }

    private LoadBalanceMembershipHandler lbMembershipHandler;

    public DynamicLoadbalanceEndpoint() {
    }

    public void setLoadBalanceMembershipHandler(LoadBalanceMembershipHandler lbMembershipHandler) {
        this.lbMembershipHandler = lbMembershipHandler;
    }

    public LoadBalanceMembershipHandler getLbMembershipHandler() {
        return lbMembershipHandler;
    }

    public void send(MessageContext synCtx) {
        SessionInformation sessionInformation = null;
        Member currentMember = null;
        //TODO Temp hack: ESB removes the session id from request in a random manner.
        setCookieHeader(synCtx);

        ConfigurationContext configCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext().getConfigurationContext();
        if (lbMembershipHandler.getConfigurationContext() == null) {
            lbMembershipHandler.setConfigurationContext(configCtx);
        }

        if (isSessionAffinityBasedLB()) {
            // first check if this session is associated with a session. if so, get the endpoint
            // associated for that session.
            sessionInformation =
                    (SessionInformation) synCtx.getProperty(
                            SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);

            currentMember = (Member) synCtx.getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER);

            if (sessionInformation == null && currentMember == null) {
                sessionInformation = dispatcher.getSession(synCtx);
                if (sessionInformation != null) {

                    if (log.isDebugEnabled()) {
                        log.debug("Current session id : " + sessionInformation.getId());
                    }

                    currentMember = sessionInformation.getMember();
                    synCtx.setProperty(
                            SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER, currentMember);
                    // This is for reliably recovery any session information if while response is getting ,
                    // session information has been removed by cleaner.
                    // This will not be a cost as  session information a not heavy data structure
                    synCtx.setProperty(
                            SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION, sessionInformation);
                }
            }

        }


        DynamicLoadbalanceFaultHandlerImpl faultHandler = new DynamicLoadbalanceFaultHandlerImpl();
        if (sessionInformation != null && currentMember != null) {
            //send message on current session
            sessionInformation.updateExpiryTime();
            sendToApplicationMember(synCtx, currentMember, faultHandler, false);
        } else {
            // prepare for a new session
            currentMember = lbMembershipHandler.getNextApplicationMember(algorithmContext);
            if (currentMember == null) {
                String msg = "No application members available";
                log.error(msg);
                throw new SynapseException(msg);
            }
            sendToApplicationMember(synCtx, currentMember, faultHandler, true);
        }
    }

    protected void setCookieHeader(MessageContext synCtx) {
        String cookieHeader = extractSessionID(synCtx, "Cookie");
        if (cookieHeader != null) {
            synCtx.setProperty("LB_COOKIE_HEADER", cookieHeader);
        }
    }

    //TODO following methods are to extract the session ID temporary hack for Stratos 1.0.0 release
    protected String extractSessionID(MessageContext synCtx, String key) {

        if (key != null) {
            Map headerMap = getTransportHeaderMap(synCtx);

            if (headerMap != null) {
                Object cookieObj = headerMap.get(key);

                if (cookieObj instanceof String) {
                    return (String) cookieObj;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Couldn't find the " + key + " header to find the session");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Couldn't find the TRANSPORT_HEADERS to find the session");
                }

            }
        }
        return null;
    }

    private Map getTransportHeaderMap(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        Object o = axis2MessageContext.getProperty("TRANSPORT_HEADERS");
        if (o != null && o instanceof Map) {
            return (Map) o;
        }
        return null;
    }

	/**
	 * Adding the X-Forwarded-For/X-Originating-IP headers to the outgoing message.
	 * 
	 * @param synCtx Current message context
	 */
	protected void setupTransportHeaders(MessageContext synCtx) {
		Axis2MessageContext axis2smc = (Axis2MessageContext) synCtx;
		org.apache.axis2.context.MessageContext axis2MessageCtx = axis2smc.getAxis2MessageContext();
		Object headers = axis2MessageCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		if (headers != null && headers instanceof Map) {
			Map headersMap = (Map) headers;
			String xForwardFor = (String) headersMap.get(NhttpConstants.HEADER_X_FORWARDED_FOR);
		    String remoteHost = (String) axis2MessageCtx.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);

			if (xForwardFor != null && !"".equals(xForwardFor)) {
				StringBuilder xForwardedForString = new StringBuilder();
				xForwardedForString.append(xForwardFor);
				if (remoteHost != null && !"".equals(remoteHost)) {
					xForwardedForString.append(",").append(remoteHost);
				}
				headersMap.put(NhttpConstants.HEADER_X_FORWARDED_FOR, xForwardedForString.toString());
			} else {
				headersMap.put(NhttpConstants.HEADER_X_FORWARDED_FOR, remoteHost);
			}

			//Extracting information of X-Originating-IP
			if (headersMap.get(NhttpConstants.HEADER_X_ORIGINATING_IP_FORM_1) != null) {
				headersMap.put(NhttpConstants.HEADER_X_ORIGINATING_IP_FORM_1, headersMap.get(NhttpConstants.HEADER_X_ORIGINATING_IP_FORM_1));
			} else if (headersMap.get(NhttpConstants.HEADER_X_ORIGINATING_IP_FORM_2) != null) {
				headersMap.put(NhttpConstants.HEADER_X_ORIGINATING_IP_FORM_2, headersMap.get(NhttpConstants.HEADER_X_ORIGINATING_IP_FORM_2));
			}

		}
	}

    public void setName(String name) {
        super.setName(name);
//        algorithmContext.setContextID(name);
    }

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public void setSessionAffinity(boolean sessionAffinity){
        this.sessionAffinity = sessionAffinity;
    }

    public boolean isSessionAffinityBasedLB(){
        return sessionAffinity;
    }

    protected void sendToApplicationMember(MessageContext synCtx,
                                           Member currentMember,
                                           DynamicLoadbalanceFaultHandler faultHandler,
                                           boolean newSession) {
        //Rewriting the URL
        org.apache.axis2.context.MessageContext axis2MsgCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        //Removing the REST_URL_POSTFIX - this is a hack.
        //In this loadbalance endpoint we create an endpoint per request by setting the complete url as the adress.
        //If a REST message comes Axis2FlexibleMEPClient append the REST_URL_POSTFIX to the adress. Hence endpoint fails
        //do send the request. e.g.  http://localhost:8080/example/index.html/example/index.html
        axis2MsgCtx.removeProperty(NhttpConstants.REST_URL_POSTFIX);

        String transport = axis2MsgCtx.getTransportIn().getName();
        String address = synCtx.getTo().getAddress();
        int incomingPort = extractPort(synCtx, transport);
        EndpointReference to = getEndpointReferenceAfterURLRewrite(currentMember,
                transport, address, incomingPort);
        synCtx.setTo(to);

        faultHandler.setTo(to);
        faultHandler.setCurrentMember(currentMember);
        synCtx.pushFaultHandler(faultHandler);
        if (isFailover()) {
            synCtx.getEnvelope().build();
        }

        Endpoint endpoint = getEndpoint(to, currentMember, synCtx);
        faultHandler.setCurrentEp(endpoint);

        if (isSessionAffinityBasedLB()) {
            if(newSession) {
            	prepareEndPointSequence(synCtx, endpoint);	
            	synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER, currentMember);
            	// we should also indicate that this is the first message in the session. so that
            	// onFault(...) method can resend only the failed attempts for the first message.
            	synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_FIRST_MESSAGE_IN_SESSION, Boolean.TRUE);
            }
        }

        if (isSessionAffinityBasedLB()) {
            synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_DEFAULT_SESSION_TIMEOUT, getSessionTimeout());
            synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_DISPATCHER, dispatcher);
        }

        Map<String, String> memberHosts;
        if ((memberHosts = (Map<String, String>) currentMember.getProperties().get(HttpSessionDispatcher.HOSTS)) == null) {
            currentMember.getProperties().put(HttpSessionDispatcher.HOSTS,
                    memberHosts = new HashMap<String, String>());
        }
        memberHosts.put(extractHost(synCtx), "true");
        setupTransportHeaders(synCtx);
        try {
            endpoint.send(synCtx);
        } catch (Exception e) {
            if(e.getMessage().toLowerCase().contains("io reactor shutdown")){
                log.fatal("System cannot continue normal operation. Restarting", e);
                System.exit(121); // restart
            } else {
                throw new SynapseException(e);
            }
        }
    }

    /*
    * Preparing the endpoint sequence for a new session establishment request
    */
    private void prepareEndPointSequence(MessageContext synCtx, Endpoint endpoint) {

        Object o = synCtx.getProperty(SynapseConstants.PROP_SAL_ENDPOINT_ENDPOINT_LIST);
        List<Endpoint> endpointList;
        if (o instanceof List) {
            endpointList = (List<Endpoint>) o;
            endpointList.add(this);

        } else {
            // this is the first endpoint in the hierarchy. so create the queue and
            // insert this as the first element.
            endpointList = new ArrayList<Endpoint>();
            endpointList.add(this);
            synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_ENDPOINT_LIST, endpointList);
            synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_DISPATCHER, dispatcher);
        }

        // if the next endpoint is not a session affinity one, endpoint sequence ends
        // here. but we have to add the next endpoint to the list.
        if (!(endpoint instanceof DynamicLoadbalanceEndpoint)) {
            endpointList.add(endpoint);
            // Clearing out if there any any session information with current message
            if (dispatcher.isServerInitiatedSession()) {
                dispatcher.removeSessionID(synCtx);
            }
        }
    }

    private EndpointReference getEndpointReferenceAfterURLRewrite(Member currentMember,
                                                                  String transport,
                                                                  String address,
                                                                  int incomingPort) {
        if (currentMember == null) {
            String msg = "application member not available";
            log.error(msg);
            throw new SynapseException(msg);
        }

        if (transport.startsWith("https")) {
            transport = "https";
        } else if (transport.startsWith("http")) {
            transport = "http";
        } else {
            String msg = "Cannot load balance for non-HTTP/S transport " + transport;
            log.error(msg);
            throw new SynapseException(msg);
        }
        // URL Rewrite
        if (transport.startsWith("http") || transport.startsWith("https")) {
            if (address.startsWith("http://") || address.startsWith("https://")) {
                try {
                    String _address= address.indexOf("?")>0? address.substring(address.indexOf("?"), address.length()):"";
                    address = new URL(address).getPath()+_address;
                } catch (MalformedURLException e) {
                    String msg = "URL " + address + " is malformed";
                    log.error(msg, e);
                    throw new SynapseException(msg, e);
                }
            }

            int port;
            Properties memberProperties = currentMember.getProperties();
            String mappedPort = memberProperties.getProperty(PORT_MAPPING_PREFIX + incomingPort);
            if (mappedPort != null) {
                port = Integer.parseInt(mappedPort);
            } else if (transport.startsWith("https")) {
                port = currentMember.getHttpsPort();
            } else {
                port = currentMember.getHttpPort();
            }

            String remoteHost = memberProperties.getProperty("remoteHost");
            String hostName = (remoteHost == null) ? currentMember.getHostName() : remoteHost;
            return new EndpointReference(transport + "://" + hostName +
                                 ":" + port + address);
        } else {
            String msg = "Cannot load balance for non-HTTP/S transport " + transport;
            log.error(msg);
            throw new SynapseException(msg);
        }
    }

    /**
     *
     * @param to get an endpoint to send the information
     * @param member The member to which an EP has to be created
     * @param synCtx synapse context
     * @return the created endpoint
     */
    private Endpoint getEndpoint(EndpointReference to, Member member, MessageContext synCtx) {
        AddressEndpoint endpoint = new AddressEndpoint();
        endpoint.setEnableMBeanStats(false);
        endpoint.setName("DLB:" +  member.getHostName() +
                ":" + member.getPort() + ":" + UUID.randomUUID());
        EndpointDefinition definition = new EndpointDefinition();
        definition.setSuspendMaximumDuration(10000);
        definition.setReplicationDisabled(true);
        definition.setAddress(to.getAddress());
        endpoint.setDefinition(definition);

        AxisConfiguration axisConfiguration =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext()
                        .getConfigurationContext().getAxisConfiguration();
        if (axisConfiguration != null) {
            endpoint.init((SynapseEnvironment)
                                  axisConfiguration.
                                          getParameterValue(SynapseConstants.SYNAPSE_ENV));
        } else {
            String msg = "AxisConfiguration not available - Server might be shutting down";
            log.error(msg);
            throw new SynapseException(msg);
        }
        return endpoint;
    }

    private String extractHost(MessageContext synCtx) {
        org.apache.axis2.context.MessageContext msgCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        Map headerMap = (Map) msgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String hostName = null;
        if (headerMap != null) {
            Object hostObj = headerMap.get(HTTP.TARGET_HOST);
            hostName = (String) hostObj;
            if (hostName.contains(":")) {
                hostName = hostName.substring(0, hostName.indexOf(":"));
            }
        }
        return hostName;
    }

    private int extractPort(MessageContext synCtx, String transport) {
        org.apache.axis2.context.MessageContext msgCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        Map headerMap = (Map) msgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        int port = -1;
		if (headerMap != null) {
			String hostHeader = (String) headerMap.get(HTTP.TARGET_HOST);
			int preIndex = hostHeader.indexOf(":");
			int postIndex = hostHeader.indexOf("/");
			if (preIndex != -1 && postIndex != -1) {
				port = Integer.parseInt(hostHeader.trim().substring(preIndex + 1, postIndex));
			} else if (preIndex != -1) {
				port = Integer.parseInt(hostHeader.trim().substring(preIndex + 1));
			} else {
				if ("http".equals(transport)) {
					port = 80;
				} else if ("https".equals(transport)) {
					port = 443;
				}
			}
		}
        return port;
    }



    /**
     * This FaultHandler will try to resend the message to another member if an error occurs
     * while sending to some member. This is a failover mechanism
     */
    private class DynamicLoadbalanceFaultHandlerImpl extends DynamicLoadbalanceFaultHandler {

        private EndpointReference to;
        private Member currentMember;
        private Endpoint currentEp;

        public void setCurrentMember(Member currentMember) {
            this.currentMember = currentMember;
        }

        public void setTo(EndpointReference to) {
            this.to = to;
        }

        private DynamicLoadbalanceFaultHandlerImpl() {
        }

        public void onFault(MessageContext synCtx) {
            //cleanup endpoint if exists
            if(currentEp != null){
                currentEp.destroy();
            }
            if (currentMember == null) {
                return;
            }

            Stack faultStack = synCtx.getFaultStack();
            if (faultStack != null && !faultStack.isEmpty()) {
                faultStack.pop();  // Remove the LoadbalanceFaultHandler
            }

            currentMember = lbMembershipHandler.getNextApplicationMember(algorithmContext);
            if(currentMember == null){
                String msg = "No application members available";
                log.error(msg);
                throw new SynapseException(msg);
            }
            synCtx.setTo(to);
            if(isSessionAffinityBasedLB()){
                //We are sending the this message on a new session,
                // hence we need to remove previous session information
                Set pros = synCtx.getPropertyKeySet();
                if (pros != null) {
                    pros.remove(SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);
                }
            }
            sendToApplicationMember(synCtx, currentMember, this, true);
        }

        public void setCurrentEp(Endpoint currentEp) {
            this.currentEp = currentEp;
        }
    }
}
