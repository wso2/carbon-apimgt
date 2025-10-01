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

package org.wso2.carbon.apimgt.notification.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.event.stream.core.EventStreamService;

import java.util.HashMap;
import java.util.Map;

/**
 * Service holder class to keep osgi references.
 */
public class ServiceReferenceHolder {

    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private Map<String, EventHandler> eventHandlerMap = new HashMap<>();
    private EventStreamService eventStreamService;
    private APIManagerConfigurationService apiManagerConfigurationService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {

        return instance;
    }

    public EventHandler getEventHandlerByType(String type) {

        return eventHandlerMap.get(type);
    }

    public void addEventHandler(String type, EventHandler eventHandler) {

        if (log.isDebugEnabled()) {
            log.debug("Adding event handler for type: " + type);
        }
        eventHandlerMap.put(type, eventHandler);

    }

    public void removeEventHandlers(String type) {

        if (log.isDebugEnabled()) {
            log.debug("Removing event handler for type: " + type);
        }
        eventHandlerMap.remove(type);

    }

    public void setEventStreamService(EventStreamService eventStreamService) {

        this.eventStreamService = eventStreamService;
    }

    public EventStreamService getEventStreamService() {

        return eventStreamService;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return apiManagerConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService apiManagerConfigurationService) {
        this.apiManagerConfigurationService = apiManagerConfigurationService;
    }
}
