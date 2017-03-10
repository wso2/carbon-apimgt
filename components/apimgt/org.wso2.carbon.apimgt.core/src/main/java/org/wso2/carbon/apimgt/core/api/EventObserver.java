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

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.models.Event;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Interface for observer which is used to observe any possible {@link org.wso2.carbon.apimgt.core.models.Event}
 * occur in an {@link org.wso2.carbon.apimgt.core.api.APIMObservable}.
 * Each {@link org.wso2.carbon.apimgt.core.api.EventObserver} can have different actions for event occurrences.
 */
@FunctionalInterface
public interface EventObserver {
    /**
     * Captures API Manager events and event related details.
     * Each {@link org.wso2.carbon.apimgt.core.api.EventObserver} can have different actions for event occurrences
     * by providing different implementation to this method.
     *
     * @param event     Event occurred
     * @param username  Logged in user's username
     * @param eventTime Time at which event occurred
     * @param metadata  Event specific metadata
     */
    void captureEvent(Event event, String username, ZonedDateTime eventTime, Map<String, String> metadata);
}
