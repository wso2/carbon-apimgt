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
package org.apache.synapse.task.service;

import org.apache.synapse.task.TaskDescription;

import java.util.List;

/**
 * A service for management of task
 * Implementation can be a web service , OSGi service , etc
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface TaskManagementService {

    /**
     * Adding a  TaskDescription instance
     *
     * @param taskDescription TaskDescription instance
     */
    void addTaskDescription(TaskDescription taskDescription);

    /**
     * Deletes a TaskDescription instance with given name
     *
     * @param name Name of the TaskDescription  to be deleted
     */
    void deleteTaskDescription(String name);

    /**
     * Edit a TaskDescription
     * Remove existing one with new one
     *
     * @param taskDescription New TaskDescription instance
     */
    void editTaskDescription(TaskDescription taskDescription);

    /**
     * Lists of all TaskDescription instances
     *
     * @return A List of TaskDescription
     */
    List<TaskDescription> getAllTaskDescriptions();

    /**
     * Get a TaskDescription instance with given name
     *
     * @param name Name of the TaskDescription instance to be returned
     * @return TaskDescription instance
     */
    TaskDescription getTaskDescription(String name);

    /**
     * Explicit check for availability of a particular TaskDescription instance
     *
     * @param name Name of the TaskDescription instance
     * @return Returns true if there is a TaskDescription instance with given name ,
     *         otherwise , false
     */
    boolean isContains(String name);

    /**
     * Returns list of names of assignable instance properties of the task implementation
     *
     * @param taskClass A name of the task implementation
     * @return List of assignable property's names
     */
    List<String> getPropertyNames(String taskClass);
}
