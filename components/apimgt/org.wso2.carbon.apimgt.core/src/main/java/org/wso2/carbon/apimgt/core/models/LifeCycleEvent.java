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

    private String id;
    private String oldStatus;
    private String newStatus;
    private String userId;
    private Date date = new Date();


    public String getId() {
        return id;
    }

    public String getOldStatus() {
        return oldStatus;
    }



    public String getNewStatus() {
        return newStatus;
    }

    public String getUserId() {
        return userId;
    }


    public Date getDate() {
        return new Date(date.getTime());
    }

    public LifeCycleEvent(String id, String oldStatus, String newStatus, String userId, Date date) {
        this.id = id;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.userId = userId;
        this.date = new Date(date.getTime());
    }
}
