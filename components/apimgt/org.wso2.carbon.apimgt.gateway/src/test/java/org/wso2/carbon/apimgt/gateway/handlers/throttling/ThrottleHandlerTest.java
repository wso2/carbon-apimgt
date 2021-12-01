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
 *
 *
 */
package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.synapse.MessageContext;
import org.apache.synapse.api.ApiConstants;
import org.apache.synapse.commons.throttle.core.AccessInformation;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.common.gateway.extensionlistener.ExtensionListener;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;

import org.wso2.carbon.metrics.manager.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases for for ThrottleHandler.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class})
public class ThrottleHandlerTest {

    private Timer timer;
    private Timer.Context context;
    private ThrottleConditionEvaluator throttleEvaluator;
    private AccessInformation accessInformation;
    private ConditionGroupDTO conditionGroupDTO;
    private ConditionGroupDTO[] conditionGroupDTOs;
    private List<VerbInfoDTO> verbInfoDTO;
    private VerbInfoDTO verbInfo;
    private String resourceLevelThrottleKey;
    private String apiLevelThrottleKey;
    private String apiContext = "weatherAPI";
    private String apiVersion = "v1";
    private String httpVerb = "GET";
    private String resourceUri = "/foo";
    private String throttlingTier = "50KPerMin";
    private static final String RESPONSE = "RESPONSE";
    private static final String API_AUTH_CONTEXT = "__API_AUTH_CONTEXT";
    private static final String VERB_INFO_DTO = "VERB_INFO";
    private static final String blockedUserWithTenantDomain = "blockedUser@carbon.super";
    private static final String userWithTenantDomain = "user@carbon.super";
    private static final String blockedUserWithOutTenantDomain = "blockedUser";
    private Map<String, ExtensionListener> extensionListenerMap = new HashMap<>();

    @Before
    public void init() {
        timer = Mockito.mock(Timer.class);
        timer = Mockito.mock(Timer.class);
        context = Mockito.mock(Timer.Context.class);
        throttleEvaluator = Mockito.mock(ThrottleConditionEvaluator.class);
        accessInformation = Mockito.mock(AccessInformation.class);
        Mockito.when(timer.start()).thenReturn(context);
        verbInfoDTO = new ArrayList<>();
        verbInfo = new VerbInfoDTO();
        verbInfo.setHttpVerb(httpVerb);
        verbInfo.setRequestKey(apiContext + "/" + apiVersion + resourceUri + ":" + httpVerb);
        verbInfo.setThrottling(throttlingTier);
        verbInfoDTO.add(verbInfo);
        conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("_default");
        conditionGroupDTOs = new ConditionGroupDTO[1];
        conditionGroupDTOs[0] = conditionGroupDTO;

        apiLevelThrottleKey = apiContext + ":" + apiVersion;
        resourceLevelThrottleKey = apiContext + "/" + apiVersion + resourceUri + ":" + httpVerb;

        org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder serviceReferenceHolder =
                Mockito.mock(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class);
        PowerMockito.mockStatic(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.class);
        Mockito.when(org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder
                .getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService
                .class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn
                (apiManagerConfigurationService);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getExtensionListenerMap()).thenReturn(extensionListenerMap);

    }

