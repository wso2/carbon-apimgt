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

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.gateway.utils.WebhooksUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.WebhooksDTO;
import org.wso2.carbon.apimgt.impl.dto.WebhooksListDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.base.MultitenantConstants;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
This class holds the subscribers map of the tenant
 */
public class SubscriptionDataStore {

    private String tenantDomain = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
    public static final int retrievalRetries = 15;
    private static final Log log = LogFactory.getLog(SubscriptionDataStore.class);
    private Map<String, Map<String, WebhooksDTO>> subscribersMap;
    private Map<String, Boolean> throttlingStatusMap;
    ExecutorService executor = Executors.newSingleThreadExecutor();

    public SubscriptionDataStore(String tenantDomain) {

        this.tenantDomain = tenantDomain;
        initializeStore();
    }

    public SubscriptionDataStore() {
        initializeStore();
    }

    /**
     * This method is used to initilize a task to retrieve subscriptions.
     */
    private void initializeStore() {
        this.subscribersMap = new ConcurrentHashMap<>();
        this.throttlingStatusMap = new ConcurrentHashMap<>();
        executor.submit(() -> {
            List<WebhooksDTO> subscriptions = loadSubscriptions();
            for (WebhooksDTO subscriber: subscriptions) {
                String subscriptionKey = subscriber.getApiUUID() + "_" + subscriber.getTopicName();
                String throttleKey = subscriber.getAppID() + "_" + subscriber.getApiUUID();
                addSubscriber(subscriptionKey, subscriber);
                throttlingStatusMap.put(throttleKey, false);
            }
        });
    }

    /**
     * This method is used to add subscriber to the in memeory map.
     *
     * @param key               the subscription key (api key + topic name).
     * @param subscriber        the subscriber.
     */
    public void addSubscriber(String key, WebhooksDTO subscriber) {
        Map<String, WebhooksDTO> subscribersOfAPIMap = subscribersMap.get(key);
        if (subscribersOfAPIMap != null) {
            subscribersOfAPIMap.put(subscriber.getCallbackURL(), subscriber);
            subscribersMap.replace(key, subscribersOfAPIMap);
        } else {
            subscribersOfAPIMap = new HashMap<>();
            subscribersOfAPIMap.put(subscriber.getCallbackURL(), subscriber);
            subscribersMap.put(key, subscribersOfAPIMap);
        }
    }

    /**
     * This method is used to update the throttling status in the in memeory map.
     *
     * @param key           the throttling key (appID + apiUUID).
     * @param status        the subscriber.
     */
    public void updateThrottleStatus(String key, boolean status) {
        throttlingStatusMap.replace(key, status);
    }

    /**
     * This method is used to get the throttling status from the in memeory map.
     *
     * @param key           the throttling key (appID + apiUUID).
     * @return  status
     */
    public boolean getThrottleStatus(String key) {
        if (throttlingStatusMap.containsKey(key)) {
            return throttlingStatusMap.get(key);
        }
        return false;
    }

    /**
     * remove subscriber from the in memmory map.
     *
     * @param key               the subscription key (api key + topic name).
     * @param subscriber        the subscriber.
     */
    public void removeSubscriber(String key, WebhooksDTO subscriber) {
        List<WebhooksDTO> subscriberList = getSubscribers(key);
        if (subscriberList != null) {
            subscriberList.removeIf(existingSubscriber -> existingSubscriber.getCallbackURL().equals(
                    subscriber.getCallbackURL()));
        }
    }

    /**
     * This method is used to call the eventhub rest API to retrieve subscribers.
     *
     * @return the response body.
     */
    private String invokeService() {
        try {
            // The resource resides in the throttle web app. Hence reading throttle configs
            String url = WebhooksUtils.getEventHubConfiguration().getServiceUrl().concat(APIConstants.
                    INTERNAL_WEB_APP_EP).concat(APIConstants.Webhooks.GET_SUBSCRIPTIONS_URL);
            HttpGet method = new HttpGet(url);
            byte[] credentials = Base64.encodeBase64((WebhooksUtils.getEventHubConfiguration().getUsername() + ":" +
                    WebhooksUtils.getEventHubConfiguration().getPassword()).getBytes(StandardCharsets.UTF_8));
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            method.setHeader(APIConstants.HEADER_TENANT, tenantDomain);
            URL eventHubURL = new URL(url);
            int eventHubPort = eventHubURL.getPort();
            String eventHubProtocol = eventHubURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(eventHubPort, eventHubProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < retrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving webhooks subscription data from remote endpoint: " +
                                ex.getMessage() + ". Retrying after " + retrievalRetries + " seconds...");
                        Thread.sleep(retrievalRetries * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while (retry);
            return EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving webhooks subscription data from remote endpoint ", e);
            return null;
        }
    }

    /**
     * This method is used to load subscription list.
     *
     * @return the subscription list.
     */
    private List<WebhooksDTO> loadSubscriptions() {
        String responseString = invokeService();
        List<WebhooksDTO> subscriptions = new ArrayList<>();
        if (responseString != null && !responseString.isEmpty()) {
            subscriptions = new Gson().fromJson(responseString, WebhooksListDTO.class).getList();
        }
        return subscriptions;
    }

    /**
     * This method is used to get subscribers lists of a given API key.
     *
     * @param api the api key
     * @return the subscription list.
     */
    public List<WebhooksDTO> getSubscribers(String api) {
        Map<String, WebhooksDTO> subscribers = subscribersMap.get(api);
        if (subscribers != null) {
            List<WebhooksDTO> subscriberList = new ArrayList<WebhooksDTO>(subscribersMap.get(api).values());
            long now = Instant.now().toEpochMilli();
            subscriberList.removeIf(existingSubscriber -> existingSubscriber.getExpiryTime() != 0 &&
                    existingSubscriber.getExpiryTime() < now);
            return subscriberList;
        }
        return null;
    }
}
