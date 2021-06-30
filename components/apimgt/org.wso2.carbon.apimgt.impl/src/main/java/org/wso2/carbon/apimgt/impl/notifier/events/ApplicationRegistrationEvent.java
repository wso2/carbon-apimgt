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
 * An Event Object which can holds the data related to Application Registration which are required
 * for the validation purpose in a gateway.
 */
public class ApplicationRegistrationEvent extends Event {
    private String applicationUUID;
    private int applicationId;
    private String consumerKey;
    private String keyType;
    private String keyManager;

    public ApplicationRegistrationEvent(String eventId, long timestamp, String type, int tenantId,
                                        String tenantDomain, int applicationId, String applicationUUID,
                                        String consumerKey, String keyType, String keyManager) {
        this.eventId = eventId;
        this.timeStamp = timestamp;
        this.type = type;
        this.tenantId = tenantId;
        this.applicationId = applicationId;
        this.consumerKey = consumerKey;
        this.keyType = keyType;
        this.keyManager = keyManager;
        this.tenantDomain = tenantDomain;
        this.applicationUUID = applicationUUID;
    }

    @Override
    public String toString() {

        return "ApplicationRegistrationEvent{" +
                "applicationUUID='" + applicationUUID + '\'' +
                ", applicationId=" + applicationId +
                ", consumerKey='" + consumerKey + '\'' +
                ", keyType='" + keyType + '\'' +
                ", keyManager='" + keyManager + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ApplicationRegistrationEvent)) return false;
        ApplicationRegistrationEvent that = (ApplicationRegistrationEvent) o;
        return getApplicationId() == that.getApplicationId() &&
                getConsumerKey().equals(that.getConsumerKey()) &&
                getKeyType().equals(that.getKeyType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getApplicationId(), getConsumerKey(), getKeyType());
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public String getKeyManager() {

        return keyManager;
    }

    public void setKeyManager(String keyManager) {

        this.keyManager = keyManager;
    }

    public String getApplicationUUID() {

        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {

        this.applicationUUID = applicationUUID;
    }
}
