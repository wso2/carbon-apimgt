/*
 *  Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.AIConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;

import java.util.Collections;
import java.util.List;

/**
 * A unified wrapper to abstract differences between {@link APIDTO} and {@link MCPServerDTO}
 * and provide a common interface for accessing shared properties.
 */
public class APIDTOWrapper {

    private static final Log log = LogFactory.getLog(APIDTOWrapper.class);

    private final APIDTO apiDto;
    private final MCPServerDTO mcpServerDto;

    public APIDTOWrapper(APIDTO apiDto) {

        if (apiDto == null) {
            throw new IllegalArgumentException("APIDTO cannot be null");
        }
        this.apiDto = apiDto;
        this.mcpServerDto = null;
    }

    public APIDTOWrapper(MCPServerDTO mcpServerDto) {

        if (mcpServerDto == null) {
            throw new IllegalArgumentException("MCPServerDTO cannot be null");
        }
        this.apiDto = null;
        this.mcpServerDto = mcpServerDto;
    }

    public boolean isAPIDTO() {

        return apiDto != null;
    }

    public boolean isMCPServerDTO() {

        return mcpServerDto != null;
    }

    public Object getWrappedDTO() {

        return isAPIDTO() ? apiDto : mcpServerDto;
    }

    public String getId() {

        return isAPIDTO() ? apiDto.getId() : mcpServerDto.getId();
    }

    public void setId(String id) {

        if (isAPIDTO()) {
            apiDto.setId(id);
        } else {
            mcpServerDto.setId(id);
        }
    }

    public String getName() {

        return isAPIDTO() ? apiDto.getName() : mcpServerDto.getName();
    }

    public void setName(String name) {

        if (isAPIDTO()) {
            apiDto.setName(name);
        } else {
            mcpServerDto.setName(name);
        }
    }

    public String getDescription() {

        return isAPIDTO() ? apiDto.getDescription() : mcpServerDto.getDescription();
    }

    public void setDescription(String description) {

        if (isAPIDTO()) {
            apiDto.setDescription(description);
        } else {
            mcpServerDto.setDescription(description);
        }
    }

    public String getContext() {

        return isAPIDTO() ? apiDto.getContext() : mcpServerDto.getContext();
    }

    public void setContext(String context) {

        if (isAPIDTO()) {
            apiDto.setContext(context);
        } else {
            mcpServerDto.setContext(context);
        }
    }

    public String getVersion() {

        return isAPIDTO() ? apiDto.getVersion() : mcpServerDto.getVersion();
    }

    public void setVersion(String version) {

        if (isAPIDTO()) {
            apiDto.setVersion(version);
        } else {
            mcpServerDto.setVersion(version);
        }
    }

    public String getProvider() {

        return isAPIDTO() ? apiDto.getProvider() : mcpServerDto.getProvider();
    }

    public void setProvider(String provider) {

        if (isAPIDTO()) {
            apiDto.setProvider(provider);
        } else {
            mcpServerDto.setProvider(provider);
        }
    }

    public APIDTO.TypeEnum getType() {

        return isAPIDTO() ? apiDto.getType() : null;
    }

    public void setType(APIDTO.TypeEnum type) {

        if (isAPIDTO()) {
            apiDto.setType(type);
        }
    }

    public List<APIOperationsDTO> getOperations() {

        return isAPIDTO() ? apiDto.getOperations() : mcpServerDto.getOperations();
    }

    public String getAuthorizationHeader() {

        return isAPIDTO() ? apiDto.getAuthorizationHeader() : mcpServerDto.getAuthorizationHeader();
    }

    public void setAuthorizationHeader(String header) {

        if (isAPIDTO()) {
            apiDto.setAuthorizationHeader(header);
        } else {
            mcpServerDto.setAuthorizationHeader(header);
        }
    }

    public String getApiKeyHeader() {

        return isAPIDTO() ? apiDto.getApiKeyHeader() : mcpServerDto.getApiKeyHeader();
    }

    public void setApiKeyHeader(String header) {

        if (isAPIDTO()) {
            apiDto.setApiKeyHeader(header);
        } else {
            mcpServerDto.setApiKeyHeader(header);
        }
    }

    public String getApiThrottlingPolicy() {

        return isAPIDTO() ? apiDto.getApiThrottlingPolicy() : mcpServerDto.getApiThrottlingPolicy();
    }

