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

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationResponse;
import org.wso2.carbon.apimgt.gateway.utils.OpenAPIUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.BasicAuthValidationInfoDTO;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

import java.util.TreeMap;

@RunWith(PowerMockRunner.class)
@PrepareForTest({OpenAPIUtils.class, BasicAuthAuthenticator.class, BasicAuthCredentialValidator.class,
        ServiceReferenceHolder.class})
public class BasicAuthAuthenticatorTest {
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgCntxt;
    private BasicAuthAuthenticator basicAuthAuthenticator;
    private final String CUSTOM_AUTH_HEADER = "AUTH-HEADER";
    private final String UNLIMITED_THROTTLE_POLICY= "Unlimited";
    private APIManagerConfiguration apiManagerConfiguration;

    @Before
    public void setup() throws Exception {
        PowerMockito.mockStatic(OpenAPIUtils.class);
        PowerMockito.when(OpenAPIUtils.getResourceAuthenticationScheme(Mockito.any(), Mockito.any()))
                .thenReturn(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);

        axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        messageContext = new Axis2MessageContext(axis2MsgCntxt, null,null);
        messageContext.setProperty(APIMgtGatewayConstants.REQUEST_RECEIVED_TIME,"1506576365");
        messageContext.setProperty(APIMgtGatewayConstants.OPEN_API_OBJECT, Mockito.mock(OpenAPI.class));
        messageContext.setProperty(BasicAuthAuthenticator.PUBLISHER_TENANT_DOMAIN, "carbon.super");

        basicAuthAuthenticator = new BasicAuthAuthenticator(CUSTOM_AUTH_HEADER, true, UNLIMITED_THROTTLE_POLICY);
        BasicAuthCredentialValidator basicAuthCredentialValidator = Mockito.mock(BasicAuthCredentialValidator.class);
        BasicAuthValidationInfoDTO basicAuthValidationInfoDTO = new BasicAuthValidationInfoDTO();

        Mockito.when(basicAuthCredentialValidator.validate(Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(invocationOnMock -> {
                    Object argument1 = invocationOnMock.getArguments()[0];
                    Object argument2 = invocationOnMock.getArguments()[1];

                    if ((argument1.equals("test_username@carbon.super") || argument1
                            .equals("test_username_blocked@carbon.super")) && argument2.equals("test_password")) {
                        basicAuthValidationInfoDTO.setAuthenticated(true);
                        basicAuthValidationInfoDTO.setHashedPassword("hashed_test_password");
                        if ("test_username@carbon.super".equals(argument1)) {
                            basicAuthValidationInfoDTO.setDomainQualifiedUsername("test_username@carbon.super");
                        } else if ("test_username_blocked@carbon.super".equals(argument1)) {
                            basicAuthValidationInfoDTO.setDomainQualifiedUsername("test_username_blocked@carbon.super");
                        }
                        String[] userRoleList = { "roleQ", "roleX" };
                        basicAuthValidationInfoDTO.setUserRoleList(userRoleList);
                        return basicAuthValidationInfoDTO;
                    }
                    return basicAuthValidationInfoDTO;
                });

        Mockito.when(basicAuthCredentialValidator
                .validateScopes(Mockito.anyString(), Mockito.any(OpenAPI.class), Mockito.any(MessageContext.class),
                        Mockito.anyObject())).thenAnswer(invocationOnMock -> {
            Object argument = invocationOnMock.getArguments()[0];
            if (argument.equals("test_username@carbon.super")) {
                return true;
            } else if (argument.equals("test_username_blocked@carbon.super")) {
                throw new APISecurityException(APISecurityConstants.INVALID_SCOPE, "Scope validation failed");
            }
            return false;
        });
        PowerMockito.whenNew(BasicAuthCredentialValidator.class).withNoArguments().thenReturn(basicAuthCredentialValidator);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn("true");
    }

    @Test
    public void testAuthenticateWithoutBasicHeaderSegment() {
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(APIMgtGatewayConstants.AUTHORIZATION, "gsu64r874tcin7ry8oe");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        AuthenticationResponse authenticationResponse = basicAuthAuthenticator.authenticate(messageContext);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
        Assert.assertEquals(authenticationResponse.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_CREDENTIALS);
    }

    @Test
    public void testAuthenticateWithoutAuthorizationHeader() {
        TreeMap transportHeaders = new TreeMap();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        AuthenticationResponse authenticationResponse = basicAuthAuthenticator.authenticate(messageContext);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
        Assert.assertEquals(authenticationResponse.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_CREDENTIALS);
    }
    
    @Test
    public void testAuthenticateWithInvalidBasicHeader_2() {
        TreeMap transportHeaders = new TreeMap();
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic eHh4eA=="); // encode64(xxxx)='eHh4eA=='
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        AuthenticationResponse authenticationResponse = basicAuthAuthenticator.authenticate(messageContext);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
        Assert.assertEquals(authenticationResponse.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
    }

    @Test
    public void testAuthenticateWithScopesInvalid() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username_blocked:test_password)='dGVzdF91c2VybmFtZV9ibG9ja2VkOnRlc3RfcGFzc3dvcmQ='
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic dGVzdF91c2VybmFtZV9ibG9ja2VkOnRlc3RfcGFzc3dvcmQ=");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        AuthenticationResponse authenticationResponse = basicAuthAuthenticator.authenticate(messageContext);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
        Assert.assertEquals(authenticationResponse.getErrorCode(), APISecurityConstants.INVALID_SCOPE);
    }

    @Test
    public void testAuthenticateWithValidBasicAuthCredentials() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username:test_password)='dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk'
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        Assert.assertTrue(basicAuthAuthenticator.authenticate(messageContext).isAuthenticated());
        transportHeaders = (TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Assert.assertNull(transportHeaders.get(CUSTOM_AUTH_HEADER));
        Assert.assertEquals(messageContext.getProperty(APIMgtGatewayConstants.END_USER_NAME),"test_username@carbon.super");
    }

    @Test
    public void testAuthenticateWithValidBasicAuthCredentialsWithInvalidHeader() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username:test_password)='dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk'
        // 'AUTH-HEADER: Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk' expected,
        // 'AUTHORIZATION: Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk' found
        transportHeaders.put(APIMgtGatewayConstants.AUTHORIZATION, "Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS)).thenReturn(transportHeaders);

        AuthenticationResponse authenticationResponse = basicAuthAuthenticator.authenticate(messageContext);
        Assert.assertFalse(authenticationResponse.isAuthenticated());
        Assert.assertEquals(authenticationResponse.getErrorCode(), APISecurityConstants.API_AUTH_MISSING_CREDENTIALS);
    }

    @Test
    public void testAuthenticateWithRemoveOAuthHeadersFromOutMessageSetToFalse() {
        TreeMap transportHeaders = new TreeMap();
        // encode64(test_username:test_password)='dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk'
        transportHeaders.put(CUSTOM_AUTH_HEADER, "Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk");
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(transportHeaders);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.REMOVE_OAUTH_HEADERS_FROM_MESSAGE))
                .thenReturn("false");

        Assert.assertTrue(basicAuthAuthenticator.authenticate(messageContext).isAuthenticated());
        transportHeaders =
                (TreeMap) axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Assert.assertNotNull(transportHeaders.get(CUSTOM_AUTH_HEADER));
        Assert.assertEquals(transportHeaders.get(CUSTOM_AUTH_HEADER), "Basic dGVzdF91c2VybmFtZTp0ZXN0X3Bhc3N3b3Jk");
    }
}