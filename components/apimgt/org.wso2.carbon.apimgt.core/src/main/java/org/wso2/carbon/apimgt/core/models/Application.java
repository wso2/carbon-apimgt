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


import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * This class represent the Application in api model
 */
public final class Application {
    private String name;
    private String uuid;
    private String description;
    private Policy policy;
    private String status;
    private String createdUser;
    private Instant createdTime;
    private String updatedUser;
    private Instant updatedTime;
    private List<OAuthApplicationInfo> applicationKeys;
    private ApplicationToken applicationToken;
    private String permissionString;
    private HashMap permissionMap;


    public Application(String name, String createdUser) {
        this.name = name;
        this.createdUser = createdUser;
        this.applicationKeys = new ArrayList<>();
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

    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedUser() {
        return updatedUser;
    }

    public void setUpdatedUser(String updatedUser) {
        this.updatedUser = updatedUser;
    }

    public Instant getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Instant updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Policy getPolicy() {
        return policy;
    }

    public void setPolicy(Policy policy) {
        this.policy = policy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<OAuthApplicationInfo> getApplicationKeys() {
        return applicationKeys;
    }

    public void setApplicationKeys(List<OAuthApplicationInfo> applicationKeys) {
        this.applicationKeys = applicationKeys;
    }

    public void addApplicationKeys(OAuthApplicationInfo oAuthApplicationInfo) {
        applicationKeys.add(oAuthApplicationInfo);
    }

    public ApplicationToken getApplicationToken() {
        return applicationToken;
    }

    public void setApplicationToken(ApplicationToken applicationToken) {
        this.applicationToken = applicationToken;
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
                Objects.equals(uuid, that.uuid) &&
                Objects.equals(description, that.description) &&
                Objects.equals(status, that.status) &&
                Objects.equals(permissionString, that.permissionString) &&
                Objects.equals(createdUser, that.createdUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, uuid, description,  status, createdUser);
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
        return "Application [name=" + name + ", uuid=" + uuid + ", policyId=" +
                ", description=" + description + ", status=" + status + ", createdUser="
                + createdUser + ", createdTime=" + createdTime + ", updatedUser="
                + updatedUser + ", updatedTime=" + updatedTime + ", numberOfKeys=" + applicationKeys.size()
                + ", permissionString=" + permissionString + ", permissionMap=" + permissionMap + "]";
    }
}
