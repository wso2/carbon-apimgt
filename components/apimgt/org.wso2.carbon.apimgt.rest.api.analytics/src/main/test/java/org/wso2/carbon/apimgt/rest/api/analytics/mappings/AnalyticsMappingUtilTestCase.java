/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.analytics.mappings;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;
import org.wso2.carbon.apimgt.rest.api.analytics.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APICountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.APISubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.SubscriptionInfoListDTO;

import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil.epochToISO8601DateTime;

/**
 * Test cases for Analytics Mapping Util methods.
 */
public class AnalyticsMappingUtilTestCase {

    @Test
    public void fromApplicationCountToListDTOTest() {
        List<ApplicationCount> applicationCountList = new ArrayList<>();
        applicationCountList.add(SampleTestObjectCreator.createRandomApplicationCountObject());
        applicationCountList.add(SampleTestObjectCreator.createRandomApplicationCountObject());
        applicationCountList.add(SampleTestObjectCreator.createRandomApplicationCountObject());
        ApplicationCountListDTO applicationCountListDTO = AnalyticsMappingUtil.
                fromApplicationCountToListDTO(applicationCountList, ZoneOffset.UTC);

        Assert.assertEquals(applicationCountList.size(), applicationCountListDTO.getList().size());
        for (int i = 0; i < applicationCountList.size(); i++) {
            Assert.assertEquals(epochToISO8601DateTime(applicationCountList.get(i).getTimestamp(),
                    ZoneOffset.UTC), applicationCountListDTO.getList().get(i).getTime());
        }
    }

    @Test
    public void fromAPICountToListDTOTest() {
        List<APICount> apiCountList = new ArrayList<>();
        apiCountList.add(SampleTestObjectCreator.createRandomAPICountObject());
        apiCountList.add(SampleTestObjectCreator.createRandomAPICountObject());
        apiCountList.add(SampleTestObjectCreator.createRandomAPICountObject());
        APICountListDTO apiCountListDTO = AnalyticsMappingUtil.
                fromAPICountToListDTO(apiCountList, ZoneOffset.UTC);

        Assert.assertEquals(apiCountList.size(), apiCountListDTO.getList().size());
        for (int i = 0; i < apiCountList.size(); i++) {
            Assert.assertEquals(epochToISO8601DateTime(apiCountList.get(i).getTimestamp(), ZoneOffset.UTC),
                    apiCountListDTO.getList().get(i).getTime());
        }
    }

    @Test
    public void fromAPIInfoListToDTOTest() {
        List<APIInfo> apiInfoList = new ArrayList<>();
        apiInfoList.add(SampleTestObjectCreator.createRandomAPIInfoObject());
        apiInfoList.add(SampleTestObjectCreator.createRandomAPIInfoObject());
        apiInfoList.add(SampleTestObjectCreator.createRandomAPIInfoObject());
        APIInfoListDTO apiInfoListDTO = AnalyticsMappingUtil.
                fromAPIInfoListToDTO(apiInfoList, ZoneOffset.UTC);

        Assert.assertEquals(apiInfoList.size(), apiInfoListDTO.getList().size());
        for (int i = 0; i < apiInfoList.size(); i++) {
            Assert.assertEquals(epochToISO8601DateTime(apiInfoList.get(i).getCreatedTime(), ZoneOffset.UTC),
                    apiInfoListDTO.getList().get(i).getTime());
            Assert.assertEquals(apiInfoList.get(i).getName(), apiInfoListDTO.getList().get(i).getName());
            Assert.assertEquals(apiInfoList.get(i).getContext(), apiInfoListDTO.getList().get(i).getContext());
            Assert.assertEquals(apiInfoList.get(i).getDescription(), apiInfoListDTO.getList().get(i).getDescription());
            Assert.assertEquals(apiInfoList.get(i).getLifeCycleStatus(),
                    apiInfoListDTO.getList().get(i).getLifeCycleStatus());
            Assert.assertEquals(apiInfoList.get(i).getProvider(), apiInfoListDTO.getList().get(i).getProvider());
            Assert.assertEquals(apiInfoList.get(i).getVersion(), apiInfoListDTO.getList().get(i).getVersion());
            Assert.assertEquals(apiInfoList.get(i).getId(), apiInfoListDTO.getList().get(i).getId());
        }
    }

