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
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.NotifierConfigurations;
import org.wso2.carbon.apimgt.core.exception.NotificationException;
import org.wso2.carbon.apimgt.core.impl.Notifier;
import org.wso2.carbon.apimgt.core.impl.NotifierConstants;
import org.wso2.carbon.apimgt.core.template.dto.NotificationDTO;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.List;
import java.util.Map;
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

        List<NotifierConfigurations> notifierConfigurations = new APIMConfigurations().
                getNotificationConfigurations().getNewVersionNotifierConfiguration().getNotifierConfigurations();

        if (notificationDTO.getType().equalsIgnoreCase(NotifierConstants.NOTIFICATION_TYPE_NEW_VERSION)) {
            for (NotifierConfigurations listItem : notifierConfigurations) {
                String executorClass = listItem.getExecutorClass();
                Map property = listItem.getPropertyList();
                Properties prop = notificationDTO.getProperties();
                prop.putAll(property);
                notificationDTO.setProperties(prop);

                //starting Notifier threads
                if (executorClass != null && !executorClass.isEmpty()) {

                    Notifier notifier;
                    try {
                        notifier = (Notifier) APIUtils.getClassForName(executorClass).newInstance();
                    } catch (InstantiationException e) {
                        throw new NotificationException("Instantiation Error while Initializing the notifier class", e);
                    } catch (IllegalAccessException e) {
                        throw new NotificationException("IllegalAccess Error while Initializing the notifier class", e);
                    } catch (ClassNotFoundException e) {
                        throw new NotificationException("ClassNotFound Error while Initializing the notifier class", e);
                    }
                    notifier.setNotificationDTO(notificationDTO);
                    executor.execute(notifier);
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Class " + executorClass + " Empty Or Null");
                    }
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Notification Type Does Not match with " + NotifierConstants.NOTIFICATION_TYPE_NEW_VERSION);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
    }

}
