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

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * This class is used to save all the notification related fields
 */
public class NotificationDTO {

    private String title;
    private String message;
    private String type;
    private int tenantID;
    private Properties properties;
    private Set<String> notifierSet;
    private String tenantDomain;

    public NotificationDTO(Properties properties, String type) {
        this.properties = properties;
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String subject) {
        this.title = subject;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public Object getProperty(String key) {
        return properties.get(key);
    }

    public int getTenantID() {
        return tenantID;
    }

    public void setTenantID(int tenantID) {
        this.tenantID = tenantID;
    }

    public Set<String> getNotifierSet() {
        return notifierSet;
    }

    public void setNotifierSet(Set<String> notifierSet) {
        this.notifierSet = notifierSet;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
