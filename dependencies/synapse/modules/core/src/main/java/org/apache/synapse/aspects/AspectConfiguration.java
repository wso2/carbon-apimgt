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
package org.apache.synapse.aspects;

import org.apache.synapse.Identifiable;
import org.apache.synapse.aspects.statistics.StatisticsConfigurable;

/**
 * Aspect configuration
 * Currently contains only statistics configuration related things
 */
public class AspectConfiguration implements StatisticsConfigurable, Identifiable {

    /* Whether statistics enable */
    private boolean statisticsEnable = false;
    /* Identifier for a particular aspects configuration */
    private String id;

    public AspectConfiguration(String id) {
        this.id = id;
    }

    public boolean isStatisticsEnable() {
        return statisticsEnable;
    }

    public void disableStatistics() {
        if (statisticsEnable) {
            this.statisticsEnable = false;
        }
    }

    public void enableStatistics() {
        if (!statisticsEnable) {
            statisticsEnable = true;
        }
    }

    public String getId() {
        return id;
    }
}
