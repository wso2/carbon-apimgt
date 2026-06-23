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

package org.wso2.carbon.apimgt.impl.service;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.PlatformGatewayRegistrationResult;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayMode;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.api.dto.GatewayVisibilityPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayDeploymentDispatcher;
import org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig;
import org.wso2.carbon.apimgt.impl.dto.PlatformGatewayConnectConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of platform gateway service. Delegates persistence to {@link PlatformGatewayDAO}.
 */
public class PlatformGatewayServiceImpl implements PlatformGatewayService {

    private static final Log log = LogFactory.getLog(PlatformGatewayServiceImpl.class);
    private static final PlatformGatewayServiceImpl INSTANCE = new PlatformGatewayServiceImpl();

    public static PlatformGatewayServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayServiceImpl() {
    }

    /**
     * Gateway environments for an organization plus any stored under the global-tenant scope
     * ({@link APIConstants.GatewayNotification#WSO2_ALL_TENANTS}), so globally-scoped platform gateways are not
     * omitted from list/name checks. When {@code organizationId} is already the global scope, only that list is used.
     */
    private static List<Environment> getEnvironmentsForOrganizationMergedWithGlobal(String organizationId)
            throws APIManagementException {
        return getEnvironmentsForOrganizationMergedWithGlobal(new APIAdminImpl(), organizationId);
    }

    private static List<Environment> getEnvironmentsForOrganizationMergedWithGlobal(APIAdminImpl apiAdmin,
                                                                               String organizationId)
            throws APIManagementException {
        List<Environment> merged = new ArrayList<>(apiAdmin.getAllEnvironments(organizationId));
        if (!APIConstants.GatewayNotification.WSO2_ALL_TENANTS.equals(organizationId)) {
            merged.addAll(apiAdmin.getAllEnvironments(APIConstants.GatewayNotification.WSO2_ALL_TENANTS));
        }
        return merged;
    }

