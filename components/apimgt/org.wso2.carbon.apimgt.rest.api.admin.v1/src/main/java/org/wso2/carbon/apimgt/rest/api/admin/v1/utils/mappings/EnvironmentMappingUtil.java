/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayTierMapping;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.dto.PlatformGatewayConnectConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AdditionalPropertyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentPermissionsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayTierMappingDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.VHostDTO;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class manage Environment mapping to EnvironmentDTO
 */
public class EnvironmentMappingUtil {

    private static final Log log = LogFactory.getLog(EnvironmentMappingUtil.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Convert list of Environment to EnvironmentListDTO
     *
     * @param envList List of Environment
     * @return EnvironmentListDTO containing Environment list
     */
    public static EnvironmentListDTO fromEnvListToEnvListDTO(List<Environment> envList) {
        EnvironmentListDTO envListDTO = new EnvironmentListDTO();
        envListDTO.setCount(envList.size());
        envListDTO.setList(envList.stream().map(EnvironmentMappingUtil::fromEnvToEnvDTO).collect(Collectors.toList()));
        return envListDTO;
    }

    /**
     * Convert Environment to EnvironmentDTO
     *
     * @param env Environment
     * @return EnvironmentDTO containing Environment
     */
    public static EnvironmentDTO fromEnvToEnvDTO(Environment env) {
        EnvironmentDTO envDTO = new EnvironmentDTO();
        envDTO.setId(env.getUuid());
        envDTO.setName(env.getName());
        envDTO.setType(env.getType());
        envDTO.setDisplayName(env.getDisplayName());
        envDTO.setDescription(env.getDescription());
        envDTO.setProvider(env.getProvider());
        envDTO.setGatewayType(env.getGatewayType());
        envDTO.setIsReadOnly(env.isReadOnly());
        envDTO.setMode(EnvironmentDTO.ModeEnum.valueOf(env.getMode()));
        envDTO.setApiDiscoveryScheduledWindow(env.getApiDiscoveryScheduledWindow());
        envDTO.setVhosts(env.getVhosts().stream().map(EnvironmentMappingUtil::fromVHostToVHostDTO)
                .collect(Collectors.toList()));
        envDTO.setAdditionalProperties(fromAdditionalPropertiesToAdditionalPropertiesDTO
                (env.getAdditionalProperties()));
        envDTO.setTierMappings(fromTierMappingsToTierMappingDTOs(env.getTierMappings()));
        envDTO.setPermissions(mapPermissionsToDTO(env.getPermissions()));
        envDTO.setUniversalGatewayVersion(resolveUniversalGatewayVersion());
        return envDTO;
    }

    /**
     * Convert a Platform Gateway to EnvironmentDTO so it can be included in the unified
     * GET /environments list (deploy targets). UI can use gatewayType to distinguish from
     * traditional gateway environments.
     *
     * @param gateway       PlatformGateway from AM_PLATFORM_GATEWAY
     * @param gatewayType   gateway type constant (e.g. Universal)
     * @return EnvironmentDTO suitable for deploy-target list
     */
    public static EnvironmentDTO fromPlatformGatewayToEnvDTO(PlatformGateway gateway, String gatewayType,
            GatewayVisibilityPermissionConfigurationDTO permissions) {
        EnvironmentDTO envDTO = new EnvironmentDTO();
        envDTO.setId(gateway.getId());
        envDTO.setName(gateway.getName());
        envDTO.setDisplayName(gateway.getDisplayName());
        envDTO.setDescription(gateway.getDescription());
        envDTO.setGatewayType(gatewayType);
        // Allow delete in UI; server validates and returns 409 if API revisions are deployed
        envDTO.setIsReadOnly(false);
        envDTO.setMode(EnvironmentDTO.ModeEnum.WRITE_ONLY);
        envDTO.setType("hybrid");

        // Populate vhosts from platform gateway's vhost
        List<VHostDTO> vhosts = new ArrayList<>();
        if (gateway.getVhost() != null && !gateway.getVhost().isEmpty()) {
            VHostDTO vhostDTO = new VHostDTO();
            vhostDTO.setHost(gateway.getVhost());
            vhostDTO.setHttpPort(80);
            vhostDTO.setHttpsPort(443);
            vhostDTO.setWsPort(9099);
            vhostDTO.setWssPort(8099);
            vhosts.add(vhostDTO);
        }
        envDTO.setVhosts(vhosts);
        envDTO.setEndpointURIs(new ArrayList<>());
        // Gateway URL for platform gateways (same shape as Platform Gateways API vhost)
        if (gateway.getVhost() != null && !gateway.getVhost().isEmpty()) {
            envDTO.setVhost(java.net.URI.create("https://" + gateway.getVhost().trim()));
        }

        // Include platform gateway metadata in additionalProperties for UI consumption
        List<AdditionalPropertyDTO> additionalProps = new ArrayList<>();
        AdditionalPropertyDTO isActiveProperty = new AdditionalPropertyDTO();
        isActiveProperty.setKey("isActive");
        isActiveProperty.setValue(String.valueOf(gateway.isActive()));
        additionalProps.add(isActiveProperty);
        AdditionalPropertyDTO platformGatewayIdProperty = new AdditionalPropertyDTO();
        platformGatewayIdProperty.setKey("platformGatewayId");
        platformGatewayIdProperty.setValue(gateway.getId());
        additionalProps.add(platformGatewayIdProperty);
        envDTO.setAdditionalProperties(additionalProps);

        envDTO.setPermissions(mapPermissionsToDTO(permissions));
        // Gateway connection status for GET /environments (Active/Inactive for platform gateways)
        envDTO.setStatus(Boolean.TRUE.equals(gateway.isActive())
                ? EnvironmentDTO.StatusEnum.ACTIVE
                : EnvironmentDTO.StatusEnum.INACTIVE);
        envDTO.setUniversalGatewayVersion(resolveUniversalGatewayVersion());
        return envDTO;
    }

    /**
     * Resolve Universal Gateway version from config (apim.universal_gateway.version in api-manager.xml).
     */
    private static String resolveUniversalGatewayVersion() {
        PlatformGatewayConnectConfig config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration().getPlatformGatewayConnectConfig();
        if (config == null) {
            return null;
        }
        String global = config.getUniversalGatewayVersion();
        return (global != null && !global.isEmpty()) ? global : null;
    }

    /**
     * Map internal permissions model to REST API DTO.
     * Always returns a non-null DTO, defaulting to PUBLIC if permissions are null.
     */
    private static EnvironmentPermissionsDTO mapPermissionsToDTO(
            GatewayVisibilityPermissionConfigurationDTO permissions) {
        EnvironmentPermissionsDTO dto = new EnvironmentPermissionsDTO();
        if (permissions == null || permissions.getPermissionType() == null) {
            dto.setPermissionType(EnvironmentPermissionsDTO.PermissionTypeEnum.PUBLIC);
            return dto;
        }
        dto.setPermissionType(EnvironmentPermissionsDTO.PermissionTypeEnum
                .fromValue(permissions.getPermissionType()));
        dto.setRoles(permissions.getRoles());
        return dto;
    }

    /**
     * Converts AdditionalProperties into a AdditionalPropertiesDTO.
     *
     * @param additionalProperties Set of additional properties
     * @return List<AdditionalPropertyDTO>
     */
    public static List<AdditionalPropertyDTO> fromAdditionalPropertiesToAdditionalPropertiesDTO(Map<String, String>
                                                                                                        additionalProperties) {
        List<AdditionalPropertyDTO> additionalPropertyDTOList = new ArrayList<>();
        for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
            AdditionalPropertyDTO additionalPropertyDTO = new AdditionalPropertyDTO();
            additionalPropertyDTO.setKey(entry.getKey());
            additionalPropertyDTO.setValue(entry.getValue());
            additionalPropertyDTOList.add(additionalPropertyDTO);
        }
        return additionalPropertyDTOList;
    }
    /**
     * Convert VHost to VHostDTO
     *
     * @param vHost VHost
     * @return VHostDTO
     */
    public static VHostDTO fromVHostToVHostDTO(VHost vHost) {
        VHostDTO vHostDTO = new VHostDTO();
        vHostDTO.setHost(vHost.getHost());
        vHostDTO.setHttpContext(vHost.getHttpContext());
        vHostDTO.setHttpPort(vHost.getHttpPort());
        vHostDTO.setHttpsPort(vHost.getHttpsPort());
        vHostDTO.setWsPort(vHost.getWsPort());
        vHostDTO.setWssPort(vHost.getWssPort());
        return vHostDTO;
    }

