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

import org.wso2.carbon.apimgt.api.model.SubscribedAPI;

import java.util.Objects;
import java.util.UUID;

/**
 * An Event Object which can holds the data related to Subscription which are required
 * for the validation purpose in a gateway.
 */
public class SubscriptionEvent extends Event {

    private int subscriptionId;
    private String subscriptionUUID;
    private int apiId;
    private String apiUUID;
    private int applicationId;
    private String applicationUUID;
    private String policyId;
    private String subscriptionState;
    private String apiName;
    private String apiVersion;

    public SubscriptionEvent(String eventId, long timestamp, String type, int tenantId, String tenantDomain,
                             int subscriptionId, String subscriptionUUID, int apiId, String apiUUID, int applicationId,
                             String applicationUUID,
                             String policyId, String subscriptionState, String apiName, String apiVersion) {

        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.subscriptionUUID = subscriptionUUID;
        this.apiId = apiId;
        this.applicationId = applicationId;
        this.policyId = policyId;
        this.subscriptionState = subscriptionState;
        this.tenantDomain = tenantDomain;
        this.applicationUUID = applicationUUID;
        this.apiUUID = apiUUID;
        this.apiName = apiName;
        this.apiVersion = apiVersion;
    }

    public SubscriptionEvent(String type, SubscribedAPI subscribedAPI, int tenantId, String tenantDomain) {

        this.eventId = UUID.randomUUID().toString();
        this.timeStamp = System.currentTimeMillis();
        this.type = type;
        this.tenantId = tenantId;
        this.tenantDomain = tenantDomain;
        this.subscriptionUUID = subscribedAPI.getUUID();
        this.subscriptionId = subscribedAPI.getSubscriptionId();
        this.apiId = subscribedAPI.getApiId();
        this.apiUUID = subscribedAPI.getAPIUUId();
        this.applicationId = subscribedAPI.getApplication().getId();
        this.applicationUUID = subscribedAPI.getApplication().getUUID();
        this.policyId = subscribedAPI.getTier().getName();
        this.subscriptionState = subscribedAPI.getSubStatus();
        this.apiName = subscribedAPI.getIdentifier().getName();
        this.apiVersion = subscribedAPI.getIdentifier().getVersion();

    }

    @Override
    public String toString() {

        return "SubscriptionEvent{" +
                "subscriptionId=" + subscriptionId +
                ", subscriptionUUID='" + subscriptionUUID + '\'' +
                ", apiId=" + apiId +
                ", apiUUID='" + apiUUID + '\'' +
                ", applicationId=" + applicationId +
                ", applicationUUID='" + applicationUUID + '\'' +
                ", policyId='" + policyId + '\'' +
                ", subscriptionState='" + subscriptionState + '\'' +
                ", tenantDomain='" + tenantDomain + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof SubscriptionEvent)) return false;
        SubscriptionEvent that = (SubscriptionEvent) o;
        return getSubscriptionId() == that.getSubscriptionId() &&
                getApplicationId() == that.getApplicationId() &&
                getApiId() == (that.getApiId()) &&
                getPolicyId().equals(that.getPolicyId()) &&
                getSubscriptionState().equals(that.getSubscriptionState());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getSubscriptionId(), getApiId(), getApplicationId(), getPolicyId(), getSubscriptionState());
    }

    public int getSubscriptionId() {

        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {

        this.subscriptionId = subscriptionId;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiName(int apiId) {

        this.apiId = apiId;
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

    public String getSubscriptionUUID() {

        return subscriptionUUID;
    }

    public void setSubscriptionUUID(String subscriptionUUID) {

        this.subscriptionUUID = subscriptionUUID;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public String getApiUUID() {

        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {

        this.apiUUID = apiUUID;
    }

    public String getApplicationUUID() {

        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {

        this.applicationUUID = applicationUUID;
    }

    public String getApiName() {

        return apiName;
    }

    public void setApiName(String apiName) {

        this.apiName = apiName;
    }

    public String getApiVersion() {

        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {

        this.apiVersion = apiVersion;
    }
}