    @Override
    public PlatformGatewayRegistrationResult createGateway(String organizationId, String name, String displayName,
                                                     String description, String vhost, String propertiesJson)
            throws APIManagementException {
        APIAdminImpl apiAdmin = new APIAdminImpl();
        boolean nameExists = getEnvironmentsForOrganizationMergedWithGlobal(apiAdmin, organizationId).stream()
                .anyMatch(e -> APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(e.getGatewayType())
                        && name.equals(e.getName()));
        if (nameExists) {
            throw new APIManagementException(
                    String.format("A platform gateway with name '%s' already exists in the organization", name),
                    ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS);
        }

        String gatewayId = UUID.randomUUID().toString();
        Timestamp now = Timestamp.from(Instant.now());
        Environment env = toEnvironment(gatewayId, name, displayName, description, vhost);
        Map<String, String> additional = new HashMap<>();
        additional.put("organization", organizationId);
        additional.put("isActive", "false");
        additional.put("createdAt", String.valueOf(now.getTime()));
        additional.put("updatedAt", String.valueOf(now.getTime()));
        if (StringUtils.isNotBlank(propertiesJson)) {
            additional.put("properties", propertiesJson);
        }
        env.setAdditionalProperties(additional);

        apiAdmin.addEnvironment(organizationId, env);
        try {
            String tokenId = PlatformGatewayTokenUtil.generateTokenId();
            String plainToken = PlatformGatewayTokenUtil.generateToken();
            String tokenHash;
            try {
                tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken);
            } catch (NoSuchAlgorithmException e) {
                throw new APIManagementException("Error hashing gateway token", e);
            }
            PlatformGatewayDAO.PlatformGateway gateway = new PlatformGatewayDAO.PlatformGateway(
                    gatewayId, organizationId, name, displayName, description, vhost,
                    propertiesJson, false, now, now);
            PlatformGatewayDAO.getInstance().createGatewayWithTokenAndGatewayInstance(gateway, tokenId, tokenHash,
                    Collections.singletonList(name));

            String registrationToken = tokenId + PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR + plainToken;
            return new PlatformGatewayRegistrationResult(envToApiModel(env), registrationToken);
        } catch (APIManagementException e) {
            try {
                apiAdmin.deleteEnvironment(organizationId, gatewayId);
            } catch (Exception rollbackEx) {
                log.warn("Rollback: failed to delete orphan environment for gateway " + gatewayId
                        + " after DAO failure; manual cleanup may be required", rollbackEx);
            }
            throw e;
        }
    }

    /**
     * Build an Environment for AM_GATEWAY_ENVIRONMENT (same shape as addPlatformGatewaysToEnvironmentsMap).
     * Caller must set UUID on the result when using for addEnvironment so the gateway id is stored as env UUID.
     */
    private static Environment toEnvironment(String gatewayId, String name, String displayName,
                                              String description, String vhost) {
        Environment env = new Environment();
        env.setUuid(gatewayId);
        env.setName(name);
        env.setDisplayName(StringUtils.isNotBlank(displayName) ? displayName : name);
        env.setDescription(description);
        env.setType(APIConstants.GATEWAY_ENV_TYPE_HYBRID);
        env.setProvider(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        env.setMode(GatewayMode.WRITE_ONLY.getMode());
        String vhostStr = StringUtils.isNotBlank(vhost) ? vhost : "default";
        String vhostHost = vhostStr;
        int httpsPort = 8443;
        if (vhostStr.contains(":")) {
            String[] parts = vhostStr.split(":", 2);
            vhostHost = parts[0];
            if (parts.length > 1 && StringUtils.isNumeric(parts[1])) {
                httpsPort = Integer.parseInt(parts[1]);
            }
        }
        VHost vhostObj = new VHost();
        vhostObj.setHost(vhostHost);
        vhostObj.setWsHost(vhostHost);
        vhostObj.setHttpPort(VHost.DEFAULT_HTTP_PORT);
        vhostObj.setHttpsPort(httpsPort);
        env.setVhosts(Collections.singletonList(vhostObj));
        setDefaultVisibilityPublic(env);
        return env;
    }

    /** Default visibility to PUBLIC (user can change later from Admin UI: Public, By roles, etc.). */
    private static void setDefaultVisibilityPublic(Environment env) {
        env.setVisibility(APIConstants.PERMISSION_NOT_RESTRICTED);
        GatewayVisibilityPermissionConfigurationDTO perms = new GatewayVisibilityPermissionConfigurationDTO();
        perms.setPermissionType(APIConstants.PERMISSION_NOT_RESTRICTED);
        env.setPermissions(perms);
    }

    /**
     * Build an Environment from a base URL (http or https). URL must already be validated via
     * {@link ConnectGatewayConfig#setUrl(String)}.
     */
    private static Environment toEnvironmentFromUrl(String gatewayId, String name, String displayName,
                                                    String description, String url) {
        URI u = URI.create(url);
        String host = u.getHost();
        int port = u.getPort() != -1 ? u.getPort()
                : ("https".equalsIgnoreCase(u.getScheme()) ? VHost.DEFAULT_HTTPS_PORT : VHost.DEFAULT_HTTP_PORT);
        boolean isHttps = "https".equalsIgnoreCase(u.getScheme());
        String httpContext = u.getPath() != null && !u.getPath().isEmpty() && !"/".equals(u.getPath())
                ? u.getPath() : "";

        VHost vhostObj = new VHost();
        vhostObj.setHost(host);
        vhostObj.setWsHost(host);
        vhostObj.setWssHost(host);
        vhostObj.setHttpContext(httpContext);
        if (isHttps) {
            vhostObj.setHttpsPort(port);
            vhostObj.setHttpPort(VHost.DEFAULT_HTTP_PORT);
        } else {
            vhostObj.setHttpPort(port);
            vhostObj.setHttpsPort(VHost.DEFAULT_HTTPS_PORT);
        }
        vhostObj.setWsPort(VHost.DEFAULT_WS_PORT);
        vhostObj.setWssPort(VHost.DEFAULT_WSS_PORT);

        Environment env = new Environment();
        env.setUuid(gatewayId);
        env.setName(name);
        env.setDisplayName(StringUtils.isNotBlank(displayName) ? displayName : name);
        env.setDescription(description);
        env.setType(APIConstants.GATEWAY_ENV_TYPE_HYBRID);
        env.setProvider(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        env.setGatewayType(APIConstants.WSO2_API_PLATFORM_GATEWAY);
        env.setMode(GatewayMode.WRITE_ONLY.getMode());
        env.setVhosts(Collections.singletonList(vhostObj));
        setDefaultVisibilityPublic(env);
        return env;
    }

    @Override
    public PlatformGateway getGatewayByNameAndOrganization(String name, String organizationId)
            throws APIManagementException {
        Environment env = getEnvironmentsForOrganizationMergedWithGlobal(organizationId).stream()
                .filter(e -> APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(e.getGatewayType())
                        && name.equals(e.getName()))
                .findFirst()
                .orElse(null);
        return env != null ? envToApiModel(env) : null;
    }

    @Override
    public List<PlatformGateway> listGatewaysByOrganization(String organizationId) throws APIManagementException {
        return getEnvironmentsForOrganizationMergedWithGlobal(organizationId).stream()
                .filter(e -> APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(e.getGatewayType()))
                .map(PlatformGatewayServiceImpl::envToApiModel)
                .collect(Collectors.toList());
    }

    @Override
    public List<PlatformGateway> listGatewaysByOrganizationWithInstance(String organizationId)
            throws APIManagementException {
        List<String> uuids = PlatformGatewayDAO.getInstance().getPlatformGatewayUuidsWithInstance(organizationId);
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        return uuids.stream()
                .map(uuid -> {
                    try {
                        return apiMgtDAO.getEnvironmentByUuid(uuid);
                    } catch (APIManagementException e) {
                        return null;
                    }
                })
                .filter(env -> env != null)
                .map(PlatformGatewayServiceImpl::envToApiModel)
                .collect(Collectors.toList());
    }

    @Override
    public PlatformGateway getGatewayById(String id) throws APIManagementException {
        Environment env = ApiMgtDAO.getInstance().getEnvironmentByUuid(id);
        if (env == null || !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
            return null;
        }
        return envToApiModel(env);
    }

    @Override
    public PlatformGatewayRegistrationResult regenerateGatewayToken(String organizationId, String gatewayId)
            throws APIManagementException {
        PlatformGateway existing = getGatewayById(gatewayId);
        if (existing == null) {
            throw new APIManagementException("Platform gateway not found: " + gatewayId,
                    ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND);
        }
        if (!organizationId.equals(existing.getOrganizationId())
                && !APIConstants.GatewayNotification.WSO2_ALL_TENANTS.equals(existing.getOrganizationId())) {
            throw new APIManagementException("Platform gateway not found in organization: " + gatewayId,
                    ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND);
        }

        String tokenId = PlatformGatewayTokenUtil.generateTokenId();
        String plainToken = PlatformGatewayTokenUtil.generateToken();
        String tokenHash;
        try {
            tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken);
        } catch (NoSuchAlgorithmException e) {
            throw new APIManagementException("Error hashing gateway token", e);
        }

        Timestamp now = Timestamp.from(Instant.now());
        PlatformGatewayDAO.getInstance().regenerateToken(gatewayId, tokenId, tokenHash, now);

        String registrationToken = tokenId + PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR + plainToken;
        return new PlatformGatewayRegistrationResult(existing, registrationToken);
    }

    @Override
    public void deleteGateway(String organizationId, String gatewayId) throws APIManagementException {
        String storageOrgId = resolveStorageOrganizationId(organizationId, gatewayId);
        Environment env = storageOrgId != null
                ? ApiMgtDAO.getInstance().getEnvironment(storageOrgId, gatewayId) : null;
        if (env == null || !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
            throw new APIManagementException("Platform gateway not found: " + gatewayId,
                    ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND);
        }
        if (ApiMgtDAO.getInstance().hasExistingAPIRevisions(gatewayId, storageOrgId)) {
            throw new APIManagementException(
                    "Cannot delete platform gateway: API revisions are currently deployed to it. "
                            + "Undeploy all APIs from this gateway first.",
                    ExceptionCodes.from(ExceptionCodes.GATEWAY_ENVIRONMENT_API_REVISIONS_EXIST,
                            String.format("UUID '%s'", gatewayId)));
        }
        PlatformGatewayDeploymentDispatcher dispatcher =
                ServiceReferenceHolder.getInstance().getPlatformGatewayDeploymentDispatcher();
        if (dispatcher != null) {
            dispatcher.closeGatewayConnection(gatewayId);
        }
        PlatformGatewayDAO.getInstance().deleteGatewayWithReferences(gatewayId, env.getName(), storageOrgId);
    }

    @Override
    public PlatformGateway updateGateway(String organizationId, String gatewayId, String displayName,
                                         String description, String propertiesJson)
            throws APIManagementException {
        String storageOrgId = resolveStorageOrganizationId(organizationId, gatewayId);
        Environment env = storageOrgId != null
                ? ApiMgtDAO.getInstance().getEnvironment(storageOrgId, gatewayId) : null;
        if (env == null || !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
            throw new APIManagementException("Platform gateway not found: " + gatewayId,
                    ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND);
        }
        if (displayName != null) {
            env.setDisplayName(displayName);
        }
        if (description != null) {
            env.setDescription(description);
        }
        if (propertiesJson != null) {
            Map<String, String> additional = env.getAdditionalProperties() != null
                    ? new HashMap<>(env.getAdditionalProperties()) : new HashMap<>();
            additional.put("properties", propertiesJson);
            additional.put("updatedAt", String.valueOf(System.currentTimeMillis()));
            env.setAdditionalProperties(additional);
        }
        new APIAdminImpl().updateEnvironment(storageOrgId, env);
        return envToApiModel(ApiMgtDAO.getInstance().getEnvironment(storageOrgId, gatewayId));
    }

    @Override
    public void updateGatewayActiveStatus(String gatewayId, String organizationId, boolean active)
            throws APIManagementException {
        String storageOrgId = resolveStorageOrganizationId(organizationId, gatewayId);
        Environment env = storageOrgId != null
                ? ApiMgtDAO.getInstance().getEnvironment(storageOrgId, gatewayId) : null;
        if (env == null || !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
            return;
        }
        Map<String, String> additional = env.getAdditionalProperties() != null
                ? new HashMap<>(env.getAdditionalProperties()) : new HashMap<>();
        additional.put("isActive", String.valueOf(active));
        additional.put("updatedAt", String.valueOf(System.currentTimeMillis()));
        env.setAdditionalProperties(additional);
        new APIAdminImpl().updateEnvironment(storageOrgId, env);
    }

    /**
     * Resolves the organization under which a platform gateway environment is stored.
     * Falls back to {@code WSO2-ALL-TENANTS} when the gateway was created as a shared connect gateway.
     */
    public static String resolveStorageOrganizationId(String requestOrganizationId, String gatewayId)
            throws APIManagementException {
        if (StringUtils.isBlank(gatewayId)) {
            return null;
        }
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        if (StringUtils.isNotBlank(requestOrganizationId)) {
            Environment env = apiMgtDAO.getEnvironment(requestOrganizationId, gatewayId);
            if (env != null && APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
                return requestOrganizationId;
            }
        }
        String allTenantsOrg = APIConstants.GatewayNotification.WSO2_ALL_TENANTS;
        if (!allTenantsOrg.equals(requestOrganizationId)) {
            Environment env = apiMgtDAO.getEnvironment(allTenantsOrg, gatewayId);
            if (env != null && APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
                return allTenantsOrg;
            }
        }
        return null;
    }

    /**
     * Map Environment (AM_GATEWAY_ENVIRONMENT with GATEWAY_TYPE=Platform) to API model.
     * Reads isActive, properties, createdAt, updatedAt from additionalProperties.
     */
    private static PlatformGateway envToApiModel(Environment env) {
        if (env == null) {
            return null;
        }
        Map<String, String> add = env.getAdditionalProperties();
        String vhost = "";
        if (env.getVhosts() != null && !env.getVhosts().isEmpty()) {
            org.wso2.carbon.apimgt.api.model.VHost v = env.getVhosts().get(0);
            vhost = v.getHost() != null ? v.getHost() : "";
            if (v.getHttpsPort() > 0) {
                vhost = vhost + ":" + v.getHttpsPort();
            }
        }
        boolean isActive = add != null && "true".equalsIgnoreCase(add.get("isActive"));
        String properties = add != null ? add.get("properties") : null;
        Date createdAt = null;
        Date updatedAt = null;
        if (add != null) {
            String ct = add.get("createdAt");
            if (ct != null) {
                try {
                    createdAt = new Date(Long.parseLong(ct));
                } catch (NumberFormatException ignored) { }
            }
            String ut = add.get("updatedAt");
            if (ut != null) {
                try {
                    updatedAt = new Date(Long.parseLong(ut));
                } catch (NumberFormatException ignored) { }
            }
        }
        PlatformGateway api = new PlatformGateway();
        api.setId(env.getUuid());
        api.setOrganizationId(env.getName() != null ? getOrganizationFromEnv(env) : null);
        api.setName(env.getName());
        api.setDisplayName(env.getDisplayName());
        api.setDescription(env.getDescription());
        api.setVhost(vhost);
        api.setProperties(properties);
        api.setActive(isActive);
        api.setCreatedAt(createdAt);
        api.setUpdatedAt(updatedAt);
        return api;
    }

    private static String getOrganizationFromEnv(Environment env) {
        if (env.getAdditionalProperties() != null && env.getAdditionalProperties().get("organization") != null) {
            return env.getAdditionalProperties().get("organization");
        }
        return null;
    }

    private static PlatformGateway toApiModel(PlatformGatewayDAO.PlatformGateway g) {
        return fromDAO(g);
    }

    /**
     * Convert DAO model to API model. Public so REST/admin layer can build env list from DAO when service is null.
     */
    public static PlatformGateway fromDAO(PlatformGatewayDAO.PlatformGateway g) {
        if (g == null) {
            return null;
        }
        PlatformGateway api = new PlatformGateway();
        api.setId(g.id);
        api.setOrganizationId(g.organizationId);
        api.setName(g.name);
        api.setDisplayName(g.displayName);
        api.setDescription(g.description);
        api.setVhost(g.vhost);
        api.setProperties(g.properties);
        api.setActive(g.isActive);
        api.setCreatedAt(g.createdAt != null ? new Date(g.createdAt.getTime()) : null);
        api.setUpdatedAt(g.updatedAt != null ? new Date(g.updatedAt.getTime()) : null);
        return api;
    }

    public static boolean ensurePlatformGatewayFromConnectToken(PlatformGatewayConnectConfig config,
                                                                String gatewayId, ConnectGatewayConfig entry) {
        if (config == null || entry == null || StringUtils.isBlank(entry.getRegistrationToken())) {
            return false;
        }
        String orgId = entry.resolveOrganization();
        String registrationToken = entry.getRegistrationToken();
        String name = StringUtils.isNotBlank(entry.getName()) ? entry.getName() : gatewayId;
        String displayNameOverride = entry.getDisplayName();
        String descriptionOverride = entry.getDescription();
        String urlOverride = entry.getUrl();
        if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(registrationToken)) {
            return false;
        }
        if (StringUtils.isNotBlank(entry.getName())) {
            try {
                Environment existingByName = getEnvironmentsForOrganizationMergedWithGlobal(orgId).stream()
                        .filter(e -> APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(e.getGatewayType())
                                && entry.getName().equals(e.getName()))
                        .findFirst()
                        .orElse(null);
                if (existingByName != null && !gatewayId.equals(existingByName.getUuid())) {
                    if (log.isDebugEnabled()) {
                        log.debug("Connect with token: name '" + entry.getName()
                                + "' already exists; using gateway_id as name");
                    }
                    name = gatewayId;
                }
            } catch (APIManagementException e) {
                // ignore, proceed with requested name
            }
        }
        if (!registrationToken.contains(PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR)) {
            if (log.isDebugEnabled()) {
                log.debug("Connect with token skipped: registration_token must be in format tokenId.plainToken");
            }
            return false;
        }
        int sep = registrationToken.indexOf(PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR);
        String tokenId = registrationToken.substring(0, sep);
        String plainToken = registrationToken.substring(sep + 1);
        if (StringUtils.isBlank(tokenId) || StringUtils.isBlank(plainToken)) {
            return false;
        }
        try {
            PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
            if (dao.getActiveTokenById(tokenId) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Connect with token: gateway already exists for token_id=" + tokenId);
                }
                return false;
            }
            String tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken);
            APIAdminImpl apiAdmin = new APIAdminImpl();
            Environment existing = null;
            try {
                existing = ApiMgtDAO.getInstance().getEnvironmentByUuid(gatewayId);
            } catch (APIManagementException e) {
                // ignore
            }
            String displayName = StringUtils.isNotBlank(displayNameOverride) ? displayNameOverride : name;
            String description = StringUtils.isNotBlank(descriptionOverride) ? descriptionOverride : "";
            String vhostForDao = StringUtils.isNotBlank(urlOverride) ? urlOverride : "default";
            Timestamp now = Timestamp.from(Instant.now());
            if (existing == null) {
                Environment env = StringUtils.isNotBlank(urlOverride)
                        ? toEnvironmentFromUrl(gatewayId, name, displayName, description, urlOverride)
                        : toEnvironment(gatewayId, name, displayName, description, "default");
                Map<String, String> additional = new HashMap<>();
                additional.put("organization", orgId);
                additional.put("isActive", "false");
                additional.put("createdAt", String.valueOf(now.getTime()));
                additional.put("updatedAt", String.valueOf(now.getTime()));
                env.setAdditionalProperties(additional);
                apiAdmin.addEnvironment(orgId, env);
            }
            PlatformGatewayDAO.PlatformGateway gateway = new PlatformGatewayDAO.PlatformGateway(
                    gatewayId, orgId, name, displayName, description, vhostForDao,
                    null, false, now, now);
            dao.createGatewayWithTokenAndGatewayInstance(gateway, tokenId, tokenHash,
                    Collections.singletonList(name));
            if (log.isInfoEnabled()) {
                log.info("Platform gateway connected with token: gateway_id=" + gatewayId + ", name=" + name
                        + ", organization=" + orgId);
            }
            return true;
        } catch (NoSuchAlgorithmException | APIManagementException e) {
            if (log.isWarnEnabled()) {
                log.warn("Connect with token failed for gateway_id=" + gatewayId + ": " + e.getMessage(), e);
            }
            return false;
        }
    }

    /**
     * Startup hook for connect config. Gateway records are created on first connect, not at startup.
     */
    public static void ensurePlatformGatewayFromConfigOnStartup(PlatformGatewayConnectConfig config) {
        // No startup creation; gateway is registered on first connect with registration_token.
    }
}
