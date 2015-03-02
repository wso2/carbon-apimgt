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

package org.apache.synapse.message.processor.impl.forwarder;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.MessageProcessorConstants;
import org.apache.synapse.message.processor.Service;
import org.apache.synapse.message.senders.blocking.BlockingMsgSender;
import org.apache.synapse.util.MessageHelper;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.Map;
import java.util.Set;

public class ForwardingService implements InterruptableJob, Service {
    private static final Log log = LogFactory.getLog(ForwardingService.class);

    /** The consumer that is associated with the particular message store */
    private MessageConsumer messageConsumer;

    /** Owner of the this job */
    private MessageProcessor messageProcessor;

    /** This is the client which sends messages to the end point */
    private BlockingMsgSender sender;

    /** Interval between two retries to the client. This only come to affect only if the client is un-reachable */
    private int retryInterval = 1000;

    /** Sequence to invoke in a failure */
    private String faultSeq = null;

    /** Sequence to reply on success */
    private String replySeq = null;

    private String targetEndpoint = null;

    /**
     * This is specially used for REST scenarios where http status codes can take semantics in a RESTful architecture.
     */
    private String[] nonRetryStatusCodes = null;

    /**
     * These two maintain the state of service. For each iteration these should be reset
     */
    private boolean isSuccessful = false;
    private volatile boolean isTerminated = false;

    /** Number of retries before shutting-down the processor. -1 default value indicates that
     * retry should happen forever */
    private int maxDeliverAttempts = -1;
    private int attemptCount = 0;

    private boolean isThrottling = true;

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        init(jobExecutionContext);

        do {
            resetService();

            try {
                if (!this.messageProcessor.isDeactivated()) {
                    MessageContext messageContext = fetch(messageConsumer);

                    if (messageContext != null) {

                        String serverName = (String)
                                messageContext.getProperty(SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME);

                        if(serverName != null && messageContext instanceof Axis2MessageContext) {

                            AxisConfiguration configuration = ((Axis2MessageContext)messageContext).
                                    getAxis2MessageContext().
                                    getConfigurationContext().getAxisConfiguration();

                            String myServerName = getAxis2ParameterValue(configuration,
                                    SynapseConstants.Axis2Param.SYNAPSE_SERVER_NAME);

                            if(!serverName.equals(myServerName)) {
                                return;
                            }
                        }

                        Set proSet = messageContext.getPropertyKeySet();

                        if (proSet != null) {
                            if (proSet.contains(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR)) {
                                proSet.remove(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR);
                            }
                        }

                        dispatch(messageContext);

                    } else {
                        // either the connection is broken or there are no new massages.
                        if (log.isDebugEnabled()) {
                            log.debug("No messages were received for message processor ["+ messageProcessor.getName() + "]");
                        }
                    }
                } else {

                    // we need this because when start the server while the processors in deactivated mode
                    // the deactivation may not come in to play because the service may not be running.
                    isTerminated = true;

                    if (log.isDebugEnabled()) {
                        log.debug("Exiting service since the message processor is deactivated");
                    }
                }
            } catch (Throwable e) {
                // All the possible recoverable exceptions are handles case by case and yet if it comes this
                // we have to shutdown the processor
                log.fatal("Deactivating the message processor [" + this.messageProcessor.getName() + "]", e);

                this.messageProcessor.deactivate();
            }

            if (log.isDebugEnabled()) {
                log.debug("Exiting the iteration of message processor [" + this.messageProcessor.getName() + "]");
            }

        } while (isThrottling && !isTerminated);

