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

package org.wso2.carbon.apimgt.impl.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Config for Platform Gateway connect-with-token ({@code [[apim.platform_gateway.connect]]})
 * and version metadata ({@code apim.platform_gateway.versions}).
 * Separate from {@link GatewayNotificationConfiguration} for notification/heartbeat settings.
 */
public class PlatformGatewayConnectConfig {
    private List<String> platformGatewayVersions = new ArrayList<>();
    private List<ConnectGatewayConfig> connectGateways = new ArrayList<>();

    /**
     * Global API Platform Gateway versions (e.g. ["1.0.0"]).
     */
    public List<String> getPlatformGatewayVersions() {
        if (platformGatewayVersions == null) {
            platformGatewayVersions = new ArrayList<>();
        }
        return Collections.unmodifiableList(platformGatewayVersions);
    }

    public void setPlatformGatewayVersions(List<String> platformGatewayVersions) {
        this.platformGatewayVersions = platformGatewayVersions != null
                ? new ArrayList<>(platformGatewayVersions)
                : new ArrayList<>();
    }

    /**
     * Connect configs (one per gateway) for connect-with-token. If empty, platform connect is disabled.
     */
    public List<ConnectGatewayConfig> getConnectGateways() {
        if (connectGateways == null) {
            connectGateways = new ArrayList<>();
        }
        return connectGateways;
    }

    public void setConnectGateways(List<ConnectGatewayConfig> connectGateways) {
        this.connectGateways = connectGateways != null ? new ArrayList<>(connectGateways) : new ArrayList<>();
    }
}
