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

import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.processor.MessageProcessorConstants;
import org.apache.synapse.message.processor.impl.ScheduledMessageProcessor;
import org.quartz.JobDataMap;

public class SamplingProcessor extends ScheduledMessageProcessor {

    public static final String CONCURRENCY = "concurrency";
    public static final String SEQUENCE = "sequence";

    private SamplingProcessorView view;

    @Override
    public void init(SynapseEnvironment se) {
        super.init(se);

        try {
            view = new SamplingProcessorView(this);
        } catch (Exception e) {
            throw new SynapseException(e);
        }

        // register MBean
        org.apache.synapse.commons.jmx.MBeanRegistrar.getInstance().registerMBean(view,
                "Message Sampling Processor view", getName());
    }

    @Override
    protected JobDataMap getJobDataMap() {
        JobDataMap jdm = new JobDataMap();
        jdm.put(MessageProcessorConstants.PROCESSOR_INSTANCE, this);
        return jdm;
    }

    public SamplingProcessorView getView() {
        return view;
    }
}
