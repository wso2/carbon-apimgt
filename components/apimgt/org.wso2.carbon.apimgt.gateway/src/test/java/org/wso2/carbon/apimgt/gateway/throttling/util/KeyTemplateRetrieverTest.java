/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;

import java.util.HashMap;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class KeyTemplateRetrieverTest {
    @Rule
    public WireMockRule wireMockRule;
    public static WireMockConfiguration wireMockConfiguration = new WireMockConfiguration();

    @Test
    public void run() throws Exception {
        Map map = new HashMap();
        map.put("$userId","$userId");
        map.put("$apiContext","$apiContext");
        map.put("$apiVersion","$apiVersion");

        wireMockRule = new WireMockRule(wireMockConfiguration);
        wireMockRule.stubFor(WireMock.get(urlEqualTo("/throttle/data/v1/keyTemplates"))
                .withBasicAuth("admin", "admin").willReturn(aResponse()
                        .withStatus(201).withHeader("Content-Type", "application/json")
                        .withBody("[\"$userId\",\"$apiContext\",\"$apiVersion\"]")));
        wireMockRule.start();
        ThrottleProperties throttleProperties = new ThrottleProperties();
        ThrottleProperties.BlockCondition blockCondition = new ThrottleProperties.BlockCondition();
        blockCondition.setUsername("admin");
        blockCondition.setPassword("admin");
        blockCondition.setEnabled(true);
        blockCondition.setServiceUrl("http://localhost:" + wireMockConfiguration.portNumber() + "/throttle/data/v1");
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        throttleProperties.setBlockCondition(blockCondition);
        KeyTemplateRetriever keyTemplateRetriever = new KeyTemplateRetrieverWrapper(throttleProperties,
                throttleDataHolder);
        keyTemplateRetriever.run();
        wireMockRule.resetAll();
        wireMockRule.stop();
        Map<String,String> keyTemplateMap = throttleDataHolder.getKeyTemplateMap();
        Assert.assertNotNull(keyTemplateMap);
        Assert.assertEquals(map,keyTemplateMap);
        // Check KeyTemplateRetrieving for
        //keyTemplateRetriever.run();
    }
}