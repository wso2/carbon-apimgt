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
 * An Event Object which can holds the data related to Subscription which are required
 * for the validation purpose in a gateway.
 */
public class SubscriptionEvent extends Event {
    private int subscriptionId;
    private String apiName;
    private int applicationId;
    private String policyId;
    private String subscriptionState;

    public SubscriptionEvent(String eventId, long timestamp, String type, int tenantId, int subscriptionId, String apiName, int applicationId, String policyId, String subscriptionState) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.apiName = apiName;
        this.applicationId = applicationId;
        this.policyId = policyId;
        this.subscriptionState = subscriptionState;
    }

    @Override
    public String toString() {
        return "SubscriptionEvent{" +
                "subscriptionId=" + subscriptionId +
                ", apiName='" + apiName + '\'' +
                ", applicationId=" + applicationId +
                ", policyId='" + policyId + '\'' +
                ", subscriptionState='" + subscriptionState + '\'' +
                ", eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionEvent)) return false;
        SubscriptionEvent that = (SubscriptionEvent) o;
        return getSubscriptionId() == that.getSubscriptionId() &&
                getApplicationId() == that.getApplicationId() &&
                getApiName().equals(that.getApiName()) &&
                getPolicyId().equals(that.getPolicyId()) &&
                getSubscriptionState().equals(that.getSubscriptionState());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubscriptionId(), getApiName(), getApplicationId(), getPolicyId(), getSubscriptionState());
    }

    public int getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiUUID) {
        this.apiName = apiName;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    public String getSubscriptionState() {
        return subscriptionState;
    }

    public void setSubscriptionState(String subscriptionState) {
        this.subscriptionState = subscriptionState;
    }
}
