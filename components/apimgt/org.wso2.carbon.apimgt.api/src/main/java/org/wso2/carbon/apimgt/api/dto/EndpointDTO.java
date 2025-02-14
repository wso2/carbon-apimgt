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

/**
 * Data Transfer Object (DTO) representing an endpoint.
 * Contains details such as the endpoint's UUID, name, deployment stage, and configuration.
 */
public class EndpointDTO {

    private String endpointUuid;
    private String endpointName;
    private String deploymentStage;
    private EndpointConfigDTO endpointConfig;

    /**
     * Gets the unique identifier (UUID) of the endpoint.
     *
     * @return The UUID of the endpoint.
     */
    public String getEndpointUuid() {

        return endpointUuid;
    }

    /**
     * Sets the unique identifier (UUID) of the endpoint.
     *
     * @param endpointUuid The UUID to set.
     */
    public void setEndpointUuid(String endpointUuid) {

        this.endpointUuid = endpointUuid;
    }

    /**
     * Gets the name of the endpoint.
     *
     * @return The name of the endpoint.
     */
    public String getEndpointName() {

        return endpointName;
    }

    /**
     * Sets the name of the endpoint.
     *
     * @param endpointName The name to set for the endpoint.
     */
    public void setEndpointName(String endpointName) {

        this.endpointName = endpointName;
    }

    /**
     * Gets the deployment stage associated with the endpoint.
     *
     * @return The deployment stage of the endpoint.
     */
    public String getDeploymentStage() {

        return deploymentStage;
    }

    /**
     * Sets the deployment stage for the endpoint.
     *
     * @param deploymentStage The deployment stage to set.
     */
    public void setDeploymentStage(String deploymentStage) {

        this.deploymentStage = deploymentStage;
    }

    /**
     * Gets the configuration details of the endpoint.
     *
     * @return The configuration of the endpoint.
     */
    public EndpointConfigDTO getEndpointConfig() {

        return endpointConfig;
    }

    /**
     * Sets the configuration for the endpoint.
     *
     * @param endpointConfig The configuration to set for the endpoint.
     */
    public void setEndpointConfig(EndpointConfigDTO endpointConfig) {

        this.endpointConfig = endpointConfig;
    }
}
