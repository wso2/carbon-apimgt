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

    //key: API_CONTEXT$API_VERSION   value : HashMap<CONSUMER_KEY, SUBSCRIPTION_POLICY>
    private Map<String, HashMap<String, String>> apiSubscriptionMap = Collections.synchronizedMap(
            new ExtendedLinkedHashMap());

    public static APISubscriptionDataHolder getInstance() {
        return instance;
    }

    /**
     * @param apiContext  API Context
     * @param apiVersion  API Version
     * @param consumerKey Consumer Key of Application
     * @return Policy name if a subscription is available, otherwise null
     * @throws APIManagementException
     */
    public String getApiSubscriptionPolicyIfAvailable(String apiContext, String apiVersion, String consumerKey) {
        HashMap<String, String> subscriptionMap = apiSubscriptionMap.get(apiContext + DELIMITER + apiVersion);
        if (subscriptionMap != null) {
            return subscriptionMap.get(consumerKey);
        }
        return null;
    }

    /**
     * Add new subscription to Subscription Map
     *
     * @param apiContext  API Context
     * @param apiVersion  API Version
     * @param consumerKey Consumer Key of Application
     * @param policy      Subscription Policy
     * @throws APIManagementException
     */
    public void addApiSubscriptionToMap(String apiContext, String apiVersion, String consumerKey, String policy) {
        String apiKey = apiContext + DELIMITER + apiVersion;
        synchronized (apiKey.intern()) {
            if (apiSubscriptionMap.get(apiKey) == null) {
                HashMap<String, String> subscriptionsOfApi = new HashMap<>();
                subscriptionsOfApi.put(consumerKey, policy);
                apiSubscriptionMap.put(apiKey, subscriptionsOfApi);
            } else {
                apiSubscriptionMap.get(apiKey).put(consumerKey, policy);
            }
            if (log.isDebugEnabled()) {
                log.debug("Subscription entry added to Subscription Map. API: " + apiContext + ':' + apiVersion +
                        " Consumer Key: " + consumerKey + " Subscription Policy: " + policy);
            }
            //todo: remove this line
            log.info("Subscription entry added to Subscription Map. API: " + apiContext + ':' + apiVersion +
                    " Consumer Key: " + consumerKey + " Subscription Policy: " + policy);
        }
    }

    /**
     * Remove subscription from Subscription Map
     *
     * @param apiContext  API Context
     * @param apiVersion  API Version
     * @param consumerKey Consumer Key of Application
     * @throws APIManagementException
     */
    public void removeApiSubscriptionFromMap(String apiContext, String apiVersion, String consumerKey) {
        String apiKey = apiContext + DELIMITER + apiVersion;
        synchronized (apiKey.intern()) {
            HashMap<String, String> subscriptionsOfApi = apiSubscriptionMap.get(apiKey);
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

    private static class ExtendedLinkedHashMap extends LinkedHashMap<String, HashMap<String, String>> {

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
