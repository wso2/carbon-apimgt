/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.dto;

import com.google.gson.annotations.SerializedName;
import org.wso2.carbon.apimgt.api.model.EndpointSecurity;

import java.util.List;

/**
 * DTO object to represent the configuration of API endpoints in a system.
 */
public class EndpointConfigDTO {

    @SerializedName("endpoint_type")
    private String endpointType;

    @SerializedName("sandbox_endpoints")
    private EndpointDetails sandboxEndpoints;

    @SerializedName("production_endpoints")
    private EndpointDetails productionEndpoints;

    @SerializedName("endpoint_security")
    private EndpointSecurityConfig endpointSecurity;

    /**
     * Gets the type of the endpoint (e.g., SOAP, REST).
     *
     * @return the type of the endpoint
     */
    public String getEndpointType() {

        return endpointType;
    }

    /**
     * Sets the type of the endpoint.
     *
     * @param endpointType the type of the endpoint
     */
    public void setEndpointType(String endpointType) {

        this.endpointType = endpointType;
    }

    /**
     * Gets the configuration for sandbox endpoints.
     *
     * @return the sandbox endpoint configuration
     */
    public EndpointDetails getSandboxEndpoints() {

        return sandboxEndpoints;
    }

    /**
     * Sets the configuration for sandbox endpoints.
     *
     * @param sandboxEndpoints the sandbox endpoint configuration
     */
    public void setSandboxEndpoints(EndpointDetails sandboxEndpoints) {

        this.sandboxEndpoints = sandboxEndpoints;
    }

    /**
     * Gets the configuration for production endpoints.
     *
     * @return the production endpoint configuration
     */
    public EndpointDetails getProductionEndpoints() {

        return productionEndpoints;
    }

    /**
     * Sets the configuration for production endpoints.
     *
     * @param productionEndpoints the production endpoint configuration
     */
    public void setProductionEndpoints(EndpointDetails productionEndpoints) {

        this.productionEndpoints = productionEndpoints;
    }

    /**
     * Gets the security configuration for the endpoints.
     *
     * @return the endpoint security configuration
     */
    public EndpointSecurityConfig getEndpointSecurity() {

        return endpointSecurity;
    }

    /**
     * Sets the security configuration for the endpoints.
     *
     * @param endpointSecurity the endpoint security configuration
     */
    public void setEndpointSecurity(EndpointSecurityConfig endpointSecurity) {

        this.endpointSecurity = endpointSecurity;
    }

    /**
     * Inner class to represent the endpoint details, including URL and configuration.
     */
    public static class EndpointDetails {

        @SerializedName("url")
        private String url;

        @SerializedName("config")
        private Config config;

        /**
         * Gets the URL of the endpoint.
         *
         * @return the endpoint URL
         */
        public String getUrl() {

            return url;
        }

        /**
         * Sets the URL of the endpoint.
         *
         * @param url the endpoint URL
         */
        public void setUrl(String url) {

            this.url = url;
        }

        /**
         * Gets the configuration for the endpoint.
         *
         * @return the endpoint configuration
         */
        public Config getConfig() {

            return config;
        }

        /**
         * Sets the configuration for the endpoint.
         *
         * @param config the endpoint configuration
         */
        public void setConfig(Config config) {

            this.config = config;
        }
    }

    /**
     * Inner class to represent the security configuration for endpoints.
     */
    public static class EndpointSecurityConfig {

        @SerializedName("production")
        private EndpointSecurity production;

        @SerializedName("sandbox")
        private EndpointSecurity sandbox;

        /**
         * Gets the security configuration for production endpoints.
         *
         * @return the production endpoint security configuration
         */
        public EndpointSecurity getProduction() {

            return production;
        }

        /**
         * Sets the security configuration for production endpoints.
         *
         * @param production the production endpoint security configuration
         */
        public void setProduction(EndpointSecurity production) {

            this.production = production;
        }

        /**
         * Gets the security configuration for sandbox endpoints.
         *
         * @return the sandbox endpoint security configuration
         */
        public EndpointSecurity getSandbox() {

            return sandbox;
        }

        /**
         * Sets the security configuration for sandbox endpoints.
         *
         * @param sandbox the sandbox endpoint security configuration
         */
        public void setSandbox(EndpointSecurity sandbox) {

            this.sandbox = sandbox;
        }
    }

