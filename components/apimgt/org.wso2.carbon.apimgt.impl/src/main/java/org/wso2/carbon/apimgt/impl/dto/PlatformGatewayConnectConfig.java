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
 * Config for the Universal Gateway connect feature ([[apim.universal_gateway.connect]]).
 * Separate from {@link GatewayNotificationConfiguration} so existing notification/heartbeat
 * code is unchanged. Used only by the connect-with-token flow.
 */
public class PlatformGatewayConnectConfig {
    private List<ConnectGatewayConfig> connectGateways = new ArrayList<>();
    private String universalGatewayVersion;

    /**
     * Global default Universal Gateway version (e.g. "0.9.0"). From apim.universal_gateway.version.
     */
    public String getUniversalGatewayVersion() {
        return universalGatewayVersion;
    }

    public void setUniversalGatewayVersion(String universalGatewayVersion) {
        this.universalGatewayVersion = universalGatewayVersion != null ? universalGatewayVersion : "";
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
