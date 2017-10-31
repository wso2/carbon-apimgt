/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.models.Event;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Thread used to notify the observers asynchronously, whenever an {@link org.wso2.carbon.apimgt.core.models.Event}
 * occurs in an {@link org.wso2.carbon.apimgt.core.api.APIMObservable}.
 */
public class ObserverNotifier implements Runnable {

    private Event event;
    private String username;
    private APIMObservable observable;
    private ZonedDateTime eventTime;
    private Map<String, String> metadata;

    /**
     * Constructor.
     *
     * @param event      Event which occurred
     * @param username   Logged in user's username
     * @param eventTime  Time at which event occurred
     * @param metadata   Event specific metadata
     * @param observable APIMObservable object in which the Event occurred
     */
    public ObserverNotifier(Event event, String username, ZonedDateTime eventTime, Map<String, String> metadata,
                            APIMObservable observable) {
        this.event = event;
        this.username = username;
        this.observable = observable;
        this.eventTime = eventTime;
        this.metadata = metadata;
    }

    /**
     * Run method which calls the
     * {@link org.wso2.carbon.apimgt.core.api.APIMObservable#notifyObservers(Event, String, ZonedDateTime, Map)} method
     * to notify each registered {@link org.wso2.carbon.apimgt.core.api.EventObserver}.
     */
    @Override
    public void run() {
        if (observable == null) {
            throw new IllegalArgumentException("APIMObservable must not be null");
        }
        observable.notifyObservers(event, username, eventTime, metadata);
    }
}