    /**
     * Inner class to represent the configuration object inside an endpoint.
     */
    public static class Config {

        @SerializedName("actionDuration")
        private String actionDuration;

        @SerializedName("actionSelect")
        private String actionSelect;

        @SerializedName("factor")
        private String factor;

        @SerializedName("retryDelay")
        private String retryDelay;

        @SerializedName("retryErroCode")
        private List<String> retryErrorCode;

        @SerializedName("retryTimeOut")
        private String retryTimeout;

        @SerializedName("suspendDuration")
        private String suspendDuration;

        @SerializedName("suspendErrorCode")
        private List<String> suspendErrorCode;

        @SerializedName("suspendMaxDuration")
        private String suspendMaxDuration;

        /**
         * Gets the action duration configuration.
         *
         * @return the action duration
         */
        public String getActionDuration() {

            return actionDuration;
        }

        /**
         * Sets the action duration configuration.
         *
         * @param actionDuration the action duration
         */
        public void setActionDuration(String actionDuration) {

            this.actionDuration = actionDuration;
        }

        /**
         * Gets the action selection configuration.
         *
         * @return the action selection
         */
        public String getActionSelect() {

            return actionSelect;
        }

        /**
         * Sets the action selection configuration.
         *
         * @param actionSelect the action selection
         */
        public void setActionSelect(String actionSelect) {

            this.actionSelect = actionSelect;
        }

        /**
         * Gets the factor configuration.
         *
         * @return the factor
         */
        public String getFactor() {

            return factor;
        }

        /**
         * Sets the factor configuration.
         *
         * @param factor the factor
         */
        public void setFactor(String factor) {

            this.factor = factor;
        }

        /**
         * Gets the retry delay configuration.
         *
         * @return the retry delay
         */
        public String getRetryDelay() {

            return retryDelay;
        }

        /**
         * Sets the retry delay configuration.
         *
         * @param retryDelay the retry delay
         */
        public void setRetryDelay(String retryDelay) {

            this.retryDelay = retryDelay;
        }

        /**
         * Gets the retry error codes for the endpoint.
         *
         * @return the retry error codes
         */
        public List<String> getRetryErrorCode() {

            return retryErrorCode;
        }

        /**
         * Sets the retry error codes for the endpoint.
         *
         * @param retryErrorCode the retry error codes
         */
        public void setRetryErrorCode(List<String> retryErrorCode) {

            this.retryErrorCode = retryErrorCode;
        }

        /**
         * Gets the retry timeout configuration.
         *
         * @return the retry timeout
         */
        public String getRetryTimeout() {

            return retryTimeout;
        }

        /**
         * Sets the retry timeout configuration.
         *
         * @param retryTimeout the retry timeout
         */
        public void setRetryTimeout(String retryTimeout) {

            this.retryTimeout = retryTimeout;
        }

        /**
         * Gets the suspend duration configuration.
         *
         * @return the suspend duration
         */
        public String getSuspendDuration() {

            return suspendDuration;
        }

        /**
         * Sets the suspend duration configuration.
         *
         * @param suspendDuration the suspend duration
         */
        public void setSuspendDuration(String suspendDuration) {

            this.suspendDuration = suspendDuration;
        }

        /**
         * Gets the suspend error codes for the endpoint.
         *
         * @return the suspend error codes
         */
        public List<String> getSuspendErrorCode() {

            return suspendErrorCode;
        }

        /**
         * Sets the suspend error codes for the endpoint.
         *
         * @param suspendErrorCode the suspend error codes
         */
        public void setSuspendErrorCode(List<String> suspendErrorCode) {

            this.suspendErrorCode = suspendErrorCode;
        }

        /**
         * Gets the maximum suspend duration configuration.
         *
         * @return the maximum suspend duration
         */
        public String getSuspendMaxDuration() {

            return suspendMaxDuration;
        }

        /**
         * Sets the maximum suspend duration configuration.
         *
         * @param suspendMaxDuration the maximum suspend duration
         */
        public void setSuspendMaxDuration(String suspendMaxDuration) {

            this.suspendMaxDuration = suspendMaxDuration;
        }
    }
}
