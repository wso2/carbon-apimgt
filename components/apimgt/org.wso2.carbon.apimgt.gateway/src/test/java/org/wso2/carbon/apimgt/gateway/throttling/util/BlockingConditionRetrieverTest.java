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

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;

@RunWith (PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class BlockingConditionRetrieverTest {
    @Test
    public void run() throws Exception {
        String content = "{\"api\":[\"/pizzashack/1.0.0\"],\"application\":[\"admin:DefaultApplication\"]," +
                "\"ip\":[{\"fixedIp\":\"127.0.0.1\",\"invert\":false,\"type\":\"IP\",\"tenantDomain\":\"carbon" +
                ".super\"}],\"user\":[\"admin\"],\"custom\":[]}";
        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream(content.getBytes()));
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        BDDMockito.given(APIUtil.getHttpClient(Mockito.anyInt(),Mockito.anyString())).willReturn(httpClient);
        ThrottleProperties throttleProperties = new ThrottleProperties();
        ThrottleProperties.BlockCondition blockCondition = new ThrottleProperties.BlockCondition();
        blockCondition.setUsername("admin");
        blockCondition.setPassword("admin");
        blockCondition.setEnabled(true);
        blockCondition.setServiceUrl("http://localhost:18083/internal/data/v1");
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        throttleProperties.setBlockCondition(blockCondition);
        BlockingConditionRetriever blockingConditionRetriever = new BlockingConditionRetrieverWrapper
                (throttleProperties,
                throttleDataHolder);
        blockingConditionRetriever.run();
        Assert.assertTrue(throttleDataHolder.isRequestBlocked("/pizzashack/1.0.0", "admin:DefaultApplication",
                "admin", "127.0.0.1", "carbon.super"));
    }
}