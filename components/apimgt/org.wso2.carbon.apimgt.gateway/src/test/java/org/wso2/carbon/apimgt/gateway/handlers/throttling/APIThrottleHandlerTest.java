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
import org.apache.synapse.commons.throttle.core.AccessRateController;
import org.apache.synapse.commons.throttle.core.ConcurrentAccessController;
import org.apache.synapse.commons.throttle.core.RoleBasedAccessRateController;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleDataHolder;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.metrics.manager.Timer;

import javax.xml.stream.XMLStreamException;

public class APIThrottleHandlerTest {

    private APIThrottleHandler apiThrottleHandler;
    private Timer timer;
    private Timer.Context context;
    private MessageContext messageContext;
    private AccessRateController accessRateController;
    private RoleBasedAccessRateController roleBasedAccessController;
    private RoleBasedAccessRateController applicationRoleBasedAccessController;
    private static final String THROTTLE_POLICY_KEY = "gov:/apimgt/applicationdata/tiers.xml";
    private static final String THROTTLE_POLICY_RESOURCE_KEY = "gov:/apimgt/applicationdata/res-tiers.xml";
    private static final String RESPONSE = "RESPONSE";
    private static final String PRODUCTION_MAX_COUNT = "600";
    private static final String SANDBOX_MAX_COUNT = "600";
    private AxisConfiguration axisConfiguration;
    private ConfigurationContext configurationContext;
    private VerbInfoDTO verbInfoDTO;
    private String throttleID = "89";
    private String throttleKey;
    private String domain = "localhost";
    private String IP = "127.0.0.1";
    private ThrottleDataHolder throttleDataHolder = new ThrottleDataHolder();
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
                    "                        <throttle:MaximumCount>20</throttle:MaximumCount>\n" +
                    "                        <throttle:UnitTime>60000</throttle:UnitTime>\n" +
                    "                        <wsp:Policy>\n" +
                    "                            <throttle:Attributes>\n" +
                    "                                " +
                    "<throttle:x-wso2-BillingPlan>FREE</throttle:x-wso2-BillingPlan>\n" +
                    "                                " +
                    "<throttle:x-wso2-StopOnQuotaReach>false</throttle:x-wso2-StopOnQuotaReach>\n" +
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
        accessRateController = Mockito.mock(AccessRateController.class);
        applicationRoleBasedAccessController = Mockito.mock(RoleBasedAccessRateController.class);
        roleBasedAccessController = Mockito.mock(RoleBasedAccessRateController.class);
        Mockito.when(timer.start()).thenReturn(context);
        messageContext = TestUtils.getMessageContextWithAuthContext("api", "v1");
        axisConfiguration = new AxisConfiguration();
        configurationContext = new ConfigurationContext(axisConfiguration);
        throttleKey = "throttle_" + throttleID + "_cac_key";
        verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling("Unlimited");
        verbInfoDTO.setRequestKey("/weather");
        verbInfoDTO.setHttpVerb("GET");

    }

    @Test(expected = SynapseException.class)
    public void testDoThrottleThrowsSynapseExceptionWhenConfigurationContextIsNull() {
        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(null);
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void testInitThrottleThrowSynapseExceptionWhenPolicyIsNull() throws XMLStreamException {
        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void testInitThrottlingThrowSynapseExceptionWhenThrottlingPolicyIsUnSpecified() {
        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        messageContext.getConfiguration().getLocalRegistry().put(THROTTLE_POLICY_KEY, new Object());
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test(expected = SynapseException.class)
    public void testInitThrottlingThrowSynapseExceptionWhenResourceThrottlingPolicyIsUnSpecified() throws
            XMLStreamException {
        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_KEY,
                true, 0,
                messageContext);
        messageContext.setProperty(RESPONSE, "false");
        apiThrottleHandler.handleRequest(messageContext);
    }

    @Test
    public void testDoThrottleWhenMsgIsAResponse() throws XMLStreamException {
        concurrentAccessController = new ConcurrentAccessController(100);

        configurationContext.setProperty(throttleKey, concurrentAccessController);

        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "true");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueThrottlingWhenConcurrencyCountIsNotExceeded() throws XMLStreamException {
        concurrentAccessController = new ConcurrentAccessController(100);

        configurationContext.setProperty(throttleKey, concurrentAccessController);

        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgThrottleOutWhenConcurrencyCountExceeded() throws XMLStreamException {
        //Set concurrency count to be 1
        concurrentAccessController = new ConcurrentAccessController(1);

        configurationContext.setProperty(throttleKey, concurrentAccessController);

        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "ROLE", "GOLD"),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        //This request should be succeed since the concurrency level is not exceeded
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));
        //This request should fail and throttle out since the concurrency count is exceeded by second request
        Assert.assertFalse(apiThrottleHandler.handleRequest(messageContext));
    }

    @Test
    public void testMsgContinueWhenDomainBasedThrottlingAccessStateIsTrue() throws XMLStreamException,
            ThrottleException {
        //Set concurrency count to be 1
        concurrentAccessController = new ConcurrentAccessController(100);

        configurationContext.setProperty(throttleKey, concurrentAccessController);

        apiThrottleHandler = new APIThrottleHandlerWrapper(timer, throttleContext, accessRateController,
                roleBasedAccessController, applicationRoleBasedAccessController);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setConfigurationContext(configurationContext);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_ADDR", IP);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REMOTE_HOST", domain);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "DOMAIN", domain),
                THROTTLE_POLICY_KEY, true, 0, messageContext);
        TestUtils.loadAPIThrottlingPolicyEntry(String.format(THROTTLING_POLICY_DEFINITION, "DOMAIN", domain),
                THROTTLE_POLICY_RESOURCE_KEY, true, 0, messageContext);
        messageContext.setProperty(RESPONSE, "false");
        messageContext.setProperty(APIConstants.VERB_INFO_DTO, verbInfoDTO);
        apiThrottleHandler.setPolicyKey(THROTTLE_POLICY_KEY);
        apiThrottleHandler.setPolicyKeyResource(THROTTLE_POLICY_RESOURCE_KEY);
        apiThrottleHandler.setId(throttleID);
        // Mockito.when(accessRateController.canAccess()).thenReturn(null);
        //This request should be succeed since the concurrency level is not exceeded
        Assert.assertTrue(apiThrottleHandler.handleRequest(messageContext));

    }

}
