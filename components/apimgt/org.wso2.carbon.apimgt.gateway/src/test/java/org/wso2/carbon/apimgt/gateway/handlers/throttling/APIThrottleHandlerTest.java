/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.throttle.core.CallerConfiguration;
import org.apache.synapse.commons.throttle.core.ConcurrentAccessController;
import org.apache.synapse.commons.throttle.core.ThrottleConfiguration;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.impl.rolebase.RoleBaseThrottleContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.metrics.manager.Timer;

import javax.xml.stream.XMLStreamException;
import java.util.TreeMap;

public class APIThrottleHandlerTest {

    private APIThrottleHandler apiThrottleHandler;
    private Timer timer;
    private Timer.Context context;
    private MessageContext messageContext;
    private ThrottleConfiguration throttleConfiguration;
    private CallerConfiguration callerConfiguration;
    private static final String THROTTLE_POLICY_KEY = "gov:/apimgt/applicationdata/tiers.xml";
    private static final String THROTTLE_POLICY_RESOURCE_KEY = "gov:/apimgt/applicationdata/res-tiers.xml";
    private static final String RESPONSE = "RESPONSE";
    private static final String PRODUCTION_MAX_COUNT = "600";
    private static final String SANDBOX_MAX_COUNT = "600";
    private static final String API_AUTH_CONTEXT = "__API_AUTH_CONTEXT";
    private AxisConfiguration axisConfiguration;
    private ConfigurationContext configurationContext;
    private VerbInfoDTO verbInfoDTO;
    private String throttleID = "89";
    private String throttleKey;
    private String domain = "localhost";
    private String IP = "127.0.0.1";
    private String apiContext = "weatherAPI";
    private String apiVersion = "v1";
    private ThrottleContext throttleContext;
    private ConcurrentAccessController concurrentAccessController;
    private String THROTTLING_POLICY_DEFINITION =
                    "<wsp:Policy xmlns:wsp=\"http://schemas.xmlsoap.org/ws/2004/09/policy\"\n" +
                    "            xmlns:throttle=\"http://www.wso2.org/products/wso2commons/throttle\">\n" +
                    "    <throttle:MediatorThrottleAssertion>\n" +
                    "        <wsp:Policy>\n" +
                    "            <throttle:ID throttle:type=\"%s\">%s</throttle:ID>\n" +
                    "            <wsp:Policy>\n" +
                    "                <throttle:Control>\n" +
                    "                    <wsp:Policy>\n" +
                    "                        <throttle:MaximumCount>%d</throttle:MaximumCount>\n" +
                    "                        <throttle:UnitTime>%d</throttle:UnitTime>\n" +
                    "                        <wsp:Policy>\n" +
                    "                            <throttle:Attributes>\n" +
                    "                                " +
                    "<throttle:x-wso2-BillingPlan>FREE</throttle:x-wso2-BillingPlan>\n" +
                    "                                " +
                    "<throttle:x-wso2-StopOnQuotaReach>%s</throttle:x-wso2-StopOnQuotaReach>\n" +
                    "                            </throttle:Attributes>\n" +
                    "                        </wsp:Policy>\n" +
                    "                    </wsp:Policy>\n" +
                    "                </throttle:Control>\n" +
                    "            </wsp:Policy>\n" +
                    "        </wsp:Policy>\n" +
                    "    </throttle:MediatorThrottleAssertion>\n" +
                    "</wsp:Policy>\n";

