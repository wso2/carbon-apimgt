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
package org.apache.synapse.message.processor.impl.forwarder;

import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.processor.MessageProcessorConstants;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.quartz.JobDataMap;

import java.util.Map;
import org.apache.synapse.message.senders.blocking.BlockingMsgSender;

/**
 * Redelivery processor is the Message processor which implements the Dead letter channel EIP
 * It will Time to time Redeliver the Messages to a given target.
 */
public class ScheduledMessageForwardingProcessor extends ScheduledMessageProcessor {

    public static final String BLOCKING_SENDER = "blocking.sender";

    private BlockingMsgSender sender = null;
    private MessageForwardingProcessorView view;

    @Override
    public void init(SynapseEnvironment se) {
        super.init(se);

        try {
            view = new MessageForwardingProcessorView(this);
        } catch (Exception e) {
            throw new SynapseException(e);
        }

        // register MBean
        org.apache.synapse.commons.jmx.MBeanRegistrar.getInstance().registerMBean(view,
                "Message Forwarding Processor view", getName());
    }

    @Override
    protected JobDataMap getJobDataMap() {
        JobDataMap jdm = new JobDataMap();
        sender = initMessageSender(parameters);

        jdm.put(BLOCKING_SENDER, sender);
        jdm.put(MessageProcessorConstants.PROCESSOR_INSTANCE, this);
        jdm.put(ForwardingProcessorConstants.TARGET_ENDPOINT, getTargetEndpoint());
        jdm.put(ForwardingProcessorConstants.THROTTLE, isThrottling(this.interval));
        jdm.put(ForwardingProcessorConstants.NON_RETRY_STATUS_CODES, this.nonRetryStatusCodes);

        return jdm;
    }

    private BlockingMsgSender initMessageSender(Map<String, Object> params) {

        String axis2repo = (String) params.get(ForwardingProcessorConstants.AXIS2_REPO);
        String axis2Config = (String) params.get(ForwardingProcessorConstants.AXIS2_CONFIG);

        sender = new BlockingMsgSender();
        if (axis2repo != null) {
            sender.setClientRepository(axis2repo);
        }
        if (axis2Config != null) {
            sender.setAxis2xml(axis2Config);
        }
        sender.init();

        return sender;
    }

    /**
     * This method is used by back end of the message processor
     * @return The associated MBean.
     */
    public MessageForwardingProcessorView getView() {
        return view;
    }
}
