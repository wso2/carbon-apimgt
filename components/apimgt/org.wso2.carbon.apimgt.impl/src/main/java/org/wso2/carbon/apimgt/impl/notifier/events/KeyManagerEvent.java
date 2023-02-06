/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.notifier.events;

import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Key Manager Event to handle create/update/deletes of Key Managers
 */
public class KeyManagerEvent extends Event {
    private String name;
    private boolean enabled;
    private String value;
    private String action;
    private String organization;
    private String keyManagerType;
    private String tokenType;

    public KeyManagerEvent(String eventId, long timeStamp, int tenantId, String tenantDomain,
                           String action, String name, String keyManagerType, boolean enabled,
                           String value, String organization, String tokenType) {
        super(eventId, timeStamp, APIConstants.EventType.KEY_MANAGER_CONFIGURATION.name(),
                tenantId, tenantDomain);
        this.action = action;
        this.name = name;
        this.keyManagerType = keyManagerType;
        this.enabled = enabled;
        this.value = value;
        this.organization = organization;
        this.tokenType = tokenType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getKeyManagerType() {
        return keyManagerType;
    }

    public void setKeyManagerType(String keyManagerType) {
        this.keyManagerType = keyManagerType;
    }

    @Override
    public String toString() {
        return "KeyManagerEvent{" +
                "eventId='" + eventId + '\'' +
                ", timeStamp=" + timeStamp +
                ", type='" + type + '\'' +
                ", tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", name='" + name + '\'' +
                ", enabled=" + enabled +
                ", value='" + value + '\'' +
                ", action='" + action + '\'' +
                ", organization='" + organization + '\'' +
                ", keyManagerType='" + keyManagerType + '\'' +
                ", tokenType='" + tokenType + '\'' +
                '}';
    }
}
