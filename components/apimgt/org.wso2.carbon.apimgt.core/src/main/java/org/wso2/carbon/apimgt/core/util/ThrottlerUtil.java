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

package org.wso2.carbon.apimgt.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.PolicyConstants;
import org.wso2.carbon.apimgt.core.models.policy.QuotaPolicy;
import org.wso2.carbon.apimgt.core.models.policy.RequestCountLimit;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;

import java.util.UUID;

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

        int[] requestCount = new int[] { 50, 20, 10, Integer.MAX_VALUE };
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
            if (!isPolicyExist(APIMgtAdminService.PolicyLevel.application, policyName)) {
                ApplicationPolicy applicationPolicy = new ApplicationPolicy(policyName);
                applicationPolicy.setUuid(UUID.randomUUID().toString());
                applicationPolicy.setDisplayName(policyName);
                applicationPolicy.setDescription(appPolicyDecs[i]);
                applicationPolicy.setDeployed(true);
                QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
                RequestCountLimit requestCountLimit = new RequestCountLimit(ThrottleConstants.TIME_UNIT_MINUTE, 1,
                        requestCount[i]);
                defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
                defaultQuotaPolicy.setLimit(requestCountLimit);
                applicationPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
                policyDAO.addApplicationPolicy(applicationPolicy);
            }
        }

        //Adding Subscription level policies
        int[] requestCountSubPolicies = new int[] { 5000, 2000, 1000, 500, Integer.MAX_VALUE };
        String[] subPolicies = new String[] { ThrottleConstants.DEFAULT_SUB_POLICY_GOLD,
                ThrottleConstants.DEFAULT_SUB_POLICY_SILVER, ThrottleConstants.DEFAULT_SUB_POLICY_BRONZE,
                ThrottleConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED, ThrottleConstants.DEFAULT_SUB_POLICY_UNLIMITED };
        String[] subPolicyDecs = new String[] { ThrottleConstants.DEFAULT_SUB_POLICY_GOLD_DESC,
                ThrottleConstants.DEFAULT_SUB_POLICY_SILVER_DESC, ThrottleConstants.DEFAULT_SUB_POLICY_BRONZE_DESC,
                ThrottleConstants.DEFAULT_SUB_POLICY_UNAUTHENTICATED_DESC,
                ThrottleConstants.DEFAULT_SUB_POLICY_UNLIMITED_DESC };
        for (int i = 0; i < subPolicies.length; i++) {
            policyName = subPolicies[i];
            if (!isPolicyExist(APIMgtAdminService.PolicyLevel.subscription, policyName)) {
                SubscriptionPolicy subscriptionPolicy = new SubscriptionPolicy(policyName);
                subscriptionPolicy.setUuid(UUID.randomUUID().toString());
                subscriptionPolicy.setDisplayName(policyName);
                subscriptionPolicy.setDescription(subPolicyDecs[i]);
                subscriptionPolicy.setDeployed(true);
                QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
                RequestCountLimit requestCountLimit = new RequestCountLimit(ThrottleConstants.TIME_UNIT_MINUTE, 1,
                        requestCountSubPolicies[i]);
                defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
                defaultQuotaPolicy.setLimit(requestCountLimit);
                subscriptionPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
                subscriptionPolicy.setStopOnQuotaReach(true);
                subscriptionPolicy.setBillingPlan(ThrottleConstants.BILLING_PLAN_FREE);
                policyDAO.addSubscriptionPolicy(subscriptionPolicy);
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
        int[] requestCountApiPolicies = new int[] { 50000, 20000, 10000, Integer.MAX_VALUE };
        for (int i = 0; i < apiPolicies.length; i++) {
            policyName = apiPolicies[i];
            if (!isPolicyExist(APIMgtAdminService.PolicyLevel.api, policyName)) {
                APIPolicy apiPolicy = new APIPolicy(policyName);
                apiPolicy.setUuid(UUID.randomUUID().toString());
                apiPolicy.setDisplayName(policyName);
                apiPolicy.setDescription(apiPolicyDecs[i]);
                apiPolicy.setUserLevel(ThrottleConstants.API_POLICY_API_LEVEL);
                apiPolicy.setDeployed(true);
                QuotaPolicy defaultQuotaPolicy = new QuotaPolicy();
                RequestCountLimit requestCountLimit = new RequestCountLimit(ThrottleConstants.TIME_UNIT_MINUTE, 1,
                        requestCountApiPolicies[i]);
                defaultQuotaPolicy.setType(PolicyConstants.REQUEST_COUNT_TYPE);
                defaultQuotaPolicy.setLimit(requestCountLimit);
                apiPolicy.setDefaultQuotaPolicy(defaultQuotaPolicy);
                policyDAO.addApiPolicy(apiPolicy);
            }
        }
    }

    /**
     * Utility for check policy Exist
     *
     * @param policyLevel Level of policy
     * @param policyName policy Name
     * @return existence of policy
     * @throws APIManagementException throws if any exception occured
     */
    public static boolean isPolicyExist(APIMgtAdminService.PolicyLevel policyLevel, String policyName)
            throws APIManagementException {
        return DAOFactory.getPolicyDAO().policyExists(policyLevel, policyName);
    }
}
