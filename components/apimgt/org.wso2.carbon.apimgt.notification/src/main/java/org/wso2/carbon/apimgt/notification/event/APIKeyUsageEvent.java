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

/**
 * API Key Usage Event Model to Retrieve Event.
 */
public class APIKeyUsageEvent extends Event {

    private static final long serialVersionUID = 1L;

    private String eventId;
    private String apiKeyHash;
    private long lastUsedTime;
    private String ttl;
    private long expiryTime;
    private String type;
    private int tenantId;

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getApiKeyHash() {
        return apiKeyHash;
    }

    public void setApiKeyHash(String apiKeyHash) {
        this.apiKeyHash = apiKeyHash;
    }

    public long getLastUsedTime() {
        return lastUsedTime;
    }

    public void setLastUsedTime(long lastUsedTime) {
        this.lastUsedTime = lastUsedTime;
    }

    public String getTtl() {
        return ttl;
    }

    public void setTtl(String ttl) {
        this.ttl = ttl;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    public String toString() {

        return "APIKeyUsageEvent{" +
                "eventId='" + eventId + '\'' +
                ", apiKeyHash='" + apiKeyHash +
                ", lastUsedTime=" + lastUsedTime + '\'' +
                ", ttl=" + ttl +
                ", expiryTime=" + expiryTime + '\'' +
                ", type='" + type +
                ", tenantId=" + tenantId + '\'' +
                '}';
    }
}
