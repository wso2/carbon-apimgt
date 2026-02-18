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

package org.wso2.carbon.apimgt.api.model;

/**
 * Result of creating a platform gateway, including the one-time registration token.
 */
public class CreatePlatformGatewayResult {

    private final PlatformGateway gateway;
    private final String registrationToken;

    public CreatePlatformGatewayResult(PlatformGateway gateway, String registrationToken) {
        this.gateway = gateway;
        this.registrationToken = registrationToken;
    }

    public PlatformGateway getGateway() {
        return gateway;
    }

    public String getRegistrationToken() {
        return registrationToken;
    }
}
