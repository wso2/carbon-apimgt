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
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMDependencyConfiguration;
import org.wso2.carbon.apimgt.impl.APIMDependencyConfigurationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.MCPUtils;
import org.wso2.carbon.apimgt.impl.utils.TierNameComparator;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
@PrepareForTest({RestApiCommonUtil.class, WorkflowExecutorFactory.class, APIUtil.class, OASParserUtil.class,
        MCPUtils.class})
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
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(false);
        Assert.assertFalse(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(false);
        Assert.assertFalse(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(false);
        Assert.assertFalse(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(endpoints)).thenReturn(true);
        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
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
        boolean flag = PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(apiDTO));
        Assert.assertTrue(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "300");
        flag = PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(apiDTO));
        Assert.assertTrue(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "300e");
        flag = PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(apiDTO));
        Assert.assertFalse(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "300.0");
        flag = PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(apiDTO));
        Assert.assertFalse(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "sdwed");
        flag = PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(apiDTO));
        Assert.assertFalse(flag);
        endpointConfigs.put(PublisherCommonUtils.SESSION_TIMEOUT_CONFIG_KEY, "1000000000000000000000000000000000");
        flag = PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(apiDTO));
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

    @Test
    public void testGetRemovedProductResourcesWhenResourceIsUnchanged() throws Exception {

        API existingAPI = createAPIWithResources(true, "GET:/hello", "POST:/hello");
        APIDTO updatedAPIDTO = createAPIDTOWithOperations("GET:/hello", "POST:/hello");

        List<APIResource> removedResources = Whitebox.invokeMethod(PublisherCommonUtils.class,
                "getRemovedProductResources", updatedAPIDTO, existingAPI);

        Assert.assertTrue("An unchanged product used resource was reported as removed: " + removedResources,
                removedResources.isEmpty());
    }

    /**
     * Resource paths of an API are case-sensitive, therefore changing only the letter case of a resource path
     * removes the original resource. This has to be reported when the resource is used by one or more API Products,
     * otherwise the product to resource mapping of the API Product is left dangling.
     */
    @Test
    public void testGetRemovedProductResourcesWhenResourcePathLetterCaseChanged() throws Exception {

        API existingAPI = createAPIWithResources(true, "GET:/hello");
        APIDTO updatedAPIDTO = createAPIDTOWithOperations("GET:/Hello");

        List<APIResource> removedResources = Whitebox.invokeMethod(PublisherCommonUtils.class,
                "getRemovedProductResources", updatedAPIDTO, existingAPI);

        Assert.assertEquals("A letter case only change of a product used resource path was not detected as a "
                + "removed resource", 1, removedResources.size());
        Assert.assertEquals(new APIResource("GET", "/hello"), removedResources.get(0));
    }

    /**
     * Path parameter names are part of the URL template stored against the API Product, hence a letter case change
     * of a path parameter has to be treated the same way as any other resource path change.
     */
    @Test
    public void testGetRemovedProductResourcesWhenPathParameterLetterCaseChanged() throws Exception {

        API existingAPI = createAPIWithResources(true, "GET:/pizza/{orderId}");
        APIDTO updatedAPIDTO = createAPIDTOWithOperations("GET:/pizza/{orderid}");

        List<APIResource> removedResources = Whitebox.invokeMethod(PublisherCommonUtils.class,
                "getRemovedProductResources", updatedAPIDTO, existingAPI);

        Assert.assertEquals("A letter case only change of a path parameter was not detected as a removed resource",
                1, removedResources.size());
        Assert.assertEquals(new APIResource("GET", "/pizza/{orderId}"), removedResources.get(0));
    }

    /**
     * HTTP verbs are persisted in upper case and are written to the API definition in lower case, therefore the verb
     * comparison has to stay case-insensitive.
     */
    @Test
    public void testGetRemovedProductResourcesWhenHttpVerbLetterCaseChanged() throws Exception {

        API existingAPI = createAPIWithResources(true, "GET:/hello");
        APIDTO updatedAPIDTO = createAPIDTOWithOperations("get:/hello");

        List<APIResource> removedResources = Whitebox.invokeMethod(PublisherCommonUtils.class,
                "getRemovedProductResources", updatedAPIDTO, existingAPI);

        Assert.assertTrue("A product used resource was reported as removed for an HTTP verb letter case difference: "
                + removedResources, removedResources.isEmpty());
    }

    @Test
    public void testGetRemovedProductResourcesWhenNonProductResourcePathLetterCaseChanged() throws Exception {

        API existingAPI = createAPIWithResources(false, "GET:/hello");
        APIDTO updatedAPIDTO = createAPIDTOWithOperations("GET:/Hello");

        List<APIResource> removedResources = Whitebox.invokeMethod(PublisherCommonUtils.class,
                "getRemovedProductResources", updatedAPIDTO, existingAPI);

        Assert.assertTrue("A resource which is not used by any API Product was reported as removed: "
                + removedResources, removedResources.isEmpty());
    }

    @Test
    public void testGetRemovedProductResourcesWhenResourcePathChanged() throws Exception {

        API existingAPI = createAPIWithResources(true, "GET:/v1/hello");
        APIDTO updatedAPIDTO = createAPIDTOWithOperations("GET:/v3/hello");

        List<APIResource> removedResources = Whitebox.invokeMethod(PublisherCommonUtils.class,
                "getRemovedProductResources", updatedAPIDTO, existingAPI);

        Assert.assertEquals("A removed product used resource was not detected", 1, removedResources.size());
        Assert.assertEquals(new APIResource("GET", "/v1/hello"), removedResources.get(0));
    }

    private API createAPIWithResources(boolean usedByProducts, String... verbAndPaths) {
        API api = new API(new APIIdentifier(PROVIDER, "PizzaShackAPI", API_PRODUCT_VERSION));
        api.setUuid(API_ID);
        Set<URITemplate> uriTemplates = new HashSet<>();
        for (String verbAndPath : verbAndPaths) {
            String[] verbAndPathParts = verbAndPath.split(":", 2);
            URITemplate uriTemplate = new URITemplate();
            uriTemplate.setHTTPVerb(verbAndPathParts[0]);
            uriTemplate.setUriTemplate(verbAndPathParts[1]);
            uriTemplate.setResourceURI(verbAndPathParts[1]);
            if (usedByProducts) {
                uriTemplate.addUsedByProduct(
                        new APIProductIdentifier(PROVIDER, API_PRODUCT_NAME, API_PRODUCT_VERSION));
            }
            uriTemplates.add(uriTemplate);
        }
        api.setUriTemplates(uriTemplates);
        return api;
    }

    private APIDTO createAPIDTOWithOperations(String... verbAndPaths) {
        APIDTO apiDto = new APIDTO();
        List<APIOperationsDTO> operations = new ArrayList<>();
        for (String verbAndPath : verbAndPaths) {
            String[] verbAndPathParts = verbAndPath.split(":", 2);
            APIOperationsDTO operation = new APIOperationsDTO();
            operation.setVerb(verbAndPathParts[0]);
            operation.setTarget(verbAndPathParts[1]);
            operations.add(operation);
        }
        apiDto.setOperations(operations);
        return apiDto;
    }

    @Test
    public void testValidateEndpointsRejectsArrayWithNoUsableUrl() throws APIManagementException {
        // A populated endpoint array whose entries carry no usable URL must raise the endpoint-specific error,
        // not silently produce an empty endpoint list.
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        List<Object> productionEndpoints = new ArrayList<>();
        productionEndpoints.add("not-an-object");                // non-object entry
        productionEndpoints.add(new HashMap<String, Object>());  // object without a url
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoints);
        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);

        try {
            PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto));
            fail("Expected ENDPOINT_URL_NOT_PROVIDED for an endpoint array with no usable URL");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.ENDPOINT_URL_NOT_PROVIDED.getErrorCode(),
                    e.getErrorHandler().getErrorCode());
        }
    }

    @Test
    public void testValidateEndpointsAcceptsMixedArrayWithOneUsableUrl() throws APIManagementException {
        // A mixed array must still validate: the usable URL is kept and the malformed entry is skipped.
        APIDTO apiDto = Mockito.mock(APIDTO.class);
        HashMap<String, Object> endpointConfig = new HashMap<>();
        endpointConfig.put(API_ENDPOINT_CONFIG_PROTOCOL_TYPE, "http");
        List<Object> productionEndpoints = new ArrayList<>();
        productionEndpoints.add("not-an-object");                // skipped
        HashMap<String, Object> valid = new HashMap<>();
        valid.put("url", "https://valid.test");
        productionEndpoints.add(valid);                          // usable
        endpointConfig.put(API_DATA_PRODUCTION_ENDPOINTS, productionEndpoints);
        Mockito.when(apiDto.getEndpointConfig()).thenReturn(endpointConfig);

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        PowerMockito.when(RestApiCommonUtil.getLoggedInUserTenantDomain()).thenReturn(ORGANIZATION);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.validateEndpointURLs(Mockito.any())).thenReturn(true);

        Assert.assertTrue(PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(apiDto)));
    }

    @Test
    public void testUpdateSwaggerSkipsRoleValidationOfSharedScopes() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        Scope sharedScope = createScope("shared_scope", "APPLICATION/test_role");
        mockSwaggerUpdate(apiProvider, oasParser, sharedScope);
        Mockito.when(apiProvider.isSharedScopeNameExists("shared_scope", -1234)).thenReturn(true);

        Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.never());
        APIUtil.isRoleNameExist(Mockito.anyString(), Mockito.eq("APPLICATION/test_role"));
        Mockito.verify(apiProvider, Mockito.never()).updateSharedScope(Mockito.any(Scope.class), Mockito.anyString());
        Assert.assertEquals(Collections.singleton(sharedScope), captureValidatedSharedScopes(apiProvider));
        Mockito.verify(apiProvider).saveSwaggerDefinition(Mockito.eq(api), Mockito.anyString(),
                Mockito.eq(ORGANIZATION));
    }

    @Test
    public void testUpdateSwaggerRejectsLocalScopeWithInvalidRole() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        Scope localScope = createScope("local_scope", "APPLICATION/test_role");
        mockSwaggerUpdate(apiProvider, oasParser, localScope);
        Mockito.when(APIUtil.isRoleNameExist(PROVIDER, "APPLICATION/test_role")).thenReturn(false);

        try {
            Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                    createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);
            fail("Expected APIManagementException for a local scope bound to a role that does not exist");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Role 'APPLICATION/test_role' does not exist."));
        }
        Mockito.verify(apiProvider, Mockito.never()).updateSharedScope(Mockito.any(Scope.class), Mockito.anyString());
        Mockito.verify(apiProvider, Mockito.never()).saveSwaggerDefinition(Mockito.any(API.class), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testUpdateSwaggerWithSharedScopeAndValidLocalScope() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        Scope sharedScope = createScope("shared_scope", "APPLICATION/test_role");
        Scope localScope = createScope("local_scope", "admin");
        mockSwaggerUpdate(apiProvider, oasParser, sharedScope, localScope);
        Mockito.when(apiProvider.isSharedScopeNameExists("shared_scope", -1234)).thenReturn(true);
        Mockito.when(APIUtil.isRoleNameExist(PROVIDER, "admin")).thenReturn(true);

        Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.isRoleNameExist(PROVIDER, "admin");
        PowerMockito.verifyStatic(APIUtil.class, Mockito.never());
        APIUtil.isRoleNameExist(Mockito.anyString(), Mockito.eq("APPLICATION/test_role"));
        Assert.assertEquals(Collections.singleton(sharedScope), captureValidatedSharedScopes(apiProvider));
        Mockito.verify(apiProvider, Mockito.never()).updateSharedScope(Mockito.any(Scope.class), Mockito.anyString());
        Mockito.verify(apiProvider).saveSwaggerDefinition(Mockito.eq(api), Mockito.anyString(),
                Mockito.eq(ORGANIZATION));
    }

    @Test
    public void testUpdateSwaggerWithValidLocalScope() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        Scope localScope = createScope("local_scope", "Application/test_role");
        mockSwaggerUpdate(apiProvider, oasParser, localScope);
        Mockito.when(APIUtil.isRoleNameExist(PROVIDER, "Application/test_role")).thenReturn(true);

        Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);

        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.isRoleNameExist(PROVIDER, "Application/test_role");
        Assert.assertEquals(Collections.singleton(localScope), api.getScopes());
        Assert.assertEquals("Application/test_role", localScope.getRoles());
        Assert.assertTrue(captureValidatedSharedScopes(apiProvider).isEmpty());
        Mockito.verify(apiProvider, Mockito.never()).updateSharedScope(Mockito.any(Scope.class), Mockito.anyString());
        Mockito.verify(apiProvider).saveSwaggerDefinition(Mockito.eq(api), Mockito.anyString(),
                Mockito.eq(ORGANIZATION));
    }

    @Test
    public void testUpdateSwaggerWithSharedScopeAndInvalidLocalScope() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        // The shared scope is processed first, so skipping it must not skip validation of the local scope after it.
        Scope sharedScope = createScope("shared_scope", "APPLICATION/test_role");
        Scope localScope = createScope("local_scope", "invalid_role");
        mockSwaggerUpdate(apiProvider, oasParser, sharedScope, localScope);
        Mockito.when(apiProvider.isSharedScopeNameExists("shared_scope", -1234)).thenReturn(true);
        Mockito.when(APIUtil.isRoleNameExist(PROVIDER, "invalid_role")).thenReturn(false);

        try {
            Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                    createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);
            fail("Expected APIManagementException for a local scope bound to a role that does not exist");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Role 'invalid_role' does not exist."));
        }
        Mockito.verify(apiProvider, Mockito.never()).updateSharedScope(Mockito.any(Scope.class), Mockito.anyString());
        Mockito.verify(apiProvider, Mockito.never()).saveSwaggerDefinition(Mockito.any(API.class), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testUpdateSwaggerRejectsScopeAssignedLocallyByAnotherAPI() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        Scope otherApiScope = createScope("other_api_scope", "APPLICATION/test_role");
        mockSwaggerUpdate(apiProvider, oasParser, otherApiScope);
        Mockito.when(apiProvider.isScopeKeyAssignedLocally("TestAPI", "other_api_scope", ORGANIZATION))
                .thenReturn(true);

        try {
            Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                    createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);
            fail("Expected APIManagementException for a scope assigned locally by another API");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.SCOPE_ALREADY_ASSIGNED.getErrorCode(),
                    e.getErrorHandler().getErrorCode());
        }
        Mockito.verify(apiProvider, Mockito.never()).updateSharedScope(Mockito.any(Scope.class), Mockito.anyString());
        Mockito.verify(apiProvider, Mockito.never()).saveSwaggerDefinition(Mockito.any(API.class), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testUpdateSwaggerRejectsLocalScopeWithOneInvalidRoleOfMany() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        APIDefinition oasParser = Mockito.mock(APIDefinition.class);
        API api = createAPIForSwaggerUpdate();
        Scope localScope = createScope("local_scope", "admin,invalid_role");
        mockSwaggerUpdate(apiProvider, oasParser, localScope);
        Mockito.when(APIUtil.isRoleNameExist(PROVIDER, "admin")).thenReturn(true);
        Mockito.when(APIUtil.isRoleNameExist(PROVIDER, "invalid_role")).thenReturn(false);

        try {
            Whitebox.invokeMethod(PublisherCommonUtils.class, "prepareForUpdateSwagger", API_ID,
                    createValidationResponse(oasParser), false, apiProvider, ORGANIZATION, oasParser, api, false);
            fail("Expected APIManagementException for a local scope bound to a role that does not exist");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("Role 'invalid_role' does not exist."));
        }
        PowerMockito.verifyStatic(APIUtil.class);
        APIUtil.isRoleNameExist(PROVIDER, "admin");
        Mockito.verify(apiProvider, Mockito.never()).saveSwaggerDefinition(Mockito.any(API.class), Mockito.anyString(),
                Mockito.anyString());
    }

    @Test
    public void testValidateScopesSkipsLocalScopeChecksForSharedScope() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Scope sharedScope = createScope("shared_scope", "invalid_role");
        sharedScope.setName("");
        sharedScope.setDescription(null);
        API api = createAPIForSwaggerUpdate();
        api.setScopes(new HashSet<>(Collections.singletonList(sharedScope)));
        mockScopeValidation(apiProvider);
        Mockito.when(apiProvider.isSharedScopeNameExists("shared_scope", -1234)).thenReturn(true);
        Mockito.when(APIUtil.hasRestrictedScopePrefix("shared_scope")).thenReturn(true);

        PublisherCommonUtils.validateScopes(api);

        PowerMockito.verifyStatic(APIUtil.class, Mockito.never());
        APIUtil.isRoleNameExist(Mockito.anyString(), Mockito.anyString());
        // Display name and description of a shared scope are left for the key manager to replace.
        Assert.assertEquals("", sharedScope.getName());
        Assert.assertNull(sharedScope.getDescription());
        Assert.assertEquals(Collections.singleton(sharedScope), captureValidatedSharedScopes(apiProvider));
    }

    @Test
    public void testValidateScopesSetsDefaultsForLocalScope() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Scope localScope = createScope("local_scope", null);
        localScope.setName(" ");
        localScope.setDescription(null);
        API api = createAPIForSwaggerUpdate();
        api.setScopes(new HashSet<>(Collections.singletonList(localScope)));
        mockScopeValidation(apiProvider);

        PublisherCommonUtils.validateScopes(api);

        Assert.assertEquals("local_scope", localScope.getName());
        Assert.assertEquals("", localScope.getDescription());
        Assert.assertTrue(captureValidatedSharedScopes(apiProvider).isEmpty());
    }

    @Test
    public void testValidateScopesSkipsScopeLookupsForAllowedScope() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Scope allowedScope = createScope("allowed_scope", null);
        API api = createAPIForSwaggerUpdate();
        api.setScopes(new HashSet<>(Collections.singletonList(allowedScope)));
        mockScopeValidation(apiProvider);
        Mockito.when(APIUtil.isAllowedScope("allowed_scope")).thenReturn(true);

        PublisherCommonUtils.validateScopes(api);

        Mockito.verify(apiProvider, Mockito.never()).isScopeKeyAssignedLocally(Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString());
        Mockito.verify(apiProvider, Mockito.never()).isSharedScopeNameExists(Mockito.anyString(), Mockito.anyInt());
        Assert.assertTrue(captureValidatedSharedScopes(apiProvider).isEmpty());
    }

    @Test
    public void testValidateScopesRejectsLocalScopeWithRestrictedPrefix() throws Exception {

        APIProvider apiProvider = Mockito.mock(APIProvider.class);
        Scope localScope = createScope("restricted_scope", null);
        API api = createAPIForSwaggerUpdate();
        api.setScopes(new HashSet<>(Collections.singletonList(localScope)));
        mockScopeValidation(apiProvider);
        Mockito.when(APIUtil.hasRestrictedScopePrefix("restricted_scope")).thenReturn(true);

        try {
            PublisherCommonUtils.validateScopes(api);
            fail("Expected APIManagementException for a local scope with a restricted prefix");
        } catch (APIManagementException e) {
            Assert.assertEquals(ExceptionCodes.INVALID_SCOPE_NAME.getErrorCode(), e.getErrorHandler().getErrorCode());
        }
        Mockito.verify(apiProvider, Mockito.never()).validateSharedScopes(Mockito.anySet(), Mockito.anyString());
    }

    private API createAPIForSwaggerUpdate() {

        API api = new API(new APIIdentifier(PROVIDER, "TestAPI", "1.0.0"));
        api.setUuid(API_ID);
        api.setOrganization(ORGANIZATION);
        api.setType("HTTP");
        return api;
    }

    private Scope createScope(String key, String roles) {

        Scope scope = new Scope();
        scope.setKey(key);
        scope.setName(key);
        scope.setDescription("");
        scope.setRoles(roles);
        return scope;
    }

    private APIDefinitionValidationResponse createValidationResponse(APIDefinition oasParser) {

        APIDefinitionValidationResponse response = new APIDefinitionValidationResponse();
        response.setJsonContent("{}");
        response.setParser(oasParser);
        return response;
    }

    @SuppressWarnings("unchecked")
    private Set<Scope> captureValidatedSharedScopes(APIProvider apiProvider) throws APIManagementException {

        ArgumentCaptor<Set<Scope>> captor = ArgumentCaptor.forClass(Set.class);
        Mockito.verify(apiProvider).validateSharedScopes(captor.capture(), Mockito.eq(ORGANIZATION));
        return captor.getValue();
    }

    private void mockSwaggerUpdate(APIProvider apiProvider, APIDefinition oasParser, Scope... scopes)
            throws APIManagementException {

        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setUriTemplate("/test");
        uriTemplate.setHTTPVerb("GET");
        Set<URITemplate> uriTemplates = new HashSet<>();
        uriTemplates.add(uriTemplate);
        Mockito.when(oasParser.getURITemplates(Mockito.anyString())).thenReturn(uriTemplates);
        Mockito.when(oasParser.getScopes(Mockito.anyString())).thenReturn(new LinkedHashSet<>(Arrays.asList(scopes)));
        Mockito.when(oasParser.populateCustomManagementInfo(Mockito.anyString(), Mockito.any(), Mockito.any()))
                .thenReturn("{}");
        Mockito.when(apiProvider.getRemovedProductResources(Mockito.anySet(), Mockito.any(API.class)))
                .thenReturn(new ArrayList<>());
        PowerMockito.mockStatic(OASParserUtil.class);
        Mockito.when(OASParserUtil.preProcess(Mockito.anyString(), Mockito.any())).thenReturn("{}");
        PowerMockito.mockStatic(MCPUtils.class);
        APIMDependencyConfigurationService dependencyConfigurationService =
                Mockito.mock(APIMDependencyConfigurationService.class);
        Mockito.when(dependencyConfigurationService.getAPIMDependencyConfigurations())
                .thenReturn(Mockito.mock(APIMDependencyConfiguration.class));
        ServiceReferenceHolder.getInstance().setAPIMDependencyConfigurationService(dependencyConfigurationService);
        mockScopeValidation(apiProvider);
    }

    private void mockScopeValidation(APIProvider apiProvider) throws APIManagementException {

        PowerMockito.mockStatic(RestApiCommonUtil.class);
        Mockito.when(RestApiCommonUtil.getLoggedInUsername()).thenReturn(PROVIDER);
        Mockito.when(RestApiCommonUtil.getProvider(PROVIDER)).thenReturn(apiProvider);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(APIUtil.getInternalOrganizationId(ORGANIZATION)).thenReturn(-1234);
        Mockito.when(APIUtil.getTenantDomainFromTenantId(-1234)).thenReturn(ORGANIZATION);
    }
}
