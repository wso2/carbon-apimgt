/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.HashSet;

/**
 * This class contains test cases for {@link APIMappingUtil}.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, RestAPIStoreUtils.class, ServiceReferenceHolder.class})
public class APIMappingUtilTestCase {

    /**
     * Mocking the relevant methods.
     * @throws APIManagementException API Management Exception.
     */
    @Before
    public void init() throws APIManagementException {
        APIConsumer apiConsumer = Mockito.mock(APIConsumer.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.doReturn(apiManagerConfiguration).when(apiManagerConfigurationService).getAPIManagerConfiguration();
        Mockito.doReturn(apiManagerConfigurationService).when(serviceReferenceHolder)
                .getAPIManagerConfigurationService();
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.mockStatic(RestAPIStoreUtils.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn("admin");
        PowerMockito.when(RestApiUtil.getLoggedInUserConsumer()).thenReturn(apiConsumer);
    }

    /**
     * This method is to test whether the conversion from API to DTO succeeds without any exceptions and whether
     * relevant properties are reflected in the DTO object as well, when the API has additional properties.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testFromAPItoDTOWithAdditionalProperties() throws APIManagementException {
        API api = getSampleAPI();
        api.addProperty("securedAPI", "true");
        APIDTO apidto = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("API model to DTO conversion failed when there are additional properties in the API",
                apidto);
        Assert.assertEquals("API model to DTO conversion failed when there are additional properties in the API",
                "true", apidto.getAdditionalProperties().get("securedAPI"));
    }

    /**
     * This method is to test whether the conversion from API to DTO succeeds without any exceptions and whether
     * relevant properties are reflected in the DTO object as well, when the API does not have additional properties.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testFromAPItoDTOWithoutAdditionalProperties() throws APIManagementException {
        APIDTO apidto = APIMappingUtil.fromAPItoDTO(getSampleAPI());
        Assert.assertNotNull("API model to DTO conversion failed when there are no custom properties", apidto);
        Assert.assertEquals("API model to DTO conversion failed when there are no custom properties", null,
                apidto.getAdditionalProperties().get("securedAPI"));
        Assert.assertEquals("API model to DTO conversion failed when there are no custom properties",
                apidto.getStatus(), APIStatus.CREATED.getStatus());
    }

    /**
     * To get the sample API with the minimum number of parameters.
     * @return Relevant API.
     */
    private API getSampleAPI() {
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "dtoTest", "v1.0.0");
        API api = new API(apiIdentifier);
        api.setStatus(APIStatus.CREATED);
        api.setTransports("https");
        api.setEnvironments(new HashSet<String>() {{
            add("SANDBOX");
        }});
        return api;
    }
}
