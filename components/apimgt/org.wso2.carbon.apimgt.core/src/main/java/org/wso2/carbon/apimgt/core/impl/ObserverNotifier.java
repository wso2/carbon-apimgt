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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.models.Event;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Thread used to notify the observers whenever an event occurs in an Observable
 */
public class ObserverNotifier implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ObserverNotifier.class);

    private Event event;
    private String username;
    private APIMObservable observable;
    private ZonedDateTime eventTime;
    private Map<String, String> metadata;

    public ObserverNotifier(Event event, String username, ZonedDateTime eventTime, Map<String, String> metadata,
                            APIMObservable observable) {
        this.event = event;
        this.username = username;
        this.observable = observable;
        this.eventTime = eventTime;
        this.metadata = metadata;
    }

    @Override
    public void run() {
        if (observable == null) {
            throw new IllegalArgumentException("APIMObservable must not be null");
        }
        observable.notifyObservers(event, username, eventTime, metadata);
    }
}
