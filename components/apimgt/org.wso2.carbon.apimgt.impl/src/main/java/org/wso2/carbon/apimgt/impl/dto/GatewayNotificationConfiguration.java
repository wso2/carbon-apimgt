/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
 * Configuration DTO for Gateway Notification settings including Heartbeat, Deployment Acknowledgement and Gateway Cleanup
 */
public class GatewayNotificationConfiguration {
    private boolean enabled = true;
    private HeartbeatConfiguration heartbeat = new HeartbeatConfiguration();
    private DeploymentAcknowledgementConfiguration deploymentAcknowledgement = new DeploymentAcknowledgementConfiguration();
    private RegistrationConfiguration registration = new RegistrationConfiguration();
    private GatewayCleanupConfiguration gatewayCleanupConfiguration = new GatewayCleanupConfiguration();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public HeartbeatConfiguration getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(HeartbeatConfiguration heartbeat) {
        this.heartbeat = heartbeat;
    }

    public DeploymentAcknowledgementConfiguration getDeploymentAcknowledgement() {
        return deploymentAcknowledgement;
    }

    public void setDeploymentAcknowledgement(DeploymentAcknowledgementConfiguration deploymentAcknowledgement) {
        this.deploymentAcknowledgement = deploymentAcknowledgement;
    }

    public RegistrationConfiguration getRegistration() {
        return registration;
    }

    public void setRegistration(RegistrationConfiguration registration) {
        this.registration = registration;
    }

    public GatewayCleanupConfiguration getGatewayCleanupConfiguration() {
        return gatewayCleanupConfiguration;
    }

    public void setGatewayCleanupConfiguration(GatewayCleanupConfiguration gatewayCleanupConfiguration) {
        this.gatewayCleanupConfiguration = gatewayCleanupConfiguration;
    }

    /**
     * Configuration for Gateway Heartbeat functionality
     */
    public static class HeartbeatConfiguration {
        private int notifyIntervalSeconds = 30; // Default 30 seconds
        private String gatewayID = "";

        public int getNotifyIntervalSeconds() {
            return notifyIntervalSeconds;
        }

        public void setNotifyIntervalSeconds(int notifyIntervalSeconds) {
            this.notifyIntervalSeconds = notifyIntervalSeconds;
        }

        public String getGatewayID() {
            return gatewayID;
        }

        public void setGatewayID(String gatewayID) {
            this.gatewayID = gatewayID;
        }
    }

    /**
     * Configuration for Deployment Acknowledgement functionality
     */
    public static class DeploymentAcknowledgementConfiguration {
        private int batchSize = 200; // Default batch size
        private long batchIntervalMillis = 1000; // Default 1 second
        private int maxRetryCount = 5; // Default max retry count
        private long retryDuration = 10000; // Default 10 seconds
        private double retryProgressionFactor = 2.0; // Default progression factor

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getBatchIntervalMillis() {
            return batchIntervalMillis;
        }

        public void setBatchIntervalMillis(long batchIntervalMillis) {
            this.batchIntervalMillis = batchIntervalMillis;
        }

        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }

        public long getRetryDuration() {
            return retryDuration;
        }

        public void setRetryDuration(long retryDuration) {
            this.retryDuration = retryDuration;
        }

        public double getRetryProgressionFactor() {
            return retryProgressionFactor;
        }

        public void setRetryProgressionFactor(double retryProgressionFactor) {
            this.retryProgressionFactor = retryProgressionFactor;
        }
    }

    /**
     * Configuration for Gateway Registration functionality
     */
    public static class RegistrationConfiguration {
        private int maxRetryCount = 5; // Default max retry count
        private long retryDuration = 10000; // Default 10 seconds
        private double retryProgressionFactor = 2.0; // Default progression factor

        public int getMaxRetryCount() {
            return maxRetryCount;
        }

        public void setMaxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
        }

        public long getRetryDuration() {
            return retryDuration;
        }

        public void setRetryDuration(long retryDuration) {
            this.retryDuration = retryDuration;
        }

        public double getRetryProgressionFactor() {
            return retryProgressionFactor;
        }

        public void setRetryProgressionFactor(double retryProgressionFactor) {
            this.retryProgressionFactor = retryProgressionFactor;
        }
    }

    public static class GatewayCleanupConfiguration {

        private int expireTimeSeconds = 30; // Default 30 Seconds
        private int dataRetentionPeriodSeconds = 120; // Default 24 hours (1440 Seconds)
        private int cleanupIntervalSeconds = 60; // Default 60 Seconds

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

}
