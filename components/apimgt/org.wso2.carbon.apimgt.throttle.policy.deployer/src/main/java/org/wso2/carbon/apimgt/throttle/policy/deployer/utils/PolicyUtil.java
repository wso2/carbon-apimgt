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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.throttle.policy.deployer.PolicyRetriever;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Policy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.exception.ThrottlePolicyDeployerException;
import org.wso2.carbon.apimgt.throttle.policy.deployer.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

public class PolicyUtil {
    private static final Log log = LogFactory.getLog(PolicyUtil.class);

    public static void deployPolicy(Policy policy) {
        EventProcessorService eventProcessorService =
                ServiceReferenceHolder.getInstance().getEventProcessorService();
        ThrottlePolicyTemplateBuilder policyTemplateBuilder = new ThrottlePolicyTemplateBuilder();

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(policy.getTenantId(), true);
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            policy.setTenantDomain(tenantDomain);
            String policyFile = null;
            if (Policy.POLICY_TYPE.SUBSCRIPTION.equals(policy.getType())) {
                policyFile = tenantDomain + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" + policy.getName();
            }
            String policyString = policyTemplateBuilder.getThrottlePolicyForSubscriptionLevel((SubscriptionPolicy) policy);

            String executionPlan = null;
            try {
                executionPlan = eventProcessorService.getActiveExecutionPlan(policyFile);
            } catch (ExecutionPlanConfigurationException e) {
                eventProcessorService.deployExecutionPlan(policyString);
            }
            if (executionPlan != null) {
                eventProcessorService.editActiveExecutionPlan(policyString, policyFile);
            }

        } catch (APITemplateException e) {
            log.error("Error in creating execution plan", e);
        } catch (ExecutionPlanConfigurationException | ExecutionPlanDependencyValidationException e) {
            log.error("Error in validating execution plan", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void undeployPolicy(String policyName, APIConstants.PolicyType policyType, String tenantDomain) {
        try {
            String policyFile = null;
            if (APIConstants.PolicyType.SUBSCRIPTION.equals(policyType)) {
                policyFile = tenantDomain + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" + policyName;
            }

            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantDomain(tenantDomain, true);

            EventProcessorService eventProcessorService =
                    ServiceReferenceHolder.getInstance().getEventProcessorService();
            eventProcessorService.undeployActiveExecutionPlan(policyFile);
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Error in removing execution plan", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void deployAllPolicies() {
        PolicyRetriever policyRetriever = new PolicyRetriever();
        try {
            SubscriptionPolicyList subscriptionPolicies = policyRetriever.retrieveAllSubscriptionPolicies();
            for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies.getList()) {
                deployPolicy(subscriptionPolicy);
            }
        } catch (ThrottlePolicyDeployerException e) {
            log.error("Error in retrieving subscription policies");
        }
    }

}
