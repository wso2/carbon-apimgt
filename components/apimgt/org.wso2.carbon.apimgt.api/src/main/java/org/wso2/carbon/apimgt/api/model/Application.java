/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represent the Application in api model
 */
public class Application {

    private int id;
    private String name;
    private String uuid;
    private Subscriber subscriber;
    private Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI>();
    private List<APIKey> keys = new ArrayList<APIKey>();
    private Map<String, Map<String,OAuthApplicationInfo>> keyManagerWiseOAuthApp = new HashMap<>();

    public Map<String, Map<String, OAuthApplicationInfo>> getKeyManagerWiseOAuthApp() {

        return keyManagerWiseOAuthApp;
    }

    private String tier;
    private String tierQuotaType;
    private String callbackUrl;
    private String description;
    private String status;
    private String groupId;
    private Boolean isBlackListed;
    private String owner;
    private Map<String, String> applicationAttributes = new HashMap<String, String>();
    private String createdTime;
    private String lastUpdatedTime;
    private String tokenType;
    private String keyType;
    private int subscriptionCount;
    private String keyManager;
    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    /**
     * Holds workflow status
     **/
    private String applicationWorkFlowStatus;

    public Application(String name, Subscriber subscriber) {
        this.name = name;
        this.subscriber = subscriber;
    }

    public Application(int appId) {
        id = appId;
        this.subscriber = new Subscriber(null);
    }

    public Application(String uuid) {
        this.uuid = uuid;
        this.subscriber = new Subscriber(null);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Subscriber getSubscriber() {
        return subscriber;
    }

    public Set<SubscribedAPI> getSubscribedAPIs() {
        return subscribedAPIs;
    }

    public void setSubscribedAPIs(Set<SubscribedAPI> subscribedAPIs) {
        this.subscribedAPIs = subscribedAPIs;
    }

    public void addSubscribedAPIs(Set<SubscribedAPI> subscribedAPIs) {
        for (SubscribedAPI subscribedAPI : subscribedAPIs) {
            subscribedAPI.setApplication(this);
        }
        this.subscribedAPIs.addAll(subscribedAPIs);
    }

    public void removeSubscribedAPIs(Set<SubscribedAPI> subscribedAPIs) {
        this.subscribedAPIs.removeAll(subscribedAPIs);
    }

    public Map<String, String> getApplicationAttributes() {

        return applicationAttributes;
    }

    public void setApplicationAttributes(Map<String, String> applicationAttributes) {

        this.applicationAttributes = applicationAttributes;
    }



    public Map<String, OAuthApplicationInfo> getOAuthApp(String keyType) {
        return keyManagerWiseOAuthApp.get(keyType);
    }

    public void addOAuthApp(String keyType,String keyManager,OAuthApplicationInfo oAuthApplicationInfo){

        Map<String, OAuthApplicationInfo> keyTypeWiseOauthApp = keyManagerWiseOAuthApp.getOrDefault(keyType,
                new HashMap<>());
        keyTypeWiseOauthApp.put(keyManager,oAuthApplicationInfo);
        keyManagerWiseOAuthApp.put(keyType,keyTypeWiseOauthApp);
    }

    public OAuthApplicationInfo getOAuthApp(String keyType, String keyManager) {

        Map<String, OAuthApplicationInfo> keyManagerWiseOauthAPP =
                keyManagerWiseOAuthApp.getOrDefault(keyType, new HashMap<>());
        return keyManagerWiseOauthAPP.get(keyManager);
    }
    public void clearOAuthApps() {
        keyManagerWiseOAuthApp.clear();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<APIKey> getKeys() {
        return keys;
    }

    public void addKey(APIKey key) {
        keys.add(key);
    }

    public String getTier() {
        return tier;
    }

    public void setTier(String tier) {
        this.tier = tier;
    }


    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Application that = (Application) o;
        return id == that.id && name.equals(that.name) && subscriber.equals(that.subscriber);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + Integer.valueOf(id).hashCode();
        result = 31 * result + subscriber.hashCode();
        return result;
    }

    public String getApplicationWorkFlowStatus() {
        return applicationWorkFlowStatus;
    }

    public void setApplicationWorkFlowStatus(String applicationWorkFlowStatus) {
        this.applicationWorkFlowStatus = applicationWorkFlowStatus;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getUUID() {
        return uuid;
    }

    public Boolean getIsBlackListed() {
        return isBlackListed;
    }

    public void setIsBlackListed(Boolean isBlackListed) {
        this.isBlackListed = isBlackListed;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }

    public void setSubscriptionCount(int subscriptionCount) {
        this.subscriptionCount = subscriptionCount;
    }

    public int getSubscriptionCount() {
        return subscriptionCount;
    }

    public void updateSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    public String getTierQuotaType() {
        return tierQuotaType;
    }

    public void setTierQuotaType(String tierQuotaType) {
        this.tierQuotaType = tierQuotaType;
    }

    public String getKeyManager() {

        return keyManager;
    }

    public void setKeyManager(String keyManager) {

        this.keyManager = keyManager;
    }
}
