/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.api.gateway;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * DTO representing the deployment configuration for failover policies in API Gateway.
 */
public class FailoverPolicyDeploymentConfigDTO {

    @SerializedName("targetModel")
    private ModelEndpointDTO targetModelEndpoint;

    @SerializedName("fallbackModels")
    private List<ModelEndpointDTO> fallbackModelEndpoints;

    /**
     * Gets the target model endpoint.
     *
     * @return the target model endpoint
     */
    public ModelEndpointDTO getTargetModelEndpoint() {

        return targetModelEndpoint;
    }

    /**
     * Sets the target model endpoint.
     *
     * @param targetModelEndpoint the target model endpoint to set
     */
    public void setTargetModelEndpoint(ModelEndpointDTO targetModelEndpoint) {

        this.targetModelEndpoint = targetModelEndpoint;
    }

    /**
     * Gets the list of fallback model endpoints.
     *
     * @return the fallback model endpoints
     */
    public List<ModelEndpointDTO> getFallbackModelEndpoints() {

        return fallbackModelEndpoints;
    }

    /**
     * Sets the list of fallback model endpoints.
     *
     * @param fallbackModelEndpoints the fallback model endpoints to set
     */
    public void setFallbackModelEndpoints(List<ModelEndpointDTO> fallbackModelEndpoints) {

        this.fallbackModelEndpoints = fallbackModelEndpoints;
    }
}