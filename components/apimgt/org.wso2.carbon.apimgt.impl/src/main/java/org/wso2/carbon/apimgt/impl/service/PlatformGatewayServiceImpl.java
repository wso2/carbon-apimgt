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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PlatformGatewayService;
import org.wso2.carbon.apimgt.api.model.CreatePlatformGatewayResult;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayMode;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.PlatformGatewayDAO;
import org.wso2.carbon.apimgt.impl.utils.PlatformGatewayTokenUtil;

import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of platform gateway service. Delegates persistence to {@link PlatformGatewayDAO}.
 */
public class PlatformGatewayServiceImpl implements PlatformGatewayService {

    private static final PlatformGatewayServiceImpl INSTANCE = new PlatformGatewayServiceImpl();

    public static PlatformGatewayServiceImpl getInstance() {
        return INSTANCE;
    }

    private PlatformGatewayServiceImpl() {
    }

    @Override
    public CreatePlatformGatewayResult createGateway(String organizationId, String name, String displayName,
                                                     String description, String vhost, boolean isCritical,
                                                     String functionalityType, String propertiesJson)
            throws APIManagementException {
        PlatformGatewayDAO dao = PlatformGatewayDAO.getInstance();
        if (dao.getGatewayByNameAndOrganization(name, organizationId) != null) {
            throw new APIManagementException(
                    String.format("A platform gateway with name '%s' already exists in the organization", name),
                    org.wso2.carbon.apimgt.api.ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS);
        }

        String gatewayId = UUID.randomUUID().toString();
        String tokenId = PlatformGatewayTokenUtil.generateTokenId();
        String plainToken = PlatformGatewayTokenUtil.generateToken();
        String tokenHash;
        try {
            tokenHash = PlatformGatewayTokenUtil.hashToken(plainToken);
        } catch (NoSuchAlgorithmException e) {
            throw new APIManagementException("Error hashing gateway token", e);
        }

        Timestamp now = Timestamp.from(Instant.now());
        PlatformGatewayDAO.PlatformGateway gateway = new PlatformGatewayDAO.PlatformGateway(
                gatewayId,
                organizationId,
                name,
                displayName,
                description,
                vhost,
                isCritical,
                functionalityType,
                propertiesJson,
                false,
                now,
                now
        );

        dao.createGatewayWithTokenAndGatewayInstance(gateway, tokenId, tokenHash,
                Collections.singletonList(name));

        // Same approach as other gateways: store in AM_GATEWAY_ENVIRONMENT so lookup by UUID and deployment use one source.
        Environment env = toEnvironment(gatewayId, name, displayName, description, vhost);
        APIAdminImpl apiAdmin = new APIAdminImpl();
        apiAdmin.addEnvironment(organizationId, env);

        String registrationToken = tokenId + PlatformGatewayTokenUtil.COMBINED_TOKEN_SEPARATOR + plainToken;
        return new CreatePlatformGatewayResult(toApiModel(gateway), registrationToken);
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
        env.setProvider("wso2");
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
        return env;
    }

    @Override
    public PlatformGateway getGatewayByNameAndOrganization(String name, String organizationId)
            throws APIManagementException {
        PlatformGatewayDAO.PlatformGateway g = PlatformGatewayDAO.getInstance()
                .getGatewayByNameAndOrganization(name, organizationId);
        return g != null ? toApiModel(g) : null;
    }

    @Override
    public List<PlatformGateway> listGatewaysByOrganization(String organizationId) throws APIManagementException {
        List<PlatformGatewayDAO.PlatformGateway> list = PlatformGatewayDAO.getInstance()
                .listGatewaysByOrganization(organizationId);
        return list.stream().map(PlatformGatewayServiceImpl::toApiModel).collect(Collectors.toList());
    }

    @Override
    public List<PlatformGateway> listGatewaysByOrganizationWithInstance(String organizationId)
            throws APIManagementException {
        List<PlatformGatewayDAO.PlatformGateway> list = PlatformGatewayDAO.getInstance()
                .listGatewaysByOrganizationWithInstance(organizationId);
        return list.stream().map(PlatformGatewayServiceImpl::toApiModel).collect(Collectors.toList());
    }

    @Override
    public PlatformGateway getGatewayById(String id) throws APIManagementException {
        PlatformGatewayDAO.PlatformGateway g = PlatformGatewayDAO.getInstance().getGatewayById(id);
        return g != null ? toApiModel(g) : null;
    }

    private static PlatformGateway toApiModel(PlatformGatewayDAO.PlatformGateway g) {
        PlatformGateway api = new PlatformGateway();
        api.setId(g.id);
        api.setOrganizationId(g.organizationId);
        api.setName(g.name);
        api.setDisplayName(g.displayName);
        api.setDescription(g.description);
        api.setVhost(g.vhost);
        api.setCritical(g.isCritical);
        api.setFunctionalityType(g.functionalityType);
        api.setProperties(g.properties);
        api.setActive(g.isActive);
        api.setCreatedAt(g.createdAt != null ? new java.util.Date(g.createdAt.getTime()) : null);
        api.setUpdatedAt(g.updatedAt != null ? new java.util.Date(g.updatedAt.getTime()) : null);
        return api;
    }
}
