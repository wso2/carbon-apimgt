/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security.basicauth;


import io.swagger.models.Swagger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;

import java.util.TreeMap;

public class BasicAuthAuthenticatorTest {
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgCntxt;
    private BasicAuthAuthenticator basicAuthAuthenticator;
    private final String CUSTOM_AUTH_HEADER = "AUTH-HEADER";

    @Before
    public void setup() {
        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(APIMgtGatewayConstants.REQUEST_RECEIVED_TIME)).thenReturn("1506576365");
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);

        basicAuthAuthenticator = new BasicAuthAuthenticator(CUSTOM_AUTH_HEADER, null);
        basicAuthAuthenticator.setBasicAuthCredentialValidator(new BasicAuthCredentialValidator() {
            @Override
            public boolean validate(String username, String password) {
                if ((username.equals("test_username") || username.equals("test_username_blocked")) && password.equals("test_password")) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean validateScopes(String username, Swagger swagger, MessageContext synCtx) throws APISecurityException {
                if (username.equals("test_username")) {
                    return true;
                } else if (username.equals("test_username_blocked")) {
                    throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
                }
                return false;
            }
        });
    }

    @Test
    public void testAuthenticateWithoutBasicHeaderSegment() {
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.AUTHORIZATION, "gsu64r874tcin7ry8oe");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            basicAuthAuthenticator.authenticate(messageContext);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_BASIC_AUTH_CREDENTIALS);
        }
    }

    @Test
    public void testAuthenticateWithoutAuthorizationHeader() {
        TreeMap transportHeaders = new TreeMap();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            basicAuthAuthenticator.authenticate(messageContext);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_BASIC_AUTH_CREDENTIALS);
        }
    }

    @Test
    public void testAuthenticateWithInvalidBasicHeader_1() {
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic xxxxxxx"); //Throw Decode64 exception
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            basicAuthAuthenticator.authenticate(messageContext);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_BASIC_AUTH_CREDENTIALS);
        }
    }

    @Test
    public void testAuthenticateWithInvalidBasicHeader_2() {
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic eHh4eA=="); // encode64(xxxx)='eHh4eA=='
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            basicAuthAuthenticator.authenticate(messageContext);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_BASIC_AUTH_CREDENTIALS);
        }
    }

    @Test
    public void testAuthenticateWithScopesInvalid() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username_blocked:test_password)='dGVzdF91c2VybmFtZV9ibG9ja2VkOnRlc3RfcGFzc3dvcmQ='
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic dGVzdF91c2VybmFtZV9ibG9ja2VkOnRlc3RfcGFzc3dvcmQ=");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            basicAuthAuthenticator.authenticate(messageContext);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.INVALID_SCOPE);
        }
    }

    @Test
    public void testAuthenticateWithValidBasicAuthCredentials() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username:test_password)='dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk'
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            Assert.assertTrue(basicAuthAuthenticator.authenticate(messageContext));
        } catch (APISecurityException e) {
            Assert.fail();
        }
    }

    @Test
    public void testAuthenticateWithValidBasicAuthCredentialsWithInvalidHeader() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username:test_password)='dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk'
        // 'AUTH-HEADER: Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk' expected,
        // 'AUTHORIZATION: Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk' found
        transportHeaders.put(APIMgtGatewayConstants.AUTHORIZATION, "Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        try {
            basicAuthAuthenticator.authenticate(messageContext);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_BASIC_AUTH_CREDENTIALS);
        }
    }
}