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

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the test class for {@link APIMappingUtil}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, APIUtil.class})
public class APIMappingUtilTest {

    /**
     * Initializing the mocks.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Before
    public void init() throws APIManagementException {
        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername()).thenReturn("admin");
        PowerMockito.when(RestApiUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        PowerMockito.mockStatic(APIUtil.class);
        CORSConfiguration corsConfiguration = Mockito.mock(CORSConfiguration.class);
        PowerMockito.when(APIUtil.getDefaultCorsConfiguration()).thenReturn(corsConfiguration);
    }

    /**
     * This method tests the behaviour of the fromAPItoDTO method.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testFromAPItoDTO() throws APIManagementException {
        API api = getSampleAPI();
        APIDetailedDTO apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed without additional properties", apiDetailedDTO);

        api.addProperty("test", "test");
        apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed with additional properties", apiDetailedDTO);
        Assert.assertEquals("Additional properties added in the API is not preserved after converting to APIDetailedDTO",
                "test", apiDetailedDTO.getAdditionalProperties().get("test"));
    }
    /**
     * This method tests the behaviour of the fromAPItoDTO method.
     *
     * @throws APIManagementException API Management Exception.
     */

    @Test
    public void testFromDTOtoAPI() throws APIManagementException {
        APIDetailedDTO apiDetailedDTO = APIMappingUtil.fromAPItoDTO(getSampleAPI());
        API api = APIMappingUtil.fromDTOtoAPI(apiDetailedDTO, "admin");
        Assert.assertNotNull("Conversion from DTO to API failed without properties", api);

        apiDetailedDTO.setAdditionalProperties(new HashMap<String, String>() {{
            put("secured", "false");
        }});
        api = APIMappingUtil.fromDTOtoAPI(apiDetailedDTO, "admin");
        Assert.assertNotNull("Conversion from DTO to API failed with properties", api);
        Assert.assertEquals("Conversion produces different DTO object that does not match with orginal API passed",
                "false", api.getAdditionalProperties().get("secured"));
    }

    /**
     * To get the sample API with the minimum number of parameters.
     *
     * @return Relevant API.
     */
    private API getSampleAPI() {
        APIIdentifier apiIdentifier = new APIIdentifier("admin", "dtoTest", "v1.0.0");
        API api = new API(apiIdentifier);
        api.setStatus(APIConstants.CREATED);
        api.setTransports("https,http");
        api.setEnvironments(new HashSet<String>() {{
            add("SANDBOX");
        }});
        api.setContextTemplate("/test");
        api.setType("HTTP");
        api.setVisibility("public");
        Set< String > environmentList = new HashSet<>();
        environmentList.add("SANDBOX");
        api.setEnvironmentList(environmentList);
        return api;
    }

}
