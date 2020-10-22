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
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.PolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.throttle.policy.deployer.PolicyRetriever;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.*;
import org.wso2.carbon.apimgt.throttle.policy.deployer.exception.ThrottlePolicyDeployerException;
import org.wso2.carbon.apimgt.throttle.policy.deployer.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class PolicyUtil {
    private static final Log log = LogFactory.getLog(PolicyUtil.class);
    private static final String[] excludedPolicyNames = {"requestPreProcessorExecutionPlan"}; //TODO: Add a config for this

    public static void deployPolicy(Policy policy, PolicyEvent policyEvent) {
        EventProcessorService eventProcessorService =
                ServiceReferenceHolder.getInstance().getEventProcessorService();
        ThrottlePolicyTemplateBuilder policyTemplateBuilder = new ThrottlePolicyTemplateBuilder();
        Map<String, String> policiesToDeploy = new HashMap<>();
        List<String> policiesToUndeploy = new ArrayList<>();

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(policy.getTenantId(), true);
            String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            policy.setTenantDomain(tenantDomain);
            String policyFile;
            String policyString;
            if (Policy.POLICY_TYPE.SUBSCRIPTION.equals(policy.getType())) {
                policyFile = tenantDomain + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" + policy.getName();
                policyString = policyTemplateBuilder.getThrottlePolicyForSubscriptionLevel((SubscriptionPolicy) policy);
                policiesToDeploy.put(policyFile, policyString);
            } else if (Policy.POLICY_TYPE.APPLICATION.equals(policy.getType())) {
                policyFile = tenantDomain + "_" + PolicyConstants.POLICY_LEVEL_APP + "_" + policy.getName();
                policyString = policyTemplateBuilder.getThrottlePolicyForAppLevel((ApplicationPolicy) policy);
                policiesToDeploy.put(policyFile, policyString);
            } else if (Policy.POLICY_TYPE.API.equals(policy.getType())) {
                policiesToDeploy = policyTemplateBuilder.getThrottlePolicyForAPILevel((ApiPolicy) policy);
                String defaultPolicy = policyTemplateBuilder.getThrottlePolicyForAPILevelDefault((ApiPolicy) policy);
                policyFile = policy.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE +
                        "_" + policy.getName();
                String defaultPolicyName = policyFile + "_default";
                policiesToDeploy.put(defaultPolicyName, defaultPolicy);
                if (policyEvent != null) {
                    List<Integer> deletedConditionGroupIds =
                            ((APIPolicyEvent) policyEvent).getDeletedConditionGroupIds();
                    if (deletedConditionGroupIds != null) {
                        for (int conditionGroupId : deletedConditionGroupIds) {
                            policiesToUndeploy.add(policyFile + "_condition_" + conditionGroupId);
                        }
                    }
                }
            }

            for (String flowName : policiesToUndeploy) {
                try {
                    String executionPlan = eventProcessorService.getActiveExecutionPlan(flowName);
                    if (executionPlan != null) {
                        eventProcessorService.undeployActiveExecutionPlan(flowName);
                    }
                } catch (ExecutionPlanConfigurationException e) {
                    // Do nothing when execution plan not found
                }
            }

            for (Map.Entry<String, String> pair : policiesToDeploy.entrySet()) {
                String policyPlanName = pair.getKey();
                String flowString = pair.getValue();
                String executionPlan = null;
                try {
                    executionPlan = eventProcessorService.getActiveExecutionPlan(policyPlanName);
                } catch (ExecutionPlanConfigurationException e) {
                    eventProcessorService.deployExecutionPlan(flowString);
                }
                if (executionPlan != null) {
                    eventProcessorService.editActiveExecutionPlan(flowString, policyPlanName);
                }
            }

        } catch (APITemplateException e) {
            log.error("Error in creating execution plan", e);
        } catch (ExecutionPlanConfigurationException | ExecutionPlanDependencyValidationException e) {
            log.error("Error in validating execution plan", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public static void deployAllPolicies() {
        PolicyRetriever policyRetriever = new PolicyRetriever();
        try {
            EventProcessorService eventProcessorService =
                    ServiceReferenceHolder.getInstance().getEventProcessorService();
            Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap =
                    eventProcessorService.getAllActiveExecutionConfigurations();
            for (Map.Entry<String, ExecutionPlanConfiguration> pair : executionPlanConfigurationMap.entrySet()) {
                String policyPlanName = pair.getKey();
                boolean excluded = false;
                for (String excludedPolicyName : excludedPolicyNames) {
                    if (excludedPolicyName.equalsIgnoreCase(policyPlanName)) {
                        excluded = true;
                        break;
                    }
                }
                if (!excluded) {
                    eventProcessorService.undeployActiveExecutionPlan(policyPlanName);
                }
            }

            SubscriptionPolicyList subscriptionPolicies = policyRetriever.getAllSubscriptionPolicies();
            for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies.getList()) {
                deployPolicy(subscriptionPolicy, null);
            }
            ApplicationPolicyList applicationPolicies = policyRetriever.getAllApplicationPolicies();
            for (ApplicationPolicy applicationPolicy : applicationPolicies.getList()) {
                deployPolicy(applicationPolicy, null);
            }
            ApiPolicyList apiPolicies = policyRetriever.getAllApiPolicies();
            for (ApiPolicy apiPolicy : apiPolicies.getList()) {
                deployPolicy(apiPolicy, null);
            }
        } catch (ThrottlePolicyDeployerException e) {
            log.error("Error in retrieving subscription policies", e);
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Error in removing existing policies", e);
        }
    }

    public static void undeployPolicy(SubscriptionPolicyEvent policyEvent) {
        List<String> policyFileNames = new ArrayList<>();
        String policyFile = policyEvent.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile);
        undeployPolicy(policyFileNames, policyEvent.getTenantDomain());
    }

    public static void undeployPolicy(ApplicationPolicyEvent policyEvent) {
        List<String> policyFileNames = new ArrayList<>();
        String policyFile = policyEvent.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_APP + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile);
        undeployPolicy(policyFileNames, policyEvent.getTenantDomain());
    }

    public static void undeployPolicy(APIPolicyEvent policyEvent) {
        List<String> policyFileNames = new ArrayList<>();
        String policyFile = policyEvent.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile + "_default");
        for (int conditionGroupId : policyEvent.getDeletedConditionGroupIds()) {
            policyFileNames.add(policyFile + "_condition_" + conditionGroupId);
        }
        undeployPolicy(policyFileNames, policyEvent.getTenantDomain());
    }

    private static void undeployPolicy(List<String> policyFileNames, String tenantDomain) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantDomain(tenantDomain, true);

            EventProcessorService eventProcessorService =
                    ServiceReferenceHolder.getInstance().getEventProcessorService();
            for (String policyFileName : policyFileNames) {
                eventProcessorService.undeployActiveExecutionPlan(policyFileName);
            }
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Error in removing execution plan", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

}
