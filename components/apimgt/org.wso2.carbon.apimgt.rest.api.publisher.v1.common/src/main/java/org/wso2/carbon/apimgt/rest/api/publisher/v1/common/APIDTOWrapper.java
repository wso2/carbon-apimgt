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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common;

import com.google.gson.Gson;
import org.wso2.carbon.apimgt.api.model.AIConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationPolicyDTO;

import java.util.Collections;
import java.util.List;

public class APIDTOWrapper {

    private APIDTO apiDto;
    private MCPServerDTO mcpServerDto;

    /**
     * Create a wrapper for an {@link APIDTO} instance.
     *
     * @param apiDto underlying APIDTO
     */
    public APIDTOWrapper(APIDTO apiDto) {

        this.apiDto = apiDto;
    }

    /**
     * Create a wrapper for an {@link MCPServerDTO} instance.
     *
     * @param mcpServerDto underlying MCPServerDTO
     */
    public APIDTOWrapper(MCPServerDTO mcpServerDto) {

        this.mcpServerDto = mcpServerDto;
    }

    /**
     * Returns the identifier of the wrapped DTO.
     *
     * @return id value
     */
    public String getId() {

        return apiDto != null ? apiDto.getId() : mcpServerDto.getId();
    }

    /**
     * Sets the identifier on the wrapped DTO.
     *
     * @param id identifier value
     */
    public void setId(String id) {

        if (apiDto != null) {
            apiDto.setId(id);
        } else {
            mcpServerDto.setId(id);
        }
    }

    /**
     * Returns the API/MCP name.
     *
     * @return name value
     */
    public String getName() {

        return apiDto != null ? apiDto.getName() : mcpServerDto.getName();
    }

    /**
     * Sets the API/MCP name.
     *
     * @param name new name
     */
    public void setName(String name) {

        if (apiDto != null) {
            apiDto.setName(name);
        } else {
            mcpServerDto.setName(name);
        }
    }

    /**
     * Returns the description of the wrapped DTO.
     *
     * @return description text
     */
    public String getDescription() {

        return apiDto != null ? apiDto.getDescription() : mcpServerDto.getDescription();
    }

    /**
     * Sets the description of the wrapped DTO.
     *
     * @param description description text
     */
    public void setDescription(String description) {

        if (apiDto != null) {
            apiDto.setDescription(description);
        } else {
            mcpServerDto.setDescription(description);
        }
    }

    /**
     * Returns the context of the API or MCP server.
     *
     * @return context path
     */
    public String getContext() {

        return apiDto != null ? apiDto.getContext() : mcpServerDto.getContext();
    }

    /**
     * Sets the context of the API or MCP server.
     *
     * @param context context path
     */
    public void setContext(String context) {

        if (apiDto != null) {
            apiDto.setContext(context);
        } else {
            mcpServerDto.setContext(context);
        }
    }

    /**
     * Returns the version value.
     *
     * @return version
     */
    public String getVersion() {

        return apiDto != null ? apiDto.getVersion() : mcpServerDto.getVersion();
    }

    /**
     * Sets the version value.
     *
     * @param version version string
     */
    public void setVersion(String version) {

        if (apiDto != null) {
            apiDto.setVersion(version);
        } else {
            mcpServerDto.setVersion(version);
        }
    }

    /**
     * Returns the provider value.
     *
     * @return provider
     */
    public String getProvider() {

        return apiDto != null ? apiDto.getProvider() : mcpServerDto.getProvider();
    }

    /**
     * Sets the provider value.
     *
     * @param provider provider name
     */
    public void setProvider(String provider) {

        if (apiDto != null) {
            apiDto.setProvider(provider);
        } else {
            mcpServerDto.setProvider(provider);
        }
    }

    public void setType(APIDTO.TypeEnum type) {

        if (apiDto != null) {
            apiDto.setType(type);
        }
    }

    public APIDTO.TypeEnum getType() {

        return apiDto != null ? apiDto.getType() : null;
    }

