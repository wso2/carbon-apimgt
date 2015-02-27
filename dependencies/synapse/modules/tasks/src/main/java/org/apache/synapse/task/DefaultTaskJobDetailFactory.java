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
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Default JobDetailFactory ships with synapse utils
 */
public class DefaultTaskJobDetailFactory implements TaskJobDetailFactory {

    private static final Log log = LogFactory.getLog(DefaultTaskJobDetailFactory.class);

    /**
     * @see TaskJobDetailFactory
     */
    public JobDetail createJobDetail(TaskDescription taskDescription, Map<String,
            Object> resources, Class<? extends Job> jobClass) {

        JobDetail jobDetail;
        
        if (taskDescription == null) {
            throw new SynapseTaskException("Task Description cannot be found.", log);
        }

        if (jobClass == null) {
            throw new SynapseTaskException("Job Class cannot be found.", log);
        }

        if (resources == null) {
            resources = new HashMap<String, Object>();
        }
        
        JobDataMap jobDataMap = new JobDataMap(resources);

        String className = taskDescription.getTaskClass();
        if (className != null && !"".equals(className)) {
            jobDataMap.put(TaskDescription.CLASSNAME, className);
        }

        Set xmlProperties = taskDescription.getProperties();
        if (xmlProperties != null) {
            jobDataMap.put(TaskDescription.PROPERTIES, xmlProperties);
        }

        String name = taskDescription.getName();
        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Name cannot be found.", log);
        }

        String group = taskDescription.getGroup();
        if (group == null || "".equals(group)) {
            group = TaskDescription.DEFAULT_GROUP;
        }

        jobDetail = JobBuilder.newJob(jobClass).withIdentity(
				name, group).usingJobData(jobDataMap).build();
        
        return jobDetail;
    }
}
