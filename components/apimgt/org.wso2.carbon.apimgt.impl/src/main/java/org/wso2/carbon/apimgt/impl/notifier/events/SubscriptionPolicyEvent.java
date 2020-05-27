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

/**
 * An Event Object which can holds the data related to Subscription Policy which are required
 * for the validation purpose in a gateway.
 */
public class SubscriptionPolicyEvent extends Event {
    private int policyId;
    private String policyName;
    private String quotaType;
    private int rateLimitCount;
    private String rateLimitTimeUnit;
    private boolean stopOnQuotaReach;

    public SubscriptionPolicyEvent(String eventId, long timestamp, String type, int tenantId, int policyId,
                                   String policyName, String quotaType, int rateLimitCount, String rateLimitTimeUnit,
                                   boolean stopOnQuotaReach) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.policyId = policyId;
        this.policyName = policyName;
        this.quotaType = quotaType;
        this.rateLimitCount = rateLimitCount;
        this.rateLimitTimeUnit = rateLimitTimeUnit;
        this.stopOnQuotaReach = stopOnQuotaReach;
    }

    @Override
    public String toString() {
        return "SubscriptionPolicyEvent{" +
                "policyId=" + policyId +
                ", policyName='" + policyName + '\'' +
                ", quotaType='" + quotaType + '\'' +
                ", rateLimitCount=" + rateLimitCount +
                ", rateLimitTimeUnit='" + rateLimitTimeUnit + '\'' +
                ", stopOnQuotaReach=" + stopOnQuotaReach +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPolicyEvent)) return false;
        SubscriptionPolicyEvent that = (SubscriptionPolicyEvent) o;
        return getPolicyId() == that.getPolicyId() &&
                getRateLimitCount() == that.getRateLimitCount() &&
                isStopOnQuotaReach() == that.isStopOnQuotaReach() &&
                getPolicyName().equals(that.getPolicyName()) &&
                getQuotaType().equals(that.getQuotaType()) &&
                getRateLimitTimeUnit().equals(that.getRateLimitTimeUnit());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPolicyId(), getPolicyName(), getQuotaType(), getRateLimitCount(), getRateLimitTimeUnit(), isStopOnQuotaReach());
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

    public boolean isStopOnQuotaReach() {
        return stopOnQuotaReach;
    }

    public void setStopOnQuotaReach(boolean stopOnQuotaReach) {
        this.stopOnQuotaReach = stopOnQuotaReach;
    }
}
