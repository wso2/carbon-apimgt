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
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;

import java.sql.Timestamp;
import java.util.List;

/**
 * AnalyzerDaoImpl Test cases.
 */
public class AnalyzerDaoImplIT extends DAOIntegrationTestBase {

    @Test
    public void testGetApplicationCount() throws Exception {

        String fromTimeStamp = (new Timestamp(System.currentTimeMillis() - 10000)).toString();
        TestUtil.addCustomApplication("app1", "john");
        String toTimeStamp = (new Timestamp(System.currentTimeMillis() + 10000)).toString();
        AnalyticsDAO analyticsDAO = DAOFactory.getAnalyticsDAO();
        List<ApplicationCount> applicationCountList = analyticsDAO
                .getApplicationCount(fromTimeStamp, toTimeStamp);
        Assert.assertEquals(applicationCountList.size(), 1);
    }

}
