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
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AnalyzerImpl Test Cases.
 */
public class AnalyzerImplTestCase {

    private static final String USER_NAME = "john";
    private static final String FROM_TIMESTAMP = "2017-06-30 2:32:41.972";
    private static final String TO_TIMESTAMP = "2017-07-30 2:32:41.972";

    @Test(description = "get application count test")
    public void testGetApplicationCount() throws APIManagementException {
        AnalyticsDAO analyticsDAO = Mockito.mock(AnalyticsDAO.class);
        ApplicationCount applicationCount1 = new ApplicationCount();
        ApplicationCount applicationCount2 = new ApplicationCount();
        List<ApplicationCount> dummyApplicationCountList = new ArrayList<>();
        dummyApplicationCountList.add(applicationCount1);
        dummyApplicationCountList.add(applicationCount2);
        Analyzer analyzer = getAnalyzerImpl(analyticsDAO);
        when(analyticsDAO.getApplicationCount(USER_NAME, null, FROM_TIMESTAMP, TO_TIMESTAMP))
                .thenReturn(dummyApplicationCountList);
        List<ApplicationCount> applicationCountListFromDB = analyzer
                .getApplicationCount(USER_NAME, null, FROM_TIMESTAMP, TO_TIMESTAMP);
        Assert.assertNotNull(applicationCountListFromDB);
        verify(analyticsDAO, Mockito.times(1)).getApplicationCount(USER_NAME, null, FROM_TIMESTAMP, TO_TIMESTAMP);
    }

    private AnalyzerImpl getAnalyzerImpl(AnalyticsDAO analyticsDAO) {
        return new AnalyzerImpl("john", analyticsDAO);
    }
}