    public List<APIOperationsDTO> getOperations() {

        if (apiDto != null) {
            return apiDto.getOperations();
        } else if (mcpServerDto != null) {
            return mcpServerDto.getOperations();
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Returns true if the wrapper contains an APIDTO.
     *
     * @return true if APIDTO is wrapped
     */
    public boolean isAPIDTO() {

        return apiDto != null;
    }

    /**
     * Returns true if the wrapper contains a MCPServerDTO.
     *
     * @return true if MCPServerDTO is wrapped
     */
    public boolean isMCPServerDTO() {

        return mcpServerDto != null;
    }

    /**
     * Returns the wrapped DTO as Object.
     *
     * @return APIDTO or MCPServerDTO instance
     */
    public Object getWrappedDTO() {

        return apiDto != null ? apiDto : mcpServerDto;
    }

    public String getAuthorizationHeader() {

        return isAPIDTO() ? apiDto.getAuthorizationHeader() : mcpServerDto.getAuthorizationHeader();
    }

    public void setAuthorizationHeader(String header) {

        if (isAPIDTO()) apiDto.setAuthorizationHeader(header);
        else mcpServerDto.setAuthorizationHeader(header);
    }

    public String getApiKeyHeader() {

        return isAPIDTO() ? apiDto.getApiKeyHeader() : mcpServerDto.getApiKeyHeader();
    }

    public void setApiKeyHeader(String header) {

        if (isAPIDTO()) apiDto.setApiKeyHeader(header);
        else mcpServerDto.setApiKeyHeader(header);
    }

    public String getApiThrottlingPolicy() {

        return isAPIDTO() ? apiDto.getApiThrottlingPolicy() : mcpServerDto.getApiThrottlingPolicy();
    }

    public List<String> getKeyManagers() {

        Object keyManagers = isAPIDTO() ? apiDto.getKeyManagers() : mcpServerDto.getKeyManagers();
        if (keyManagers instanceof List) {
            return (List<String>) keyManagers;
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
                ? apiDto.getVisibility() == APIDTO.VisibilityEnum.RESTRICTED
                : mcpServerDto.getVisibility() == MCPServerDTO.VisibilityEnum.RESTRICTED;
    }

    public String getResolvedApiSubtype() {

        String subtype = isAPIDTO()
                ? apiDto.getSubtypeConfiguration() != null ? apiDto.getSubtypeConfiguration().getSubtype() : null
                : mcpServerDto.getSubtypeConfiguration() != null ? mcpServerDto.getSubtypeConfiguration().getSubtype() :
                null;
        return subtype != null ? subtype : APIConstants.API_SUBTYPE_DEFAULT;
    }

    public AIConfiguration getAiConfiguration() {

        if (!isAPIDTO()) return null;
        if (apiDto.getSubtypeConfiguration() != null &&
                APIConstants.API_SUBTYPE_AI_API.equals(apiDto.getSubtypeConfiguration().getSubtype())) {
            return new Gson().fromJson(apiDto.getSubtypeConfiguration().getConfiguration().toString(),
                    AIConfiguration.class);
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
        if (apiDto != null) {
            apiDto.setLifeCycleStatus(status);
        } else if (mcpServerDto != null) {
            mcpServerDto.setLifeCycleStatus(status);
        }
    }

    public Object getEndpointConfig() {
        if (apiDto != null) {
            return apiDto.getEndpointConfig();
        } else if (mcpServerDto != null) {
            return mcpServerDto.getBackendAPIEndpointConfig();
        }
        return null;
    }

    public void setEndpointConfig(Object endpointConfig) {
        if (apiDto != null) {
            apiDto.setEndpointConfig(endpointConfig);
        } else if (mcpServerDto != null) {
            mcpServerDto.setBackendAPIEndpointConfig(endpointConfig);
        }
    }

    public List<MediationPolicyDTO> getMediationPolicies() {
        if (apiDto != null) {
            return apiDto.getMediationPolicies();
        } else if (mcpServerDto != null) {
            return mcpServerDto.getMediationPolicies();
        }
        return null;
    }

    public void setMediationPolicies(List<MediationPolicyDTO> mediationPolicies) {
        if (apiDto != null) {
            apiDto.setMediationPolicies(mediationPolicies);
        } else if (mcpServerDto != null) {
            mcpServerDto.setMediationPolicies(mediationPolicies);
        }
    }

    public void setPolicies(List<String> policies) {

        if (apiDto != null) {
            apiDto.setPolicies(policies);
        } else if (mcpServerDto != null) {
            mcpServerDto.setPolicies(policies);
        }
    }

    public List<String> getPolicies() {
        if (apiDto != null) {
            return apiDto.getPolicies();
        } else if (mcpServerDto != null) {
            return mcpServerDto.getPolicies();
        }
        return null;
    }


}