    public List<String> getKeyManagers() {

        Object managers = isAPIDTO() ? apiDto.getKeyManagers() : mcpServerDto.getKeyManagers();
        if (managers instanceof List<?>) {
            return (List<String>) managers;
        } else {
            return Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS);
        }
    }

    public String getGatewayVendor() {

        return isAPIDTO() ? apiDto.getGatewayVendor() : mcpServerDto.getGatewayVendor();
    }

    public String getGatewayType() {

        return isAPIDTO() ? apiDto.getGatewayType() : mcpServerDto.getGatewayType();
    }

    public boolean isEgress() {

        return isAPIDTO() ? apiDto.isEgress() : mcpServerDto.isEgress();
    }

    public boolean isVisibilityRestricted() {

        return isAPIDTO()
                ? APIDTO.VisibilityEnum.RESTRICTED.equals(apiDto.getVisibility())
                : MCPServerDTO.VisibilityEnum.RESTRICTED.equals(mcpServerDto.getVisibility());
    }

    public String getResolvedApiSubtype() {

        String subtype = isAPIDTO()
                ? (apiDto.getSubtypeConfiguration() != null ? apiDto.getSubtypeConfiguration().getSubtype() : null)
                :
                (mcpServerDto.getSubtypeConfiguration() != null ? mcpServerDto.getSubtypeConfiguration().getSubtype() :
                        null);
        return subtype != null ? subtype : APIConstants.API_SUBTYPE_DEFAULT;
    }

    public AIConfiguration getAiConfiguration() {

        if (!isAPIDTO() || apiDto.getSubtypeConfiguration() == null) {
            return null;
        }
        String subtype = apiDto.getSubtypeConfiguration().getSubtype();
        if (APIConstants.API_SUBTYPE_AI_API.equals(subtype)) {
            try {
                return new Gson().fromJson(apiDto.getSubtypeConfiguration().getConfiguration().toString(),
                        AIConfiguration.class);
            } catch (JsonSyntaxException e) {
                log.error("Failed to parse AI configuration", e);
                return null;
            }
        }
        return null;
    }

    public List<String> getVisibleRoles() {

        return isAPIDTO() ? apiDto.getVisibleRoles() : mcpServerDto.getVisibleRoles();
    }

    public List<String> getAccessControlRoles() {

        return isAPIDTO() ? apiDto.getAccessControlRoles() : mcpServerDto.getAccessControlRoles();
    }

    public List<APIInfoAdditionalPropertiesDTO> getAdditionalProperties() {

        return isAPIDTO() ? apiDto.getAdditionalProperties() : mcpServerDto.getAdditionalProperties();
    }

    public void setLifeCycleStatus(String status) {

        if (isAPIDTO()) {
            apiDto.setLifeCycleStatus(status);
        } else {
            mcpServerDto.setLifeCycleStatus(status);
        }
    }

    public Object getEndpointConfig() {

        return isAPIDTO() ? apiDto.getEndpointConfig() : mcpServerDto.getBackendAPIEndpointConfig();
    }

    public void setEndpointConfig(Object endpointConfig) {

        if (isAPIDTO()) {
            apiDto.setEndpointConfig(endpointConfig);
        } else {
            mcpServerDto.setBackendAPIEndpointConfig(endpointConfig);
        }
    }

    public List<MediationPolicyDTO> getMediationPolicies() {

        return isAPIDTO() ? apiDto.getMediationPolicies() : mcpServerDto.getMediationPolicies();
    }

    public void setMediationPolicies(List<MediationPolicyDTO> policies) {

        if (isAPIDTO()) {
            apiDto.setMediationPolicies(policies);
        } else {
            mcpServerDto.setMediationPolicies(policies);
        }
    }

    public List<String> getPolicies() {

        return isAPIDTO() ? apiDto.getPolicies() : mcpServerDto.getPolicies();
    }

    public void setPolicies(List<String> policies) {

        if (isAPIDTO()) {
            apiDto.setPolicies(policies);
        } else {
            mcpServerDto.setPolicies(policies);
        }
    }

    public List<OrganizationPoliciesDTO> getOrganizationPolicies() {

        return isAPIDTO() ? apiDto.getOrganizationPolicies() : mcpServerDto.getOrganizationPolicies();
    }

    public void setOrganizationPolicies(List<OrganizationPoliciesDTO> policies) {

        if (isAPIDTO()) {
            apiDto.setOrganizationPolicies(policies);
        } else {
            mcpServerDto.setOrganizationPolicies(policies);
        }
    }
}
