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

/**
 * Platform gateway websocket/control-plane protocol constants.
 */
public final class PlatformGatewayWebSocketConstants {

    public static final String EVENT_CONNECTION_ACK = "connection.ack";
    public static final String EVENT_DEPLOYMENT_ACK = "deployment.ack";
    public static final String EVENT_API_DEPLOYED = "api.deployed";
    public static final String EVENT_API_UNDEPLOYED = "api.undeployed";
    public static final String EVENT_API_DELETED = "api.deleted";
    public static final String EVENT_APIKEY_CREATED = "apikey.created";
    public static final String EVENT_APIKEY_UPDATED = "apikey.updated";
    public static final String EVENT_APIKEY_REVOKED = "apikey.revoked";

    public static final String RESOURCE_TYPE_API = "api";

    public static final String ACTION_DEPLOY = "deploy";
    public static final String ACTION_UNDEPLOY = "undeploy";

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_FAILURE = "failure";

    private PlatformGatewayWebSocketConstants() {
    }
}