    @Test
    public void testDoNotThrottleWhenMsgIsAResponseAndAuthCtxNotAvailable() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        messageContext.setProperty(RESPONSE, "true");
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testSubscriptionLevelThrottlingInitWhenThrottleCtxIsNull() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(RESPONSE, "true");
        //Test subscription level throttle context initialisation when throttle holder is null
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testSubscriptionLevelThrottlingInitialization() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(RESPONSE, "true");
        //Test subscription level throttle context initialisation when throttle holder is null
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
        //Test subscription level throttle context initialisation when throttle holder is already initialized by first
        //request
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenBlockingConditionsAreSatisfied() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        throttleDataHolder.addIpBlockingCondition("carbon.super", 1, "{\"fixedIp\":\"127.0.0.1\",\"invert\":false}",
                APIConstants.BLOCKING_CONDITIONS_IP);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
//        Mockito.when(throttleDataHolder.isRequestBlocked(apiContext, authenticationContext
//                .getSubscriber() + ":" + authenticationContext.getApplicationName(), authenticationContext
//                .getUsername(), "carbon.super" + ":" + "127.0.0.1")).thenReturn(true);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
        throttleDataHolder.removeIpBlockingCondition("carbon.super", 1);
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleContinueWhenAPITierIsNotAvailable() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        //Make sure that the tier info is not available in the message context
        Assert.assertNull((VerbInfoDTO) messageContext.getProperty(VERB_INFO_DTO));
        //Should continue the message flow if the message context does not have throttling tier information
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgDoContinueWhenAllThrottlingLevelsAreNotThrolled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        ServiceReferenceHolder.getInstance().setThrottleDataPublisher(new ThrottleDataPublisher());
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        //Should continue the message flow if API level, application level, resource level, subscription level,
        //subscription spike level and hard throttling limit levels are not throttled
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgDoThrottleWhenUserLevelThrottlingIsTriggerred() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        verbInfo.setApplicableLevel("userLevel");
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        //Should continue the message flow, when user level throttling is triggered and not exceeded
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));

    }

    @Test
    public void testMsgThrottleOutWhenAPILevelIsThrottled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        //Set conditional group
        verbInfo.setConditionGroups(conditionGroupDTOs);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);

        String combinedResourceLevelThrottleKey = apiLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        throttleDataHolder.addThrottledAPIKey(apiLevelThrottleKey, System.currentTimeMillis() + 10000);
        throttleDataHolder.addThrottleData(combinedResourceLevelThrottleKey, System.currentTimeMillis() + 10000);
        Mockito.when(throttleEvaluator.getApplicableConditions(messageContext, authenticationContext,
                conditionGroupDTOs)).thenReturn(matchingConditions);

        //Should throttle out and discontinue message flow, when api level is throttled out
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenResourceLevelIsThrottled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier("Unlimited");
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        throttleDataHolder.addThrottledAPIKey(resourceLevelThrottleKey, System.currentTimeMillis() + 10000);
        throttleDataHolder.addThrottleData(combinedResourceLevelThrottleKey, System.currentTimeMillis() + 10000);

        Mockito.when(throttleEvaluator.getApplicableConditions(messageContext, authenticationContext,
                conditionGroupDTOs)).thenReturn(matchingConditions);

        //Should throttle out and discontinue message flow, when resource level is throttled out
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }


    @Test
    public void testMsgThrottleOutWhenSubscriptionLevelIsThrottledAndStopOnQuotaReachIsEnabled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setStopOnQuotaReach(true);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion + ":" + authenticationContext.getTier();
        throttleDataHolder.addThrottleData(subscriptionLevelThrottleKey, System.currentTimeMillis() + 10000);
        //Should throttle out and discontinue message flow, when subscription level is throttled out
        //and stop on quota reach is enabled
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));

    }

    @Test
    public void testMsgContinueWhenSubscriptionLevelIsThrottledAndStopOnQuotaReachIsDisabled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        //Set stopOnQuota
        authenticationContext.setStopOnQuotaReach(false);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        //Set subscription level throttled out
        throttleDataHolder.addThrottleData(subscriptionLevelThrottleKey, System.currentTimeMillis() + 10000);
        //Though subscription level is throttled out, should continue the message flow, if stop on quota reach is
        //disabled
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenApplicationLevelIsThrottled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername()+ "@" + throttleHandler.getTenantDomain();
        //Set application level throttled out
        throttleDataHolder.addThrottleData(applicationLevelThrottleKey, System.currentTimeMillis() + 10000);

        //Should discontinue message flow, when application level is throttled
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenProductionHardThrottlingLimitsThrottled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator,
                accessInformation);
        throttleHandler.setProductionMaxCount("100");
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        throttleHandler.init(synapseEnvironment);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setStopOnQuotaReach(false);
        authenticationContext.setKeyType("PRODUCTION");
        authenticationContext.setSpikeArrestLimit(0);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);

        //Should discontinue message flow if PRODUCTION hard throttling limits are exceeded
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenSandBoxHardThrottlingLimitsThrottled() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator,
                accessInformation);
        throttleHandler.setSandboxMaxCount("100");
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        throttleHandler.init(synapseEnvironment);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setStopOnQuotaReach(false);
        authenticationContext.setKeyType("SANDBOX");
        authenticationContext.setSpikeArrestLimit(0);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername()+ "@" + throttleHandler.getTenantDomain();
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
//        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(false);
//        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(false);
//        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(false);
//        Mockito.when(throttleDataHolder.isKeyTemplatesPresent()).thenReturn(false);
//        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);

        //Should discontinue message flow if SANDBOX hard throttling limits are exceeded
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }


    @Test
    public void testMsgThrottleOutWhenHardThrottlingFailedWithThrottleException() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator,
                accessInformation);
        throttleHandler.setProductionMaxCount("100");
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        throttleHandler.init(synapseEnvironment);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setKeyType("SANDBOX");
        authenticationContext.setSpikeArrestLimit(0);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);


        //Throw ThrottleException while retrieving access information
        Mockito.doThrow(ThrottleException.class).when(accessInformation).isAccessAllowed();
        //Should discontinue message flow, when an exception is thrown during hard limit throttling information
        //process time
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenCustomThrottlingLimitExceeded() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator,
                accessInformation);
        throttleHandler.setProductionMaxCount("100");
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        throttleHandler.init(synapseEnvironment);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setSpikeArrestLimit(0);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);
        matchingConditions.add(conditionGroupDTO);
        throttleDataHolder.addKeyTemplate("$user", "$user");
        throttleDataHolder.addKeyTemplate("testKeyTemplate", "testKeyTemplateValue");
        throttleDataHolder.addThrottleData("testKeyTemplate", System.currentTimeMillis() + 10000);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
        throttleDataHolder.removeKeyTemplate("testKeyTemplate");
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenHittingSubscriptionLevelSpike() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator,
                accessInformation);
        throttleHandler.setSandboxMaxCount("100");
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        throttleHandler.init(synapseEnvironment);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setKeyType("SANDBOX");
        authenticationContext.setSpikeArrestLimit(100);
        authenticationContext.setStopOnQuotaReach(true);

        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        throttleDataHolder.addKeyTemplate("$user", "$user");
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testHandleResponse() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        Assert.assertTrue(throttleHandler.handleResponse(messageContext));
    }

    @Test
    public void testCheckForStaledThrottleData() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
        ServiceReferenceHolder.getInstance().setThrottleDataPublisher(new ThrottleDataPublisher());
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator,
                accessInformation);
        throttleHandler.setProductionMaxCount("100");
        SynapseEnvironment synapseEnvironment = Mockito.mock(SynapseEnvironment.class);
        throttleHandler.init(synapseEnvironment);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setSpikeArrestLimit(0);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);
        matchingConditions.add(conditionGroupDTO);
        throttleDataHolder.addKeyTemplate("testKeyTemplate", "testKeyTemplateValue");
        throttleDataHolder.addThrottleData("testKeyTemplate", System.currentTimeMillis() - 10000);
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWithUserBlockingConditions() {
        ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        verbInfo.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        // Adding a user blocking condition
        throttleDataHolder.addUserBlockingCondition(blockedUserWithTenantDomain, blockedUserWithTenantDomain);
        matchingConditions.add(conditionGroupDTO);
        authenticationContext.setApiTier("Unlimited");

        // When a blocked user is invoking
        authenticationContext.setUsername(blockedUserWithTenantDomain);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        throttleDataHolder.addThrottledAPIKey(resourceLevelThrottleKey, System.currentTimeMillis() + 10000);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));

        // When an unblocked user is invoking
        authenticationContext.setUsername(userWithTenantDomain);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        throttleDataHolder.addThrottledAPIKey(resourceLevelThrottleKey, System.currentTimeMillis() + 10000);
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));

        // When a blocked user without tenant domain in the username is invoking
        authenticationContext.setUsername(blockedUserWithOutTenantDomain);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        throttleDataHolder.addThrottledAPIKey(resourceLevelThrottleKey, System.currentTimeMillis() + 10000);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));

        // Remove the user block condition and use blocked user to invoke
        throttleDataHolder.removeUserBlockingCondition(blockedUserWithTenantDomain);
        authenticationContext.setUsername(blockedUserWithTenantDomain);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        throttleDataHolder.addThrottledAPIKey(resourceLevelThrottleKey, System.currentTimeMillis() + 10000);
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    /**
     * This method will test request flow when "isGraphqlSubscriptionRequest" property is set in axis2 message context
     * when incoming transport is websocket. This occurs during Graphql Subscription request flow.
     */
    @Test
    public void testHandleRequestForGraphQLSubscriptions() {

        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, new ThrottleDataHolder(),
                throttleEvaluator, accessInformation);
        Axis2MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MessageContext =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(messageContext.
                getAxis2MessageContext()).thenReturn(axis2MessageContext);
        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("ws");
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(true);
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));

        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("wss");
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));

        // clean up message context
        Mockito.when(messageContext.getProperty(APIConstants.GRAPHQL_SUBSCRIPTION_REQUEST)).thenReturn(false);
        Mockito.when(axis2MessageContext.getIncomingTransportName()).thenReturn("http");
    }

}
