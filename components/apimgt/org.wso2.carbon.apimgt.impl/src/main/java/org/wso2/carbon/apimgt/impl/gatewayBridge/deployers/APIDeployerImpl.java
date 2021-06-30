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

package org.wso2.carbon.apimgt.impl.gatewayBridge.deployers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayBridge.dto.WebhookSubscriptionDTO;
import org.wso2.carbon.apimgt.impl.gatewayBridge.webhooks.WebhookSubscriptionGetService;
import org.wso2.carbon.apimgt.impl.gatewayBridge.webhooks.WebhookSubscriptionGetServiceImpl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Deploy apis in external gateways.
 */
public class APIDeployerImpl implements APIDeployer {
    List<WebhookSubscriptionDTO> subscriptionsList = new ArrayList<>();
    private static final Log log = LogFactory.getLog(APIDeployerImpl.class);


    /**
     * Sending gateway artifacts to subscribed gateways.
     * Retrieves subscription list from database and
     * send webhooks.
     * @param gatewayAPIDTO     the API DTO contains API details
     * @param subscriberName             the topic subscribed
     */
    @Override
    public void deployArtifacts(GatewayAPIDTO gatewayAPIDTO, String subscriberName, RuntimeArtifactDto runtimeArtifactDto) throws Exception {

        WebhookSubscriptionGetService webhookSubscriptionGetService = new WebhookSubscriptionGetServiceImpl();
        subscriptionsList = webhookSubscriptionGetService.getWebhookSubscription(subscriberName);
        Iterator<WebhookSubscriptionDTO> iterator = subscriptionsList.iterator();
        HttpPost post;

        while (iterator.hasNext()) {

            post = new HttpPost(iterator.next().getCallback());

            String artifactsArray = runtimeArtifactDto.getArtifact().toString();
            String payload = null;

            JSONArray artifacts = new JSONArray(artifactsArray);
            for (int i = 0; i < artifacts.length(); i++) {
                JSONObject obj = artifacts.getJSONObject(i);
                if (obj.has("data")) {
                    JSONObject dataObj =obj.getJSONObject("data");
                    if (dataObj.has("id")) {
                        if (dataObj.getString("id").equals(gatewayAPIDTO.getApiId())) {
                            payload = dataObj.toString();
                        }
                    }
                }
            }
            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("name", gatewayAPIDTO.getName()));
            urlParameters.add(new BasicNameValuePair("version", gatewayAPIDTO.getVersion()));
            urlParameters.add(new BasicNameValuePair("provider", gatewayAPIDTO.getProvider()));
            urlParameters.add(new BasicNameValuePair("apiId", gatewayAPIDTO.getApiId()));
            if (payload != null) {
                urlParameters.add(new BasicNameValuePair("apiDefinition", payload));
            }

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                String statusCode = "code: " + response.getStatusLine().getStatusCode();
                log.info(statusCode);
                log.debug(EntityUtils.toString(response.getEntity()));
            }

        }
    }

    @Override
    public void unDeployArtifacts(GatewayAPIDTO gatewayAPIDTO, String subscriberName) throws Exception {

        WebhookSubscriptionGetService webhookSubscriptionGetService = new WebhookSubscriptionGetServiceImpl();
        subscriptionsList = webhookSubscriptionGetService.getWebhookSubscription(subscriberName);
        Iterator<WebhookSubscriptionDTO> iterator = subscriptionsList.iterator();
        HttpPost post;

        while (iterator.hasNext()) {
            post = new HttpPost(iterator.next().getCallback());

            List<NameValuePair> urlParameters = new ArrayList<>();
            urlParameters.add(new BasicNameValuePair("apiId", gatewayAPIDTO.getApiId()));
            urlParameters.add(new BasicNameValuePair("name", gatewayAPIDTO.getName()));
            urlParameters.add(new BasicNameValuePair("version", gatewayAPIDTO.getVersion()));

            post.setEntity(new UrlEncodedFormEntity(urlParameters));

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(post)) {

                String statusCode = "code: " + response.getStatusLine().getStatusCode();
                log.info(statusCode);
                log.debug(EntityUtils.toString(response.getEntity()));

            }
        }
    }

}
