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

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.endpoints.dispatch.Dispatcher;
import org.apache.synapse.endpoints.dispatch.HttpSessionDispatcher;
import org.apache.synapse.endpoints.dispatch.SALSessions;
import org.apache.synapse.endpoints.dispatch.SessionInformation;
import org.apache.synapse.transport.passthru.util.RelayUtils;

/**
 * SALoadbalanceEndpoint supports session affinity based load balancing. Each of this endpoint
 * maintains a list of dispatchers. These dispatchers will be updated for both request (for client
 * initiated sessions) and response (for server initiated sessions). Once updated, each dispatcher
 * will check if has already encountered that session. If not, it will update the
 * session -> endpoint map. To update sessions for response messages, all SALoadbalanceEndpoint
 * objects are kept in a global property. When a message passes through SALoadbalanceEndpoints, each
 * endpoint appends its "Synapse unique ID" to the operation context. Once the response for that
 * message arrives, response sender checks first endpoint of the endpoint sequence from the
 * operation context and get that endpoint from the above mentioned global property. Then it will
 * invoke updateSession(...) method of that endpoint. After that, each endpoint will call
 * updateSession(...) method of their appropriate child endpoint, so that all the sending endpoints
 * for the session will be updated.
 * <p/>
 * This endpoint gets the target endpoint first from the dispatch manager, which will ask all listed
 * dispatchers for a matching session. If a matching session is found it will just invoke the
 * send(...) method of that endpoint. If not it will find an endpoint using the load balancing
 * policy and send to that endpoint.
 */
public class SALoadbalanceEndpoint extends LoadbalanceEndpoint {
  
    /**
     * Dispatcher used for session affinity.
     */
    private Dispatcher dispatcher = null;


    /* Sessions time out interval*/
    private long sessionTimeout = -1;

    public void init(SynapseEnvironment synapseEnvironment) {
        ConfigurationContext cc =
                ((Axis2SynapseEnvironment) synapseEnvironment).getAxis2ConfigurationContext();
        if (!initialized) {

            super.init(synapseEnvironment);
            // Initialize the SAL Sessions if already has not been initialized.
            SALSessions salSessions = SALSessions.getInstance();
            if (!salSessions.isInitialized()) {
                salSessions.initialize(isClusteringEnabled, cc);
            }

            //For each root level SAL endpoints , all children are registered 
            // This is for cluttering as in clustering only endpoint names are replicated 
            // and it needs way to pick endpoints by name
            if (isClusteringEnabled && (this.getParentEndpoint() == null ||
                    !(this.getParentEndpoint() instanceof SALoadbalanceEndpoint))) {
                SALSessions.getInstance().registerChildren(this, getChildren());
            }

        }
    }

    public void send(MessageContext synCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Start : Session Affinity Load-balance Endpoint " + getName());
        }

        if (getContext().isState(EndpointContext.ST_OFF)) {
            informFailure(synCtx, SynapseConstants.ENDPOINT_LB_NONE_READY,
                    "Loadbalance endpoint : " + getName() != null ? getName() : SynapseConstants.ANONYMOUS_ENDPOINT + " - is inactive");
            return;
        }

        // first check if this session is associated with a session. if so, get the endpoint
        // associated for that session.

        SessionInformation sessionInformation =
                (SessionInformation) synCtx.getProperty(
                        SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);

        List<Endpoint> endpoints = (List<Endpoint>) synCtx.getProperty(
                SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_ENDPOINT_LIST);

        if (!(dispatcher instanceof HttpSessionDispatcher)) {
            try {
                RelayUtils.buildMessage(((Axis2MessageContext) synCtx).getAxis2MessageContext(),false);
            } catch (Exception e) {
                handleException("Error while building message", e);
            }
        }

        // evaluate the properties
        evaluateProperties(synCtx);
        
        if (sessionInformation == null && endpoints == null) {

            sessionInformation = dispatcher.getSession(synCtx);
            if (sessionInformation != null) {

                if (log.isDebugEnabled()) {
                    log.debug("Current session id : " + sessionInformation.getId());
                }
                endpoints =
                        dispatcher.getEndpoints(sessionInformation);
                if (log.isDebugEnabled()) {
                    log.debug("Endpoint sequence (path) on current session : " + this + endpoints);
                }

                synCtx.setProperty(
                        SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_ENDPOINT_LIST, endpoints);
                // This is for reliably recovery any session information if while response is getting ,
                // session information has been removed by cleaner.
                // This will not be a cost as  session information a not heavy data structure
                synCtx.setProperty(
                        SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION, sessionInformation);
            }
        }

