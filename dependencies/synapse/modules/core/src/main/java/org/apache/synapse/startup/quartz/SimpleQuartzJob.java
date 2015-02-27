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
package org.apache.synapse.startup.quartz;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.commons.util.PropertyHelper;
import org.apache.synapse.task.Task;

import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.task.TaskDescription;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Set;

public class SimpleQuartzJob implements Job {
    
    public static final String SYNAPSE_ENVIRONMENT = "SynapseEnvironment";
    public static final String CLASSNAME = "ClassName";
    public static final String PROPERTIES = "Properties";

    private static final Log log = LogFactory.getLog(SimpleQuartzJob.class);

    public void execute(JobExecutionContext ctx) throws JobExecutionException {

        String jobName = ctx.getJobDetail().getKey().getName();
        if (log.isDebugEnabled()) {
            log.debug("Executing task : " + jobName);
        }
        JobDataMap jdm = ctx.getMergedJobDataMap();
        String jobClassName = (String) jdm.get(CLASSNAME);
        if (jobClassName == null) {
            handleException("No " + CLASSNAME + " in JobDetails");
        }

        boolean initRequired = false;

        Task task = (Task) jdm.get(TaskDescription.INSTANCE);
        if (task == null) {
            initRequired = true;
        }

        SynapseEnvironment se = (SynapseEnvironment) jdm.get("SynapseEnvironment");

        if (initRequired) {
            try {
                task = (Task) getClass().getClassLoader().loadClass(jobClassName).newInstance();
            } catch (Exception e) {
                handleException("Cannot instantiate task : " + jobClassName, e);
            }

            Set properties = (Set) jdm.get(PROPERTIES);
            for (Object property : properties) {
                OMElement prop = (OMElement) property;
                log.debug("Found Property : " + prop.toString());
                PropertyHelper.setStaticProperty(prop, task);
            }

            // 1. Initialize
            if (task instanceof ManagedLifecycle && se != null) {
                ((ManagedLifecycle) task).init(se);
            }
        }

        // 2. Execute
        if (se != null && task != null && se.isInitialized()) {
            task.execute();
        }

        if (initRequired) {
            // 3. Destroy
            if (task instanceof ManagedLifecycle && se != null) {
                ((ManagedLifecycle) task).destroy();
            }
        }
    }

    private void handleException(String msg) throws JobExecutionException {
        log.error(msg);
        throw new JobExecutionException(msg);
    }

    private void handleException(String msg, Exception e) throws JobExecutionException {
        log.error(msg, e);
        throw new JobExecutionException(msg, e);
    }

}
