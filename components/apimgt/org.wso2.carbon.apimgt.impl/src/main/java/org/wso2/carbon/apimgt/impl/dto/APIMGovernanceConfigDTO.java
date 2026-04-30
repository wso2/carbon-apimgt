/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

/**
 * This class represent the APIM Governance Configuration DTO.
 */
public class APIMGovernanceConfigDTO {
    private String dataSourceName;
    private int schedulerThreadPoolSize;
    private int schedulerQueueSize;

    private int schedulerTaskCheckInterval;
    private int schedulerTaskCleanupInterval;

    // Deduplication configuration
    private boolean deduplicationEnabled = true;
    private double deduplicationSimilarityThreshold = 0.95;
    private double deduplicationHighConfidenceThreshold = 0.99;
    private String deduplicationMode = "audit";
    private int deduplicationNumHashFunctions = 256;
    private int deduplicationNumBands = 32;
    private int deduplicationShingleSize = 5;

    // Deprecation Guide configuration
    private boolean deprecationGuideEnabled = false;
    private int deprecationGuideScanIntervalMinutes = 60;
    private double deprecationGuideSuccessorSimilarityThreshold = 0.6;

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public int getSchedulerThreadPoolSize() {
        return schedulerThreadPoolSize;
    }

    public void setSchedulerThreadPoolSize(int schedulerThreadPoolSize) {
        this.schedulerThreadPoolSize = schedulerThreadPoolSize;
    }

    public int getSchedulerQueueSize() {
        return schedulerQueueSize;
    }

    public void setSchedulerQueueSize(int schedulerQueueSize) {
        this.schedulerQueueSize = schedulerQueueSize;
    }

    public int getSchedulerTaskCheckInterval() {
        return schedulerTaskCheckInterval;
    }

    public void setSchedulerTaskCheckInterval(int schedulerTaskCheckInterval) {
        this.schedulerTaskCheckInterval = schedulerTaskCheckInterval;
    }

    public int getSchedulerTaskCleanupInterval() {
        return schedulerTaskCleanupInterval;
    }

    public void setSchedulerTaskCleanupInterval(int schedulerTaskCleanupInterval) {
        this.schedulerTaskCleanupInterval = schedulerTaskCleanupInterval;
    }

    // Deduplication getters/setters
    public boolean isDeduplicationEnabled() {
        return deduplicationEnabled;
    }

    public void setDeduplicationEnabled(boolean deduplicationEnabled) {
        this.deduplicationEnabled = deduplicationEnabled;
    }

    public double getDeduplicationSimilarityThreshold() {
        return deduplicationSimilarityThreshold;
    }

    public void setDeduplicationSimilarityThreshold(double deduplicationSimilarityThreshold) {
        this.deduplicationSimilarityThreshold = deduplicationSimilarityThreshold;
    }

    public double getDeduplicationHighConfidenceThreshold() {
        return deduplicationHighConfidenceThreshold;
    }

    public void setDeduplicationHighConfidenceThreshold(double deduplicationHighConfidenceThreshold) {
        this.deduplicationHighConfidenceThreshold = deduplicationHighConfidenceThreshold;
    }

    public String getDeduplicationMode() {
        return deduplicationMode;
    }

    public void setDeduplicationMode(String deduplicationMode) {
        this.deduplicationMode = deduplicationMode;
    }

    public int getDeduplicationNumHashFunctions() {
        return deduplicationNumHashFunctions;
    }

    public void setDeduplicationNumHashFunctions(int deduplicationNumHashFunctions) {
        this.deduplicationNumHashFunctions = deduplicationNumHashFunctions;
    }

    public int getDeduplicationNumBands() {
        return deduplicationNumBands;
    }

    public void setDeduplicationNumBands(int deduplicationNumBands) {
        this.deduplicationNumBands = deduplicationNumBands;
    }

    public int getDeduplicationShingleSize() {
        return deduplicationShingleSize;
    }

    public void setDeduplicationShingleSize(int deduplicationShingleSize) {
        this.deduplicationShingleSize = deduplicationShingleSize;
    }

    // Deprecation Guide getters/setters
    public boolean isDeprecationGuideEnabled() {
        return deprecationGuideEnabled;
    }

    public void setDeprecationGuideEnabled(boolean deprecationGuideEnabled) {
        this.deprecationGuideEnabled = deprecationGuideEnabled;
    }

    public int getDeprecationGuideScanIntervalMinutes() {
        return deprecationGuideScanIntervalMinutes;
    }

    public void setDeprecationGuideScanIntervalMinutes(int deprecationGuideScanIntervalMinutes) {
        this.deprecationGuideScanIntervalMinutes = deprecationGuideScanIntervalMinutes;
    }

    public double getDeprecationGuideSuccessorSimilarityThreshold() {
        return deprecationGuideSuccessorSimilarityThreshold;
    }

    public void setDeprecationGuideSuccessorSimilarityThreshold(double deprecationGuideSuccessorSimilarityThreshold) {
        this.deprecationGuideSuccessorSimilarityThreshold = deprecationGuideSuccessorSimilarityThreshold;
    }
}