    /**
     * Convert EnvironmentListDTO to list of Environment
     *
     * @param envListDto EnvironmentListDTO
     * @return EnvironmentListDTO containing Environment list
     */
    public static List<Environment> fromEnvListDtoToEnvList(EnvironmentListDTO envListDto)
            throws APIManagementException {
        List<Environment> envList = new ArrayList<>(envListDto.getCount());
        for (EnvironmentDTO envDto : envListDto.getList()) {
            envList.add(fromEnvDtoToEnv(envDto));
        }
        return envList;
    }

    /**
     * Convert EnvironmentDTO to Environment
     *
     * @param envDTO EnvironmentDTO
     * @return Environment
     */
    public static Environment fromEnvDtoToEnv(EnvironmentDTO envDTO) throws APIManagementException {
        Environment env = new Environment();
        env.setUuid(envDTO.getId());
        env.setName(envDTO.getName());
        env.setType(envDTO.getType());
        // Backward compatibility: displayName is optional for gateway environments.
        // If it's missing, default it to the environment `name` to avoid DB/validation failures.
        String displayName = envDTO.getDisplayName();
        if (displayName == null || displayName.trim().isEmpty()) {
            displayName = envDTO.getName();
        }
        env.setDisplayName(displayName);
        env.setDescription(envDTO.getDescription());
        env.setProvider(envDTO.getProvider());
        env.setGatewayType(envDTO.getGatewayType());
        env.setReadOnly(envDTO.isIsReadOnly());
        env.setMode(envDTO.getMode().toString());
        env.setApiDiscoveryScheduledWindow(envDTO.getApiDiscoveryScheduledWindow());
        env.setVhosts(envDTO.getVhosts().stream().map(EnvironmentMappingUtil::fromVHostDtoToVHost)
                .collect(Collectors.toList()));
        env.setAdditionalProperties(fromAdditionalPropertiesDTOToAdditionalProperties
                (envDTO.getAdditionalProperties()));
        env.setTierMappings(fromTierMappingDTOsToTierMappings(envDTO.getTierMappings()));
        EnvironmentPermissionsDTO permissions = envDTO.getPermissions();
        if (permissions != null && permissions.getPermissionType() != null) {
            GatewayVisibilityPermissionConfigurationDTO permissionsConfiguration = new GatewayVisibilityPermissionConfigurationDTO();
            permissionsConfiguration.setPermissionType(permissions.getPermissionType().toString());
            permissionsConfiguration.setRoles(permissions.getRoles());
            env.setPermissions(permissionsConfiguration);
        } else {
            env.setPermissions(new GatewayVisibilityPermissionConfigurationDTO());
        }
        return env;
    }

