/*
 *   Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.notification.event;

import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import java.util.List;

/**
 * Opaque API Key Info Event Model to Send Event.
 */
public class OpaqueAPIKeyEvent extends Event {

    private static final long serialVersionUID = 1L;

    private String apiKeyHash;
    private String applicationId;
    private long expiryTime;
    private String user;
    private List<SubscribedAPI> subscribedApis;
    private String status;
    private String salt;

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<SubscribedAPI> getSubscribedApis() {
        return subscribedApis;
    }

    public void setSubscribedApis(List<SubscribedAPI> subscribedApis) {
        this.subscribedApis = subscribedApis;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    @Override
    public String toString() {

        return "APIKeyUsageEvent{" +
                "apiKeyHash='" + apiKeyHash + '\'' +
                ", applicationId=" + applicationId +
                ", expiryTime=" + expiryTime + '\'' +
                ", user='" + user +
                ", subscribedApis=" + subscribedApis + '\'' +
                ", status=" + status +
                ", salt='" + salt + '\'' +
                ", eventId='" + eventId +
                ", timeStamp=" + timeStamp + '\'' +
                ", type='" + type +
                ", tenantId=" + tenantId + '\'' +
                ", tenantDomain='" + tenantDomain +
                '}';
    }
}
