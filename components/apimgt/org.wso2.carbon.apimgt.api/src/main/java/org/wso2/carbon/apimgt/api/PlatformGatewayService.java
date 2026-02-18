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
     * @param isCritical      whether the gateway is critical
     * @param functionalityType functionality type (e.g. regular, ai, event)
     * @param propertiesJson  optional JSON string for custom properties
     * @return created gateway and registration token (returned only once)
     * @throws APIManagementException if validation fails or name already exists (use ExceptionCodes.PLATFORM_GATEWAY_NAME_ALREADY_EXISTS for 409)
     */
    CreatePlatformGatewayResult createGateway(String organizationId, String name, String displayName,
                                              String description, String vhost, boolean isCritical,
                                              String functionalityType, String propertiesJson)
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
     * Get a platform gateway by id.
     *
     * @param id gateway id
     * @return the gateway, or null if not found
     */
    PlatformGateway getGatewayById(String id) throws APIManagementException;
}
