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
import org.wso2.carbon.apimgt.impl.notification.exception.NotificationException;
import org.wso2.carbon.context.PrivilegedCarbonContext;


public abstract class Notifier implements Runnable {

    private NotificationDTO notificationDTO;
    private static final Log log = LogFactory.getLog(Notifier.class);
    private String tenantDomain;

    /**
     * Sends notifications implement your own logic to send different typeof notifications
     * @param notificationDTO
     * @throws NotificationException
     */
    public abstract void sendNotifications(NotificationDTO notificationDTO) throws NotificationException;


    @Override
    public void run() {
        try {
            setThreadLocalContxet(tenantDomain);
            sendNotifications(notificationDTO);
        }
        catch (Exception e) {
            log.error("Exception Occured during notification Sending ", e);
        }
    }

    public void setNotificationDTO(NotificationDTO notificationDTO) {
        this.notificationDTO = notificationDTO;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    /**
     * Setting Thread local variables
     * @param tenantDomain
     */
    public void setThreadLocalContxet(String tenantDomain){
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain,true);

    }

}


