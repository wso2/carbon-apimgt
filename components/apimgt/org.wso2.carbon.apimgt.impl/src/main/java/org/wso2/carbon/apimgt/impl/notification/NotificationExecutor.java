/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.notification;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.Map;
import java.util.Properties;

public class NotificationExecutor {

    private static final Log log = LogFactory.getLog(NewAPIVersionEmailNotifier.class);

    /**
     * Executes the notifer classes in separtate threads.
     * @param notificationDTO
     * @throws NotificationException
     */
    public void sendAsyncNotifications(NotificationDTO notificationDTO) throws NotificationException {

        JSONObject notificationConfig;
        String notificationType = notificationDTO.getType();

        try {
                notificationConfig = APIUtil.getTenantConfig(notificationDTO.getTenantDomain());
                JSONArray notificationArray = (JSONArray) notificationConfig.get(NotifierConstants.Notifications_KEY);

                for (Object notification : notificationArray) {

                    JSONObject notificationJson = (JSONObject) notification;
                    String notifierType = (String) notificationJson.get(NotifierConstants.TYPE_KEY);
                    if (notificationType.equals(notifierType)) {
                        JSONArray notifierArray = (JSONArray) notificationJson.get("Notifiers");
                        for (Object notifier : notifierArray) {
                            JSONObject jsonNotifier = (JSONObject) notifier;
                            String notifierClass = (String) jsonNotifier.get("Class");

                            Map map = (Map) jsonNotifier;
                            Properties prop = notificationDTO.getProperties();
                            prop.putAll(map);
                            notificationDTO.setProperties(prop);

                            //starting Notifier threads
                            if (notifierClass != null && !notifierClass.isEmpty()) {

                                Notifier notfier = (Notifier) APIUtil.getClassInstance(notifierClass);
                                notfier.setNotificationDTO(notificationDTO);
                                notfier.setTenantDomain(notificationDTO.getTenantDomain());
                                Thread notificationThread = new Thread(notfier);
                                notificationThread.start();
                            }
                        }
                    }
                }

        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException | APIManagementException e) {
            throw new NotificationException("Error while Initializing the notifier class",e);
        }
    }

}
