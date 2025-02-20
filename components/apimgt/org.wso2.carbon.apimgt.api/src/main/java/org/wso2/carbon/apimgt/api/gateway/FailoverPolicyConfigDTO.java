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

/**
 * DTO representing the configuration for failover policies in API Gateway.
 */
public class FailoverPolicyConfigDTO {

    private FailoverPolicyDeploymentConfigDTO production;
    private FailoverPolicyDeploymentConfigDTO sandbox;
    private Long requestTimeout;
    private Long suspendDuration;

    /**
     * Gets the failover policy configuration for production.
     *
     * @return the production failover policy configuration
     */
    public FailoverPolicyDeploymentConfigDTO getProduction() {

        return production;
    }

    /**
     * Sets the failover policy configuration for production.
     *
     * @param production the production failover policy configuration to set
     */
    public void setProduction(FailoverPolicyDeploymentConfigDTO production) {

        this.production = production;
    }

    /**
     * Gets the failover policy configuration for sandbox.
     *
     * @return the sandbox failover policy configuration
     */
    public FailoverPolicyDeploymentConfigDTO getSandbox() {

        return sandbox;
    }

    /**
     * Sets the failover policy configuration for sandbox.
     *
     * @param sandbox the sandbox failover policy configuration to set
     */
    public void setSandbox(FailoverPolicyDeploymentConfigDTO sandbox) {

        this.sandbox = sandbox;
    }

    /**
     * Gets the request timeout duration.
     *
     * @return the request timeout in milliseconds
     */
    public Long getRequestTimeout() {

        return requestTimeout;
    }

    /**
     * Sets the request timeout duration.
     *
     * @param requestTimeout the request timeout in milliseconds to set
     */
    public void setRequestTimeout(Long requestTimeout) {

        this.requestTimeout = requestTimeout;
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

