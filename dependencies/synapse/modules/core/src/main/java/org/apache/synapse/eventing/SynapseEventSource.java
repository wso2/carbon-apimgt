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

package org.apache.synapse.eventing;

import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.AddressEndpoint;
import org.apache.synapse.endpoints.EndpointDefinition;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.SynapseMessageReceiver;
import org.apache.synapse.eventing.builders.ResponseMessageBuilder;
import org.apache.synapse.eventing.builders.SubscriptionMessageBuilder;
import org.apache.synapse.util.MessageHelper;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.Event;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.exceptions.EventException;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Eventsource that accepts the event requests using a message receiver.
 * Eventsource is responsible on two tasks accepting the subscriptions and subscription related
 * reqests and dispatching events.
 * Subscriptions contains operations listed in the WS-Eventing specification. {SubscribeOP,
 * UnsubscribeOP, RenewOP, GetstatusOP, SubscriptionEndOP}
 * based on the action in the request eventsource identify the operation and send it for processing.
 * Eventsource link with a subscription manager to store the subscriptions.
 */
public class SynapseEventSource extends SynapseMessageReceiver {

    private String name;
    private SubscriptionManager subscriptionManager;
    private static final Log log = LogFactory.getLog(SynapseEventSource.class);
    private String fileName;
    /* Contains properties used in the configuration and possess confidential information such as
     encrypted passwords  */
    private Map<String, String> configurationProperties = new HashMap<String, String>();

    public SynapseEventSource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SubscriptionManager getSubscriptionManager() {
        return subscriptionManager;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager) {
        this.subscriptionManager = subscriptionManager;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void buildService(AxisConfiguration axisCfg) throws AxisFault {
        AxisService eventSourceService = new AxisService();
        eventSourceService.setName(this.name);
        // Add wse operations 
        addOperations(eventSourceService);
        axisCfg.addService(eventSourceService);
        //Set the service parameters
        eventSourceService
                .addParameter(EventingConstants.SUBSCRIPTION_MANAGER, subscriptionManager);
        eventSourceService.addParameter(SynapseEventingConstants.SERVICE_TYPE,
                SynapseEventingConstants.EVENTING_ST);
    }

    /**
     * Override the Message receiver method to accept subscriptions and events
     *
     * @param mc message context
     * @throws AxisFault
     */
    public void receive(MessageContext mc) throws AxisFault {
        // Create synapse message context from the axis2 message context
        SynapseConfiguration synCfg = (SynapseConfiguration) mc.getConfigurationContext()
                .getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_CONFIG).getValue();
        SynapseEnvironment synEnv = (SynapseEnvironment) mc.getConfigurationContext()
                .getAxisConfiguration().getParameter(SynapseConstants.SYNAPSE_ENV).getValue();
        org.apache.synapse.MessageContext smc = new Axis2MessageContext(mc, synCfg, synEnv);
        // initialize the response message builder using the message context
        ResponseMessageBuilder messageBuilder = new ResponseMessageBuilder(mc);
        try {
            if (EventingConstants.WSE_SUBSCRIBE.equals(mc.getWSAAction())) {
                // add new subscription to the SynapseSubscription store through subscription manager
                processSubscriptionRequest(mc, messageBuilder);
            } else if (EventingConstants.WSE_UNSUBSCRIBE.equals(mc.getWSAAction())) {
                // Unsubscribe the matching subscription
                processUnSubscribeRequest(mc, messageBuilder);
            } else if (EventingConstants.WSE_GET_STATUS.equals(mc.getWSAAction())) {
                // Response with the status of the subscription
                processGetStatusRequest(mc, messageBuilder);
            } else if (EventingConstants.WSE_RENEW.equals(mc.getWSAAction())) {
                // Renew subscription
                processReNewRequest(mc, messageBuilder);
            } else {
                // Treat as an Event
                if (log.isDebugEnabled()) {
                    log.debug("Event received");
                }
                dispatchEvents(smc);
            }
        } catch (EventException e) {
            handleException("Subscription manager processing error", e);
        }
    }

