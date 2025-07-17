/*
 *  Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.impl.dto.systemNotifications;

import org.wso2.carbon.apimgt.impl.systemNotifications.NotificationMetaData;
import org.wso2.carbon.apimgt.impl.systemNotifications.NotificationType;

import java.sql.Timestamp;
import java.util.List;

public class NotificationDTO {

    private String notificationId;
    private NotificationType notificationType;
    private Timestamp createdTime;
    private NotificationMetaData notificationMetadata;
    private List<NotificationEndUserDTO> endUsers;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public NotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public NotificationMetaData getNotificationMetadata() {
        return notificationMetadata;
    }

    public void setNotificationMetadata(NotificationMetaData notificationMetadata) {
        this.notificationMetadata = notificationMetadata;
    }

    public List<NotificationEndUserDTO> getEndUsers() {
        return endUsers;
    }

    public void setEndUsers(List<NotificationEndUserDTO> endUsers) {
        this.endUsers = endUsers;
    }
}
