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
 * Service for platform gateway deployment events (AM_GW_PLATFORM_DEPLOYMENT_EVENT).
 * Used for multi-CP WebSocket sync: persist deploy/undeploy/delete per gateway;
 * on gateway connect, get pending events and mark delivered after sending.
 */
public interface PlatformGatewayDeploymentEventService {

    /**
     * Persist one deployment event for the given gateway (full WebSocket message as payload).
     *
     * @param gatewayId    target gateway UUID
     * @param apiId       API UUID
     * @param revisionUuid revision/deployment ID (may be null for api.deleted)
     * @param eventType   e.g. api.deployed, api.undeployed, api.deleted
     * @param payload     full WebSocket message JSON (sent as-is on push-on-connect)
     */
    void persistEvent(String gatewayId, String apiId, String revisionUuid, String eventType, String payload)
            throws APIManagementException;

    /**
     * Get pending events for the gateway and mark them as delivered in one transaction.
     * Call when gateway connects; send each payload over WebSocket then they are already marked.
     *
     * @param gatewayId gateway UUID
     * @return list of records (id, payload) that were pending and are now marked delivered
     */
    List<PlatformGatewayDeploymentEventRecord> getAndMarkDeliveredPendingEventsForGateway(String gatewayId)
            throws APIManagementException;
}
