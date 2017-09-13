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
import org.wso2.carbon.apimgt.core.models.analytics.APICount;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;

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

    @Test(description = "get application count test")
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
    }

    @Test(description = "get API count test")
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
    }

    private AnalyzerImpl getAnalyzerImpl(AnalyticsDAO analyticsDAO) {
        return new AnalyzerImpl("john", analyticsDAO);
    }
}
