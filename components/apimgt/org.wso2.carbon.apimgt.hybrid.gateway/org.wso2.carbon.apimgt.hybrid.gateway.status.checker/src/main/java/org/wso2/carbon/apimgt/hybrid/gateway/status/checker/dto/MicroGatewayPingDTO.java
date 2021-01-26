/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.hybrid.gateway.status.checker.dto;

/**
 * DTO class to store pinging related details
 */
public class MicroGatewayPingDTO {
    private String tenantDomain = null;
    private String token = null;

    /**
     * Retrieve tenant domain
     *
     * @return
     */
    public String getTenantDomain() {
        return tenantDomain;
    }

    /**
     * Assign tenant domain
     *
     * @param tenantDomain
     */
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Retrieve token
     *
     * @return
     */
    public String getToken() {
        return token;
    }

    /**
     * Assign token
     *
     * @param token
     */
    public void setToken(String token) {
        this.token = token;
    }

}
