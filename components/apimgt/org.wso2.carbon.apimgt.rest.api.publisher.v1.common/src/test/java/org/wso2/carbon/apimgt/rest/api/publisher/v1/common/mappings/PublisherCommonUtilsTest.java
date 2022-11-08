/*
 *
 *   Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdvertiseInfoDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_DATA_PRODUCTION_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_DATA_SANDBOX_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class, WorkflowExecutorFactory.class, APIUtil.class})
public class PublisherCommonUtilsTest {

    private static final String PROVIDER = "admin";
    private static final String API_PRODUCT_NAME = "test";
    private static final String API_PRODUCT_VERSION = "1.0.0";
    private static final String ORGANIZATION = "carbon.super";
    private static final String UUID = "63e1e37e-a5b8-4be6-86a5-d6ae0749f131";

    @Test
    public void testGetInvalidTierNames() throws Exception {

        List<String>  currentTiers = Arrays.asList(new String[]{"Unlimitted", "Platinum", "gold"});
        Tier mockTier = Mockito.mock(Tier.class);
        Tier tier1 = new Tier("Gold");
        Tier tier2 = new Tier("Unlimitted");
        Tier tier3 = new Tier("Silver");
        Set<Tier> allTiers = new HashSet<Tier>();
        allTiers.add(tier1);
        allTiers.add(tier2);
        allTiers.add(tier3);
        PowerMockito.whenNew(Tier.class).withAnyArguments().thenReturn(mockTier);
        Mockito.when(mockTier.getName()).thenReturn("Unlimitted");
        List<String> expectedInvalidTier = Arrays.asList(new String[]{"Platinum", "gold"});
        Assert.assertEquals(PublisherCommonUtils.getInvalidTierNames(allTiers, currentTiers), expectedInvalidTier);
    }

    @Test
    public void testChangeApiOrApiProductLifecycleToInvalidState() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Map<String, Object> apiLcData = new HashMap<>();
        String[] nextStates = new String[]{"Block", "Deploy as a Prototype", "Demote to Created", "Deprecate"};
        apiLcData.put(APIConstants.LC_NEXT_STATES, nextStates);
        apiLcData.put(APIConstants.API_STATUS, APIStatus.PUBLISHED.getStatus());
        Mockito.when(apiProvider.getAPILifeCycleData(Mockito.anyString(), Mockito.anyString())).thenReturn(apiLcData);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        try {
            PublisherCommonUtils.changeApiOrApiProductLifecycle("Retire", createMockAPIProduct(),
                    StringUtils.EMPTY, ORGANIZATION);
        } catch (APIManagementException e) {
            Assert.assertNotNull(e.getMessage());
            Assert.assertTrue(e.getMessage().contains("Action 'Retire' is not allowed"));
        }
    }

    private ApiTypeWrapper createMockAPIProduct() {

        APIProduct product = new APIProduct(new APIProductIdentifier(PROVIDER, API_PRODUCT_NAME, API_PRODUCT_VERSION,
                UUID));
        product.setState(APIConstants.PUBLISHED);
        return new ApiTypeWrapper(product);
    }

    @Test
    public void testValidateEndpointsDefaultType() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "default");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateValidEndpoints() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "https://sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(false);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateInvalidProductionEndpoint() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https//productionendpoint.test";
        String sandboxEndpointString = "https://sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(false);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(false);
        Assert.assertFalse(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateInvalidSandboxEndpoint() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(false);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(false);
        Assert.assertFalse(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateValidExternalEndpoints() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "https://sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);
        String externalProductionEndpointString = "https://exproductionendpoint.test";
        String externalSandboxEndpointString = "https://exsandboxendpoint.test";
        String originalDevPortalUrl = "https://devportal.test";

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);
        endpoints.add(externalProductionEndpointString);
        endpoints.add(externalSandboxEndpointString);
        endpoints.add(originalDevPortalUrl);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(apiDto.getAdvertiseInfo()).thenReturn(advertiseInfoDto);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(true);
        Mockito.when(advertiseInfoDto.getApiExternalProductionEndpoint()).thenReturn(externalProductionEndpointString);
        Mockito.when(advertiseInfoDto.getApiExternalSandboxEndpoint()).thenReturn(externalSandboxEndpointString);
        Mockito.when(advertiseInfoDto.getOriginalDevPortalUrl()).thenReturn(originalDevPortalUrl);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateInvalidExternalEndpoints() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "https://sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);
        String externalProductionEndpointString = "exproductionendpoint.test";
        String externalSandboxEndpointString = "https://exsandboxendpoint.test";
        String originalDevPortalUrl = "https://devportal.test";

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);
        endpoints.add(externalProductionEndpointString);
        endpoints.add(externalSandboxEndpointString);
        endpoints.add(originalDevPortalUrl);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(apiDto.getAdvertiseInfo()).thenReturn(advertiseInfoDto);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(true);
        Mockito.when(advertiseInfoDto.getApiExternalProductionEndpoint()).thenReturn(externalProductionEndpointString);
        Mockito.when(advertiseInfoDto.getApiExternalSandboxEndpoint()).thenReturn(externalSandboxEndpointString);
        Mockito.when(advertiseInfoDto.getOriginalDevPortalUrl()).thenReturn(originalDevPortalUrl);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(false);
        Assert.assertFalse(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateEndpointsNullEndpointConfig() {

        APIDTO apiDto = Mockito.mock(APIDTO.class);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);
        String externalProductionEndpointString = "https://exproductionendpoint.test";

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(externalProductionEndpointString);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(null);
        Mockito.when(apiDto.getAdvertiseInfo()).thenReturn(advertiseInfoDto);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(true);
        Mockito.when(advertiseInfoDto.getApiExternalProductionEndpoint()).thenReturn(externalProductionEndpointString);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateEndpointsNullAdvertiseInfo() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "https://sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(apiDto.getAdvertiseInfo()).thenReturn(null);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(apiDto));
    }

    @Test
    public void testValidateEndpointsNullExternalEndpoint() {

        // endpointConfig
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        String productionEndpointString = "https://productionendpoint.test";
        String sandboxEndpointString = "https://sandboxendpoint.test";
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        HashMap<String, Object> productionEndpoint = new HashMap<>();
        productionEndpoint.put("url", productionEndpointString);
        HashMap<String, Object> sandboxEndpoint = new HashMap<>();
        sandboxEndpoint.put("url", sandboxEndpointString);
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoint);
        endpointConfig.put(API_DATA_SANDBOX_ENDPOINTS, sandboxEndpoint);

        // advertiseInfo
        AdvertiseInfoDTO advertiseInfoDto = Mockito.mock(AdvertiseInfoDTO.class);
        String externalSandboxEndpointString = "https://exsandboxendpoint.test";
        String originalDevPortalUrl = "https://devportal.test";

        // extracted endpoints
        ArrayList<String> endpoints = new ArrayList<>();
        endpoints.add(sandboxEndpointString);
        endpoints.add(productionEndpointString);
        endpoints.add(externalSandboxEndpointString);
        endpoints.add(originalDevPortalUrl);

        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);
        Mockito.when(apiDto.getAdvertiseInfo()).thenReturn(advertiseInfoDto);
        Mockito.when(advertiseInfoDto.isAdvertised()).thenReturn(true);
        Mockito.when(advertiseInfoDto.getApiExternalProductionEndpoint()).thenReturn(null);
        Mockito.when(advertiseInfoDto.getApiExternalSandboxEndpoint()).thenReturn(externalSandboxEndpointString);
        Mockito.when(advertiseInfoDto.getOriginalDevPortalUrl()).thenReturn(originalDevPortalUrl);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(apiDto));
    }
}
