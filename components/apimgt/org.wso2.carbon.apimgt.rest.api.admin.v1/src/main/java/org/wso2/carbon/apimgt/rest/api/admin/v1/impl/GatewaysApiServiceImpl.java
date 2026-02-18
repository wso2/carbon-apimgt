/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayWithTokenDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import org.apache.cxf.jaxrs.ext.MessageContext;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

/**
 * Implementation of Platform Gateways Admin API (register/list self-hosted gateways with registration token).
 */
public class GatewaysApiServiceImpl implements GatewaysApiService {

    private static final Log log = LogFactory.getLog(GatewaysApiServiceImpl.class);

    /** Name pattern: lowercase alphanumeric and hyphens only. */
    private static final Pattern GATEWAY_NAME_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

    @Override
    public Response gatewaysPost(CreatePlatformGatewayRequestDTO body, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        validateCreateBody(body);
        if (log.isInfoEnabled()) {
            log.info("Creating new platform gateway with name: " + body.getName());
        }

        org.wso2.carbon.apimgt.api.PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        String propertiesJson = serializeProperties(body.getProperties());
        org.wso2.carbon.apimgt.api.model.CreatePlatformGatewayResult result = service.createGateway(
                organization,
                body.getName(),
                body.getDisplayName(),
                body.getDescription(),
                body.getVhost(),
                body.isIsCritical() != null && body.isIsCritical(),
                body.getFunctionalityType().value(),
                propertiesJson);

        PlatformGateway gateway = result.getGateway();
        PlatformGatewayWithTokenDTO dto = toDTOWithToken(gateway, result.getRegistrationToken());
        try {
            URI location = new URI(RestApiConstants.RESOURCE_PATH_PLATFORM_GATEWAYS + "/" + gateway.getId());
            return Response.created(location).entity(dto).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.CREATED).entity(dto).build();
        }
    }

    @Override
    public Response gatewaysGet(MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        org.wso2.carbon.apimgt.api.PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        List<PlatformGateway> gateways = service.listGatewaysByOrganization(organization);
        PlatformGatewayListDTO listDTO = new PlatformGatewayListDTO();
        listDTO.setCount(gateways.size());
        listDTO.setList(gateways.stream().map(this::toDTO).collect(Collectors.toList()));
        return Response.ok().entity(listDTO).build();
    }

    private void validateCreateBody(CreatePlatformGatewayRequestDTO body) throws APIManagementException {
        if (body == null) {
            throw RestApiUtil.buildBadRequestException("Request body is required");
        }
        if (StringUtils.isBlank(body.getName())) {
            throw RestApiUtil.buildBadRequestException("name is required");
        }
        if (body.getName().length() < 3 || body.getName().length() > 64) {
            throw RestApiUtil.buildBadRequestException("name must be between 3 and 64 characters");
        }
        if (!GATEWAY_NAME_PATTERN.matcher(body.getName()).matches()) {
            throw RestApiUtil.buildBadRequestException("name must contain only lowercase letters, numbers and hyphens (pattern: ^[a-z0-9-]+$)");
        }
        if (StringUtils.isBlank(body.getDisplayName())) {
            throw RestApiUtil.buildBadRequestException("displayName is required");
        }
        if (body.getDisplayName().length() > 128) {
            throw RestApiUtil.buildBadRequestException("displayName must be at most 128 characters");
        }
        if (StringUtils.isBlank(body.getVhost())) {
            throw RestApiUtil.buildBadRequestException("vhost is required");
        }
        if (body.getVhost().length() > 255) {
            throw RestApiUtil.buildBadRequestException("vhost must be at most 255 characters");
        }
        if (StringUtils.isNotBlank(body.getDescription()) && body.getDescription().length() > 1023) {
            throw RestApiUtil.buildBadRequestException("description must be at most 1023 characters");
        }
        if (body.getFunctionalityType() == null) {
            throw RestApiUtil.buildBadRequestException("functionalityType is required");
        }
    }

    /** Serialize properties map to JSON string for DB storage; null if empty/null. */
    private static String serializeProperties(Map<String, Object> properties) {
        if (properties == null || properties.isEmpty()) {
            return null;
        }
        return GSON.toJson(properties);
    }

    /** Deserialize properties JSON from DB to Map for response; null if empty/null. */
    private static Map<String, Object> deserializeProperties(String propertiesJson) {
        if (propertiesJson == null || propertiesJson.trim().isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(propertiesJson, MAP_TYPE);
        } catch (Exception e) {
            log.warn("Failed to parse gateway properties JSON, returning null", e);
            return null;
        }
    }

    /** Convert DB functionalityType string to response enum; defaults to REGULAR if unknown. */
    private static PlatformGatewayDTO.FunctionalityTypeEnum functionalityTypeFromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PlatformGatewayDTO.FunctionalityTypeEnum.REGULAR;
        }
        PlatformGatewayDTO.FunctionalityTypeEnum e = PlatformGatewayDTO.FunctionalityTypeEnum.fromValue(value.trim());
        return e != null ? e : PlatformGatewayDTO.FunctionalityTypeEnum.REGULAR;
    }

    private PlatformGatewayDTO toDTO(PlatformGateway g) {
        PlatformGatewayDTO dto = new PlatformGatewayDTO();
        dto.setId(g.getId());
        dto.setOrganizationId(g.getOrganizationId());
        dto.setName(g.getName());
        dto.setDisplayName(g.getDisplayName());
        dto.setDescription(g.getDescription());
        dto.setProperties(deserializeProperties(g.getProperties()));
        dto.setVhost(g.getVhost());
        dto.setIsCritical(g.isCritical());
        dto.setFunctionalityType(functionalityTypeFromString(g.getFunctionalityType()));
        dto.setIsActive(g.isActive());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        return dto;
    }

    private PlatformGatewayWithTokenDTO toDTOWithToken(PlatformGateway g, String registrationToken) {
        PlatformGatewayWithTokenDTO dto = new PlatformGatewayWithTokenDTO();
        dto.setId(g.getId());
        dto.setOrganizationId(g.getOrganizationId());
        dto.setName(g.getName());
        dto.setDisplayName(g.getDisplayName());
        dto.setDescription(g.getDescription());
        dto.setProperties(deserializeProperties(g.getProperties()));
        dto.setVhost(g.getVhost());
        dto.setIsCritical(g.isCritical());
        dto.setFunctionalityType(functionalityTypeFromStringWithToken(g.getFunctionalityType()));
        dto.setIsActive(g.isActive());
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        dto.setRegistrationToken(registrationToken);
        return dto;
    }

    private static PlatformGatewayWithTokenDTO.FunctionalityTypeEnum functionalityTypeFromStringWithToken(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PlatformGatewayWithTokenDTO.FunctionalityTypeEnum.REGULAR;
        }
        PlatformGatewayWithTokenDTO.FunctionalityTypeEnum e =
                PlatformGatewayWithTokenDTO.FunctionalityTypeEnum.fromValue(value.trim());
        return e != null ? e : PlatformGatewayWithTokenDTO.FunctionalityTypeEnum.REGULAR;
    }
}
