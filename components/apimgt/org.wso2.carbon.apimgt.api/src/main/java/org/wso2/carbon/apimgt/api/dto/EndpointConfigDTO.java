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
 * DTO object to represent endpoints.
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

    public String getEndpointType() {
        return endpointType;
    }

    public void setEndpointType(String endpointType) {
        this.endpointType = endpointType;
    }

    public EndpointDetails getSandboxEndpoints() {
        return sandboxEndpoints;
    }

    public void setSandboxEndpoints(EndpointDetails sandboxEndpoints) {
        this.sandboxEndpoints = sandboxEndpoints;
    }

    public EndpointDetails getProductionEndpoints() {
        return productionEndpoints;
    }

    public void setProductionEndpoints(EndpointDetails productionEndpoints) {
        this.productionEndpoints = productionEndpoints;
    }

    public EndpointSecurityConfig getEndpointSecurity() {
        return endpointSecurity;
    }

    public void setEndpointSecurity(EndpointSecurityConfig endpointSecurity) {
        this.endpointSecurity = endpointSecurity;
    }

    /**
     * Inner class to represent endpoint details with URL and config.
     */
    public static class EndpointDetails {

        @SerializedName("url")
        private String url;

        @SerializedName("config")
        private Config config;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Config getConfig() {
            return config;
        }

        public void setConfig(Config config) {
            this.config = config;
        }
    }

    /**
     * Inner class to represent endpoint security configurations.
     */
    public static class EndpointSecurityConfig {

        @SerializedName("production")
        private EndpointSecurity production;

        @SerializedName("sandbox")
        private EndpointSecurity sandbox;

        public EndpointSecurity getProduction() {
            return production;
        }

        public void setProduction(EndpointSecurity production) {
            this.production = production;
        }

        public EndpointSecurity getSandbox() {
            return sandbox;
        }

        public void setSandbox(EndpointSecurity sandbox) {
            this.sandbox = sandbox;
        }
    }

    /**
     * Inner class to represent config object inside endpoints.
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

        public String getActionDuration() {
            return actionDuration;
        }

        public void setActionDuration(String actionDuration) {
            this.actionDuration = actionDuration;
        }

        public String getActionSelect() {
            return actionSelect;
        }

        public void setActionSelect(String actionSelect) {
            this.actionSelect = actionSelect;
        }

        public String getFactor() {
            return factor;
        }

        public void setFactor(String factor) {
            this.factor = factor;
        }

        public String getRetryDelay() {
            return retryDelay;
        }

        public void setRetryDelay(String retryDelay) {
            this.retryDelay = retryDelay;
        }

        public List<String> getRetryErrorCode() {
            return retryErrorCode;
        }

        public void setRetryErrorCode(List<String> retryErrorCode) {
            this.retryErrorCode = retryErrorCode;
        }

        public String getRetryTimeout() {
            return retryTimeout;
        }

        public void setRetryTimeout(String retryTimeout) {
            this.retryTimeout = retryTimeout;
        }

        public String getSuspendDuration() {
            return suspendDuration;
        }

        public void setSuspendDuration(String suspendDuration) {
            this.suspendDuration = suspendDuration;
        }

        public List<String> getSuspendErrorCode() {
            return suspendErrorCode;
        }

        public void setSuspendErrorCode(List<String> suspendErrorCode) {
            this.suspendErrorCode = suspendErrorCode;
        }

        public String getSuspendMaxDuration() {
            return suspendMaxDuration;
        }

        public void setSuspendMaxDuration(String suspendMaxDuration) {
            this.suspendMaxDuration = suspendMaxDuration;
        }
    }
}

