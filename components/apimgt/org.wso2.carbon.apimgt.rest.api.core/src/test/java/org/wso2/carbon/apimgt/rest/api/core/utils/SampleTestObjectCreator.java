/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.core.utils;

import com.google.common.net.InetAddresses;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.core.models.CorsConfiguration;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
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
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.REQUEST_COUNT_TYPE;

/**
 * Creates sample objects used for test cases.
 */
public class SampleTestObjectCreator {

    protected static final String ADMIN_ROLE_ID = "cfbde56e-4352-498e-b6dc-85a6f1f8b058";
    protected static final String DEVELOPER_ROLE_ID = "cfdce56e-8434-498e-b6dc-85a6f2d8f035";
    private static final String HTTP = "http";
    private static final String TAG_FOOD = "food";
    private static final String TAG_BEVERAGE = "beverage";
    private static final String API_VERSION = "1.0.0";
    private static final String NAME_BUSINESS_OWNER_1 = "John Doe";
    private static final String NAME_BUSINESS_OWNER_2 = "Jane Doe";
    private static final String EMAIL_BUSINESS_OWNER_1 = "john.doe@annonymous.com";
    private static final String EMAIL_BUSINESS_OWNER_2 = "jane.doe@annonymous.com";
    private static final String SAMPLE_API_POLICY = "SampleAPIPolicy";
    private static final String SAMPLE_API_POLICY_DESCRIPTION = "SampleAPIPolicy Description";
    private static final String SAMPLE_APP_POLICY = "SampleAppPolicy";
    private static final String SAMPLE_APP_POLICY_DESCRIPTION = "SampleAppPolicy Description";
    private static final String SAMPLE_SUBSCRIPTION_POLICY = "SampleSubscriptionPolicy";
    private static final String SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION = "SampleSubscriptionPolicy Description";
    private static final String SAMPLE_API_WSDL = "http://www.webservicex.net/globalweather.asmx?op=GetWeather?wsdl";
    private static final String TIME_UNIT_SECONDS = "s";
    private static final String TIME_UNIT_MONTH = "Month";
    private static final String CUSTOMER_ROLE = "customer";
    private static final String EMPLOYEE_ROLE = "employee";
    private static final String MANAGER_ROLE = "manager";
    private static final String ALLOWED_HEADER_AUTHORIZATION = "Authorization";
    private static final String ALLOWED_HEADER_CUSTOM = "X-Custom";
    private static final String API_CREATOR = "Adam Doe";
    private static final String SAMPLE_CUSTOM_RULE = "Sample Custom Rule";
    private static final String GOLD_TIER = "Gold";
    private static final String SILVER_TIER = "Silver";
    private static final String BRONZE_TIER = "Bronze";
    private static final String FIFTY_PER_MIN_TIER = "50PerMin";
    protected static APIPolicy goldApiPolicy = new APIPolicy(UUID.randomUUID().toString(), GOLD_TIER);
    protected static SubscriptionPolicy silverSubscriptionPolicy =
            new SubscriptionPolicy(UUID.randomUUID().toString(), SILVER_TIER);
    protected static SubscriptionPolicy bronzeSubscriptionPolicy =
            new SubscriptionPolicy(UUID.randomUUID().toString(), BRONZE_TIER);
    protected static ApplicationPolicy fiftyPerMinApplicationPolicy =
            new ApplicationPolicy(UUID.randomUUID().toString(), FIFTY_PER_MIN_TIER);
    protected static String apiDefinition;

