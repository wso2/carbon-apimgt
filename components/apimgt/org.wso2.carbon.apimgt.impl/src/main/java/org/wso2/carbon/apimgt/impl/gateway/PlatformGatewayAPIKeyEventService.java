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
     */
    void broadcastAPIKeyCreated(PlatformGatewayAPIKeyEvents.Created event);

    /**
     * Broadcast apikey.updated to platform gateways in the given organization.
     */
    void broadcastAPIKeyUpdated(PlatformGatewayAPIKeyEvents.Updated event);

    /**
     * Broadcast apikey.revoked to platform gateways in the given organization.
     */
    void broadcastAPIKeyRevoked(PlatformGatewayAPIKeyEvents.Revoked event);
}
