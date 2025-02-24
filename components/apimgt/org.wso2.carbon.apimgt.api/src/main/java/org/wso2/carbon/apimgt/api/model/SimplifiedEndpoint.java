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

package org.wso2.carbon.apimgt.api.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.EndpointConfigDTO;
import org.wso2.carbon.apimgt.api.dto.EndpointDTO;

public class SimplifiedEndpoint {

    private String endpointUuid;
    private boolean endpointSecurityEnabled;
    private String endpointName;
    private String apiKeyIdentifier;
    private String apiKeyValue;
    private String apiKeyIdentifierType;
    private String deploymentStage;
    private static final String PRODUCTION = "PRODUCTION";
    private static final String SANDBOX = "SANDBOX";
    private static final Log log = LogFactory.getLog(SimplifiedEndpoint.class);

    /**
     * Constructs a SimplifiedEndpointDTO from an EndpointDTO.
     *
     * @param endpointDTO The endpoint to simplify
     */
    public SimplifiedEndpoint(EndpointDTO endpointDTO) {

        if (endpointDTO == null) {
            throw new IllegalArgumentException("EndpointDTO cannot be null");
        }
        if (!PRODUCTION.equals(endpointDTO.getDeploymentStage()) &&
                !SANDBOX.equals(endpointDTO.getDeploymentStage())) {
            throw new IllegalArgumentException(
                    "Invalid deployment stage: " + endpointDTO.getDeploymentStage());
        }

        this.endpointUuid = endpointDTO.getId();
        this.endpointName = endpointDTO.getName();
        this.deploymentStage = endpointDTO.getDeploymentStage();

        EndpointConfigDTO config = endpointDTO.getEndpointConfig();
        EndpointConfigDTO.EndpointSecurityConfig securityConfig =
                config != null ? config.getEndpointSecurity() : null;

        if (securityConfig == null) {
            this.endpointSecurityEnabled = false;
            return;
        }

        EndpointSecurity endpointSecurity = PRODUCTION.equals(deploymentStage)
                ? securityConfig.getProduction()
                : securityConfig.getSandbox();

        this.endpointSecurityEnabled = endpointSecurity != null &&
                endpointSecurity.isEnabled();

        if (this.endpointSecurityEnabled) {
            this.apiKeyIdentifier = endpointSecurity.getApiKeyIdentifier();
            this.apiKeyValue = endpointSecurity.getApiKeyValue();
            this.apiKeyIdentifierType = endpointSecurity.getApiKeyIdentifierType();
        }
    }

    public String getDeploymentStage() {

        return deploymentStage;
    }

    public String getEndpointUuid() {

        return endpointUuid;
    }

    public String getEndpointName() {

        return endpointName;
    }

    public String getApiKeyIdentifier() {

        return apiKeyIdentifier;
    }

    public String getApiKeyValue() {

        return apiKeyValue;
    }

    public String getApiKeyIdentifierType() {

        return apiKeyIdentifierType;
    }

    public boolean getEndpointSecurityEnabled() {

        return endpointSecurityEnabled;
    }

    /**
     * Returns a string representation of the simplified endpoint.
     *
     * @return A formatted string containing endpoint details
     */
    @Override
    public String toString() {

        return "SimplifiedEndpointDTO{" +
                "  endpointUuid='" + endpointUuid + '\'' +
                ", endpointSecurityEnabled=" + endpointSecurityEnabled +
                ", endpointName='" + endpointName + '\'' +
                ", apiKeyIdentifier='" + apiKeyIdentifier + '\'' +
                ", apiKeyValue='" + apiKeyValue + '\'' +
                ", apiKeyIdentifierType='" + apiKeyIdentifierType + '\'' +
                ", deploymentStage='" + deploymentStage + '\'' +
                '}';
    }
}
