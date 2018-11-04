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
import org.wso2.carbon.apimgt.impl.internal.APIManagerComponent;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.service.TenantRegistryLoader;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

/**
 * This is the test class for {@link APIMappingUtil}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({RestApiUtil.class, APIUtil.class, MultitenantUtils.class, PrivilegedCarbonContext.class,
        ServiceReferenceHolder.class, APIManagerComponent.class})

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
     * This method tests the behaviour of the fromAPItoDTO method when api is configured with endpoint security.
     *
     * @throws APIManagementException API Management Exception.
     */
    @Test
    public void testEndpointSecurityFromAPItoDTO() throws APIManagementException, UserStoreException,
            RegistryException {
        System.setProperty(CARBON_HOME, "");
        API api = getSampleAPI();
        api.setEndpointSecured(true);
        api.setEndpointAuthDigest(false);
        api.setEndpointUTUsername("testuser");
        api.setEndpointUTPassword("password");

        String json = "{\"ExposeEndpointPassword\":\"true\"}";
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.when(APIUtil.replaceEmailDomainBack(api.getId().getProviderName())).thenReturn("admin");
        Mockito.when(MultitenantUtils.getTenantDomain(Mockito.anyString())).thenReturn("carbon.super");
        CORSConfiguration corsConfiguration = Mockito.mock(CORSConfiguration.class);
        PowerMockito.when(APIUtil.getDefaultCorsConfiguration()).thenReturn(corsConfiguration);
        Mockito.when(corsConfiguration.getAccessControlAllowHeaders()).thenReturn(new ArrayList<String>());
        Mockito.when(corsConfiguration.getAccessControlAllowMethods()).thenReturn(new ArrayList<String>());
        Mockito.when(corsConfiguration.getAccessControlAllowOrigins()).thenReturn(new ArrayList<String>());
        Mockito.when(corsConfiguration.isAccessControlAllowCredentials()).thenReturn(false);
        Mockito.when(corsConfiguration.isCorsConfigurationEnabled()).thenReturn(false);

        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(APIManagerComponent.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(-1234);
        RegistryService registryService = Mockito.mock(RegistryService.class);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryService);
        UserRegistry userRegistry = Mockito.mock(UserRegistry.class);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(registryService.getConfigSystemRegistry(-1234)).thenReturn(userRegistry);
        Mockito.when(userRegistry.resourceExists(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(true);
        Mockito.when(userRegistry.get(APIConstants.API_TENANT_CONF_LOCATION)).thenReturn(resource);
        Mockito.when(resource.getContent()).thenReturn(json.getBytes());
        TenantIndexingLoader tenantIndexingLoader = Mockito.mock(TenantIndexingLoader.class);
        TenantRegistryLoader tenantRegistryLoader = Mockito.mock(TenantRegistryLoader.class);
        Mockito.when(serviceReferenceHolder.getIndexLoaderService()).thenReturn(tenantIndexingLoader);
        Mockito.when(APIManagerComponent.getTenantRegistryLoader()).thenReturn(tenantRegistryLoader);
        Mockito.doNothing().when(tenantRegistryLoader).loadTenantRegistry(-1234);

        APIDetailedDTO apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed with endpoint security expose password is " +
                "'true'", apiDetailedDTO);
        Assert.assertEquals(apiDetailedDTO.getEndpointSecurity().getPassword(), "password");

        json = "{\"ExposeEndpointPassword\": true}";
        Mockito.when(resource.getContent()).thenReturn(json.getBytes());
        apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed with endpoint security expose password is " +
                "true", apiDetailedDTO);
        Assert.assertEquals(apiDetailedDTO.getEndpointSecurity().getPassword(), "password");

        json = "{\"ExposeEndpointPassword\": false}";
        Mockito.when(resource.getContent()).thenReturn(json.getBytes());
        apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed with endpoint security expose password false",
                apiDetailedDTO);
        Assert.assertEquals("Conversion from API to dto failed with endpoint security expose password false",
                apiDetailedDTO.getEndpointSecurity().getPassword(), "");

        json = "{\"EnableMonetization\":\"true\"}";
        Mockito.when(resource.getContent()).thenReturn(json.getBytes());
        apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed when expose endpoint password config not exist",
                apiDetailedDTO);
        Assert.assertEquals("Conversion from API to dto failed when expose endpoint password config not exist",
                apiDetailedDTO.getEndpointSecurity().getPassword(), "");

        json = "{\"ExposeEndpointPassword\":\"123\"}";
        Mockito.when(resource.getContent()).thenReturn(json.getBytes());
        apiDetailedDTO = APIMappingUtil.fromAPItoDTO(api);
        Assert.assertNotNull("Conversion from API to dto failed when expose endpoint password config is '123'",
                apiDetailedDTO);
        Assert.assertEquals("Conversion from API to dto failed when expose endpoint password config is '123'",
                apiDetailedDTO.getEndpointSecurity().getPassword(), "");
        try {
            json = "{\"ExposeEndpointPassword\": aaa}";
            Mockito.when(resource.getContent()).thenReturn(json.getBytes());
            APIDetailedDTO apiDetailDTO = APIMappingUtil.fromAPItoDTO(api);
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("ParseException thrown when parsing API tenant config from " +
                    "registry while reading ExposeEndpointPassword config"));
        }

        json = "{\"ExposeEndpointPassword\": false}";
        try {
            Mockito.when(resource.getContent()).thenReturn(json.getBytes());
            Mockito.doThrow(UserStoreException.class).when(tenantManager).getTenantId("carbon.super");
            APIDetailedDTO apiDetailDTO = APIMappingUtil.fromAPItoDTO(api);
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("UserStoreException thrown when getting API tenant config from " +
                    "registry while reading ExposeEndpointPassword config"));
        }

        try {
            json = "{\"ExposeEndpointPassword\": false}";
            Mockito.when(resource.getContent()).thenReturn(json.getBytes());
            Mockito.doThrow(RegistryException.class).when(registryService).getConfigSystemRegistry(-1234);
            Mockito.reset(tenantManager);
            Mockito.when(tenantManager.getTenantId("carbon.super")).thenReturn(-1234);
            APIDetailedDTO apiDetailDTO = APIMappingUtil.fromAPItoDTO(api);
        } catch (APIManagementException ex) {
            Assert.assertTrue(ex.getMessage().contains("RegistryException thrown when getting API tenant config from " +
                    "registry while reading ExposeEndpointPassword config"));
        }
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
