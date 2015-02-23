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
package org.apache.synapse.commons.datasource;


import java.util.HashMap;
import java.util.Map;


/**
 * Data source connection pool stats collector implementation
 */
public class DBPoolView implements DBPoolViewMBean {

    private int numActive = 0;
    private int numIdle = 0;
    private final Map<String, Long> connectionsUsage = new HashMap<String, Long>();
    private String name;

    public DBPoolView(String name) {
        this.name = name;
    }

    public int getNumActive() {
        return numActive;
    }

    public void setNumActive(int numActive) {
        this.numActive = numActive;
    }

    public int getNumIdle() {
        return numIdle;
    }

    public void setNumIdle(int numIdle) {
        this.numIdle = numIdle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateConnectionUsage(String connectionID) {
        if (connectionID != null && !"".equals(connectionID)) {
            Long currentUsage = connectionsUsage.get(connectionID);
            if (currentUsage != null) {
                currentUsage += 1;
                connectionsUsage.put(connectionID, currentUsage);
            } else {
                connectionsUsage.put(connectionID, (long) 1);
            }
        }
    }

    public Map getConnectionUsage() {
        return connectionsUsage;
    }

    public void reset() {
        numActive = 0;
        numIdle = 0;
        connectionsUsage.clear();
    }
}
