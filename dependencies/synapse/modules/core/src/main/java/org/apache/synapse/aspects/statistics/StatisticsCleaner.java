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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapsePropertiesLoader;

/**
 * Clean the statistics stored in the StatisticsCollector based on a policy
 */
public class StatisticsCleaner {

    private static final Log log = LogFactory.getLog(StatisticsCleaner.class);

    private final static long DEFAULT_CLEAN_INTERVAL = 1000 * 60 * 5;
    private final static String CLEAN_INTERVAL = "statistics.clean.interval";
    private final static String CLEAN_ENABLE = "statistics.clean.enable";

    private StatisticsCollector collector;
    private long cleanInterval;
    private long nextTime = -1;
    private boolean isCleanEnable = true;

    public StatisticsCleaner(StatisticsCollector collector) {
        this.collector = collector;
        this.cleanInterval = Long.parseLong(SynapsePropertiesLoader.getPropertyValue(
                CLEAN_INTERVAL,
                String.valueOf(DEFAULT_CLEAN_INTERVAL)));
        this.isCleanEnable = Boolean.parseBoolean(
                SynapsePropertiesLoader.getPropertyValue(
                        CLEAN_ENABLE, String.valueOf(true)));
        if (isCleanEnable) {
            if (log.isDebugEnabled()) {
                log.debug("Statistics cleaning is will be occured with interval : " +
                        cleanInterval / 1000 + " s.");
            }
        }
    }

    /**
     * Clean the expired statistics
     */
    public void clean() {

        try {

            if (!isCleanEnable) {
                if (log.isDebugEnabled()) {
                    log.debug("Statistics cleaning is disabled.");
                }
                return;
            }

            if (collector == null) {
                if (log.isDebugEnabled()) {
                    log.debug("There are no statistics to be cleaned.");
                }
                return;
            }

            long currentTime = System.currentTimeMillis();
            if (nextTime == -1) {
                nextTime = currentTime + cleanInterval;
            }
            
            if (nextTime <= currentTime) {
                collector.clearStatistics();
                nextTime = currentTime + cleanInterval;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("There are no expired statistics to be cleaned.");
                }
            }
        } catch (Throwable ignored) {
        }
    }
}
