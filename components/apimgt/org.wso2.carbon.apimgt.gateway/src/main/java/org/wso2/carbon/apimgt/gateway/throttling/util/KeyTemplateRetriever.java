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


import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

public class KeyTemplateRetriever implements Runnable {
    private static final Log log = LogFactory.getLog(KeyTemplateRetriever.class);

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
            ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = ServiceReferenceHolder
                    .getInstance().getThrottleProperties().getBlockCondition();
            String url = blockConditionRetrieverConfiguration.getServiceUrl() + "/keyTemplates";
            byte[] credentials = Base64.encodeBase64((blockConditionRetrieverConfiguration.getUsername() + ":" +
                                                      blockConditionRetrieverConfiguration.getPassword()).getBytes
                    (StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(method);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                JSONArray jsonArray = (JSONArray) new JSONParser().parse(responseString);
                return (String[]) jsonArray.toArray(new String[jsonArray.size()]);
            }
        } catch (IOException e) {
            log.error("Exception when retrieving throttling data from remote endpoint ", e);
        } catch (ParseException e) {
            log.error("Exception when retrieving throttling data from remote endpoint ", e);
        }
        return null;
    }


    public void loadKeyTemplatesFromWebService() {

        List keyListMap = Arrays.asList(retrieveKeyTemplateData());
        ServiceReferenceHolder.getInstance().getThrottleDataHolder().getKeyTemplateMap().clear();
        ServiceReferenceHolder.getInstance().getThrottleDataHolder().getKeyTemplateMap().putAll(
                GatewayUtils.generateMap(keyListMap));


    }

    public void startKeyTemplateDataRetriever() {

        ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = ServiceReferenceHolder
                .getInstance().getThrottleProperties().getBlockCondition();

        ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor
                (blockConditionRetrieverConfiguration.getCorePoolSize());

        scheduledExecutorService.scheduleAtFixedRate(new KeyTemplateRetriever(),
                                                     blockConditionRetrieverConfiguration.getInitDelay(),
                                                     blockConditionRetrieverConfiguration.getPeriod(),
                                                     TimeUnit.MILLISECONDS);
    }


}
