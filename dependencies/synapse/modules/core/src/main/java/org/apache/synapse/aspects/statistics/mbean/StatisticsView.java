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
package org.apache.synapse.aspects.statistics.mbean;

import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.StatisticsCollector;
import org.apache.synapse.aspects.statistics.view.InOutStatisticsView;
import org.apache.synapse.aspects.statistics.view.StatisticsViewStrategy;
import org.apache.synapse.aspects.statistics.view.SystemViewStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @see org.apache.synapse.aspects.statistics.mbean.StatisticsViewMBean
 */
public class StatisticsView implements StatisticsViewMBean {

    private final StatisticsCollector collector;
    private final StatisticsViewStrategy systemViewStrategy = new SystemViewStrategy();

    public StatisticsView(StatisticsCollector collector) {
        this.collector = collector;
    }

    public List<String> getSystemEndpointStats(String id) {
        return getAsList(
                this.systemViewStrategy.determineView(id,
                        collector.getStatisticsRecords(),
                        ComponentType.ENDPOINT));
    }

    public List<String> getSystemSequenceStats(String id) {
        return getAsList(this.systemViewStrategy.determineView(id,
                collector.getStatisticsRecords(),
                ComponentType.SEQUENCE));
    }

    public List<String> getSystemProxyServiceStats(String id) {
        return getAsList(this.systemViewStrategy.determineView(id,
                collector.getStatisticsRecords(),
                ComponentType.PROXYSERVICE));

    }

    public List<String> getSystemEndpointsStats() {
        return getAllStatsAsList(
                this.systemViewStrategy.determineView(collector.getStatisticsRecords(),
                        ComponentType.ENDPOINT));
    }

    public List<String> getSystemSequencesStats() {
        return getAllStatsAsList(
                this.systemViewStrategy.determineView(collector.getStatisticsRecords(),
                        ComponentType.SEQUENCE));
    }

    public List<String> getSystemProxyServicesStats() {
        return getAllStatsAsList(
                this.systemViewStrategy.determineView(collector.getStatisticsRecords(),
                        ComponentType.PROXYSERVICE));

    }

    public void clearAllStatistics() {
        this.collector.clearStatistics();
    }

    private List<String> getAsList(Map<String, InOutStatisticsView> viewMap) {
        List<String> returnList = new ArrayList<String>();
        for (InOutStatisticsView view : viewMap.values()) {
            if (view != null) {
                returnList.add(view.toString());
            }
        }
        return returnList;
    }

    private List<String> getAllStatsAsList(Map<String, Map<String, InOutStatisticsView>> statsMap) {
        List<String> tobeReturn = new ArrayList<String>();
        for (Map<String, InOutStatisticsView> viewMap : statsMap.values()) {
            tobeReturn.addAll(getAsList(viewMap));
        }
        return tobeReturn;
    }
}