    /**
     * Convert VHostDTO to VHost
     *
     * @param vhostDTO VHostDTO
     * @return VHostDTO
     */
    public static VHost fromVHostDtoToVHost(VHostDTO vhostDTO) {
        VHost vhost = new VHost();
        vhost.setHost(vhostDTO.getHost());
        vhost.setHttpContext(vhostDTO.getHttpContext());
        vhost.setHttpPort(vhostDTO.getHttpPort());
        vhost.setHttpsPort(vhostDTO.getHttpsPort());
        vhost.setWsPort(vhostDTO.getWsPort());
        vhost.setWssPort(vhostDTO.getWssPort());
        if (vhostDTO.getWsHost() == null) {
            vhost.setWsHost(vhostDTO.getHost());
        } else {
            vhost.setWsHost(vhostDTO.getWsHost());
        }
        if (vhostDTO.getWssHost() == null) {
            vhost.setWssHost(vhostDTO.getHost());
        } else {
            vhost.setWssHost(vhostDTO.getWssHost());
        }
        return vhost;
    }

    /**
     * Converts AdditionalPropertiesDTO into a AdditionalProperties map.
     *
     * @param additionalPropertiesDTOs Set of additional propertyDTOs
     * @return Map<String, String> of Additional properties
     */
    public static Map<String, String>  fromAdditionalPropertiesDTOToAdditionalProperties(List<AdditionalPropertyDTO>
                                                                                                 additionalPropertiesDTOs) {
        Map<String,String> additionalProperties = new HashMap<>();
        for (AdditionalPropertyDTO entry : additionalPropertiesDTOs) {
            additionalProperties.putIfAbsent(entry.getKey(),entry.getValue());
        }
        return additionalProperties;
    }

