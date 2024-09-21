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
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Mockito.when;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_DATA_PRODUCTION_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_DATA_SANDBOX_ENDPOINTS;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE;
import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils.addDocumentationToAPI;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiCommonUtil.class, WorkflowExecutorFactory.class, APIUtil.class})
public class PublisherCommonUtilsTest {

    private static final String PROVIDER = "admin";
    private static final String API_PRODUCT_NAME = "test";
    private static final String API_PRODUCT_VERSION = "1.0.0";
    private static final String ORGANIZATION = "carbon.super";

    private static final String TENANT_ORGANIZATION = "wso2.com";
    private static final String UUID = "63e1e37e-a5b8-4be6-86a5-d6ae0749f131";

    private static final String API_PRODUCT_CONTEXT = "/test-context";
    private static final String API_PRODUCT_VERSION_APPENDED_CONTEXT = "/test-context/1.0.0";

    private static final String API_PRODUCT_CONTEXT_FOR_TENANT = "/t/wso2.com/test-context";

    private static final String API_PRODUCT_CONTEXT_TEMPLATE = "/test-context/{version}";
    private static final String API_ID = "f4dbe403-4e19-44e9-bb14-c83eda633791";
    private static final String DOC_NAME = "test/documentation";
    private static final String DOC_TYPE = "HOWTO";
    private static final String DOC_SUMMARY = "Summary of test documentation";
    private static final String DOC_SOURCE_TYPE = "INLINE";
    private static final String DOC_VISIBILITY = "API_LEVEL";

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
    public void testValidateEndpointsDefaultType() throws APIManagementException {

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
    public void testValidateValidEndpoints() throws APIManagementException {

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
    public void testValidateInvalidProductionEndpoint() throws APIManagementException {

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
    public void testValidateInvalidSandboxEndpoint() throws APIManagementException {

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
    public void testValidateValidExternalEndpoints() throws APIManagementException {

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
    public void testValidateInvalidExternalEndpoints() throws APIManagementException {

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
    public void testValidateEndpointsNullEndpointConfig() throws APIManagementException {

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
    public void testValidateEndpointsNullAdvertiseInfo() throws APIManagementException {

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
    public void testValidateEndpointsNullExternalEndpoint() throws APIManagementException {

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

    /**
     * Tests the validation of endpoint configurations for an APIDTO object.
     * This method checks if the validation of session timeout values in the
     * endpoint configuration map is working as expected.
     * The session timeout value can be an integer, long or a numeric string that can be parsed as a long.
     */
    @Test
    public void testValidateEndpointConfigs() {
        APIDTO apiDTO = new APIDTO();
        LinkedHashMap<Object, Object> endpointConfigs = new LinkedHashMap<>();
        apiDTO.setEndpointConfig(endpointConfigs);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, 300);
        boolean flag = PublisherCommonUtils.validateEndpointConfigs(apiDTO);
        Assert.assertTrue(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "300");
        flag = PublisherCommonUtils.validateEndpointConfigs(apiDTO);
        Assert.assertTrue(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "300e");
        flag = PublisherCommonUtils.validateEndpointConfigs(apiDTO);
        Assert.assertFalse(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "300.0");
        flag = PublisherCommonUtils.validateEndpointConfigs(apiDTO);
        Assert.assertFalse(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "sdwed");
        flag = PublisherCommonUtils.validateEndpointConfigs(apiDTO);
        Assert.assertFalse(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "1000000000000000000000000000000000");
        flag = PublisherCommonUtils.validateEndpointConfigs(apiDTO);
        Assert.assertFalse(flag);

    }

    @Test
    public void testCheckDuplicateContextForExistingVersions() throws APIManagementException {

        APIProductDTO apiProductDTO = getAPIProductDTOForDuplicateContextTest();
        APIProvider apiProvider = Mockito.mock(APIProvider.class);

        List<String> apiVersions = new ArrayList<>(Arrays.asList("1.0.0", "2.0.0", "3.0.0"));
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Mockito.when(apiProvider.getApiVersionsMatchingApiNameAndOrganization(API_PRODUCT_NAME, PROVIDER, ORGANIZATION))
                .thenReturn(apiVersions);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_CONTEXT, ORGANIZATION))
                .thenReturn(true);
        Mockito.when(apiProvider.getTiers()).thenReturn(tiers);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);

        String expectedMessage =
                "Error occurred while adding the API Product. A duplicate API context already exists for "
                        + API_PRODUCT_CONTEXT + " in the organization : " + ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for context with "/{version}"
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT_TEMPLATE);

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for context which has version already appended
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_VERSION_APPENDED_CONTEXT);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_VERSION_APPENDED_CONTEXT,
                ORGANIZATION)).thenReturn(true);
        expectedMessage = "Error occurred while adding the API Product. A duplicate API context already exists for "
                + API_PRODUCT_VERSION_APPENDED_CONTEXT + " in the organization : " + ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for tenant context
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT_FOR_TENANT);
        Mockito.when(apiProvider.getApiVersionsMatchingApiNameAndOrganization(API_PRODUCT_NAME, PROVIDER,
                TENANT_ORGANIZATION)).thenReturn(apiVersions);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_CONTEXT_FOR_TENANT,
                TENANT_ORGANIZATION)).thenReturn(true);
        expectedMessage = "Error occurred while adding the API Product. A duplicate API context already exists for "
                + API_PRODUCT_CONTEXT_FOR_TENANT + " in the organization : " + TENANT_ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, TENANT_ORGANIZATION, expectedMessage);
    }

    @Test
    public void testCheckDuplicateContextForSimilarAPINameWithDifferentContext() throws APIManagementException {

        APIProductDTO apiProductDTO = getAPIProductDTOForDuplicateContextTest();
        APIProvider apiProvider = Mockito.mock(APIProvider.class);

        List<String> apiVersions = new ArrayList<>(Arrays.asList("1.0.0", "2.0.0", "3.0.0"));
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        Mockito.when(apiProvider.getApiVersionsMatchingApiNameAndOrganization(API_PRODUCT_NAME, PROVIDER, ORGANIZATION))
                .thenReturn(apiVersions);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_CONTEXT, ORGANIZATION))
                .thenReturn(false);
        Mockito.when(apiProvider.getTiers()).thenReturn(tiers);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);

        String expectedMessage = "Error occurred while adding API Product. API Product with name " + API_PRODUCT_NAME
                + " already exists with different context " + API_PRODUCT_CONTEXT + " in the organization : "
                + ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for context with "/{version}"
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT_TEMPLATE);

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for context which has version already appended
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_VERSION_APPENDED_CONTEXT);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_VERSION_APPENDED_CONTEXT,
                ORGANIZATION)).thenReturn(false);
        expectedMessage = "Error occurred while adding API Product. API Product with name " + API_PRODUCT_NAME
                + " already exists with different context " + API_PRODUCT_VERSION_APPENDED_CONTEXT
                + " in the organization : " + ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for tenant context
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT_FOR_TENANT);
        Mockito.when(apiProvider.getApiVersionsMatchingApiNameAndOrganization(API_PRODUCT_NAME, PROVIDER,
                TENANT_ORGANIZATION)).thenReturn(apiVersions);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_CONTEXT_FOR_TENANT,
                TENANT_ORGANIZATION)).thenReturn(false);
        expectedMessage = "Error occurred while adding API Product. API Product with name " + API_PRODUCT_NAME
                + " already exists with different context " + API_PRODUCT_CONTEXT_FOR_TENANT + " in the organization : "
                + TENANT_ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, TENANT_ORGANIZATION, expectedMessage);
    }

    @Test
    public void testCheckDuplicateContextForNoPreviousVersions() throws APIManagementException {

        APIProductDTO apiProductDTO = getAPIProductDTOForDuplicateContextTest();
        APIProvider apiProvider = Mockito.mock(APIProvider.class);

        List<String> apiVersions = new ArrayList<>();
        Set<Tier> tiers = new TreeSet<Tier>(new TierNameComparator());
        String contextWithVersion = API_PRODUCT_CONTEXT + "/" + API_PRODUCT_VERSION;
        Mockito.when(apiProvider.getApiVersionsMatchingApiNameAndOrganization(API_PRODUCT_NAME, PROVIDER, ORGANIZATION))
                .thenReturn(apiVersions);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_CONTEXT, ORGANIZATION))
                .thenReturn(false);
        Mockito.when(apiProvider.isContextExistForAPIProducts(API_PRODUCT_CONTEXT, contextWithVersion, ORGANIZATION))
                .thenReturn(true);
        Mockito.when(apiProvider.getTiers()).thenReturn(tiers);

        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);

        String expectedMessage =
                "Error occurred while adding the API Product. A duplicate API context already " + "exists for "
                        + API_PRODUCT_CONTEXT + " in the organization : " + ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for context with "/{version}"
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT_TEMPLATE);

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for context which has version already appended
        contextWithVersion = API_PRODUCT_VERSION_APPENDED_CONTEXT + "/" + API_PRODUCT_VERSION;
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_VERSION_APPENDED_CONTEXT);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_VERSION_APPENDED_CONTEXT,
                ORGANIZATION)).thenReturn(false);
        Mockito.when(apiProvider.isContextExistForAPIProducts(API_PRODUCT_VERSION_APPENDED_CONTEXT, contextWithVersion,
                ORGANIZATION)).thenReturn(true);
        expectedMessage = "Error occurred while adding the API Product. A duplicate API context already exists for "
                + API_PRODUCT_VERSION_APPENDED_CONTEXT + " in the organization : " + ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, ORGANIZATION, expectedMessage);

        // Test for tenant context
        contextWithVersion = API_PRODUCT_CONTEXT_FOR_TENANT + "/" + API_PRODUCT_VERSION;
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT_FOR_TENANT);
        Mockito.when(apiProvider.isDuplicateContextTemplateMatchingOrganization(API_PRODUCT_CONTEXT_FOR_TENANT,
                TENANT_ORGANIZATION)).thenReturn(false);
        Mockito.when(apiProvider.isContextExistForAPIProducts(API_PRODUCT_CONTEXT_FOR_TENANT, contextWithVersion,
                TENANT_ORGANIZATION)).thenReturn(true);
        expectedMessage = "Error occurred while adding the API Product. A duplicate API context already exists for "
                + API_PRODUCT_CONTEXT_FOR_TENANT + " in the organization : " + TENANT_ORGANIZATION;

        testDuplicateContextValidation(apiProductDTO, PROVIDER, TENANT_ORGANIZATION, expectedMessage);
    }

    private APIProductDTO getAPIProductDTOForDuplicateContextTest() {

        APIProductDTO apiProductDTO = Mockito.mock(APIProductDTO.class);
        Mockito.when(apiProductDTO.getContext()).thenReturn(API_PRODUCT_CONTEXT);
        Mockito.when(apiProductDTO.getVersion()).thenReturn(API_PRODUCT_VERSION);
        Mockito.when(apiProductDTO.getName()).thenReturn(API_PRODUCT_NAME);
        Mockito.when(apiProductDTO.getProvider()).thenReturn(PROVIDER);
        Mockito.when(apiProductDTO.getPolicies()).thenReturn(new ArrayList<>());
        Mockito.when(apiProductDTO.getAdditionalProperties()).thenReturn(null);
        Mockito.when(apiProductDTO.getVisibility()).thenReturn(APIProductDTO.VisibilityEnum.PUBLIC);
        Mockito.when(apiProductDTO.getAuthorizationHeader()).thenReturn(APIConstants.AUTHORIZATION_HEADER_DEFAULT);
        return apiProductDTO;
    }

    private void testDuplicateContextValidation(APIProductDTO apiProductDTO, String provider, String organization,
            String expectedMessage) {

        try {
            PublisherCommonUtils.addAPIProductWithGeneratedSwaggerDefinition(apiProductDTO, provider, organization);
            Assert.fail("Duplicate context did not get identified");
        } catch (APIManagementException e) {
            Assert.assertTrue("Received an incorrect error message", e.getMessage().contains(expectedMessage));
        } catch (FaultGatewaysException e) {
            Assert.fail("Received an incorrect exception");
        }
    }

    @Test
    public void testDocumentCreationWithIllegalCharacters() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        when(RestApiCommonUtil.getLoggedInUserProvider()).thenReturn(apiProvider);
        DocumentDTO documentDto = new DocumentDTO();
        documentDto.setName(DOC_NAME);
        documentDto.setType(DocumentDTO.TypeEnum.valueOf(DOC_TYPE));
        documentDto.setSummary(DOC_SUMMARY);
        documentDto.setSourceType(DocumentDTO.SourceTypeEnum.valueOf(DOC_SOURCE_TYPE));
        documentDto.setVisibility(DocumentDTO.VisibilityEnum.valueOf(DOC_VISIBILITY));
        try {
            addDocumentationToAPI(documentDto, API_ID, ORGANIZATION);
            fail("Expected APIManagementException was not thrown");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Document name cannot contain illegal characters  "));
        }
    }

}
