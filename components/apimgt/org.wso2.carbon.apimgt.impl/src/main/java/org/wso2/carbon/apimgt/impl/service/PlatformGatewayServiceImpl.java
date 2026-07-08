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
import org.wso2.carbon.apimgt.impl.dto.ConnectGatewayConfig;
import org.wso2.carbon.apimgt.impl.dto.PlatformGatewayConnectConfig;
import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayDeploymentDispatcher;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;

import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Implementation of platform gateway service. Delegates persistence to {@link PlatformGatewayDAO}.
 */
public class PlatformGatewayServiceImpl implements PlatformGatewayService {

    private static final Log log = LogFactory.getLog(PlatformGatewayServiceImpl.class);
    private static final PlatformGatewayServiceImpl INSTANCE = new PlatformGatewayServiceImpl();
    private static final ConcurrentHashMap<String, Object> CONNECT_TOKEN_BOOTSTRAP_LOCKS = new ConcurrentHashMap<>();
    private static final String CONNECT_GATEWAY_ID_NAMESPACE = "platform-gateway-connect:";

    public static PlatformGatewayServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayServiceImpl() {
    }

    @Override
    public PlatformGatewayRegistrationResult createGateway(String organizationId, String name, String displayName,
                                                     String description, String vhost, String propertiesJson)
            throws APIManagementException {
        APIAdminImpl apiAdmin = new APIAdminImpl();
        boolean nameExists = apiAdmin.getAllEnvironments(organizationId).stream()
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
        Environment env = new APIAdminImpl().getAllEnvironments(organizationId).stream()
                .filter(e -> APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(e.getGatewayType())
                        && name.equals(e.getName()))
                .findFirst()
                .orElse(null);
        return env != null ? envToApiModel(env) : null;
    }

