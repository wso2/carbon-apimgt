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

package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.models.Event;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * An {@link org.wso2.carbon.apimgt.core.api.EventObserver} which is used to observe
 * {@link org.wso2.carbon.apimgt.core.api.APIMObservable} objects and log any event occurrence.
 */
public class EventLogger implements EventObserver {

    private static final Logger log = LoggerFactory.getLogger(EventObserver.class);

    /**
     * Constructor.
     */
    public EventLogger() {

    }

    /**
     * Used to log all the {@link org.wso2.carbon.apimgt.core.models.Event} occurrences
     * in an {@link org.wso2.carbon.apimgt.core.api.APIMObservable} object.
     * <p>
     * This is a specific implementation for
     * {@link org.wso2.carbon.apimgt.core.api.EventObserver#captureEvent(Event, String, ZonedDateTime, Map)} method,
     * provided by {@link org.wso2.carbon.apimgt.core.impl.EventLogger} which implements
     * {@link org.wso2.carbon.apimgt.core.api.EventObserver} interface.
     * <p>
     * {@inheritDoc}
     *
     * @see org.wso2.carbon.apimgt.core.impl.FunctionTrigger#captureEvent(Event, String, ZonedDateTime, Map)
     */
    @Override
    public void captureEvent(Event event, String username, ZonedDateTime eventTime,
                             Map<String, String> metadata) {
        if (event == null) {
            throw new IllegalArgumentException("Event must not be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        // Following statement is used to log any events
        log.info("New event occurred: -Event: " + event.getEventAsString() + " -Component Name: " +
                event.getComponent() + " -Username: " + username + " \n");
    }
}
