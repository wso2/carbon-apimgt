/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.core.executors;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.NotificationException;
import org.wso2.carbon.apimgt.core.impl.Notifier;
import org.wso2.carbon.apimgt.core.impl.NotifierConstants;
import org.wso2.carbon.apimgt.core.template.dto.NotificationDTO;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Used to execute notification threads.
 */
public class NotificationExecutor {

    private static final Log log = LogFactory.getLog(NotificationExecutor.class);
    ExecutorService executor = Executors.newFixedThreadPool(10);

    /**
     * Executes the notifer classes in separtate threads.
     *
     * @param notificationDTO
     * @throws org.wso2.carbon.apimgt.core.exception.NotificationException
     */
    public void sendAsyncNotifications(NotificationDTO notificationDTO) throws NotificationException {

        String notificationTemplate = new APIMConfigurations().
                getNotificationConfigurations().getNewVersionNotifierConfiguration().getNotifierTemplate();
        if (notificationDTO.getType().equalsIgnoreCase(NotifierConstants.NOTIFICATION_TYPE_NEW_VERSION)) {
            JSONParser parser = new JSONParser();
            JSONObject notificationConfig;
            try {
                notificationConfig = (JSONObject) parser.parse(notificationTemplate);
            } catch (ParseException e) {
                throw new NotificationException("Error while Parsing Object", e);
            }
            JSONArray notifierArray = (JSONArray) notificationConfig.get(NotifierConstants.NOTIFIER);

            for (Object notifierObject : notifierArray) {
                JSONObject notifierJson = (JSONObject) notifierObject;
                String notifierClass = (String) notifierJson.get(NotifierConstants.CLASS);

                Properties prop = notificationDTO.getProperties();
                prop.putAll(notifierJson);
                notificationDTO.setProperties(prop);

                //starting Notifier threads
                if (notifierClass != null && !notifierClass.isEmpty()) {

                    Notifier notifier;
                    try {
                        notifier = (Notifier) APIUtils.getClassForName(notifierClass).newInstance();
                    } catch (InstantiationException e) {
                        throw new NotificationException("Instantiation Error while Initializing the notifier class", e);
                    } catch (IllegalAccessException e) {
                        throw new NotificationException("IllegalAccess Error while Initializing the notifier class", e);
                    } catch (ClassNotFoundException e) {
                        throw new NotificationException("ClassNotFound Error while Initializing the notifier class", e);
                    }
                    notifier.setNotificationDTO(notificationDTO);
                    Runnable notificationThread = notifier;
                    executor.execute(notificationThread);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Class" + notifierClass + "Empty Or Null");
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Notification Type Does Not match with" + NotifierConstants.NOTIFICATION_TYPE_NEW_VERSION);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }
}

