/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.throttle.policy.deployer.utils;

import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.APIPolicyConditionGroup;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Condition;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.QuotaPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.RequestCountLimit;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An Utility class to generate dummy policies.
 */
public class TestUtil {

    /**
     * Generate a dummy API Policy object
     *
     * @return an API Policy
     */
    public static ApiPolicy getPolicyAPILevel() {
        ApiPolicy apiPolicy = new ApiPolicy();
        apiPolicy.setId(1);
        apiPolicy.setTenantId(-1234);
        apiPolicy.setTenantDomain("carbon.super");
        apiPolicy.setName("policy1");
        apiPolicy.setQuotaType("requestCount");

        QuotaPolicy defaultLimit = new QuotaPolicy();
        defaultLimit.setQuotaType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setRequestCount(1);
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(1);
        defaultLimit.setRequestCount(requestCountLimit);
        apiPolicy.setDefaultLimit(defaultLimit);

        apiPolicy.setApplicableLevel("apiLevel");

        List<APIPolicyConditionGroup> conditionGroups = new ArrayList<>();
        APIPolicyConditionGroup conditionGroup1 = new APIPolicyConditionGroup();
        conditionGroup1.setPolicyId(1);
        conditionGroup1.setQuotaType("requestCount");
        conditionGroup1.setConditionGroupId(1);

        Set<Condition> conditions = new HashSet<>();

        Condition condition1 = new Condition();
        condition1.setConditionType("IPSpecific");
        condition1.setName("IPSpecific");
        condition1.setValue("127.0.0.1");
        condition1.setInverted(false);
        conditions.add(condition1);

        conditionGroup1.setCondition(conditions);

        QuotaPolicy defaultLimitCondGroup1 = new QuotaPolicy();
        defaultLimitCondGroup1.setQuotaType("requestCount");
        RequestCountLimit requestCountLimitCondGroup1 = new RequestCountLimit();
        requestCountLimitCondGroup1.setRequestCount(1);
        requestCountLimitCondGroup1.setTimeUnit("min");
        requestCountLimitCondGroup1.setUnitTime(1);
        defaultLimitCondGroup1.setRequestCount(requestCountLimitCondGroup1);
        conditionGroup1.setDefaultLimit(defaultLimitCondGroup1);

        conditionGroups.add(conditionGroup1);

        apiPolicy.setConditionGroups(conditionGroups);

        return apiPolicy;
    }

    /**
     * Generate a dummy Application Policy object
     *
     * @return an Application Policy
     */
    public static ApplicationPolicy getPolicyAppLevel() {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy();
        applicationPolicy.setId(1);
        applicationPolicy.setTenantId(-1234);
        applicationPolicy.setTenantDomain("carbon.super");
        applicationPolicy.setName("policy1");
        applicationPolicy.setQuotaType("requestCount");

        QuotaPolicy defaultLimit = new QuotaPolicy();
        defaultLimit.setQuotaType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setRequestCount(1);
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(1);
        defaultLimit.setRequestCount(requestCountLimit);
        applicationPolicy.setDefaultLimit(defaultLimit);

        return applicationPolicy;
    }

    /**
     * Generate a dummy Subscription Policy object
     *
     * @return an Subscription Policy
     */
    public static SubscriptionPolicy getPolicySubLevel() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy();
        subscriptionPolicy.setId(1);
        subscriptionPolicy.setTenantId(-1234);
        subscriptionPolicy.setTenantDomain("carbon.super");
        subscriptionPolicy.setName("policy1");
        subscriptionPolicy.setQuotaType("requestCount");

        QuotaPolicy defaultLimit = new QuotaPolicy();
        defaultLimit.setQuotaType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setRequestCount(1);
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(1);
        defaultLimit.setRequestCount(requestCountLimit);
        subscriptionPolicy.setDefaultLimit(defaultLimit);

        subscriptionPolicy.setGraphQLMaxComplexity(0);
        subscriptionPolicy.setGraphQLMaxDepth(0);
        subscriptionPolicy.setRateLimitCount(0);
        subscriptionPolicy.setRateLimitTimeUnit(null);
        subscriptionPolicy.setStopOnQuotaReach(true);
        subscriptionPolicy.setSubscriberCount(0);

        return subscriptionPolicy;
    }

    /**
     * Generate a dummy Global Policy object
     *
     * @return an Global Policy
     */
    public static GlobalPolicy getPolicyGlobalLevel() {
        GlobalPolicy globalPolicy = new GlobalPolicy();
        globalPolicy.setId(1);
        globalPolicy.setTenantId(-1234);
        globalPolicy.setTenantDomain("carbon.super");
        globalPolicy.setName("policy1");
        globalPolicy.setQuotaType("requestCount");

        QuotaPolicy defaultLimit = new QuotaPolicy();
        defaultLimit.setQuotaType("requestCount");
        RequestCountLimit requestCountLimit = new RequestCountLimit();
        requestCountLimit.setRequestCount(1);
        requestCountLimit.setTimeUnit("min");
        requestCountLimit.setUnitTime(1);
        defaultLimit.setRequestCount(requestCountLimit);
        globalPolicy.setDefaultLimit(defaultLimit);

        globalPolicy.setSiddhiQuery("FROM   RequestStream SELECT   userId,   " +
                "(userId == 'admin@carbon.super') AS isEligible,   " +
                "str :concat('admin@carbon.super', '') as throttleKey INSERT INTO   " +
                "EligibilityStream; FROM   EligibilityStream [isEligible==true] " +
                "#throttler:timeBatch(1 min) SELECT throttleKey, (count(userId) >= 15) " +
                "as isThrottled, expiryTimeStamp group by throttleKey INSERT ALL EVENTS " +
                "into ResultStream;");
        globalPolicy.setKeyTemplate("$userId:$apiContext:$apiVersion");

        return globalPolicy;
    }
}
