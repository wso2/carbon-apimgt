/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dao.test;

import junit.framework.TestCase;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.policy.*;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.util.ArrayList;
import java.util.List;

public class ThrottlingPolicyTest extends TestCase {
    ApiMgtDAO apiMgtDAO;

    @Override
    protected void setUp() throws Exception {
        String dbConfigPath = System.getProperty("APIManagerDBConfigurationPath");
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.load(dbConfigPath);
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(new APIManagerConfigurationServiceImpl(config));
        APIMgtDBUtil.initialize();
        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    public void testInsertApplicationPolicy() throws APIManagementException {
        apiMgtDAO.addApplicationPolicy((ApplicationPolicy) getApplicationPolicy());
    }

    public void testInsertSubscriptionPolicy() throws APIManagementException {
        apiMgtDAO.addSubscriptionPolicy((SubscriptionPolicy) getSubscriptionPolicy());
    }

    public void testInsertAPIPolicy() throws APIManagementException {
        apiMgtDAO.addAPIPolicy((APIPolicy) getPolicyAPILevelPerUser());
    }

    public void testUpdateApplicationPolicy() throws APIManagementException {
        ApplicationPolicy policy = (ApplicationPolicy) getApplicationPolicy();
        policy.setDescription("Updated application description");
        apiMgtDAO.updateApplicationPolicy(policy);
    }

    public void testUpdateSubscriptionPolicy() throws APIManagementException {
        SubscriptionPolicy policy = (SubscriptionPolicy) getSubscriptionPolicy();
        policy.setDescription("Updated subscription description");
        apiMgtDAO.updateSubscriptionPolicy(policy);
    }

    public void testUpdateAPIPolicy() throws APIManagementException {
        APIPolicy policy = (APIPolicy) getPolicyAPILevelPerUser();
        policy.setDescription("New Description");

        ArrayList<Pipeline> pipelines = new ArrayList<Pipeline>();

        Pipeline p = new Pipeline();

        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(50);
        requestCountLimit.setRequestCount(1000);
        quotaPolicy.setLimit(requestCountLimit);

        ArrayList<Condition> conditions =  new ArrayList<Condition>();

        DateCondition dateCondition = new DateCondition();
        dateCondition.setSpecificDate("2016-03-03");
        conditions.add(dateCondition);

        HeaderCondition headerCondition1 = new HeaderCondition();
        headerCondition1.setHeader("User-Agent");
        headerCondition1.setValue("Chrome");
        conditions.add(headerCondition1);

        HeaderCondition headerCondition2 = new HeaderCondition();
        headerCondition2.setHeader("Accept-Ranges");
        headerCondition2.setValue("bytes");
        conditions.add(headerCondition2);

        QueryParameterCondition queryParameterCondition1 = new QueryParameterCondition();
        queryParameterCondition1.setParameter("test1");
        queryParameterCondition1.setValue("testValue1");
        conditions.add(queryParameterCondition1);

        QueryParameterCondition queryParameterCondition2 = new QueryParameterCondition();
        queryParameterCondition2.setParameter("x");
        queryParameterCondition2.setValue("abc");
        conditions.add(queryParameterCondition2);

        JWTClaimsCondition jwtClaimsCondition1= new JWTClaimsCondition();
        jwtClaimsCondition1.setClaimUrl("test_url");
        jwtClaimsCondition1.setAttribute("test_attribute");
        conditions.add(jwtClaimsCondition1);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(conditions);
        pipelines.add(p);

        policy.setPipelines(pipelines);
        apiMgtDAO.updateAPIPolicy(policy);
    }

    public void testDeletePolicy() throws APIManagementException {
        apiMgtDAO.removeThrottlePolicy("app", "Bronze", -1234);
    }

    public void testGetApplicationPolicies() throws APIManagementException {
        apiMgtDAO.getApplicationPolicies(-1234);
    }

    public void testGetSubscriptionPolicies() throws APIManagementException {
        apiMgtDAO.getSubscriptionPolicies(-1234);
    }

    public void testGetApiPolicies() throws APIManagementException {
        apiMgtDAO.getAPIPolicies(-1234);
    }

    public void testGetApplicationPolicy() throws APIManagementException {
        apiMgtDAO.getApplicationPolicy("Bronze", 4);
    }

    public void testGetSubscriptionPolicy() throws APIManagementException {
        apiMgtDAO.getSubscriptionPolicy("Silver", 6);
    }

    public void testGetApiPolicy() throws APIManagementException {
        apiMgtDAO.getAPIPolicy("Bronze", -1234);
    }

    private Policy getPolicyAPILevelPerUser(){
        APIPolicy policy = new APIPolicy("Bronze");

        policy.setUserLevel(PolicyConstants.PER_USER);
        policy.setDescription("Description");
        policy.setTenantId(-1234);

        BandwidthLimit defaultLimit = new BandwidthLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setDataAmount(400);
        defaultLimit.setDataUnit("MB");

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);

        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);

