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
    private String authenticationType;
    private String accessKey;
    private String secretKey;
    private String region;
    private String service;
    private String endpoint;
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

        EndpointSecurityDTO endpointSecurity = PRODUCTION.equals(deploymentStage)
                ? securityConfig.getProduction()
                : securityConfig.getSandbox();
        EndpointConfigDTO.EndpointDetails endpointDetails =
                PRODUCTION.equals(deploymentStage) ? config.getProductionEndpoints() : config.getSandboxEndpoints();
        this.endpointSecurityEnabled = endpointSecurity != null &&
                endpointSecurity.isEnabled();

        if (this.endpointSecurityEnabled) {
            this.authenticationType = endpointSecurity.getType();
            this.apiKeyIdentifier = endpointSecurity.getApiKeyIdentifier();
            this.apiKeyValue = endpointSecurity.getApiKeyValue();
            this.apiKeyIdentifierType = endpointSecurity.getApiKeyIdentifierType();
            this.accessKey = endpointSecurity.getAccessKey();
            this.secretKey = endpointSecurity.getSecretKey();
            this.region = endpointSecurity.getRegion();
            this.service = endpointSecurity.getService();
            this.endpoint = endpointDetails.getUrl();
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

    public void setEndpointUuid(String endpointUuid) {
        this.endpointUuid = endpointUuid;
    }

    public boolean isEndpointSecurityEnabled() {
        return endpointSecurityEnabled;
    }

    public void setEndpointSecurityEnabled(boolean endpointSecurityEnabled) {
        this.endpointSecurityEnabled = endpointSecurityEnabled;
    }

    public void setEndpointName(String endpointName) {
        this.endpointName = endpointName;
    }

    public void setApiKeyIdentifier(String apiKeyIdentifier) {
        this.apiKeyIdentifier = apiKeyIdentifier;
    }

    public void setApiKeyValue(String apiKeyValue) {
        this.apiKeyValue = apiKeyValue;
    }

    public void setApiKeyIdentifierType(String apiKeyIdentifierType) {
        this.apiKeyIdentifierType = apiKeyIdentifierType;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setDeploymentStage(String deploymentStage) {
        this.deploymentStage = deploymentStage;
    }

    /**
     * Returns a string representation of the simplified endpoint.
     *
     * @return A formatted string containing endpoint details
     */
    @Override
    public String toString() {
        return "SimplifiedEndpoint{" +
                "endpointUuid='" + endpointUuid + '\'' +
                ", endpointSecurityEnabled=" + endpointSecurityEnabled +
                ", endpointName='" + endpointName + '\'' +
                ", endpoint='" + endpoint + '\'' +
                ", deploymentStage='" + deploymentStage + '\'' +
                '}';
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }
}
