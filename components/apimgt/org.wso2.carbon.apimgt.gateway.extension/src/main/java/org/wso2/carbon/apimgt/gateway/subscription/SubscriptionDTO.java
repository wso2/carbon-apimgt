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

package org.wso2.carbon.apimgt.gateway.subscription;

import java.util.Objects;

/**
 * Model for data required for subscription validation
 */
public final class SubscriptionDTO {

    private String apiContext;
    private String apiName;
    private String apiVersion;
    private String apiProvider;
    private String consumerKey;
    private String subscriptionPolicy;
    private String applicationName;
    private String applicationOwner;
    private String keyAuthType;
    private String keyEnvType;

    public String getApiContext() {
        return apiContext;
    }

    public void setApiContext(String apiContext) {
        this.apiContext = apiContext;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getApiProvider() {
        return apiProvider;
    }

    public void setApiProvider(String apiProvider) {
        this.apiProvider = apiProvider;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getSubscriptionPolicy() {
        return subscriptionPolicy;
    }

    public void setSubscriptionPolicy(String subscriptionPolicy) {
        this.subscriptionPolicy = subscriptionPolicy;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationOwner() {
        return applicationOwner;
    }

    public void setApplicationOwner(String applicationOwner) {
        this.applicationOwner = applicationOwner;
    }

    public String getKeyAuthType() {
        return keyAuthType;
    }

    public void setKeyAuthType(String keyAuthType) {
        this.keyAuthType = keyAuthType;
    }

    public String getKeyEnvType() {
        return keyEnvType;
    }

    public void setKeyEnvType(String keyEnvType) {
        this.keyEnvType = keyEnvType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscriptionDTO)) {
            return false;
        }
        SubscriptionDTO that = (SubscriptionDTO) o;
        return Objects.equals(apiContext, that.apiContext) &&
                Objects.equals(apiName, that.apiName) &&
                Objects.equals(apiVersion, that.apiVersion) &&
                Objects.equals(apiProvider, that.apiProvider) &&
                Objects.equals(consumerKey, that.consumerKey) &&
                Objects.equals(subscriptionPolicy, that.subscriptionPolicy) &&
                Objects.equals(applicationName, that.applicationName) &&
                Objects.equals(applicationOwner, that.applicationOwner) &&
                Objects.equals(keyAuthType, that.keyAuthType) &&
                Objects.equals(keyEnvType, that.keyEnvType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(apiContext, apiName, apiVersion, apiProvider, consumerKey, subscriptionPolicy,
                applicationName, applicationOwner, keyAuthType, keyEnvType);
    }
}
