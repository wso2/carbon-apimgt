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

package org.wso2.carbon.apimgt.impl.gateway;

/**
 * Service to broadcast API key lifecycle events to platform gateways via WebSocket.
 * When an opaque API key is created, updated, or revoked (e.g. from DevPortal/Admin or internal API),
 * call the corresponding method so connected API Platform gateways can update their key cache.
 * <p>
 * If no implementation is registered (e.g. internal data service not loaded), getter returns null
 * and callers should no-op.
 */
public interface PlatformGatewayAPIKeyEventService {

    /**
     * Broadcast apikey.created to platform gateways in the given organization.
     *
     * @param organizationId    organization id used to resolve target platform gateways (required)
     * @param apiId             API UUID (required)
     * @param keyUuid           Optional control-plane API key UUID
     * @param apiKey            Plain text API key (required)
     * @param name              URL-safe key identifier, 3-63 chars (required)
     * @param operations        Comma-separated or wildcard (required)
     * @param externalRefId     Optional external reference
     * @param expiresAt         Optional ISO 8601 expiry
     * @param expiresInDuration Optional duration for expiresIn
     * @param expiresInUnit     Optional unit (seconds, minutes, hours, days, weeks, months)
     * @param displayName       Optional display name
     * @param userId            Optional user id
     */
    void broadcastAPIKeyCreated(String organizationId, String apiId, String keyUuid, String apiKey, String name,
                                String operations, String externalRefId, String expiresAt, Integer expiresInDuration,
                                String expiresInUnit, String displayName, String userId);

    /**
     * Broadcast apikey.updated to platform gateways in the given organization.
     *
     * @param organizationId    organization id used to resolve target platform gateways (required)
     * @param apiId             API UUID (required)
     * @param keyUuid           Optional control-plane API key UUID
     * @param keyName           Key name (required)
     * @param apiKey            Plain text API key (required)
     * @param externalRefId     Optional
     * @param operations        Optional
     * @param displayName       Required for updated event
     * @param expiresAt         Optional ISO 8601
     * @param expiresInDuration Optional
     * @param expiresInUnit     Optional
     * @param userId            Optional
     */
    void broadcastAPIKeyUpdated(String organizationId, String apiId, String keyUuid, String keyName, String apiKey,
                                String externalRefId, String operations, String displayName, String expiresAt,
                                Integer expiresInDuration, String expiresInUnit, String userId);

    /**
     * Broadcast apikey.revoked to platform gateways in the given organization.
     *
     * @param organizationId organization id used to resolve target platform gateways (required)
     * @param apiId   API UUID (required)
     * @param keyName Key name (required)
     * @param userId Optional user id
     */
    void broadcastAPIKeyRevoked(String organizationId, String apiId, String keyName, String userId);
}
