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
package org.apache.synapse.message.processor.impl;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.deployers.MessageProcessorDeployer;
import org.apache.synapse.message.processor.MessageProcessorConstants;
import org.apache.synapse.message.processor.impl.forwarder.ForwardingProcessorConstants;
import org.apache.synapse.message.processor.impl.forwarder.ForwardingService;
import org.apache.synapse.message.processor.impl.sampler.SamplingProcessor;
import org.apache.synapse.message.processor.impl.sampler.SamplingService;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

public abstract class ScheduledMessageProcessor extends AbstractMessageProcessor {
    private static final Log logger = LogFactory.getLog(ScheduledMessageProcessor.class.getName());

    /**
     * The scheduler, run the the processor
     */
    protected Scheduler scheduler = null;

    /**
     * The interval at which this processor runs , default value is 1000ms
     */
    protected long interval = 1000;

    /**
     * The quartz configuration file if specified as a parameter
     */
    protected String quartzConfig = null;

    /**
     * A cron expression to run the sampler
     */
    protected String cronExpression = null;

    /**
     * This only needed for the associated service. This value could not be changed manually. Moving to this state
     * only happens when the service reaches the maximum retry limit
     */
    protected AtomicBoolean isPaused = new AtomicBoolean(false);

    private AtomicBoolean isActivated = new AtomicBoolean(true);

    /**
     * This is specially used for REST scenarios where http status codes can take semantics in a RESTful architecture.
     */
    protected String[] nonRetryStatusCodes = null;

    public void init(SynapseEnvironment se) {
        if (!isPinnedServer(se.getServerContextInformation().getServerConfigurationInformation()
                .getServerName())) {
            // If it is not a pinned server we do not start the message processor. In that server.
            setActivated(false);
        }

        super.init(se);
        StdSchedulerFactory sf = null;

        try {
            sf = new StdSchedulerFactory(getSchedulerProperties(this.name));
            if (quartzConfig != null && !"".equals(quartzConfig)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Initiating a Scheduler with configuration : " + quartzConfig);
                }
                sf.initialize(quartzConfig);
            }
        } catch (SchedulerException e) {
            throw new SynapseException("Error initiating scheduler factory "
                    + sf + "with configuration loaded from " + quartzConfig, e);
        }

        try {
            scheduler = sf.getScheduler();
        } catch (SchedulerException e) {
            throw new SynapseException("Error getting a  scheduler instance form scheduler" +
                    " factory " + sf, e);
        }