    /**
     * Dispatch the message to the target endpoint
     *
     * @param soapEnvelope   Soap Enevlop with message
     * @param responseAction WSE action for the response
     * @param mc             Message Context
     * @param faultMessage   Fault message
     * @throws AxisFault     AxisFault
     */
    private void dispatchResponse(SOAPEnvelope soapEnvelope,
                                  String responseAction,
                                  MessageContext mc,
                                  boolean faultMessage) throws AxisFault {
        MessageContext rmc = MessageContextBuilder.createOutMessageContext(mc);
        rmc.getOperationContext().addMessageContext(rmc);
        rmc.setEnvelope(soapEnvelope);
        rmc.setWSAAction(responseAction);
        rmc.setSoapAction(responseAction);
        rmc.setProperty(SynapseConstants.ISRESPONSE_PROPERTY, Boolean.TRUE);
        if (faultMessage) {
            AxisEngine.sendFault(rmc);
        } else {
            AxisEngine.send(rmc);
        }
    }

    /**
     * Public method for event dispatching, used by the eventPublisher mediator and eventSource
     *
     * @param msgCtx message context
     */
    public void dispatchEvents(org.apache.synapse.MessageContext msgCtx) {

        List<Subscription> subscribers = null;

        // Call event dispatcher
        msgCtx.getEnvironment().getExecutorService()
                .execute(new EventDispatcher(msgCtx));
    }

    /**
     * Dispatching events async on a different thread
     */
    class EventDispatcher implements Runnable {
        private org.apache.synapse.MessageContext synCtx;
        private List<Subscription> subscriptions;

        EventDispatcher(org.apache.synapse.MessageContext synCtx) {
            this.synCtx = synCtx;
        }

        public void run() {
            try {
                MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
                Event<MessageContext> event = new Event(msgCtx);
                subscriptions = subscriptionManager.getMatchingSubscriptions(event);
            } catch (EventException e) {
                handleException("Matching subscriptions fetching error", e);
            }

            for (Subscription subscription : subscriptions) {
                synCtx.setProperty(SynapseConstants.OUT_ONLY,
                        "true");    // Set one way message for events
                try {
                    getEndpointFromURL(subscription.getEndpointUrl(), synCtx.getEnvironment())
                            .send(MessageHelper.cloneMessageContext(synCtx));
                } catch (AxisFault axisFault) {
                    log.error("Event sending failure " + axisFault.toString());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Event push to  : " + subscription.getEndpointUrl());
                }
            }
        }
    }

