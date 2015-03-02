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
 * Strategy that determine a system wide statistics view
 */
public class SystemViewStrategy implements StatisticsViewStrategy {

    private static final Log log = LogFactory.getLog(SystemViewStrategy.class);

    public Map<String, Map<String, InOutStatisticsView>> determineView(
            List<StatisticsRecord> statisticsRecords,
            ComponentType type) {

        final Map<String, Map<String, InOutStatisticsView>> statisticsMap =
                new HashMap<String, Map<String, InOutStatisticsView>>();

        if (statisticsRecords == null) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics records cannot be found.");
            }
            return statisticsMap;
        }

        final Map<String, InOutStatisticsView> perResourceMap =
                new HashMap<String, InOutStatisticsView>();

        for (StatisticsRecord record : statisticsRecords) {
            if (record != null) {

                final StatisticsUpdateStrategy strategy = new StatisticsUpdateStrategy(record);
                final Iterator<String> logIds = record.getAllLogIds(type);
                while (logIds.hasNext()) {
                    String id = logIds.next();

                    InOutStatisticsView view;
                    if (!perResourceMap.containsKey(id)) {
                        view = new InOutStatisticsView(id, Statistics.ALL, type);
                        perResourceMap.put(id, view);
                    } else {
                        view = perResourceMap.get(id);
                    }
                    updateStatistics(id, type, view, strategy);
                }
            }
        }
        statisticsMap.put(Statistics.ALL, perResourceMap);
        return statisticsMap;
    }


    public Map<String, InOutStatisticsView> determineView(String id,
                                                          List<StatisticsRecord> statisticsRecords,
                                                          ComponentType type) {
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

        InOutStatisticsView view = new InOutStatisticsView(id, Statistics.ALL, type);
        for (StatisticsRecord record : statisticsRecords) {
            if (record != null) {
                updateStatistics(id, type, view, new StatisticsUpdateStrategy(record));
            }
        }
        statisticsMap.put(Statistics.ALL, view);
        return statisticsMap;
    }

    private void updateStatistics(String id, ComponentType type, InOutStatisticsView view,
                                  StatisticsUpdateStrategy strategy) {
        if (view != null) {
            strategy.updateStatistics(id, type, view);
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
