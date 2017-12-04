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
package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * Class to hold Notification configuration parameters
 */
@Configuration(description = "Notification Configurations")
public class NotificationConfigurations {
    @Element(description = "NotificationEnable")
    private Boolean notificationEnable = false;

    @Element(description = "Notification Configurations")
    private MailConfigurations mailConfigurations = new MailConfigurations();

    @Element(description = "Notification Configurations")
    private NewVersionNotifierConfigurations newVersionNotifierConfigurations = new NewVersionNotifierConfigurations();

    public Boolean getNotificationEnable() {
        return notificationEnable;
    }

    public void setNotificationEnable(Boolean notificationEnable) {
        this.notificationEnable = notificationEnable;
    }

    public MailConfigurations getMailConfigurations() {
        return mailConfigurations;
    }

    public void setMailConfigurations(MailConfigurations mailConfigurations) {
        this.mailConfigurations = mailConfigurations;
    }

    public NewVersionNotifierConfigurations getNewVersionNotifierConfiguration() {
        return newVersionNotifierConfigurations;
    }

    public void setNewVersionNotifierConfiguration(NewVersionNotifierConfigurations newVersionNotifierConfiguration) {
        this.newVersionNotifierConfigurations = newVersionNotifierConfiguration;
    }
}