        this.start();
    }

    public boolean start() {

        try {
            if (isActivated.get()) {
                setMessageConsumer(configuration.getMessageStore(messageStore).getConsumer());
                scheduler.start();

                if (logger.isDebugEnabled()) {
                    logger.debug("Started message processor. [" + getName() + "].");
                }
            }
        } catch (SchedulerException e) {
            throw new SynapseException("Error starting the scheduler", e);
        }

        Trigger trigger;
        TriggerBuilder<Trigger> triggerBuilder = newTrigger().withIdentity(name + "-trigger");

        if (cronExpression == null || "".equals(cronExpression)) {
            trigger = triggerBuilder
                    .withSchedule(simpleSchedule()
                        .withIntervalInMilliseconds(isThrottling(this.interval) ? 1000 : this.interval)
                        .repeatForever()
                        .withMisfireHandlingInstructionNextWithRemainingCount())
                    .build();
        } else {
            trigger = triggerBuilder
                    .startNow()
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                        .withMisfireHandlingInstructionDoNothing())
                    .build();
        }

        JobDataMap jobDataMap = getJobDataMap();
        jobDataMap.put(MessageProcessorConstants.PARAMETERS, parameters);

        JobBuilder jobBuilder = getJobBuilder();
        JobDetail jobDetail = jobBuilder.usingJobData(jobDataMap).build();

        try {
            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new SynapseException("Error scheduling job : " + jobDetail
                    + " with trigger " + trigger, e);
        }

        return true;
    }

    public boolean isDeactivated() {
        try {
            return scheduler.isInStandbyMode();
        } catch (SchedulerException e) {
            throw new SynapseException("Error Standing-by Message processor scheduler ", e);
        }
    }

    public void setParameters(Map<String, Object> parameters) {

        super.setParameters(parameters);

        if (parameters != null && !parameters.isEmpty()) {
            Object o = parameters.get(MessageProcessorConstants.CRON_EXPRESSION);
            if (o != null) {
                cronExpression = o.toString();
            }
            o = parameters.get(MessageProcessorConstants.INTERVAL);
            if (o != null) {
                interval = Integer.parseInt(o.toString());
            }
            o = parameters.get(MessageProcessorConstants.QUARTZ_CONF);
            if (o != null) {
                quartzConfig = o.toString();
            }
            o = parameters.get(MessageProcessorConstants.IS_ACTIVATED);
            if (o != null) {
                isActivated.set(Boolean.valueOf(o.toString()));
            }
            o = parameters.get(ForwardingProcessorConstants.NON_RETRY_STATUS_CODES);
            if (o != null) {
                // we take it out of param set and send it because we need split the array.
                nonRetryStatusCodes = o.toString().split(",");
            }
        }
    }

    private JobBuilder getJobBuilder() {
        // This is just to set the default one
        JobBuilder jobBuilder;

        if (this instanceof SamplingProcessor) {
            jobBuilder = JobBuilder.newJob(SamplingService.class);
        }
        else {
            jobBuilder = JobBuilder.newJob(ForwardingService.class);
        }

        jobBuilder.withIdentity(name + "-job", MessageProcessorConstants.SCHEDULED_MESSAGE_PROCESSOR_GROUP);

        return jobBuilder;
    }

    protected JobDataMap getJobDataMap() {
        return new JobDataMap();
    }

    public boolean stop() {
        try {
            if (scheduler != null) {
                // There could be servers that are disabled at startup time. therefore not started but initiated.
                if (scheduler.isStarted()) {
                    // This is to immediately stop the scheduler to avoid firing new services
                    scheduler.standby();

                    if (logger.isDebugEnabled()) {
                        logger.debug("ShuttingDown Message Processor Scheduler : " + scheduler.getMetaData());
                    }

                    try {
                        scheduler.interrupt(new JobKey(name + "-job", MessageProcessorConstants.SCHEDULED_MESSAGE_PROCESSOR_GROUP));
                    } catch (UnableToInterruptJobException e) {
                        logger.info("Unable to interrupt job [" + name + "-job]");
                    }
                }

                // gracefully shutdown
                scheduler.shutdown(true);
            }

        } catch (SchedulerException e) {
            throw new SynapseException("Error ShuttingDown Message processor scheduler ", e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Stopped message processor [" + getName() + "].");
        }

        return true;
    }

    public void destroy() {
        // Since for one scheduler there is only one job, we can simply shutdown the scheduler
        // which will cause to shutdown the job

        stop();

        if (getMessageConsumer() != null) {
            boolean success = getMessageConsumer().cleanup();
            if (!success) {
                logger.error("[" + getName() + "] Could not cleanup message consumer.");
            }
        } else {
            logger.warn("[" + getName() + "] Could not find the message consumer to cleanup.");
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Successfully destroyed message processor [" + getName() + "].");
        }
    }

    public boolean deactivate() {
        try {
            if (scheduler != null && scheduler.isStarted()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Deactivating message processor [" + getName() + "]");
                }

                // This is to immediately stop the scheduler to avoid firing new services
                scheduler.standby();

                try {
                    scheduler.interrupt(new JobKey(name + "-job", MessageProcessorConstants.SCHEDULED_MESSAGE_PROCESSOR_GROUP));
                } catch (UnableToInterruptJobException e) {
                    logger.info("Unable to interrupt job [" + name + "-job]");
                }

                // This is to remove the consumer from the queue.
                messageConsumer.cleanup();

                logger.info("Successfully deactivated the message processor [" + getName() + "]");

                setActivated(isActive());

                // This means the deactivation has happened automatically. So we have to persist the
                // deactivation manually.
                if (isPaused()) {
                    try {
                        // TODO: Need to make sure if this is the best way.
                        String directory = configuration.getPathToConfigFile() + "/message-processors";
                        DeploymentEngine deploymentEngine = (DeploymentEngine) configuration.getAxisConfiguration().getConfigurator();
                        MessageProcessorDeployer dep = (MessageProcessorDeployer) deploymentEngine.getDeployer(directory, "xml");
                        dep.restoreSynapseArtifact(name);
                    } catch (Exception e) {
                        logger.warn("Couldn't persist the state of the message processor [" + name + "]");
                    }
                }

                return true;
            }
            else {
                return false;
            }
        } catch (SchedulerException e) {
            throw new SynapseException("Error Standing-by Message processor scheduler ", e);
        }
    }

    public boolean activate() {
        try {
            if (messageConsumer == null) {
                // This is for the message processors who are deactivated at startup time.
                setMessageConsumer(configuration.getMessageStore(messageStore).getConsumer());
            }
            if (scheduler != null && scheduler.isInStandbyMode()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Starting Message Processor Scheduler : " + scheduler.getMetaData());
                }

                scheduler.start();

                if (this.isPaused()) {
                    resumeService();
                }

                logger.info("Successfully re-activated the message processor [" + getName() + "]");

                setActivated(isActive());

                return true;
            }
            else {
                return false;
            }
        } catch (SchedulerException e) {
            throw new SynapseException("Error Standing-by Message processor scheduler ", e);
        }
    }

    public void pauseService() {
        try {
            this.scheduler.pauseTrigger(new TriggerKey(name + "-trigger"));
            this.isPaused.set(true);
        } catch (SchedulerException se) {
            throw new SynapseException("Error while pausing the service", se);
        }
    }

    public void resumeService() {
        try {
            this.scheduler.resumeTrigger(new TriggerKey(name + "-trigger"));
            this.isPaused.set(false);
        } catch (SchedulerException se) {
            throw new SynapseException("Error while pausing the service", se);
        }
    }

    public boolean isActive() {
        return !isDeactivated();
    }

    public boolean isPaused() {
        return isPaused.get();
    }

    public boolean getActivated() {
        return isActivated.get();
    }

    public void setActivated(boolean activated) {
        isActivated.set(activated);
        parameters.put(MessageProcessorConstants.IS_ACTIVATED, String.valueOf(getActivated()));
    }

    private Properties getSchedulerProperties(String name) {
        Properties config = new Properties();

        // This is important to have a separate scheduler for each message processor.
        config.put(MessageProcessorConstants.SCHEDULER_INSTANCE_NAME, name);
        config.put(MessageProcessorConstants.SCHEDULER_RMI_EXPORT, "false");
        config.put(MessageProcessorConstants.SCHEDULER_RMI_PROXY, "false");
        config.put(MessageProcessorConstants.SCHEDULER_WRAP_JOB_EXE_IN_USER_TRANSACTION, "false");
        config.put(MessageProcessorConstants.THREAD_POOL_CLASS, "org.quartz.simpl.SimpleThreadPool");
        // This is set to one because according to the current implementation one scheduler
        // only have one job
        config.put(MessageProcessorConstants.THREAD_POOL_THREAD_COUNT, "1");
        config.put(MessageProcessorConstants.THREAD_POOL_THREAD_PRIORITY, "5");
        config.put(MessageProcessorConstants.JOB_STORE_MISFIRE_THRESHOLD, "60000");
        config.put(MessageProcessorConstants.THREAD_INHERIT_CONTEXT_CLASSLOADER_OF_INIT_THREAD, "true");
        config.put(MessageProcessorConstants.JOB_STORE_CLASS, "org.quartz.simpl.RAMJobStore");

        return config;
    }

    private boolean isPinnedServer(String serverName) {
        boolean pinned = false;
        Object pinnedServersObj = this.parameters.get(MessageProcessorConstants.PINNED_SERVER);

        if (pinnedServersObj != null && pinnedServersObj instanceof String) {

            String pinnedServers = (String) pinnedServersObj;
            StringTokenizer st = new StringTokenizer(pinnedServers, " ,");

            while (st.hasMoreTokens()) {
                String token = st.nextToken().trim();
                if (serverName.equals(token)) {
                    pinned = true;
                    break;
                }
            }
            if (!pinned) {
                logger.info("Message processor '" + name + "' pinned on '" + pinnedServers + "' not starting on" +
                        " this server '" + serverName + "'");
            }
        } else {
            // this means we have to use the default value that is to start the message processor
            pinned = true;
        }

        return pinned;
    }

    /**
     * Quarts does not except 0 for its schedule interval. Therefore when the interval is zero we have
     * to handle as a separate case.
     * @param interval in which scheduler triggers its job.
     * @return true if it has run on non-throttle mode.
     */
    protected boolean isThrottling(long interval) {
        return interval == 0;
    }
}
