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
 * An Observer which is used to observe possible APIMObservables and logs any event occurrence.
 */
public class EventLogger implements EventObserver {

    private static final Logger log = LoggerFactory.getLogger(EventObserver.class);

    private EventLogger() {

    }

    private static class SingletonHelper {
        static final EventLogger instance = new EventLogger();
    }

    public static EventLogger getInstance() {
        return SingletonHelper.instance;
    }

    @Override
    public void captureEvent(Event event, String username, ZonedDateTime eventTime, Map<String, String> extraInformation) {
        // the following statement is used to log any events
        log.info("New event occurred: -Event: " + event.getEventAsString() + " -Component Name: " + event.getComponent() +
                " -Username: " + username + "\n");
    }
}