    /**
     * Converts a list of GatewayTierMapping model objects to GatewayTierMappingDTOs.
     * The remotePlanReference JSON string is deserialized to a Map for the DTO.
     */
    public static List<GatewayTierMappingDTO> fromTierMappingsToTierMappingDTOs(
            List<GatewayTierMapping> tierMappings) {
        List<GatewayTierMappingDTO> dtos = new ArrayList<>();
        if (tierMappings == null) {
            return dtos;
        }
        for (GatewayTierMapping mapping : tierMappings) {
            GatewayTierMappingDTO dto = new GatewayTierMappingDTO();
            dto.setLocalTierName(mapping.getLocalTierName());
            Map<String, Object> refMap = Collections.emptyMap();
            if (mapping.getRemotePlanReference() != null) {
                try {
                    refMap = OBJECT_MAPPER.readValue(mapping.getRemotePlanReference(),
                            new TypeReference<Map<String, Object>>() {});
                } catch (JsonProcessingException e) {
                    log.warn("Invalid JSON in remote plan reference for tier: " + mapping.getLocalTierName()
                            + ", treating it as a raw value");
                    // Stored value is not valid JSON; expose as-is under "raw" key
                    refMap = new HashMap<>();
                    refMap.put("raw", mapping.getRemotePlanReference());
                }
            }
            dto.setRemotePlanReference(refMap);
            dtos.add(dto);
        }
        return dtos;
    }

    /**
     * Converts a list of GatewayTierMappingDTOs to GatewayTierMapping model objects.
     * The remotePlanReference Map is serialized to a JSON string for storage.
     */
    public static List<GatewayTierMapping> fromTierMappingDTOsToTierMappings(
            List<GatewayTierMappingDTO> dtos) throws APIManagementException {
        List<GatewayTierMapping> mappings = new ArrayList<>();
        if (dtos == null) {
            return mappings;
        }
        for (GatewayTierMappingDTO dto : dtos) {
            String refJson = null;
            if (dto.getRemotePlanReference() != null) {
                try {
                    refJson = OBJECT_MAPPER.writeValueAsString(dto.getRemotePlanReference());
                } catch (JsonProcessingException e) {
                    String tierName = StringUtils.defaultIfBlank(dto.getLocalTierName(), "<unknown>");
                    log.error("Failed to serialize remote plan reference for tier: " + tierName, e);
                    throw new APIManagementException("Failed to serialize remote plan reference for tier: "
                            + tierName, e, ExceptionCodes.INTERNAL_ERROR);
                }
            }
            GatewayTierMapping mapping = new GatewayTierMapping();
            mapping.setLocalTierName(dto.getLocalTierName());
            mapping.setRemotePlanReference(refJson);
            mappings.add(mapping);
        }
        return mappings;
    }

}
