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
package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_endpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.msf4j.Request;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class TestUtil {

    APIDTO getAPIDTO(String apiID, String apiName, String apiVersion, String apiContext) {

        EndPointDTO endPointDTO1 = new EndPointDTO();
        endPointDTO1.setId("123");
        endPointDTO1.setName("EP1");
        endPointDTO1.setEndpointConfig("endpointConfig1");

        API_endpointDTO apiEndpointDTO1 = new API_endpointDTO();
        apiEndpointDTO1.setKey("k1");
        apiEndpointDTO1.setType("EP");
        apiEndpointDTO1.setInline(endPointDTO1);

        List<API_endpointDTO> list1 = new ArrayList<>();
        list1.add(apiEndpointDTO1);

        Set<String> transport = new HashSet<>();
        transport.add("http");

        Set<Policy> policies = new HashSet<>();
        policies.add(new SubscriptionPolicy("Silver"));
        policies.add(new SubscriptionPolicy("Bronze"));

        Set<String> tags = new HashSet<>();
        tags.add("tag1");
        tags.add("tag2");

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

        APIDTO api = new APIDTO();
        api.setId(apiID);
        api.setName(apiName);
        api.setContext(apiContext);
        api.setVersion(apiVersion);
        api.setProvider("provider");
        api.setDescription("sample descripiton");
        api.setLifeCycleStatus("PUBLISHED");
        api.setEndpoint(list1);
        api.setWsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl");
        api.setResponseCaching("true");
        api.isDefaultVersion(true);
        api.setCacheTimeout(120);

        return api;
    }

    Request getMockRequest() {
        Request request = null;
        return request;
    }
}
