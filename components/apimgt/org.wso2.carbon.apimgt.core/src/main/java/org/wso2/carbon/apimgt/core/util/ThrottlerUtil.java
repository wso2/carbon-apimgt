package org.wso2.carbon.apimgt.core.util;
/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.api.ThrottlePolicyDeploymentManager;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

/**
 * Util function for Throttling related task
 */
public class ThrottlerUtil {

    private static final Log log = LogFactory.getLog(ThrottlerUtil.class);

    /**
     * Deploy default throttle polices at startup
     *
     * @throws APIManagementException throws if any exception occured
     */
    public static void addDefaultAdvancedThrottlePolicies() throws APIManagementException {
        ThrottlePolicyDeploymentManager policyDeploymentManager = APIManagerFactory.getInstance()
                .getThrottlePolicyDeploymentManager();

        long[] requestCount = new long[] { 50, 20, 10, Integer.MAX_VALUE };
        //Adding application level throttle policies
        String[] appPolicies = new String[] { ThrottleConstants.DEFAULT_APP_POLICY_FIFTY_REQ_PER_MIN,
                ThrottleConstants.DEFAULT_APP_POLICY_TWENTY_REQ_PER_MIN,
                ThrottleConstants.DEFAULT_APP_POLICY_TEN_REQ_PER_MIN, ThrottleConstants.DEFAULT_APP_POLICY_UNLIMITED };
        String[] appPolicyDecs = new String[] { ThrottleConstants.DEFAULT_APP_POLICY_LARGE_DESC,
                ThrottleConstants.DEFAULT_APP_POLICY_MEDIUM_DESC, ThrottleConstants.DEFAULT_APP_POLICY_SMALL_DESC,
                ThrottleConstants.DEFAULT_APP_POLICY_UNLIMITED_DESC };
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        String policyName;
        //Add application level throttle policies
        for (int i = 0; i < appPolicies.length; i++) {
            policyName = appPolicies[i];
            if (!isPolicyExist(PolicyConstants.POLICY_LEVEL_APP, policyName)) {
                ApplicationPolicy applicationPolicy = new ApplicationPolicy(policyName);
                applicationPolicy.setDisplayName(policyName);
                applicationPolicy.setDescription(appPolicyDecs[i]);
                applicationPolicy.setDeployed(true);
                QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
                RequestCountLimit requestCountLimit = new RequestCountLimit();
                requestCountLimit.setRequestCount(requestCount[i]);
                requestCountLimit.setUnitTime(1);
                requestCountLimit.setTimeUnit(ThrottleConstants.TIME_UNIT_MINUTE);
                defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
                defaultQuotaPolicy.setLimit(requestCountLimit);
                applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
                policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, applicationPolicy);
                policyDeploymentManager
                        .deployPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, applicationPolicy);
            }
        }

        //Adding Subscription level policies
        long[] requestCountSubPolicies = new long[] { 5000, 2000, 1000, 500, Integer.MAX_VALUE };
        String[] subPolicies = new String[] { ThrottleConstants.DEFAULT_SUB_POLICY_GOLD,
                ThrottleConstants.DEFAULT_SUB_POLICY_SILVER, ThrottleConstants.DEFAULT_SUB_POLICY_BRONZE,
                ThrottleConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED, ThrottleConstants.DEFAULT_SUB_POLICY_UNLIMITED };
        String[] subPolicyDecs = new String[] { ThrottleConstants.DEFAULT_SUB_POLICY_GOLD_DESC,
                ThrottleConstants.DEFAULT_SUB_POLICY_SILVER_DESC, ThrottleConstants.DEFAULT_SUB_POLICY_BRONZE_DESC,
                ThrottleConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED_DESC,
                ThrottleConstants.DEFAULT_SUB_POLICY_UNLIMITED_DESC };
        for (int i = 0; i < subPolicies.length; i++) {
            policyName = subPolicies[i];
            if (!isPolicyExist(PolicyConstants.POLICY_LEVEL_SUB, policyName)) {
                SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(policyName);
                subscriptionPolicy.setDisplayName(policyName);
                subscriptionPolicy.setDescription(subPolicyDecs[i]);
                subscriptionPolicy.setDeployed(true);
                QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
                RequestCountLimit requestCountLimit = new RequestCountLimit();
                requestCountLimit.setRequestCount(requestCountSubPolicies[i]);
                requestCountLimit.setUnitTime(1);
                requestCountLimit.setTimeUnit(ThrottleConstants.TIME_UNIT_MINUTE);
                defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
                defaultQuotaPolicy.setLimit(requestCountLimit);
                subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
                subscriptionPolicy.setStopOnQuotaReach(true);
                subscriptionPolicy.setBillingPlan(ThrottleConstants.BILLING_PLAN_FREE);
                policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, subscriptionPolicy);
                policyDeploymentManager
                        .deployPolicy(APIMgtConstants.ThrottlePolicyConstants.SUBSCRIPTION_LEVEL, subscriptionPolicy);
            }
        }

        //Adding Resource level policies
        String[] apiPolicies = new String[] { ThrottleConstants.DEFAULT_API_POLICY_FIFTY_THOUSAND_REQ_PER_MIN,
                ThrottleConstants.DEFAULT_API_POLICY_TWENTY_THOUSAND_REQ_PER_MIN,
                ThrottleConstants.DEFAULT_API_POLICY_TEN_THOUSAND_REQ_PER_MIN,
                ThrottleConstants.DEFAULT_API_POLICY_UNLIMITED };
        String[] apiPolicyDecs = new String[] { ThrottleConstants.DEFAULT_API_POLICY_ULTIMATE_DESC,
                ThrottleConstants.DEFAULT_API_POLICY_PLUS_DESC, ThrottleConstants.DEFAULT_API_POLICY_BASIC_DESC,
                ThrottleConstants.DEFAULT_API_POLICY_UNLIMITED_DESC };
        long[] requestCountApiPolicies = new long[] { 50000, 20000, 10000, Integer.MAX_VALUE };
        for (int i = 0; i < apiPolicies.length; i++) {
            policyName = apiPolicies[i];
            if (!isPolicyExist(PolicyConstants.POLICY_LEVEL_API, policyName)) {
                APIPolicy apiPolicy = new APIPolicy(policyName);
                apiPolicy.setDisplayName(policyName);
                apiPolicy.setDescription(apiPolicyDecs[i]);
                apiPolicy.setUserLevel(ThrottleConstants.API_POLICY_API_LEVEL);
                apiPolicy.setDeployed(true);
                QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
                RequestCountLimit requestCountLimit = new RequestCountLimit();
                requestCountLimit.setRequestCount(requestCountApiPolicies[i]);
                requestCountLimit.setUnitTime(1);
                requestCountLimit.setTimeUnit(ThrottleConstants.TIME_UNIT_MINUTE);
                defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
                defaultQuotaPolicy.setLimit(requestCountLimit);
                apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
                policyDAO.addPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL, apiPolicy);
                boolean status = policyDeploymentManager
                        .deployPolicy(APIMgtConstants.ThrottlePolicyConstants.API_LEVEL, apiPolicy);
                if (status) {
                    log.debug("Successfully deployed policy " + apiPolicy.getPolicyName());
                } else {
                    log.warn("Default policy '" + apiPolicy.getPolicyName() + "' deployment failed");
                }
            }
        }
    }

    public static boolean isPolicyExist(String policyLevel, String policyName) throws APIManagementException {
        PolicyDAO policyDAO = DAOFactory.getPolicyDAO();
        Policy policy = policyDAO.getPolicy(policyLevel, policyName);
        return policy != null;
    }
}
