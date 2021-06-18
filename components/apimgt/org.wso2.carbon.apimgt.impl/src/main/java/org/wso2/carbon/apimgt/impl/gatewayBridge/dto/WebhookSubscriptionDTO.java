/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.gatewayBridge.dto;

import java.io.Serializable;

/**
 * Class for representing the webhook subscription.
 */
public class WebhookSubscriptionDTO implements Serializable {

    private static final long serialVersionUID = 1L;
    private String subscriberName;
    private String callback;
    private String vHost;
    private long updatedTime;
    private long expiryTime;

    public WebhookSubscriptionDTO() {
    }

    public WebhookSubscriptionDTO(String subscriberName, String callback, String vHost) {
        this.subscriberName = subscriberName;
        this.callback = callback;
        this.vHost = vHost;
        this.expiryTime = 10;
    }

    public WebhookSubscriptionDTO(String subscriberName, String callback, String vHost, long expiryTime) {
        this.subscriberName = subscriberName;
        this.callback = callback;
        this.vHost = vHost;
        this.expiryTime = expiryTime;
    }

    public String getvHost() {
        return vHost;
    }

    public void setvHost(String vHost) {
        this.vHost = vHost;
    }

    public String getSubscriberName() {
        return subscriberName;
    }

    public void setSubscriberName(String subscriberName) {
        this.subscriberName = subscriberName;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }
}

