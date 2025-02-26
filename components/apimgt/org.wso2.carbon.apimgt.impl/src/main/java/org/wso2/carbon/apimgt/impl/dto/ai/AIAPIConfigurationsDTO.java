/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dto.ai;

/**
 * Represents AI API configuration.
 */
public class AIAPIConfigurationsDTO {

    private boolean enabled;
    private FailoverConfigurations failoverConfigurations = new FailoverConfigurations();
    private Long defaultRequestTimeout;

    /**
     * Gets the enabled status.
     *
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabled() {

        return enabled;
    }

    /**
     * Sets the enabled status.
     *
     * @param enabled true to enable, false to disable.
     */
    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    /**
     * Gets failover configurations.
     *
     * @return failover configurations.
     */
    public FailoverConfigurations getFailoverConfigurations() {

        return failoverConfigurations;
    }

    /**
     * Sets failover configurations.
     *
     * @param failoverConfigurations failover configurations to set.
     */
    public void setFailoverConfigurations(FailoverConfigurations failoverConfigurations) {

        this.failoverConfigurations = failoverConfigurations;
    }

    /**
     * Gets default request timeout.
     *
     * @return default request timeout in milliseconds.
     */
    public Long getDefaultRequestTimeout() {

        return defaultRequestTimeout;
    }

    /**
     * Sets default request timeout.
     *
     * @param defaultRequestTimeout timeout in milliseconds.
     */
    public void setDefaultRequestTimeout(Long defaultRequestTimeout) {

        this.defaultRequestTimeout = defaultRequestTimeout;
    }

    /**
     * Represents failover configurations.
     */
    public static class FailoverConfigurations {

        private int failoverEndpointsLimit;
        private long defaultRequestTimeout;

        /**
         * Gets failover endpoints limit.
         *
         * @return number of failover endpoints.
         */
        public int getFailoverEndpointsLimit() {

            return failoverEndpointsLimit;
        }

        /**
         * Sets failover endpoints limit.
         *
         * @param failoverEndpointsLimit number of failover endpoints.
         */
        public void setFailoverEndpointsLimit(int failoverEndpointsLimit) {

            this.failoverEndpointsLimit = failoverEndpointsLimit;
        }

        /**
         * Gets default request timeout.
         *
         * @return default request timeout in milliseconds.
         */
        public long getDefaultRequestTimeout() {

            return defaultRequestTimeout;
        }

        /**
         * Sets default request timeout.
         *
         * @param defaultRequestTimeout timeout in milliseconds.
         */
        public void setDefaultRequestTimeout(long defaultRequestTimeout) {

            this.defaultRequestTimeout = defaultRequestTimeout;
        }
    }
}
