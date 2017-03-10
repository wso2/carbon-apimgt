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
 * Interface for observable which can be observed by {@link org.wso2.carbon.apimgt.core.api.EventObserver}
 * whenever an {@link org.wso2.carbon.apimgt.core.models.Event} occurs.
 */
public interface APIMObservable {

    /**
     * To register an observer to {@link org.wso2.carbon.apimgt.core.api.APIMObservable}.
     * Registered {@link org.wso2.carbon.apimgt.core.api.EventObserver} can then observe any
     * {@link org.wso2.carbon.apimgt.core.models.Event} occurs in
     * {@link org.wso2.carbon.apimgt.core.api.APIMObservable} and take its action.
     *
     * @param observer Observer which needs to be registered
     */
    void registerObserver(EventObserver observer);

    /**
     * To notify all the registered {@link org.wso2.carbon.apimgt.core.api.EventObserver}, whenever an
     * {@link org.wso2.carbon.apimgt.core.models.Event} occurs.
     *
     * @param event     Event which occurred
     * @param username  Logged in user's username
     * @param eventTime Time at which event occurred
     * @param metadata  Event specific metadata
     */
    void notifyObservers(Event event, String username, ZonedDateTime eventTime, Map<String, String> metadata);

    /**
     * To remove a registered {@link org.wso2.carbon.apimgt.core.api.EventObserver}. Once removed, it won't get anymore
     * notifications whenever an {@link org.wso2.carbon.apimgt.core.models.Event} occurs.
     *
     * @param observer Observer which needs to be removed
     */
    void removeObserver(EventObserver observer);

}
