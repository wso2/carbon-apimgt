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

import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import org.wso2.carbon.apimgt.impl.dto.EventHubConfigurationDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class})
public class KeyTemplateRetrieverTest {

    @Test
    public void run() throws Exception {
        Map map = new HashMap();
        map.put("$userId","$userId");
        map.put("$apiContext","$apiContext");
        map.put("$apiVersion","$apiVersion");
        String content = "[\"$userId\",\"$apiContext\",\"$apiVersion\"]";
        PowerMockito.mockStatic(APIUtil.class);
        HttpClient httpClient = Mockito.mock(HttpClient.class);
        CloseableHttpResponse httpResponse = Mockito.mock(CloseableHttpResponse.class);
        BasicHttpEntity httpEntity = new BasicHttpEntity();
        httpEntity.setContent(new ByteArrayInputStream(content.getBytes()));
        Mockito.when(httpResponse.getEntity()).thenReturn(httpEntity);
        Mockito.when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        Mockito.when(APIUtil.executeHTTPRequestWithRetries(Mockito.any(HttpGet.class), Mockito.any(HttpClient.class)))
                .thenReturn(httpResponse);
        StatusLine status = Mockito.mock(StatusLine.class);
        Mockito.when(status.getStatusCode()).thenReturn(200);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(status);
        BDDMockito.given(APIUtil.getHttpClient(Mockito.anyInt(),Mockito.anyString())).willReturn(httpClient);

        EventHubConfigurationDto eventHubConfigurationDto = new EventHubConfigurationDto();
        eventHubConfigurationDto.setUsername("admin");
        eventHubConfigurationDto.setPassword("admin".toCharArray());
        eventHubConfigurationDto.setEnabled(true);
        eventHubConfigurationDto.setServiceUrl("http://localhost:18084/internal/data/v1");
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        KeyTemplateRetriever keyTemplateRetriever = new KeyTemplateRetrieverWrapper(eventHubConfigurationDto,
                throttleDataHolder);
        keyTemplateRetriever.run();
        Map<String,String> keyTemplateMap = throttleDataHolder.getKeyTemplateMap();
        Assert.assertNotNull(keyTemplateMap);
        Assert.assertEquals(map,keyTemplateMap);
    }
}