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

package org.wso2.carbon.apimgt.impl.portalNotifications;

import java.sql.Timestamp;
import java.util.List;

public class PortalNotificationDTO {

    private String notificationId;
    private PortalNotificationType notificationType;
    private Timestamp createdTime;
    private PortalNotificationMetaData notificationMetadata;
    private List<PortalNotificationEndUserDTO> endUsers;

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String notificationId) {
        this.notificationId = notificationId;
    }

    public PortalNotificationType getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(PortalNotificationType notificationType) {
        this.notificationType = notificationType;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }

    public PortalNotificationMetaData getNotificationMetadata() {
        return notificationMetadata;
    }

    public void setNotificationMetadata(PortalNotificationMetaData notificationMetadata) {
        this.notificationMetadata = notificationMetadata;
    }

    public List<PortalNotificationEndUserDTO> getEndUsers() {
        return endUsers;
    }

    public void setEndUsers(List<PortalNotificationEndUserDTO> endUsers) {
        this.endUsers = endUsers;
    }
}
