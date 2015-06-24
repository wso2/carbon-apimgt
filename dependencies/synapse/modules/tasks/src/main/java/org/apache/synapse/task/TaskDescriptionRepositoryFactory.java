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
import java.util.Map;

public class TaskDescriptionRepositoryFactory {

    private static final Log log = LogFactory.getLog(TaskDescriptionRepositoryFactory.class);
    private final static Map<String, TaskDescriptionRepository> repositoryMap
            = new HashMap<String, TaskDescriptionRepository>();

    private TaskDescriptionRepositoryFactory() {
    }

    /**
     * Returns a TaskDescriptionRepository instance
     * There is an only one instance of TaskDescriptionRepository for a given id as
     *  Factory caches TaskDescriptionRepositories
     *
     * @param id Identifier for TaskDescriptionRepository
     * @return TaskDescriptionRepository instance
     */
    public static TaskDescriptionRepository getTaskDescriptionRepository(String id) {

        if (id == null || "".equals(id)) {
            throw new SynapseTaskException("Name cannot be found.", log);
        }

        TaskDescriptionRepository repository = repositoryMap.get(id);
        if (repository == null) {
            if (log.isDebugEnabled()) {
                log.debug("Creating a TaskDescriptionRepository with id : " + id);
            }
            repository = new TaskDescriptionRepository();
            repositoryMap.put(id, repository);
        }
        return repository;
    }
}
