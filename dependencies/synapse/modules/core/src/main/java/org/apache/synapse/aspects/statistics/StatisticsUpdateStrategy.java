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
import org.apache.synapse.aspects.statistics.view.InOutStatisticsView;
import org.apache.synapse.aspects.statistics.view.Statistics;

import java.util.List;

/**
 * Updates the given statistics base on statistics logs in the given statistics record.
 * This is to use only at viewing statistics
 */
public class StatisticsUpdateStrategy {

    private final StatisticsRecord statisticsRecord;

    public StatisticsUpdateStrategy(StatisticsRecord statisticsRecord) {
        this.statisticsRecord = statisticsRecord;
    }

    public void updateStatistics(String id,
                                 ComponentType componentType,
                                 InOutStatisticsView statisticsView) {

        StatisticsLog startLog = null;
        StatisticsLog endLog = null;
        final List<StatisticsLog> statisticsLogs = statisticsRecord.getAllStatisticsLogs();
        for (StatisticsLog log : statisticsLogs) {

            if (log == null) {
                continue;
            }

            switch (componentType) {
                case SEQUENCE: {

                    if (startLog != null) {
                        if (log.isEndAnyLog() ||
                                log.getComponentType() == ComponentType.ANY) {
                            endLog = log;
                        }
                    }
                    if (componentType == log.getComponentType()) {
                        if (!id.equals(log.getId())) {
                            continue;
                        }
                        if (startLog == null) {
                            startLog = log;
                        } else {
                            endLog = log;
                        }
                    }
                    break;
                }
                default: {
                    if (componentType == log.getComponentType()) {
                        if (!id.equals(log.getId())) {
                            continue;
                        }
                        startLog = log;
                    } else if (log.getComponentType() == ComponentType.ANY) {
                        if (startLog != null) {
                            endLog = log;
                        }
                        break;
                    }
                }
            }

            if (endLog != null && startLog != null) {
                Statistics statistics;
                switch (componentType) {
                    case SEQUENCE: {
                        if (startLog.isResponse()) {
                            statistics = statisticsView.getOutStatistics();
                        } else {
                            statistics = statisticsView.getInStatistics();
                        }
                        statistics.update(endLog.getTime() - startLog.getTime(), endLog.isFault());
                        if (endLog.isFault()) {
                            statistics.addErrorLog(endLog.getErrorLog());
                        }
                        break;
                    }
                    case ENDPOINT: {
                        statistics = statisticsView.getInStatistics();
                        statistics.update(endLog.getTime() - startLog.getTime(), endLog.isFault());
                        if (endLog.isFault()) {
                            statistics.addErrorLog(endLog.getErrorLog());
                        }
                        break;
                    }
                    case PROXYSERVICE: {
                        Statistics inStatistics = statisticsView.getInStatistics();
                        Statistics outStatistics = statisticsView.getOutStatistics();
                        inStatistics.update(endLog.getTime() - startLog.getTime(), endLog.isFault());
                        if (endLog.isFault()) {
                            inStatistics.addErrorLog(endLog.getErrorLog());
                        }
                        if (!endLog.isEndAnyLog()) {
                            StatisticsLog lastLog = statisticsLogs.get(statisticsLogs.size() - 1);
                            if (lastLog != endLog) {
                                outStatistics.update(
                                        lastLog.getTime() - endLog.getTime(), lastLog.isFault());
                                if (lastLog.isFault()) {
                                    outStatistics.addErrorLog(lastLog.getErrorLog());
                                }
                            }
                        }
                        return;
                    }
                }
                startLog = null;
                endLog = null;
            }
        }

        if (startLog != null && componentType == ComponentType.PROXYSERVICE) {
            Statistics inStatistics = statisticsView.getInStatistics();
            StatisticsLog lastLog = statisticsLogs.get(statisticsLogs.size() - 1);
            if (lastLog != startLog) {
                inStatistics.update(lastLog.getTime() - startLog.getTime(), lastLog.isFault());
            }
        }
    }
}
