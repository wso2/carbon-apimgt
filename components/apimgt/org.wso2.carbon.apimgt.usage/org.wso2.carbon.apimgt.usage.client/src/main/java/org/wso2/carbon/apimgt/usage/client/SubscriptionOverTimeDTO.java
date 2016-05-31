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
package org.wso2.carbon.apimgt.usage.client;
/**
 * This class is used as a DTO to represent develoers over time
 */
public class SubscriptionOverTimeDTO {

    private int subscription_count;
    private long created_time;
    private String api_name;
    private String api_version;

    public SubscriptionOverTimeDTO(int subscription_count, long created_time, String api_name, String api_version) {
        this.subscription_count = subscription_count;
        this.created_time = created_time;
        this.api_name = api_name;
        this.api_version = api_version;
    }

    public int getSubscription_count() {
        return subscription_count;
    }

    public void setSubscription_count(int subscription_count) {
        this.subscription_count = subscription_count;
    }

    public long getCreated_time() {
        return created_time;
    }

    public void setCreated_time(long created_time) {
        this.created_time = created_time;
    }

    public String getApi_name() {
        return api_name;
    }

    public void setApi_name(String api_name) {
        this.api_name = api_name;
    }

    public String getApi_version() {
        return api_version;
    }

    public void setApi_version(String api_version) {
        this.api_version = api_version;
    }
}
