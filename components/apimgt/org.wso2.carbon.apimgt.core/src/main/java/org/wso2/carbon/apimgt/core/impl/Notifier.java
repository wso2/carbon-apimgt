/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.template.dto.NotificationDTO;

/**
 * Abstract Notifier class.
 */
public abstract class Notifier implements Runnable {

    private NotificationDTO notificationDTO;
    private static final Log log = LogFactory.getLog(Notifier.class);

    /**
     * Sends notifications implement logic to send different typeof notifications.
     *
     * @param notificationDTO
     * @throws APIManagementException
     */
    public abstract void sendNotifications(NotificationDTO notificationDTO)
            throws APIManagementException;


    @Override
    public void run() {
        try {
            sendNotifications(notificationDTO);
        } catch (APIManagementException e) {
            log.error("Exception Occurred during notification Sending ", e);
        }
    }

    public void setNotificationDTO(NotificationDTO notificationDTO) {
        this.notificationDTO = notificationDTO;
    }

}


