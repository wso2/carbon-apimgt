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


import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * This class represent the Application in api model
 */
public final class Application {
    private String name;
    private String id;
    private Subscriber subscriber;
    private Set<APISubscription> apiSubscriptions = new LinkedHashSet<APISubscription>();
    private String groupId;
    private String uuid;
    private String description;
    private String tier;
    private String status;
    private String callbackUrl;
    private List<APIKey> keys = new ArrayList<APIKey>();


    public Application(String name, Subscriber subscriber) {
        this.name = name;
        this.subscriber = subscriber;
    }

    public String getName() {
        return name;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public Set<APISubscription> getAPISubscriptions() {
        return apiSubscriptions;
    }

    public void addAPISubscriptions(Set<APISubscription> apiSubscriptions) {
        this.apiSubscriptions.addAll(apiSubscriptions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Application that = (Application) o;
        return name.equals(that.name) && subscriber.equals(that.subscriber);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + subscriber.hashCode();
        return result;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
}