    @Test
    public void fromAPISubscriptionCountListToDTOTest() {
        List<APISubscriptionCount> apiSubscriptionCountList = new ArrayList<>();
        apiSubscriptionCountList.add(SampleTestObjectCreator.createRandomAPISubscriptionCountObject());
        apiSubscriptionCountList.add(SampleTestObjectCreator.createRandomAPISubscriptionCountObject());
        apiSubscriptionCountList.add(SampleTestObjectCreator.createRandomAPISubscriptionCountObject());
        APISubscriptionCountListDTO apiSubscriptionCountListDTO = AnalyticsMappingUtil.
                fromAPISubscriptionCountListToDTO(apiSubscriptionCountList);

        Assert.assertEquals(apiSubscriptionCountList.size(), apiSubscriptionCountListDTO.getList().size());
        for (int i = 0; i < apiSubscriptionCountList.size(); i++) {
            Assert.assertEquals(apiSubscriptionCountList.get(i).getProvider(),
                    apiSubscriptionCountListDTO.getList().get(i).getProvider());
            Assert.assertEquals(apiSubscriptionCountList.get(i).getVersion(),
                    apiSubscriptionCountListDTO.getList().get(i).getVersion());
            Assert.assertEquals(apiSubscriptionCountList.get(i).getId(),
                    apiSubscriptionCountListDTO.getList().get(i).getId());
        }
    }

    @Test
    public void fromSubscriptionCountListToDTOTest() {
        List<SubscriptionCount> subscriptionCountList = new ArrayList<>();
        subscriptionCountList.add(SampleTestObjectCreator.createRandomSubscriptionCountObject());
        subscriptionCountList.add(SampleTestObjectCreator.createRandomSubscriptionCountObject());
        subscriptionCountList.add(SampleTestObjectCreator.createRandomSubscriptionCountObject());
        SubscriptionCountListDTO subscriptionCountListToDTO = AnalyticsMappingUtil.
                fromSubscriptionCountListToDTO(subscriptionCountList, ZoneOffset.UTC);

        Assert.assertEquals(subscriptionCountList.size(), subscriptionCountListToDTO.getList().size());
        for (int i = 0; i < subscriptionCountList.size(); i++) {
            Assert.assertEquals(Long.valueOf(subscriptionCountList.get(i).getCount()),
                    Long.valueOf(subscriptionCountListToDTO.getList().get(i).getCount()));
            Assert.assertEquals(epochToISO8601DateTime(subscriptionCountList.get(i).getTimestamp(), ZoneOffset.UTC),
                    subscriptionCountListToDTO.getList().get(i).getTime());
        }
    }

    @Test
    public void fromSubscriptionInfoListToDTOTest() {
        List<SubscriptionInfo> subscriptionInfoList = new ArrayList<>();
        subscriptionInfoList.add(SampleTestObjectCreator.createRandomSubscriptionInfoObject());
        subscriptionInfoList.add(SampleTestObjectCreator.createRandomSubscriptionInfoObject());
        subscriptionInfoList.add(SampleTestObjectCreator.createRandomSubscriptionInfoObject());
        SubscriptionInfoListDTO subscriptionInfoListDTO = AnalyticsMappingUtil.
                fromSubscriptionInfoListToDTO(subscriptionInfoList, ZoneOffset.UTC);

        Assert.assertEquals(subscriptionInfoList.size(), subscriptionInfoListDTO.getList().size());
        for (int i = 0; i < subscriptionInfoList.size(); i++) {
            Assert.assertEquals(subscriptionInfoList.get(i).getName(),
                    subscriptionInfoListDTO.getList().get(i).getName());
            Assert.assertEquals(subscriptionInfoList.get(i).getId(),
                    subscriptionInfoListDTO.getList().get(i).getId());
            Assert.assertEquals(subscriptionInfoList.get(i).getDescription(),
                    subscriptionInfoListDTO.getList().get(i).getDescription());
            Assert.assertEquals(subscriptionInfoList.get(i).getAppName(),
                    subscriptionInfoListDTO.getList().get(i).getAppName());
            Assert.assertEquals(subscriptionInfoList.get(i).getVersion(),
                    subscriptionInfoListDTO.getList().get(i).getVersion());
            Assert.assertEquals(subscriptionInfoList.get(i).getSubscriptionStatus(),
                    subscriptionInfoListDTO.getList().get(i).getSubscriptionStatus());
            Assert.assertEquals(subscriptionInfoList.get(i).getSubscriptionTier(),
                    subscriptionInfoListDTO.getList().get(i).getSubscriptionTier());
            Assert.assertEquals(epochToISO8601DateTime(subscriptionInfoList.get(i).getCreatedTime(), ZoneOffset.UTC),
                    subscriptionInfoListDTO.getList().get(i).getCreatedTime());
        }
    }
}
