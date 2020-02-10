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

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.dto.ConditionDTO;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;
import org.wso2.carbon.apimgt.gateway.TestUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.apimgt.impl.dto.ThrottleProperties;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import scala.actors.threadpool.Arrays;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * ThrottleConditionEvaluatorTest test cases
 */
public class ThrottleConditionEvaluatorTest {

    private static final String THROTTLE_POLICY_DEFAULT = "_default";
    private String apiContext = "weatherAPI";
    private String apiVersion = "v1";
    private ConditionGroupDTO defaultConditionGroupDTO;
    private ThrottleConditionEvaluator throttleConditionEvaluator;
    private String JWTToken =
            "FIg0KIH17DQogICAgImh0.ew0KICAgICJpc3MiOiJ3c28yLm9yZy9wcm9kdWN0cy9hbSIsDQogICAgImV4cCI6MTM0NTE4MzQ" +
                    "5MjE4MSwNCiAgICAiaHR0cDovL3dzbzIub3JnL2NsYWltcy9zdWJzY3JpYmVyIjoiYWRtaW4iDQogfQ==";

    @Before
    public void init() {

        defaultConditionGroupDTO = new ConditionGroupDTO();
        defaultConditionGroupDTO.setConditionGroupId(THROTTLE_POLICY_DEFAULT);
        throttleConditionEvaluator = ThrottleConditionEvaluator.getInstance();
    }

    @Test
    public void testRetrievingDefaultThrottlingConditionGroupWhenConditionGroupsAreNotAvailable() {

        ConditionGroupDTO[] conditionGroupDTOS = {defaultConditionGroupDTO};
        List<ConditionGroupDTO> conditionGroupDTOList = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertEquals(conditionGroupDTOList.size(), 1);
        Assert.assertEquals(conditionGroupDTOList.get(0).getConditionGroupId(), THROTTLE_POLICY_DEFAULT);
    }

    @Test
    public void testRetrievingEmptyApplicableConditionsWhenDefaultAndConditionGroupsAreNotAvailable() {

        ConditionGroupDTO[] conditionGroupDTOS = new ConditionGroupDTO[0];
        List<ConditionGroupDTO> conditionGroupDTOList = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        //Should return empty Condition group in the Condition group array
        Assert.assertNull(conditionGroupDTOList.get(0));
    }

