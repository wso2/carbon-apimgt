/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
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

package org.wso2.carbon.apimgt.impl.notifier.events;

/**
 * An Event Object which can holds the data related to API Key regeneration which are required
 * for the validation purpose in a gateway.
 */
public class APIKeyRegenerationEvent extends Event {
    private String oldApiKeyHash;
    private String newApiKeyHash;

    public APIKeyRegenerationEvent(String eventId, long timeStamp, String type, int tenantId, String tenantDomain,
                                   String oldApiKeyHash, String newApiKeyHash) {
        super(eventId, timeStamp, type, tenantId, tenantDomain);
        this.oldApiKeyHash = oldApiKeyHash;
        this.newApiKeyHash = newApiKeyHash;
    }

    public APIKeyRegenerationEvent() {
    }

    public String getOldApiKeyHash() {
        return oldApiKeyHash;
    }

    public void setOldApiKeyHash(String oldApiKeyHash) {
        this.oldApiKeyHash = oldApiKeyHash;
    }

    public String getNewApiKeyHash() {
        return newApiKeyHash;
    }

    public void setNewApiKeyHash(String newApiKeyHash) {
        this.newApiKeyHash = newApiKeyHash;
    }
}
