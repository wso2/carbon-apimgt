/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.GatewayDeployer;

public class GatewayDto {
    private String name;
    private GatewayDeployer gatewayDeployer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GatewayDeployer getGatewayDeployer() {
        return gatewayDeployer;
    }

    public void setGatewayDeployer(GatewayDeployer gatewayDeployer) {
        this.gatewayDeployer = gatewayDeployer;
    }

}
