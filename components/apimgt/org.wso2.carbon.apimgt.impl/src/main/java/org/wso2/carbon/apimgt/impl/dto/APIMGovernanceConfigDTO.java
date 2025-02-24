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
}
