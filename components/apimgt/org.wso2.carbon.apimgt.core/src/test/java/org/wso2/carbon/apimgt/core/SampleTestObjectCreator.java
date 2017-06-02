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
import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
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
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public static String endpointId = UUID.randomUUID().toString();

    static {
        try {
            inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("swagger.json");
            apiDefinition = IOUtils.toString(inputStream);
        } catch (IOException e) {
            log.error(e.getMessage());
        }

    }

    public static API.APIBuilder createDefaultAPI() {
        Set<String> transport = new HashSet<>();
        transport.add(HTTP);
        transport.add(HTTPS);

        Set<String> tags = new HashSet<>();
        tags.add(TAG_CLIMATE);

        Set<String> policies = new HashSet<>();
        policies.add(GOLD_TIER);
        policies.add(SILVER_TIER);
        policies.add(BRONZE_TIER);

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
        //+ "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\",\"DELETE\"]}]";

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
                visibleRoles(new HashSet<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                apiType(ApiType.STANDARD).
                createdTime(LocalDateTime.now()).
                createdBy(ADMIN).
                updatedBy(ADMIN).
                lastUpdatedTime(LocalDateTime.now()).
                //permission(permissionJson).
                uriTemplates(getMockUriTemplates()).
                apiDefinition(apiDefinition);
//        HashMap map = new HashMap();
//        map.put("developer", 6);
//        map.put("admin", 7);
        apiBuilder.permissionMap(Collections.EMPTY_MAP);
        return apiBuilder;
    }

    public static API getSummaryFromAPI(API api) {
        return new API.APIBuilder(api.getProvider(), api.getName(), api.getVersion()).
                id(api.getId()).
                context(api.getContext()).
                description(api.getDescription()).
                apiType(ApiType.STANDARD).
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
                apiType(fromAPI.getApiType()).
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
        Set<String> transport = new HashSet<>();
        transport.add(HTTP);

        Set<String> tags = new HashSet<>();
        tags.add(TAG_FOOD);
        tags.add(TAG_BEVERAGE);

        Set<String> policies = new HashSet<>();
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

//        HashMap permissionMap = new HashMap();
//        permissionMap.put("developer", 6);
//        permissionMap.put("admin", 7);

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
                visibleRoles(new HashSet<>(Arrays.asList(CUSTOMER_ROLE, MANAGER_ROLE, EMPLOYEE_ROLE))).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                apiType(ApiType.STANDARD).
                permissionMap(Collections.EMPTY_MAP).
                createdTime(LocalDateTime.now()).
                createdBy(API_CREATOR).
                apiDefinition(apiDefinition).
                lastUpdatedTime(LocalDateTime.now());

        return apiBuilder;
    }

    public static API.APIBuilder createUniqueAPI() {
        Set<String> transport = new HashSet<>();
        transport.add(HTTP);

        Set<String> tags = new HashSet<>();
        tags.add(TAG_FOOD);
        tags.add(TAG_BEVERAGE);

        Set<String> policies = new HashSet<>();
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

//        HashMap permissionMap = new HashMap();
//        permissionMap.put("developer", 6);
//        permissionMap.put("admin", 7);

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
                visibleRoles(new HashSet<>(Arrays.asList(CUSTOMER_ROLE, MANAGER_ROLE, EMPLOYEE_ROLE))).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                apiType(ApiType.STANDARD).
                permissionMap(Collections.EMPTY_MAP).
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
                apiType(api.getApiType()).
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

    /**
     * create default api policy
     *
     * @return APIPolicy object is returned
     */
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

    /**
     * create default pipeline for api policy
     *
     * @return list of Pipeline objects is returned
     */
    public static List<Pipeline> createDefaultPipelines() {
        //Pipeline 1
        IPCondition ipCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        ipCondition.setStartingIP("192.168.12.3");
        ipCondition.setEndingIP("192.168.88.19");
        IPCondition ipConditionSpecific = new IPCondition(PolicyConstants.IP_SPECIFIC_TYPE);
        ipConditionSpecific.setSpecificIP("123.42.14.56");

        //adding above conditions to condition list of pipeline 1
        List<Condition> conditionsList = new ArrayList<>(); //contains conditions for each pipeline
        conditionsList.add(ipCondition);
        conditionsList.add(ipConditionSpecific);
        //set quota policy with bandwidth limit
        BandwidthLimit bandwidthLimit = new BandwidthLimit();
        bandwidthLimit.setDataAmount(1000);
        bandwidthLimit.setDataUnit(PolicyConstants.MB);
        bandwidthLimit.setUnitTime(1);
        bandwidthLimit.setTimeUnit(TIME_UNIT_MONTH);
        QuotaPolicy quotaPolicy1 = new QuotaPolicy();
        quotaPolicy1.setType(PolicyConstants.BANDWIDTH_TYPE);
        quotaPolicy1.setLimit(bandwidthLimit);

        Pipeline pipeline1 = new Pipeline();
        pipeline1.setConditions(conditionsList);
        pipeline1.setQuotaPolicy(quotaPolicy1);

        //End of pipeline 1 -> Beginning of pipeline 2
        HeaderCondition headerCondition = new HeaderCondition();
        headerCondition.setHeader("Browser");
        headerCondition.setValue("Chrome");
        JWTClaimsCondition jwtClaimsCondition = new JWTClaimsCondition();
        jwtClaimsCondition.setClaimUrl("/path/path2");
        jwtClaimsCondition.setAttribute("attributed");
        QueryParameterCondition queryParameterCondition = new QueryParameterCondition();
        queryParameterCondition.setParameter("Location");
        queryParameterCondition.setValue("Colombo");

        //adding conditions to condition list of pipeline2
        conditionsList = new ArrayList<>();
        conditionsList.add(headerCondition);
        conditionsList.add(jwtClaimsCondition);
        conditionsList.add(queryParameterCondition);
        //pipeline 2 with request count as quota policy
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setRequestCount(1000);
        requestCountLimit.setUnitTime(1);
        requestCountLimit.setTimeUnit(TIME_UNIT_SECONDS);
        QuotaPolicy quotaPolicy2 = new QuotaPolicy();
        quotaPolicy2.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        quotaPolicy2.setLimit(requestCountLimit);

        Pipeline pipeline2 = new Pipeline();
        pipeline2.setConditions(conditionsList);
        pipeline2.setQuotaPolicy(quotaPolicy2);
        //adding pipelines
        List<Pipeline> pipelineList = new ArrayList<>();    //contains all the default pipelines
        pipelineList.add(pipeline1);
        pipelineList.add(pipeline2);

        return pipelineList;
    }

    /**
     * Create default api policy with bandwidth limit as quota policy
     *
     * @return APIPolicy object with bandwidth limit as quota policy is returned
     */
    public static APIPolicy createDefaultAPIPolicyWithBandwidthLimit() {
        BandwidthLimit bandwidthLimit = new BandwidthLimit();
        bandwidthLimit.setDataAmount(1000);
        bandwidthLimit.setDataUnit(PolicyConstants.MB);
        bandwidthLimit.setUnitTime(1);
        bandwidthLimit.setTimeUnit(TIME_UNIT_MONTH);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);
        defaultQuotaPolicy.setLimit(bandwidthLimit);
        //set default API Policy
        APIPolicy apiPolicy = new APIPolicy(SAMPLE_API_POLICY);
        apiPolicy.setDisplayName(SAMPLE_API_POLICY);
        apiPolicy.setDescription(SAMPLE_API_POLICY_DESCRIPTION);
        apiPolicy.setUserLevel(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL);
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
                .maxTps(1000L).security("{'enabled':false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();
    }

    public static Endpoint createUpdatedEndpoint() {
        return new Endpoint.Builder().endpointConfig("{'type':'soap','url':'http://localhost:8280'}").id(endpointId)
                .maxTps(1000L).security("{'enabled':false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();
    }

    public static Endpoint createAlternativeEndpoint() {
        String uuid = UUID.randomUUID().toString();
        return new Endpoint.Builder().endpointConfig("{'type':'soap','url':'http://localhost:8280'}").id(uuid)
                .name("Endpoint2").maxTps(1000L).security("{'enabled':false}")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build();

    }

    public static Map<String, Endpoint> getMockEndpointMap() {
        Map<String, Endpoint> endpointMap = new HashedMap();
        endpointMap.put(PRODUCTION_ENDPOINT,
                new Endpoint.Builder().id(endpointId).applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).build());
        return endpointMap;
    }

    public static Map<String, UriTemplate> getMockUriTemplates() {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
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

    public static Workflow createWorkflow(String workflowReferenceID) throws APIMgtDAOException {
        Workflow workflow = new ApplicationCreationWorkflow(DAOFactory.getApplicationDAO(),
                DAOFactory.getWorkflowDAO());
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

    public static Comment createDefaultComment(String apiId) {
        Comment comment = new Comment();
        comment.setUuid(UUID.randomUUID().toString());
        comment.setApiId(apiId);
        comment.setCommentText("this is a sample comment");
        comment.setCommentedUser("admin");
        comment.setUpdatedUser("admin");
        comment.setCreatedTime(LocalDateTime.now());
        comment.setUpdatedTime(LocalDateTime.now());
        return comment;
    }

    public static Rating createDefaultRating(String apiId) {
        Rating rating = new Rating();
        rating.setUuid(UUID.randomUUID().toString());
        rating.setApiId(apiId);
        rating.setRating(4);
        rating.setUsername("john");
        rating.setLastUpdatedUser("john");
        rating.setCreatedTime(LocalDateTime.now());
        rating.setLastUpdatedTime(LocalDateTime.now());
        return rating;
    }

    public static API.APIBuilder createDefaultAPIWithApiLevelEndpoint() {
        Set<String> transport = new HashSet<>();
        transport.add(HTTP);
        transport.add(HTTPS);

        Set<String> tags = new HashSet<>();
        tags.add(TAG_CLIMATE);

        Set<String> policies = new HashSet<>();
        policies.add(GOLD_TIER);
        policies.add(SILVER_TIER);
        policies.add(BRONZE_TIER);

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
//            + "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\",\"DELETE\"]}]";
        Map<String, Endpoint> endpointMap = new HashMap<>();
        endpointMap.put(APIMgtConstants.PRODUCTION_ENDPOINT,
                new Endpoint.Builder().id(endpointId).name("api1-production--endpint")
                        .applicableLevel(APIMgtConstants.API_SPECIFIC_ENDPOINT).build());
        API.APIBuilder apiBuilder = new API.APIBuilder(ADMIN, "WeatherAPI", API_VERSION).
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Weather Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                lifecycleInstanceId(UUID.randomUUID().toString()).
                endpoint(endpointMap).
                wsdlUri("http://localhost:9443/echo?wsdl").
                isResponseCachingEnabled(false).
                cacheTimeout(60).
                isDefaultVersion(false).
                apiPolicy(APIMgtConstants.DEFAULT_API_POLICY).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new HashSet<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                apiType(ApiType.STANDARD).
                createdTime(LocalDateTime.now()).
                createdBy(ADMIN).
                updatedBy(ADMIN).
                lastUpdatedTime(LocalDateTime.now()).
                //permission(permissionJson).
                uriTemplates(getMockUriTemplates()).
                apiDefinition(apiDefinition);
//        HashMap map = new HashMap();
//        map.put("developer", 6);
//        map.put("admin", 7);
        apiBuilder.permissionMap(Collections.EMPTY_MAP);
        return apiBuilder;
    }
}
