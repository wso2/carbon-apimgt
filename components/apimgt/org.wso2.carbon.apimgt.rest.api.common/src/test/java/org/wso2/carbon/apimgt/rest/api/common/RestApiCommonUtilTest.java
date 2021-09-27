/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.base.CarbonBaseConstants;
import org.wso2.carbon.context.CarbonContext;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.h2.osgi.utils.CarbonConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonContext.class, APIManagerFactory.class, RestApiCommonUtil.class})
public class RestApiCommonUtilTest {

    @Test
    public void testGetLoggedInUserProvider() throws Exception {

        System.setProperty(CARBON_HOME, "");
        String providerName = "admin";

        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        APIProvider testApiProvider = Mockito.mock(APIProvider.class);
        when(apiManagerFactory.getAPIProvider(providerName)).thenReturn(testApiProvider);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(providerName);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenCallRealMethod();

        APIProvider loggedInUserProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Assert.assertEquals(testApiProvider, loggedInUserProvider);
    }

    @Test
    public void testGetLoggedInUserTenantDomain() {

        String defaultTenantDomain = "wso2.com";
        System.setProperty(CarbonBaseConstants.CARBON_HOME, "");

        PowerMockito.mockStatic(CarbonContext.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        when(carbonContext.getTenantDomain()).thenReturn(defaultTenantDomain);

        String loggedInUsername = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Assert.assertEquals(defaultTenantDomain, loggedInUsername);
    }

    @Test
    public void testGetConsumer() throws APIManagementException {

        String userName = "TEST_USER";

        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        when(apiManagerFactory.getAPIConsumer(userName)).thenReturn(apiConsumer);

        Assert.assertEquals(apiConsumer, RestApiCommonUtil.getConsumer(userName));
    }

    @Test
    public void testGenerateOpenAPI() throws JsonProcessingException {

        String openAPIDefinition = RestApiCommonUtil
                .generateOpenAPIForAsync("testAPI", "3.14", "hello", "http://www.mocky.io/v2/5185415ba171ea3a00704eed");
        String expected =
                "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"testAPI\",\"description\":\"API Definition of testAPI\"," +
                        "\"version\":\"3.14\"},\"servers\":[{\"url\":\"/\"}],\"paths\":{\"/*\":{\"post\":{\"responses\"" +
                        ":{\"default\":{\"description\":\"Default response\"}}}}},\"x-wso2-production-endpoints\":" +
                        "{\"urls\":[\"http://www.mocky.io/v2/5185415ba171ea3a00704eed\"],\"type\":\"http\"}," +
                        "\"x-wso2-sandbox-endpoints\":{\"urls\":[\"http://www.mocky.io/v2/5185415ba171ea3a00704eed\"]," +
                        "\"type\":\"http\"},\"x-wso2-auth-header\":\"Authorization\",\"x-wso2-basePath\":\"hello/3.14\"," +
                        "\"x-wso2-disable-security\":true}";
        Assert.assertEquals("", expected, openAPIDefinition);
    }
}
