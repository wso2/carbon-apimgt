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

/**
 * Model for data required for subscription validation
 */
public final class SubscriptionValidationData {

    private String apiContext;
    private String apiName;
    private String apiVersion;
    private String apiProvider;
    private String consumerKey;
    private String subscriptionPolicy;
    private String keyEnvType;
    private String applicationId;
    private String status;

    public SubscriptionValidationData(String apiContext, String apiVersion, String consumerKey) {
        this.apiContext = apiContext;
        this.apiVersion = apiVersion;
        this.consumerKey = consumerKey;
    }

    public String getApiContext() {
        return apiContext;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public String getSubscriptionPolicy() {
        return subscriptionPolicy;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiProvider() {
        return apiProvider;
    }

    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
    }

    public void setSubscriptionPolicy(String subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }

    public String getKeyEnvType() {
        return keyEnvType;
    }

    public void setKeyEnvType(String keyEnvType) {
        this.keyEnvType = keyEnvType;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
