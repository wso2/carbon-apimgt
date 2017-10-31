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
package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.collections.map.HashedMap;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentContent;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.transport.http.netty.message.HTTPCarbonMessage;
import org.wso2.msf4j.Request;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class TestUtil {

    private final static Logger logger = LoggerFactory.getLogger(TestUtil.class);
    private static final String USER = "admin";

    protected static void printTestMethodName() {
        logger.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }

    // Sample request to be used by tests
    protected static Request getRequest() throws APIMgtSecurityException {
        CarbonMessage carbonMessage = new HTTPCarbonMessage();
        carbonMessage.setProperty("LOGGED_IN_USER", USER);
        Request request = new Request(carbonMessage);
        return request;
    }

    protected static DocumentInfo createAPIDoc(String docId, String name, String fileName, String summary,
                                               DocumentInfo.DocType docType, String otherType, DocumentInfo.SourceType
                                                       sourceType, String sourceUrl, DocumentInfo.Visibility visibility) {

        return new DocumentInfo.Builder().
                id(docId).
                name(name).
                fileName(fileName).
                summary(summary).
                type(docType).
                otherType(otherType).
                sourceType(sourceType).
                sourceURL(sourceUrl).
                visibility(visibility).build();
    }

    protected static DocumentContent createDocContent(DocumentInfo documentInfo, String
            inlineContent, InputStream fileContent) {

        return new DocumentContent.Builder().documentInfo(documentInfo).inlineContent(inlineContent).
                fileContent(fileContent).build();

    }

    protected static Map<String, Endpoint> createEndpointTypeToIdMap(Endpoint sandboxEndpointId, Endpoint
            productionEndpointId) {

        Map<String, Endpoint> endpointTypeToIdMap = new HashedMap();
        endpointTypeToIdMap.put("PRODUCTION", productionEndpointId);
        endpointTypeToIdMap.put("SANDBOX", sandboxEndpointId);
        return endpointTypeToIdMap;
    }

    protected static API.APIBuilder createApi(String provider, String apiId, String name, String version, String
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
                apiDefinition("swagger definition").
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

}
