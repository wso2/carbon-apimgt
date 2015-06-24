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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Local repository for holds Task descriptions
 */
@SuppressWarnings({"UnusedDeclaration"})
public class TaskDescriptionRepository {

    private static final Log log = LogFactory.getLog(TaskDescriptionRepository.class);
    private final Map<String, TaskDescription> taskDescriptionMap
            = new HashMap<String, TaskDescription>();

    /**
     * Stores a given TaskDescription
     *
     * @param taskDescription TaskDescription instance
     */
    public void addTaskDescription(TaskDescription taskDescription) {

        validateTaskDescription(taskDescription);

        String name = taskDescription.getName();
        validateName(name);
        validateUniqueness(name);

        if (log.isDebugEnabled()) {
            log.debug("Storing a TaskDescription : " + taskDescription);
        }
        taskDescriptionMap.put(name, taskDescription);

    }

    /**
     * Gets a TaskDescription
     *
     * @param name Name of the TaskDescription to be looked up
     * @return TaskDescription instance
     */
    public TaskDescription getTaskDescription(String name) {
        validateName(name);
        TaskDescription taskDescription = taskDescriptionMap.get(name);
        if (taskDescription == null) {
            if (log.isDebugEnabled()) {
                log.debug("TaskDescription cannot be found for name :" + name +
                        " , returning null");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Returning TaskDescription : " + taskDescription);
            }
        }
        return taskDescription;
    }

    /**
     * Removing a TaskDescription
     *
     * @param name Name of the TaskDescription to be removed
     * @return Removed TaskDescription instance
     */
    public TaskDescription removeTaskDescription(String name) {
        validateName(name);
        if (log.isDebugEnabled()) {
            log.debug("Removing a TaskDescription with name : " + name);
        }
        TaskDescription taskDescription = taskDescriptionMap.remove(name);
        if (taskDescription == null) {
            if (log.isDebugEnabled()) {
                log.debug("There is no TaskDescription to be removed with name : " + name);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Removed TaskDescription : " + taskDescription);
            }
        }
        return taskDescription;

    }

    /**
     * Return all TaskDescritions
     *
     * @return Iterator for access taskDescritions
     */
    public Iterator<TaskDescription> getAllTaskDescriptions() {
        return taskDescriptionMap.values().iterator();
    }

    /**
     * Explicit check for determine whether there is a task description with a name in interest
     *
     * @param name Name of the TaskDescription
     * @return Returns true , if there is no TaskDescription associated with given name ,
     *         otherwise , false
     */
    public boolean isUnique(String name) {
        validateName(name);
        return taskDescriptionMap.isEmpty() || !taskDescriptionMap.containsKey(name);
    }

    public void clear() {
        this.taskDescriptionMap.clear();
    }

    private void validateName(String name) {
        if (name == null || "".equals(name)) {
            throw new SynapseTaskException("Task name is null or empty", log);
        }

    }

    private void validateUniqueness(String name) {
        if (taskDescriptionMap.containsKey(name)) {
            throw new SynapseTaskException("Name with ' " + name + " ' is already there", log);
        }
    }

    private void validateTaskDescription(TaskDescription taskDescription) {
        if (taskDescription == null) {
            throw new SynapseTaskException("TaskDescription is null", log);
        }

    }
}
