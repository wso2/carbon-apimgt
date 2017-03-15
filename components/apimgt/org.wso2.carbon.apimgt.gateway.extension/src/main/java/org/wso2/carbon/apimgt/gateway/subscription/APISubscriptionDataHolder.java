/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.subscription;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class holds API Subscription data in each gateway node.
 */

public class APISubscriptionDataHolder {

    private static final Logger log = LoggerFactory.getLogger(APISubscriptionDataHolder.class);
    private static final int MAX_APIS = 50;
    private static final char DELIMITER = '@';
    private static APISubscriptionDataHolder instance = new APISubscriptionDataHolder();

    //key: API_CONTEXT$API_VERSION   value : Map<CONSUMER_KEY, SUBSCRIPTION_DATA_MAP>
    private Map<String, Map<String, Map<String, String>>> apiSubscriptionMap = Collections.synchronizedMap(
            new ExtendedLinkedHashMap());

    public static APISubscriptionDataHolder getInstance() {
        return instance;
    }

    /**
     * @param apiContext  API Context
     * @param apiVersion  API Version
     * @param consumerKey Consumer Key of Application
     * @return Subscription data map if a subscription is available, otherwise null
     */
    public Map<String, String> getApiSubscriptionPolicyIfAvailable(String apiContext, String apiVersion,
                                                                   String consumerKey) {
        Map<String, Map<String, String>> subscriptionMap = apiSubscriptionMap.get(apiContext + DELIMITER + apiVersion);
        if (subscriptionMap != null) {
            return subscriptionMap.get(consumerKey);
        }
        return null;
    }

    /**
     * Add new subscription to Subscription Map
     *
     * @param apiContext       API Context
     * @param apiVersion       API Version
     * @param consumerKey      Consumer Key of Application
     * @param subscriptionData Subscription Data Map
     */
    public void addApiSubscriptionToMap(String apiContext, String apiVersion, String consumerKey,
                                        Map<String, String> subscriptionData) {
        String apiKey = apiContext + DELIMITER + apiVersion;
        synchronized (apiKey.intern()) {
            if (apiSubscriptionMap.get(apiKey) == null) {
                Map<String, Map<String, String>> subscriptionsOfApi = new HashMap<>();
                subscriptionsOfApi.put(consumerKey, subscriptionData);
                apiSubscriptionMap.put(apiKey, subscriptionsOfApi);
            } else {
                apiSubscriptionMap.get(apiKey).put(consumerKey, subscriptionData);
            }
            if (log.isDebugEnabled()) {
                log.debug("Subscription entry added to Subscription Map. API: " + apiContext + ':' + apiVersion +
                        " Consumer Key: " + consumerKey + " Subscription Policy: " + subscriptionData);
            }
            //todo: remove this line
            log.info("Subscription entry added to Subscription Map. API: " + apiContext + ':' + apiVersion +
                    " Consumer Key: " + consumerKey + " Subscription Policy: " + subscriptionData);
        }
    }

    /**
     * Remove subscription from Subscription Map
     *
     * @param apiContext  API Context
     * @param apiVersion  API Version
     * @param consumerKey Consumer Key of Application
     */
    public void removeApiSubscriptionFromMap(String apiContext, String apiVersion, String consumerKey) {
        String apiKey = apiContext + DELIMITER + apiVersion;
        synchronized (apiKey.intern()) {
            Map<String, Map<String, String>> subscriptionsOfApi = apiSubscriptionMap.get(apiKey);
            if (subscriptionsOfApi != null) {
                subscriptionsOfApi.remove(consumerKey);
            }
            if (log.isDebugEnabled()) {
                log.debug("Subscription entry removed from Subscription Map. API: " + apiContext + ':' + apiVersion +
                        " Consumer Key: " + consumerKey);
            }
            //todo: remove this line
            log.info("Subscription entry removed from Subscription Map. API: " + apiContext + ':' + apiVersion +
                    " Consumer Key: " + consumerKey);
        }
    }

    private static class ExtendedLinkedHashMap extends LinkedHashMap<String, Map<String, Map<String, String>>> {

        private static final long serialVersionUID = 6103479545023548050L;

        ExtendedLinkedHashMap() {
            super(APISubscriptionDataHolder.MAX_APIS + 1, 1.0F, false);
        }

        // This method is called just after a new entry has been added
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_APIS;
        }
    }
}
