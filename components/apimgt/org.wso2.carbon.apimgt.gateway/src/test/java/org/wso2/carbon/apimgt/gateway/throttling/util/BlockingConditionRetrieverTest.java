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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class BlockingConditionRetrieverTest {
    @Rule
    public WireMockRule wireMockRule;
    public static WireMockConfiguration wireMockConfiguration = new WireMockConfiguration().port(8083);

    @Test
    public void run() throws Exception {
        wireMockRule = new WireMockRule(wireMockConfiguration);
        wireMockRule.stubFor(WireMock.get(urlEqualTo("/throttle/data/v1/block"))
                .withBasicAuth("admin", "admin").willReturn(aResponse()
                        .withStatus(200).withHeader("Content-Type", "application/json")
                        .withBody("{\"api\":[\"/pizzashack/1.0.0\"],\"application\":[\"admin:DefaultApplication\"]," +
                                "\"ip\":[\"carbon.super:carbon.super:127.0.0.1\"],\"user\":[\"admin\"]," +
                                "\"custom\":[]}")));
        wireMockRule.start();
        ThrottleProperties throttleProperties = new ThrottleProperties();
        ThrottleProperties.BlockCondition blockCondition = new ThrottleProperties.BlockCondition();
        blockCondition.setUsername("admin");
        blockCondition.setPassword("admin");
        blockCondition.setEnabled(true);
        blockCondition.setServiceUrl("http://localhost:8083/throttle/data/v1");
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        throttleProperties.setBlockCondition(blockCondition);
        BlockingConditionRetriever blockingConditionRetriever = new BlockingConditionRetrieverWrapper
                (throttleProperties,
                throttleDataHolder);
        blockingConditionRetriever.run();
        wireMockRule.resetAll();
        wireMockRule.stop();
        Assert.assertTrue(throttleDataHolder.isRequestBlocked("/pizzashack/1.0.0", "admin:DefaultApplication",
                "admin", "127.0.0.1"));
    }
}