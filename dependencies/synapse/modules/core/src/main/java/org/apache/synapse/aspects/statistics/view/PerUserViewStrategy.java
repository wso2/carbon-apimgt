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
package org.apache.synapse.aspects.statistics.view;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.StatisticsRecord;
import org.apache.synapse.aspects.statistics.StatisticsUpdateStrategy;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Strategy that determine a per user of remote party statistics view
 */
public abstract class PerUserViewStrategy implements StatisticsViewStrategy {

    protected final static int IP = 0;
    protected final static int DOMAIN = 1;
    private Log log;

    protected PerUserViewStrategy() {
        log = LogFactory.getLog(getClass());
    }

    public Map<String, Map<String, InOutStatisticsView>> determineView(
            List<StatisticsRecord> statisticsRecords,
            ComponentType type, int userIDType) {

        final Map<String, Map<String, InOutStatisticsView>> statisticsMap =
                new HashMap<String, Map<String, InOutStatisticsView>>();

        if (statisticsRecords == null) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics records cannot be found.");
            }
            return statisticsMap;
        }

        Map<String, InOutStatisticsView> perUserMap = new HashMap<String, InOutStatisticsView>();

        for (StatisticsRecord record : statisticsRecords) {
            if (record != null) {

                String userID;
                if (IP == userIDType) {
                    userID = record.getClientIP();
                } else {
                    userID = record.getClientHost();
                }

                if (userID == null || "".equals(userID)) {
                    if (log.isDebugEnabled()) {
                        log.debug("user ID cannot be found.");
                    }
                    continue;
                }

                Map<String, InOutStatisticsView> perResourceMap;
                if (statisticsMap.containsKey(userID)) {
                    perResourceMap = statisticsMap.get(userID);
                } else {
                    perResourceMap = new HashMap<String, InOutStatisticsView>();
                    statisticsMap.put(userID, perResourceMap);
                }

                if (perResourceMap == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("There are not statistics for user ID : " + userID);
                    }
                    continue;
                }

                final StatisticsUpdateStrategy strategy = new StatisticsUpdateStrategy(record);
                final Iterator<String> logIds = record.getAllLogIds(type);
                while (logIds.hasNext()) {
                    String id = logIds.next();

                    InOutStatisticsView view;
                    if (!perUserMap.containsKey(id)) {
                        view = new InOutStatisticsView(id, userID, type);
                        perUserMap.put(id, view);
                    } else {
                        view = perUserMap.get(id);
                    }
                    updateStatistics(id, type, view, strategy);
                }

            }
        }
        return statisticsMap;
    }


    public Map<String, InOutStatisticsView> determineView(String id,
                                                          List<StatisticsRecord> statisticsRecords,
                                                          ComponentType type, int userIDType) {
        if (id == null || "".equals(id)) {
            handleException("Resource Id cannot be null");
        }

        Map<String, InOutStatisticsView> statisticsMap = new HashMap<String, InOutStatisticsView>();
        if (statisticsRecords == null) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics records cannot be found.");
            }
            return statisticsMap;
        }

        for (StatisticsRecord record : statisticsRecords) {
            if (record != null) {

                String userID;
                if (IP == userIDType) {
                    userID = record.getClientIP();
                } else {
                    userID = record.getClientHost();
                }

                if (userID == null || "".equals(userID)) {
                    if (log.isDebugEnabled()) {
                        log.debug("user ID cannot be found.");
                    }
                    continue;
                }

                InOutStatisticsView view;
                if (statisticsMap.containsKey(userID)) {
                    view = statisticsMap.get(userID);
                } else {
                    view = new InOutStatisticsView(id, userID, type);
                    statisticsMap.put(userID, view);
                }
                updateStatistics(id, type, view, new StatisticsUpdateStrategy(record));
            }
        }

        return statisticsMap;
    }

    private void updateStatistics(String id, ComponentType type, InOutStatisticsView view,
                                  StatisticsUpdateStrategy strategy) {
        if (view != null) {
            strategy.updateStatistics(id, type, view);
        }
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
