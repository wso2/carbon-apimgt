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
import org.apache.synapse.commons.throttle.core.AccessInformation;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Test cases for for ThrottleHandler
 */
public class ThrottleHandlerTest {
    private Timer timer;
    private Timer.Context context;
    private ThrottleDataHolder throttleDataHolder;
    private ThrottleConditionEvaluator throttleEvaluator;
    private AccessInformation accessInformation;
    private ConditionGroupDTO conditionGroupDTO;
    private ConditionGroupDTO[] conditionGroupDTOs;
    private VerbInfoDTO verbInfoDTO;
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

    @Before
    public void init() {
        timer = Mockito.mock(Timer.class);
        timer = Mockito.mock(Timer.class);
        context = Mockito.mock(Timer.Context.class);
        throttleDataHolder = Mockito.mock(ThrottleDataHolder.class);
        throttleEvaluator = Mockito.mock(ThrottleConditionEvaluator.class);
        accessInformation = Mockito.mock(AccessInformation.class);
        Mockito.when(timer.start()).thenReturn(context);

        verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setHttpVerb(httpVerb);
        verbInfoDTO.setRequestKey(apiContext + "/" + apiVersion + resourceUri + ":" + httpVerb);
        verbInfoDTO.setThrottling(throttlingTier);
        conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("_default");
        conditionGroupDTOs = new ConditionGroupDTO[1];
        conditionGroupDTOs[0] = conditionGroupDTO;

        apiLevelThrottleKey = apiContext + ":" + apiVersion;
        resourceLevelThrottleKey = apiContext + "/" + apiVersion + resourceUri + ":" + httpVerb;

    }

    @Test
    public void testDoNotThrottleWhenMsgIsAResponseAndAuthCtxNotAvailable() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        messageContext.setProperty(RESPONSE, "true");
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testSubscriptionLevelThrottlingInitWhenThrottleCtxIsNull() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(RESPONSE, "true");
        //Test subscription level throttle context initialisation when throttle holder is null
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testSubscriptionLevelThrottlingInitialization() {
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
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);

        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        Mockito.when(throttleDataHolder.isBlockingConditionsPresent()).thenReturn(true);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        Mockito.when(throttleDataHolder.isRequestBlocked(apiContext, authenticationContext
                .getSubscriber() + ":" + authenticationContext.getApplicationName(), authenticationContext
                .getUsername(), "carbon.super" + ":" + "127.0.0.1")).thenReturn(true);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleContinueWhenAPITierIsNotAvailable() {
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
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        //Should continue the message flow if API level, application level, resource level, subscription level,
        //subscription spike level and hard throttling limit levels are not throttled
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgDoThrottleWhenUserLevelThrottlingIsTriggerred() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        verbInfoDTO.setApplicableLevel("userLevel");
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        //Should continue the message flow, when user level throttling is triggered and not exceeded
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));

    }

    @Test
    public void testMsgThrottleOutWhenAPILevelIsThrottled() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        //Set conditional group
        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);

        String combinedResourceLevelThrottleKey = apiLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        Mockito.when(throttleDataHolder.isAPIThrottled(apiLevelThrottleKey)).thenReturn(true);
        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(true);
        Mockito.when(throttleEvaluator.getApplicableConditions(messageContext, authenticationContext,
                conditionGroupDTOs)).thenReturn(matchingConditions);

        //Should throttle out and discontinue message flow, when api level is throttled out
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenResourceLevelIsThrottled() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier("Unlimited");
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();

        Mockito.when(throttleDataHolder.isAPIThrottled(resourceLevelThrottleKey)).thenReturn(true);
        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(true);
        Mockito.when(throttleEvaluator.getApplicableConditions(messageContext, authenticationContext,
                conditionGroupDTOs)).thenReturn(matchingConditions);

        //Should throttle out and discontinue message flow, when resource level is throttled out
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }


    @Test
    public void testMsgThrottleOutWhenSubscriptionLevelIsThrottledAndStopOnQuotaReachIsEnabled() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        authenticationContext.setStopOnQuotaReach(true);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(true);
        //Should throttle out and discontinue message flow, when subscription level is throttled out
        //and stop on quota reach is enabled
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));

    }

    @Test
    public void testMsgContinueWhenSubscriptionLevelIsThrottledAndStopOnQuotaReachIsDisabled() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        //Set stopOnQuota
        authenticationContext.setStopOnQuotaReach(false);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        //Set subscription level throttled out
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(true);
        //Though subscription level is throttled out, should continue the message flow, if stop on quota reach is
        //disabled
        Assert.assertTrue(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenApplicationLevelIsThrottled() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        messageContext.setProperty(VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().getProperty(org.apache.axis2.context
                .MessageContext.TRANSPORT_HEADERS);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty(API_AUTH_CONTEXT);
        authenticationContext.setApiTier(throttlingTier);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername();
        //Set application level throttled out
        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(true);
        //Should discontinue message flow, when application level is throttled
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenProductionHardThrottlingLimitsThrottled() {
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

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername();
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();

        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isKeyTemplatesPresent()).thenReturn(false);
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);

        //Should discontinue message flow if PRODUCTION hard throttling limits are exceeded
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenSandBoxHardThrottlingLimitsThrottled() {
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

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername();
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isKeyTemplatesPresent()).thenReturn(false);
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);

        //Should discontinue message flow if SANDBOX hard throttling limits are exceeded
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }


    @Test
    public void testMsgThrottleOutWhenHardThrottlingFailedWithThrottleException() {
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

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername();
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isKeyTemplatesPresent()).thenReturn(false);

        //Throw ThrottleException while retrieving access information
        Mockito.doThrow(ThrottleException.class).when(accessInformation).isAccessAllowed();
        //Should discontinue message flow, when an exception is thrown during hard limit throttling information
        //process time
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenCustomThrottlingLimitExceeded() {
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

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername();
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isKeyTemplatesPresent()).thenReturn(true);
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);
        Map<String, String> keyTemplateMap = new HashMap<>();
        keyTemplateMap.put("testKeyTemplate", "testKeyTemplateValue");
        Mockito.when(throttleDataHolder.getKeyTemplateMap()).thenReturn(keyTemplateMap);
        Mockito.when(throttleDataHolder.isThrottled("testKeyTemplate")).thenReturn(true);

        //Should discontinue message flow, if custom throttling limit is exceeded
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenHittingSubscriptionLevelSpike() {
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

        verbInfoDTO.setConditionGroups(conditionGroupDTOs);
        ArrayList<ConditionGroupDTO> matchingConditions = new ArrayList<>();
        matchingConditions.add(conditionGroupDTO);
        String subscriptionLevelThrottleKey = authenticationContext.getApplicationId() + ":" + apiContext + ":"
                + apiVersion;
        String applicationLevelThrottleKey = authenticationContext.getApplicationId() + ":" + authenticationContext
                .getUsername();
        String combinedResourceLevelThrottleKey = resourceLevelThrottleKey + conditionGroupDTO.getConditionGroupId();
        Mockito.when(throttleDataHolder.isThrottled(combinedResourceLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(subscriptionLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isThrottled(applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(throttleDataHolder.isKeyTemplatesPresent()).thenReturn(true);
        Mockito.when(accessInformation.isAccessAllowed()).thenReturn(false);
        Assert.assertFalse(throttleHandler.handleRequest(messageContext));
    }

    @Test
    public void testHandleResponse() {
        ThrottleHandler throttleHandler = new ThrottlingHandlerWrapper(timer, throttleDataHolder, throttleEvaluator);
        MessageContext messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        Assert.assertTrue(throttleHandler.handleResponse(messageContext));
    }
}
