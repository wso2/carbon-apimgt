/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.persistence.dto;

import java.util.Date;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.PublisherAPI. Minimal API information required only for
 * listing APIs in publisher which are stored in the
 * persistence layer are included in this.
 */
public class PublisherAPIInfo {
    private String id;
    private String apiName;
    private String version;
    private String providerName;
    private String context;
    private String status;
    private String type;
    private String thumbnail; // thumbnail is not required for normal ApiGet
    private String createdTime;
    private Date updatedTime;

    public String getApiName() {
        return apiName;
    }

    public String getVersion() {
        return version;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getCreatedTime() { return createdTime; }

    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public Date getUpdatedTime() { return updatedTime; }

    public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }

    @Override
    public String toString() {
        return "PublisherAPIInfo [id=" + id + ", apiName=" + apiName + ", version=" + version + ", providerName="
                + providerName + ", context=" + context + ", status=" + status + ", type=" + type + ", thumbnail="
                + thumbnail + "]";
    }
}
