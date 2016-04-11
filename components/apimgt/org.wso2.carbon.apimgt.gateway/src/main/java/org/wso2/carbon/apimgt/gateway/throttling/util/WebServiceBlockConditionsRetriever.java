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
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WebServiceBlockConditionsRetriever implements Runnable {
    private static final Log log = LogFactory.getLog(WebServiceBlockConditionsRetriever.class);

    @Override
    public void run() {
        if(log.isDebugEnabled()){
            log.debug("Starting web service based block condition data retrieving process.");
        }
        loadBlockConditionsFromWebService();
    }


    /**
     * This method will retrieve throttled events from service deployed in global policy server.
     *
     * @return String object array which contains throttled keys.
     */
    private BlockConditionsDTO retrieveBlockConditionsData() {

        try {
            ThrottleProperties.BlockCondition blockConditionRetrieverConfiguration = ServiceReferenceHolder
                    .getInstance().getThrottleProperties().getBlockCondition();
            String url = blockConditionRetrieverConfiguration.getServiceUrl();
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
            log.error("Exception when retrieving throttling data from remote endpoint ", e);
        }
        return null;
    }


    /**
     * This method will call throttle data web service deployed in central plocy engine at server loading time.
     * Then it will update local throttle data map with the results obtained. This need to be fine tuned as large
     * number of results can slow down web service call. However this need to be controlled from server side and
     * this client will add all recieved events to local map. Even if we missed few events from this call it will
     * eventually update as missed events go to global policy engine and decision will be anyway pushed to topic and
     * all subscriber will notify it
     */
    public void loadBlockConditionsFromWebService() {
        BlockConditionsDTO blockConditionsDTO = retrieveBlockConditionsData();
        if (blockConditionsDTO != null) {

        }
    }

    public void startWebServiceBlockConditionDataRetriever() {
        new Thread(this).start();
    }
}
