package org.wso2.apk.apimgt.impl.dao;

import org.wso2.apk.apimgt.api.model.policy.*;
import org.wso2.apk.apimgt.impl.APIConstants;

import java.util.UUID;

public class TestObjectCreator {
    private static final String APPLICATION_POLICY_NAME = "100PerMin";
    private static final String SUBSCRIPTION_POLICY_NAME = "Platinum";
    private static final String API_POLICY_NAME = "70PerMin";

    public static ApplicationPolicy createDefaultApplicationPolicy() {
        ApplicationPolicy applicationPolicy = new ApplicationPolicy(APPLICATION_POLICY_NAME);
        applicationPolicy.setDisplayName("100 Per Min");
        applicationPolicy.setUUID(UUID.randomUUID().toString());
        applicationPolicy.setDescription("Custom Application Policy");
        applicationPolicy.setTenantId(-1234);
        applicationPolicy.setTenantDomain("carbon.super");
        applicationPolicy.setDeployed(true);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit limit = new RequestCountLimit();
        limit.setTimeUnit("min");
        limit.setUnitTime(1);
        limit.setRequestCount(100);
        quotaPolicy.setLimit(limit);
        applicationPolicy.setDefaultQuotaPolicy(quotaPolicy);
        return applicationPolicy;
    }

    public static ApplicationPolicy createUpdatedApplicationPolicy() {
        ApplicationPolicy policyToUpdate = createDefaultApplicationPolicy();
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit limit = new RequestCountLimit();
        limit.setTimeUnit("min");
        limit.setUnitTime(1);
        limit.setRequestCount(200);
        quotaPolicy.setLimit(limit);
        policyToUpdate.setDefaultQuotaPolicy(quotaPolicy);
        policyToUpdate.setDescription("Updated Custom Application Policy");
        return policyToUpdate;
    }

    public static SubscriptionPolicy createDefaultSubscriptionPolicy() {
        SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(SUBSCRIPTION_POLICY_NAME);
        subscriptionPolicy.setDisplayName("Platinum Policy");
        subscriptionPolicy.setUUID(UUID.randomUUID().toString());
        subscriptionPolicy.setDescription("Custom Subscription Policy");
        subscriptionPolicy.setTenantId(-1234);
        subscriptionPolicy.setTenantDomain("carbon.super");
        subscriptionPolicy.setDeployed(true);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit limit = new RequestCountLimit();
        limit.setTimeUnit("min");
        limit.setUnitTime(1);
        limit.setRequestCount(1000);
        quotaPolicy.setLimit(limit);
        subscriptionPolicy.setDefaultQuotaPolicy(quotaPolicy);
        subscriptionPolicy.setBillingPlan(APIConstants.BILLING_PLAN_FREE);
        return subscriptionPolicy;
    }

    public static SubscriptionPolicy createUpdatedSubscriptionPolicy() {
        SubscriptionPolicy policyToUpdate = createDefaultSubscriptionPolicy();
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit limit = new RequestCountLimit();
        limit.setTimeUnit("min");
        limit.setUnitTime(1);
        limit.setRequestCount(200);
        quotaPolicy.setLimit(limit);
        policyToUpdate.setDefaultQuotaPolicy(quotaPolicy);
        policyToUpdate.setDescription("Updated Custom Subscription Policy");
        return policyToUpdate;
    }

    public static APIPolicy createDefaultAPIPolicy() {
        APIPolicy apiPolicy = new APIPolicy(API_POLICY_NAME);
        apiPolicy.setDisplayName("70 Per Min");
        apiPolicy.setUUID(UUID.randomUUID().toString());
        apiPolicy.setDescription("Custom API Policy");
        apiPolicy.setTenantId(-1234);
        apiPolicy.setTenantDomain("carbon.super");
        apiPolicy.setDeployed(true);
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit limit = new RequestCountLimit();
        limit.setTimeUnit("min");
        limit.setUnitTime(1);
        limit.setRequestCount(100);
        quotaPolicy.setLimit(limit);
        apiPolicy.setDefaultQuotaPolicy(quotaPolicy);
        apiPolicy.setUserLevel(PolicyConstants.ACROSS_ALL);
        return apiPolicy;
    }

    public static APIPolicy createUpdatedAPIPolicy() {
        APIPolicy policyToUpdate = createDefaultAPIPolicy();
        QuotaPolicy quotaPolicy = new QuotaPolicy();
        quotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
        RequestCountLimit limit = new RequestCountLimit();
        limit.setTimeUnit("min");
        limit.setUnitTime(1);
        limit.setRequestCount(200);
        quotaPolicy.setLimit(limit);
        policyToUpdate.setDefaultQuotaPolicy(quotaPolicy);
        policyToUpdate.setDescription("Updated Custom API Policy");
        return policyToUpdate;
    }

}
