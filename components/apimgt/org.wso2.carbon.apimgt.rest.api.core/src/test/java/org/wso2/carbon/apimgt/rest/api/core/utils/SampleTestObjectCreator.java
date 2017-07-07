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

import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.SECONDS_TIMUNIT;
import static org.wso2.carbon.apimgt.core.dao.impl.PolicyDAOImpl.UNLIMITED_TIER;
import static org.wso2.carbon.apimgt.core.models.policy.PolicyConstants.REQUEST_COUNT_TYPE;
import static org.wso2.carbon.apimgt.core.util.APIMgtConstants.SANDBOX_ENDPOINT;

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

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
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
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

public class SampleTestObjectCreator {

    private static final String SAMPLE_API_POLICY = "SampleAPIPolicy";
    private static final String SAMPLE_API_POLICY_DESCRIPTION = "SampleAPIPolicy Description";
    private static final String SAMPLE_APP_POLICY = "SampleAppPolicy";
    private static final String SAMPLE_APP_POLICY_DESCRIPTION = "SampleAppPolicy Description";
    private static final String SAMPLE_SUBSCRIPTION_POLICY = "SampleSubscriptionPolicy";
    private static final String SAMPLE_SUBSCRIPTION_POLICY_DESCRIPTION = "SampleSubscriptionPolicy Description";
    private static final String TIME_UNIT_SECONDS = "s";
    private static final String TIME_UNIT_MONTH = "Month";
    private static final String SAMPLE_CUSTOM_RULE = "Sample Custom Rule";

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

    /**
     * Create default application policy.
     *
     * @return
     */
    public static ApplicationPolicy createDefaultApplicationPolicy() {
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
     * @return
     */
    public static SubscriptionPolicy createDefaultSubscriptionPolicy() {
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
     * @return
     */
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
}
