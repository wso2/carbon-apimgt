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
 */
package org.wso2.carbon.apimgt.core.util;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.API;

/**
 * 
 * Test cases for API Comparison
 *
 */
public class APIComparatorTestCase {

    @Test
    public void testAPIComparator() {

        API api1 = new API.APIBuilder("p1", "name1", "1.0.0").build();
        API api2 = new API.APIBuilder("p1", "name1", "1.1.0").build();
        API api3 = new API.APIBuilder("p1", "name1", "0.9.1").build();
        API api4 = new API.APIBuilder("p1", "name1", "1.0.0").build();
        API api5 = new API.APIBuilder("p4", "name4", "1.0.0.wso2v1").build();
        API api6 = new API.APIBuilder("p4", "name4", "1.0.0.wso2v1").build();

        API api7 = new API.APIBuilder("p4", "name4", "1.0.0").build();
        API api8 = new API.APIBuilder("p4", "name4", "1.0.0-SNAPSHOT").build();

        API api9 = new API.APIBuilder("p4", "name4", "1.1.1").build();
        API api10 = new API.APIBuilder("p2", "name4", "1.0.0-SNAPSHOT").build();
        API api11 = new API.APIBuilder("p2", "name9", "1.2.9").build();
        API api12 = new API.APIBuilder("p5", "name10", "1.3.0").build();

        APIComparator apiComparator = new APIComparator();

        int comparisonResult1 = apiComparator.compare(api1, api2);
        int comparisonResult2 = apiComparator.compare(api1, api3);
        int comparisonResult3 = apiComparator.compare(api1, api4);
        int comparisonResult4 = apiComparator.compare(api6, api5);
        int comparisonResult5 = apiComparator.compare(api7, api8);
        int comparisonResult6 = apiComparator.compare(api9, api10);
        int comparisonResult7 = apiComparator.compare(api10, api11);
        int comparisonResult8 = apiComparator.compare(api11, api12);

        Assert.assertTrue(comparisonResult1 < 0, "Error, comparisonResult is non negative. ");
        Assert.assertTrue(comparisonResult2 > 0, "Error, comparisonResult is non positive. ");
        Assert.assertEquals(comparisonResult3, 0, "Error, compared versions are not equal. ");
        Assert.assertEquals(comparisonResult4, 0, "Error, compared versions are not equal. ");
        Assert.assertTrue(comparisonResult5 > 0, "Error, comparisonResult is non positive. ");
        Assert.assertTrue(comparisonResult6 > 0, "Error, comparisonResult is non positive. ");
        Assert.assertTrue(comparisonResult7 < 0, "Error, comparisonResult is non negative. ");
        Assert.assertTrue(comparisonResult8 < 0, "Error, comparisonResult is non negative. ");
    }
}
