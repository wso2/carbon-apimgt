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
package org.wso2.carbon.apimgt.core.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.Analyzer;
import org.wso2.carbon.apimgt.core.dao.AnalyticsDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.APIInfo;
import org.wso2.carbon.apimgt.core.models.analytics.APISubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionCount;
import org.wso2.carbon.apimgt.core.models.analytics.SubscriptionInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AnalyzerImpl Test Cases.
 */
public class AnalyzerImplTestCase {

    private static final String FROM_TIMESTAMP = "2011-12-03T10:15:30Z";
    private static final String TO_TIMESTAMP = "2011-12-03T10:15:30Z";

    @Test(description = "Get application count test")
    public void testGetApplicationCount() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        ApplicationCount applicationCount1 = new ApplicationCount();
        ApplicationCount applicationCount2 = new ApplicationCount();
        List<ApplicationCount> dummyApplicationCountList = new ArrayList<>();
        dummyApplicationCountList.add(applicationCount1);
        dummyApplicationCountList.add(applicationCount2);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getApplicationCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null))
                .thenReturn(dummyApplicationCountList);
        List<ApplicationCount> applicationCountListFromDB = analyzer
                .getApplicationCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null);
        Assert.assertNotNull(applicationCountListFromDB);
        verify(analyticsDAO, Mockito.times(1)).getApplicationCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(
                TO_TIMESTAMP), null);

        //Error path
        Mockito.when(analyticsDAO.getApplicationCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                null)).thenThrow(APIMgtDAOException.class);
        try {
            analyzer.getApplicationCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                    null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while fetching application count information");
        }
    }

    @Test(description = "Get API count test")
    public void testGetAPICount() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        APICount apiCount1 = new APICount();
        APICount apiCount2 = new APICount();
        List<APICount> apiCountList = new ArrayList<>();
        apiCountList.add(apiCount1);
        apiCountList.add(apiCount2);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getAPICount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null))
                .thenReturn(apiCountList);
        List<APICount> apiCountListFromDB = analyzer
                .getAPICount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null);
        Assert.assertNotNull(apiCountListFromDB);
        verify(analyticsDAO, Mockito.times(1)).getAPICount(Instant.parse(FROM_TIMESTAMP),
                Instant.parse(TO_TIMESTAMP), null);

        //Error path
        Mockito.when(analyticsDAO.getAPICount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                null)).thenThrow(APIMgtDAOException.class);
        try {
            analyzer.getAPICount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                    null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while fetching API count information");
        }
    }

    @Test(description = "Get subscription count test")
    public void testGetSubscriptionCount() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        SubscriptionCount subscriptionCount = new SubscriptionCount();
        List<SubscriptionCount> subscriptionCountList = new ArrayList<>();
        subscriptionCountList.add(subscriptionCount);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getSubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null))
                .thenReturn(subscriptionCountList);
        List<SubscriptionCount> subscriptionCountDB = analyzer
                .getSubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null);
        Assert.assertNotNull(subscriptionCountDB);
        verify(analyticsDAO, Mockito.times(1)).getSubscriptionCount(Instant.parse(FROM_TIMESTAMP),
                Instant.parse(TO_TIMESTAMP), null);

        //Error path
        Mockito.when(analyticsDAO.getSubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                null)).thenThrow(APIMgtDAOException.class);
        try {
            analyzer.getSubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                    null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while fetching Subscription count information");
        }
    }

    @Test(description = "Get API Info test")
    public void testGetAPIInfo() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        APIInfo apiInfo = new APIInfo();
        List<APIInfo> apiInfos = new ArrayList<>();
        apiInfos.add(apiInfo);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getAPIInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null))
                .thenReturn(apiInfos);
        List<APIInfo> apiInfoResult = analyzer
                .getAPIInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null);
        Assert.assertNotNull(apiInfoResult);
        verify(analyticsDAO, Mockito.times(1)).getAPIInfo(Instant.parse(FROM_TIMESTAMP),
                Instant.parse(TO_TIMESTAMP), null);

        //Error path
        Mockito.when(analyticsDAO.getAPIInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                null)).thenThrow(APIMgtDAOException.class);
        try {
            analyzer.getAPIInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                    null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while fetching API information");
        }
    }

    @Test(description = "Get Subscription Info test")
    public void testGetSubscrptionInfo() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        SubscriptionInfo subscriptionInfo = new SubscriptionInfo();
        List<SubscriptionInfo> subscriptionInfos = new ArrayList<>();
        subscriptionInfos.add(subscriptionInfo);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getSubscriptionInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null))
                .thenReturn(subscriptionInfos);
        List<SubscriptionInfo> subscriptionInfoResult = analyzer
                .getSubscriptionInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null);
        Assert.assertNotNull(subscriptionInfoResult);
        verify(analyticsDAO, Mockito.times(1)).getSubscriptionInfo(Instant.parse(FROM_TIMESTAMP),
                Instant.parse(TO_TIMESTAMP), null);

        //Error path
        Mockito.when(analyticsDAO.getSubscriptionInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                null)).thenThrow(APIMgtDAOException.class);
        try {
            analyzer.getSubscriptionInfo(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                    null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while fetching Subscription information");
        }
    }

    @Test(description = "Get Subscription count for API test")
    public void testGetAPISubscrptionCount() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        APISubscriptionCount apiSubscriptionCount = new APISubscriptionCount();
        List<APISubscriptionCount> apiSubscriptionCountList = new ArrayList<>();
        apiSubscriptionCountList.add(apiSubscriptionCount);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getAPISubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null))
                .thenReturn(apiSubscriptionCountList);
        List<APISubscriptionCount> apiSubscriptionCountResult = analyzer
                .getAPISubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP), null);
        Assert.assertNotNull(apiSubscriptionCountResult);
        verify(analyticsDAO, Mockito.times(1)).getAPISubscriptionCount(Instant.parse(FROM_TIMESTAMP),
                Instant.parse(TO_TIMESTAMP), null);

        //Error path
        Mockito.when(analyticsDAO.getAPISubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                null)).thenThrow(APIMgtDAOException.class);
        try {
            analyzer.getAPISubscriptionCount(Instant.parse(FROM_TIMESTAMP), Instant.parse(TO_TIMESTAMP),
                    null);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while fetching API subscription count information");
        }
    }

    private AnalyzerImpl getAnalyzerImpl(AnalyticsDAO analyticsDAO) {
        return new AnalyzerImpl("john", analyticsDAO);
    }
}
