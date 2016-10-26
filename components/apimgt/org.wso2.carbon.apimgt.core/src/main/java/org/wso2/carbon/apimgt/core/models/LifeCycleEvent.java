/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.core.models;

import java.util.Date;

/**
 * This method contains the content of API lifecycle
 */
public class LifeCycleEvent {

    private APIIdentifier api;
    private String oldStatus;
    private String newStatus;
    private String userId;
    private Date date = new Date();

    public APIIdentifier getApi() {
        return api;
    }

    public void setApi(APIIdentifier api) {
        this.api = api;
    }

    public String getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(APIStatus oldStatus) {
        this.oldStatus = oldStatus.toString();
    }

    public void setOldStatus(String oldStatus) {
        this.oldStatus = oldStatus;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(APIStatus newStatus) {
        this.newStatus = newStatus.toString();
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
