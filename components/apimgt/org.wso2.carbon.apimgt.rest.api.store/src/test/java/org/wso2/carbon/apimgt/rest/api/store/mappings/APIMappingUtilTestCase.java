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
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.apache.commons.collections.map.HashedMap;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class APIMappingUtilTestCase {

    @Test
    public void testAPIListDTO() throws APIManagementException {

        String api1Id = UUID.randomUUID().toString();
        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        String api2Id = UUID.randomUUID().toString();
        Endpoint api2SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd123").build();
        Endpoint api2ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef123").build();
        API api2 = createApi("provider1", api2Id, "testapi2", "1.0.0", "Test API 2 - version 1.0.0",
                createEndpointTypeToIdMap(api2SandBoxEndpointId, api2ProdEndpointId)).build();

        List<API> apiList = new ArrayList<>();
        apiList.add(api1);
        apiList.add(api2);

        APIMappingUtil apiMappingUtil = new APIMappingUtil();
        APIListDTO apiListDTO = apiMappingUtil.toAPIListDTO(apiList);

        Assert.assertEquals(apiListDTO.getList().get(0).getName(), "testapi1");
        Assert.assertEquals(apiListDTO.getList().get(1).getName(), "testapi2");
    }

    @Test
    public void testToAPIDTO() throws APIManagementException {
        String api1Id = UUID.randomUUID().toString();
        Endpoint api1SandBoxEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("abcd").build();
        Endpoint api1ProdEndpointId = new Endpoint.Builder().id(UUID.randomUUID().toString()).applicableLevel
                (APIMgtConstants.API_SPECIFIC_ENDPOINT).name("cdef").build();
        API api1 = createApi("provider1", api1Id, "testapi1", "1.0.0", "Test API 1 - version 1.0.0",
                createEndpointTypeToIdMap(api1SandBoxEndpointId, api1ProdEndpointId)).build();

        APIMappingUtil apiMappingUtil = new APIMappingUtil();
        APIDTO apidto = apiMappingUtil.toAPIDTO(api1);

        Assert.assertEquals(apidto.getName(), "testapi1");
    }

    private static API.APIBuilder createApi(String provider, String apiId, String name, String version, String
            description, Map<String, Endpoint> endpointTypeToIdMap)
            throws APIManagementException {
        Set<String> transport = new HashSet<>();
        transport.add("http");


        Set<Policy> policies = new HashSet<>();
        policies.add(new SubscriptionPolicy("Silver"));
        policies.add(new SubscriptionPolicy("Bronze"));

        Set<String> tags = new HashSet<>();
        tags.add("food");
        tags.add("beverage");

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner("John Doe");
        businessInformation.setBusinessOwnerEmail("john.doe@annonymous.com");
        businessInformation.setTechnicalOwner("Jane Doe");
        businessInformation.setBusinessOwnerEmail("jane.doe@annonymous.com");

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(Arrays.asList("GET", "POST", "DELETE"));
        corsConfiguration.setAllowHeaders(Arrays.asList("Authorization", "X-Custom"));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Collections.singletonList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder(provider, name, version).
                id(apiId).
                context(UUID.randomUUID().toString()).
                description(description).
                lifeCycleStatus("CREATED").
                apiDefinition("").
                wsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl").
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy(new APIPolicy("Gold")).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(new HashSet<>(Arrays.asList("customer", "manager", "employee"))).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                lastUpdatedTime(LocalDateTime.now()).
                endpoint(endpointTypeToIdMap);


        apiBuilder.uriTemplates(Collections.emptyMap());

        return apiBuilder;
    }

    private static Map<String, Endpoint> createEndpointTypeToIdMap(Endpoint sandboxEndpointId, Endpoint
            productionEndpointId) {

        Map<String, Endpoint> endpointTypeToIdMap = new HashedMap();
        endpointTypeToIdMap.put("PRODUCTION", productionEndpointId);
        endpointTypeToIdMap.put("SANDBOX", sandboxEndpointId);
        return endpointTypeToIdMap;
    }
}
