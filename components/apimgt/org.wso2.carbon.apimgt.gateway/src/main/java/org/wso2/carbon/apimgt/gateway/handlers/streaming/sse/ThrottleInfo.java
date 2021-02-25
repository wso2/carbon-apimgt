/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.streaming.sse;

import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * Holds information related to throttling for the purpose of re use in case of sse apis response event flow.
 */
public class ThrottleInfo {

    private String apiName;
    private String apiVersion;
    private String apiContext;

    private String tier;
    private String apiTier;
    private String resourceTier;

    private String subscriptionLevelThrottleKey;
    private String applicationLevelThrottleKey;
    private String apiLevelThrottleKey;
    private String resourceLevelThrottleKey;

    private String applicationName;
    private String applicationId;
    private String applicationTier;

    private String subscriberTenantDomain;
    private String authorizedUser;
    private String subscriber;
    private String type;

    private String remoteIp;

    public ThrottleInfo(AuthenticationContext context, String apiContext, String version,
                        String resourceLevelThrottleKey, String resourceTier, String remoteIp) {

        this.apiName = context.getApiName();
        this.apiVersion = version;
        this.apiContext = apiContext;
        this.tier = context.getTier();
        this.apiTier = context.getApiTier();
        this.resourceTier = resourceTier;
        this.applicationName = context.getApplicationName();
        this.applicationId = context.getApplicationId();
        this.applicationTier = context.getApplicationTier();
        this.subscriberTenantDomain = context.getSubscriberTenantDomain();
        this.subscriber = context.getSubscriber();
        this.type = context.getKeyType();
        this.remoteIp = remoteIp;
        this.authorizedUser = subscriber;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(subscriberTenantDomain)) {
            authorizedUser += "@" + subscriberTenantDomain;
        }
        this.subscriptionLevelThrottleKey = applicationId + ":" + apiContext + ":" + apiVersion;
        this.applicationLevelThrottleKey = applicationId + ":" + authorizedUser;
        this.apiLevelThrottleKey = apiContext + ":" + apiVersion;
        this.resourceLevelThrottleKey = resourceLevelThrottleKey;
    }

    public String getApplicationTier() {
        return applicationTier;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public String getTier() {
        return tier;
    }

    public String getType() {
        return type;
    }

    public String getApiTier() {
        return apiTier;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getSubscriberTenantDomain() {
        return subscriberTenantDomain;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public String getApiName() {
        return apiName;
    }

    public String getApiContext() {
        return apiContext;
    }

    public String getRemoteIp() {
        return remoteIp;
    }

    public String getResourceLevelThrottleKey() {
        return resourceLevelThrottleKey;
    }

    public String getResourceTier() {
        return resourceTier;
    }

    public String getAuthorizedUser() {
        return authorizedUser;
    }

    public String getSubscriptionLevelThrottleKey() {
        return subscriptionLevelThrottleKey;
    }

    public String getApplicationLevelThrottleKey() {
        return applicationLevelThrottleKey;
    }

    public String getApiLevelThrottleKey() {
        return apiLevelThrottleKey;
    }
}
