/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import java.util.Date;

/**
 * This class represent the model for API comments
 */
public class Comment {

    private String id;
    private String text;
    private String user;
    private Date createdTime;
    private String updatedBy;
    private Date updatedTime;
    private String apiId;
    private String parentCommentID;
    private String entryPoint;
    private String category;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {this.text = text; }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {return updatedBy;    }

    public void setUpdatedBy(String updatedBy) {this.updatedBy = updatedBy;  }

    public Date getUpdatedTime() {return updatedTime; }

    public void setUpdatedTime(Date updatedTime) {this.updatedTime = updatedTime; }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getParentCommentID() {return parentCommentID; }

    public void setParentCommentID(String parentCommentID) {
        this.parentCommentID = parentCommentID;
    }

    public String getEntryPoint() {return entryPoint; }

    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }

    public String getCategory() {return category; }

    public void setCategory(String category) {
        this.category = category;
    }

    
}
