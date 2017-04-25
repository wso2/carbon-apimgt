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
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
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
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
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

    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static final String GOLD_TIER = "Gold";
    private static final String SILVER_TIER = "Silver";
    private static final String BRONZE_TIER = "Bronze";
    private static final String ADMIN = "admin";
    private static final String TAG_CLIMATE = "climate";
    private static final String TAG_FOOD = "food";
    private static final String TAG_BEVERAGE = "beverage";
    private static final String API_VERSION = "1.0.0";
    private static final String NAME_BUSINESS_OWNER_1 = "John Doe";
    private static final String NAME_BUSINESS_OWNER_2 = "Jane Doe";
    private static final String EMAIL_BUSINESS_OWNER_1 = "john.doe@annonymous.com";
    private static final String EMAIL_BUSINESS_OWNER_2 = "jane.doe@annonymous.com";
    private static final String EMPTY_STRING = "";
    private static final String SAMPLE_API_POLICY = "SampleAPIPolicy";
    private static final String SAMPLE_API_POLICY_DESCRIPTION = "SampleAPIPolicy Description";
    private static final String SAMPLE_APP_POLICY = "SampleAppPolicy";
    private static final String SAMPLE_APP_POLICY_DESCRIPTION = "SampleAppPolicy Description";
    private static final String SAMPLE_SUBSCRIPTION_POLICY = "SampleSubscriptionPolicy";
    private static final String SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION = "SampleSubscriptionPolicy Description";
    private static final String PRODUCTION_ENDPOINT = "production";
    private static final String SAMPLE_API_WSDL = "http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl";
    private static final String FIFTY_PER_MIN_TIER = "50PerMin";
    private static final String TWENTY_PER_MIN_TIER = "20PerMin";
    private static final String TIME_UNIT_SECONDS = "s";
    private static final String TIME_UNIT_MONTH = "Month";
    private static final String CUSTOMER_ROLE = "customer";
    private static final String EMPLOYEE_ROLE = "employee";
    private static final String MANAGER_ROLE = "manager";
    private static final String ALLOWED_HEADER_AUTHORIZATION = "Authorization";
    private static final String ALLOWED_HEADER_CUSTOM = "X-Custom";
    private static final String API_CREATOR = "Adam Doe";
    private static final String CALLBACK_URL_1 = "http://localhost/myapp";
    private static final String CALLBACK_URL_2 = "http://localhost/myapp2";
    private static final String GROUP_1 = "groupx";
    private static final String GROUP_2 = "groupx2";
    private static final String SAMPLE_DOC_NAME = "CalculatorDoc";
    private static final String TEST_APP_1 = "TestApp";
    private static final String TEST_APP_2 = "TestApp2";
    private static final String TEMPLATE_ID = "getApisApiIdGet";
    private static final String ACCESS_URL = "https://test.";
    private static final String ALT_SWAGGER_PATH = "api/alternativeSwagger.json";
    private static final String SAMPLE_GTW_CONFIG_PATH = "api/sampleGatewayConfig.bal";
    private static final String ALT_GTW_CONFIG_PATH = "api/alternativeGatewayConfig.bal";
    private static final String PATH_THUMBNAIL_IMG_1 = "api/thumbnail1.jpg";
    private static final String PATH_THUMBNAIL_IMG_2 = "api/thumbnail2.jpg";
    private static final String PATH_INLINE_DOC_1 = "document/inline1.txt";
    private static final String PATH_INLINE_DOC_2 = "document/inline2.txt";

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
        transport.add(HTTP);
        transport.add(HTTPS);

        List<String> tags = new ArrayList<>();
        tags.add(TAG_CLIMATE);

        List<String> policies = new ArrayList<>();
        policies.add(GOLD_TIER);
        policies.add(SILVER_TIER);
        policies.add(BRONZE_TIER);

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String permissionJson = "[{\"groupId\" : 1000, \"permission\" : "
                + "[\"READ\",\"UPDATE\"]},{\"groupId\" : 1001, \"permission\" : [\"READ\",\"UPDATE\"]}]";

        API.APIBuilder apiBuilder = new API.APIBuilder(ADMIN, "WeatherAPI", API_VERSION).
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
                apiPolicy(APIMgtConstants.DEFAULT_API_POLICY).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new ArrayList<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy(ADMIN).
                updatedBy(ADMIN).
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
        lifecycleState.setLcName(APIMgtConstants.API_LIFECYCLE);
        lifecycleState.setLifecycleId(lifecycleId);
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        return lifecycleState;
    }

    public static API getMockApiSummaryObject() {
        return new API.APIBuilder(ADMIN, "Sample", API_VERSION).build();
    }

    public static API.APIBuilder createAlternativeAPI() {
        List<String> transport = new ArrayList<>();
        transport.add(HTTP);

        List<String> tags = new ArrayList<>();
        tags.add(TAG_FOOD);
        tags.add(TAG_BEVERAGE);

        List<String> policies = new ArrayList<>();
        policies.add(SILVER_TIER);
        policies.add(BRONZE_TIER);

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner(NAME_BUSINESS_OWNER_1);
        businessInformation.setBusinessOwnerEmail(EMAIL_BUSINESS_OWNER_1);
        businessInformation.setTechnicalOwner(NAME_BUSINESS_OWNER_2);
        businessInformation.setBusinessOwnerEmail(EMAIL_BUSINESS_OWNER_2);

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(
                Arrays.asList(APIMgtConstants.FunctionsConstants.GET, APIMgtConstants.FunctionsConstants.POST,
                        APIMgtConstants.FunctionsConstants.DELETE));
        corsConfiguration.setAllowHeaders(Arrays.asList(ALLOWED_HEADER_AUTHORIZATION, ALLOWED_HEADER_CUSTOM));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder("Adam", "restaurantAPI", "0.9").
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Food & Beverage Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                endpoint(Collections.emptyMap()).
                wsdlUri(SAMPLE_API_WSDL).
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy(GOLD_TIER).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList(CUSTOMER_ROLE, MANAGER_ROLE, EMPLOYEE_ROLE)).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy(API_CREATOR).
                apiDefinition(apiDefinition).
                lastUpdatedTime(LocalDateTime.now());

        return apiBuilder;
    }

    public static API.APIBuilder createUniqueAPI() {
        List<String> transport = new ArrayList<>();
        transport.add(HTTP);

        List<String> tags = new ArrayList<>();
        tags.add(TAG_FOOD);
        tags.add(TAG_BEVERAGE);

        List<String> policies = new ArrayList<>();
        policies.add(SILVER_TIER);
        policies.add(BRONZE_TIER);

        BusinessInformation businessInformation = new BusinessInformation();
        businessInformation.setBusinessOwner(NAME_BUSINESS_OWNER_1);
        businessInformation.setBusinessOwnerEmail(EMAIL_BUSINESS_OWNER_1);
        businessInformation.setTechnicalOwner(NAME_BUSINESS_OWNER_2);
        businessInformation.setBusinessOwnerEmail(EMAIL_BUSINESS_OWNER_2);

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(
                Arrays.asList(APIMgtConstants.FunctionsConstants.GET, APIMgtConstants.FunctionsConstants.POST,
                        APIMgtConstants.FunctionsConstants.DELETE));
        corsConfiguration.setAllowHeaders(Arrays.asList(ALLOWED_HEADER_AUTHORIZATION, ALLOWED_HEADER_CUSTOM));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        API.APIBuilder apiBuilder = new API.APIBuilder(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                API_VERSION).
                id(UUID.randomUUID().toString()).
                context(UUID.randomUUID().toString()).
                description("Get Food & Beverage Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                endpoint(Collections.emptyMap()).
                wsdlUri(SAMPLE_API_WSDL).
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy(GOLD_TIER).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(Arrays.asList(CUSTOMER_ROLE, MANAGER_ROLE, EMPLOYEE_ROLE)).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy(API_CREATOR).
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
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(ALT_SWAGGER_PATH));
    }

    public static String createSampleGatewayConfig() {
        try {
            return IOUtils.toString(
                    Thread.currentThread().getContextClassLoader().getResourceAsStream(SAMPLE_GTW_CONFIG_PATH));
        } catch (IOException e) {
            log.error("Error while reading " + SAMPLE_GTW_CONFIG_PATH, e);
            return null;
        }
    }

    public static String createAlternativeGatewayConfig() throws IOException {
        return IOUtils
                .toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(ALT_GTW_CONFIG_PATH));
    }

    public static InputStream createDefaultThumbnailImage() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_THUMBNAIL_IMG_1);
    }

    public static InputStream createAlternativeThumbnailImage() {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_THUMBNAIL_IMG_2);
    }

    public static DocumentInfo createDefaultDocumentationInfo() {
        //created by admin
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.id(UUID.randomUUID().toString());
        builder.name(SAMPLE_DOC_NAME);
        builder.type(DocumentInfo.DocType.HOWTO);
        builder.summary("Summary of Calculator Documentation");
        builder.sourceType(DocumentInfo.SourceType.INLINE);
        builder.sourceURL(EMPTY_STRING);
        builder.otherType(EMPTY_STRING);
        builder.visibility(DocumentInfo.Visibility.API_LEVEL);
        builder.createdTime(LocalDateTime.now());
        builder.lastUpdatedTime(LocalDateTime.now());
        return builder.build();
    }

    public static DocumentInfo createAlternativeDocumentationInfo(String uuid) {
        //created by admin
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.id(uuid);
        builder.name(SAMPLE_DOC_NAME);
        builder.type(DocumentInfo.DocType.HOWTO);
        builder.summary("Summary of Calculator Documentation - alternative");
        builder.sourceType(DocumentInfo.SourceType.INLINE);
        builder.sourceURL(EMPTY_STRING);
        builder.otherType(EMPTY_STRING);
        builder.visibility(DocumentInfo.Visibility.API_LEVEL);
        builder.createdTime(LocalDateTime.now());
        builder.lastUpdatedTime(LocalDateTime.now());
        return builder.build();
    }

    public static String createDefaultInlineDocumentationContent() throws IOException {
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_INLINE_DOC_1));
    }

    public static String createAlternativeInlineDocumentationContent() throws IOException {
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_INLINE_DOC_2));
    }

    public static Application createDefaultApplication() {
        //created by admin
        Application application = new Application(TEST_APP_1, ADMIN);
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl(CALLBACK_URL_1);
        application.setDescription("This is a test application");
        application.setGroupId(GROUP_1);
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setTier(FIFTY_PER_MIN_TIER);
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser(ADMIN);
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createAlternativeApplication() {
        //created by admin and updated by admin2
        Application application = new Application(TEST_APP_2, ADMIN);
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl(CALLBACK_URL_2);
        application.setDescription("This is test application 2");
        application.setGroupId(GROUP_2);
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
        application.setTier(TWENTY_PER_MIN_TIER);
        application.setUpdatedUser("admin2");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createCustomApplication(String applicationName, String owner) {
        Application application = new Application(applicationName, owner);
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl(CALLBACK_URL_1);
        application.setDescription("This is a test application");
        application.setGroupId(GROUP_1);
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setTier(FIFTY_PER_MIN_TIER);
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser(ADMIN);
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createApplicationWithPermissions() {
        //created by admin
        HashMap permissionMap = new HashMap();
        permissionMap.put(APIMgtConstants.Permission.UPDATE, APIMgtConstants.Permission.UPDATE_PERMISSION);
        Application application = new Application(TEST_APP_1, ADMIN);
        application.setId(UUID.randomUUID().toString());
        application.setCallbackUrl(CALLBACK_URL_1);
        application.setDescription("This is a test application");
        application.setGroupId(GROUP_1);
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setTier(FIFTY_PER_MIN_TIER);
        application.setPermissionMap(permissionMap);
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser(ADMIN);
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static APIPolicy createDefaultAPIPolicy() {
        APIPolicy apiPolicy = new APIPolicy(SAMPLE_API_POLICY);
        apiPolicy.setDisplayName(SAMPLE_API_POLICY);
        apiPolicy.setDescription(SAMPLE_API_POLICY_DESCRIPTION);
        apiPolicy.setUserLevel(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit(TIME_UNIT_SECONDS);
        requestCountLimit.setRequestCount(10000);
        requestCountLimit.setUnitTime(1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        apiPolicy.setPipelines(createDefaultPipelines());
        return apiPolicy;
    }

    public static List<Pipeline> createDefaultPipelines() {
        List<Pipeline> pipelineList = new ArrayList<>();    //contains all the default pipelines
        List<Condition> conditionsList = new ArrayList<>(); //contains conditions for each pipeline
        //Pipeline 1
        QuotaPolicy quotaPolicy1 = new QuotaPolicy();
        quotaPolicy1.setType(PolicyConstants.BANDWIDTH_TYPE);
        BandwidthLimit bandwidthLimit = new BandwidthLimit();
        IPCondition ipCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        ipCondition.setStartingIP("20.23.1.3");
        ipCondition.setEndingIP("30.23.1.4");

        IPCondition ipConditionSpecific = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        ipCondition.setStartingIP("192.34.21.1");

        Pipeline pipeline1 = new Pipeline();
        conditionsList.add(ipCondition);
        conditionsList.add(ipConditionSpecific);
        pipeline1.setConditions(conditionsList);
        bandwidthLimit.setDataAmount(1000);
        bandwidthLimit.setDataUnit("MB");
        bandwidthLimit.setUnitTime(1);
        bandwidthLimit.setTimeUnit(TIME_UNIT_MONTH);

        quotaPolicy1.setLimit(bandwidthLimit);
        pipeline1.setQuotaPolicy(quotaPolicy1);
        pipelineList.add(pipeline1);

        //End of pipeline 1 -> Beginning of pipeline 2
        Pipeline pipeline2 = new Pipeline();
        QuotaPolicy quotaPolicy2 = new QuotaPolicy();
        quotaPolicy2.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        HeaderCondition headerCondition = new HeaderCondition();
        headerCondition.setHeader("Browser");
        headerCondition.setValue("Chrome");

        JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
        jwtClaimsCondition.setClaimUrl("/path/path");
        jwtClaimsCondition.setAttribute("attribute");

        QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
        queryParameterCondition.setParameter("Location");
        queryParameterCondition.setValue("Colombo");

        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setRequestCount(1000);
        requestCountLimit.setUnitTime(1);
        requestCountLimit.setTimeUnit(TIME_UNIT_SECONDS);
        quotaPolicy2.setLimit(requestCountLimit);

        conditionsList = new ArrayList<>();
        conditionsList.add(headerCondition);
        conditionsList.add(jwtClaimsCondition);
        conditionsList.add(queryParameterCondition);

        pipeline2.setConditions(conditionsList);
        pipeline2.setQuotaPolicy(quotaPolicy2);
        pipelineList.add(pipeline2);

        return pipelineList;
    }
    public static APIPolicy createDefaultAPIPolicyWithBandwidthLimit() {
        APIPolicy apiPolicy = new APIPolicy(SAMPLE_API_POLICY);
        apiPolicy.setDisplayName(SAMPLE_API_POLICY);
        apiPolicy.setDescription(SAMPLE_API_POLICY_DESCRIPTION);
        apiPolicy.setUserLevel(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);
        BandwidthLimit bandwidthLimit = new BandwidthLimit();
        bandwidthLimit.setDataAmount(1000);
        bandwidthLimit.setDataUnit(PolicyConstants.MB);
        bandwidthLimit.setUnitTime(1);
        bandwidthLimit.setTimeUnit(TIME_UNIT_MONTH);
        defaultQuotaPolicy.setLimit(bandwidthLimit);
        apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return apiPolicy;
    }

    public static ApplicationPolicy createDefaultApplicationPolicy() {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy(SAMPLE_APP_POLICY);
        applicationPolicy.setDisplayName(SAMPLE_APP_POLICY);
        applicationPolicy.setDescription(SAMPLE_APP_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit(TIME_UNIT_SECONDS);
        requestCountLimit.setRequestCount(10000);
        requestCountLimit.setUnitTime(1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return applicationPolicy;
    }

    public static SubscriptionPolicy createDefaultSubscriptionPolicy() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setDisplayName(SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setDescription(SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit(TIME_UNIT_SECONDS);
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

    public static List<API> createMockAPIList() {
        List<API> apiList = new ArrayList<>();
        API api1 = createDefaultAPI().build();
        API api2 = createAlternativeAPI().build();
        apiList.add(api1);
        apiList.add(api2);
        return apiList;
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
        endpointMap.put(PRODUCTION_ENDPOINT, endpointId);
        return endpointMap;
    }

    public static Map<String, UriTemplate> getMockUriTemplates() {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.endpoint(getMockEndpointMap());
        uriTemplateBuilder.templateId(TEMPLATE_ID);
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy(APIMgtConstants.DEFAULT_API_POLICY);
        uriTemplateBuilder.httpVerb(APIMgtConstants.FunctionsConstants.GET);
        uriTemplateMap.put(TEMPLATE_ID, uriTemplateBuilder.build());
        return uriTemplateMap;
    }

    public static Label.Builder createLabel(String name) {

        List<String> accessUrls = new ArrayList<>();
        accessUrls.add(ACCESS_URL + name);
        return new Label.Builder().
                id(UUID.randomUUID().toString()).
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
        
        Map<String, String> properties = new HashMap<>();
        properties.put("property1", "value1");
        properties.put("property2", "value2");
        workflow.setAttributes(properties);
        
        return workflow;
    }
    public static DocumentInfo createDefaultFileDocumentationInfo() {
        //created by admin
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.id(UUID.randomUUID().toString());
        builder.name(SAMPLE_DOC_NAME);
        builder.type(DocumentInfo.DocType.HOWTO);
        builder.summary("Summary of Calculator Documentation");
        builder.sourceType(DocumentInfo.SourceType.FILE);
        builder.sourceURL(EMPTY_STRING);
        builder.otherType(EMPTY_STRING);
        builder.visibility(DocumentInfo.Visibility.API_LEVEL);
        builder.createdTime(LocalDateTime.now());
        builder.lastUpdatedTime(LocalDateTime.now());
        return builder.build();
    }
}
