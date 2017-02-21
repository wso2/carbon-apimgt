/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.core.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Enum for selecting Event options
 */
public enum Event {
    API_CREATION("API_CREATION", Component.API_PUBLISHER), API_DELETION("API_DELETION", Component.API_PUBLISHER),
    API_UPDATE("API_UPDATE", Component.API_PUBLISHER), LIFE_CYCLE_CHANGE("LIFE_CYCLE_CHANGE", Component.API_PUBLISHER),
    DOC_CREATION("DOC_CREATION", Component.API_PUBLISHER), DOC_DELETION("DOC_DELETION", Component.API_PUBLISHER),
    SUBSCRIPTION_REQUEST("SUBSCRIPTION_REQUEST", Component.API_PUBLISHER),
    DOC_MODIFICATION("DOC_MODIFICATION", Component.API_PUBLISHER),
    APP_CREATION("APP_CREATION", Component.API_STORE), APP_MODIFICATION("APP_MODIFICATION", Component.API_STORE),
    APP_DELETION("APP_DELETION", Component.API_STORE), FORUM_CREATION("FORUM_CREATION", Component.API_STORE);

    private String event;
    private Component component;

    Event(String event, Component component) {
        this.event = event;
        this.component = component;
    }

    public String getEventAsString() {
        return event;
    }

    public Component getComponent() {
        return component;
    }

    public List<Event> getEventsForComponent(Component component) {
        List<Event> componentSpecificEvents = new ArrayList<>();

        for (Event event : Event.values()) {
            if (event.getComponent().equals(component)) {
                componentSpecificEvents.add(event);
            }
        }
        return componentSpecificEvents;
    }
}
