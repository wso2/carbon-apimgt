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
import org.wso2.carbon.apimgt.gateway.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;

public class BlockingConditionRetriever extends TimerTask {
    private static final Log log = LogFactory.getLog(BlockingConditionRetriever.class);

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
            ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = ServiceReferenceHolder
                    .getInstance().getThrottleProperties().getBlockCondition();
            String url = blockConditionRetrieverConfiguration.getServiceUrl() + "/block";
            byte[] credentials = Base64.encodeBase64((blockConditionRetrieverConfiguration.getUsername() + ":" +
                                                      blockConditionRetrieverConfiguration.getPassword()).getBytes
                    (StandardCharsets.UTF_8));
            HttpGet method = new HttpGet(url);
            method.setHeader("Authorization", "Basic " + new String(credentials, StandardCharsets.UTF_8));
            HttpClient httpClient = HttpClientBuilder.create().build();
            HttpResponse httpResponse = httpClient.execute(method);

            String responseString = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            if (responseString != null && !responseString.isEmpty()) {
                return new Gson().fromJson(responseString, BlockConditionsDTO.class);
            }
        } catch (IOException e) {
            log.error("Exception when retrieving Blocking Conditions from remote endpoint ", e);
        }
        return null;
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

        new Timer().schedule(this, ServiceReferenceHolder
                .getInstance().getThrottleProperties().getBlockCondition().getInitDelay());
    }
}
