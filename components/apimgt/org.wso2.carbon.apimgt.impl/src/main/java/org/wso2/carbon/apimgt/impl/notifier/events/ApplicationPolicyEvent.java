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

package org.wso2.carbon.apimgt.impl.notifier.events;

import java.util.Objects;

import org.wso2.carbon.apimgt.impl.APIConstants.PolicyType;

/**
 * An Event Object which can holds the data related to Application Policy which are required
 * for the validation purpose in a gateway.
 */
public class ApplicationPolicyEvent extends PolicyEvent {
    private int policyId;
    private String policyName;
    private String quotaType;
    private int rateLimitCount;
    private String rateLimitTimeUnit;

    public int getRateLimitCount() {
        return rateLimitCount;
    }

    public void setRateLimitCount(int rateLimitCount) {
        this.rateLimitCount = rateLimitCount;
    }

    public String getRateLimitTimeUnit() {
        return rateLimitTimeUnit;
    }

    public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
        this.rateLimitTimeUnit = rateLimitTimeUnit;
    }

    public ApplicationPolicyEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
            int policyId, String policyName, String quotaType, int rateLimitCount, String rateLimitTimeUnit) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.policyId = policyId;
        this.policyName = policyName;
        this.quotaType = quotaType;
        this.tenantDomain = tenantDomain;
        this.policyType = PolicyType.APPLICATION;
        this.rateLimitCount = rateLimitCount;
        this.rateLimitTimeUnit = rateLimitTimeUnit;
    }

    @Override
    public String toString() {
        return "ApplicationPolicyEvent{" +
                "policyId=" + policyId +
                ", policyName='" + policyName + '\'' +
                ", quotaType='" + quotaType + '\'' +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId + '\'' +
                ", tenantDomain=" + tenantDomain +
                ", rateLimitCount=" + rateLimitCount +
                ", rateLimitTimeUnit=" + rateLimitTimeUnit +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationPolicyEvent)) return false;
        ApplicationPolicyEvent that = (ApplicationPolicyEvent) o;
        return getPolicyId() == that.getPolicyId() &&
                getPolicyName().equals(that.getPolicyName()) &&
                getQuotaType().equals(that.getQuotaType()) &&
                getRateLimitCount() == that.getRateLimitCount() &&
                getRateLimitTimeUnit().equals(that.getRateLimitTimeUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPolicyId(), getPolicyName(), getQuotaType());
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public String getQuotaType() {
        return quotaType;
    }

    public void setQuotaType(String quotaType) {
        this.quotaType = quotaType;
    }
}
