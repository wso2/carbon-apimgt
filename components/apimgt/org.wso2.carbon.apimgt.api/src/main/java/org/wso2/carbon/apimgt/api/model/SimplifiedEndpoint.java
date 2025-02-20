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
            return;
        }

        this.endpointUuid = endpointDTO.getId();
        this.endpointName = endpointDTO.getName();
        this.deploymentStage = endpointDTO.getDeploymentStage();

        if (endpointDTO.getEndpointConfig() != null) {
            EndpointConfigDTO.EndpointSecurityConfig securityConfig =
                    endpointDTO.getEndpointConfig().getEndpointSecurity();
            if (securityConfig != null) {
                this.endpointSecurityEnabled = true;
                EndpointSecurity endpointSecurity = null;
                if (PRODUCTION.equals(deploymentStage)) {
                    endpointSecurity = securityConfig.getProduction();
                } else if (SANDBOX.equals(deploymentStage)) {
                    endpointSecurity = securityConfig.getSandbox();
                }
                if (endpointSecurity != null && endpointSecurity.isEnabled()) {
                    this.apiKeyIdentifier = endpointSecurity.getApiKeyIdentifier();
                    this.apiKeyValue = endpointSecurity.getApiKeyValue();
                    this.apiKeyIdentifierType = endpointSecurity.getApiKeyIdentifierType();
                }
            } else {
                this.endpointSecurityEnabled = false;
            }
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
