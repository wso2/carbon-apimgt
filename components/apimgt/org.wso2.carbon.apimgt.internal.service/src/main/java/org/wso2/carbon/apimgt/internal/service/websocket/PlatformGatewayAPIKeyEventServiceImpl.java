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

package org.wso2.carbon.apimgt.internal.service.websocket;

import org.wso2.carbon.apimgt.impl.gateway.PlatformGatewayAPIKeyEventService;

/**
 * Implementation of {@link PlatformGatewayAPIKeyEventService} that delegates to
 * {@link PlatformGatewayAPIKeyEventBroadcaster} to push apikey.created, apikey.updated,
 * and apikey.revoked events to all connected platform gateways via WebSocket.
 */
public class PlatformGatewayAPIKeyEventServiceImpl implements PlatformGatewayAPIKeyEventService {

    @Override
    public void broadcastAPIKeyCreated(String organizationId, String apiId, String keyUuid, String apiKey,
                                      String name, String operations, String externalRefId, String expiresAt,
                                      Integer expiresInDuration,
                                      String expiresInUnit, String displayName, String userId) {
        PlatformGatewayAPIKeyEventBroadcaster.getInstance().broadcastAPIKeyCreated(
                organizationId, apiId, keyUuid, apiKey, name, operations, externalRefId, expiresAt, expiresInDuration,
                expiresInUnit, displayName, userId);
    }

    @Override
    public void broadcastAPIKeyUpdated(String organizationId, String apiId, String keyUuid, String keyName,
                                      String apiKey, String externalRefId, String operations, String displayName,
                                      String expiresAt,
                                      Integer expiresInDuration, String expiresInUnit, String userId) {
        PlatformGatewayAPIKeyEventBroadcaster.getInstance().broadcastAPIKeyUpdated(
                organizationId, apiId, keyUuid, keyName, apiKey, externalRefId, operations, displayName, expiresAt,
                expiresInDuration, expiresInUnit, userId);
    }

    @Override
    public void broadcastAPIKeyRevoked(String organizationId, String apiId, String keyName, String userId) {
        PlatformGatewayAPIKeyEventBroadcaster.getInstance().broadcastAPIKeyRevoked(
                organizationId, apiId, keyName, userId);
    }
}
