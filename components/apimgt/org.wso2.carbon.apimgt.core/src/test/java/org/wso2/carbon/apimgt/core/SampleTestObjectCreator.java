/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SampleTestObjectCreator {
    public static String apiDefinition;
    public static InputStream inputStream;
    private static final Logger log = LoggerFactory.getLogger(SampleTestObjectCreator.class);
    static String endpointId = UUID.randomUUID().toString();

    static {
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("swagger.json");
            apiDefinition = IOUtils.toString(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public static API.APIBuilder createDefaultAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");
        transport.add("https");

        List<String> tags = new ArrayList<>();
        tags.add("climate");

        List<String> policies = new ArrayList<>();
        policies.add("Gold");
        policies.add("Silver");
        policies.add("Bronze");

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String permissionJson = "[{\"groupId\" : 1000, \"permission\" : " +
                "[\"READ\",\"UPDATE\"]},{\"groupId\" : 1001, \"permission\" : [\"READ\",\"UPDATE\"]}]";

        API.APIBuilder apiBuilder = new API.APIBuilder("admin", "WeatherAPI", "1.0.0").
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Weather Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                lifecycleInstanceId(UUID.randomUUID().toString()).
                endpoint(Collections.emptyMap()).
                wsdlUri("http://localhost:9443/echo?wsdl").
                isResponseCachingEnabled(false).
                cacheTimeout(60).
                isDefaultVersion(false).
                apiPolicy("Unlimited").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new ArrayList<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("admin").
                updatedBy("admin").
                lastUpdatedTime(LocalDateTime.now()).
                permission(permissionJson).
                uriTemplates(getMockUriTemplates()).
                apiDefinition(apiDefinition);
        HashMap map = new HashMap();
        map.put("1000", 6);
        map.put("1001", 4);
        apiBuilder.permissionMap(map);
        return apiBuilder;
    }

    public static API getSummaryFromAPI(API api) {
        return new API.APIBuilder(api.getProvider(), api.getName(), api.getVersion()).
                id(api.getId()).
                context(api.getContext()).
                description(api.getDescription()).
                lifeCycleStatus(api.getLifeCycleStatus()).
                lifecycleInstanceId(api.getLifecycleInstanceId()).build();
    }

    public static API copyAPIIgnoringNonEditableFields(API fromAPI, API toAPI) {
        API.APIBuilder builder = new API.APIBuilder(toAPI);

        return builder.provider(fromAPI.getProvider()).
                id(fromAPI.getId()).
                name(fromAPI.getName()).
                version(fromAPI.getVersion()).
                context(fromAPI.getContext()).
                createdTime(fromAPI.getCreatedTime()).
                createdBy(fromAPI.getCreatedBy()).
                lifecycleInstanceId(fromAPI.getLifecycleInstanceId()).
                lifeCycleStatus(fromAPI.getLifeCycleStatus()).
                copiedFromApiId(fromAPI.getCopiedFromApiId()).build();

    }

    public static LifecycleState getMockLifecycleStateObject(String lifecycleId) {
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setLcName("API_LIFECYCLE");
        lifecycleState.setLifecycleId(lifecycleId);
        lifecycleState.setState("PUBLISHED");
        return lifecycleState;
    }

    public static API getMockApiSummaryObject() {
        return new API.APIBuilder("admin", "Sample", "1.0.0").build();
    }

    public static API.APIBuilder createAlternativeAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");

        List<String> tags = new ArrayList<>();
        tags.add("food");
        tags.add("beverage");

        List<String> policies = new ArrayList<>();
        policies.add("Silver");
        policies.add("Bronze");

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
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder("Adam", "restaurantAPI", "0.9").
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Food & Beverage Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                endpoint(Collections.emptyMap()).
                wsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl").
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy("Gold").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList("customer", "manager", "employee")).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                apiDefinition(apiDefinition).
                lastUpdatedTime(LocalDateTime.now());

        return apiBuilder;
    }

    public static API.APIBuilder createUniqueAPI() {
        List<String> transport = new ArrayList<>();
        transport.add("http");

        List<String> tags = new ArrayList<>();
        tags.add("food");
        tags.add("beverage");

        List<String> policies = new ArrayList<>();
        policies.add("Silver");
        policies.add("Bronze");

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
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                "1.0.0").
                id(UUID.randomUUID().toString()).
                context(UUID.randomUUID().toString()).
                description("Get Food & Beverage Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                endpoint(Collections.emptyMap()).
                wsdlUri("http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl").
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy("Gold").
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList("customer", "manager", "employee")).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy("Adam Doe").
                uriTemplates(Collections.emptyMap()).
                apiDefinition(apiDefinition).
                lastUpdatedTime(LocalDateTime.now());

        return apiBuilder;
    }


    public static API.APIBuilder createCustomAPI(String name, String version, String context) {
        API.APIBuilder apiBuilder = createDefaultAPI();
        apiBuilder.name(name);
        apiBuilder.version(version);
        apiBuilder.context(context);
        apiBuilder.endpoint(Collections.emptyMap());
        apiBuilder.uriTemplates(Collections.EMPTY_MAP);
        return apiBuilder;
    }

    public static API copyAPISummary(API api) {
        return new API.APIBuilder(api.getProvider(), api.getName(), api.getVersion()).
                id(api.getId()).
                context(api.getContext()).
                description(api.getDescription()).
                lifeCycleStatus(api.getLifeCycleStatus()).
                lifecycleInstanceId(api.getLifecycleInstanceId()).build();
    }

    public static String createAlternativeSwaggerDefinition() throws IOException {
        return IOUtils
                .toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("api/alternativeSwagger.json"));
    }

    public static String createSampleGatewayConfig() {
        try {
            return IOUtils
                    .toString(Thread.currentThread().getContextClassLoader()
                            .getResourceAsStream("api/sampleGatewayConfig.bal"));
        } catch (IOException e) {
            log.error("Error while reading 'api/sampleGatewayConfig.bal'", e);
            return null;
        }
    }

    public static String createAlternativeGatewayConfig() throws IOException {
        return IOUtils
                .toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("api/alternativeGatewayConfig.bal"));
    }

    public static InputStream createDefaultThumbnailImage() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("api/thumbnail1.jpg");
    }

    public static InputStream createAlternativeThumbnailImage() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("api/thumbnail2.jpg");
    }

    public static DocumentInfo createDefaultDocumentationInfo() {
        //created by admin
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.id(UUID.randomUUID().toString());
        builder.name("CalculatorDoc");
        builder.type(DocumentInfo.DocType.HOWTO);
        builder.summary("Summary of Calculator Documentation");
        builder.sourceType(DocumentInfo.SourceType.INLINE);
        builder.sourceURL("");
        builder.otherType("");
        builder.visibility(DocumentInfo.Visibility.API_LEVEL);
        builder.createdTime(LocalDateTime.now());
        builder.lastUpdatedTime(LocalDateTime.now());
        return builder.build();
    }

    public static DocumentInfo createAlternativeDocumentationInfo(String uuid) {
        //created by admin
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.id(uuid);
        builder.name("CalculatorDoc");
        builder.type(DocumentInfo.DocType.HOWTO);
        builder.summary("Summary of Calculator Documentation - alternative");
        builder.sourceType(DocumentInfo.SourceType.INLINE);
        builder.sourceURL("");
        builder.otherType("");
        builder.visibility(DocumentInfo.Visibility.API_LEVEL);
        builder.createdTime(LocalDateTime.now());
        builder.lastUpdatedTime(LocalDateTime.now());
        return builder.build();
    }

    public static String createDefaultInlineDocumentationContent() throws IOException {
        return IOUtils
                .toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("document/inline1.txt"));
    }

    public static String createAlternativeInlineDocumentationContent() throws IOException {
        return IOUtils
                .toString(Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("document/inline2.txt"));
    }

    public static Application createDefaultApplication() {
        //created by admin
        Application application = new Application("TestApp", "admin");
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp");
        application.setDescription("This is a test application");
        application.setGroupId("groupx");
        application.setStatus("CREATED");
        application.setTier("50PerMin");
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser("admin");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createAlternativeApplication() {
        //created by admin and updated by admin2
        Application application = new Application("TestApp2", "admin");
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp2");
        application.setDescription("This is test application 2");
        application.setGroupId("groupx2");
        application.setStatus("APPROVED");
        application.setTier("20PerMin");
        application.setUpdatedUser("admin2");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createCustomApplication(String applicationName, String owner) {
        Application application = new Application(applicationName, owner);
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl("http://localhost/myapp");
        application.setDescription("This is a test application");
        application.setGroupId("groupx");
        application.setStatus("CREATED");
        application.setTier("50PerMin");
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser("admin");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static APIPolicy createDefaultAPIPolicy() {
        APIPolicy apiPolicy = new APIPolicy("SampleAPIPolicy");
        apiPolicy.setDisplayName("SampleAPIPolicy");
        apiPolicy.setDescription("SampleAPIPolicy Description");
        apiPolicy.setUserLevel("API");
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("s");
        requestCountLimit.setRequestCount(10000);
        requestCountLimit.setUnitTime(1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return apiPolicy;
    }

    public static ApplicationPolicy createDefaultApplicationPolicy() {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy("SampleAppPolicy");
        applicationPolicy.setDisplayName("SampleAppPolicy");
        applicationPolicy.setDescription("SampleAppPolicy Description");
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("s");
        requestCountLimit.setRequestCount(10000);
        requestCountLimit.setUnitTime(1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return applicationPolicy;
    }

    public static SubscriptionPolicy createDefaultSubscriptionPolicy() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy("SampleAPIPolicy");
        subscriptionPolicy.setDisplayName("SampleSubscriptionPolicy");
        subscriptionPolicy.setDescription("SampleSubscriptionPolicy Description");
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("s");
        requestCountLimit.setRequestCount(10000);
        requestCountLimit.setUnitTime(1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return subscriptionPolicy;
    }

    public static DocumentInfo getMockDocumentInfoObject(String docId) {

        DocumentInfo.Builder builder = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide")
                .id(docId);
        return builder.build();
    }

    public static List<DocumentInfo> getMockDocumentInfoObjectsList() {
        List<DocumentInfo> docList = new ArrayList<>();

        DocumentInfo doc1 = new DocumentInfo.Builder().fileName("sample1").id("123").build();
        DocumentInfo doc2 = new DocumentInfo.Builder().fileName("sample1").id("124").build();
        DocumentInfo doc3 = new DocumentInfo.Builder().fileName("sample1").id("125").build();

        docList.add(doc1);
        docList.add(doc2);
        docList.add(doc3);

        return docList;
    }

    public static Endpoint createMockEndpoint() {
        return new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:8280'}").id(endpointId)
                .maxTps(1000L).security("{'enabled':false}").name("Endpoint1").build();
    }

    public static Endpoint createUpdatedEndpoint() {
        return new Endpoint.Builder().endpointConfig("{'type':'soap','url':'http://localhost:8280'}").id(endpointId)
                .maxTps(1000L).security("{'enabled':false}").name("Endpoint1").build();
    }

    public static Endpoint createAlternativeEndpoint() {
        String uuid = UUID.randomUUID().toString();
        return new Endpoint.Builder().endpointConfig("{'type':'soap','url':'http://localhost:8280'}").id(uuid)
                .maxTps(1000L).security("{'enabled':false}").build();

    }

    public static Map<String, String> getMockEndpointMap() {
        Map<String, String> endpointMap = new HashedMap();
        endpointMap.put("production", endpointId);
        return endpointMap;
    }

    public static Map<String, UriTemplate> getMockUriTemplates() {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.endpoint(getMockEndpointMap());
        uriTemplateBuilder.templateId("getApisApiIdGet");
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy("Unlimited");
        uriTemplateBuilder.httpVerb("GET");
        uriTemplateMap.put("getApisApiIdGet", uriTemplateBuilder.build());
        return uriTemplateMap;
    }

    public static Label.Builder createLabel(String name) {

        List<String> accessUrls = new ArrayList<>();
        accessUrls.add("https://test." + name);
        return new Label.Builder().
                name(name).
                accessUrls(accessUrls);
    }

    public static Workflow createWorkflow(String workflowReferenceID) {
        Workflow workflow = new Workflow();
        workflow.setExternalWorkflowReference(workflowReferenceID);
        workflow.setStatus(WorkflowStatus.CREATED);
        workflow.setCreatedTime(LocalDateTime.now());
        workflow.setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
        workflow.setWorkflowReference(UUID.randomUUID().toString());
        return workflow;
    }
}