    @Override
    public List<PlatformGateway> listGatewaysByOrganization(String organizationId) throws APIManagementException {
        return new APIAdminImpl().getAllEnvironments(organizationId).stream()
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
        if (!organizationId.equals(existing.getOrganizationId())) {
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
        ResolvedPlatformGatewayEnvironment resolved =
                resolvePlatformGatewayEnvironment(organizationId, gatewayId);
        if (resolved == null) {
            throw new APIManagementException("Platform gateway not found: " + gatewayId,
                    ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND);
        }
        Environment env = resolved.environment;
        String storageOrgId = resolved.storageOrganizationId;
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
        ResolvedPlatformGatewayEnvironment resolved =
                resolvePlatformGatewayEnvironment(organizationId, gatewayId);
        if (resolved == null) {
            throw new APIManagementException("Platform gateway not found: " + gatewayId,
                    ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND);
        }
        Environment env = resolved.environment;
        String storageOrgId = resolved.storageOrganizationId;
        if (displayName != null) {
            env.setDisplayName(displayName);
        }
        if (description != null) {
            env.setDescription(description);
        }
        if (displayName != null || description != null || propertiesJson != null) {
            Map<String, String> additional = env.getAdditionalProperties() != null
                    ? new HashMap<>(env.getAdditionalProperties()) : new HashMap<>();
            if (propertiesJson != null) {
                additional.put("properties", propertiesJson);
            }
            additional.put("updatedAt", String.valueOf(System.currentTimeMillis()));
            env.setAdditionalProperties(additional);
        }
        new APIAdminImpl().updateEnvironment(storageOrgId, env);
        return envToApiModel(env);
    }

    @Override
    public void updateGatewayActiveStatus(String gatewayId, String organizationId, boolean active)
            throws APIManagementException {
        ResolvedPlatformGatewayEnvironment resolved =
                resolvePlatformGatewayEnvironment(organizationId, gatewayId);
        if (resolved == null) {
            return;
        }
        Environment env = resolved.environment;
        String storageOrgId = resolved.storageOrganizationId;
        Map<String, String> additional = env.getAdditionalProperties() != null
                ? new HashMap<>(env.getAdditionalProperties()) : new HashMap<>();
        additional.put("isActive", String.valueOf(active));
        additional.put("updatedAt", String.valueOf(System.currentTimeMillis()));
        env.setAdditionalProperties(additional);
        new APIAdminImpl().updateEnvironment(storageOrgId, env);
    }

    /**
     * Resolved platform gateway environment and the organization key under which it is stored.
     */
    public static final class ResolvedPlatformGatewayEnvironment {
        public final String storageOrganizationId;
        public final Environment environment;

        ResolvedPlatformGatewayEnvironment(String storageOrganizationId, Environment environment) {
            this.storageOrganizationId = storageOrganizationId;
            this.environment = environment;
        }
    }

    /**
     * Resolves a platform gateway environment with a single DB read under {@code requestOrganizationId}.
     */
    public static ResolvedPlatformGatewayEnvironment resolvePlatformGatewayEnvironment(
            String requestOrganizationId, String gatewayId) throws APIManagementException {
        if (StringUtils.isBlank(gatewayId) || StringUtils.isBlank(requestOrganizationId)) {
            return null;
        }
        Environment env = ApiMgtDAO.getInstance().getEnvironment(requestOrganizationId, gatewayId);
        if (env != null && APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
            return new ResolvedPlatformGatewayEnvironment(requestOrganizationId, env);
        }
        return null;
    }

    /**
     * Resolves the organization under which a platform gateway environment is stored.
     * Platform gateways are single-tenant scoped; lookup is limited to {@code requestOrganizationId}.
     */
    public static String resolveStorageOrganizationId(String requestOrganizationId, String gatewayId)
            throws APIManagementException {
        ResolvedPlatformGatewayEnvironment resolved =
                resolvePlatformGatewayEnvironment(requestOrganizationId, gatewayId);
        return resolved != null ? resolved.storageOrganizationId : null;
    }

    /**
     * Reconstructs the gateway base URL from a {@link VHost} populated by {@link #toEnvironmentFromUrl(String, String,
     * String, String, String)}. Uses HTTP when a non-default HTTP port is set and HTTPS port is still the default.
     */
    public static String buildGatewayBaseUrlFromVHost(VHost v) {
        if (v == null || StringUtils.isBlank(v.getHost())) {
            return "";
        }
        int httpPort = v.getHttpPort();
        int httpsPort = v.getHttpsPort();
        if (httpPort > 0 && httpPort != VHost.DEFAULT_HTTP_PORT
                && (httpsPort <= 0 || httpsPort == VHost.DEFAULT_HTTPS_PORT)) {
            return v.getHttpUrl();
        }
        return v.getHttpsUrl();
    }

    /**
     * Resolves the gateway base URL for API/admin responses from environment storage.
     */
    public static String resolveGatewayBaseUrl(Environment env) {
        if (env == null) {
            return "";
        }
        Map<String, String> additional = env.getAdditionalProperties();
        if (additional != null) {
            String stored = additional.get(APIConstants.GatewayNotification.GATEWAY_BASE_URL);
            if (StringUtils.isNotBlank(stored)) {
                return stored.trim();
            }
        }
        if (env.getVhosts() != null && !env.getVhosts().isEmpty()) {
            return buildGatewayBaseUrlFromVHost(env.getVhosts().get(0));
        }
        return "";
    }

    /**
     * Gateway base URLs for Dev Portal try-out / endpoint listing on a platform gateway environment.
     * Uses the configured connect/admin base URL so an {@code http://host:port} entry is not rewritten as
     * {@code https://host:443} via default {@link VHost#getHttpsUrl()}.
     */
    public static Map<String, String> resolveInvocationUrlsForTransports(Environment env, String transports) {
        if (StringUtils.isBlank(transports)) {
            return resolveInvocationUrlsForTransports(env, Collections.emptyList());
        }
        return resolveInvocationUrlsForTransports(env,
                Arrays.stream(transports.split(","))
                        .map(String::trim)
                        .filter(StringUtils::isNotEmpty)
                        .collect(Collectors.toList()));
    }

    public static Map<String, String> resolveInvocationUrlsForTransports(Environment env,
                                                                          Collection<String> transports) {
        Map<String, String> urls = new java.util.LinkedHashMap<>();
        if (env == null || !APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(env.getGatewayType())) {
            return urls;
        }
        String baseUrl = resolveGatewayBaseUrl(env);
        if (StringUtils.isBlank(baseUrl)) {
            return urls;
        }
        boolean includesHttp = transports != null && transports.stream()
                .anyMatch(t -> APIConstants.HTTP_PROTOCOL.equalsIgnoreCase(StringUtils.trimToEmpty(t)));
        boolean includesHttps = transports != null && transports.stream()
                .anyMatch(t -> APIConstants.HTTPS_PROTOCOL.equalsIgnoreCase(StringUtils.trimToEmpty(t)));
        if (baseUrl.startsWith(APIConstants.HTTP_PROTOCOL_URL_PREFIX)) {
            // HTTP-configured gateway: only expose under HTTP. OAS try-out prepends https:// for the HTTPS key,
            // so putting an http:// base URL there produces malformed URLs like https://http//host:port.
            if (includesHttp || includesHttps) {
                urls.put(APIConstants.HTTP_PROTOCOL, baseUrl);
            }
        } else if (baseUrl.startsWith(APIConstants.HTTPS_PROTOCOL_URL_PREFIX)) {
            if (includesHttps) {
                urls.put(APIConstants.HTTPS_PROTOCOL, baseUrl);
            }
            if (includesHttp) {
                urls.put(APIConstants.HTTP_PROTOCOL, baseUrl);
            }
        }
        return urls;
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
        String vhost = resolveGatewayBaseUrl(env);
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
                                                                ConnectGatewayConfig entry) {
        if (config == null || entry == null || StringUtils.isBlank(entry.getRegistrationToken())) {
            return false;
        }
        String orgId = entry.resolveOrganization();
        String registrationToken = entry.getRegistrationToken();
        String name = StringUtils.isNotBlank(entry.getName()) ? entry.getName() : null;
        String displayNameOverride = entry.getDisplayName();
        String descriptionOverride = entry.getDescription();
        String urlOverride = entry.getUrl();
        if (StringUtils.isBlank(registrationToken)) {
            return false;
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
        String persistedGatewayId = resolveConnectGatewayId(tokenId);
        if (StringUtils.isBlank(persistedGatewayId)) {
            return false;
        }
        if (name == null) {
            name = persistedGatewayId;
        }
        Object bootstrapLock = CONNECT_TOKEN_BOOTSTRAP_LOCKS.computeIfAbsent(tokenId, id -> new Object());
        synchronized (bootstrapLock) {
            try {
                PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
                PlatformGatewayDAO.TokenWithGateway activeToken = dao.getActiveTokenById(tokenId);
                if (activeToken != null) {
                    if (!PlatformGatewayTokenUtil.matchesActiveTokenHash(activeToken, plainToken)) {
                        if (log.isWarnEnabled()) {
                            log.warn("Connect with token: configured token hash mismatch for token_id=" + tokenId);
                        }
                        return false;
                    }
                    Environment bootstrappedEnv = null;
                    try {
                        bootstrappedEnv = ApiMgtDAO.getInstance().getEnvironmentByUuid(persistedGatewayId);
                    } catch (APIManagementException e) {
                        // ignore
                    }
                    if (bootstrappedEnv != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Connect with token: gateway already exists for token_id=" + tokenId);
                        }
                        return true;
                    }
                    if (log.isWarnEnabled()) {
                        log.warn("Connect with token: active token row exists but environment missing for gateway_id="
                                + persistedGatewayId + "; recreating environment");
                    }
                    String displayName = StringUtils.isNotBlank(displayNameOverride) ? displayNameOverride : name;
                    String description = StringUtils.isNotBlank(descriptionOverride) ? descriptionOverride : "";
                    Timestamp now = Timestamp.from(Instant.now());
                    APIAdminImpl apiAdmin = new APIAdminImpl();
                    Environment env = StringUtils.isNotBlank(urlOverride)
                            ? toEnvironmentFromUrl(persistedGatewayId, name, displayName, description, urlOverride)
                            : toEnvironment(persistedGatewayId, name, displayName, description, "default");
                    Map<String, String> additional = new HashMap<>();
                    additional.put("organization", orgId);
                    additional.put("isActive", "false");
                    additional.put("createdAt", String.valueOf(now.getTime()));
                    additional.put("updatedAt", String.valueOf(now.getTime()));
                    if (StringUtils.isNotBlank(urlOverride)) {
                        additional.put(APIConstants.GatewayNotification.GATEWAY_BASE_URL, urlOverride.trim());
                    }
                    env.setAdditionalProperties(additional);
                    apiAdmin.addEnvironment(orgId, env);
                    return true;
                }
                if (StringUtils.isNotBlank(entry.getName())) {
                    try {
                        APIAdminImpl apiAdminForNameCheck = new APIAdminImpl();
                        Environment existingByName = apiAdminForNameCheck.getAllEnvironments(orgId).stream()
                                .filter(e -> APIConstants.WSO2_API_PLATFORM_GATEWAY.equals(e.getGatewayType())
                                        && entry.getName().equals(e.getName()))
                                .findFirst()
                                .orElse(null);
                        if (existingByName != null && !persistedGatewayId.equals(existingByName.getUuid())) {
                            if (log.isDebugEnabled()) {
                                log.debug("Connect with token: name '" + entry.getName()
                                        + "' already exists; using gateway_id as name");
                            }
                            name = persistedGatewayId;
                        }
                    } catch (APIManagementException e) {
                        // ignore, proceed with requested name
                    }
                }
                String tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken);
                APIAdminImpl apiAdmin = new APIAdminImpl();
                Environment existing = null;
                try {
                    existing = ApiMgtDAO.getInstance().getEnvironmentByUuid(persistedGatewayId);
                } catch (APIManagementException e) {
                    // ignore
                }
                String displayName = StringUtils.isNotBlank(displayNameOverride) ? displayNameOverride : name;
                String description = StringUtils.isNotBlank(descriptionOverride) ? descriptionOverride : "";
                String vhostForDao = StringUtils.isNotBlank(urlOverride) ? urlOverride : "default";
                Timestamp now = Timestamp.from(Instant.now());
                boolean environmentCreated = false;
                if (existing == null) {
                    Environment env = StringUtils.isNotBlank(urlOverride)
                            ? toEnvironmentFromUrl(persistedGatewayId, name, displayName, description, urlOverride)
                            : toEnvironment(persistedGatewayId, name, displayName, description, "default");
                    Map<String, String> additional = new HashMap<>();
                    additional.put("organization", orgId);
                    additional.put("isActive", "false");
                    additional.put("createdAt", String.valueOf(now.getTime()));
                    additional.put("updatedAt", String.valueOf(now.getTime()));
                    if (StringUtils.isNotBlank(urlOverride)) {
                        additional.put(APIConstants.GatewayNotification.GATEWAY_BASE_URL, urlOverride.trim());
                    }
                    env.setAdditionalProperties(additional);
                    apiAdmin.addEnvironment(orgId, env);
                    environmentCreated = true;
                }
                try {
                    PlatformGatewayDAO.PlatformGateway gateway = new PlatformGatewayDAO.PlatformGateway(
                            persistedGatewayId, orgId, name, displayName, description, vhostForDao,
                            null, false, now, now);
                    dao.createGatewayWithTokenAndGatewayInstance(gateway, tokenId, tokenHash,
                            Collections.singletonList(name));
                } catch (APIManagementException e) {
                    if (environmentCreated) {
                        try {
                            apiAdmin.deleteEnvironment(orgId, persistedGatewayId);
                        } catch (Exception rollbackEx) {
                            log.warn("Rollback failed for connect-with-token environment cleanup: "
                                    + persistedGatewayId, rollbackEx);
                        }
                    }
                    activeToken = dao.getActiveTokenById(tokenId);
                    try {
                        if (activeToken != null
                                && PlatformGatewayTokenUtil.matchesActiveTokenHash(activeToken, plainToken)) {
                            return true;
                        }
                    } catch (NoSuchAlgorithmException hashEx) {
                        log.warn("Connect-with-token race recovery: hash verification failed for token_id="
                                + tokenId, hashEx);
                        return false;
                    }
                    throw e;
                }
                if (log.isInfoEnabled()) {
                    log.info("Platform gateway connected with token: gateway_id=" + persistedGatewayId + ", name="
                            + name + ", organization=" + orgId);
                }
                return true;
            } catch (NoSuchAlgorithmException | APIManagementException e) {
                if (log.isWarnEnabled()) {
                    log.warn("Connect with token failed for gateway_id=" + persistedGatewayId + ": "
                            + e.getMessage(), e);
                }
                return false;
            }
        }
    }

    /**
     * Deterministic platform gateway environment UUID for a connect-with-token bootstrap token row ID.
     */
    public static String resolveConnectGatewayId(String tokenId) {
        if (StringUtils.isBlank(tokenId)) {
            return null;
        }
        return UUID.nameUUIDFromBytes(
                (CONNECT_GATEWAY_ID_NAMESPACE + tokenId.trim()).getBytes(StandardCharsets.UTF_8)).toString();
    }
}
