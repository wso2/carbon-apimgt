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
package org.apache.synapse.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Abstraction for scheduling a Task
 */
@SuppressWarnings({"UnusedDeclaration"})
public class TaskScheduler {

    private static Log log = LogFactory.getLog(TaskScheduler.class);

    /**
     * scheduler instance
     */
    private Scheduler scheduler;

    /** 
     * Determines whether scheduler has been initialized and is ready to schedule a task or not.
     */
    private boolean initialized = false;

    /**
     * Default trigger factory
     */
    private TaskTriggerFactory triggerFactory = new DefaultTaskTriggerFactory();
    /**
     * Default job detail factory
     */
    private TaskJobDetailFactory jobDetailFactory = new DefaultTaskJobDetailFactory();

    /**
     * Property look up key for get a quartz configuration
     */
    public final static String QUARTZ_CONF = "quartz.conf";

    /**
     * Name of the scheduler
     */
    private String name;

    public TaskScheduler(String name) {
        this.name = name;
    }

    /**
     * Initialize the scheduler based on provided properties
     * Looking for  'quartz.conf' and if found , use it for initiating quartz scheduler
     *
     * @param properties Properties
     */
    public void init(Properties properties) {

        StdSchedulerFactory sf = new StdSchedulerFactory();

        if (properties != null) {
            String quartzConf = properties.getProperty(QUARTZ_CONF);
            try {
                if (quartzConf != null && !"".equals(quartzConf)) {

                    if (log.isDebugEnabled()) {
                        log.debug("Initiating a Scheduler with configuration : " + quartzConf);
                    }

                    sf.initialize(quartzConf);
                }
            } catch (SchedulerException e) {
                throw new SynapseTaskException("Error initiating scheduler factory "
                        + sf + "with configuration loaded from " + quartzConf, e, log);
            }
        }

        try {

            if (name != null) {
                scheduler = sf.getScheduler(name);
            }
            if (scheduler == null) {
                scheduler = sf.getScheduler();
            }

        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error getting a  scheduler instance form scheduler" +
                    " factory " + sf, e, log);
        }
        initialized = true;
        start();
    }

    /**
     * Explicitly start up call for scheduler, return if already it has been started
     */
    public void start() {

        assertInitialized();
        try {
            if (!scheduler.isStarted()) {

                if (log.isDebugEnabled()) {
                    log.debug("Starting a Scheduler : [ " + scheduler.getMetaData() + " ]");
                }
                scheduler.start();
            }
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error starting scheduler ", e, log);
        }
    }

    /**
     * Pauses all tasks.
     * 
     * @throws SynapseTaskException if an error occurs pausing all tasks.
     */
    public void pauseAll() {
      
        try {
            assertInitialized();
            assertStarted();
            
            scheduler.pauseAll();
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error pausing tasks ", e, log);
        }
    }
    
    public void resumeAll() {
        try {
            assertInitialized();
            assertStarted();
            
            scheduler.resumeAll();
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error resuming tasks ", e, log);
        }
    }
    
    /**
     * Schedule a Task
     *
     * @param taskDescription TaskDescription , an information about Task
     * @param resources       Any initial resources for task
     * @param jobClass        Quartz job class
     */
    public void scheduleTask(TaskDescription taskDescription, Map<String,
            Object> resources, Class<? extends Job> jobClass) {

        assertInitialized();
        assertStarted();

        if (taskDescription == null) {
            throw new SynapseTaskException("Task Description cannot be found", log);
        }

        if (jobClass == null) {
            throw new SynapseTaskException("Job Class cannot be found", log);
        }

        if (triggerFactory == null) {
            throw new SynapseTaskException("TriggerFactory cannot be found", log);
        }

        if (jobDetailFactory == null) {
            throw new SynapseTaskException("JobDetailFactory cannot be found", log);
        }

        Trigger trigger = triggerFactory.createTrigger(taskDescription);
        if (trigger == null) {
            throw new SynapseTaskException("Trigger cannot be created from : "
                    + taskDescription, log);
        }

        JobDetail jobDetail = jobDetailFactory.createJobDetail(taskDescription,
                resources, jobClass);
        if (jobDetail == null) {
            throw new SynapseTaskException("JobDetail cannot be created from : " + taskDescription +
                    " and job class " + jobClass.getName(), log);
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("scheduling job : " + jobDetail + " with trigger " + trigger);
            }
            if (taskDescription.getCount() != 0 && !isTaskAlreadyRunning(jobDetail.getKey())) {
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (ObjectAlreadyExistsException e) {
                    log.warn("did not schedule the job : " + jobDetail + ". the job is already running.");
                }

            } else {
                if (log.isDebugEnabled()) {
                    log.debug("did not schedule the job : " + jobDetail + ". count is zero.");
                }
            }
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error scheduling job : " + jobDetail
                    + " with trigger " + trigger);
        }

    }

    /**
     * Schedule a Task
     *
     * @param taskDescription TaskDescription , an information about Task
     * @param resources       Any initial resources for task
     * @param jobClass        Quartz job class
     * @param task            The task to be executed
     */
    public void scheduleTask(TaskDescription taskDescription, Map<String,
            Object> resources, Class<? extends Job> jobClass, Task task) {

        assertInitialized();
        assertStarted();

        if (taskDescription == null) {
            throw new SynapseTaskException("Task Description cannot be found", log);
        }

        if (jobClass == null) {
            throw new SynapseTaskException("Job Class cannot be found", log);
        }

        if (triggerFactory == null) {
            throw new SynapseTaskException("TriggerFactory cannot be found", log);
        }

        if (jobDetailFactory == null) {
            throw new SynapseTaskException("JobDetailFactory cannot be found", log);
        }

        Trigger trigger = triggerFactory.createTrigger(taskDescription);
        if (trigger == null) {
            throw new SynapseTaskException("Trigger cannot be created from : "
                    + taskDescription, log);
        }

        JobDetail jobDetail = jobDetailFactory.createJobDetail(taskDescription,
                resources, jobClass);
        if (jobDetail == null) {
            throw new SynapseTaskException("JobDetail cannot be created from : " + taskDescription +
                    " and job class " + jobClass.getName(), log);
        }

        jobDetail.getJobDataMap().put(TaskDescription.INSTANCE, task);

        try {
            if (log.isDebugEnabled()) {
                log.debug("scheduling job : " + jobDetail + " with trigger " + trigger);
            }
            if (taskDescription.getCount() != 0 && !isTaskAlreadyRunning(jobDetail.getKey())) {
                try {
                    scheduler.scheduleJob(jobDetail, trigger);
                } catch (ObjectAlreadyExistsException e) {
                    log.warn("did not schedule the job : " + jobDetail + ". the job is already running.");
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("did not schedule the job : " + jobDetail + ". count is zero.");
                }
            }
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error scheduling job : " + jobDetail
                    + " with trigger " + trigger);
        }

    }

    /**
     * ShutDown the underlying quartz scheduler
     */
    public void shutDown() {

        if (isInitialized()) {

            try {
                if (scheduler != null && scheduler.isStarted()) {
                    if (log.isDebugEnabled()) {
                        log.debug("ShuttingDown Task Scheduler : " + scheduler.getMetaData());
                    }
                    scheduler.shutdown();
                }
                initialized = false;
            } catch (SchedulerException e) {
                throw new SynapseTaskException("Error ShuttingDown task scheduler ", e, log);
            }
        }
    }

    /**
     * @return Returns true if the scheduler is ready for schedule a task
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Deletes a Task
     *
     * @param name  Name of the Task
     * @param group Group name of the task
     *              Default value @see org.apache.synapse.util.task.TaskDescription.DEFAULT_GROUP
     */
    public void deleteTask(String name, String group) {

        assertInitialized();
        assertStarted();

        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Task Name can not be null", log);
        }

        if (group == null || "".equals(group)) {
            group = TaskDescription.DEFAULT_GROUP;
            if (log.isDebugEnabled()) {
                log.debug("Task group is null or empty , using default group :"
                        + TaskDescription.DEFAULT_GROUP);
            }
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("Deleting a Job with [ Name :" + name + " ]" +
                        " [ Group :" + group + " ]");
            }
            scheduler.deleteJob(new JobKey(name, group));
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error deleting a job with  [ Name :" + name + " ]" +
                    " [ Group :" + group + " ]");
        }
    }
    
    public int getRunningTaskCount(){
    
        int runningTasks = 0;
        try {
            if (scheduler != null) {
                runningTasks = scheduler.getCurrentlyExecutingJobs().size();
            }
        } catch (SchedulerException e) {
            log.error("Error querying currently executing jobs", e);
        }
        return runningTasks;
    }

    public boolean isTaskAlreadyRunning(JobKey jobKey) throws SchedulerException {
        List<JobExecutionContext> currentJobs = this.scheduler.getCurrentlyExecutingJobs();
        JobKey currentJobKey;
        for (JobExecutionContext jobCtx : currentJobs) {
            currentJobKey = jobCtx.getJobDetail().getKey();
            if (currentJobKey.compareTo(jobKey) == 0) {
                //found it!
                log.warn("the job is already running");
                return true;
            }
        }
        return false;
    }

    /**
     * Sets a Trigger Factory , if it needs to void using default factory
     *
     * @param triggerFactory TaskTriggerFactory instance
     */
    public void setTriggerFactory(TaskTriggerFactory triggerFactory) {
        this.triggerFactory = triggerFactory;
    }

    /**
     * Sets a JobDetail Factory, if it needs to void using default factory
     *
     * @param jobDetailFactory TaskJobDetailFactory instance
     */
    public void setJobDetailFactory(TaskJobDetailFactory jobDetailFactory) {
        this.jobDetailFactory = jobDetailFactory;
    }

    @Override
    public String toString() {
        StringBuffer schedulerMetaData = new StringBuffer();
        if (scheduler != null) {
            schedulerMetaData = schedulerMetaData.append("[ Scheduler : ")
                    .append(scheduler).append(" ]");
        }
        return new StringBuffer().append("[ TaskScheduler[ Name :").
                append(name).append("]").append(schedulerMetaData).append(" ]").toString();
    }

    private void assertInitialized() {

        if (!initialized) {
            throw new SynapseTaskException("Scheduler has not been initialled yet", log);
        }
    }

    private void assertStarted() {

        try {
            if (!scheduler.isStarted()) {
                throw new SynapseTaskException("Scheduler has not been started yet", log);
            }
        } catch (SchedulerException e) {
            throw new SynapseTaskException("Error determine start state of the scheduler ", e, log);
        }
    }

}
