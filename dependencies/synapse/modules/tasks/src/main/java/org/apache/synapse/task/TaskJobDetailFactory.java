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

import org.quartz.Job;
import org.quartz.JobDetail;

import java.util.Map;

/**
 * Creates a JobDetail based on give Task Description , additional resources and job class
 */
public interface TaskJobDetailFactory {

    /**
     * Factory method for creating a JobDetail based on give Task Description ,
     * additional resources and job class
     *
     * @param taskDescription TaskDescription instance
     * @param resources       Additional resources .
     *                        This can be used to provide context specific resources
     *                        Example : Synapse Environment
     * @param jobClass        Job class
     * @return JobDetail instance , if there are enough information to create a instance ,
     *         otherwise , return null
     */
    JobDetail createJobDetail(TaskDescription taskDescription, Map<String,
            Object> resources, Class<? extends Job> jobClass);
}
