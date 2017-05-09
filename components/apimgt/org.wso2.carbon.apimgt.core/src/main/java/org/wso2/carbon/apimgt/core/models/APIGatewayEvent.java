/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * n compliance with the License.
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

package org.wso2.carbon.apimgt.core.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the gateway event generated from APIM Core
 */
public class APIGatewayEvent {
    private List<String> gatewayLabels;
    private String eventType;
    private String notificationMessage;
    private Map<String, String> eventDetails;

    public APIGatewayEvent(String eventType) {
        this.eventDetails = new HashMap<String, String>();
        this.eventType = eventType;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public List<String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(List<String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventDetail(String key) {
        return eventDetails.get(key);
    }

    public void addEventDetail(String key, String value) {
        eventDetails.put(key, value);
    }
}
