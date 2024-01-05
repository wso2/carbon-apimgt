/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.gateway;

/**
 * This contains policy sequences related data from Gateway policy mapping to deploy in Gateway.
 */
public class GatewayPolicyDTO {

    private GatewayContentDTO[] gatewayPolicySequenceToBeAdded;
    private String tenantDomain;

    public GatewayContentDTO[] getGatewayPolicySequenceToBeAdded() {
        return gatewayPolicySequenceToBeAdded;
    }

    public void setGatewayPolicySequenceToBeAdded(GatewayContentDTO[] gatewayPolicySequenceToBeAdded) {
        this.gatewayPolicySequenceToBeAdded = gatewayPolicySequenceToBeAdded;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
