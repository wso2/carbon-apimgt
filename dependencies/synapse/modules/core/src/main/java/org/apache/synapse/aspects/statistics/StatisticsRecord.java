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
package org.apache.synapse.aspects.statistics;

import org.apache.synapse.aspects.ComponentType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Holds a record for statistics for current message
 */
@SuppressWarnings("unused")
public class StatisticsRecord {

    private String id;
    private final List<StatisticsLog> statisticsLogs = new CopyOnWriteArrayList<StatisticsLog>();
    private String clientIP;
    private String clientHost;
    private ComponentType owner;
    private boolean isEndReported = false;

    public StatisticsRecord(String id, String clientIP, String clientHost) {
        this.id = id;
        this.clientIP = clientIP;
        this.clientHost = clientHost;
    }

    public String getId() {
        return id;
    }

    public String getClientIP() {
        return clientIP;
    }

    public String getClientHost() {
        return clientHost;
    }

    /**
     * Collecting statistics for a particular component
     *
     * @param log StatisticsLog
     */
    public void collect(StatisticsLog log) {
        if (log != null) {
            statisticsLogs.add(log);
        }
    }

    /**
     * Gets all the StatisticsLogs
     *
     * @return A Iterator for all StatisticsLogs
     */
    public List<StatisticsLog> getAllStatisticsLogs() {
        final List<StatisticsLog> logs = new ArrayList<StatisticsLog>();
        logs.addAll(statisticsLogs);
        return logs;
    }

    /**
     * Get all log ids related with a given component
     *
     * @param componentType The component that belong statistics
     * @return A List of Log ids
     */
    public Iterator<String> getAllLogIds(ComponentType componentType) {
        final List<String> logIds = new ArrayList<String>();
        for (StatisticsLog startLog : statisticsLogs) {
            if (startLog != null && startLog.getComponentType() == componentType) {
                String id = startLog.getId();
                if (id != null && !"".equals(id) && !logIds.contains(id)) {
                    logIds.add(id);
                }
            }
        }
        return logIds.iterator();
    }


    public void clearLogs() {
        statisticsLogs.clear();
    }

    public ComponentType getOwner() {
        return owner;
    }

    public void setOwner(ComponentType owner) {
        this.owner = owner;
    }

    public boolean isEndReported() {
        return isEndReported;
    }

    public void setEndReported(boolean endReported) {
        isEndReported = endReported;
    }

    public String toString() {
        return new StringBuffer()
                .append("[Message id : ").append(id).append(" ]")
                .append("[Remote  IP : ").append(clientIP).append(" ]")
                .append("[Remote host : ").append(clientHost).append(" ]")
                .toString();
    }
}
