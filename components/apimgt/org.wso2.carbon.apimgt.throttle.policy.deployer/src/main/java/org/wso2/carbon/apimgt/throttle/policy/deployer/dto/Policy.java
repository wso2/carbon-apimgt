/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.throttle.policy.deployer.dto;

import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;

/**
 * Top level entity for representing a Throttling Policy.
 */
public class Policy {

    public enum POLICY_TYPE {
        SUBSCRIPTION,
        APPLICATION,
        API,
        GLOBAL
    }

    private Integer id = null;
    private Integer tenantId = null;
    private String tenantDomain = null;
    private String name = null;
    private String quotaType = null;
    private QuotaPolicy defaultLimit = null;
    private POLICY_TYPE type;

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getQuotaType() {

        return quotaType;
    }

    public void setQuotaType(String quotaType) {

        this.quotaType = quotaType;
    }

    public boolean isContentAware() {

        return PolicyConstants.BANDWIDTH_TYPE.equals(quotaType);
    }

    public int getTenantId() {

        return tenantId;
    }

    public void setTenantId(int tenantId) {

        this.tenantId = tenantId;
    }

    public String getName() {

        return name;
    }

    public void setTierName(String name) {

        this.name = name;
    }

    public QuotaPolicy getDefaultLimit() {
        return defaultLimit;
    }

    public void setDefaultLimit(QuotaPolicy defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    public POLICY_TYPE getType() {
        return type;
    }

    public void setType(POLICY_TYPE type) {
        this.type = type;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
