/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SettingsMappingUtil {

    private static final Log log = LogFactory.getLog(SettingsMappingUtil.class);

    /**
     * This method feeds data into the settingsDTO
     *
     * @param isUserAvailable check if user is logged in
     * @return SettingsDTO
     * @throws APIManagementException
     */
    public SettingsDTO fromSettingsToDTO(Boolean isUserAvailable) throws APIManagementException {

        SettingsDTO settingsDTO = new SettingsDTO();
        if (isUserAvailable) {
            settingsDTO.setAnalyticsEnabled(APIUtil.isAnalyticsEnabled());
            settingsDTO.setKeyManagerConfiguration(getSettingsKeyManagerConfigurationDTOList());
            settingsDTO.setGatewayConfiguration(getSettingsGatewayConfigurationDTOList());
        }
        settingsDTO.setScopes(getScopeList());
        settingsDTO.setGatewayTypes(APIUtil.getGatewayTypes());
        settingsDTO.setIsJWTEnabledForLoginTokens(APIUtil.isJWTEnabledForPortals());
        settingsDTO.setOrgAccessControlEnabled(APIUtil.isOrganizationAccessControlEnabled());
        return settingsDTO;
    }

    private List<SettingsKeyManagerConfigurationDTO> getSettingsKeyManagerConfigurationDTOList() {
        List<SettingsKeyManagerConfigurationDTO> list = new ArrayList<>();
        Map<String, KeyManagerConnectorConfiguration> keyManagerConnectorConfigurationMap =
                APIUtil.getKeyManagerConfigurations();
        keyManagerConnectorConfigurationMap.forEach((keyManagerName, keyManagerConfiguration) -> {
            if (!APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE.equals(keyManagerName)){
                list.add(fromKeyManagerConfigurationToSettingsKeyManagerConfigurationDTO(keyManagerName,
                        keyManagerConfiguration.getDisplayName(),keyManagerConfiguration.getDefaultScopesClaim(),
                        keyManagerConfiguration.getDefaultConsumerKeyClaim(),
                        keyManagerConfiguration.getConnectionConfigurations(),
                        keyManagerConfiguration.getEndpointConfigurations()));
            }
        });
        return list;
    }

    private List<String> getScopeList() throws APIManagementException {
        String definition = null;
        try {
            definition = IOUtils
                    .toString(RestApiUtil.class.getResourceAsStream("/admin-api.yaml"), "UTF-8");
        } catch (IOException e) {
            log.error("Error while reading the swagger definition", e);
        }
        APIDefinition oasParser = OASParserUtil.getOASParser(definition);
        Set<Scope> scopeSet = oasParser.getScopes(definition);
        List<String> scopeList = new ArrayList<>();
        for (Scope entry : scopeSet) {
            scopeList.add(entry.getKey());
        }
        return scopeList;
    }

    private static SettingsKeyManagerConfigurationDTO fromKeyManagerConfigurationToSettingsKeyManagerConfigurationDTO(
            String name, String displayName, String scopesClaim, String consumerKeyClaim,
            List<ConfigurationDto> connectionConfigurationDtoList,List<ConfigurationDto> endpointConfigurations) {

        SettingsKeyManagerConfigurationDTO settingsKeyManagerConfigurationDTO =
                new SettingsKeyManagerConfigurationDTO();
        settingsKeyManagerConfigurationDTO.setDisplayName(displayName);
        settingsKeyManagerConfigurationDTO.setType(name);
        settingsKeyManagerConfigurationDTO.setDefaultScopesClaim(scopesClaim);
        settingsKeyManagerConfigurationDTO.setDefaultConsumerKeyClaim(consumerKeyClaim);
        if (connectionConfigurationDtoList != null) {
            for (ConfigurationDto configurationDto : connectionConfigurationDtoList) {
                KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                keyManagerConfigurationDTO.setName(configurationDto.getName());
                keyManagerConfigurationDTO.setLabel(configurationDto.getLabel());
                keyManagerConfigurationDTO.setType(configurationDto.getType());
                keyManagerConfigurationDTO.setRequired(configurationDto.isRequired());
                keyManagerConfigurationDTO.setMask(configurationDto.isMask());
                keyManagerConfigurationDTO.setMultiple(configurationDto.isMultiple());
                keyManagerConfigurationDTO.setTooltip(configurationDto.getTooltip());
                keyManagerConfigurationDTO.setDefault(configurationDto.getDefaultValue());
                keyManagerConfigurationDTO.setValues(configurationDto.getValues());
                settingsKeyManagerConfigurationDTO.getConfigurations().add(keyManagerConfigurationDTO);
            }
        }
        if (endpointConfigurations != null) {
            for (ConfigurationDto configurationDto : endpointConfigurations) {
                KeyManagerConfigurationDTO keyManagerConfigurationDTO = new KeyManagerConfigurationDTO();
                keyManagerConfigurationDTO.setName(configurationDto.getName());
                keyManagerConfigurationDTO.setLabel(configurationDto.getLabel());
                keyManagerConfigurationDTO.setType(configurationDto.getType());
                keyManagerConfigurationDTO.setRequired(configurationDto.isRequired());
                keyManagerConfigurationDTO.setMask(configurationDto.isMask());
                keyManagerConfigurationDTO.setMultiple(configurationDto.isMultiple());
                keyManagerConfigurationDTO.setTooltip(configurationDto.getTooltip());
                keyManagerConfigurationDTO.setDefault(configurationDto.getDefaultValue());
                keyManagerConfigurationDTO.setValues(configurationDto.getValues());
                settingsKeyManagerConfigurationDTO.getEndpointConfigurations().add(keyManagerConfigurationDTO);
            }
        }
        return settingsKeyManagerConfigurationDTO;
    }

    private static List<SettingsGatewayConfigurationDTO> getSettingsGatewayConfigurationDTOList() {
        List<SettingsGatewayConfigurationDTO> list = new ArrayList<>();
        Map<String, GatewayAgentConfiguration> gatewayConfigurations =
                ServiceReferenceHolder.getInstance().getExternalGatewayConnectorConfigurations();
        gatewayConfigurations.forEach((gatewayName, gatewayConfiguration) -> {
            SettingsGatewayConfigurationDTO settingsFederatedGatewayConfigurationDTO =
                    new SettingsGatewayConfigurationDTO();
            settingsFederatedGatewayConfigurationDTO.setType(gatewayConfiguration.getType());
            settingsFederatedGatewayConfigurationDTO.setDisplayName(gatewayConfiguration.getType());
            settingsFederatedGatewayConfigurationDTO.setDefaultHostnameTemplate(gatewayConfiguration.getDefaultHostnameTemplate());
            List<ConfigurationDto> connectionConfigurations = gatewayConfiguration.getConnectionConfigurations();
            if (connectionConfigurations != null) {
                for (ConfigurationDto dto : connectionConfigurations) {
                    settingsFederatedGatewayConfigurationDTO.getConfigurations().add(fromConfigurationToConfigurationDTO(dto));
                }
            }
            list.add(settingsFederatedGatewayConfigurationDTO);

        });

        //Add APK and Synapse Gateways configured through toml to the list
        List<String> gatewayTypesFromConfig = APIUtil.getGatewayTypes();
        for (String type : gatewayTypesFromConfig) {
            SettingsGatewayConfigurationDTO gateway = new SettingsGatewayConfigurationDTO();
            gateway.setType(type);
            gateway.setDisplayName(type);
            if (list.stream().noneMatch(obj -> obj.getType().equals(type))) {
                list.add(gateway);
            }
        }
        return list;
    }

    private static GatewayConfigurationDTO fromConfigurationToConfigurationDTO(ConfigurationDto configuration) {
        GatewayConfigurationDTO dto = new GatewayConfigurationDTO();
        dto.setName(configuration.getName());
        dto.setLabel(configuration.getLabel());
        dto.setType(configuration.getType());
        dto.setRequired(configuration.isRequired());
        dto.setMask(configuration.isMask());
        dto.setMultiple(configuration.isMultiple());
        dto.setTooltip(configuration.getTooltip());
        dto.setDefault(configuration.getDefaultValue());
        dto.setValues(configuration.getValues());
        return dto;
    }

    public List<String> GetRoleScopeList(String[] userRoles, Map<String, String> scopeRoleMapping) {
        List<String> userRoleList;
        List<String> authorizedScopes = new ArrayList<>();

        if (userRoles == null || userRoles.length == 0) {
            userRoles = new String[0];
        }

        userRoleList = Arrays.asList(userRoles);
        Iterator<Map.Entry<String, String>> iterator = scopeRoleMapping.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            for (String aRole : entry.getValue().split(",")) {
                if (userRoleList.contains(aRole)) {
                    authorizedScopes.add(entry.getKey());
                }
            }
        }
        return authorizedScopes;
    }
}