        List<Pipeline> pipelines;
        Pipeline p;
        QuotaPolicy quotaPolicy;
        List<Condition> condition;
        BandwidthLimit bandwidthLimit;
        RequestCountLimit requestCountLimit;
        pipelines = new ArrayList<Pipeline>();


        ///////////pipeline item 1 start//////
        p = new Pipeline();

        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);
        bandwidthLimit = new BandwidthLimit();
        bandwidthLimit.setTimeUnit("min");
        bandwidthLimit.setUnitTime(5);
        bandwidthLimit.setDataAmount(100);
        bandwidthLimit.setDataUnit("GB");
        quotaPolicy.setLimit(bandwidthLimit);

        condition =  new ArrayList<Condition>();
        HTTPVerbCondition verbCond = new HTTPVerbCondition();
        verbCond.setHttpVerb("POST");
        condition.add(verbCond);

        IPRangeCondition ipRangeCondition = new IPRangeCondition();
        ipRangeCondition.setStartingIP("123.3.4.5");
        ipRangeCondition.setEndingIP("123.3.4.25");
        condition.add(ipRangeCondition);


        DateRangeCondition dateRangeCondition = new DateRangeCondition();
        dateRangeCondition.setStartingDate("2016-01-03");
        dateRangeCondition.setEndingDate("2016-01-31");
        condition.add(dateRangeCondition);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item 1 end//////

        ///////////pipeline item 2 start//////
        p = new Pipeline();

        quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType("requestCount");
        requestCountLimit = new RequestCountLimit();
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(50);
        requestCountLimit.setRequestCount(1000);
        quotaPolicy.setLimit(requestCountLimit);

        condition =  new ArrayList<Condition>();

        DateCondition dateCondition = new DateCondition();
        dateCondition.setSpecificDate("2016-01-02");
        condition.add(dateCondition);

        HeaderCondition headerCondition1 = new HeaderCondition();
        headerCondition1.setHeader("User-Agent");
        headerCondition1.setValue("Firefox");
        condition.add(headerCondition1);

        HeaderCondition headerCondition2 = new HeaderCondition();
        headerCondition2.setHeader("Accept-Ranges");
        headerCondition2.setValue("bytes");
        condition.add(headerCondition2);

        QueryParameterCondition queryParameterCondition1 = new QueryParameterCondition();
        queryParameterCondition1.setParameter("test1");
        queryParameterCondition1.setValue("testValue1");
        condition.add(queryParameterCondition1);

        QueryParameterCondition queryParameterCondition2 = new QueryParameterCondition();
        queryParameterCondition2.setParameter("test2");
        queryParameterCondition2.setValue("testValue2");
        condition.add(queryParameterCondition2);

        JWTClaimsCondition jwtClaimsCondition1= new JWTClaimsCondition();
        jwtClaimsCondition1.setClaimUrl("test_url");
        jwtClaimsCondition1.setAttribute("test_attribute");
        condition.add(jwtClaimsCondition1);

        JWTClaimsCondition jwtClaimsCondition2= new JWTClaimsCondition();
        jwtClaimsCondition2.setClaimUrl("test_url");
        jwtClaimsCondition2.setAttribute("test_attribute");
        condition.add(jwtClaimsCondition2);

        p.setQuotaPolicy(quotaPolicy);
        p.setConditions(condition);
        pipelines.add(p);
        ///////////pipeline item 2 end//////

        policy.setPipelines(pipelines);
        return policy;
    }

    private Policy getApplicationPolicy(){
        ApplicationPolicy policy = new ApplicationPolicy("Bronze");

        policy.setDescription("Application policy Description");
        policy.setTenantId(4);

        BandwidthLimit defaultLimit = new BandwidthLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(5);
        defaultLimit.setDataAmount(600);
        defaultLimit.setDataUnit("KB");

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType(PolicyConstants.BANDWIDTH_TYPE);

        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return policy;
    }

    private Policy getSubscriptionPolicy(){
        SubscriptionPolicy policy = new SubscriptionPolicy("Silver");

        policy.setDescription("Subscription policy Description");
        policy.setTenantId(6);

        RequestCountLimit defaultLimit = new RequestCountLimit();
        defaultLimit.setTimeUnit("min");
        defaultLimit.setUnitTime(50);
        defaultLimit.setRequestCount(800);

        QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
        defaultQuotaPolicy.setLimit(defaultLimit);
        defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);

        policy.setDefaultQuotaPolicy(defaultQuotaPolicy);
        return policy;
    }
}
