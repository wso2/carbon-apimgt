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

package org.apache.synapse.message.processor.impl.sampler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.processor.MessageProcessor;
import org.apache.synapse.message.processor.MessageProcessorConstants;
import org.apache.synapse.message.processor.Service;
import org.apache.synapse.message.processor.impl.forwarder.ScheduledMessageForwardingProcessor;
import org.quartz.InterruptableJob;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public class SamplingService implements InterruptableJob, Service {
    private static Log log = LogFactory.getLog(SamplingService.class);

    /** The consumer that is associated with the particular message store */
    private MessageConsumer messageConsumer;

    /** Owner of the this job */
    private MessageProcessor messageProcessor;

    /** Determines how many messages at a time it should execute */
    private int concurrency = 1;

    /** Represents the send sequence of a message */
    private String sequence;

    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        try {
            init(jobExecutionContext);

            if (!this.messageProcessor.isDeactivated()) {
                for (int i = 0; i < concurrency; i++) {

                    final MessageContext messageContext = fetch(messageConsumer);

                    if (messageContext != null) {
                        dispatch(messageContext);
                    }
                    else {
                        // either the connection is broken or there are no new massages.
                        if (log.isDebugEnabled()) {
                            log.debug("No messages were received for message processor ["+ messageProcessor.getName() + "]");
                        }
                    }
                }
            }
            else {
                if (log.isDebugEnabled()) {
                    log.debug("Exiting service since the message processor is deactivated");
                }
            }
        } catch (Throwable t) {
            // All the possible recoverable exceptions are handles case by case and yet if it comes this
            // we have to shutdown the processor
            log.fatal("Deactivating the message processor [" + this.messageProcessor.getName() + "]", t);

            this.messageProcessor.stop();
        }

        if (log.isDebugEnabled()) {
            log.debug("Exiting service thread of message processor [" + this.messageProcessor.getName() + "]");
        }
    }

    public boolean init(JobExecutionContext jobExecutionContext) {

        JobDataMap jdm = jobExecutionContext.getMergedJobDataMap();
        Map<String, Object> parameters = (Map<String, Object>) jdm.get(MessageProcessorConstants.PARAMETERS);

        messageConsumer = ((MessageProcessor)jdm.get(MessageProcessorConstants.PROCESSOR_INSTANCE)).getMessageConsumer();
        sequence = (String) parameters.get(SamplingProcessor.SEQUENCE);
        messageProcessor = (MessageProcessor)jdm.get(MessageProcessorConstants.PROCESSOR_INSTANCE);

        String con = (String) parameters.get(SamplingProcessor.CONCURRENCY);
        if (con != null) {
            try {
                concurrency = Integer.parseInt(con);
            } catch (NumberFormatException nfe) {
                parameters.remove(SamplingProcessor.CONCURRENCY);
                log.error("Invalid value for concurrency switching back to default value", nfe);
            }
        }

        return true;
    }

    public MessageContext fetch(MessageConsumer msgConsumer) {
        MessageContext newMsg = messageConsumer.receive();
        if (newMsg != null) {
            messageConsumer.ack();
        }

        return newMsg;
    }

    public boolean dispatch(final MessageContext messageContext) {

        final ExecutorService executor = messageContext.getEnvironment().
                getExecutorService();
        executor.submit(new Runnable() {
            public void run() {
                try {
                    Mediator processingSequence = messageContext.getSequence(sequence);
                    if (processingSequence != null) {
                        processingSequence.mediate(messageContext);
                    }
                } catch (SynapseException syne) {
                    if (!messageContext.getFaultStack().isEmpty()) {
                        (messageContext.getFaultStack().pop()).handleFault(messageContext, syne);
                    }
                    log.error("Error occurred while executing the message", syne);
                } catch (Throwable t) {
                    // TODO : Should I send throw this ???
                    log.error("Error occurred while executing the message", t);
                }
            }
        });

        return true;
    }

    public boolean terminate() {
        messageConsumer.cleanup();
        return true;
    }

    public void interrupt() throws UnableToInterruptJobException {
        // we don't need do anything here since this does not block on anything. This is
        // here just to have the consistency with message forwarder.
    }
}
