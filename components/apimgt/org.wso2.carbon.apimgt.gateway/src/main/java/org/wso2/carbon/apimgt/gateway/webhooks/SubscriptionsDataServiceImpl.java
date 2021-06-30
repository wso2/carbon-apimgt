/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.gateway.webhooks;

import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;
import org.wso2.carbon.apimgt.impl.webhooks.SubscriptionsDataService;

import java.util.List;

/**
 * Implementation of  {@code SubscriptionsDataService}. This class provides access to subscription data.
 */
public class SubscriptionsDataServiceImpl implements SubscriptionsDataService {

    /**
     * This method is used to add subscription to the data store.
     *
     * @param apiKey            the api key to uniquely identify the API.
     * @param topicName         the subscriber's topic name.
     * @param tenantDomain      the tenant domain.
     * @param subscriber        the webhooks subscriber.
     */
    public void addSubscription(String apiKey, String topicName, String tenantDomain, WebhooksDTO subscriber) {
        String subscriptionKey = apiKey + "_" + topicName;
        SubscriptionDataStore dataStore = WebhooksDataHolder.getInstance()
                .getTenantSubscriptionStore(tenantDomain);
        dataStore.addSubscriber(subscriptionKey, subscriber);
    }

    /**
     * This method is used to remove subscription from the datastore.
     *
     * @param apiKey            the api key to uniquely identify the API.
     * @param tenantDomain      the tenant domain.
     * @param topicName         the subscriber's topic name.
     * @param subscriber        the webhooks subscriber.
     */
    public void removeSubscription(String apiKey, String topicName, String tenantDomain, WebhooksDTO subscriber) {
        String subscriptionKey = apiKey + "_" + topicName;
        SubscriptionDataStore dataStore = WebhooksDataHolder.getInstance()
                .getTenantSubscriptionStore(tenantDomain);
        dataStore.removeSubscriber(subscriptionKey, subscriber);
    }

    /**
     * This method is used to retrieve list of subscriptions.
     *
     * @param apiKey            the api key to uniquely identify the API.
     * @param tenantDomain      the tenant domain.
     * @return the subscriptions list.
     */
    public List<WebhooksDTO> getSubscriptionsList(String apiKey, String tenantDomain) {
        SubscriptionDataStore dataStore = WebhooksDataHolder.getInstance()
                .getTenantSubscriptionStore(tenantDomain);
        return dataStore.getSubscribers(apiKey);
    }

    /**
     * This method is used to update throttling status.
     *
     * @param appID             the application ID.
     * @param apiUUID           the API UUID.
     * @param tenantDomain      the tenant domain.
     * @param status            the status
     */
    public void updateThrottleStatus(String appID, String apiUUID, String tenantDomain, boolean status) {
        SubscriptionDataStore dataStore = WebhooksDataHolder.getInstance()
                .getTenantSubscriptionStore(tenantDomain);
        String throttleKey = appID + "_" + apiUUID;
        dataStore.updateThrottleStatus(throttleKey, status);
    }

    /**
     * This method is used to update throttling status.
     *
     * @param appID             the application ID.
     * @param apiUUID           the API UUID.
     * @param tenantDomain      the tenant domain.
     * return throttling status
     */
    public boolean getThrottleStatus(String appID, String apiUUID, String tenantDomain) {
        SubscriptionDataStore dataStore = WebhooksDataHolder.getInstance()
                .getTenantSubscriptionStore(tenantDomain);
        String throttleKey = appID + "_" + apiUUID;
        return dataStore.getThrottleStatus(throttleKey);
    }
}
