/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model.subscription;

/**
 * Entity for representing a SubscriptionDTO in APIM
 */
public class Subscription implements CacheableEntity<String> {

    private int subscriptionId;
    private String subscriptionUUID;
    private String policyId = null;
    private int apiId;
    private String apiUUID;
    private int appId;
    private String applicationUUID;
    private String subscriptionState = null;

    public int getSubscriptionId() {

        return subscriptionId;
    }

    public void setSubscriptionId(int subscriptionId) {

        this.subscriptionId = subscriptionId;
    }

    public String getPolicyId() {

        return policyId;
    }

    public void setPolicyId(String policyId) {

        this.policyId = policyId;
    }

    public int getApiId() {

        return apiId;
    }

    public void setApiId(int apiId) {

        this.apiId = apiId;
    }

    public int getAppId() {

        return appId;
    }

    public void setAppId(int appId) {

        this.appId = appId;
    }

    public String getSubscriptionState() {

        return subscriptionState;
    }

    public void setSubscriptionState(String subscriptionState) {

        this.subscriptionState = subscriptionState;
    }

    @Override
    public String getCacheKey() {

        return getSubscriptionCacheKey(getAppId(), getApiId());
    }

    private static String getSubscriptionCacheKey(int appId, int apiId) {

        return appId + DELEM_PERIOD + apiId;
    }

    public String getSubscriptionUUID() {

        return subscriptionUUID;
    }

    public void setSubscriptionUUID(String subscriptionUUID) {

        this.subscriptionUUID = subscriptionUUID;
    }

    public String getApiUUID() {

        return apiUUID;
    }

    public void setApiUUID(String apiUUID) {

        this.apiUUID = apiUUID;
    }

    public String getApplicationUUID() {

        return applicationUUID;
    }

    public void setApplicationUUID(String applicationUUID) {

        this.applicationUUID = applicationUUID;
    }
}
