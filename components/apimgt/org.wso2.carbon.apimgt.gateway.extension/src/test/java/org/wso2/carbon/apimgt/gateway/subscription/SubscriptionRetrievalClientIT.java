/*
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
 */

package org.wso2.carbon.apimgt.gateway.subscription;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class SubscriptionRetrievalClientIT {

    private static final int APIM_REST_API_PORT = 9292;
    private static final int TEST_PORT_OFFSET = 500;

    private WireMockServer wireMockServer;
    private JsonArray subscriptions;
    private JsonObject subscription;
    private String apimCoreBaseUrl;

    @BeforeClass
    public void init() {
        apimCoreBaseUrl = "http://localhost:" + getRestApiPort();
        wireMockServer = new WireMockServer(options().port(getRestApiPort()));
        wireMockServer.start();
        configureFor("127.0.0.1", getRestApiPort());

        subscriptions = new JsonArray();
        for (int i = 0; i < 3; i++) {
            JsonObject subscription = new JsonObject();
            subscription.addProperty("apiContext", "/test" + i);
            subscription.addProperty("apiName", "TestAPI" + i);
            subscription.addProperty("apiVersion", i + ".0.0");
            subscription.addProperty("apiProvider", "admin" + i);
            subscription.addProperty("consumerKey", "1234-5678-90" + i);
            subscription.addProperty("subscriptionPolicy", "Gold" + i);
            subscription.addProperty("applicationName", "MyApp" + i);
            subscription.addProperty("applicationOwner", "admin" + i);
            subscription.addProperty("keyEnvType", "Production" + i);
            subscriptions.add(subscription);
        }

        JsonObject subscriptionList = new JsonObject();
        subscriptionList.add("list", subscriptions);

        stubFor(get(urlPathEqualTo("/subscriptions"))
                .withQueryParam("limit", equalTo("-1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(subscriptionList.toString())));

        stubFor(get(urlPathEqualTo("/subscriptions"))
                .withQueryParam("limit", equalTo("0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"list\":[]}")));

        subscription = new JsonObject();
        subscription.addProperty("apiContext", "/test");
        subscription.addProperty("apiName", "TestAPI");
        subscription.addProperty("apiVersion", "1.0.0");
        subscription.addProperty("apiProvider", "admin");
        subscription.addProperty("consumerKey", "1234-5678-90");
        subscription.addProperty("subscriptionPolicy", "Gold");
        subscription.addProperty("applicationName", "MyApp");
        subscription.addProperty("applicationOwner", "admin");
        subscription.addProperty("keyEnvType", "Production");

        stubFor(get(urlPathEqualTo("/subscriptions"))
                .withQueryParam("context", equalTo("/test"))
                .withQueryParam("version", equalTo("1.0.0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(subscription.toString())));
    }

    @Test
    public void testLoadAllSubscriptions() {
        //load all subscriptions (limit = -1)
        SubscriptionListDTO subscriptionList = new SubscriptionRetrievalClient(apimCoreBaseUrl).loadSubscriptions(-1);
        for (SubscriptionDTO subscriptionDTO : subscriptionList.getSubscriptions()) {
            Gson gson = new Gson();
            if (subscriptionDTO.getApiName().equals(
                    ((JsonObject) this.subscriptions.get(0)).get("apiName").getAsString())) {
                Assert.assertEquals(subscriptionDTO, gson.fromJson(subscriptions.get(0), SubscriptionDTO.class));
            } else if (subscriptionDTO.getApiName().equals(
                    ((JsonObject) this.subscriptions.get(1)).get("apiName").getAsString())) {
                Assert.assertEquals(subscriptionDTO, gson.fromJson(subscriptions.get(1), SubscriptionDTO.class));
            } else if (subscriptionDTO.getApiName().equals(
                    ((JsonObject) this.subscriptions.get(2)).get("apiName").getAsString())) {
                Assert.assertEquals(subscriptionDTO, gson.fromJson(subscriptions.get(2), SubscriptionDTO.class));
            } else {
                Assert.fail("Invalid subscription found!");
            }
        }

    }

    @Test
    public void testLoadSubscriptionsWithLimit0() {

        //load n subscriptions (limit = n). Currently, this only supports 0
        SubscriptionListDTO subscriptionList = new SubscriptionRetrievalClient(apimCoreBaseUrl).loadSubscriptions(0);
        Assert.assertEquals(subscriptionList.getSubscriptions().size(), 0, "Subscription count should be zero, " +
                "but received " + subscriptions.size());

    }

    @Test
    public void testLoadSubscriptionsOfApi() {
        //load all subscriptions of api test/1.0.0
        SubscriptionListDTO subsList = new SubscriptionRetrievalClient(apimCoreBaseUrl)
                .loadSubscriptionsOfApi("/test", "1.0.0");
        for (SubscriptionDTO subscriptionDTO : subsList.getSubscriptions()) {
            Assert.assertEquals(subscriptionDTO, new Gson().fromJson(subscription, SubscriptionDTO.class));
        }
    }

    @AfterClass
    public void clean() {
        wireMockServer.resetAll();
        wireMockServer.stop();
    }

    public static int getRestApiPort() {
        return APIM_REST_API_PORT + TEST_PORT_OFFSET;
    }

}