    @Before
    public void init() {
        throttleContext = Mockito.mock(ThrottleContext.class);
        timer = Mockito.mock(Timer.class);
        context = Mockito.mock(Timer.Context.class);
        throttleConfiguration = Mockito.mock(ThrottleConfiguration.class);
        callerConfiguration = Mockito.mock(CallerConfiguration.class);
        Mockito.when(timer.start()).thenReturn(context);
        messageContext = TestUtils.getMessageContextWithAuthContext(apiContext, apiVersion);
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        throttleKey = "throttle_" + throttleID + "_cac_key";
        verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling("Silver");
        verbInfoDTO.setRequestKey("/weather");
        verbInfoDTO.setHttpVerb("GET");
        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext);
    }

    @Test(expected = SynapseException.class)
    public void testDoThrottleThrowsSynapseExceptionWhenConfigurationContextIsNull() {
        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(null);
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void testInitThrottleThrowsSynapseExceptionWhenPolicyEntryIsNull() throws XMLStreamException {
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void testInitThrottlingThrowsSynapseExceptionWhenThrottlingPolicyDefinitionUnSpecified() {
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        messageContext.getConfiguration().getLocalRegistry().put(THROTTLE_POLICY_KEY, new Object());
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void testInitThrottlingThrowsSynapseExceptionWhenThrottlingPolicyDefinitionInInvalid() throws XMLStreamException {
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        messageContext.getConfiguration().getLocalRegistry().put(THROTTLE_POLICY_KEY, new Entry());
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }


    @Test(expected = SynapseException.class)
    public void testInitThrottlingThrowsSynapseExceptionWhenResourceThrottlingPolicyIsUnSpecified() throws
            XMLStreamException {
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 20, 60000,
                "true"), THROTTLE_POLICY_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test
    public void testMsgContinueWhenMsgIsAResponse() throws XMLStreamException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 0, 60000,
                "true"), THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 0, 60000,
                "true"), THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "true");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Throttling policies are not getting applied, when the message is a response
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueThrottlingWhenConcurrencyCountIsNotExceeded() throws XMLStreamException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 20, 60000,
                "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 20, 60000,
                "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Message should continue as concurrency controller limits are not exceeded
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenConcurrencyCountExceeded() throws XMLStreamException {
        //Set concurrency count to be 1
        concurrentAccessController = new ConcurrentAccessController(1);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 20, 60000,
                "true"), THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD", 20, 60000,
                "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //This request should be succeeded since the concurrency level is not exceeded
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        //This request should fail and throttle out since the concurrency count is exceeded by second request
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueWhenDomainBasedThrottlingAccessRateIsNotExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_ADDR", IP);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_HOST", domain);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "DOMAIN", domain, 20,
                60000, "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "DOMAIN", domain, 20,
                60000, "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Both requests should served by gateway and should not throttle out as the access rates are not exceeded
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }


    @Test
    public void testMsgThrottleOutWhenDomainBasedThrottlingAccessRateIsExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_ADDR", IP);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_HOST", domain);
        //Set domain based throttling limits to be 1/60000
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "DOMAIN", domain, 1,
                60000, "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "DOMAIN", domain, 1,
                60000, "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //First request should continue as the access rates are not exceeded yet (Access rate = 1 per 60000ms )
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        //Second request should throttle out as the access rate is exceeded
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueWhenIPBasedThrottlingAccessRateIsNotExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_ADDR", IP);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_HOST", domain);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "IP", IP, 20, 60000, "true"),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "IP", IP, 20, 60000, "true"),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Both requests should served by gateway and should not throttle out as the access rates are not exceeded
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenIPBasedThrottlingAccessRateIsExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_ADDR", IP);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_HOST", domain);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "IP", IP, 1, 60000, "true"),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "IP", IP, 1, 60000, "true"),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //First request should continue as the access rates are not exceeded yet (Access rate = 1 per 60000ms )
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        //Second request should throttle out as the access rate is exceeded
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueWhenRemoteIPIsNotSpecifiedInMsgCtx() throws XMLStreamException,
            ThrottleException {
        //Set concurrency count to be 100
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, new TreeMap<String, Object>());
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "IP", IP, 0, 60000, "true"),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "IP", IP, 0, 60000, "true"),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Throttling limits won't get applied, since the remote IP is not specified in message context,
        //Thus message will be continued by the gateway, even though the limits are exceeded 0/60000
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test(expected = SynapseException.class)
    public void testMsgFailWhenUnsupportedThrottleTypeProvided() throws XMLStreamException,
            ThrottleException {
        //Set concurrency count to be 100
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "INVALID", IP, 0, 60000,
                "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test
    public void testMsgContinueWhenAuthContextNotProvidedForRoleBasedThrottling() throws XMLStreamException,
            ThrottleException {
        //Get messageContext without Authentication context
        messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        concurrentAccessController = new ConcurrentAccessController(100);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Gold", 0, 60000
                , "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Gold", 0, 60000,
                "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Throttling limits won't apply, since the auth context is not available
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueWhenAccessTokenNotProvidedForRoleBasedThrottling() throws XMLStreamException,
            ThrottleException {
        messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        //Set empty Authentication context
        messageContext.setProperty(API_AUTH_CONTEXT, new AuthenticationContext());
        concurrentAccessController = new ConcurrentAccessController(100);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Gold", 0, 60000,
                "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Gold", 0, 60000,
                "true"),THROTTLE_POLICY_RESOURCE_KEY, true,0,messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //Throttling limits won't apply, since the API token in not available
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgFailWhenResourceThrottlingInfoNotAvaialable() throws XMLStreamException,
            ThrottleException {
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Gold", 20, 60000,
                "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Gold", 20, 60000,
                "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        ThrottleConfiguration throttleConfiguration = Mockito.mock(ThrottleConfiguration.class);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        //Throttling limits won't apply, since the auth context is not available (verbInfoDTO = null)
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenApplicationLevelQuotaExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setApplicationTier("Silver");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 0,
                60000, "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 0,
                60000, "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        Mockito.when(throttleConfiguration.getCallerConfiguration(Mockito.anyString())).thenReturn
                (callerConfiguration);
        //Set application level access state to be ACCESS_DENIED
        Mockito.when(callerConfiguration.getAccessState()).thenReturn(1);
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenResourceLevelQuotaExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1, 60000,
                "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1, 60000,
                "true"),THROTTLE_POLICY_RESOURCE_KEY, true,0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        Mockito.when(throttleConfiguration.getCallerConfiguration(Mockito.anyString())).thenReturn
                (callerConfiguration);
        //Set resource level access state to be ACCESS_DENIED
        Mockito.when(callerConfiguration.getAccessState()).thenReturn(1);
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueWhenResourceLevelQuotaExceededAndStopOnQuotaReachDisabled() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        //Set StopOnQuotaReach = false
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1, 60000,
                "false"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1, 60000,
                "false"),THROTTLE_POLICY_RESOURCE_KEY, true,0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        Mockito.when(throttleConfiguration.getCallerConfiguration(Mockito.anyString())).thenReturn
                (callerConfiguration);
        //Set resource level access state to be ACCESS_DENIED
        Mockito.when(callerConfiguration.getAccessState()).thenReturn(1);
        //Message should be successful even though the access state is set to be 'ACCESS_DENIED', since the
        //stopOnQuotaReach is disabled
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenAPILevelQuotaExceeded() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, new VerbInfoDTO());
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1,
                60000, "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1,
                60000, "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenHardLevelQuotaExceededForProductionEndpoint() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, new VerbInfoDTO());
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1,
                60000, "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1,
                60000, "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        apiThrottleHandler.setProductionMaxCount(PRODUCTION_MAX_COUNT);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        Mockito.when(throttleConfiguration.getCallerConfiguration(Mockito.anyString())).thenReturn
                (callerConfiguration);
        Mockito.when(callerConfiguration.getAccessState()).thenReturn(1);
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenSandboxHardLevelQuotaExceededForSandBoxEndpoint() throws XMLStreamException,
            ThrottleException {
        concurrentAccessController = new ConcurrentAccessController(100);
        configurationContext.setProperty(throttleKey, concurrentAccessController);
        AuthenticationContext authenticationContext = (AuthenticationContext) messageContext.getProperty
                (API_AUTH_CONTEXT);
        authenticationContext.setKeyType("SANDBOX");

        messageContext.setProperty(API_AUTH_CONTEXT, authenticationContext);
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, new VerbInfoDTO());
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1,
                60000, "true"),THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "Silver", 1,
                60000, "true"),THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        apiThrottleHandler.setSandboxMaxCount(SANDBOX_MAX_COUNT);
        Mockito.when(throttleContext.getThrottleConfiguration()).thenReturn(throttleConfiguration);
        Mockito.when(throttleConfiguration.getCallerConfiguration(Mockito.anyString())).thenReturn
                (callerConfiguration);
        Mockito.when(callerConfiguration.getAccessState()).thenReturn(1);
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }


}
