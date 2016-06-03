/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.usage.client.dto;

public class APIListDTO implements Comparable{
    private int SubscriptionCount;
    private String apiName;
    private String apiVersion;
    private String apiProvider;

    public APIListDTO(int subscriptionCount, String apiName, String apiVersion, String apiProvider) {
        SubscriptionCount = subscriptionCount;
        this.apiName = apiName;
        this.apiVersion = apiVersion;
        this.apiProvider = apiProvider;
    }

    public int getSubscriptionCount() {
        return SubscriptionCount;
    }

    public void setSubscriptionCount(int subscriptionCount) {
        SubscriptionCount = subscriptionCount;
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

    @Override
    /**
     *  use to compare object using api provider and name
     */ public int compareTo(Object obj) {
        APIListDTO o = (APIListDTO) obj;
        if (this.apiName.equals(o.getApiName()) && this.apiProvider.equals(o.getApiProvider())) {
            return 0;
        }
        return -1;
    }
}
