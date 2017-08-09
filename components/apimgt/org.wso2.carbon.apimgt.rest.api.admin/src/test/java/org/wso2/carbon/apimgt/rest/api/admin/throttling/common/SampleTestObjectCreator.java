/*
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
package org.wso2.carbon.apimgt.rest.api.admin.throttling.common;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.BandwidthLimit;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;

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

import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.UNLIMITED_TIER;

public class SampleTestObjectCreator {
    private static final String ACCESS_URL = "dummyUrl";
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
    private static final String NAME_TECHNICAL_OWNER_1 = "John Bob";
    private static final String NAME_TECHNICAL_OWNER_2 = "Jane Bob";
    private static final String EMAIL_TECHNICAL_OWNER_1 = "john.doe@annonymous1.com";
    private static final String EMAIL_TECHNICAL_OWNER_2 = "jane.doe@annonymous1.com";
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
    private static final String SAMPLE_IP_1 = "12.32.45.3";
    private static final String SAMPLE_IP_2 = "24.34.1.45";
    private static final String SAMPLE_CUSTOM_RULE = "Sample Custom Rule";
    public static final String ADMIN_ROLE_ID = "cfbde56e-4352-498e-b6dc-85a6f1f8b058";
    public static final String DEVELOPER_ROLE_ID = "cfdce56e-8434-498e-b6dc-85a6f2d8f035";
    public static APIPolicy unlimitedApiPolicy = new APIPolicy(UUID.randomUUID().toString(), UNLIMITED_TIER);
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
    public static ApplicationPolicy fiftyPerMinApplicationPolicy =
            new ApplicationPolicy(UUID.randomUUID().toString(), FIFTY_PER_MIN_TIER);
    public static  ApplicationPolicy twentyPerMinApplicationPolicy =
            new ApplicationPolicy(UUID.randomUUID().toString(), TWENTY_PER_MIN_TIER);
    public static String apiDefinition;
    public static InputStream inputStream;
    private static final Logger log = LoggerFactory.getLogger(SampleTestObjectCreator.class);
    public static String endpointId = UUID.randomUUID().toString();
    public static String WORKFLOW_STATUS = "ACTIVE";

    public static CompositeAPI.Builder createCompositeAPIModelBuilder() {
        CompositeAPI.Builder compositeAPIBuilder = new CompositeAPI.Builder();
        compositeAPIBuilder.id(UUID.randomUUID().toString()).name("CompisteAPI").apiDefinition("definition").
                            applicationId(UUID.randomUUID().toString()).context("testcontext").provider("provider")
                            .version("1.0.0").context("testcontext").description("testdesc").labels(new HashSet<>());
        return compositeAPIBuilder;
    }

    public static Label.Builder createLabel(String name) {

        List<String> accessUrls = new ArrayList<>();
        accessUrls.add(ACCESS_URL + name);
        return new Label.Builder().
                id(UUID.randomUUID().toString()).
                name(name).
                accessUrls(accessUrls);
    }

    public static Subscription createSubscription(String uuid) {
        if(StringUtils.isEmpty(uuid)) {
            uuid = UUID.randomUUID().toString();
        }
        Subscription subscription = new Subscription(uuid, createDefaultApplication(), createDefaultAPI().build(),
                goldApiPolicy);
        subscription.setStatus(APIMgtConstants.SubscriptionStatus.ACTIVE);
        return subscription;
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
        businessInformation.setBusinessOwner(NAME_BUSINESS_OWNER_1);
        businessInformation.setBusinessOwnerEmail(EMAIL_BUSINESS_OWNER_1);
        businessInformation.setTechnicalOwner(NAME_TECHNICAL_OWNER_1);
        businessInformation.setTechnicalOwnerEmail(EMAIL_TECHNICAL_OWNER_1);

        String permissionJson = "[{\"groupId\" : \"developer\", \"permission\" : "
                + "[\"READ\",\"UPDATE\"]},{\"groupId\" : \"admin\", \"permission\" : [\"READ\",\"UPDATE\"," +
                "\"DELETE\", \"MANAGE_SUBSCRIPTION\"]}]";

        Set<String> visibleRoles = new HashSet<>();
        visibleRoles.add("testRple");

        Set<String> labels = new HashSet<>();
        labels.add("testLabel");

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setEnabled(true);
        corsConfiguration.setAllowMethods(
                Arrays.asList(APIMgtConstants.FunctionsConstants.GET, APIMgtConstants.FunctionsConstants.POST,
                        APIMgtConstants.FunctionsConstants.DELETE));
        corsConfiguration.setAllowHeaders(Arrays.asList(ALLOWED_HEADER_AUTHORIZATION, ALLOWED_HEADER_CUSTOM));
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.setAllowOrigins(Arrays.asList("*"));

        Map<String, Endpoint> endpointMap = new HashMap<>();

        endpointMap.put("TestEndpoint", createMockEndpoint());

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
                apiPolicy(unlimitedApiPolicy).
                transport(transport).
                tags(tags).
                policies(policies).
                visibility(API.Visibility.PUBLIC).
                visibleRoles(visibleRoles).
                businessInformation(businessInformation).
                corsConfiguration(corsConfiguration).
                createdTime(LocalDateTime.now()).
                createdBy(ADMIN).
                updatedBy(ADMIN).
                lastUpdatedTime(LocalDateTime.now()).
                apiPermission(permissionJson).
                uriTemplates(getMockUriTemplates()).
                apiDefinition(apiDefinition).workflowStatus(WORKFLOW_STATUS).
                labels(labels).endpoint(endpointMap);
        Map map = new HashMap();
        map.put(DEVELOPER_ROLE_ID, 6);
        map.put(ADMIN_ROLE_ID, 15);
        apiBuilder.permissionMap(map);
        return apiBuilder;
    }

    public static Endpoint createMockEndpoint() {
        return new Endpoint.Builder().endpointConfig("{'type':'http','url':'http://localhost:8280'}").id(endpointId)
                .maxTps(1000L).security("{\"enabled\":false}").name("Endpoint1")
                .applicableLevel(APIMgtConstants.GLOBAL_ENDPOINT).type("http").build();
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

    public static SubscriptionPolicy createSubscriptionPolicyWithRequestLimit(String name) {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(name);
        subscriptionPolicy.setDescription("testDescription");
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 60 ,10);
        quotaPolicy.setLimit(requestCountLimit);
        subscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        return subscriptionPolicy;
    }

    public static SubscriptionPolicy createSubscriptionPolicyWithBndwidthLimit(String name) {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(name);
        subscriptionPolicy.setDescription("testDescription");
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("bandwidth");
        BandwidthLimit bandwidthLimit = new BandwidthLimit("s", 60 ,10, "mb");
        quotaPolicy.setLimit(bandwidthLimit);
        subscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        return subscriptionPolicy;
    }

    public static ApplicationPolicy createApplicationPolicyWithRequestLimit(String name) {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy(name);
        applicationPolicy.setDescription("testDescription");
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 60 ,10);
        quotaPolicy.setLimit(requestCountLimit);
        applicationPolicy.setDefaultQuotaPolicy(quotaPolicy);
        applicationPolicy.setDisplayName("displayName");
        return applicationPolicy;
    }

    public static ApplicationPolicy createApplicationPolicyWithBndwidthLimit(String name) {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy(name);
        applicationPolicy.setDescription("testDescription");
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("bandwidth");
        BandwidthLimit bandwidthLimit = new BandwidthLimit("s", 60 ,10, "mb");
        quotaPolicy.setLimit(bandwidthLimit);
        applicationPolicy.setDefaultQuotaPolicy(quotaPolicy);
        applicationPolicy.setDisplayName("displayName");
        return applicationPolicy;
    }

    public static APIPolicy createAPIPolicyWithRequestLimit(String name) {
        APIPolicy apiPolicy = new APIPolicy(name);
        apiPolicy.setDescription("testDescription");
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit("s", 60 ,10);
        quotaPolicy.setLimit(requestCountLimit);
        apiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        apiPolicy.setDisplayName("displayName");
        return apiPolicy;
    }

    public static APIPolicy createAPIPolicyWithBndwidthLimit(String name) {
        APIPolicy apiPolicy = new APIPolicy(name);
        apiPolicy.setDescription("testDescription");
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("bandwidth");
        BandwidthLimit bandwidthLimit = new BandwidthLimit("s", 60 ,10, "mb");
        quotaPolicy.setLimit(bandwidthLimit);
        apiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        apiPolicy.setDisplayName("displayName");
        return apiPolicy;
    }
}
