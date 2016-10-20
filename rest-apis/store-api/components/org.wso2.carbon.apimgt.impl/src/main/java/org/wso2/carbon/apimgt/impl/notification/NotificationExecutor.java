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
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;

public class NotificationExecutor{

    private static final Log log = LogFactory.getLog(NewAPIVersionEmailNotifier.class);

    /**
     * Executes the notifer classes in separtate threads.
     * @param notificationDTO
     * @throws NotificationException
     */
    public void sendAsyncNotifications(NotificationDTO notificationDTO) throws NotificationException {

        Registry registry = null;
        int tenantId = notificationDTO.getTenantID();
        String content = null;
        JSONObject notificationConfig;
        String notificationType = notificationDTO.getType();

        try {
            registry = ServiceReferenceHolder.getInstance().getRegistryService().getConfigSystemRegistry
                    (tenantId);
            if (registry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)) {
                Resource resource = registry.get(APIConstants.API_TENANT_CONF_LOCATION);
                content = new String((byte[]) resource.getContent(), Charset.defaultCharset());
            }

            if (content != null) {
                JSONParser parser = new JSONParser();
                notificationConfig = (JSONObject) parser.parse(content);
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

                                Notifier notfier = (Notifier) APIUtil.getClassForName(notifierClass).newInstance();
                                notfier.setNotificationDTO(notificationDTO);
                                notfier.setTenantDomain(notificationDTO.getTenantDomain());
                                Thread notificationThread = new Thread(notfier);
                                notificationThread.start();
                            }
                        }
                    }
                }
            }

        } catch (IllegalAccessException e) {
            throw new NotificationException("Error while Initializing the notifier class",e);
        } catch (InstantiationException e) {
            throw new NotificationException("Error while Initializing the notifier class",e);
        } catch (ClassNotFoundException e) {
            throw new NotificationException("Error while Initializing the notifier class",e);
        } catch (RegistryException e) {
            throw new NotificationException("Error while Reading notification Configuration",e);
        } catch (ParseException e) {
            throw new NotificationException("Error while passing notification Configuration",e);
        }
    }
}
