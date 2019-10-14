/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.dto;

/**
 *  DTO of condition group
 */
public class ConditionGroupDTO {

    private int conditionGroupId;

    private String policyName;

    private int tenantId;

    private ConditionDTO[] conditions;

    public int getConditionGroupId() {
        return conditionGroupId;
    }

    public void setConditionGroupId(int conditionGroupId) {
        this.conditionGroupId = conditionGroupId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public ConditionDTO[] getConditions() {
        return conditions;
    }

    public void setConditions(ConditionDTO[] conditions) {
        this.conditions = conditions;
    }
}
