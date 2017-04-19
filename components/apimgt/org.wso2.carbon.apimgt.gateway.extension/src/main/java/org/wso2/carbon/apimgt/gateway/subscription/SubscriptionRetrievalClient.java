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

import feign.Feign;
import feign.Param;
import feign.RequestLine;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

/**
 * Http client util for subscription validation.
 */
public final class SubscriptionRetrievalClient {

    private static final String DEFAULT_APIM_CORE_BASE_URL = "https://localhost:9292";

    private SubscriptionRetrievalService subscriptionRetrievalService = null;

    public SubscriptionRetrievalClient(String apimCoreBaseUrl) {
        subscriptionRetrievalService = Feign.builder()
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(SubscriptionRetrievalService.class, apimCoreBaseUrl);
    }

    public SubscriptionRetrievalClient() {
        this(DEFAULT_APIM_CORE_BASE_URL);
    }

    private interface SubscriptionRetrievalService {
        @RequestLine("GET /subscriptions?limit={limit}")
        SubscriptionListDTO getSubscriptions(@Param("limit") int limit);

        @RequestLine("GET /subscriptions?context={context}&version={version}")
        SubscriptionListDTO getSubscriptions(@Param("context") String context, @Param("version") String version);
    }

    SubscriptionListDTO loadSubscriptions(int limit) {
        return subscriptionRetrievalService.getSubscriptions(limit);
    }

    SubscriptionListDTO loadSubscriptionsOfApi(String apiContext, String apiVersion) {
        return subscriptionRetrievalService.getSubscriptions(apiContext, apiVersion);
    }

}
