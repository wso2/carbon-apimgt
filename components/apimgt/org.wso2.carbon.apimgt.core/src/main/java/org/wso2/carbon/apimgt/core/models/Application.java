/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This class represent the Application in api model
 */
public final class Application {
    private String name;
    private String groupId;
    private String uuid;
    private String policyId;
    private String description;
    private String tier;
    private String status;
    private String callbackUrl;
    private String createdUser;
    private LocalDateTime createdTime;
    private String updatedUser;
    private LocalDateTime updatedTime;
    private List<APIKey> keys;
    private String permissionString;
    private HashMap permissionMap;


    public Application(String name, String createdUser) {
        this.name = name;
        this.createdUser = createdUser;
        keys = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return uuid;
    }

    public void setId(String uuid) {
        this.uuid = uuid;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedUser() {
        return updatedUser;
    }

    public void setUpdatedUser(String updatedUser) {
        this.updatedUser = updatedUser;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public List<APIKey> getKeys() {
        return keys;
    }

    public void addKey(APIKey key) {
        keys.add(key);
    }

    public String getPolicyId() {
        return policyId;
    }

    public void setPolicyId(String policyId) {
        this.policyId = policyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Application)) {
            return false;
        }
        Application that = (Application) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(groupId, that.groupId) &&
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(policyId, that.policyId) &&
                Objects.equals(description, that.description) &&
                Objects.equals(tier, that.tier) &&
                Objects.equals(status, that.status) &&
                Objects.equals(callbackUrl, that.callbackUrl) &&
                Objects.equals(permissionString, that.permissionString) &&
                Objects.equals(createdUser, that.createdUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, groupId, uuid, policyId, description, tier, status, callbackUrl, createdUser);
    }

    public String getPermissionString() {
        return permissionString;
    }

    public void setPermissionString(String permissionString) {
        this.permissionString = permissionString;
    }

    public HashMap getPermissionMap() {
        return permissionMap;
    }

    public void setPermissionMap(HashMap permissionMap) {
        this.permissionMap = permissionMap;
    }

    @Override
    public String toString() {
        return "Application [name=" + name + ", groupId=" + groupId + ", uuid=" + uuid + ", policyId=" + policyId
                + ", description=" + description + ", tier=" + tier + ", status=" + status + ", callbackUrl="
                + callbackUrl + ", createdUser=" + createdUser + ", createdTime=" + createdTime + ", updatedUser="
                + updatedUser + ", updatedTime=" + updatedTime + ", keys=" + keys + ", permissionString="
                + permissionString + ", permissionMap=" + permissionMap + "]";
    }    
}
