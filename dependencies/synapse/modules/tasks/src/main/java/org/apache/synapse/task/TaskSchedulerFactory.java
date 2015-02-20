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


import java.util.HashMap;
import java.util.Map;

/**
 * Factory method for retrieve / create a TaskScheduler
 */
public class TaskSchedulerFactory {

    private final static Map<String, TaskScheduler> MAP = new HashMap<String, TaskScheduler>();

    private TaskSchedulerFactory() {
    }

    /**
     * Returns a TaskScheduler whose name is match with given name.
     * There is an only one instance of TaskScheduler for a given name as Factory caches
     *
     * @param name Name of the TaskScheduler
     * @return TaskScheduler instance
     */
    public static TaskScheduler getTaskScheduler(String name) {

        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Name cannot be found.");
        }

        TaskScheduler taskScheduler = MAP.get(name);
        if (taskScheduler == null) {
            taskScheduler = new TaskScheduler(name);
            MAP.put(name, taskScheduler);
        }

        return taskScheduler;
    }
}
