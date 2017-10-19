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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.APIStore;

public class APIStoreNameComparatorTestCase {

    @Test
    public void testCompareEquals() {
        APIStore apiStore1 = new APIStore();
        apiStore1.setDisplayName("Store1");
        apiStore1.setName("Store1");
        APIStore apiStore2 = new APIStore();
        apiStore2.setDisplayName("Store1");
        apiStore2.setName("Store1");

        APIStoreNameComparator apiStoreNameComparator = new APIStoreNameComparator();
        int result = apiStoreNameComparator.compare(apiStore1, apiStore2);
        Assert.assertEquals(0, result);
    }

    @Test
    public void testCompareNotEquals() {
        APIStore apiStore1 = new APIStore();
        apiStore1.setDisplayName("Store1");
        apiStore1.setName("Store1");
        APIStore apiStore2 = new APIStore();
        apiStore2.setDisplayName("Store2");
        apiStore2.setName("Store2");

        APIStoreNameComparator apiStoreNameComparator = new APIStoreNameComparator();
        int result = apiStoreNameComparator.compare(apiStore1, apiStore2);
        Assert.assertNotEquals(0, result);
    }

}
