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
package org.apache.synapse.experimental.mediators.seda;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */

public class SEDAQueuePolicy {

    public static final String QUEUE_TYPE_LINKED_BLOCKING = "LinkedBlocking";
    public static final String QUEUE_TYPE_PRIORITY_BLOCKING = "PriorityBlocking";
    public static final String QUEUE_TYPE_SYNCHRONOUS = "Synchronous";
    private int queueSize = 100;
    private int queueWorkers;
    private String queueType = QUEUE_TYPE_LINKED_BLOCKING;
    private final Map<String, String> properties = new HashMap<String, String>();

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getQueueWorkers() {
        return queueWorkers;
    }

    public void setQueueWorkers(int queueWorkers) {
        this.queueWorkers = queueWorkers;
    }

    public String getQueueType() {
        return queueType;
    }

    public void setQueueType(String queueType) {
        this.queueType = queueType;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void addProperties(String name, String value) {
        this.properties.put(name, value);
    }
}
