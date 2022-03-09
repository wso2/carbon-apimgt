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
package org.wso2.carbon.apimgt.gateway.throttling.util;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class KeyTemplateRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(KeyTemplateRetriever.class);
    private static final int keyTemplateRetrievalTimeoutInSeconds = 15;
    private static final int keyTemplateRetrievalRetries = 15;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Starting web service based block condition data retrieving process.");
        }
        loadKeyTemplatesFromWebService();
    }


    /**
     * This method will retrieve KeyTemplates
     *
     * @return String object array which contains Blocking conditions.
     */
    private String[] retrieveKeyTemplateData() {

        try {
            String url = getEventHubConfiguration().getServiceUrl().concat(APIConstants.INTERNAL_WEB_APP_EP).concat(
                    "/keyTemplates");
            byte[] credentials = Base64.encodeBase64(
                    (getEventHubConfiguration().getUsername() + ":" + getEventHubConfiguration().getPassword())
                            .getBytes(StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry = true;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        retry = false;
                    }
                } catch (IOException ex) {
                    if (retryCount >= keyTemplateRetrievalRetries) {
                        throw ex;
                    }else{
                        log.warn("Failed retrieving throttling data from remote endpoint: " + ex.getMessage()
                                + ". Retrying after " + keyTemplateRetrievalTimeoutInSeconds + " seconds...");
                    }
                }
                if (retry) {
                    if (retryCount < keyTemplateRetrievalRetries) {
                        log.warn("Failed retrieving throttling data from remote endpoint. Retrying after "
                                + keyTemplateRetrievalTimeoutInSeconds + " seconds...");
                        Thread.sleep(keyTemplateRetrievalTimeoutInSeconds * 1000);
                    } else {
                        retry = false;
                    }
                    retryCount++;
                }
            } while (retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                Object jsonObject = new JSONParser().parse(responseString);
                if (jsonObject instanceof JSONArray) {
                    JSONArray jsonArray = (JSONArray) new JSONParser().parse(responseString);
                    return (String[]) jsonArray.toArray(new String[jsonArray.size()]);
                } else {
                    log.error("Invalid throttling data response: " + responseString);
                }
            }
        } catch (IOException | InterruptedException | ParseException e) {
            log.error("Exception when retrieving throttling data from remote endpoint ", e);
        }
        return null;
    }

    protected EventHubConfigurationDto getEventHubConfiguration() {
        return ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getEventHubConfigurationDto();
    }


    public void loadKeyTemplatesFromWebService() {
        List keyListMap = Arrays.asList(retrieveKeyTemplateData());
        if (!keyListMap.isEmpty()) {
            getThrottleDataHolder().addKeyTemplateFromMap(GatewayUtils.generateMap(keyListMap));
        }
    }

    protected ThrottleDataHolder getThrottleDataHolder() {
        return ServiceReferenceHolder.getInstance().getThrottleDataHolder();
    }

    public void startKeyTemplateDataRetriever() {

        new Timer().schedule(this, getEventHubConfiguration().getInitDelay());
    }


}
