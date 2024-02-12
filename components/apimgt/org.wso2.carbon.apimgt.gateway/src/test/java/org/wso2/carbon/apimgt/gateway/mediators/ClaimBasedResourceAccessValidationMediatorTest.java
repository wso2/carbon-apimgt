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

import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Test;
import org.mockito.Mockito;
import org.testng.Assert;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;

import java.util.HashMap;
import java.util.Map;

public class ClaimBasedResourceAccessValidationMediatorTest {

    MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
    private final ClaimBasedResourceAccessValidationMediator mediator
            = Mockito.spy(new ClaimBasedResourceAccessValidationMediator());
    public static final String APPLICATION = "APPLICATION";
    public static final String APPLICATION_USER = "APPLICATION_USER";
    public static final String AUT = "aut";

    @Test
    public void testClientCredentialsFlowWhenClientCredentialsPolicyIsApplied() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ACCESS_GRANT_CLAIM_NAME))
                .thenReturn(AUT);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ACCESS_GRANT_CLAIM_VALUE))
                .thenReturn(APPLICATION);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.SHOULD_ALLOW_ACCESS_VALIDATION))
                .thenReturn("true");
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testAuthCodeFlowWhenAuthCodePolicyIsApplied() throws Exception {

        Map<String, String> jwtTokenClaimsMap = new HashMap<>();
        jwtTokenClaimsMap.put(AUT, APPLICATION_USER);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.JWT_CLAIMS))
                .thenReturn(jwtTokenClaimsMap);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ACCESS_GRANT_CLAIM_NAME))
                .thenReturn(AUT);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ACCESS_GRANT_CLAIM_VALUE))
                .thenReturn(APPLICATION_USER);
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.SHOULD_ALLOW_ACCESS_VALIDATION))
                .thenReturn("true");
        Assert.assertTrue(mediator.mediate(messageContext));
    }

    @Test
    public void testFlowWhenValidationIsNotAllowed() throws Exception {

        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.SHOULD_ALLOW_ACCESS_VALIDATION))
                .thenReturn("false");
        Assert.assertTrue(mediator.mediate(messageContext));
    }
}
