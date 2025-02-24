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

package org.wso2.carbon.apimgt.api.gateway;

import java.util.List;

/**
 * DTO representing the configuration for RB (Routing and Balancing) policies in API Gateway.
 */
public class RBPolicyConfigDTO {

    private List<ModelEndpointDTO> production;
    private List<ModelEndpointDTO> sandbox;
    private Long suspendDuration;

    /**
     * Gets the production model endpoints.
     *
     * @return the list of production model endpoints
     */
    public List<ModelEndpointDTO> getProduction() {

        return production;
    }

    /**
     * Sets the production model endpoints.
     *
     * @param production the list of production model endpoints to set
     */
    public void setProduction(List<ModelEndpointDTO> production) {

        this.production = production;
    }

    /**
     * Gets the sandbox model endpoints.
     *
     * @return the list of sandbox model endpoints
     */
    public List<ModelEndpointDTO> getSandbox() {

        return sandbox;
    }

    /**
     * Sets the sandbox model endpoints.
     *
     * @param sandbox the list of sandbox model endpoints to set
     */
    public void setSandbox(List<ModelEndpointDTO> sandbox) {

        this.sandbox = sandbox;
    }

    /**
     * Gets the suspend duration for a failed endpoint.
     *
     * @return the suspend duration in milliseconds
     */
    public Long getSuspendDuration() {

        return suspendDuration;
    }

    /**
     * Sets the suspend duration for a failed endpoint.
     *
     * @param suspendDuration the suspend duration in milliseconds to set
     */
    public void setSuspendDuration(Long suspendDuration) {

        this.suspendDuration = suspendDuration;
    }
}

