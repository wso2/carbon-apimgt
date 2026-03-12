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

package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.model.CreatePlatformGatewayResult;
import org.wso2.carbon.apimgt.api.model.PlatformGateway;

import java.util.List;

/**
 * Service for platform gateway registration (create, list, get).
 * Follows the pattern: REST layer calls service, service calls DAO.
 */
public interface PlatformGatewayService {

    /**
     * Create a platform gateway and its one-time registration token in a single transaction.
     *
     * @param organizationId organization id
     * @param name            gateway name (unique per org)
     * @param displayName     display name
     * @param description     optional description
     * @param vhost           vhost
     * @param propertiesJson  optional JSON string for custom properties
     * @return created gateway and registration token (returned only once)
     * @throws APIManagementException if validation fails or name already exists (use ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS for 409)
     */
    CreatePlatformGatewayResult createGateway(String organizationId, String name, String displayName,
                                              String description, String vhost, String propertiesJson)
            throws APIManagementException;

    /**
     * Get a platform gateway by name and organization.
     *
     * @param name           gateway name
     * @param organizationId organization id
     * @return the gateway, or null if not found
     */
    PlatformGateway getGatewayByNameAndOrganization(String name, String organizationId)
            throws APIManagementException;

    /**
     * List platform gateways for an organization.
     *
     * @param organizationId organization id
     * @return list of gateways (never null)
     */
    List<PlatformGateway> listGatewaysByOrganization(String organizationId) throws APIManagementException;

    /**
     * List platform gateways that have a row in AM_GW_INSTANCES (for GET /environments; same source as deployment acks).
     * Default implementation delegates to {@link #listGatewaysByOrganization(String)} for backward compatibility
     * so external implementers are not forced to override; concrete implementations may override to return
     * only gateways that have an instance row.
     *
     * @param organizationId organization id
     * @return list of gateways (never null)
     */
    default List<PlatformGateway> listGatewaysByOrganizationWithInstance(String organizationId)
            throws APIManagementException {
        return listGatewaysByOrganization(organizationId);
    }

    /**
     * Get a platform gateway by id.
     *
     * @param id gateway id
     * @return the gateway, or null if not found
     */
    PlatformGateway getGatewayById(String id) throws APIManagementException;

    /**
     * Regenerate the registration token for a platform gateway.
     * Revokes all existing active tokens and creates a new one.
     *
     * @param organizationId organization id
     * @param gatewayId      gateway id
     * @return the gateway with the new registration token (returned only once)
     * @throws APIManagementException if gateway not found or on database error
     */
    CreatePlatformGatewayResult regenerateGatewayToken(String organizationId, String gatewayId)
            throws APIManagementException;

    /**
     * Delete a platform gateway and all references (tokens, instance mappings, revision deployment
     * records, gateway environment, permissions). Fails if any API revisions are currently deployed
     * to this gateway.
     *
     * @param organizationId organization id
     * @param gatewayId      gateway id
     * @throws APIManagementException if gateway not found, not in organization, or has active API deployments
     */
    void deleteGateway(String organizationId, String gatewayId) throws APIManagementException;

    /**
     * Update platform gateway metadata. Only updatable fields (displayName, description, properties)
     * are applied; null values mean leave existing value unchanged. Name and vhost are not updatable.
     *
     * @param organizationId organization id
     * @param gatewayId      gateway id
     * @param displayName    new display name, or null to keep existing
     * @param description    new description, or null to keep existing
     * @param propertiesJson new properties JSON string, or null to keep existing
     * @return the updated platform gateway
     * @throws APIManagementException if gateway not found or not in organization (use ExceptionCodes.PLATFORM_GATEWAY_NOT_FOUND for 404)
     */
    PlatformGateway updateGateway(String organizationId, String gatewayId, String displayName,
                                  String description, String propertiesJson)
            throws APIManagementException;

    /**
     * Update gateway connection status (e.g. WebSocket connect/disconnect). Stores in environment additionalProperties.
     *
     * @param gatewayId      gateway (environment) UUID
     * @param organizationId organization id
     * @param active         true when connected, false when disconnected
     */
    void updateGatewayActiveStatus(String gatewayId, String organizationId, boolean active)
            throws APIManagementException;
}
