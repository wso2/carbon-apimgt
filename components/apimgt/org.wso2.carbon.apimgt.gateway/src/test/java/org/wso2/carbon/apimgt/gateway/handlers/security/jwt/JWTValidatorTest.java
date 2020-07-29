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

package org.wso2.carbon.apimgt.gateway.handlers.security.jwt;

import com.nimbusds.jwt.SignedJWT;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.stub.APIGatewayAdmin;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.JWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.cache.Cache;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTValidator.class, GatewayUtils.class, MultitenantUtils.class, PrivilegedCarbonContext.class})
public class JWTValidatorTest {

    PrivilegedCarbonContext privilegedCarbonContext;

    @Before
    public void setup() {

        System.setProperty("carbon.home", "");
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
    }

    @Test
    public void testJWTValidator() throws ParseException, APISecurityException, APIManagementException, IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5000L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        Mockito.when(gatewayKeyCache.get(cacheKey)).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWT, "Default");
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
    }

    @Test
    public void testJWTValidatorExpiredInCache() throws ParseException, APISecurityException, APIManagementException,
            IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis() - 100);
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis());
        Mockito.when(gatewayKeyCache.get(cacheKey)).thenReturn(jwtValidationInfo);
        try {
            authenticate = jwtValidator.authenticate(signedJWT, messageContext);

        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWT, "Default");
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
        Mockito.verify(invalidTokenCache, Mockito.times(1)).put(signedJWT.getJWTClaimsSet().getJWTID(), "carbon.super");
    }

    @Test
    public void testJWTValidatorExpiredInCacheTenant() throws ParseException, APISecurityException,
            APIManagementException,
            IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("abc.com");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("abc.com");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis() - 100);
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis());
        Mockito.when(gatewayKeyCache.get(cacheKey)).thenReturn(jwtValidationInfo);
        try {
            authenticate = jwtValidator.authenticate(signedJWT, messageContext);

        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWT, "Default");
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
        Mockito.verify(invalidTokenCache, Mockito.times(1)).put(signedJWT.getJWTClaimsSet().getJWTID(), "abc.com");
    }

    @Test
    public void testJWTValidatorTenant() throws ParseException, APISecurityException, APIManagementException,
            IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("abc.com");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5000L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        Mockito.when(gatewayKeyCache.get(cacheKey)).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWT, "Default");
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
    }

    @Test
    public void testJWTValidatorInvalid() throws ParseException, APIManagementException,
            IOException, APISecurityException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("abc.com");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(false);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        try {
            AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
            Assert.fail("JWT get Authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.when(invalidTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        try {
            jwtValidator.authenticate(signedJWT, messageContext);
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(apiKeyValidator, Mockito.never())
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
        Mockito.verify(gatewayKeyCache, Mockito.never()).get(cacheKey);
    }

    @Test
    public void testJWTValidatorInvalidConsumerKey() throws ParseException, APIManagementException,
            IOException, APISecurityException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setAuthorized(false);
        apiKeyValidationInfoDTO.setValidationStatus(
                APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()
                , Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        try {
            jwtValidator.authenticate(signedJWT, messageContext);
            Assert.fail("JWT get Authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_FORBIDDEN);
        }
    }

    @Test
    public void testJWTValidatorTempered() throws ParseException, APISecurityException, APIManagementException,
            IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOi JSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        SignedJWT tempered =
                SignedJWT
                        .parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1TW1abU9EZ3dNVE" +
                                "UzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0.ewogICAiYXVkIjoiaHR0cDpcL1wvb3JnLndzbzIuYXBpbWd" +
                                "0XC9nYXRld2F5IiwKICAgInN1YiI6ImFkbWluMUBjYXJib24uc3VwZXIiLAogICAic2NvcGUiOiJhbV9hcHBsa" +
                                "WNhdGlvbl9zY29wZSBkZWZhdWx0IiwKICAgImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dG" +
                                "gyXC90b2tlbiIsCiAgICJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLAogICA" +
                                "iZXhwIjoxNTkwMzQyMzEzLAogICAiaWF0IjoxNTkwMzM4NzEzLAogICAianRpIjoiYjg5Mzg3NjgtMjNmZC00" +
                                "ZGVjLThiNzAtYmVkNDVlYjdjMzNkIgp9.sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfFCEFVQS1U3oY9-cqP" +
                                "wAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVk" +
                                "JUMTwZ8-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4K" +
                                "QX2VaZPBzN594c9TGriiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_-N6sqf" +
                                "xLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGpEuSe9w");

        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5000L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidationInfo temperedJWTValidationInfo = new JWTValidationInfo();
        temperedJWTValidationInfo.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        temperedJWTValidationInfo.setValid(false);
        Mockito.when(jwtValidationService.validateJWTToken(tempered, "Default")).thenReturn(temperedJWTValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        Mockito.when(gatewayKeyCache.get(cacheKey)).thenReturn(jwtValidationInfo);
        try {
            authenticate = jwtValidator.authenticate(tempered, messageContext);
            Assert.fail("tempered JWT get authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(jwtValidationService, Mockito.atLeast(1)).validateJWTToken(Mockito.any(SignedJWT.class),
                Mockito.anyString());
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
        Mockito.when(invalidTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        try {
            authenticate = jwtValidator.authenticate(tempered, messageContext);
            Assert.fail("tempered JWT get authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
    }

    @Test
    public void testJWTValidatorTemperedTenant() throws ParseException, APISecurityException, APIManagementException,
            IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("abc.com");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOi JSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".eyJhdWQiOiJodHRwOlwvXC9vcmcud3NvMi5hcGltZ3RcL2dhdGV" +
                        "3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZ" +
                        "SI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQ" +
                        "iOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0" +
                        "NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6e30sImtleXR5cGUiOiJQUk9EVUNUSU9OIiwic3Vic2NyaWJlZEFQSXMiOltdL" +
                        "CJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLCJleHAiOjE1OTAzNDIzMTMsImlhdCI6MTU5MDMzO" +
                        "DcxMywianRpIjoiYjg5Mzg3NjgtMjNmZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIn0" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");
        SignedJWT tempered =
                SignedJWT
                        .parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1TW1abU9EZ3dNVE" +
                                "UzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0.ewogICAiYXVkIjoiaHR0cDpcL1wvb3JnLndzbzIuYXBpbWd" +
                                "0XC9nYXRld2F5IiwKICAgInN1YiI6ImFkbWluMUBjYXJib24uc3VwZXIiLAogICAic2NvcGUiOiJhbV9hcHBsa" +
                                "WNhdGlvbl9zY29wZSBkZWZhdWx0IiwKICAgImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dG" +
                                "gyXC90b2tlbiIsCiAgICJjb25zdW1lcktleSI6IlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2EiLAogICA" +
                                "iZXhwIjoxNTkwMzQyMzEzLAogICAiaWF0IjoxNTkwMzM4NzEzLAogICAianRpIjoiYjg5Mzg3NjgtMjNmZC00" +
                                "ZGVjLThiNzAtYmVkNDVlYjdjMzNkIgp9.sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfFCEFVQS1U3oY9-cqP" +
                                "wAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVk" +
                                "JUMTwZ8-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4K" +
                                "QX2VaZPBzN594c9TGriiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_-N6sqf" +
                                "xLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGpEuSe9w");


        JWTConfigurationDto jwtConfigurationDto = new JWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(signedJWT.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5000L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        Mockito.when(jwtValidationService.validateJWTToken(signedJWT, "Default")).thenReturn(jwtValidationInfo);
        JWTValidationInfo temperedJWTValidationInfo = new JWTValidationInfo();
        temperedJWTValidationInfo.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        temperedJWTValidationInfo.setValid(false);
        Mockito.when(jwtValidationService.validateJWTToken(tempered, "Default")).thenReturn(temperedJWTValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        Mockito.when(messageContext.getProperty(APIMgtGatewayConstants.ELECTED_KEY_MANAGER)).thenReturn("Default");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        OpenAPIParser parser = new OpenAPIParser();
        String swagger = IOUtils.toString(this.getClass().getResourceAsStream("/swaggerEntry/openapi.json"));
        OpenAPI openAPI = parser.readContents(swagger, null, null).getOpenAPI();
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWT, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("abc.com");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        Mockito.when(gatewayKeyCache.get(cacheKey)).thenReturn(jwtValidationInfo);
        try {
            jwtValidator.authenticate(tempered, messageContext);
            Assert.fail("tempered JWT get authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(jwtValidationService, Mockito.atLeast(2)).validateJWTToken(Mockito.any(SignedJWT.class),
                Mockito.anyString());
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(2)).get(signedJWT.getJWTClaimsSet().getJWTID());
        Mockito.when(invalidTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("abc.com");
        try {
            jwtValidator.authenticate(tempered, messageContext);
            Assert.fail("tempered JWT get authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
    }

}