    /**
     * Process the subscription message request
     *
     * @param mc             axis2 message context
     * @param messageBuilder respose message builder
     * @throws AxisFault     axis fault
     * @throws EventException eventing exception
     */
    private void processSubscriptionRequest(MessageContext mc,
                                            ResponseMessageBuilder messageBuilder)
            throws AxisFault, EventException {
        SynapseSubscription subscription = SubscriptionMessageBuilder.createSubscription(mc);
        if (log.isDebugEnabled()) {
            log.debug("SynapseSubscription request recived  : " + subscription.getId());
        }
        if (subscription.getId() != null) {
            String subID = subscriptionManager.subscribe(subscription);
            if (subID != null) {
                // Send the subscription responce
                if (log.isDebugEnabled()) {
                    log.debug("Sending subscription response for SynapseSubscription ID : " +
                            subscription.getId());
                }
                SOAPEnvelope soapEnvelope =
                        messageBuilder.genSubscriptionResponse(subscription);
                dispatchResponse(soapEnvelope, EventingConstants.WSE_SUbSCRIBE_RESPONSE,
                        mc, false);
            } else {
                // Send the Fault responce
                if (log.isDebugEnabled()) {
                    log.debug("SynapseSubscription Failed, sending fault response");
                }
                SOAPEnvelope soapEnvelope = messageBuilder.genFaultResponse(mc,
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                        "Unable to subscribe ", "");
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc,
                        true);
            }
        } else {
            // Send the Fault responce
            if (log.isDebugEnabled()) {
                log.debug("SynapseSubscription Failed, sending fault response");
            }
            SOAPEnvelope soapEnvelope = messageBuilder.genFaultResponse(mc,
                    SubscriptionMessageBuilder.getErrorCode(),
                    SubscriptionMessageBuilder.getErrorSubCode(),
                    SubscriptionMessageBuilder.getErrorReason(), "");
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc,
                    true);
        }
    }

    /**
     * Process the UnSubscribe message request
     *
     * @param mc             axis2 message context
     * @param messageBuilder respose message builder
     * @throws AxisFault     axis fault
     * @throws EventException eventing exception
     */
    private void processUnSubscribeRequest(MessageContext mc,
                                           ResponseMessageBuilder messageBuilder)
            throws AxisFault, EventException {
        SynapseSubscription subscription =
                SubscriptionMessageBuilder.createUnSubscribeMessage(mc);
        if (log.isDebugEnabled()) {
            log.debug("UnSubscribe response recived for SynapseSubscription ID : " +
                    subscription.getId());
        }
        if (subscriptionManager.unsubscribe(subscription.getId())) {
            //send the response
            if (log.isDebugEnabled()) {
                log.debug("Sending UnSubscribe responce for SynapseSubscription ID : " +
                        subscription.getId());
            }
            SOAPEnvelope soapEnvelope = messageBuilder.genUnSubscribeResponse(subscription);
            dispatchResponse(soapEnvelope, EventingConstants.WSE_UNSUBSCRIBE_RESPONSE,
                    mc, false);
        } else {
            // Send the Fault responce
            if (log.isDebugEnabled()) {
                log.debug("UnSubscription failed, sending fault repsponse");
            }
            SOAPEnvelope soapEnvelope = messageBuilder.genFaultResponse(mc,
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "Unable to Unsubscribe", "");
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc,
                    true);
        }
    }

    /**
     * Process the GetStatus message request
     *
     * @param mc             axis2 message context
     * @param messageBuilder respose message builder
     * @throws AxisFault     axis fault
     * @throws EventException event
     */
    private void processGetStatusRequest(MessageContext mc,
                                         ResponseMessageBuilder messageBuilder)
            throws AxisFault, EventException {
        Subscription subscription =
                SubscriptionMessageBuilder.createGetStatusMessage(mc);
        if (log.isDebugEnabled()) {
            log.debug("GetStatus request recived for SynapseSubscription ID : " +
                    subscription.getId());
        }
        subscription = subscriptionManager.getSubscription(subscription.getId());
        if (subscription != null) {
            if (log.isDebugEnabled()) {
                log.debug("Sending GetStatus responce for SynapseSubscription ID : " +
                        subscription.getId());
            }
            //send the responce
            SOAPEnvelope soapEnvelope = messageBuilder.genGetStatusResponse(subscription);
            dispatchResponse(soapEnvelope, EventingConstants.WSE_GET_STATUS_RESPONSE,
                    mc, false);
        } else {
            // Send the Fault responce
            if (log.isDebugEnabled()) {
                log.debug("GetStatus failed, sending fault response");
            }
            SOAPEnvelope soapEnvelope = messageBuilder.genFaultResponse(mc,
                    EventingConstants.WSE_FAULT_CODE_RECEIVER, "EventSourceUnableToProcess",
                    "Subscription Not Found", "");
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc,
                    true);
        }
    }

    /**
     * Process the ReNew message request
     *
     * @param mc             axis2 message context
     * @param messageBuilder respose message builder
     * @throws AxisFault axis fault
     * @throws EventException event exception
     */
    private void processReNewRequest(MessageContext mc,
                                     ResponseMessageBuilder messageBuilder)
            throws AxisFault, EventException {
        SynapseSubscription subscription =
                SubscriptionMessageBuilder.createRenewSubscribeMessage(mc);
        if (log.isDebugEnabled()) {
            log.debug("ReNew request recived for SynapseSubscription ID : " +
                    subscription.getId());
        }
        String subID = subscription.getId();
        if (subID != null) {
            if (subscriptionManager.renew(subscription)) {
                //send the response
                if (log.isDebugEnabled()) {
                    log.debug("Sending ReNew response for SynapseSubscription ID : " +
                            subscription.getId());
                }
                SOAPEnvelope soapEnvelope =
                        messageBuilder.genRenewSubscriptionResponse(subscription);
                dispatchResponse(soapEnvelope, EventingConstants.WSE_RENEW_RESPONSE,
                        mc, false);
            } else {
                // Send the Fault responce
                if (log.isDebugEnabled()) {
                    log.debug("ReNew failed, sending fault response");
                }
                SOAPEnvelope soapEnvelope = messageBuilder.genFaultResponse(mc,
                        EventingConstants.WSE_FAULT_CODE_RECEIVER, "UnableToRenew",
                        "Subscription Not Found", "");
                dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc,
                        true);
            }
        } else {
            SOAPEnvelope soapEnvelope = messageBuilder.genFaultResponse(mc,
                    SubscriptionMessageBuilder.getErrorCode(),
                    SubscriptionMessageBuilder.getErrorSubCode(),
                    SubscriptionMessageBuilder.getErrorReason(), "");
            dispatchResponse(soapEnvelope, EventingConstants.WSA_FAULT, mc, true);
        }
    }

    /**
     * Create a Endpoint for a given URL
     *
     * @param endpointUrl      URL
     * @param se    synapse environment
     * @return AddressEndpoint address endpoint
     */
    private Endpoint getEndpointFromURL(String endpointUrl, SynapseEnvironment se) {
        AddressEndpoint endpoint = new AddressEndpoint();
        EndpointDefinition def = new EndpointDefinition();
        def.setAddress(endpointUrl.trim());
        endpoint.setDefinition(def);

        endpoint.init(se);

        return endpoint;
    }

    /**
     * Set the operations avilable for EventSource service
     *
     * @param eventSourceService service
     * @throws AxisFault         axis fault
     */
    private void addOperations(AxisService eventSourceService) throws AxisFault {
        // Create operations
        AxisOperation mediateOperation =
                new InOutAxisOperation(SynapseConstants.SYNAPSE_OPERATION_NAME);
        AxisOperation subscribeOperation =
                new InOutAxisOperation(new QName(EventingConstants.WSE_SUBSCRIBE_OP));
        AxisOperation unsubscribeOperation =
                new InOutAxisOperation(new QName(EventingConstants.WSE_UNSUBSCRIBE_OP));
        AxisOperation renewOperation =
                new InOutAxisOperation(new QName(EventingConstants.WSE_RENEW_OP));
        AxisOperation getStatusOperation =
                new InOutAxisOperation(new QName(EventingConstants.WSE_GET_STATUS_OP));
        AxisOperation subscriptionEndOperation =
                new InOutAxisOperation(new QName(EventingConstants.WSE_SUBSCRIPTIONEND_OP));
        // Assign the message reciver
        mediateOperation.setMessageReceiver(this);
        subscribeOperation.setMessageReceiver(this);
        unsubscribeOperation.setMessageReceiver(this);
        renewOperation.setMessageReceiver(this);
        getStatusOperation.setMessageReceiver(this);
        subscriptionEndOperation.setMessageReceiver(this);        
        // Set Soap Action
        subscribeOperation.setSoapAction(EventingConstants.WSE_SUBSCRIBE);
        unsubscribeOperation.setSoapAction(EventingConstants.WSE_UNSUBSCRIBE);
        renewOperation.setSoapAction(EventingConstants.WSE_RENEW);
        getStatusOperation.setSoapAction(EventingConstants.WSE_GET_STATUS);
        // Add operations to the Service
        eventSourceService.addOperation(mediateOperation);
        eventSourceService.addOperation(subscribeOperation);
        eventSourceService.addOperation(unsubscribeOperation);
        eventSourceService.addOperation(renewOperation);
        eventSourceService.addOperation(getStatusOperation);
        eventSourceService.addOperation(subscriptionEndOperation);
        
    }

    // Methods for accessing configuration properties - self-explainable

    public void putConfigurationProperty(String name, String value) {
        configurationProperties.put(name, value);
    }

    public String getConfigurationProperty(String name) {
        return configurationProperties.get(name);
    }

    public boolean isContainsConfigurationProperty(String name) {
        return configurationProperties.containsKey(name);
    }

    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new SynapseException(message, e);
    }
}
