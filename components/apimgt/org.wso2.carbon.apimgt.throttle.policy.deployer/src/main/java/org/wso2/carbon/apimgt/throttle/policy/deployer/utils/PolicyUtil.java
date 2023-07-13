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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.GlobalPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.PolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.impl.template.APITemplateException;
import org.wso2.carbon.apimgt.throttle.policy.deployer.PolicyRetriever;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.Policy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.SubscriptionPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.exception.ThrottlePolicyDeployerException;
import org.wso2.carbon.apimgt.throttle.policy.deployer.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.processor.core.EventProcessorService;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanDependencyValidationException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An Utility class for policy deploy operations.
 */
public class PolicyUtil {

    private static final Log log = LogFactory.getLog(PolicyUtil.class);
    private static final String migrationEnabled = System.getProperty(APIConstants.MIGRATE);

    /**
     * Deploy the given throttle policy in the Traffic Manager.
     *
     * @param policy      policy object
     * @param policyEvent policy event object which was triggered
     */
    public static void deployPolicy(Policy policy, PolicyEvent policyEvent) {

        EventProcessorService eventProcessorService =
                ServiceReferenceHolder.getInstance().getEventProcessorService();
        ThrottlePolicyTemplateBuilder policyTemplateBuilder = new ThrottlePolicyTemplateBuilder();

        Map<String, String> policiesToDeploy = new HashMap<>();
        List<String> policiesToUndeploy = new ArrayList<>();

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(APIConstants.SUPER_TENANT_DOMAIN, true);
            String policyFile;
            String policyString;
            if (Policy.PolicyType.SUBSCRIPTION.equals(policy.getType()) && policy instanceof SubscriptionPolicy) {
                // Add Subscription policy
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE,
                        policy.getTenantDomain(), PolicyConstants.POLICY_LEVEL_SUB, policy.getName());
                policyString = policyTemplateBuilder.getThrottlePolicyForSubscriptionLevel((SubscriptionPolicy) policy);
                policiesToDeploy.put(policyFile, policyString);
            } else if (Policy.PolicyType.APPLICATION.equals(policy.getType()) && policy instanceof ApplicationPolicy) {
                // Add Application policy
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE,
                        policy.getTenantDomain(), PolicyConstants.POLICY_LEVEL_APP, policy.getName());
                policyString = policyTemplateBuilder.getThrottlePolicyForAppLevel((ApplicationPolicy) policy);
                policiesToDeploy.put(policyFile, policyString);
            } else if (Policy.PolicyType.API.equals(policy.getType()) && policy instanceof ApiPolicy) {
                // Add API policy
                policiesToDeploy = policyTemplateBuilder.getThrottlePolicyForAPILevel((ApiPolicy) policy);
                String defaultPolicy = policyTemplateBuilder.getThrottlePolicyForAPILevelDefault((ApiPolicy) policy);
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE,
                        policy.getTenantDomain(), PolicyConstants.POLICY_LEVEL_RESOURCE, policy.getName());
                String defaultPolicyName = policyFile + APIConstants.THROTTLE_POLICY_DEFAULT;
                policiesToDeploy.put(defaultPolicyName, defaultPolicy);
                if (policyEvent instanceof APIPolicyEvent) {
                    List<Integer> deletedConditionGroupIds =
                            ((APIPolicyEvent) policyEvent).getDeletedConditionGroupIds();
                    // Undeploy removed condition groups
                    if (deletedConditionGroupIds != null) {
                        for (int conditionGroupId : deletedConditionGroupIds) {
                            policiesToUndeploy
                                    .add(policyFile + APIConstants.THROTTLE_POLICY_CONDITION + conditionGroupId);
                        }
                    }
                }
            } else if (Policy.PolicyType.GLOBAL.equals(policy.getType()) && policy instanceof GlobalPolicy) {
                // Add Global policy
                GlobalPolicy globalPolicy = (GlobalPolicy) policy;
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE,
                        PolicyConstants.POLICY_LEVEL_GLOBAL, policy.getName());
                policyString = policyTemplateBuilder.getThrottlePolicyForGlobalLevel(globalPolicy);
                policiesToDeploy.put(policyFile, policyString);
            }

            // Undeploy removed policies
            undeployPolicies(policiesToUndeploy);

            for (Map.Entry<String, String> pair : policiesToDeploy.entrySet()) {
                String policyPlanName = pair.getKey();
                String flowString = pair.getValue();
                String executionPlan = null;
                try {
                    executionPlan = eventProcessorService.getActiveExecutionPlan(policyPlanName);
                } catch (ExecutionPlanConfigurationException e) {
                    // Deploy new policies
                    eventProcessorService.deployExecutionPlan(flowString);
                }
                if (executionPlan != null) {
                    // Update existing policies
                    eventProcessorService.editActiveExecutionPlan(flowString, policyPlanName);
                }
            }

        } catch (APITemplateException e) {
            log.error("Error in creating execution plan", e);
        } catch (ExecutionPlanConfigurationException | ExecutionPlanDependencyValidationException e) {
            log.error("Error in deploying execution plan", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Deploy all the throttle policies retrieved from the database in the Traffic Manager.
     */
    public static void deployAllPolicies() {
        PolicyRetriever policyRetriever = new PolicyRetriever();
        try {
            // Deploy all the policies retrieved from the database
            SubscriptionPolicyList subscriptionPolicies = new SubscriptionPolicyList();
            if (migrationEnabled == null) {
                subscriptionPolicies = policyRetriever.getAllSubscriptionPolicies();
            }
            ApplicationPolicyList applicationPolicies = policyRetriever.getAllApplicationPolicies();
            ApiPolicyList apiPolicies = policyRetriever.getAllApiPolicies();
            GlobalPolicyList globalPolicies = policyRetriever.getAllGlobalPolicies();
            // Undeploy all existing policies
            undeployAllPolicies();
            for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies.getList()) {
                if (!(APIConstants.UNLIMITED_TIER.equalsIgnoreCase(subscriptionPolicy.getName())
                        || APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED.
                        equalsIgnoreCase(subscriptionPolicy.getName())
                        || APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED.
                        equalsIgnoreCase(subscriptionPolicy.getName()))) {
                    deployPolicy(subscriptionPolicy, null);
                }
            }
            for (ApplicationPolicy applicationPolicy : applicationPolicies.getList()) {
                if (!APIConstants.UNLIMITED_TIER.equalsIgnoreCase(applicationPolicy.getName())) {
                    deployPolicy(applicationPolicy, null);
                }
            }
            for (ApiPolicy apiPolicy : apiPolicies.getList()) {
                if (!APIConstants.UNLIMITED_TIER.equalsIgnoreCase(apiPolicy.getName())) {
                    deployPolicy(apiPolicy, null);
                }
            }
            for (GlobalPolicy globalPolicy : globalPolicies.getList()) {
                deployPolicy(globalPolicy, null);
            }
        } catch (ThrottlePolicyDeployerException e) {
            log.error("Error in retrieving throttle policies", e);
        }
    }

    /**
     * Undeploy all the throttle policies in the Traffic Manager except the excluded ones.
     */
    private static void undeployAllPolicies() {

        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        EventProcessorService eventProcessorService =
                ServiceReferenceHolder.getInstance().getEventProcessorService();
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext()
                    .setTenantDomain(APIConstants.SUPER_TENANT_DOMAIN, true);
            Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap =
                    eventProcessorService.getAllActiveExecutionConfigurations();
            // Undeploy all the policies except the skip ones provided
            for (Map.Entry<String, ExecutionPlanConfiguration> pair : executionPlanConfigurationMap.entrySet()) {
                String policyPlanName = pair.getKey();
                boolean skiped = false;
                for (String skipPolicyName :
                        apiManagerConfiguration.getThrottleProperties().getSkipRedeployingPolicies()) {
                    if (skipPolicyName.equalsIgnoreCase(policyPlanName)) {
                        skiped = true;
                        break;
                    }
                }
                if (!skiped) {
                    eventProcessorService.undeployActiveExecutionPlan(policyPlanName);
                }
            }
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Error in removing existing throttle policies", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Undeploy the subscription throttle policy passed through an event from the Traffic Manager.
     *
     * @param policyEvent subscription policy event object which was triggered
     */
    public static void undeployPolicy(SubscriptionPolicyEvent policyEvent) {

        List<String> policyFileNames = new ArrayList<>();
        String policyFile = policyEvent.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_SUB + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile);
        undeployPolicies(policyFileNames);
    }

    /**
     * Undeploy the application throttle policy passed through an event from the Traffic Manager.
     *
     * @param policyEvent application policy event object which was triggered
     */
    public static void undeployPolicy(ApplicationPolicyEvent policyEvent) {

        List<String> policyFileNames = new ArrayList<>();
        String policyFile = policyEvent.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_APP + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile);
        undeployPolicies(policyFileNames);
    }

    /**
     * Undeploy the API throttle policy passed through an event from the Traffic Manager.
     *
     * @param policyEvent API policy event object which was triggered
     */
    public static void undeployPolicy(APIPolicyEvent policyEvent) {

        List<String> policyFileNames = new ArrayList<>();
        String policyFile = policyEvent.getTenantDomain() + "_" + PolicyConstants.POLICY_LEVEL_RESOURCE + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile + "_default");
        for (int conditionGroupId : policyEvent.getDeletedConditionGroupIds()) {
            policyFileNames.add(policyFile + "_condition_" + conditionGroupId);
        }
        undeployPolicies(policyFileNames);
    }

    /**
     * Undeploy the global throttle policy passed through an event from the Traffic Manager.
     *
     * @param policyEvent global policy event object which was triggered
     */
    public static void undeployPolicy(GlobalPolicyEvent policyEvent) {

        List<String> policyFileNames = new ArrayList<>();
        String policyFile = PolicyConstants.POLICY_LEVEL_GLOBAL + "_" +
                policyEvent.getPolicyName();
        policyFileNames.add(policyFile);
        undeployPolicies(policyFileNames);
    }

    /**
     * Undeploy the throttle policies passed as a list from the Traffic Manager.
     *
     * @param policyFileNames list of policy file names
     */
    private static void undeployPolicies(List<String> policyFileNames) {

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().
                    setTenantDomain(APIConstants.SUPER_TENANT_DOMAIN, true);

            EventProcessorService eventProcessorService =
                    ServiceReferenceHolder.getInstance().getEventProcessorService();
            for (String policyFileName : policyFileNames) {
                String executionPlan = null;
                try {
                    executionPlan = eventProcessorService.getActiveExecutionPlan(policyFileName);
                } catch (ExecutionPlanConfigurationException e) {
                    // Do nothing when execution plan not found
                }
                if (executionPlan != null) {
                    eventProcessorService.undeployActiveExecutionPlan(policyFileName);
                }
            }
        } catch (ExecutionPlanConfigurationException e) {
            log.error("Error in removing execution plan", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
