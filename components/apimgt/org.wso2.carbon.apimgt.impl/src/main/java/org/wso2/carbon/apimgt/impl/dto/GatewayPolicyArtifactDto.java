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

package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;

import java.util.List;

/**
 * This class is used as a data transfer object for gateway policy artifacts.
 */
public class GatewayPolicyArtifactDto {

    List<OperationPolicyData> gatewayPolicyDataList;
    List<OperationPolicy> gatewayPolicyList;
    String tenantDomain;

    public List<OperationPolicyData> getGatewayPolicyDataList() {
        return gatewayPolicyDataList;
    }

    public void setGatewayPolicyDataList(List<OperationPolicyData> gatewayPolicyDataList) {
        this.gatewayPolicyDataList = gatewayPolicyDataList;
    }

    public List<OperationPolicy> getGatewayPolicyList() {
        return gatewayPolicyList;
    }

    public void setGatewayPolicyList(List<OperationPolicy> gatewayPolicyList) {
        this.gatewayPolicyList = gatewayPolicyList;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