    /**
     * create default api policy
     *
     * @return APIPolicy object is returned
     */
    protected static APIPolicy createDefaultAPIPolicy() {
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
        pipeline1.setId(0);
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
        pipeline2.setId(1);
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
    protected static APIPolicy createDefaultAPIPolicyWithBandwidthLimit() {
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

    /**
     * Create default application policy.
     *
     * @return
     */
    protected static ApplicationPolicy createDefaultApplicationPolicy() {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy(SAMPLE_APP_POLICY);
        applicationPolicy.setUuid(UUID.randomUUID().toString());
        applicationPolicy.setDisplayName(SAMPLE_APP_POLICY);
        applicationPolicy.setDescription(SAMPLE_APP_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 10000, 1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return applicationPolicy;
    }

    /**
     * Create a subscription policy.
     *
     * @return SubscriptionPolicy object
     */
    protected static SubscriptionPolicy createDefaultSubscriptionPolicy() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setUuid(UUID.randomUUID().toString());
        subscriptionPolicy.setDisplayName(SAMPLE_SUBSCRIPTION_POLICY);
        subscriptionPolicy.setDescription(SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION);
        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setType(REQUEST_COUNT_TYPE);
        RequestCountLimit requestCountLimit = new RequestCountLimit(TIME_UNIT_SECONDS, 10000, 1000);
        defaultQuotaPolicy.setLimit(requestCountLimit);
        subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return subscriptionPolicy;
    }

    /**
     * Create a custom policy.
     *
     * @return CustomPolicy object
     */
    protected static CustomPolicy createDefaultCustomPolicy() {
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

    /**
     * Create SubscriptionValidationData object.
     *
     * @return SubscriptionValidationData object
     */
    public static SubscriptionValidationData createSubscriptionValidationData() {
        SubscriptionValidationData subscriptionData = new SubscriptionValidationData(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), UUID.randomUUID().toString());
        subscriptionData.setSubscriptionPolicy(UUID.randomUUID().toString());
        return subscriptionData;
    }

    /**
     * Create a sample unique LabelDTO object.
     *
     * @return LabelDTO object
     */
    protected static LabelDTO createUniqueLabelDTO() {
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.setName(UUID.randomUUID().toString());
        List<String> accessURLs = new ArrayList<>();
        accessURLs.add(UUID.randomUUID().toString());
        accessURLs.add(UUID.randomUUID().toString());
        accessURLs.add(UUID.randomUUID().toString());
        labelDTO.setAccessUrls(accessURLs);
        return labelDTO;
    }

    /**
     * Create unique URI template objects.
     *
     * @return UriTemplate object
     */
    public static UriTemplate createUniqueUriTemplate() {
        UriTemplate.UriTemplateBuilder uriTemplate = UriTemplate.UriTemplateBuilder.getInstance().
                templateId(UUID.randomUUID().toString())
                .uriTemplate(UUID.randomUUID().toString());
        return uriTemplate.build();
    }

    /**
     * Create unique RegistrationSummary  objects.
     *
     * @return RegistrationSummary object
     */
    protected static RegistrationSummary createUniqueRegistrationSummary() {
        RegistrationSummary registrationSummary = new RegistrationSummary(ServiceReferenceHolder.getInstance()
                .getAPIMConfiguration());
        return registrationSummary;
    }

    /**
     * Create unique PolicyValidationData  objects.
     *
     * @return PolicyValidationData object
     */
    protected static PolicyValidationData createUniquePolicyValidationDataObject() {
        PolicyValidationData policyValidationData = new PolicyValidationData(UUID.randomUUID().toString(),
                UUID.randomUUID().toString(), false);
        return policyValidationData;
    }

    /**
     * Create unique Endpoint  objects.
     *
     * @return Endpoint object
     */
    public static Endpoint createUniqueEndpoint() {
        Endpoint endpoint = new Endpoint.Builder().id(UUID.randomUUID().toString()).name(UUID.randomUUID().toString()).
                type(UUID.randomUUID().toString()).endpointConfig(UUID.randomUUID().toString()).
                applicableLevel(UUID.randomUUID().toString()).security(UUID.randomUUID().toString()).
                maxTps(ThreadLocalRandom.current().nextLong()).config(UUID.randomUUID().toString()).build();
        return endpoint;
    }

    /**
     * Create unique BlockConditions  objects.
     *
     * @return BlockConditions object
     */
    public static BlockConditions createUniqueBlockConditions(String conditionType) {
        BlockConditions blockConditions = new BlockConditions();
        blockConditions.setUuid(UUID.randomUUID().toString());
        blockConditions.setConditionId(new Random().nextInt());
        blockConditions.setConditionType(conditionType);
        if ((APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE).equals(conditionType)) {
            // Generate random IP addresses
            blockConditions.setEndingIP(InetAddresses.fromInteger(new Random().nextInt()).getHostAddress());
            blockConditions.setStartingIP(InetAddresses.fromInteger(new Random().nextInt()).getHostAddress());
        } else if ((APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP).equals(conditionType)) {
            blockConditions.setConditionValue(InetAddresses.fromInteger(new Random().nextInt()).getHostAddress());
        }
        blockConditions.setEnabled(new Random().nextBoolean());
        return blockConditions;
    }

    /**
     * Create a Random Application.
     *
     * @return Application Object
     */
    public static Application createRandomApplication() {
        Application application = new Application(UUID.randomUUID().toString(), UUID.randomUUID().toString());
        application.setId(UUID.randomUUID().toString());
        application.setDescription("This is a test application");
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_CREATED);
        application.setPolicy(fiftyPerMinApplicationPolicy);
        application.setCreatedTime(LocalDateTime.now());
        application.setUpdatedUser("admin");
        application.setUpdatedTime(LocalDateTime.now());
        return application;
    }

    /**
     * Create an unique API.
     *
     * @return APIBuilder with unique API details
     */
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
                "\"DELETE\"]}]";

        Map permissionMap = new HashMap();
        permissionMap.put(DEVELOPER_ROLE_ID, 6);
        permissionMap.put(ADMIN_ROLE_ID, 7);

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


}
