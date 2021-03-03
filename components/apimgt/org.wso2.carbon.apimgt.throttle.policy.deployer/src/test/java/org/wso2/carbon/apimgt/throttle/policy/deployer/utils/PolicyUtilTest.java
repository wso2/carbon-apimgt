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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.notifier.events.APIPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.GlobalPolicyEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.SubscriptionPolicyEvent;
import org.wso2.carbon.apimgt.throttle.policy.deployer.PolicyRetriever;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApiPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.ApplicationPolicyList;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicy;
import org.wso2.carbon.apimgt.throttle.policy.deployer.dto.GlobalPolicyList;
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
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.wso2.carbon.context.PrivilegedCarbonContext")
@PrepareForTest({ServiceReferenceHolder.class, PrivilegedCarbonContext.class, PolicyUtil.class})
public class PolicyUtilTest {
    private EventProcessorService eventProcessorService;
    private PolicyRetriever policyRetriever;

    @Before
    public void setUp() throws Exception {
        System.setProperty("carbon.home", PolicyUtilTest.class.getResource("/").getFile());
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);

        eventProcessorService = Mockito.mock(EventProcessorService.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        Mockito.when(serviceReferenceHolder.getEventProcessorService()).thenReturn(eventProcessorService);

        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        ThrottleProperties throttleProperties = Mockito.mock(ThrottleProperties.class);
        String[] skipThrottlePolicies = {"skipPolicy1"};
        Mockito.when(throttleProperties.getSkipRedeployingPolicies()).thenReturn(skipThrottlePolicies);
        Mockito.when(apiManagerConfiguration.getThrottleProperties()).thenReturn(throttleProperties);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.VELOCITY_LOGGER)).thenReturn(null);
        Mockito.when(serviceReferenceHolder.getAPIMConfiguration()).thenReturn(apiManagerConfiguration);

        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        policyRetriever = Mockito.mock(PolicyRetriever.class);
        PowerMockito.whenNew(PolicyRetriever.class).withNoArguments().thenReturn(policyRetriever);
    }

    @Test
    public void testAddPolicy_APIType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        ExecutionPlanConfigurationException executionPlanConfigurationException =
                Mockito.mock(ExecutionPlanConfigurationException.class);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenThrow(executionPlanConfigurationException);

        ApiPolicy policy = TestUtil.getPolicyAPILevel();
        APIPolicyEvent policyEvent = new APIPolicyEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.POLICY_CREATE.name(), -1234, policy.getTenantDomain(),
                policy.getId(), policy.getName(), policy.getDefaultLimit().getQuotaType(),
                null, null);
        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(2)).deployExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testUpdatePolicy_APIType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {

        ApiPolicy policy = TestUtil.getPolicyAPILevel();
        List<Integer> deletedConditionGroupIds = new ArrayList<>();
        deletedConditionGroupIds.add(5);
        deletedConditionGroupIds.add(6);
        APIPolicyEvent policyEvent = new APIPolicyEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.POLICY_UPDATE.name(), -1234, policy.getTenantDomain(),
                policy.getId(), policy.getName(), policy.getDefaultLimit().getQuotaType(),
                null, deletedConditionGroupIds);

        ExecutionPlanConfigurationException executionPlanConfigurationException =
                Mockito.mock(ExecutionPlanConfigurationException.class);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_condition_1")).thenThrow(executionPlanConfigurationException);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_condition_5"))
                .thenReturn("EXECUTION_PLAN");
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_condition_6"))
                .thenReturn("EXECUTION_PLAN");
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_default"))
                .thenReturn("EXECUTION_PLAN");

        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(2)).undeployActiveExecutionPlan(Mockito.anyString());
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).editActiveExecutionPlan(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).deployExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testDeletePolicy_APIType() throws ExecutionPlanConfigurationException {
        ApiPolicy policy = TestUtil.getPolicyAPILevel();
        List<Integer> deletedConditionGroupIds = new ArrayList<>();
        deletedConditionGroupIds.add(5);
        deletedConditionGroupIds.add(6);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_condition_5"))
                .thenReturn("EXECUTION_PLAN");
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_condition_6"))
                .thenReturn("EXECUTION_PLAN");
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_RESOURCE +
                "_" + policy.getName() + "_default"))
                .thenReturn("EXECUTION_PLAN");
        APIPolicyEvent policyEvent = new APIPolicyEvent(UUID.randomUUID().toString(), System.currentTimeMillis(),
                APIConstants.EventType.POLICY_DELETE.name(), -1234, policy.getTenantDomain(),
                policy.getId(), policy.getName(), policy.getDefaultLimit().getQuotaType(),
                null, deletedConditionGroupIds);
        PolicyUtil.undeployPolicy(policyEvent);
        Mockito.verify(eventProcessorService, Mockito.times(3))
                .undeployActiveExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testAddPolicy_APPType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        ExecutionPlanConfigurationException executionPlanConfigurationException =
                Mockito.mock(ExecutionPlanConfigurationException.class);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenThrow(executionPlanConfigurationException);

        ApplicationPolicy policy = TestUtil.getPolicyAppLevel();
        ApplicationPolicyEvent policyEvent = new ApplicationPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(), policy.getName(),
                policy.getDefaultLimit().getQuotaType());
        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).deployExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testUpdatePolicy_APPType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenReturn("EXECUTION_PLAN");

        ApplicationPolicy policy = TestUtil.getPolicyAppLevel();
        ApplicationPolicyEvent policyEvent = new ApplicationPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(), policy.getName(),
                policy.getDefaultLimit().getQuotaType());
        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).editActiveExecutionPlan(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeletePolicy_AppType() throws ExecutionPlanConfigurationException {
        ApplicationPolicy policy = TestUtil.getPolicyAppLevel();
        ApplicationPolicyEvent policyEvent = new ApplicationPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(), policy.getName(),
                policy.getDefaultLimit().getQuotaType());
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_APP +
                "_" + policy.getName()))
                .thenReturn("EXECUTION_PLAN");
        PolicyUtil.undeployPolicy(policyEvent);
        Mockito.verify(eventProcessorService, Mockito.times(1))
                .undeployActiveExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testAddPolicy_SubType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        ExecutionPlanConfigurationException executionPlanConfigurationException =
                Mockito.mock(ExecutionPlanConfigurationException.class);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenThrow(executionPlanConfigurationException);

        SubscriptionPolicy policy = TestUtil.getPolicySubLevel();
        SubscriptionPolicyEvent policyEvent = new SubscriptionPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(),
                policy.getName(), policy.getDefaultLimit().getQuotaType(),
                policy.getRateLimitCount(), policy.getRateLimitTimeUnit(), policy.isStopOnQuotaReach(),
                policy.getGraphQLMaxDepth(), policy.getGraphQLMaxComplexity(), policy.getSubscriberCount());

        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).deployExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testUpdatePolicy_SubType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenReturn("EXECUTION_PLAN");

        SubscriptionPolicy policy = TestUtil.getPolicySubLevel();
        SubscriptionPolicyEvent policyEvent = new SubscriptionPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(),
                policy.getName(), policy.getDefaultLimit().getQuotaType(),
                policy.getRateLimitCount(), policy.getRateLimitTimeUnit(), policy.isStopOnQuotaReach(),
                policy.getGraphQLMaxDepth(), policy.getGraphQLMaxComplexity(), policy.getSubscriberCount());
        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).editActiveExecutionPlan(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeletePolicy_SubType() throws ExecutionPlanConfigurationException {
        SubscriptionPolicy policy = TestUtil.getPolicySubLevel();
        SubscriptionPolicyEvent policyEvent = new SubscriptionPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_DELETE.name(), -1234,
                policy.getTenantDomain(), policy.getId(),
                policy.getName(), policy.getDefaultLimit().getQuotaType(),
                policy.getRateLimitCount(), policy.getRateLimitTimeUnit(), policy.isStopOnQuotaReach(),
                policy.getGraphQLMaxDepth(), policy.getGraphQLMaxComplexity(), policy.getSubscriberCount());
        Mockito.when(eventProcessorService.getActiveExecutionPlan(policy.getTenantDomain() + "_" +
                PolicyConstants.POLICY_LEVEL_SUB +
                "_" + policy.getName()))
                .thenReturn("EXECUTION_PLAN");
        PolicyUtil.undeployPolicy(policyEvent);
        Mockito.verify(eventProcessorService, Mockito.times(1))
                .undeployActiveExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testAddPolicy_GlobalType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        ExecutionPlanConfigurationException executionPlanConfigurationException =
                Mockito.mock(ExecutionPlanConfigurationException.class);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenThrow(executionPlanConfigurationException);

        GlobalPolicy policy = TestUtil.getPolicyGlobalLevel();
        GlobalPolicyEvent policyEvent = new GlobalPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_CREATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(), policy.getName());
        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).deployExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testUpdatePolicy_GlobalType() throws ExecutionPlanConfigurationException,
            ExecutionPlanDependencyValidationException {
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenReturn("EXECUTION_PLAN");

        GlobalPolicy policy = TestUtil.getPolicyGlobalLevel();
        GlobalPolicyEvent policyEvent = new GlobalPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_UPDATE.name(), -1234,
                policy.getTenantDomain(), policy.getId(), policy.getName());
        PolicyUtil.deployPolicy(policy, policyEvent);
        Mockito.verify(eventProcessorService,
                Mockito.times(1)).editActiveExecutionPlan(Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testDeletePolicy_GlobalType() throws ExecutionPlanConfigurationException {
        GlobalPolicy policy = TestUtil.getPolicyGlobalLevel();
        GlobalPolicyEvent policyEvent = new GlobalPolicyEvent(UUID.randomUUID().toString(),
                System.currentTimeMillis(), APIConstants.EventType.POLICY_DELETE.name(), -1234,
                policy.getTenantDomain(), policy.getId(), policy.getName());
        Mockito.when(eventProcessorService.getActiveExecutionPlan(PolicyConstants.POLICY_LEVEL_GLOBAL +
                "_" + policy.getName()))
                .thenReturn("EXECUTION_PLAN");
        PolicyUtil.undeployPolicy(policyEvent);
        Mockito.verify(eventProcessorService, Mockito.times(1))
                .undeployActiveExecutionPlan(Mockito.anyString());
    }

    @Test
    public void testDeployAllPolicies() throws ExecutionPlanConfigurationException, ThrottlePolicyDeployerException,
            ExecutionPlanDependencyValidationException {

        ExecutionPlanConfigurationException executionPlanConfigurationException =
                Mockito.mock(ExecutionPlanConfigurationException.class);
        Mockito.when(eventProcessorService.getActiveExecutionPlan(Mockito.anyString()))
                .thenThrow(executionPlanConfigurationException);

        Map<String, ExecutionPlanConfiguration> executionPlanConfigurationMap = new HashMap<>();
        ExecutionPlanConfiguration executionPlanConfiguration = Mockito.mock(ExecutionPlanConfiguration.class);
        executionPlanConfigurationMap.put("sample_policy", executionPlanConfiguration);
        Mockito.when(eventProcessorService.getAllActiveExecutionConfigurations())
                .thenReturn(executionPlanConfigurationMap);

        SubscriptionPolicyList subscriptionPolicyList = new SubscriptionPolicyList();
        SubscriptionPolicy subscriptionPolicy = TestUtil.getPolicySubLevel();
        List<SubscriptionPolicy> subscriptionPolicies = new ArrayList<>();
        subscriptionPolicies.add(subscriptionPolicy);
        subscriptionPolicyList.setList(subscriptionPolicies);
        Mockito.when(policyRetriever.getAllSubscriptionPolicies()).thenReturn(subscriptionPolicyList);

        ApplicationPolicyList applicationPolicyList = new ApplicationPolicyList();
        ApplicationPolicy applicationPolicy = TestUtil.getPolicyAppLevel();
        List<ApplicationPolicy> applicationPolicies = new ArrayList<>();
        applicationPolicies.add(applicationPolicy);
        applicationPolicyList.setList(applicationPolicies);
        Mockito.when(policyRetriever.getAllApplicationPolicies()).thenReturn(applicationPolicyList);

        ApiPolicyList apiPolicyList = new ApiPolicyList();
        ApiPolicy apiPolicy = TestUtil.getPolicyAPILevel();
        List<ApiPolicy> apiPolicies = new ArrayList<>();
        apiPolicies.add(apiPolicy);
        apiPolicyList.setList(apiPolicies);
        Mockito.when(policyRetriever.getAllApiPolicies()).thenReturn(apiPolicyList);

        GlobalPolicyList globalPolicyList = new GlobalPolicyList();
        GlobalPolicy globalPolicy = TestUtil.getPolicyGlobalLevel();
        List<GlobalPolicy> globalPolicies = new ArrayList<>();
        globalPolicies.add(globalPolicy);
        globalPolicyList.setList(globalPolicies);
        Mockito.when(policyRetriever.getAllGlobalPolicies()).thenReturn(globalPolicyList);

        PolicyUtil.deployAllPolicies();

        Mockito.verify(eventProcessorService, Mockito.times(5)).deployExecutionPlan(Mockito.anyString());
    }

}