    @Test
    public void testRetrievingEmptyApplicableConditionsWhenConditionsAreNotAvailableInConditionGroup() {

        ConditionDTO[] conditionDTOs = new ConditionDTO[0];
        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditions(conditionDTOs);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};
        List<ConditionGroupDTO> conditionGroupDTOList = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        //Should return empty Condition group in the Condition group array
        Assert.assertNull(conditionGroupDTOList.get(0));
    }

    @Test
    public void testApplicabilityOfMatchingIPRangeCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("IPRangeConditionGroup");

        ConditionDTO matchingCondition = new ConditionDTO();
        matchingCondition.setConditionType("IPRange");
        //127.0.0.1 is in 127.0.0.0 - 127.0.0.2 IP range
        matchingCondition.setConditionName("127.0.0.0");
        matchingCondition.setConditionValue("127.0.0.2");

        ConditionDTO[] conditionDTOS = {matchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertEquals(matchingConditionGroups.size(), 1);
        Assert.assertEquals(matchingConditionGroups.get(0).getConditionGroupId(), "IPRangeConditionGroup");
    }

    @Test
    public void testApplicabilityOfNonMatchingIPRangeCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("IPRangeConditionGroup");
        //127.0.0.1 is not in 10.10.0.1 - 10.10.0.4 IP range
        ConditionDTO nonMatchingCondition = new ConditionDTO();
        nonMatchingCondition.setConditionType("IPRange");
        nonMatchingCondition.setConditionName("10.10.0.1");
        nonMatchingCondition.setConditionValue("10.10.0.4");

        ConditionDTO[] conditionDTOS = {nonMatchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfInvertedIPRangeCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("IPRangeConditionGroup");
        //127.0.0.1 is not in inversion of 127.0.0.0 - 127.0.0.3 IP range
        ConditionDTO invertedIPRangeCondition = new ConditionDTO();
        invertedIPRangeCondition.setConditionType("IPRange");
        invertedIPRangeCondition.setConditionName("127.0.0.0");
        invertedIPRangeCondition.setConditionValue("127.0.0.3");
        invertedIPRangeCondition.isInverted(true);

        ConditionDTO[] conditionDTOS = {invertedIPRangeCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfMatchingIPSpecificCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("IPSpecificConditionGroup");
        ConditionDTO matchingCondition = new ConditionDTO();
        matchingCondition.setConditionType("IPSpecific");
        matchingCondition.setConditionValue("127.0.0.1");
        ConditionDTO[] conditionDTOS = {matchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertEquals(matchingConditionGroups.get(0).getConditionGroupId(), "IPSpecificConditionGroup");
    }

    @Test
    public void testApplicabilityOfNonMatchingIPSpecificCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("IPSpecificConditionGroup");
        ConditionDTO nonMatchingCondition = new ConditionDTO();
        nonMatchingCondition.setConditionType("IPSpecific");
        nonMatchingCondition.setConditionValue("10.0.0.1");
        ConditionDTO[] conditionDTOS = {nonMatchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfInvertedIPSpecificCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("IPSpecificConditionGroup");
        ConditionDTO invertedIPRangeCondition = new ConditionDTO();
        invertedIPRangeCondition.setConditionType("IPSpecific");
        invertedIPRangeCondition.setConditionValue("127.0.0.1");
        invertedIPRangeCondition.isInverted(true);
        ConditionDTO[] conditionDTOS = {invertedIPRangeCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions(TestUtils
                .getMessageContext(apiContext, apiVersion), new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfMatchingQueryParameterTypeCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("QueryParameterTypeConditionGroup");
        ConditionDTO matchingCondition = new ConditionDTO();
        matchingCondition.setConditionType("QueryParameterType");
        matchingCondition.setConditionName("city");
        matchingCondition.setConditionValue("colombo");

        ConditionDTO[] conditionDTOS = {matchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?city=colombo");
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertEquals(matchingConditionGroups.get(0).getConditionGroupId(), "QueryParameterTypeConditionGroup");
    }

    @Test
    public void testApplicabilityOfNonMatchingQueryParameterTypeCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("QueryParameterTypeConditionGroup");
        ConditionDTO invertedCondition = new ConditionDTO();
        invertedCondition.setConditionType("QueryParameterType");
        invertedCondition.setConditionName("city");
        invertedCondition.setConditionValue("mountain-view");

        ConditionDTO[] conditionDTOS = {invertedCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?city=colombo");
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfInvertedQueryParameterTypeCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("QueryParameterTypeConditionGroup");
        ConditionDTO nonMatchingCondition = new ConditionDTO();
        nonMatchingCondition.setConditionType("QueryParameterType");
        nonMatchingCondition.setConditionName("city");
        nonMatchingCondition.setConditionValue("colombo");
        nonMatchingCondition.isInverted(true);

        ConditionDTO[] conditionDTOS = {nonMatchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?city=colombo");
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, new AuthenticationContext(), conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfMatchingJWTClaimsCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("JWTClaimsConditionGroup");
        ConditionDTO matchingCondition = new ConditionDTO();
        matchingCondition.setConditionType("JWTClaims");
        matchingCondition.setConditionName("http://wso2.org/claims/subscriber");
        matchingCondition.setConditionValue("admin");

        ConditionDTO[] conditionDTOS = {matchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(JWTToken);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, authenticationContext, conditionGroupDTOS);
        Assert.assertEquals(matchingConditionGroups.get(0).getConditionGroupId(), "JWTClaimsConditionGroup");
    }

    @Test
    public void testApplicabilityOfNonMatchingJWTClaimsCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("JWTClaimsConditionGroup");
        ConditionDTO nonMatchingCondition = new ConditionDTO();
        nonMatchingCondition.setConditionType("JWTClaims");
        nonMatchingCondition.setConditionName("http://wso2.org/claims/subscriber");
        nonMatchingCondition.setConditionValue("testUser");

        ConditionDTO[] conditionDTOS = {nonMatchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(JWTToken);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, authenticationContext, conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfInvertedJWTClaimsCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("JWTClaimsConditionGroup");
        ConditionDTO invertedCondition = new ConditionDTO();
        invertedCondition.setConditionType("JWTClaims");
        invertedCondition.setConditionName("http://wso2.org/claims/subscriber");
        invertedCondition.setConditionValue("admin");
        invertedCondition.isInverted(true);

        ConditionDTO[] conditionDTOS = {invertedCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(JWTToken);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, authenticationContext, conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testApplicabilityOfMatchingHeaderCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("HeaderConditionGroup");

        ConditionDTO matchingCondition = new ConditionDTO();
        matchingCondition.setConditionType("Header");
        matchingCondition.setConditionName("host");
        matchingCondition.setConditionValue("org.wso2.com");

        ConditionDTO[] conditionDTOS = {matchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(JWTToken);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("host", "org.wso2.com");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, authenticationContext, conditionGroupDTOS);
        Assert.assertEquals(matchingConditionGroups.get(0).getConditionGroupId(), "HeaderConditionGroup");
    }

    @Test
    public void testApplicabilityOfNotMatchingHeaderCondition() {

        ConditionGroupDTO conditionGroupDTO = new ConditionGroupDTO();
        conditionGroupDTO.setConditionGroupId("HeaderConditionGroup");

        ConditionDTO notMatchingCondition = new ConditionDTO();
        notMatchingCondition.setConditionType("Header");
        notMatchingCondition.setConditionName("host");
        notMatchingCondition.setConditionValue("org.ibm.com");

        ConditionDTO[] conditionDTOS = {notMatchingCondition};
        conditionGroupDTO.setConditions(conditionDTOS);
        ConditionGroupDTO[] conditionGroupDTOS = {conditionGroupDTO};

        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(JWTToken);

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("host", "org.wso2.com");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        List<ConditionGroupDTO> matchingConditionGroups = throttleConditionEvaluator.getApplicableConditions
                (messageContext, authenticationContext, conditionGroupDTOS);
        Assert.assertNull(matchingConditionGroups.get(0));
    }

    @Test
    public void testGetThrottledInCondition() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("default", Collections.EMPTY_LIST);
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithIPCondition() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "127.0.0.1");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIPCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIPCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithIPConditionWithDefault() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "127.0.0.2");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIPCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIPCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithIPConditionWithDefaultWithInvert() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "127.0.0.2");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIPCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIPCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithIPConditionWithDefaultWithInvertNegative() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "127.0.0.1");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIPCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIPCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithIPRangeConditionWithDefault() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "192.168.0.5");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIpRangeCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIpRangeCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithIPRangeConditionWithDefaultIpNotInRange() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "192.168.0.12");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIpRangeCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIpRangeCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithIPRangeConditionWithDefaultInvert() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "192.168.0.5");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIpRangeCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIpRangeCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithIPRangeConditionWithDefaultIpNotInRangeInvert() {

        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "192.168.0.12");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getIpRangeCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIpRangeCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithHeaderCondition() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getHeaderCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getHeaderCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithHeaderConditionNegative() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("abc", "cd1");
        map.put("bcd", "xyz");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getHeaderCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getHeaderCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithHeaderConditionInvert() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getHeaderCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getHeaderCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithHeaderConditionNegativeInvert() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("abc", "cd1");
        map.put("bcd", "xyz");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getHeaderCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getHeaderCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithJWTCondition() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableJwtConditions(true);
        String jwt =
                "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3c28yLm9yZy9wcm9kdWN0cy9hbSIsImV4cCI6MTM0NTE4MzQ5MjE4MSwiaHR0cDov" +
                        "L3dzbzIub3JnL2NsYWltcy9hYmMiOiJjZGUiLCJodHRwOi8vd3NvMi5vcmcvY2xhaW1zL2JjZCI6Inh5eiJ9.9zGU062DJ5mQ5hne" +
                        "41h4IRpLbaY_b5thRxb3feebOcA";
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(jwt);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);

        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getJWTCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getJWTCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, authenticationContext,
                conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithJWTConditionNegative() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableJwtConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3c28yLm9yZy9wcm9kdWN0cy9hbSIsImV4cCI6MTM0NTE4MzQ5MjE4MSwiaHR0cD" +
                "ovL3dzbzIub3JnL2NsYWltcy9hYmMiOiJjZCIsImh0dHA6Ly93c28yLm9yZy9jbGFpbXMvYmNkIjoieHl6In0=.yg-FAt9-h7CR" +
                "p7DCM6m4x5xWGxwj4mwXHH4b4sUP9h0";
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(jwt);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getJWTCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getJWTCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, authenticationContext,
                conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithJWTConditionInvert() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableJwtConditions(true);
        String jwt =
                "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3c28yLm9yZy9wcm9kdWN0cy9hbSIsImV4cCI6MTM0NTE4MzQ5MjE4MSwiaHR0cDov" +
                        "L3dzbzIub3JnL2NsYWltcy9hYmMiOiJjZGUiLCJodHRwOi8vd3NvMi5vcmcvY2xhaW1zL2JjZCI6Inh5eiJ9.9zGU062DJ5mQ5hne" +
                        "41h4IRpLbaY_b5thRxb3feebOcA";
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(jwt);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);

        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getJWTCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getJWTCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, authenticationContext,
                conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithJWTConditionNegativeInvert() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableJwtConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJ3c28yLm9yZy9wcm9kdWN0cy9hbSIsImV4cCI6MTM0NTE4MzQ5MjE4MSwiaHR0cD" +
                "ovL3dzbzIub3JnL2NsYWltcy9hYmMiOiJjZCIsImh0dHA6Ly93c28yLm9yZy9jbGFpbXMvYmNkIjoieHl6In0=.yg-FAt9-h7CR" +
                "p7DCM6m4x5xWGxwj4mwXHH4b4sUP9h0";
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setCallerToken(jwt);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getJWTCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getJWTCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, authenticationContext,
                conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithQueryCondition() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableQueryParamConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cde&bcd=xyz");
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getQueryParamCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getQueryParamCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithQueryConditionNegative() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableQueryParamConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cd&bcd=xyz");
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getQueryParamCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getQueryParamCondition(false)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithQueryConditionInvert() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableQueryParamConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cde&bcd=xyz");
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getQueryParamCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getQueryParamCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "default");
    }

    @Test
    public void testGetThrottledInConditionWithComplexConditionNegative() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cde&bcd=xyz");
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getComplexCondition1()}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getComplexCondition1(), getComplexCondition2()}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(null, condition);
    }

    @Test
    public void testGetThrottledInConditionWithQueryConditionNegativeInvert() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableQueryParamConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cd&bcd=xyz");
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getQueryParamCondition(true)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getQueryParamCondition(true)}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithComplexConditionWithLowerProperties() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cde&bcd=xyz");
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "127.0.0.1");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getComplexCondition1()}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getComplexCondition1(), getComplexCondition2()}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(null, condition);
    }

    @Test
    public void testGetThrottledInConditionWithComplexCondition() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        ((Axis2MessageContext) messageContext).getAxis2MessageContext().setProperty("REST_URL_POSTFIX",
                "/temperature?abc=cde&bcd=xyz");
        Map map = new TreeMap();
        map.put("X-Forwarded-For", "127.0.0.1");
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getComplexCondition1()}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getComplexCondition1(), getComplexCondition2()}));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    @Test
    public void testGetThrottledInConditionWithComplexCondition2() {

        ThrottleProperties throttleProperties = new ThrottleProperties();
        throttleProperties.setEnableHeaderConditions(true);
        ServiceReferenceHolder.getInstance().setThrottleProperties(throttleProperties);
        MessageContext messageContext = TestUtils.getMessageContext(apiContext, apiVersion);
        Map map = new TreeMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        map.put("X-Forwarded-For", "127.0.0.2");
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        Map<String, List<ConditionDto>> conditionMap = new HashMap<>();
        conditionMap.put("condition1", Arrays.asList(new ConditionDto[]{getHeaderCondition(false)}));
        conditionMap.put("default", Arrays.asList(new ConditionDto[]{getIPCondition(false), getHeaderCondition(false)
        }));
        String condition = throttleConditionEvaluator.getThrottledInCondition(messageContext, null, conditionMap);
        Assert.assertEquals(condition, "condition1");
    }

    private ConditionDto getIPCondition(boolean invert) {

        ConditionDto conditionDto = new ConditionDto();
        conditionDto.setIpCondition(new ConditionDto.IPCondition(APIUtil.ipToBigInteger("127.0.0.1"), invert));
        return conditionDto;
    }

    private ConditionDto getIpRangeCondition(boolean invert) {

        ConditionDto conditionDto = new ConditionDto();
        conditionDto.setIpRangeCondition(new ConditionDto.IPCondition(APIUtil.ipToBigInteger("192.168.0.0"), APIUtil
                .ipToBigInteger("192.168.0.10"), invert));
        return conditionDto;
    }

    private ConditionDto getHeaderCondition(boolean invert) {

        ConditionDto conditionDto = new ConditionDto();
        ConditionDto.HeaderConditions headerConditions = new ConditionDto.HeaderConditions();
        Map map = new HashMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        headerConditions.setValues(map);
        headerConditions.setInvert(invert);
        conditionDto.setHeaderConditions(headerConditions);
        return conditionDto;
    }

    private ConditionDto getQueryParamCondition(boolean invert) {

        ConditionDto conditionDto = new ConditionDto();
        ConditionDto.QueryParamConditions queryParamConditions = new ConditionDto.QueryParamConditions();
        Map map = new HashMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        queryParamConditions.setValues(map);
        queryParamConditions.setInvert(invert);
        conditionDto.setQueryParameterConditions(queryParamConditions);
        return conditionDto;
    }

    private ConditionDto getJWTCondition(boolean invert) {

        ConditionDto conditionDto = new ConditionDto();
        ConditionDto.JWTClaimConditions jwtClaimConditions = new ConditionDto.JWTClaimConditions();
        Map map = new HashMap();
        map.put("http://wso2.org/claims/abc", "cde");
        map.put("http://wso2.org/claims/bcd", "xyz");
        jwtClaimConditions.setValues(map);
        jwtClaimConditions.setInvert(invert);
        conditionDto.setJwtClaimConditions(jwtClaimConditions);
        return conditionDto;
    }

    private ConditionDto getComplexCondition1() {

        ConditionDto conditionDto = new ConditionDto();
        ConditionDto.IPCondition ipCondition = new ConditionDto.IPCondition(APIUtil.ipToBigInteger("127.0.0.1"), false);
        conditionDto.setIpCondition(ipCondition);
        ConditionDto.HeaderConditions headerConditions = new ConditionDto.HeaderConditions();
        Map map = new HashMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        headerConditions.setValues(map);
        conditionDto.setHeaderConditions(headerConditions);
        return conditionDto;
    }

    private ConditionDto getComplexCondition2() {

        ConditionDto conditionDto = new ConditionDto();
        ConditionDto.IPCondition ipCondition = new ConditionDto.IPCondition(APIUtil.ipToBigInteger("127.0.0.1"), false);
        conditionDto.setIpCondition(ipCondition);
        ConditionDto.QueryParamConditions queryParamConditions = new ConditionDto.QueryParamConditions();
        Map map = new HashMap();
        map.put("abc", "cde");
        map.put("bcd", "xyz");
        queryParamConditions.setValues(map);
        conditionDto.setQueryParameterConditions(queryParamConditions);
        return conditionDto;
    }

}