        if (log.isDebugEnabled()) {
            log.debug("Exiting service thread of message processor [" + this.messageProcessor.getName() + "]");
        }
    }

    /**
     * Helper method to get a value of a parameters in the AxisConfiguration
     *
     * @param axisConfiguration AxisConfiguration instance
     * @param paramKey The name / key of the parameter
     * @return The value of the parameter
     */
    private static String getAxis2ParameterValue(AxisConfiguration axisConfiguration,
                                                 String paramKey) {

        Parameter parameter = axisConfiguration.getParameter(paramKey);
        if (parameter == null) {
            return null;
        }
        Object value = parameter.getValue();
        if (value != null && value instanceof String) {
            return (String) parameter.getValue();
        } else {
            return null;
        }
    }

    /**
     * Though it says init() it does not instantiate objects every time the service is running. This simply
     * initialize the local variables with pre-instantiated objects.
     * @param jobExecutionContext is the Quartz context
     * @return true if it is successfully executed.
     */
    public boolean init(JobExecutionContext jobExecutionContext) {

        JobDataMap jdm = jobExecutionContext.getMergedJobDataMap();
        Map<String, Object> parameters = (Map<String, Object>) jdm.get(MessageProcessorConstants.PARAMETERS);
        sender =
                (BlockingMsgSender) jdm.get(ScheduledMessageForwardingProcessor.BLOCKING_SENDER);

        String mdaParam = (String) parameters.get(MessageProcessorConstants.MAX_DELIVER_ATTEMPTS);
        if (mdaParam != null) {
            try {
                maxDeliverAttempts = Integer.parseInt(mdaParam);
            } catch (NumberFormatException nfe) {
                parameters.remove(MessageProcessorConstants.MAX_DELIVER_ATTEMPTS);
                log.error("Invalid value for max delivery attempts switching back to default value", nfe);
            }
        }

        if (jdm.get(ForwardingProcessorConstants.TARGET_ENDPOINT) != null) {
            targetEndpoint = (String) jdm.get(ForwardingProcessorConstants.TARGET_ENDPOINT);
        }

        String ri = (String) parameters.get(MessageProcessorConstants.RETRY_INTERVAL);
        if (ri != null) {
            try {
                retryInterval = Integer.parseInt(ri);
            } catch (NumberFormatException nfe) {
                parameters.remove(MessageProcessorConstants.RETRY_INTERVAL);
                log.error("Invalid value for retry interval switching back to default value", nfe);
            }
        }

        messageProcessor = (MessageProcessor)jdm.get(MessageProcessorConstants.PROCESSOR_INSTANCE);
        messageConsumer = messageProcessor.getMessageConsumer();

        if (parameters.get(ForwardingProcessorConstants.FAULT_SEQUENCE) != null) {
            faultSeq = (String) parameters.get(ForwardingProcessorConstants.FAULT_SEQUENCE);
        }

        if (parameters.get(ForwardingProcessorConstants.REPLY_SEQUENCE) != null) {
            replySeq = (String) parameters.get(ForwardingProcessorConstants.REPLY_SEQUENCE);
        }

        if (jdm.get(ForwardingProcessorConstants.NON_RETRY_STATUS_CODES) != null) {
            nonRetryStatusCodes = (String []) jdm.get(ForwardingProcessorConstants.NON_RETRY_STATUS_CODES);
        }

        if (jdm.get(ForwardingProcessorConstants.THROTTLE) != null) {
            isThrottling = (Boolean) jdm.get(ForwardingProcessorConstants.THROTTLE);
        }

        return true;
    }

    public MessageContext fetch(MessageConsumer msgConsumer) {
        return messageConsumer.receive();
    }

    public boolean dispatch(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("Sending the message to client with message processor [" + messageProcessor.getName() + "]");
        }

        // The below code is just for keeping the backward compatibility with the old code.
        if (targetEndpoint == null) {
            targetEndpoint = (String) messageContext.getProperty(ForwardingProcessorConstants.TARGET_ENDPOINT);
        }

        MessageContext outCtx = null;
        SOAPEnvelope originalEnvelop = messageContext.getEnvelope();

        if (targetEndpoint != null) {
            Endpoint ep = messageContext.getEndpoint(targetEndpoint);

            try {

                // Send message to the client
                while (!isSuccessful && !isTerminated) {
                    try {
                        // For each retry we need to have a fresh copy of the actual message. otherwise retry may not
                        // work as expected.
                        messageContext.setEnvelope(MessageHelper.cloneSOAPEnvelope(originalEnvelop));

                        OMElement firstChild = null; //
                        org.apache.axis2.context.MessageContext origAxis2Ctx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

                        if (JsonUtil.hasAJsonPayload(origAxis2Ctx)) {
                            firstChild = origAxis2Ctx.getEnvelope().getBody().getFirstElement();
                        } // Had to do this because MessageHelper#cloneSOAPEnvelope does not clone OMSourcedElemImpl correctly.

                        if (JsonUtil.hasAJsonPayload(firstChild)) { //
                            OMElement clonedFirstElement = messageContext.getEnvelope().getBody().getFirstElement();
                            if (clonedFirstElement != null) {
                                clonedFirstElement.detach();
                                messageContext.getEnvelope().getBody().addChild(firstChild);
                            }
                        }// Had to do this because MessageHelper#cloneSOAPEnvelope does not clone OMSourcedElemImpl correctly.

                        outCtx = sender.send(ep, messageContext);
                        isSuccessful = true;

                    } catch (Exception e) {

                        // this means send has failed due to some reason so we have to retry it
                        if (e instanceof SynapseException) {
                            isSuccessful = isNonRetryErrorCode(e.getCause().getMessage());
                        }
                        if (!isSuccessful) {
                            log.error("BlockingMessageSender of message processor ["+ this.messageProcessor.getName()
                                    + "] failed to send message to the endpoint");
                        }
                    }

                    if (isSuccessful) {
                        if (outCtx != null) {
                            if ("true".equals(outCtx.
                                    getProperty(ForwardingProcessorConstants.BLOCKING_SENDER_ERROR))) {

                                // this means send has failed due to some reason so we have to retry it
                                isSuccessful = isNonRetryErrorCode(
                                        (String) outCtx.getProperty(SynapseConstants.ERROR_MESSAGE));

                                if (isSuccessful) {
                                    sendThroughReplySeq(outCtx);
                                } else {
                                    // This means some error has occurred so must try to send down the fault sequence.
                                    log.error("BlockingMessageSender of message processor ["+ this.messageProcessor.getName()
                                            + "] failed to send message to the endpoint");
                                    sendThroughFaultSeq(outCtx);
                                }
                            }
                            else {
                                // Send the message down the reply sequence if there is one
                                sendThroughReplySeq(outCtx);
                                messageConsumer.ack();
                                attemptCount = 0;
                                isSuccessful = true;

                                if (log.isDebugEnabled()) {
                                    log.debug("Successfully sent the message to endpoint [" + ep.getName() +"]"
                                                      + " with message processor [" + messageProcessor.getName() + "]");
                                }
                            }
                        }
                        else {
                            // This Means we have invoked an out only operation
                            // remove the message and reset the count
                            messageConsumer.ack();
                            attemptCount = 0;
                            isSuccessful = true;

                            if (log.isDebugEnabled()) {
                                log.debug("Successfully sent the message to endpoint [" + ep.getName() +"]"
                                                      + " with message processor [" + messageProcessor.getName() + "]");
                            }
                        }
                    }

                    if (!isSuccessful) {
                        // Then we have to retry sending the message to the client.
                        prepareToRetry();
                    }
                    else {
                        if (messageProcessor.isPaused()) {
                            this.messageProcessor.resumeService();
                            log.info("Resuming the service of message processor [" + messageProcessor.getName() + "]");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Message processor [" + messageProcessor.getName() + "] failed to send the message to" +
                        " client", e);
            }
        }
        else {
            //No Target Endpoint defined for the Message
            //So we do not have a place to deliver.
            //Here we log a warning and remove the message
            //todo: we can improve this by implementing a target inferring mechanism

            log.warn("Property " + ForwardingProcessorConstants.TARGET_ENDPOINT +
                    " not found in the message context , Hence removing the message ");

            messageConsumer.ack();
        }

        return true;
    }

    public void sendThroughFaultSeq(MessageContext msgCtx) {
        if (faultSeq == null) {
            log.warn("Failed to send the message through the fault sequence, Sequence name " + faultSeq + " does not Exist.");
            return;
        }
        Mediator mediator = msgCtx.getSequence(faultSeq);

        if (mediator == null) {
            log.warn("Failed to send the message through the fault sequence, Sequence object" + faultSeq + " does not Exist.");
            return;
        }

        mediator.mediate(msgCtx);
    }

    public void sendThroughReplySeq(MessageContext outCtx) {
        if (replySeq == null) {
            this.messageProcessor.deactivate();
            log.error("Can't Send the Out Message , Sequence name " + replySeq + " does not Exist. Deactivated the" +
                    " message processor");
            return;
        }
        Mediator mediator = outCtx.getSequence(replySeq);

        if (mediator == null) {
            this.messageProcessor.deactivate();
            log.error("Can't Send the Out Message , Sequence object " + replySeq + " does not Exist. Deactivated the" +
                    " message processor");
            return;
        }

        mediator.mediate(outCtx);
    }

    public boolean terminate() {
        try {
            isTerminated = true;
            Thread.currentThread().interrupt();

            if (log.isDebugEnabled()) {
                log.debug("Successfully terminated job of message processor [" + messageProcessor.getName() + "]");
            }
            return true;
        } catch (Exception e) {
            log.error("Failed to terminate the job of message processor [" + messageProcessor.getName() + "]");
            return false;
        }
    }

    private void checkAndDeactivateProcessor(int attemptCount, int maxAttempts) {
        if (maxAttempts > 0) {
            this.attemptCount++;

            if (attemptCount >= maxAttempts) {
                terminate();
                this.messageProcessor.deactivate();

                if (log.isDebugEnabled()) {
                    log.debug("Message processor [" + messageProcessor.getName() +
                            "] stopped due to reach of max attempts");
                }
            }
        }
    }

    public void interrupt() throws UnableToInterruptJobException {

        if (log.isDebugEnabled()) {
            log.debug("Successfully interrupted job of message processor [" + messageProcessor.getName() + "]");
        }

        terminate();
    }

    private void prepareToRetry() {
        if (!isTerminated) {
            // First stop the processor since no point in re-triggering jobs if the we can't send
            // it to the client
            if (!messageProcessor.isPaused()) {
                this.messageProcessor.pauseService();

                log.info("Pausing the service of message processor [" + messageProcessor.getName() + "]");
            }

            checkAndDeactivateProcessor(attemptCount, maxDeliverAttempts);

            if (log.isDebugEnabled()) {
                log.debug("Failed to send to client retrying after " + retryInterval +
                        "s with attempt count - " + attemptCount);
            }

            try {
                // wait for some time before retrying
                Thread.sleep(retryInterval);
            } catch (InterruptedException ignore) {
                // No harm even it gets interrupted. So nothing to handle.
            }
        }
    }

    private void resetService() {
        isSuccessful = false;
        attemptCount = 0;
    }

    private boolean isNonRetryErrorCode(String errorMsg) {
        boolean isSuccess = false;
        if (nonRetryStatusCodes != null) {
            for (String code : nonRetryStatusCodes) {
                if (errorMsg != null && errorMsg.contains(code)) {
                    isSuccess = true;
                    break;
                }
            }
        }

        return isSuccess;
    }
}
