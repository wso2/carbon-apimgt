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

import org.wso2.carbon.apimgt.api.model.PlatformGatewayDeploymentEventRecord;

import java.util.List;

/**
 * Service for platform gateway events (AM_GW_PLATFORM_EVENT).
 * Used for multi-CP WebSocket sync: persist events per gateway; on gateway connect, get pending events
 * and mark delivered after sending. Pending records returned to callers contain wire JSON for the gateway
 * (envelope stripped when applicable).
 */
public interface PlatformGatewayDeploymentEventService {

    /**
     * Persist one event for the given gateway. {@code payload} is the stored blob (typically
     * {@code { metadata?, message }} for the gateway wire JSON inside {@code message}, or legacy raw wire JSON).
     *
     * @param gatewayId target gateway UUID
     * @param eventType e.g. api.deployed, api.undeployed, api.deleted, or other event kinds
     * @param payload   UTF-8 JSON string (envelope or raw wire message)
     */
    void persistEvent(String gatewayId, String eventType, String payload) throws APIManagementException;

    /**
     * Get pending events for the gateway (DELIVERED_AT IS NULL). Does not mark as delivered.
     * Used by the scheduler to push to already-connected gateways; caller must send then call markDelivered.
     *
     * @param gatewayId gateway UUID
     * @return list of records (id, payload) where payload is wire JSON for the gateway
     */
    List<PlatformGatewayDeploymentEventRecord> getPendingEventsForGateway(String gatewayId)
            throws APIManagementException;

    /**
     * Mark the given event IDs as delivered. Call after sending payloads to the gateway.
     *
     * @param eventIds event IDs returned from getPendingEventsForGateway
     */
    void markDelivered(List<String> eventIds) throws APIManagementException;

    /**
     * Claim pending events for the gateway (reserves them for this caller; does not set DELIVERED_AT).
     * Call when gateway connects; send each payload over WebSocket, then call {@link #markDelivered(List)}
     * with the IDs of successfully sent events. Delivery is acknowledged only after markDelivered.
     *
     * @param gatewayId gateway UUID
     * @return list of records (id, payload) that were claimed for sending (payload is wire JSON for the gateway)
     */
    List<PlatformGatewayDeploymentEventRecord> getAndMarkDeliveredPendingEventsForGateway(String gatewayId)
            throws APIManagementException;

    /**
     * Delete delivered events older than the given retention window to prevent unbounded table growth.
     * Only rows with DELIVERED_AT set and older than (now - retentionMs) are removed.
     *
     * @param retentionMs retention window in milliseconds (e.g. 86400000 = 24 hours)
     * @return number of rows deleted
     */
    int cleanupDeliveredEventsOlderThan(long retentionMs) throws APIManagementException;
}
