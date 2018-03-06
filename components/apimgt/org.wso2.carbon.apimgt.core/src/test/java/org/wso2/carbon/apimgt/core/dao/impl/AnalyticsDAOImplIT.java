/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.AnalyticsDAO;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;

import java.time.Instant;
import java.util.List;

/**
 * AnalyzerDaoImpl Test cases.
 */
public class AnalyticsDAOImplIT extends DAOIntegrationTestBase {

    private static final long DELAY_TIME = 5000L;

    @Test
    public void testGetApplicationCount() throws Exception {
        Instant fromTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis());
        TestUtil.addCustomApplication("app1", "john");
        Instant toTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis() + DELAY_TIME);
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<ApplicationCount> applicationCountList = analyticsDAO
                .getApplicationCount(fromTimeStamp, toTimeStamp, null);
        Assert.assertEquals(applicationCountList.size(), 1);
    }

    @Test
    public void testGetAPICount() throws Exception {
        Instant fromTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis());
        TestUtil.addTestAPI();
        Instant toTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis() + DELAY_TIME);
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<APICount> applicationCountList = analyticsDAO
                .getAPICount(fromTimeStamp, toTimeStamp, null);
        Assert.assertEquals(applicationCountList.size(), 1);
    }

    @Test
    public void testGetAPIList() throws Exception {
        Instant fromTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis());
        API testAPI1 = TestUtil.addCustomAPI("Name1", "1.0.0", "sample1");
        API testAPI2 = TestUtil.addCustomAPI("Name2", "1.0.0", "sample2");
        Instant toTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis() + DELAY_TIME);
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<APIInfo> apiInfoList = analyticsDAO
                .getAPIInfo(fromTimeStamp, toTimeStamp, null);
        Assert.assertEquals(apiInfoList.size(), 2);
        APIInfo apiInfo1 = apiInfoList.get(0);
        APIInfo apiInfo2 = apiInfoList.get(1);
        API result1 = new API.APIBuilder(apiInfo1.getProvider(), apiInfo1.getName(), apiInfo1.getVersion()).build();
        API result2 = new API.APIBuilder(apiInfo2.getProvider(), apiInfo2.getName(), apiInfo2.getVersion()).build();
        Assert.assertTrue(TestUtil.testAPIEqualsLazy(testAPI1, result1));
        Assert.assertTrue(TestUtil.testAPIEqualsLazy(testAPI2, result2));
    }

    @Test
    public void testGetSubscriptionCount() throws Exception {
        Instant fromTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis());
        API testAPI = TestUtil.addTestAPI();
        Application testApplication = TestUtil.addTestApplication();
        TestUtil.subscribeToAPI(testAPI, testApplication);
        Instant toTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis() + DELAY_TIME);
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<SubscriptionCount> subscriptionCount = analyticsDAO.getSubscriptionCount(fromTimeStamp, toTimeStamp, null);
        Assert.assertEquals(subscriptionCount.size(), 1);
    }

    @Test
    public void testGetSubscriptionCountPerAPI() throws Exception {
        Instant fromTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis());
        API testAPI = TestUtil.addTestAPI();
        API testAPI1 = TestUtil.addCustomAPI("TestAPI1", "1.0.0", "test1");
        API testAPI2 = TestUtil.addCustomAPI("TestAPI2", "1.0.0", "test2");
        Application testApplication = TestUtil.addTestApplication();
        Application testApplication2 = TestUtil.addCustomApplication("APP2", "admin");
        TestUtil.subscribeToAPI(testAPI, testApplication);
        TestUtil.subscribeToAPI(testAPI, testApplication2);
        TestUtil.subscribeToAPI(testAPI1, testApplication2);
        TestUtil.subscribeToAPI(testAPI1, testApplication);
        TestUtil.subscribeToAPI(testAPI2, testApplication2);
        Instant toTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis() + DELAY_TIME);
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<APISubscriptionCount> subscriptionCount = analyticsDAO.getAPISubscriptionCount(fromTimeStamp, toTimeStamp,
                null);
        Assert.assertEquals(subscriptionCount.size(), 3);
    }

    @Test
    public void testGetSubscriptionList() throws Exception {
        Instant fromTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis());
        API testAPI = TestUtil.addTestAPI();
        Application testApplication = TestUtil.addTestApplication();
        Subscription subscription = TestUtil.subscribeToAPI(testAPI, testApplication);
        Instant toTimeStamp = Instant.ofEpochMilli(System.currentTimeMillis() + DELAY_TIME);
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<SubscriptionInfo> subscriptionInfo = analyticsDAO.getSubscriptionInfo(fromTimeStamp, toTimeStamp,
                null);
        Assert.assertEquals(subscriptionInfo.size(), 1);
        SubscriptionInfo subscriptionInfoResult = subscriptionInfo.get(0);
        Assert.assertEquals(subscription.getId(), subscriptionInfoResult.getId());
        Assert.assertEquals(subscription.getApi().getName(), subscriptionInfoResult.getName());
        Assert.assertEquals(subscription.getApplication().getName(), subscriptionInfoResult.getAppName());
    }
}
