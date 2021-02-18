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
     * @param appID             the application ID of the subscriber.
     * @param tenantDomain      the tenant domain.
     * @param callback          the subscriber's callback url.
     * @param topicName         the subscriber's topic name.
     * @param secret            the subscriber's secret key.
     * @param expiredAt         the expiry timestamp in millis.
     */
    public void addSubscription(String apiKey, String appID, String tenantDomain, String callback, String secret,
                                String topicName, long expiredAt) {
        String subscriptionKey = apiKey + "_" + topicName;
        WebhooksDTO subscriber = new WebhooksDTO();
        subscriber.setAppID(appID);
        subscriber.setCallbackURL(callback);
        subscriber.setSecret(secret);
        subscriber.setExpiryTime(expiredAt);
        SubscriptionDataStore dataStore = WebhooksDataHolder.getInstance()
                .getTenantSubscriptionStore(tenantDomain);
        dataStore.addSubscriber(subscriptionKey, subscriber);
    }

    /**
     * This method is used to remove subscription from the datastore.
     *
     * @param apiKey            the api key to uniquely identify the API.
     * @param appID             the application ID of the subscriber.
     * @param tenantDomain      the tenant domain.
     * @param callback          the subscriber's callback url.
     * @param topicName         the subscriber's topic name.
     * @param secret            the subscriber's secret key.
     */
    public void removeSubscription(String apiKey, String appID, String tenantDomain, String callback, String secret,
                                   String topicName) {
        String subscriptionKey = apiKey + "_" + topicName;
        WebhooksDTO subscriber = new WebhooksDTO();
        subscriber.setCallbackURL(callback);
        subscriber.setAppID(appID);
        subscriber.setSecret(secret);
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
}