        if (sessionInformation != null && endpoints != null) {
            //send message on current session
            sendMessageOnCurrentSession(sessionInformation.getId(), endpoints, synCtx);
        } else {
            // prepare for a new session 
            sendMessageOnNewSession(synCtx);
        }
    }  

    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    /**
     * It is logically incorrect to failover a session affinity endpoint after the session has started.
     * If we redirect a message belonging to a particular session, new endpoint is not aware of the
     * session. So we can't handle anything more at the endpoint level. Therefore, this method just
     * deactivate the failed endpoint and give the fault to the next fault handler.
     * <p/>
     * But if the session has not started (i.e. first message), the message will be resend by binding
     * it to a different endpoint.
     *
     * @param endpoint          Failed endpoint.
     * @param synCtx MessageContext of the failed message.
     */
    public void onChildEndpointFail(Endpoint endpoint, MessageContext synCtx) {

        logOnChildEndpointFail(endpoint, synCtx);
        Object o = synCtx.getProperty(
                SynapseConstants.PROP_SAL_ENDPOINT_FIRST_MESSAGE_IN_SESSION);

        if (o != null && Boolean.TRUE.equals(o) &&
                ((AbstractEndpoint) endpoint).isRetry(synCtx)) {
            // this is the first message. so unbind the session with failed endpoint and start
            // new one by resending.

            dispatcher.unbind(synCtx);

            // As going to be happened retry , we have to remove states related to the previous try

            Object epListObj = synCtx.getProperty(SynapseConstants.PROP_SAL_ENDPOINT_ENDPOINT_LIST);
            if (epListObj instanceof List) {
                List<Endpoint> endpointList = (List<Endpoint>) epListObj;
                if (!endpointList.isEmpty()) {
                    if (endpointList.get(0) == this) {
                        endpointList.clear();
                    } else {
                        if (endpointList.contains(this)) {
                            int lastIndex = endpointList.indexOf(this);
                            List<Endpoint> head =
                                    endpointList.subList(lastIndex, endpointList.size());
                            head.clear();
                        }
                    }
                }
            }

            send(synCtx);

        } else {
            // session has already started. we can't failover.
            informFailure(synCtx, SynapseConstants.ENDPOINT_SAL_FAILED_SESSION,
                    "Failure an endpoint " + endpoint + "  in the current session");
        }
    }

    /*
    * Helper method  that send message on the endpoint sequence on the current session
     */
    private void sendMessageOnCurrentSession(String sessionID, List<Endpoint> endpoints, MessageContext synCtx) {
                
        // get the next endpoint in the endpoint sequence
        Endpoint endpoint = null;

        boolean invalidSequence = false;
        if (endpoints.isEmpty()) {
            invalidSequence = true;
        } else {
            if (endpoints.contains(this)) {
                // This situation will come only if this endpoint is referred as an indirect endpoint.
                //  All the path before this SAL endpoint are ignored.
                int length = endpoints.size();
                if (length > 1) {
                    
                    int beginIndex = endpoints.lastIndexOf(this) + 1;
                    if (beginIndex == length) {
                        invalidSequence = true;
                    } else {
                        endpoints = endpoints.subList(beginIndex, length);
                        if (!endpoints.isEmpty()) {
                            endpoint = endpoints.remove(0);
                        } else {
                            invalidSequence = true;
                        }
                    }
                } else {
                    invalidSequence = true;
                }

            } else {
                endpoint = endpoints.remove(0);
            }
        }

        if (invalidSequence) {
            informFailure(synCtx, SynapseConstants.ENDPOINT_SAL_INVALID_PATH,
                    "Invalid endpoint sequence " + endpoints + " for session with id " + sessionID);
            return;
        }
        // endpoints given by session dispatchers may not be active. therefore, we have check
        // it here.
        if (endpoint != null && endpoint.readyToSend()) {
            if (log.isDebugEnabled()) {
                log.debug("Using the endpoint " + endpoint + " for sending the message");
            }
            synCtx.pushFaultHandler(this);
            endpoint.send(synCtx);
        } else {
            informFailure(synCtx, SynapseConstants.ENDPOINT_SAL_NOT_READY,
                    "The endpoint " + endpoint + " on the session with id " +
                            sessionID + " is not ready.");
        }
    }

    /*
     * Helper method that send message hoping to establish new session 
     */
    private void sendMessageOnNewSession(MessageContext synCtx) {

        // there is no endpoint associated with this session. get a new endpoint using the
        // load balance policy.
        Endpoint endpoint = getNextChild(synCtx);
        if (endpoint == null) {

            informFailure(synCtx, SynapseConstants.ENDPOINT_LB_NONE_READY,
                    "SLALoadbalance endpoint : " + getName() + " - no ready child endpoints");
        } else {

            prepareEndPointSequence(synCtx, endpoint);

            // this is the first request. so an endpoint has not been bound to this session and we
            // are free to failover if the currently selected endpoint is not working. but for
            // failover to work, we have to build the soap envelope.
            synCtx.getEnvelope().build();

            // we should also indicate that this is the first message in the session. so that
            // onFault(...) method can resend only the failed attempts for the first message.
            synCtx.setProperty(SynapseConstants.PROP_SAL_ENDPOINT_FIRST_MESSAGE_IN_SESSION, Boolean.TRUE);
            synCtx.pushFaultHandler(this);
            endpoint.send(synCtx);
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
        if (!(endpoint instanceof SALoadbalanceEndpoint)) {
            endpointList.add(endpoint);
            // Clearing out if there any any session information with current message 
            if (dispatcher.isServerInitiatedSession()) {
                dispatcher.removeSessionID(synCtx);
            }
        }
    }

    public long getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(long sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
