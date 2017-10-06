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
package org.wso2.carbon.apimgt.impl.util.test;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;


public class APIVersionComparatorTest {

    private final String PROVIDER_NAME_1 = "provider1";
    private final String PROVIDER_NAME_2 = "provider2";
    private final String API_NAME = "api";
    private final String VERSION_1 = "1.0.0";
    private final String VERSION_2 = "2.0.0";
    private API api1 = Mockito.mock(API.class);
    private API api2 = Mockito.mock(API.class);
    private APIIdentifier apiIdentifier1 = Mockito.mock(APIIdentifier.class);
    private APIIdentifier apiIdentifier2 = Mockito.mock(APIIdentifier.class);

    @Before
    public void setup() throws Exception {
        Mockito.when(api1.getId()).thenReturn(apiIdentifier1);
        Mockito.when(api2.getId()).thenReturn(apiIdentifier2);
    }

    @Test
    public void testNegativeVersionCompare() throws Exception {
        Mockito.when(apiIdentifier1.getProviderName()).thenReturn(PROVIDER_NAME_1);
        Mockito.when(apiIdentifier2.getProviderName()).thenReturn(PROVIDER_NAME_1);
        Mockito.when(apiIdentifier1.getApiName()).thenReturn(API_NAME);
        Mockito.when(apiIdentifier2.getApiName()).thenReturn(API_NAME);
        Mockito.when(apiIdentifier1.getVersion()).thenReturn(VERSION_1);
        Mockito.when(apiIdentifier2.getVersion()).thenReturn(VERSION_2);

        APIVersionComparator apiVersionComparator = new APIVersionComparator();
        Assert.assertTrue(apiVersionComparator.compare(api1, api2) < 0);
    }

    @Test
    public void testPositiveVersionCompare() throws Exception {
        Mockito.when(apiIdentifier1.getProviderName()).thenReturn(PROVIDER_NAME_1);
        Mockito.when(apiIdentifier2.getProviderName()).thenReturn(PROVIDER_NAME_1);
        Mockito.when(apiIdentifier1.getApiName()).thenReturn(API_NAME);
        Mockito.when(apiIdentifier2.getApiName()).thenReturn(API_NAME);
        Mockito.when(apiIdentifier1.getVersion()).thenReturn(VERSION_2);
        Mockito.when(apiIdentifier2.getVersion()).thenReturn(VERSION_1);

        APIVersionComparator apiVersionComparator = new APIVersionComparator();
        Assert.assertTrue(apiVersionComparator.compare(api1, api2) > 0);
    }

    @Test
    public void testNameCompare() throws Exception {
        Mockito.when(apiIdentifier1.getProviderName()).thenReturn(PROVIDER_NAME_1);
        Mockito.when(apiIdentifier2.getProviderName()).thenReturn(PROVIDER_NAME_2);

        APIVersionComparator apiVersionComparator = new APIVersionComparator();
        Assert.assertTrue(apiVersionComparator.compare(api1, api2) < 0);
    }
}
