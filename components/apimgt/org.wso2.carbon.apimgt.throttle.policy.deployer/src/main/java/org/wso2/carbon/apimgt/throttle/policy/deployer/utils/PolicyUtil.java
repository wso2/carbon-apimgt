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
import org.wso2.carbon.apimgt.impl.dto.LoadingTenants;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
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
import java.util.Collections;
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
        if (!isTenantAvailable(policy.getTenantDomain())) {
            if (log.isDebugEnabled()) {
                log.debug("Tenant " + policy.getTenantDomain() + " not available for policy deployment. "
                        + "Skipping policy: " + policy.getName());
            }
            return;
        }
        
        if (log.isDebugEnabled()) {
            log.debug("Deploying policy: " + policy.getName() + " of type: " + policy.getType()
                    + " for tenant: " + policy.getTenantDomain());
        }
        EventProcessorService eventProcessorService =
                ServiceReferenceHolder.getInstance().getEventProcessorService();
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        List<String> skipPolicyNames =
                apiManagerConfiguration.getThrottleProperties().getSkipDeployingPolicies() != null ?
                        apiManagerConfiguration.getThrottleProperties().getSkipDeployingPolicies() :
                        Collections.emptyList();
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
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE, policy.getTenantDomain(),
                        PolicyConstants.POLICY_LEVEL_SUB, policy.getName());
                if (shouldDeployPolicy(policyFile, skipPolicyNames)) {
                    // Add Subscription policy
                    policyString = policyTemplateBuilder.getThrottlePolicyForSubscriptionLevel(
                            (SubscriptionPolicy) policy);
                    policiesToDeploy.put(policyFile, policyString);
                }
            } else if (Policy.PolicyType.APPLICATION.equals(policy.getType()) && policy instanceof ApplicationPolicy) {
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE, policy.getTenantDomain(),
                        PolicyConstants.POLICY_LEVEL_APP, policy.getName());
                if (shouldDeployPolicy(policyFile, skipPolicyNames)) {
                    // Add Application policy
                    policyString = policyTemplateBuilder.getThrottlePolicyForAppLevel((ApplicationPolicy) policy);
                    policiesToDeploy.put(policyFile, policyString);
                }
            } else if (Policy.PolicyType.API.equals(policy.getType()) && policy instanceof ApiPolicy) {
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE, policy.getTenantDomain(),
                        PolicyConstants.POLICY_LEVEL_RESOURCE, policy.getName());
                if (shouldDeployPolicy(policyFile, skipPolicyNames)) {
                    // Add API policy
                    policiesToDeploy = policyTemplateBuilder.getThrottlePolicyForAPILevel((ApiPolicy) policy);
                    String defaultPolicy = policyTemplateBuilder.getThrottlePolicyForAPILevelDefault(
                            (ApiPolicy) policy);
                    String defaultPolicyName = policyFile + APIConstants.THROTTLE_POLICY_DEFAULT;
                    policiesToDeploy.put(defaultPolicyName, defaultPolicy);
                }
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
                policyFile = String.join(APIConstants.DELEM_UNDERSCORE,
                        PolicyConstants.POLICY_LEVEL_GLOBAL, policy.getName());
                if (shouldDeployPolicy(policyFile, skipPolicyNames)) {
                    // Add Global policy
                    GlobalPolicy globalPolicy = (GlobalPolicy) policy;
                    policyString = policyTemplateBuilder.getThrottlePolicyForGlobalLevel(globalPolicy);
                    policiesToDeploy.put(policyFile, policyString);
                }
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
                    if (log.isDebugEnabled()) {
                        log.debug("Deploying new execution plan: " + policyPlanName);
                    }
                    eventProcessorService.deployExecutionPlan(flowString);
                    log.info("Successfully deployed new policy execution plan: " + policyPlanName);
                }
                if (executionPlan != null) {
                    // Update existing policies
                    if (log.isDebugEnabled()) {
                        log.debug("Updating existing execution plan: " + policyPlanName);
                    }
                    eventProcessorService.editActiveExecutionPlan(flowString, policyPlanName);
                    log.info("Successfully updated existing policy execution plan: " + policyPlanName);
                }
            }

        } catch (APITemplateException e) {
            log.error("Error in creating execution plan for policy: " + policy.getName() + ", tenant: "
                    + policy.getTenantDomain(), e);
        } catch (ExecutionPlanConfigurationException | ExecutionPlanDependencyValidationException e) {
            log.error("Error in deploying execution plan for policy: " + policy.getName() + ", tenant: "
                    + policy.getTenantDomain(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    /**
     * Deploy all the throttle policies retrieved from the database in the Traffic Manager.
     */
    public static void deployAllPolicies() {
        if (log.isDebugEnabled()) {
            log.debug("Starting deployment of all throttle policies");
        }
        PolicyRetriever policyRetriever = new PolicyRetriever();
        try {
            // Deploy all the policies retrieved from the database
            SubscriptionPolicyList subscriptionPolicies = new SubscriptionPolicyList();
            if (migrationEnabled == null) {
                log.info("Retrieving all subscription policies for deployment");
                subscriptionPolicies = policyRetriever.getAllSubscriptionPolicies();
            } else {
                log.info("Migration mode enabled. Skipping subscription policy retrieval.");
            }
            log.info("Retrieving application policies for deployment");
            ApplicationPolicyList applicationPolicies = policyRetriever.getAllApplicationPolicies();
            log.info("Retrieving API policies for deployment");
            ApiPolicyList apiPolicies = policyRetriever.getAllApiPolicies();
            log.info("Retrieving global policies for deployment");
            GlobalPolicyList globalPolicies = policyRetriever.getAllGlobalPolicies();
            // Undeploy all existing policies
            log.info("Undeploying existing policies before fresh deployment");
            undeployAllPolicies();
            log.info("Deploying subscription policies. Total count: " + subscriptionPolicies.getList().size());
            for (SubscriptionPolicy subscriptionPolicy : subscriptionPolicies.getList()) {
                if (!(APIConstants.UNLIMITED_TIER.equalsIgnoreCase(subscriptionPolicy.getName())
                        || APIConstants.DEFAULT_SUB_POLICY_ASYNC_UNLIMITED.
                        equalsIgnoreCase(subscriptionPolicy.getName())
                        || APIConstants.DEFAULT_SUB_POLICY_ASYNC_WH_UNLIMITED.
                        equalsIgnoreCase(subscriptionPolicy.getName()))) {
                    deployPolicy(subscriptionPolicy, null);
                }
            }
            log.info("Deploying application policies. Total count: " + applicationPolicies.getList().size());
            for (ApplicationPolicy applicationPolicy : applicationPolicies.getList()) {
                if (!APIConstants.UNLIMITED_TIER.equalsIgnoreCase(applicationPolicy.getName())) {
                    deployPolicy(applicationPolicy, null);
                }
            }
            log.info("Deploying API policies. Total count: " + apiPolicies.getList().size());
            for (ApiPolicy apiPolicy : apiPolicies.getList()) {
                if (!APIConstants.UNLIMITED_TIER.equalsIgnoreCase(apiPolicy.getName())) {
                    deployPolicy(apiPolicy, null);
                }
            }
            log.info("Deploying global policies. Total count: " + globalPolicies.getList().size());
            for (GlobalPolicy globalPolicy : globalPolicies.getList()) {
                deployPolicy(globalPolicy, null);
            }
            log.info("Completed deployment of all throttle policies successfully");
        } catch (ThrottlePolicyDeployerException e) {
            log.error("Error in retrieving throttle policies for deployment", e);
        }
    }

    /**
     * Utility method to determine if a policy should be skipped
     * @param policyFileName Name of the policy file
     * @param skipPolicyNames List of policy file names to be skipped deploying
     * @return true or false
     */
    private static boolean shouldDeployPolicy(String policyFileName,
                                              List<String> skipPolicyNames) {
        if (!skipPolicyNames.isEmpty()) {
            return !skipPolicyNames.contains(policyFileName);
        }
        return true;
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

    public static boolean isTenantAvailable(String tenantDomain) {
        APIManagerConfiguration apimConfiguration = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        if (apimConfiguration != null && apimConfiguration.getThrottleProperties() != null
                && apimConfiguration.getThrottleProperties().getPolicyDeployer() != null) {
            ThrottleProperties throttleProperties = apimConfiguration.getThrottleProperties();
            ThrottleProperties.PolicyDeployer policyDeployer = throttleProperties.getPolicyDeployer();
            LoadingTenants loadingTenants = policyDeployer.getLoadingTenants();
            if (loadingTenants != null) {
                return (loadingTenants.isIncludeAllTenants() ||
                        loadingTenants.getIncludingTenants().contains(tenantDomain)) &&
                        !loadingTenants.getExcludingTenants().contains(tenantDomain);
            }
        }
        return true;
    }
}
