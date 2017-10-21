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
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.gateway.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class BlockingConditionRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(BlockingConditionRetriever.class);
    private static final int blockConditionsDataRetrievalTimeoutInSeconds = 15;
    private static final int blockConditionsDataRetrievalRetries = 15;

    @Override
    public void run() {
        if (log.isDebugEnabled()) {
            log.debug("Starting web service based data retrieving process.");
        }
        loadBlockingConditionsFromWebService();
    }

    /**
     * This method will retrieve blocking conditions by calling a WebService.
     *
     * @return String object array which contains throttled keys.
     */
    private BlockConditionsDTO retrieveBlockConditionsData() {

        try {
            ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = getThrottleProperties().getBlockCondition();
            String url = blockConditionRetrieverConfiguration.getServiceUrl() + "/block";
            byte[] credentials = Base64.encodeBase64((blockConditionRetrieverConfiguration.getUsername() + ":" +
                                                      blockConditionRetrieverConfiguration.getPassword()).getBytes
                    (StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            URL keyMgtURL = new URL(url);
            int keyMgtPort = keyMgtURL.getPort();
            String keyMgtProtocol = keyMgtURL.getProtocol();
            HttpClient httpClient = APIUtil.getHttpClient(keyMgtPort, keyMgtProtocol);
            HttpResponse httpResponse = null;
            int retryCount = 0;
            boolean retry;
            do {
                try {
                    httpResponse = httpClient.execute(method);
                    retry = false;
                } catch (IOException ex) {
                    retryCount++;
                    if (retryCount < blockConditionsDataRetrievalRetries) {
                        retry = true;
                        log.warn("Failed retrieving Blocking Conditions from remote endpoint: " + ex.getMessage()
                                 + ". Retrying after " + blockConditionsDataRetrievalTimeoutInSeconds + " seconds...");
                        Thread.sleep(blockConditionsDataRetrievalTimeoutInSeconds * 1000);
                    } else {
                        throw ex;
                    }
                }
            } while(retry);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, BlockConditionsDTO.class);
            }
        } catch (IOException | InterruptedException e) {
            log.error("Exception when retrieving Blocking Conditions from remote endpoint ", e);
        }
        return null;
    }

    protected ThrottleProperties getThrottleProperties() {
        return ServiceReferenceHolder
                .getInstance().getThrottleProperties();
    }

    public void loadBlockingConditionsFromWebService() {
        BlockConditionsDTO blockConditionsDTO = retrieveBlockConditionsData();
        if (blockConditionsDTO != null) {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().addAPIBlockingConditionsFromMap(
                    GatewayUtils.generateMap(blockConditionsDTO.getApi()));
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().addApplicationBlockingConditionsFromMap(
                    GatewayUtils.generateMap(blockConditionsDTO.getApplication()));
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().addUserBlockingConditionsFromMap(
                    GatewayUtils.generateMap(blockConditionsDTO.getUser()));
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().addIplockingConditionsFromMap(
                    GatewayUtils.generateMap(blockConditionsDTO.getIp()));
        }
    }


    public void startWebServiceThrottleDataRetriever() {

        new Timer().schedule(this, getThrottleProperties().getBlockCondition().getInitDelay());
    }
}
