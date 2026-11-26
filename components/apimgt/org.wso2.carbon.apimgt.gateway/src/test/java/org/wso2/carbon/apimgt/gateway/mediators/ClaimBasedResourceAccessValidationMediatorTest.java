/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Utils.class)
public class ClaimBasedResourceAccessValidationMediatorTest {

    MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
    private final ClaimBasedResourceAccessValidationMediator mediator
            = Mockito.spy(new ClaimBasedResourceAccessValidationMediator());
    public static final String APPLICATION = "APPLICATION";
    public static final String APPLICATION_USER = "APPLICATION_USER";
    public static final String AUT = "aut";
    public static final String ROLES = "roles";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";
    public static final String MISSING_VALUE = "missing";

    @Before
    public void setup() throws Exception {

        PowerMockito.mockStatic(Utils.class);
        PowerMockito.doNothing().when(Utils.class, "sendFault", Mockito.any(MessageContext.class), Mockito.anyInt());
    }

    @Test
    public void testFlowWhenClaimsMatchingWithRegex() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        String claimValueRegex = "^[A-Za-z]+$";
        mediator.setAccessVerificationClaim(AUT);
        mediator.setAccessVerificationClaimValue(APPLICATION);
        mediator.setAccessVerificationClaimValueRegex(claimValueRegex);
        mediator.setShouldAllowValidation(true);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenClaimsMatchingWithTickFalseWithRegex() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        String claimValueRegex = "^[A-Za-z]+$";
        mediator.setAccessVerificationClaim(AUT);
        mediator.setAccessVerificationClaimValue(APPLICATION);
        mediator.setAccessVerificationClaimValueRegex(claimValueRegex);
        mediator.setShouldAllowValidation(false);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenClaimsNotMatchingWithTickTrueWithRegex() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        String claimValueRegex = "^[A-Za-z]+$";
        mediator.setAccessVerificationClaim(AUT);
        mediator.setAccessVerificationClaimValue(APPLICATION_USER);
        mediator.setAccessVerificationClaimValueRegex(claimValueRegex);
        mediator.setShouldAllowValidation(true);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenClaimsMatchingWithTickTrueWithoutRegex() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        String claimValueRegex = "";
        mediator.setAccessVerificationClaim(AUT);
        mediator.setAccessVerificationClaimValue(APPLICATION);
        mediator.setAccessVerificationClaimValueRegex(claimValueRegex);
        mediator.setShouldAllowValidation(true);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenClaimsMatchingWithTickFalseWithoutRegex() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        String claimValueRegex = "";
        mediator.setAccessVerificationClaim(AUT);
        mediator.setAccessVerificationClaimValue(APPLICATION);
        mediator.setAccessVerificationClaimValueRegex(claimValueRegex);
        mediator.setShouldAllowValidation(false);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenClaimsNotMatchingWithTickTrueWithoutRegex() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        String claimValueRegex = "";
        mediator.setAccessVerificationClaim(AUT);
        mediator.setAccessVerificationClaimValue(APPLICATION_USER);
        mediator.setAccessVerificationClaimValueRegex(claimValueRegex);
        mediator.setShouldAllowValidation(true);
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenArrayClaimContainsConfiguredClaimWithoutRegex() throws Exception {

        Map<String, Object> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(ROLES, Arrays.asList(VALUE_1, VALUE_2));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        mediator.setAccessVerificationClaim(ROLES);
        mediator.setAccessVerificationClaimValue(VALUE_1);
        mediator.setAccessVerificationClaimValueRegex("");
        mediator.setShouldAllowValidation(false);

        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenArrayClaimDoesNotContainConfiguredClaimWithoutRegex() throws Exception {

        Map<String, Object> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(ROLES, Arrays.asList(VALUE_1, VALUE_2));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        mediator.setAccessVerificationClaim(ROLES);
        mediator.setAccessVerificationClaimValue(MISSING_VALUE);
        mediator.setAccessVerificationClaimValueRegex("");
        mediator.setShouldAllowValidation(false);

        Assert.assertFalse(mediator.mediate(messageContext));
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_CODE,
                APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_MISMATCH);
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_MISMATCH_MESSAGE);
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_DETAIL,
                APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_MISMATCH_DESCRIPTION);
        PowerMockito.verifyStatic(Utils.class);
        Utils.sendFault(Mockito.eq(messageContext), Mockito.eq(HttpStatus.SC_FORBIDDEN));
    }

    @Test
    public void testFlowWhenArrayClaimIsEmpty() throws Exception {

        Map<String, Object> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(ROLES, Collections.emptyList());
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        mediator.setAccessVerificationClaim(ROLES);
        mediator.setAccessVerificationClaimValue(VALUE_1);
        mediator.setAccessVerificationClaimValueRegex("");
        mediator.setShouldAllowValidation(false);

        Assert.assertFalse(mediator.mediate(messageContext));
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_CODE,
                APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_INVALID);
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_MESSAGE,
                APISecurityConstants.API_AUTH_ACCESS_TOKEN_CLAIMS_INVALID_MESSAGE);
        Mockito.verify(messageContext).setProperty(SynapseConstants.ERROR_DETAIL,
                String.format("Token doesn't contain the claim \"%s\"", ROLES));
        PowerMockito.verifyStatic(Utils.class);
        Utils.sendFault(Mockito.eq(messageContext), Mockito.eq(HttpStatus.SC_FORBIDDEN));
    }

    @Test
    public void testFlowWhenArrayClaimMatchesConfiguredRegex() throws Exception {

        Map<String, Object> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(ROLES, Arrays.asList("ADMIN", "VIEWER"));
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        mediator.setAccessVerificationClaim(ROLES);
        mediator.setAccessVerificationClaimValue("ADMIN");
        mediator.setAccessVerificationClaimValueRegex("^[A-Z]+$");
        mediator.setShouldAllowValidation(false);

        Assert.assertTrue(mediator.mediate(messageContext));
    }
}
