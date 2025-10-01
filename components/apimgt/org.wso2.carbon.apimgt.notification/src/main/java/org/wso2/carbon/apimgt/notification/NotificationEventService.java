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

package org.wso2.carbon.apimgt.notification;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.handlers.EventHandler;
import org.wso2.carbon.apimgt.notification.internal.ServiceReferenceHolder;

import java.util.List;
import java.util.Map;

/**
 * Osgi Service to handle NotificationEvents
 */
public class NotificationEventService {
    private static final Log log = LogFactory.getLog(NotificationEventService.class);
    
    public void processEvent(String type, String content, Map<String, List<String>> headers)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Processing notification event - Type: " + type + " Content length: " 
                    + (content != null ? content.length() : 0) + " Headers count: " 
                    + (headers != null ? headers.size() : 0));
        }
        if (StringUtils.isEmpty(type)) {
            log.debug("Event type is empty, using default key manager type");
            type = APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE;
        }
        EventHandler eventHandlerByType =
                ServiceReferenceHolder.getInstance().getEventHandlerByType(type);
        if (eventHandlerByType != null) {
            log.info("Processing event with handler type: " + type);
            eventHandlerByType.handleEvent(content, headers);
        } else {
            log.warn("No event handler found for type: " + type);
        }
    }

}
