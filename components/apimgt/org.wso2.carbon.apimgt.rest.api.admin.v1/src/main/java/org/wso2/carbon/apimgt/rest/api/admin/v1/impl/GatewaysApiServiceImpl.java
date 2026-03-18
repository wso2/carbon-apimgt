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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayRegistrationResult;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CreatePlatformGatewayRequestPermissionsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PlatformGatewayResponsePermissionsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GatewayResponseWithTokenDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UpdatePlatformGatewayRequestDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.UpdatePlatformGatewayRequestPermissionsDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementation of Platform Gateways Admin API (register/list self-hosted gateways with registration token).
 * <p>
 * Persistence: request {@code vhost} (gateway URL) is parsed to host and port. These are stored in existing DB tables
 * without schema changes:
 * <ul>
 *   <li><b>AM_GATEWAY_ENVIRONMENT</b> – one row per platform gateway (UUID=gatewayId, NAME, DISPLAY_NAME,
 *       DESCRIPTION, GATEWAY_TYPE='Universal', CONFIGURATION=JSON with organization, isActive, createdAt,
 *       updatedAt, properties).</li>
 *   <li><b>AM_GW_VHOST</b> – one row per env (GATEWAY_ENV_ID, HOST, HTTP_PORT, HTTPS_PORT, ...). Host and port
 *       come from parsed vhost URL; updated in {@link #updateDynamicEnvironmentForPlatformGateway} after create.</li>
 *   <li><b>AM_GATEWAY_TOKEN</b> / <b>AM_GW_INSTANCES</b> – token and instance registration for the gateway.</li>
 * </ul>
 * On read, gateway list/get loads from Environment (AM_GATEWAY_ENVIRONMENT + AM_GW_VHOST); response
 * {@code vhost} is derived from the first VHost's host (and default https). Same property name as platform API; type is URL.
 */
public class GatewaysApiServiceImpl implements GatewaysApiService {

    private static final Log log = LogFactory.getLog(GatewaysApiServiceImpl.class);

    /** Name pattern: lowercase alphanumeric and hyphens only. */
    private static final Pattern GATEWAY_NAME_PATTERN = Pattern.compile("^[a-z0-9-]+$");
    private static final Gson GSON = new Gson();
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() { }.getType();

    private static final String GATEWAY_PROPERTIES_SECTION = "gatewayController";
    private static final String GATEWAY_PROP_BASE_URL = "baseUrl";

    private static final int DEFAULT_HTTPS_PORT = 443;

    /**
     * Parse gateway URL (e.g. https://mg.example.com:9443) to host and port. DB stores host only.
     */
    private static class ParsedGatewayUrl {
        final String host;
        final int port;

        ParsedGatewayUrl(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    private static ParsedGatewayUrl parseGatewayUrl(String gatewayUrl) {
        if (StringUtils.isBlank(gatewayUrl)) {
            return null;
        }
        String normalized = gatewayUrl.trim();
        if (!normalized.contains("://")) {
            normalized = "https://" + normalized;
        }
        try {
            URL url = new URL(normalized);
            String host = url.getHost();
            if (host == null || host.isEmpty()) {
                return null;
            }
            int port = url.getPort();
            if (port <= 0) {
                port = url.getDefaultPort() > 0 ? url.getDefaultPort() : DEFAULT_HTTPS_PORT;
            }
            return new ParsedGatewayUrl(host, port);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Invalid vhost URL: [" + gatewayUrl + "]", e);
            } else if (log.isWarnEnabled()) {
                log.warn("Invalid vhost URL: [" + gatewayUrl + "] - " + e.getMessage());
            }
            return null;
        }
    }

    /** Build gateway URL from stored host (DB stores host only). */
    private static String toGatewayUrl(String host) {
        if (StringUtils.isBlank(host)) {
            return null;
        }
        return "https://" + host.trim();
    }

    /** VHost in request DTOs is URI; convert to string for parsing. */
    private static String vhostString(URI uri) {
        return uri != null ? uri.toString() : null;
    }

    @Override
    public Response createPlatformGateway(CreatePlatformGatewayRequestDTO body, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        validateCreateBody(body);
        validateGatewayNameAvailability(organization, body.getName());
        log.info("Creating new platform gateway with name: " + body.getName());

        PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        String propertiesJson = serializeProperties(body.getProperties());
        String host = resolveHostFromGatewayUrl(vhostString(body.getVhost()), body.getProperties());
        PlatformGatewayRegistrationResult result = service.createGateway(
                organization,
                body.getName(),
                body.getDisplayName(),
                body.getDescription(),
                host,
                propertiesJson);
        PlatformGateway gateway = result.getGateway();
        GatewayVisibilityPermissionConfigurationDTO visibility = buildGatewayVisibility(body);
        // Update the environment with VHost (from vhost URL), permissions, and additional properties
        updateDynamicEnvironmentForPlatformGateway(organization, body, gateway.getId());
        GatewayResponseWithTokenDTO dto = toDTOWithToken(gateway, result.getRegistrationToken(), visibility);
        try {
            URI location = new URI(RestApiConstants.RESOURCE_PATH_PLATFORM_GATEWAYS + "/" + gateway.getId());
            return Response.created(location).entity(dto).build();
        } catch (URISyntaxException e) {
            return Response.status(Response.Status.CREATED).entity(dto).build();
        }
    }

    /**
     * Validate that a gateway with the given name doesn't already exist.
     * Checks both platform gateways (by name) and environments (by name).
     */
    private void validateGatewayNameAvailability(String organization, String gatewayName)
            throws APIManagementException {

        // Check if a platform gateway with this name already exists
        PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        List<PlatformGateway> existingGateways = service.listGatewaysByOrganization(organization);
        boolean gatewayExists = existingGateways.stream()
                .anyMatch(gw -> StringUtils.equals(gw.getName(), gatewayName));
        if (gatewayExists) {
            throw new APIManagementException(
                    String.format(ExceptionCodes.UNIVERSAL_GATEWAY_NAME_ALREADY_EXISTS.getErrorDescription(), gatewayName),
                    ExceptionCodes.UNIVERSAL_GATEWAY_NAME_ALREADY_EXISTS);
        }

        // Also check if a non-platform environment with this name exists
        APIAdmin apiAdmin = new APIAdminImpl();
        boolean envExists = apiAdmin.getAllEnvironments(organization).stream()
                .filter(env -> !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType()))
                .anyMatch(env -> StringUtils.equals(env.getName(), gatewayName));
        if (envExists) {
            throw new APIManagementException(
                    String.format(ExceptionCodes.UNIVERSAL_GATEWAY_NAME_ALREADY_EXISTS.getErrorDescription(), gatewayName),
                    ExceptionCodes.UNIVERSAL_GATEWAY_NAME_ALREADY_EXISTS);
        }
    }

    /**
     * Update the environment created by PlatformGatewayServiceImpl with VHost (from vhost URL), permissions, and additional properties.
     */
    private void updateDynamicEnvironmentForPlatformGateway(String organization, CreatePlatformGatewayRequestDTO body,
                                                            String platformGatewayId)
            throws APIManagementException {

        APIAdmin apiAdmin = new APIAdminImpl();
        // The environment was created by PlatformGatewayServiceImpl with UUID = platformGatewayId
        Environment existingEnvironment = apiAdmin.getEnvironment(organization, platformGatewayId);
        if (existingEnvironment == null) {
            log.warn("Environment not found for platform gateway ID: " + platformGatewayId + ", skipping update");
            return;
        }

        // Update fields that weren't set during initial creation
        Map<String, String> environmentAdditionalProperties = new HashMap<>();
        environmentAdditionalProperties.put("platformGatewayId", platformGatewayId);
        existingEnvironment.setAdditionalProperties(environmentAdditionalProperties);

        List<VHost> vHosts = new ArrayList<>();
        vHosts.add(buildVHostFromCreateBody(body));
        existingEnvironment.setVhosts(vHosts);

        // Persist permissions so GET /environments and GET /environments/{id} return them for this platform gateway
        existingEnvironment.setPermissions(buildGatewayVisibility(body));

        apiAdmin.updateEnvironment(organization, existingEnvironment);
    }

    /**
     * Resolve host from vhost URL or, as fallback, from properties.gatewayController.baseUrl.
     */
    private String resolveHostFromGatewayUrl(String gatewayUrl, Map<String, Object> properties) {
        if (StringUtils.isNotBlank(gatewayUrl)) {
            ParsedGatewayUrl parsed = parseGatewayUrl(gatewayUrl);
            if (parsed != null) {
                return parsed.host;
            }
        }
        if (properties != null) {
            Object controller = properties.get(GATEWAY_PROPERTIES_SECTION);
            if (controller instanceof Map) {
                Object baseUrl = ((Map<?, ?>) controller).get(GATEWAY_PROP_BASE_URL);
                if (baseUrl instanceof String && StringUtils.isNotBlank((String) baseUrl)) {
                    try {
                        return new URL((String) baseUrl).getHost();
                    } catch (Exception ignored) {
                        // ignore
                    }
                }
            }
        }
        return null;
    }

    /**
     * Build VHost for environment from create body vhost URL (and optional properties.baseUrl fallback).
     */
    private VHost buildVHostFromCreateBody(CreatePlatformGatewayRequestDTO body) {
        VHost vHost = new VHost();
        String host = null;
        int httpsPort = VHost.DEFAULT_HTTPS_PORT;
        int httpPort = VHost.DEFAULT_HTTP_PORT;

        if (StringUtils.isNotBlank(vhostString(body.getVhost()))) {
            ParsedGatewayUrl parsed = parseGatewayUrl(vhostString(body.getVhost()));
            if (parsed != null) {
                host = parsed.host;
                httpsPort = parsed.port;
            }
        }
        if (host == null && body.getProperties() != null) {
            Object controller = body.getProperties().get(GATEWAY_PROPERTIES_SECTION);
            if (controller instanceof Map) {
                Object baseUrl = ((Map<?, ?>) controller).get(GATEWAY_PROP_BASE_URL);
                if (baseUrl instanceof String && StringUtils.isNotBlank((String) baseUrl)) {
                    try {
                        URL parsedUrl = new URL((String) baseUrl);
                        host = parsedUrl.getHost();
                        httpsPort = parsedUrl.getPort() > 0 ? parsedUrl.getPort() : parsedUrl.getDefaultPort();
                        if (httpsPort <= 0) {
                            httpsPort = VHost.DEFAULT_HTTPS_PORT;
                        }
                        httpPort = VHost.DEFAULT_HTTP_PORT;
                    } catch (Exception ignored) {
                        // fallback
                    }
                }
            }
        }
        vHost.setHost(host != null ? host : "");
        vHost.setHttpsPort(httpsPort);
        vHost.setHttpPort(httpPort);
        vHost.setWsHost(host);
        vHost.setWssHost(host);
        return vHost;
    }

    /**
     * Build gateway visibility permissions from the request body.
     * If permissions are provided in the request body, use them; otherwise default to PUBLIC.
     */
    private GatewayVisibilityPermissionConfigurationDTO buildGatewayVisibility(CreatePlatformGatewayRequestDTO body) {

        GatewayVisibilityPermissionConfigurationDTO visibility = new GatewayVisibilityPermissionConfigurationDTO();

        // Check if permissions are explicitly provided in the request
        CreatePlatformGatewayRequestPermissionsDTO requestPermissions = body.getPermissions();
        if (requestPermissions != null) {
            CreatePlatformGatewayRequestPermissionsDTO.PermissionTypeEnum permType = requestPermissions.getPermissionType();
            if (permType != null) {
                switch (permType) {
                    case ALLOW:
                        visibility.setPermissionType("ALLOW");
                        break;
                    case DENY:
                        visibility.setPermissionType("DENY");
                        break;
                    default:
                        visibility.setPermissionType("PUBLIC");
                        break;
                }
            } else {
                visibility.setPermissionType("PUBLIC");
            }
            if (requestPermissions.getRoles() != null) {
                visibility.setRoles(new ArrayList<>(requestPermissions.getRoles()));
            }
            return visibility;
        }

        // Fallback: check properties map for backward compatibility
        Map<String, Object> properties = body.getProperties();
        if (properties == null) {
            visibility.setPermissionType("PUBLIC");
            return visibility;
        }

        Object visibilityValue = properties.get("visibility");
        String visibilityType = visibilityValue != null ? visibilityValue.toString().trim().toUpperCase() : "PUBLIC";
        switch (visibilityType) {
            case "ALLOW":
            case "RESTRICTED":
                visibility.setPermissionType("ALLOW");
                break;
            case "DENY":
                visibility.setPermissionType("DENY");
                break;
            default:
                visibility.setPermissionType("PUBLIC");
                break;
        }

        Object roles = properties.get("roles");
        if (roles instanceof List) {
            List<String> roleList = ((List<?>) roles).stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .collect(Collectors.toList());
            visibility.setRoles(roleList);
        }

        return visibility;
    }

    @Override
    public Response getPlatformGateways(MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        List<PlatformGateway> gateways = service.listGatewaysByOrganization(organization);

        // Fetch environment permissions map (gateway name -> permissions)
        APIAdmin apiAdmin = new APIAdminImpl();
        List<Environment> environments = apiAdmin.getAllEnvironments(organization);
        Map<String, GatewayVisibilityPermissionConfigurationDTO> permissionsMap = environments.stream()
                .filter(env -> env.getName() != null)
                .filter(env -> env.getPermissions() != null)
                .collect(Collectors.toMap(Environment::getName, Environment::getPermissions,
                        (existing, replacement) -> existing));

        GatewayListDTO listDTO = new GatewayListDTO();
        listDTO.setCount(gateways.size());
        listDTO.setList(gateways.stream()
                .map(g -> toDTO(g, permissionsMap.get(g.getName())))
                .collect(Collectors.toList()));
        return Response.ok().entity(listDTO).build();
    }

    @Override
    public Response updatePlatformGateway(String gatewayId, UpdatePlatformGatewayRequestDTO body,
            MessageContext messageContext) throws APIManagementException {
        validateIdentifier(gatewayId, "gatewayId");
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        if (body == null) {
            throw RestApiUtil.buildBadRequestException("Request body is required");
        }
        validateUpdateBody(body);
        PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        PlatformGateway existing = service.getGatewayById(gatewayId);
        if (existing == null) {
            throw new APIManagementException("Platform gateway not found: " + gatewayId,
                    ExceptionCodes.UNIVERSAL_GATEWAY_NOT_FOUND);
        }
        if (!Objects.equals(body.getName(), existing.getName())) {
            throw RestApiUtil.buildBadRequestException("name in body must match existing gateway (immutable)");
        }
        String bodyHost = resolveHostFromGatewayUrl(vhostString(body.getVhost()), body.getProperties());
        if (bodyHost == null || bodyHost.isEmpty()) {
            throw RestApiUtil.buildBadRequestException("vhost is required for PUT");
        }
        if (!Objects.equals(bodyHost, existing.getVhost())) {
            throw RestApiUtil.buildBadRequestException("vhost in body must match existing gateway (immutable)");
        }
        log.info("Updating platform gateway: " + gatewayId);
        String displayName = body.getDisplayName();
        String description = body.getDescription();
        String propertiesJson = serializeProperties(body.getProperties());
        PlatformGateway gateway = service.updateGateway(organization, gatewayId, displayName, description,
                propertiesJson);
        // Keep gateway environment in sync: update permissions and/or displayName/description so GET /environments reflects them
        if (body.getPermissions() != null || displayName != null || description != null) {
            updateEnvironmentForPlatformGatewayPut(organization, gateway, body.getPermissions(),
                    displayName != null ? gateway.getDisplayName() : null,
                    description != null ? gateway.getDescription() : null);
        }
        GatewayVisibilityPermissionConfigurationDTO permissions = null;
        APIAdmin apiAdmin = new APIAdminImpl();
        Environment env = apiAdmin.getEnvironment(organization, gateway.getId());
        if (env != null) {
            permissions = env.getPermissions();
        }
        return Response.ok().entity(toDTO(gateway, permissions)).build();
    }

    @Override
    public Response deletePlatformGateway(String gatewayId, MessageContext messageContext)
            throws APIManagementException {
        validateIdentifier(gatewayId, "gatewayId");
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        service.deleteGateway(organization, gatewayId);
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.PLATFORM_GATEWAY, "{'id':'" + gatewayId + "'}",
                APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
        return Response.ok().build();
    }

    @Override
    public Response regeneratePlatformGatewayToken(String gatewayId, MessageContext messageContext)
            throws APIManagementException {
        validateIdentifier(gatewayId, "gatewayId");
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        PlatformGatewayService service =
                ServiceReferenceHolder.getInstance().getPlatformGatewayService();
        PlatformGatewayRegistrationResult result =
                service.regenerateGatewayToken(organization, gatewayId);
        PlatformGateway gateway = result.getGateway();

        // Fetch permissions from environment
        GatewayVisibilityPermissionConfigurationDTO permissions = null;
        APIAdmin apiAdmin = new APIAdminImpl();
        Environment env = apiAdmin.getEnvironment(organization, gateway.getId());
        if (env != null) {
            permissions = env.getPermissions();
        }

        return Response.ok().entity(toDTOWithToken(gateway, result.getRegistrationToken(), permissions)).build();
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
            throw RestApiUtil.buildBadRequestException(
                    "name must contain only lowercase letters, numbers and hyphens (pattern: ^[a-z0-9-]+$)");
        }
        if (StringUtils.isBlank(body.getDisplayName())) {
            throw RestApiUtil.buildBadRequestException("displayName is required");
        }
        if (body.getDisplayName().length() > 128) {
            throw RestApiUtil.buildBadRequestException("displayName must be at most 128 characters");
        }
        if (body.getVhost() == null || StringUtils.isBlank(body.getVhost().toString())) {
            throw RestApiUtil.buildBadRequestException("vhost is required");
        }
        String vhostStr = body.getVhost().toString();
        ParsedGatewayUrl parsed = parseGatewayUrl(vhostStr);
        if (parsed == null) {
            if (log.isWarnEnabled()) {
                log.warn("vhost validation failed for value: [" + vhostStr + "] (length=" + (vhostStr != null ? vhostStr.length() : 0) + ")");
            }
            throw RestApiUtil.buildBadRequestException("vhost must be a valid URL (e.g. https://host:port); got: " + (vhostStr != null && vhostStr.length() <= 100 ? vhostStr : "<invalid or too long>"));
        }
        if (parsed.host.length() > 255) {
            throw RestApiUtil.buildBadRequestException("vhost host must be at most 255 characters");
        }
        if (StringUtils.isNotBlank(body.getDescription()) && body.getDescription().length() > 1023) {
            throw RestApiUtil.buildBadRequestException("description must be at most 1023 characters");
        }
    }

    private void validateIdentifier(String identifier, String fieldName) throws APIManagementException {

        if (StringUtils.isBlank(identifier)) {
            throw RestApiUtil.buildBadRequestException(fieldName + " is required");
        }
    }

    private void validateUpdateBody(UpdatePlatformGatewayRequestDTO body) throws APIManagementException {
        if (StringUtils.isBlank(body.getName())) {
            throw RestApiUtil.buildBadRequestException("name is required (PUT full representation)");
        }
        if (body.getVhost() == null || StringUtils.isBlank(body.getVhost().toString())) {
            throw RestApiUtil.buildBadRequestException("vhost is required (PUT full representation)");
        }
        String vhostStr = body.getVhost().toString();
        if (parseGatewayUrl(vhostStr) == null) {
            if (log.isWarnEnabled()) {
                log.warn("vhost validation failed on update for value: [" + vhostStr + "]");
            }
            throw RestApiUtil.buildBadRequestException("vhost must be a valid URL (e.g. https://host:port); got: " + (vhostStr != null && vhostStr.length() <= 100 ? vhostStr : "<invalid or too long>"));
        }
        if (StringUtils.isBlank(body.getDisplayName())) {
            throw RestApiUtil.buildBadRequestException("displayName is required");
        }
        if (body.getDisplayName() != null && body.getDisplayName().length() > 128) {
            throw RestApiUtil.buildBadRequestException("displayName must be at most 128 characters");
        }
        if (StringUtils.isNotBlank(body.getDescription()) && body.getDescription().length() > 1023) {
            throw RestApiUtil.buildBadRequestException("description must be at most 1023 characters");
        }
    }

    /**
     * Update the gateway environment after PUT: permissions and/or displayName/description so GET /environments reflects them.
     */
    private void updateEnvironmentForPlatformGatewayPut(String organization, PlatformGateway gateway,
            UpdatePlatformGatewayRequestPermissionsDTO requestPermissions, String newDisplayName, String newDescription)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        Environment existingEnvironment = apiAdmin.getEnvironment(organization, gateway.getId());
        if (existingEnvironment == null) {
            log.warn("Environment not found for platform gateway ID: " + gateway.getId() + ", skipping environment update");
            return;
        }
        if (newDisplayName != null) {
            existingEnvironment.setDisplayName(newDisplayName);
        }
        if (newDescription != null) {
            existingEnvironment.setDescription(newDescription);
        }
        if (requestPermissions != null) {
            GatewayVisibilityPermissionConfigurationDTO visibility = new GatewayVisibilityPermissionConfigurationDTO();
            if (requestPermissions.getPermissionType() != null) {
                switch (requestPermissions.getPermissionType()) {
                    case ALLOW:
                        visibility.setPermissionType("ALLOW");
                        break;
                    case DENY:
                        visibility.setPermissionType("DENY");
                        break;
                    default:
                        visibility.setPermissionType("PUBLIC");
                        break;
                }
            } else {
                visibility.setPermissionType("PUBLIC");
            }
            if (requestPermissions.getRoles() != null) {
                visibility.setRoles(new ArrayList<>(requestPermissions.getRoles()));
            }
            existingEnvironment.setPermissions(visibility);
        }
        apiAdmin.updateEnvironment(organization, existingEnvironment);
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
    private PlatformGatewayResponseDTO toDTO(PlatformGateway g, GatewayVisibilityPermissionConfigurationDTO permissions) {

        PlatformGatewayResponseDTO dto = new PlatformGatewayResponseDTO();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setDisplayName(g.getDisplayName());
        dto.setDescription(g.getDescription());
        dto.setProperties(deserializeProperties(g.getProperties()));
        String url = toGatewayUrl(g.getVhost());
        dto.setVhost(url != null ? URI.create(url) : null);
        dto.setIsActive(g.isActive());
        dto.setPermissions(mapPermissionsToDTO(permissions));
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        return dto;
    }

    private GatewayResponseWithTokenDTO toDTOWithToken(PlatformGateway g, String registrationToken,
            GatewayVisibilityPermissionConfigurationDTO permissions) {

        GatewayResponseWithTokenDTO dto = new GatewayResponseWithTokenDTO();
        dto.setId(g.getId());
        dto.setName(g.getName());
        dto.setDisplayName(g.getDisplayName());
        dto.setDescription(g.getDescription());
        dto.setProperties(deserializeProperties(g.getProperties()));
        String urlWithToken = toGatewayUrl(g.getVhost());
        dto.setVhost(urlWithToken != null ? URI.create(urlWithToken) : null);
        dto.setIsActive(g.isActive());
        dto.setPermissions(mapPermissionsToDTO(permissions));
        dto.setCreatedAt(g.getCreatedAt());
        dto.setUpdatedAt(g.getUpdatedAt());
        dto.setRegistrationToken(registrationToken);
        return dto;
    }

    /**
     * Convert internal permissions model to REST API DTO.
     */
    private PlatformGatewayResponsePermissionsDTO mapPermissionsToDTO(
            GatewayVisibilityPermissionConfigurationDTO permissions) {

        PlatformGatewayResponsePermissionsDTO dto = new PlatformGatewayResponsePermissionsDTO();
        if (permissions == null) {
            dto.setPermissionType(PlatformGatewayResponsePermissionsDTO.PermissionTypeEnum.PUBLIC);
            return dto;
        }
        String permType = permissions.getPermissionType();
        if ("ALLOW".equalsIgnoreCase(permType)) {
            dto.setPermissionType(PlatformGatewayResponsePermissionsDTO.PermissionTypeEnum.ALLOW);
        } else if ("DENY".equalsIgnoreCase(permType)) {
            dto.setPermissionType(PlatformGatewayResponsePermissionsDTO.PermissionTypeEnum.DENY);
        } else {
            dto.setPermissionType(PlatformGatewayResponsePermissionsDTO.PermissionTypeEnum.PUBLIC);
        }
        if (permissions.getRoles() != null) {
            dto.setRoles(new ArrayList<>(permissions.getRoles()));
        }
        return dto;
    }

}
