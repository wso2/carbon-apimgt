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
package org.apache.synapse.message.processor;

public final class MessageProcessorConstants {

    public static final String PARAMETERS = "parameters";

    /**
     * Scheduled Message Processor parameters
     */
    public static final String QUARTZ_CONF = "quartz.conf";
    public static final String INTERVAL = "interval";
    public static final String CRON_EXPRESSION = "cronExpression";

    /**
     * Message processor parameters
     */
    public static final String MAX_DELIVER_ATTEMPTS = "max.delivery.attempts";

    /**
     * This is used to control the retry rate when the front end client is not reachable.
     */
    public static final String RETRY_INTERVAL = "client.retry.interval";

    public static final String IS_ACTIVATED = "is.active";

    /**
     * These configurations are belong to quartz scheduler. More information about these scheduler parameters
     * can be found in http://quartz-scheduler.org/
     */
    public static final String SCHEDULER_INSTANCE_NAME = "org.quartz.scheduler.instanceName";

    public static final String SCHEDULER_RMI_EXPORT = "org.quartz.scheduler.rmi.export";

    public static final String SCHEDULER_RMI_PROXY = "org.quartz.scheduler.rmi.proxy";

    public static final String SCHEDULER_WRAP_JOB_EXE_IN_USER_TRANSACTION =
                                                              "org.quartz.scheduler.wrapJobExecutionInUserTransaction";

    public static final String THREAD_POOL_CLASS = "org.quartz.threadPool.class";

    public static final String THREAD_POOL_THREAD_COUNT = "org.quartz.threadPool.threadCount";

    public static final String THREAD_POOL_THREAD_PRIORITY = "org.quartz.threadPool.threadPriority";

    public static final String JOB_STORE_MISFIRE_THRESHOLD = "org.quartz.jobStore.misfireThreshold";

    public static final String THREAD_INHERIT_CONTEXT_CLASSLOADER_OF_INIT_THREAD =
            "org.quartz.threadPool.threadsInheritContextClassLoaderOfInitializingThread";

    public static final String JOB_STORE_CLASS = "org.quartz.jobStore.class";

    public static final String SCHEDULED_MESSAGE_PROCESSOR_GROUP =
            "synapse.message.processor.quartz";
    public static final String PROCESSOR_INSTANCE = "processor.instance";
    public static final String PINNED_SERVER = "pinnedServers";

    /** Deprecated message processor implementation class names**/
    public static final String DEPRECATED_SAMPLING_PROCESSOR_CLASS =
            "org.apache.synapse.message.processors.sampler.SamplingProcessor";
    public static final String DEPRECATED_FORWARDING_PROCESSOR_CLASS =
            "org.apache.synapse.message.processors.forward.ScheduledMessageForwardingProcessor";

}
