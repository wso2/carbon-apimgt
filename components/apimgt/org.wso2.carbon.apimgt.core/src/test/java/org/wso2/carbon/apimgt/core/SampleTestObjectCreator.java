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
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Function;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Condition;
import org.wso2.carbon.apimgt.core.models.policy.CustomPolicy;
import org.wso2.carbon.apimgt.core.models.policy.HeaderCondition;
import org.wso2.carbon.apimgt.core.models.policy.IPCondition;
import org.wso2.carbon.apimgt.core.models.policy.JWTClaimsCondition;
import org.wso2.carbon.apimgt.core.models.policy.Pipeline;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QueryParameterCondition;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIFileUtils;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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

import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.SECONDS_TIMUNIT;
import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.UNLIMITED_TIER;
import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.REQUEST_COUNT_TYPE;
import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.SANDBOX_ENDPOINT;

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
    private static final String UPDATED_SAMPLE_API_POLICY = "UpdatedSampleAPIPolicy";
    private static final String UPDATED_SAMPLE_API_POLICY_DESCRIPTION = "Updated NewSampleAPIPolicy Description";
    private static final String UPDATED_SAMPLE_APP_POLICY = "UpdatedSampleAppPolicy";
    private static final String UPDATED_SAMPLE_APP_POLICY_DESCRIPTION = "Updated SampleAppPolicy Description";
    private static final String UPDATED_SAMPLE_SUBSCRIPTION_POLICY = "Updated SampleSubscriptionPolicy";
    private static final String UPDATED_SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION = "Updated SampleSubscriptionPolicy "
            + "Description";
    private static final String SAMPLE_CUSTOM_ATTRIBUTE = "CUSTOM ATTRIBUTE SAMPLE";
    private static final String PRODUCTION_ENDPOINT = "production";
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
    private static final String SAMPLE_DOC_NAME = "CalculatorDoc";
    private static final String TEST_APP_1 = "TestApp";
    private static final String TEST_APP_2 = "TestApp2";
    private static final String TEMPLATE_ID = "getApisApiIdGet";
    private static final String ALT_SWAGGER_PATH = "api/alternativeSwagger.json";
    private static final String SAMPLE_GTW_CONFIG_PATH = "api/sampleGatewayConfig.bal";
    private static final String ALT_GTW_CONFIG_PATH = "api/alternativeGatewayConfig.bal";
    private static final String PATH_THUMBNAIL_IMG_1 = "api/thumbnail1.jpg";
    private static final String PATH_THUMBNAIL_IMG_2 = "api/thumbnail2.jpg";
    private static final String PATH_INLINE_DOC_1 = "document/inline1.txt";
    private static final String PATH_INLINE_DOC_2 = "document/inline2.txt";
    private static final String PATH_FILE_DOC_1 = "document/pdf-sample.pdf";
    private static final String PATH_WSDL11_File_1 = "wsdl/WeatherForecast.wsdl";
    private static final String PATH_WSDL11_File_2 = "wsdl/stockQuote.wsdl";
    private static final String PATH_WSDL20_File_1 = "wsdl/myServiceWsdl2.wsdl";
    private static final String PATH_WSDL11_ZIP_1 = "wsdl/WSDL11Files_1.zip";
    private static final String PATH_WSDL11_ZIP_2 = "wsdl/WSDL11Files_2.zip";
    private static final String PATH_WSDL20_ZIP_1 = "wsdl/WSDL20Files.zip";
    private static final String SAMPLE_IP_1 = "12.32.45.3";
    private static final String SAMPLE_IP_2 = "24.34.1.45";
    private static final String SAMPLE_CUSTOM_RULE = "Sample Custom Rule";
    public static final String ADMIN_ROLE_ID = "cfbde56e-4352-498e-b6dc-85a6f1f8b058";
    public static final String DEVELOPER_ROLE_ID = "cfdce56e-8434-498e-b6dc-85a6f2d8f035";

    public static final String ACCESS_URL = "https://test.";
    public static final String ORIGINAL_ENDPOINT_WEATHER = "http://www.webservicex.net/WeatherForecast.asmx";
    public static final String ORIGINAL_ENDPOINT_STOCK_QUOTE = "http://www.webservicex.net/stockquote.asmx";

    public static  APIPolicy unlimitedApiPolicy = new APIPolicy(UUID.randomUUID().toString(), UNLIMITED_TIER);
    public static  APIPolicy goldApiPolicy = new APIPolicy(UUID.randomUUID().toString(), GOLD_TIER);
    public static  APIPolicy silverApiPolicy = new APIPolicy(UUID.randomUUID().toString(), SILVER_TIER);
    public static  APIPolicy bronzeApiPolicy = new APIPolicy(UUID.randomUUID().toString(), BRONZE_TIER);
    public static  SubscriptionPolicy unlimitedSubscriptionPolicy =
            new SubscriptionPolicy(UUID.randomUUID().toString(), UNLIMITED_TIER);
    public static  SubscriptionPolicy goldSubscriptionPolicy =
            new SubscriptionPolicy(UUID.randomUUID().toString(), GOLD_TIER);
    public static  SubscriptionPolicy silverSubscriptionPolicy =
            new SubscriptionPolicy(UUID.randomUUID().toString(), SILVER_TIER);
    public static  SubscriptionPolicy bronzeSubscriptionPolicy =
            new SubscriptionPolicy(UUID.randomUUID().toString(), BRONZE_TIER);
    public static  ApplicationPolicy fiftyPerMinApplicationPolicy =
            new ApplicationPolicy(UUID.randomUUID().toString(), FIFTY_PER_MIN_TIER);
    public static  ApplicationPolicy twentyPerMinApplicationPolicy =
            new ApplicationPolicy(UUID.randomUUID().toString(), TWENTY_PER_MIN_TIER);
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

        Set<Policy> policies = new HashSet<>();
        policies.add(goldSubscriptionPolicy);
        policies.add(silverSubscriptionPolicy);
        policies.add(bronzeSubscriptionPolicy);

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
                + "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\"," +
                "\"DELETE\", \"MANAGE_SUBSCRIPTION\"]}]";

        API.APIBuilder apiBuilder = new API.APIBuilder(ADMIN, "WeatherAPI", API_VERSION).
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Weather Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                lifecycleInstanceId(UUID.randomUUID().toString()).
                endpoint(Collections.emptyMap()).
                isResponseCachingEnabled(false).
                cacheTimeout(60).
                isDefaultVersion(false).
                apiPolicy(unlimitedApiPolicy).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new HashSet<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy(ADMIN).
                updatedBy(ADMIN).
                lastUpdatedTime(LocalDateTime.now()).
                apiPermission(permissionJson).
                uriTemplates(getMockUriTemplates()).
                apiDefinition(apiDefinition);
        Map map = new HashMap();
        map.put(DEVELOPER_ROLE_ID, 6);
        map.put(ADMIN_ROLE_ID, 15);
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
        Set<String> transport = new HashSet<>();
        transport.add(HTTP);

        Set<String> tags = new HashSet<>();
        tags.add(TAG_FOOD);
        tags.add(TAG_BEVERAGE);

        Set<Policy> policies = new HashSet<>();
        policies.add(silverSubscriptionPolicy);
        policies.add(bronzeSubscriptionPolicy);

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

        String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
                + "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\"," +
                "\"DELETE\", \"MANAGE_SUBSCRIPTION\"]}]";

        Map permissionMap = new HashMap();
        permissionMap.put(DEVELOPER_ROLE_ID, 6);
        permissionMap.put(ADMIN_ROLE_ID, 15);

        API.APIBuilder apiBuilder = new API.APIBuilder(ADMIN, "restaurantAPI", "0.9").
                id(UUID.randomUUID().toString()).
                context("weather").
                description("Get Food & Beverage Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                endpoint(Collections.emptyMap()).
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy(goldApiPolicy).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(new HashSet<>(Arrays.asList(CUSTOMER_ROLE, MANAGER_ROLE, EMPLOYEE_ROLE))).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                apiPermission(permissionJson).
                permissionMap(permissionMap).
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

        Set<Policy> policies = new HashSet<>();
        policies.add(silverSubscriptionPolicy);
        policies.add(bronzeSubscriptionPolicy);

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

        String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
                + "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\"," +
                "\"DELETE\", \"MANAGE_SUBSCRIPTION\"]}]";

        Map permissionMap = new HashMap();
        permissionMap.put(DEVELOPER_ROLE_ID, 6);
        permissionMap.put(ADMIN_ROLE_ID, 15);

        API.APIBuilder apiBuilder = new API.APIBuilder(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
                API_VERSION).
                id(UUID.randomUUID().toString()).
                context(UUID.randomUUID().toString()).
                description("Get Food & Beverage Info").
                lifeCycleStatus(APIStatus.CREATED.getStatus()).
                endpoint(Collections.emptyMap()).
                isResponseCachingEnabled(true).
                cacheTimeout(120).
                isDefaultVersion(true).
                apiPolicy(goldApiPolicy).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.RESTRICTED).
                visibleRoles(new HashSet<>(Arrays.asList(CUSTOMER_ROLE, MANAGER_ROLE, EMPLOYEE_ROLE))).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                apiPermission(permissionJson).
                permissionMap(permissionMap).
                createdTime(LocalDateTime.now()).
                createdBy(API_CREATOR).
                uriTemplates(Collections.emptyMap()).
                apiDefinition(apiDefinition).
                lastUpdatedTime(LocalDateTime.now());

        return apiBuilder;
    }

    public static CompositeAPI.Builder createUniqueCompositeAPI() {
        Set<String> transport = new HashSet<>();
        transport.add(HTTP);

        HashMap permissionMap = new HashMap();
        permissionMap.put(DEVELOPER_ROLE_ID, 6);
        permissionMap.put(ADMIN_ROLE_ID, 15);
        permissionMap.put(ADMIN_ROLE_ID, 7);
        Application app = createDefaultApplication();
        //generate random name for each time when generating unique composite API
        app.setName(UUID.randomUUID().toString());
        try {
            DAOFactory.getApplicationDAO().addApplication(app);
        } catch (APIMgtDAOException e) {
            log.error("Error adding application", e);
        }

        CompositeAPI.Builder apiBuilder = new CompositeAPI.Builder().
                id(UUID.randomUUID().toString()).
                name(UUID.randomUUID().toString()).
                provider(UUID.randomUUID().toString()).
                version(API_VERSION).
                context(UUID.randomUUID().toString()).
                description("Get Food & Beverage Info").
                transport(transport).
                permissionMap(permissionMap).
                applicationId(app.getId()).
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

    /**
     * Creates a file type documentation info sample
     *
     * @return a file type documentation info sample
     */
    public static DocumentInfo createFileDocumentationInfo() {
        //created by admin
        DocumentInfo.Builder builder = new DocumentInfo.Builder();
        builder.id(UUID.randomUUID().toString());
        builder.name(SAMPLE_DOC_NAME);
        builder.type(DocumentInfo.DocType.HOWTO);
        builder.summary("Summary of PDF Type Documentation");
        builder.sourceType(DocumentInfo.SourceType.FILE);
        builder.sourceURL(EMPTY_STRING);
        builder.otherType(EMPTY_STRING);
        builder.visibility(DocumentInfo.Visibility.API_LEVEL);
        builder.createdTime(LocalDateTime.now());
        builder.lastUpdatedTime(LocalDateTime.now());
        return builder.build();
    }

    /**
     * Retrieves a sample file inline content string
     *
     * @return file inline content string
     * @throws IOException If unable to read doc file resource
     */
    public static String createDefaultInlineDocumentationContent() throws IOException {
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_INLINE_DOC_1));
    }

    /**
     * Retrieves a sample file inline content string
     *
     * @return file inline content string
     * @throws IOException If unable to read doc file resource
     */
    public static String createAlternativeInlineDocumentationContent() throws IOException {
        return IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_INLINE_DOC_2));
    }

    /**
     * Retrieves file content byte array
     *
     * @return file content byte array
     * @throws IOException If unable to read doc file resource
     */
    public static byte[] createDefaultFileDocumentationContent() throws IOException {
        return IOUtils.toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_FILE_DOC_1));
    }

    public static Application createDefaultApplication() {
        //created by admin
        Application application = new Application(TEST_APP_1, ADMIN);
        application.setId(UUID.randomUUID().toString());
        application.setDescription("This is a test application");
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setPolicy(fiftyPerMinApplicationPolicy);
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser(ADMIN);
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createAlternativeApplication() {
        //created by admin and updated by admin2
        Application application = new Application(TEST_APP_2, ADMIN);
        application.setId(UUID.randomUUID().toString());
        application.setDescription("This is test application 2");
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
        application.setPolicy(twentyPerMinApplicationPolicy);
        application.setUpdatedUser("admin2");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    public static Application createCustomApplication(String applicationName, String owner) {
        Application application = new Application(applicationName, owner);
        application.setId(UUID.randomUUID().toString());
        application.setDescription("This is a test application");
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setPolicy(fiftyPerMinApplicationPolicy);
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
        application.setDescription("This is a test application");
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setPolicy(fiftyPerMinApplicationPolicy);
        application.setPermissionMap(permissionMap);
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser(ADMIN);
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    /**
     * Creates a sample function
     *
     * @return a sample function
     * @throws URISyntaxException if error occurred while initializing the URI
     */
    public static Function createDefaultFunction() throws URISyntaxException {
        return new Function("sampleFunction1", new URI("http://localhost/test1"));
    }

    /**
     * Creates an alternative function
     *
     * @return an alternative function
     * @throws URISyntaxException if error occurred while initializing the URI
     */
    public static Function createAlternativeFunction() throws URISyntaxException {
        return new Function("alternativeFunction1", new URI("http://localhost/test-alternative1"));
    }

    /**
     * Creates an alternative function
     *
     * @return an alternative function
     * @throws URISyntaxException if error occurred while initializing the URI
     */
    public static Function createAlternativeFunction2() throws URISyntaxException {
        return new Function("alternativeFunction2", new URI("http://localhost/test-alternative2"));
    }

    /**
     * Returns default WSDL 1.1 file content stream
     *
     * @return default WSDL 1.1 file content stream
     */
    public static InputStream createDefaultWSDL11ContentInputStream() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_WSDL11_File_1);
    }

    /**
     * Returns default WSDL 1.1 file
     *
     * @return default WSDL 1.1 file
     */
    public static byte[] createDefaultWSDL11Content() throws IOException {
        return IOUtils.toByteArray(createDefaultWSDL11ContentInputStream());
    }

    /**
     * Returns alternative WSDL 1.1 file content stream
     *
     * @return alternative WSDL 1.1 file content stream
     */
    public static InputStream createAlternativeWSDL11ContentInputStream() throws IOException {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_WSDL11_File_2);
    }

    /**
     * Returns default WSDL 1.1 file
     *
     * @return default WSDL 1.1 file
     */
    public static byte[] createAlternativeWSDL11Content() throws IOException {
        return IOUtils
                .toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_WSDL11_File_2));
    }

    /**
     * Returns default WSDL 2.0 file
     *
     * @return default WSDL 2.0 file
     */
    public static byte[] createDefaultWSDL20Content() throws IOException {
        return IOUtils
                .toByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(PATH_WSDL20_File_1));
    }

    /**
     * Returns default WSDL 1.0 archive's input stream
     *
     * @return default WSDL 1.0 archive's input stream
     */
    public static InputStream createDefaultWSDL11ArchiveInputStream() throws IOException, APIMgtDAOException {
        return Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(PATH_WSDL11_ZIP_1);
    }

    /**
     * Returns alternative WSDL 1.0 archive's input stream
     *
     * @return alternative WSDL 1.0 archive's input stream
     */
    public static InputStream createAlternativeWSDL11ArchiveInputStream() throws IOException, APIMgtDAOException {
        return Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(PATH_WSDL11_ZIP_2);
    }

    /**
     * Returns default WSDL 1.0 archive extracted path
     *
     * @return default WSDL 1.0 archive extracted path
     */
    public static String createDefaultWSDL11Archive() throws IOException, APIMgtDAOException {
        InputStream zipInputStream = createDefaultWSDL11ArchiveInputStream();
        final String tempFolderPath =
                SystemUtils.getJavaIoTmpDir() + File.separator + UUID.randomUUID().toString();
        String archivePath = tempFolderPath + File.separator + "wsdl11.zip";
        return APIFileUtils.extractUploadedArchive(zipInputStream, "extracted", archivePath, tempFolderPath);
    }

    /**
     * Returns default WSDL 2.0 archive extracted path
     *
     * @return default WSDL 2.0 archive extracted path
     */
    public static String createDefaultWSDL20Archive() throws APIMgtDAOException {
        InputStream zipInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(PATH_WSDL20_ZIP_1);
        final String tempFolderPath =
                SystemUtils.getJavaIoTmpDir() + File.separator + UUID.randomUUID().toString();
        String archivePath = tempFolderPath + File.separator + "wsdl20.zip";
        return APIFileUtils.extractUploadedArchive(zipInputStream, "extracted", archivePath, tempFolderPath);
    }

    /**
     * create default api policy
     *
     * @return APIPolicy object is returned
     */
    public static APIPolicy createDefaultAPIPolicy() {
        APIPolicy apiPolicy = new APIPolicy(SAMPLE_API_POLICY);
        apiPolicy.setUuid(UUID.randomUUID().toString());
        apiPolicy.setDisplayName(SAMPLE_API_POLICY);
        apiPolicy.setDescription(SAMPLE_API_POLICY_DESCRIPTION);
        apiPolicy.setUserLevel(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 1000, 10000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        apiPolicy.setPipelines(createDefaultPipelines());
        return apiPolicy;
    }

    /**
     * Updated the given API policy
     *
     * @param apiPolicy {@link APIPolicy} instance to be updated
     * @return updated {@link APIPolicy} instance
     */
    public static APIPolicy updateAPIPolicy(APIPolicy apiPolicy) {
        apiPolicy.setDisplayName(UPDATED_SAMPLE_API_POLICY);
        apiPolicy.setDescription(UPDATED_SAMPLE_API_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_LIMIT_TYPE);
        BandwidthLimit bandwidthLimit = new BandwidthLimit(TIME_UNIT_SECONDS, 1, 1000, "KB");
        defaultQuotaPolicy.setLimit(bandwidthLimit);
        apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        apiPolicy.setPipelines(createDefaultPipelines());
        apiPolicy.getPipelines().add(createNewIPRangePipeline());
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
        BandwidthLimit bandwidthLimit = new BandwidthLimit(TIME_UNIT_MONTH, 1, 1000, PolicyConstants.MB);
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
        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 1, 1000);
        QuotaPolicy quotaPolicy2 = new QuotaPolicy();
        quotaPolicy2.setType(REQUEST_COUNT_TYPE);
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
     * Creates a new {@link Pipeline} instance
     *
     * @return created Pipeline instance
     */
    public static Pipeline createNewIPRangePipeline() {
        IPCondition ipCondition = new IPCondition(PolicyConstants.IP_RANGE_TYPE);
        ipCondition.setStartingIP("10.100.0.105");
        ipCondition.setEndingIP("10.100.0.115");
        Pipeline pipeline = new Pipeline();

        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 1, 1000);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(REQUEST_COUNT_TYPE);
        quotaPolicy.setLimit(requestCountLimit);

        pipeline.setQuotaPolicy(quotaPolicy);
        pipeline.setConditions(Arrays.asList(ipCondition));
        return pipeline;
    }

    /**
     * Create default api policy with bandwidth limit as quota policy
     *
     * @return APIPolicy object with bandwidth limit as quota policy is returned
     */
    public static APIPolicy createDefaultAPIPolicyWithBandwidthLimit() {
        BandwidthLimit bandwidthLimit = new BandwidthLimit(TIME_UNIT_MONTH, 1, 1000, PolicyConstants.MB);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);
        defaultQuotaPolicy.setLimit(bandwidthLimit);
        //set default API Policy
        APIPolicy apiPolicy = new APIPolicy(SAMPLE_API_POLICY);
        apiPolicy.setUuid(UUID.randomUUID().toString());
        apiPolicy.setDisplayName(SAMPLE_API_POLICY);
        apiPolicy.setDescription(SAMPLE_API_POLICY_DESCRIPTION);
        apiPolicy.setUserLevel(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL);
        apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return apiPolicy;
    }

    public static ApplicationPolicy createDefaultApplicationPolicy() {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy(SAMPLE_APP_POLICY);
        applicationPolicy.setUuid(UUID.randomUUID().toString());
        applicationPolicy.setDisplayName(SAMPLE_APP_POLICY);
        applicationPolicy.setDescription(SAMPLE_APP_POLICY_DESCRIPTION);
        applicationPolicy.setCustomAttributes(SAMPLE_CUSTOM_ATTRIBUTE.getBytes());
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 10000, 1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return applicationPolicy;
    }


    public static ApplicationPolicy updateApplicationPolicy(ApplicationPolicy applicationPolicy) {
        applicationPolicy.setDisplayName(UPDATED_SAMPLE_APP_POLICY);
        applicationPolicy.setDescription(UPDATED_SAMPLE_APP_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_LIMIT_TYPE);
        BandwidthLimit bandwidthLimit = new BandwidthLimit(TIME_UNIT_SECONDS, 10, 1000, "KB");
        defaultQuotaPolicy.setLimit(bandwidthLimit);
        applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return applicationPolicy;
    }

    public static SubscriptionPolicy createDefaultSubscriptionPolicy() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setUuid(UUID.randomUUID().toString());
        subscriptionPolicy.setDisplayName(SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setDescription(SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION);
        subscriptionPolicy.setCustomAttributes(SAMPLE_CUSTOM_ATTRIBUTE.getBytes());
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 10000, 1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return subscriptionPolicy;
    }

    public static SubscriptionPolicy updateSubscriptionPolicy(SubscriptionPolicy subscriptionPolicy) {
        subscriptionPolicy.setDisplayName(UPDATED_SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setDescription(UPDATED_SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_LIMIT_TYPE);
        BandwidthLimit bandwidthLimit = new BandwidthLimit(TIME_UNIT_SECONDS, 1, 1000, "KB");
        defaultQuotaPolicy.setLimit(bandwidthLimit);
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
                new Endpoint.Builder().id(endpointId).applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).name
                        ("production").build());
        endpointMap.put(SANDBOX_ENDPOINT,
                new Endpoint.Builder().id(UUID.randomUUID().toString()).name("sandbox").applicableLevel(APIMgtConstants
                        .API_SPECIFIC_ENDPOINT).build());
        return endpointMap;
    }

    public static Map<String, UriTemplate> getMockUriTemplates() {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.templateId(TEMPLATE_ID);
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy(unlimitedApiPolicy);
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
                DAOFactory.getWorkflowDAO(), null);
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

    public static Comment createAlternativeComment(String apiId) {
        Comment comment = new Comment();
        comment.setUuid(UUID.randomUUID().toString());
        comment.setApiId(apiId);
        comment.setCommentText("this is a sample comment - alternative");
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

        Set<Policy> policies = new HashSet<>();
        policies.add(new SubscriptionPolicy(GOLD_TIER));
        policies.add(new SubscriptionPolicy(SILVER_TIER));
        policies.add(new SubscriptionPolicy(BRONZE_TIER));

        BusinessInformation businessInformation = new BusinessInformation();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
                + "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\"," +
                "\"DELETE\", \"MANAGE_SUBSCRIPTION\"]}]";

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
                apiPolicy(APIUtils.getDefaultAPIPolicy()).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(new HashSet<>()).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy(ADMIN).
                updatedBy(ADMIN).
                lastUpdatedTime(LocalDateTime.now()).
                apiPermission(permissionJson).
                uriTemplates(getMockUriTemplates()).
                apiDefinition(apiDefinition);
        Map map = new HashMap();
        map.put(DEVELOPER_ROLE_ID, 6);
        map.put(ADMIN_ROLE_ID, 15);
        apiBuilder.permissionMap(map);
        return apiBuilder;
    }

    public static BlockConditions createDefaultBlockCondition(String conditionType) {
        BlockConditions blockConditions = new BlockConditions();
        blockConditions.setConditionType(conditionType);
        blockConditions.setEnabled(true);
        if (conditionType.equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP)) {
            blockConditions.setConditionValue(SAMPLE_IP_1);
        } else if (conditionType.equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE)) {
            blockConditions.setStartingIP(SAMPLE_IP_1);
            blockConditions.setEndingIP(SAMPLE_IP_2);
        } else if (conditionType.equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_API)) {
            try {
                API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
                API api = apiBuilder.build();
                DAOFactory.getApiDAO().addAPI(api);
                blockConditions.setConditionValue(api.getContext());
            } catch (APIMgtDAOException e) {
                log.error("Error while adding default api in default block condition", e);
            }
        } else if (conditionType.equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_APPLICATION)) {
            try {
                Application app = createDefaultApplication();
                DAOFactory.getApplicationDAO().addApplication(app);
                blockConditions.setConditionValue(app.getId() + ":" + app.getName());
            } catch (APIMgtDAOException e) {
                log.error("Error while adding default app in default block condition", e);
            }
        } else if (conditionType.equals(APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_USER)) {
            blockConditions.setConditionValue(ADMIN);
        }
        return blockConditions;
    }

    public static CustomPolicy createDefaultCustomPolicy() {
        CustomPolicy customPolicy = new CustomPolicy(SAMPLE_CUSTOM_RULE);
        customPolicy.setKeyTemplate("$userId");
        String siddhiQuery = "FROM RequestStream SELECT userId, ( userId == 'admin@carbon.super' ) AS isEligible , "
                + "str:concat('admin@carbon.super','') as throttleKey INSERT INTO EligibilityStream;"
                + "FROM EligibilityStream[isEligible==true]#throttler:timeBatch(1 min) SELECT throttleKey, "
                + "(count(userId) >= 5 as isThrottled, expiryTimeStamp group by throttleKey INSERT ALL EVENTS into "
                + "ResultStream;";

        customPolicy.setSiddhiQuery(siddhiQuery);
        customPolicy.setDescription("Sample custom policy");
        return customPolicy;
    }

    public static void createDefaultPolicy(PolicyDAO policyDAO) throws APIMgtDAOException {
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(REQUEST_COUNT_TYPE);
        quotaPolicy.setLimit(new RequestCountLimit(SECONDS_TIMUNIT, 60, 1));
        unlimitedApiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addApiPolicy(unlimitedApiPolicy);
        goldApiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addApiPolicy(goldApiPolicy);
        silverApiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addApiPolicy(silverApiPolicy);
        bronzeApiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addApiPolicy(bronzeApiPolicy);
        unlimitedSubscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addSubscriptionPolicy(unlimitedSubscriptionPolicy);
        goldSubscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addSubscriptionPolicy(goldSubscriptionPolicy);
        silverSubscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addSubscriptionPolicy(silverSubscriptionPolicy);
        bronzeSubscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addSubscriptionPolicy(bronzeSubscriptionPolicy);
        fiftyPerMinApplicationPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addApplicationPolicy(fiftyPerMinApplicationPolicy);
        twentyPerMinApplicationPolicy.setDefaultQuotaPolicy(quotaPolicy);
        policyDAO.addApplicationPolicy(twentyPerMinApplicationPolicy);
    }
    public static String createDefaultSiddhiAppforAppPolicy() {
        ApplicationPolicy policy = createDefaultApplicationPolicy();
        RequestCountLimit limit = (RequestCountLimit) createDefaultApplicationPolicy().getDefaultQuotaPolicy()
                .getLimit();
        String siddhiApp =
                "@App:name('application_" + policy.getPolicyName() + "')\n" + "@App:description('ExecutionPlan for app_"
                        + policy.getPolicyName() + "')\n" +

                        "@source(type='inMemory', topic='apim', @map(type='passThrough'))\n"
                        + "define stream RequestStream (messageID string, appKey string, appTier string, "
                        + "subscriptionKey string,"
                        + " apiKey string, apiTier string, subscriptionTier string, resourceKey string,"
                        + " resourceTier string,"
                        + " userId string,  apiContext string, apiVersion string, appTenant string, apiTenant string,"
                        + " appId " + "string, apiName string, propertiesMap string);\n" +

                        "@sink(type='jms', @map(type='text'),\n"
                        + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory',"
                        + " provider.url='tcp://localhost:61616', destination='TEST.FOO', connection.factory."
                        + "type='topic',\n" + "connection.factory.jndi.name='TopicConnectionFactory')\n"
                        + "define stream GlobalThrottleStream (throttleKey string, isThrottled bool"
                        + ", expiryTimeStamp long);\n" +

                        "FROM RequestStream\n" + "SELECT messageID, (appTier == '" + policy.getPolicyName()
                        + "') AS isEligible, appKey AS throttleKey, " + "propertiesMap\n"
                        + "INSERT INTO EligibilityStream;\n" +

                        "FROM EligibilityStream[isEligible==true]#throttler:timeBatch(" + policy.getDefaultQuotaPolicy()
                        .getLimit().getUnitTime() + " " + policy.getDefaultQuotaPolicy().getLimit().getTimeUnit()
                        + ", 0)\n" + "select throttleKey, (count(messageID) >= " + limit.getRequestCount() + ")"
                        + " as isThrottled, expiryTimeStamp group by throttleKey\n"
                        + "INSERT ALL EVENTS into ResultStream;\n" +

                        "from ResultStream#throttler:emitOnStateChange(throttleKey, isThrottled)\n" + "select *\n"
                        + "insert into GlobalThrottleStream;\n";
        return siddhiApp;
    }

    public static String createDefaultSiddhiAppforSubscriptionPolicy() {
        SubscriptionPolicy policy = createDefaultSubscriptionPolicy();
        RequestCountLimit limit = (RequestCountLimit) policy.getDefaultQuotaPolicy()
                .getLimit();
        String siddhiApp = "@App:name('subscription_" + policy.getPolicyName() + "')\n"
                + "\n@App:description('ExecutionPlan for subscription_" + policy.getPolicyName() + "')\n" +

                "\n@source(type='inMemory', topic='apim', @map(type='passThrough'))\n"
                + "define stream RequestStream (messageID string, appKey string, appTier string,"
                + " subscriptionKey string,"
                + " apiKey string, apiTier string, subscriptionTier string, resourceKey string, resourceTier string,"
                + " userId string,  apiContext string, apiVersion string, appTenant string, apiTenant string, "
                + "appId string, apiName string, propertiesMap string);\n" +

                "\n@sink(type='jms', @map(type='text'),\n"
                + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory',"
                + " provider.url='tcp://localhost:61616', destination='TEST.FOO', connection.factory."
                + "type='topic',\n" + "connection.factory.jndi.name='TopicConnectionFactory')\n"
                + "define stream GlobalThrottleStream (throttleKey string, isThrottled bool"
                + ", expiryTimeStamp long);\n" +

                "\nFROM RequestStream\n" + "SELECT messageID, (subscriptionTier == '" + policy.getPolicyName()
                + "')" + " AS isEligible, subscriptionKey AS throttleKey, propertiesMap\n"
                + "INSERT INTO EligibilityStream;\n" + "\nFROM EligibilityStream[isEligible==true]#throttler:timeBatch("
                + policy.getDefaultQuotaPolicy().getLimit().getUnitTime() + " " + policy.getDefaultQuotaPolicy()
                .getLimit().getTimeUnit() + ", 0)\n" + "select throttleKey, (count(messageID) >= " + limit
                .getRequestCount() + ")" + " as isThrottled, expiryTimeStamp group by throttleKey\n"
                + "INSERT ALL EVENTS into ResultStream;\n" +

                "\nfrom ResultStream#throttler:emitOnStateChange(throttleKey, isThrottled)" + " select * "
                + "insert into GlobalThrottleStream;";
        return siddhiApp;
    }

    public static String createDefaultCustomPolicySiddhiApp() {
        CustomPolicy policy = createDefaultCustomPolicy();
        String siddhiApp =
                "@App:name('custom_" + policy.getPolicyName() + "')" + "\n@App:description('ExecutionPlan for custom_"
                        + policy.getPolicyName() + "')\n" +

                        "\n@source(type='inMemory', topic='apim', @map(type='passThrough'))\n"
                        + "define stream RequestStream (messageID string, appKey string, appTier string, "
                        + "subscriptionKey string, apiKey string, apiTier string, subscriptionTier string,"
                        + " resourceKey string, resourceTier string, userId string,  apiContext string, "
                        + "apiVersion string, appTenant string, apiTenant string, appId string, apiName string, "
                        + "propertiesMap string);\n" +

                        "\n@sink(type='jms', @map(type='text'),\n"
                        + "factory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory',"
                        + " provider.url='tcp://localhost:61616', destination='TEST.FOO',"
                        + " connection.factory.type='topic',\n"
                        + "connection.factory.jndi.name='TopicConnectionFactory')\n"
                        + "define stream GlobalThrottleStream (throttleKey string, isThrottled bool, "
                        + "expiryTimeStamp long);\n"
                        +

                        "\n" + policy.getSiddhiQuery() + "\n" +

                        "\nfrom ResultStream#throttler:emitOnStateChange(throttleKey, isThrottled)" + "\nselect *\n"
                        + "insert into GlobalThrottleStream;";

        return siddhiApp;
    }

    public static String createDefaultSiddhiAppForAPIThrottlePolicy() {
        APIPolicy apiPolicy = createDefaultAPIPolicy();
        String siddhiApp = "\n@App:name('resource_" + apiPolicy.getPolicyName() + "_condition_0')"
                + "\n@App:description('ExecutionPlan for resource_" + apiPolicy.getPolicyName() + "_condition_0')\n"

                + "\n@source(type='inMemory', topic='apim', @map(type='passThrough'))"
                + "\ndefine stream RequestStream (messageID string, appKey string, appTier string, "
                + "subscriptionKey string,"
                + " apiKey string, apiTier string, subscriptionTier string, resourceKey string,"
                + " resourceTier string, userId string,  apiContext string, apiVersion string, "
                + "appTenant string, apiTenant "
                + "string, appId string, apiName string, propertiesMap string);\n"

                + "\n@sink(type='jms', @map(type='text'),"
                + "\nfactory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory',"
                + " provider.url='tcp://localhost:61616', "
                + "destination='TEST.FOO', connection.factory.type='topic',"
                + "\nconnection.factory.jndi.name='TopicConnectionFactory')"
                + "\ndefine stream GlobalThrottleStream (throttleKey string, isThrottled bool,"
                + " expiryTimeStamp long);\n"

                + "\nFROM RequestStream"
                + "\nSELECT messageID, (resourceTier == 'SampleAPIPolicy' AND (regex:find('Chrome',"
                + "cast(map:get(propertiesMap,'Browser'),"
                + "'string'))) AND (regex:find('attributed',"
                + "cast(map:get(propertiesMap,'/path/path2'),'string'))) AND "
                + "(cast(map:get(propertiesMap,'Location'),'string')=='Colombo'))"
                + " AS isEligible, str:concat(resourceKey,"
                + "'_condition_0') AS throttleKey, propertiesMap" + "\nINSERT INTO EligibilityStream;\n"

                + "\nFROM EligibilityStream[isEligible==true]#throttler:timeBatch(1 s, 0)"
                + "\nselect throttleKey, (count(messageID) >= 1000) as isThrottled,"
                + " expiryTimeStamp group by throttleKey"
                + "\nINSERT ALL EVENTS into ResultStream;\n"

                + "\nfrom ResultStream#throttler:emitOnStateChange(throttleKey, isThrottled)" + "\nselect *"
                + "\ninsert into GlobalThrottleStream;\n";

        return siddhiApp;
    }

    public static String createDefaultSiddhiAppForAPILevelDefaultThrottlePolicy() {
        APIPolicy apiPolicy = createDefaultAPIPolicy();
        String siddhiApp = "\n@App:name('resource_" + apiPolicy.getPolicyName() + "_default')"
                + "\n@App:description('ExecutionPlan for resource_" + apiPolicy.getPolicyName() + "_default')\n"

                + "\n@source(type='inMemory', topic='apim', @map(type='passThrough'))"
                + "\ndefine stream RequestStream (messageID string, appKey string,"
                + " appTier string, subscriptionKey string,"
                + " apiKey string, apiTier string, subscriptionTier string, resourceKey string,"
                + " resourceTier string, userId string,  apiContext string, apiVersion string, appTenant string,"
                + " apiTenant string,"
                + " appId string, apiName string, propertiesMap string);\n"

                + "\n@sink(type='jms', @map(type='text'),"
                + "\nfactory.initial='org.apache.activemq.jndi.ActiveMQInitialContextFactory',"
                + " provider.url='tcp://localhost:61616',"
                + " destination='TEST.FOO', connection.factory.type='topic',"
                + "\nconnection.factory.jndi.name='TopicConnectionFactory')"
                + "\ndefine stream GlobalThrottleStream (throttleKey string, isThrottled bool,"
                + " expiryTimeStamp long);\n"

                + "\nFROM RequestStream"
                + "\nSELECT messageID, (resourceTier == 'SampleAPIPolicy' AND "
                + "NOT(((3232238595l<=cast(map:get(propertiesMap,'ip'),'Long')"
                + " AND 3232258067l>=cast(map:get(propertiesMap,'ip'),'Long')) AND "
                + "(cast(map:get(propertiesMap,'ip'),'Long')==2066353720l)) "
                + "OR ((regex:find('Chrome',cast(map:get(propertiesMap,'Browser'),'string')))"
                + " AND (regex:find('attributed',"
                + "cast(map:get(propertiesMap,'/path/path2'),'string')))"
                + " AND (cast(map:get(propertiesMap,'Location'),'string')=='Colombo'))))"
                + " AS isEligible, resourceKey AS throttleKey, propertiesMap"
                + "\nINSERT INTO EligibilityStream;\n"

                + "\nFROM EligibilityStream[isEligible==true]#throttler:timeBatch(1000 s, 0)"
                + "\nselect throttleKey, (count(messageID) >= 10000) as isThrottled,"
                + " expiryTimeStamp group by throttleKey"
                + "\nINSERT ALL EVENTS into ResultStream;\n"

                + "\nfrom ResultStream#throttler:emitOnStateChange(throttleKey, isThrottled)" + "\nselect *"
                + "\ninsert into GlobalThrottleStream;\n";

        return siddhiApp;
    }

    public static String getSampleApiSwagger() throws IOException {
        //swagger definition
        InputStream stream = null;
        String definition = null;
        try {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("sampleApi.yaml");
            definition = IOUtils.toString(stream);
        } finally {
            stream.close();
        }
        return definition;
    }
}
