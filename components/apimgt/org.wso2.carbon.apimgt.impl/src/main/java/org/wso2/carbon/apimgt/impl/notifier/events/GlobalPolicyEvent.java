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

import org.wso2.carbon.apimgt.impl.APIConstants.PolicyType;

import java.util.Objects;

/**
 * An Event Object which can holds the data related to Global Policy
 */
public class GlobalPolicyEvent extends PolicyEvent {
    private int policyId;
    private String policyName;

    public GlobalPolicyEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                             int policyId, String policyName) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.policyId = policyId;
        this.policyName = policyName;
        this.tenantDomain = tenantDomain;
        this.policyType = PolicyType.GLOBAL;
    }

    @Override
    public String toString() {
        return "GlobalPolicyEvent{" +
                "policyId=" + policyId +
                ", policyName='" + policyName + '\'' +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId + '\'' +
                ", tenantDomain=" + tenantDomain +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GlobalPolicyEvent)) return false;
        GlobalPolicyEvent that = (GlobalPolicyEvent) o;
        return getPolicyId() == that.getPolicyId() &&
                getPolicyName().equals(that.getPolicyName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPolicyId(), getPolicyName());
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
}
