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

import org.apache.synapse.aspects.ComponentType;
import org.apache.synapse.aspects.statistics.StatisticsRecord;

import java.util.List;
import java.util.Map;

/**
 * Strategy for determine Statistics Views
 */
public interface StatisticsViewStrategy {

    /**
     * Return a statistics view for all resource with given type
     *
     * @param statisticsRecords Statistics Data
     * @param type              Type of resource
     * @return A particular statistics view
     */
    public Map<String, Map<String, InOutStatisticsView>> determineView(
            List<StatisticsRecord> statisticsRecords,
            ComponentType type);

    /**
     * Return a statistics view for a resource with given type and given name
     *
     * @param id                The resource name or identifier
     * @param statisticsRecords Statistics Data
     * @param type              Type of resource
     * @return A particular statistics view
     */
    public Map<String, InOutStatisticsView> determineView(String id,
                                                          List<StatisticsRecord> statisticsRecords,
                                                          ComponentType type);
}
