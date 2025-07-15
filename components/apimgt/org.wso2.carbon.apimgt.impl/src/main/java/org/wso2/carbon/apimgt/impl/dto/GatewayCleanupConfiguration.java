/*
 * Copyright (c) 2025, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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
 * Configuration DTO for Gateway Cleanup settings
 */
public class GatewayCleanupConfiguration {
    
    private boolean enabled = false;
    private int expireTimeSeconds = 30; // Default 30 Seconds
    private int dataRetentionPeriodSeconds = 120; // Default 24 hours (1440 Seconds)
    private int cleanupIntervalSeconds = 60; // Default 60 Seconds
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public int getExpireTimeSeconds() {
        return expireTimeSeconds;
    }
    
    public void setExpireTimeSeconds(int expireTimeSeconds) {
        this.expireTimeSeconds = expireTimeSeconds;
    }
    
    public int getDataRetentionPeriodSeconds() {
        return dataRetentionPeriodSeconds;
    }
    
    public void setDataRetentionPeriodSeconds(int dataRetentionPeriodSeconds) {
        this.dataRetentionPeriodSeconds = dataRetentionPeriodSeconds;
    }
    
    public int getCleanupIntervalSeconds() {
        return cleanupIntervalSeconds;
    }
    
    public void setCleanupIntervalSeconds(int cleanupIntervalSeconds) {
        this.cleanupIntervalSeconds = cleanupIntervalSeconds;
    }
} 