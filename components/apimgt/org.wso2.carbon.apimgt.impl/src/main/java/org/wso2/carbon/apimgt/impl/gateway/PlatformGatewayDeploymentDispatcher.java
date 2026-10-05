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

import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;

import java.util.Set;

/**
 * Dispatches API deploy/undeploy events to API Platform (Envoy) gateways.
 * Implementations may push via WebSocket to connected gateways or invoke Internal Data Service REST.
 * When no implementation is registered, {@link org.wso2.carbon.apimgt.impl.notifier.PlatformGatewayDeployNotifier}
 * will no-op for events that have platform gateway targets.
 */
public interface PlatformGatewayDeploymentDispatcher {

    /**
     * Dispatch a deploy event to the given platform gateways.
     *
     * @param event               deploy event (type DEPLOY_API_IN_GATEWAY)
     * @param platformGatewayIds  set of platform gateway IDs to notify
     */
    void dispatchDeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds);

    /**
     * Dispatch an undeploy event to the given platform gateways (revision undeployed; config preserved for redeploy).
     *
     * @param event               undeploy event (type REMOVE_API_FROM_GATEWAY, deleted=false)
     * @param platformGatewayIds  set of platform gateway IDs to notify
     */
    void dispatchUndeploy(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds);

    /**
     * Dispatch a delete event to the given platform gateways (API removed from publisher; config removed).
     * Gateway will remove the API configuration so the same name+version can be reused.
     *
     * @param event               delete event (type REMOVE_API_FROM_GATEWAY, deleted=true)
     * @param platformGatewayIds  set of platform gateway IDs to notify
     */
    void dispatchDelete(DeployAPIInGatewayEvent event, Set<String> platformGatewayIds);

    /**
     * Close the WebSocket connection for the given gateway (e.g. when the gateway is deleted from the admin).
     * The gateway client will receive a close frame and can log/disconnect. No-op if no implementation is set.
     *
     * @param gatewayId platform gateway UUID
     */
    default void closeGatewayConnection(String gatewayId) {
        // No-op by default; WebSocket implementation closes the session.
    }
}
