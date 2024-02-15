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
import org.wso2.carbon.apimgt.common.gateway.dto.JWTValidationInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APIKeyValidator;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.ExtendedJWTConfigurationDto;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.jwt.SignedJWTInfo;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.keymgt.service.TokenValidationContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.text.ParseException;
import java.util.*;
import javax.cache.Cache;
import java.security.cert.X509Certificate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTValidator.class, GatewayUtils.class, MultitenantUtils.class, PrivilegedCarbonContext.class,
        ServiceReferenceHolder.class, CertificateMgtUtils.class, RevokedJWTDataHolder.class})
public class JWTValidatorTest {

    private static String PASSWORD = "wso2carbon";
    PrivilegedCarbonContext privilegedCarbonContext;
    ServiceReferenceHolder serviceReferenceHolder;

    @Before
    public void setup() {

        System.setProperty("carbon.home", "");
        PowerMockito.mockStatic(MultitenantUtils.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(RevokedJWTDataHolder.class);
        PowerMockito.when(RevokedJWTDataHolder.getInstance())
                .thenReturn(PowerMockito.mock(RevokedJWTDataHolder.class));
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        System.setProperty("javax.net.ssl.trustStorePassword", PASSWORD);
        PowerMockito.mockStatic(CertificateMgtUtils.class);
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
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet().getJWTID());
    }

    @Test
    public void testJWTValidatorForNonJTIScenario() throws ParseException, APISecurityException, APIManagementException,
            IOException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT
                        .parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9" +
                                ".eyJpc3MiOiJodHRwczovL2xvY2FsaG9zdCIsImlhdCI6MTU5OTU0ODE3NCwiZXhwIjoxNjMxMDg0MTc0LC" +
                                "JhdWQiOiJ3d3cuZXhhbXBsZS5jb20iLCJzdWIiOiJqcm9ja2V0QGV4YW1wbGUuY29tIiwiR2l2ZW5OYW1l" +
                                "IjoiSm9obm55IiwiU3VybmFtZSI6IlJvY2tldCIsIkVtYWlsIjoianJvY2tldEBleGFtcGxlLmNvbSIsIl" +
                                "JvbGUiOlsiTWFuYWdlciIsIlByb2plY3QgQWRtaW5pc3RyYXRvciJdfQ.SSQyg_VTxF5drIogztn2SyEK" +
                                "2wRE07wG6OW3tufD3vo");
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getSignature().toString())).thenReturn("carbon.super");
        Mockito.when(gatewayKeyCache.get(signedJWT.getSignature().toString())).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getSignature().toString());
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
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis() - 100);
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis());
        Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        try {
            authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);

        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet().getJWTID());
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
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("abc.com");
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis() - 100);
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis());
        Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        try {
            authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);

        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet().getJWTID());
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet().getJWTID());
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
            AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
            Assert.fail("JWT get Authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.when(invalidTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        String cacheKey = GatewayUtils
                .getAccessTokenCacheKey(signedJWT.getJWTClaimsSet().getJWTID(), "/api1", "1.0", "/pet/findByStatus",
                        "GET");
        try {
            jwtValidator.authenticate(signedJWTInfo, messageContext);
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }
        Mockito.verify(apiKeyValidator, Mockito.never())
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet().getJWTID());
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
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
            jwtValidator.authenticate(signedJWTInfo, messageContext);
            Assert.fail("JWT get Authenticated");
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_FORBIDDEN);
        }
    }


    @Test
    public void testJWTValidatorForTamperedPayload() throws ParseException, APISecurityException, APIManagementException, IOException {

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

        SignedJWT signedJWTTampered =
                SignedJWT.parse("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6Ik5UZG1aak00WkRrM05qWTBZemM1T" +
                        "W1abU9EZ3dNVEUzTVdZd05ERTVNV1JsWkRnNE56YzRaQT09In0" +
                        ".ewogICJhdWQiOiAiaHR0cDovL29yZy53c28yLmFwaW1ndC9nYXRld2F5IiwKICAic3ViIjogImFkbWluQGNhcm" +
                        "Jvbi5zdXBlciIsCiAgImFwcGxpY2F0aW9uIjogewogICAgIm93bmVyIjogImFkbWluIiwKICAgICJ0aWVyUXVvd" +
                        "GFUeXBlIjogInJlcXVlc3RDb3VudCIsCiAgICAidGllciI6ICJVbmxpbWl0ZWQiLAogICAgIm5hbWUiOiAiRGVm" +
                        "YXVsdEFwcGxpY2F0aW9uMiIsCiAgICAiaWQiOiAyLAogICAgInV1aWQiOiBudWxsCiAgfSwKICAic2NvcGUiOiA" +
                        "iYW1fYXBwbGljYXRpb25fc2NvcGUgZGVmYXVsdCIsCiAgImlzcyI6ICJodHRwczovL2xvY2FsaG9zdDo5NDQzL2" +
                        "9hdXRoMi90b2tlbiIsCiAgInRpZXJJbmZvIjoge30sCiAgImtleXR5cGUiOiAiUFJPRFVDVElPTiIsCiAgInN1Y" +
                        "nNjcmliZWRBUElzIjogW10sCiAgImNvbnN1bWVyS2V5IjogIlhnTzM5NklIRks3ZUZZeWRycVFlNEhLR3oxa2Ei" +
                        "LAogICJleHAiOiAxNTkwMzQyMzEzLAogICJpYXQiOiAxNTkwMzM4NzEzLAogICJqdGkiOiAiYjg5Mzg3NjgtMjN" +
                        "mZC00ZGVjLThiNzAtYmVkNDVlYjdjMzNkIgp9" +
                        ".sBgeoqJn0log5EZflj_G7ADvm6B3KQ9bdfF" +
                        "CEFVQS1U3oY9" +
                        "-cqPwAPyOLLh95pdfjYjakkf1UtjPZjeIupwXnzg0SffIc704RoVlZocAx9Ns2XihjU6Imx2MbXq9ARmQxQkyGVkJ" +
                        "UMTwZ8" +
                        "-SfOnprfrhX2cMQQS8m2Lp7hcsvWFRGKxAKIeyUrbY4ihRIA5vOUrMBWYUx9Di1N7qdKA4S3e8O4KQX2VaZPBzN594c9TG" +
                        "riiH8AuuqnrftfvidSnlRLaFJmko8-QZo8jDepwacaFhtcaPVVJFG4uYP-_" +
                        "-N6sqfxLw3haazPN0_xU0T1zJLPRLC5HPfZMJDMGp" +
                        "EuSe9w");

        testTamperedTokens(signedJWT, signedJWTTampered);
    }


    @Test
    public void testJWTValidatorForTamperedSignature() throws ParseException, APISecurityException, APIManagementException, IOException {

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

        SignedJWT signedJWTTampered =
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
                        "XXXXX");

        testTamperedTokens(signedJWT, signedJWTTampered);
    }

    private void testTamperedTokens(SignedJWT originalToken, SignedJWT tamperedToken) throws ParseException, APIManagementException, APISecurityException {

        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidationInfo jwtValidationInfo = new JWTValidationInfo();
        jwtValidationInfo.setValid(true);
        jwtValidationInfo.setIssuer("https://localhost");
        jwtValidationInfo.setRawPayload(originalToken.getParsedString());
        jwtValidationInfo.setJti(UUID.randomUUID().toString());
        jwtValidationInfo.setIssuedTime(System.currentTimeMillis());
        jwtValidationInfo.setExpiryTime(System.currentTimeMillis() + 5000000L);
        jwtValidationInfo.setConsumerKey(UUID.randomUUID().toString());
        jwtValidationInfo.setUser("user1");
        jwtValidationInfo.setKeyManager("Default");
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(originalToken.getParsedString(), originalToken,
                originalToken.getJWTClaimsSet());
        SignedJWTInfo signedJWTInfoTampered = new SignedJWTInfo(tamperedToken.getParsedString(), tamperedToken,
                tamperedToken.getJWTClaimsSet());
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
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
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(originalToken.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        Mockito.when(gatewayKeyCache.get(originalToken.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());

        JWTValidationInfo jwtValidationInfoInvalid = new JWTValidationInfo();
        jwtValidationInfoInvalid.setValid(false);
        jwtValidationInfoInvalid.setValidationCode(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfoTampered)).thenReturn(jwtValidationInfoInvalid);
        try {
            jwtValidator.authenticate(signedJWTInfoTampered, messageContext);
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
        }

        Mockito.verify(jwtValidationService).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(originalToken.getJWTClaimsSet().getJWTID());
    }


    @Test
    public void testAuthenticateForGraphQLSubscription() throws Exception {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT = SignedJWT.parse("eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFp" +
                "UQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16WXhNbUZrT0" +
                "dZd01XSTBaV05tTkRjeE5HWXdZbU00WlRBM01XSTJOREF6WkdRek5HTTBaR1JsTmpKa09ERmtaRFJpT1RGa01XRmhNelUyW" +
                "kdWbE5nX1JTMjU2IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbiIsImF1dCI6IkFQUExJQ0FUSU9OIiwiYXVkIjoidT" +
                "ljaTNDRmRRUDZJNG9DNU84VFcwZklBRXRnYSIsIm5iZiI6MTYzNjkxNTk4OCwiYXpwIjoidTljaTNDRmRRUDZJNG9DNU84VFc" +
                "wZklBRXRnYSIsInNjb3BlIjoic2NvcGUxIiwiaXNzIjoiaHR0cHM6XC9cL2xvY2FsaG9zdDo5NDQzXC9vYXV0aDJcL3Rva2Vu" +
                "IiwiZXhwIjoxNjM2OTE5NTg4LCJpYXQiOjE2MzY5MTU5ODgsImp0aSI6IjJiM2FmYTkxLTBjNDItNGUzNC1iYTliLTc3ZmVkND" +
                "dkMGNmZCJ9.J8VkCSDUMCUNdJrpbRJy_cj5YazIrdRyNKTJ-9Lv1EabUgwENX1XQcUioSqF686ESI_PvUxYZIwViybVIIGVRuxM" +
                "Tp9vCMQDWhxXPCuehahul7Ebn0mQtrM7K2fwL0DpyKpI0ER_UYH-PgNvnHS0f3zmJdUBNao2QwuWorXMuwzSw3oPcdHcYmF9" +
                "Jn024J8Dv3ipHtzEgSc26ULVRaO9bDzJZochzQzqdkxjLMDMBYmKizXOCXEcXJYrEnQpTRHQGOuRN9stXePvO9_gFGVTenun" +
                "9pBT7Yw7D3Sd-qg-r_AnExOjQu8QwZRjTh_l09YwBYIrMdhSbtXpeAy0GNrc0w");
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        String apiContext = "/graphql";
        String apiVersion = "1.0.0";

        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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
        jwtValidationInfo.setKeyManager("Resident Key Manager");
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("GraphQLAPI");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        apiKeyValidationInfoDTO.setGraphQLMaxDepth(3);
        apiKeyValidationInfoDTO.setGraphQLMaxComplexity(4);
        // testing happy path
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator
                .authenticateForWebSocket(signedJWTInfo, apiContext, apiVersion, null, false);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "GraphQLAPI");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Assert.assertEquals(authenticate.getRequestTokenScopes(), jwtValidationInfo.getScopes());
        Assert.assertEquals(authenticate.getGraphQLMaxComplexity(), apiKeyValidationInfoDTO.getGraphQLMaxComplexity());
        Assert.assertEquals(authenticate.getGraphQLMaxDepth(), apiKeyValidationInfoDTO.getGraphQLMaxDepth());
        //testing token validation failure
        jwtValidationInfo.setValid(false);
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
        APISecurityException apiSecurityException = null;
        try {
            jwtValidator.authenticateForWebSocket(signedJWTInfo, apiContext, apiVersion, null, false);
        } catch (APISecurityException exception) {
            apiSecurityException = exception;
            Assert.assertEquals(exception.getErrorCode(), APISecurityConstants.API_AUTH_INVALID_CREDENTIALS);
            Assert.assertEquals(exception.getMessage(), "Invalid JWT token");
        }
        if (apiSecurityException == null) {
            Assert.fail();
        }
        //testing subscription validation failure
        jwtValidationInfo.setValid(true);
        apiKeyValidationInfoDTO.setAuthorized(false);
        apiKeyValidationInfoDTO.setValidationStatus(APIConstants.KeyValidationStatus.API_AUTH_RESOURCE_FORBIDDEN);
        try {
            jwtValidator.authenticateForWebSocket(signedJWTInfo, apiContext, apiVersion, null, false);
        } catch (APISecurityException exception) {
            Assert.assertEquals(exception.getErrorCode(), apiKeyValidationInfoDTO.getValidationStatus());
            Assert.assertEquals(exception.getMessage(),
                    "User is NOT authorized to access the Resource. API Subscription validation failed.");
        }
    }

    @Test
    public void testValidateScopesForGraphQLSubscriptions() throws ParseException {
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        String apiContext = "/graphql";
        String apiVersion = "1.0.0";
        String matchingResource = "/subresource";
        SignedJWT signedJWT = SignedJWT.parse("eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFp" +
                "UQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16WXhNbUZrT0" +
                "dZd01XSTBaV05tTkRjeE5HWXdZbU00WlRBM01XSTJOREF6WkdRek5HTTBaR1JsTmpKa09ERmtaRFJpT1RGa01XRmhNelUyW" +
                "kdWbE5nX1JTMjU2IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbiIsImF1dCI6IkFQUExJQ0FUSU9OIiwiYXVkIjoidT" +
                "ljaTNDRmRRUDZJNG9DNU84VFcwZklBRXRnYSIsIm5iZiI6MTYzNjkxNTk4OCwiYXpwIjoidTljaTNDRmRRUDZJNG9DNU84VFc" +
                "wZklBRXRnYSIsInNjb3BlIjoic2NvcGUxIiwiaXNzIjoiaHR0cHM6XC9cL2xvY2FsaG9zdDo5NDQzXC9vYXV0aDJcL3Rva2Vu" +
                "IiwiZXhwIjoxNjM2OTE5NTg4LCJpYXQiOjE2MzY5MTU5ODgsImp0aSI6IjJiM2FmYTkxLTBjNDItNGUzNC1iYTliLTc3ZmVkND" +
                "dkMGNmZCJ9.J8VkCSDUMCUNdJrpbRJy_cj5YazIrdRyNKTJ-9Lv1EabUgwENX1XQcUioSqF686ESI_PvUxYZIwViybVIIGVRuxM" +
                "Tp9vCMQDWhxXPCuehahul7Ebn0mQtrM7K2fwL0DpyKpI0ER_UYH-PgNvnHS0f3zmJdUBNao2QwuWorXMuwzSw3oPcdHcYmF9" +
                "Jn024J8Dv3ipHtzEgSc26ULVRaO9bDzJZochzQzqdkxjLMDMBYmKizXOCXEcXJYrEnQpTRHQGOuRN9stXePvO9_gFGVTenun" +
                "9pBT7Yw7D3Sd-qg-r_AnExOjQu8QwZRjTh_l09YwBYIrMdhSbtXpeAy0GNrc0w");
        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        AuthenticationContext authenticationContext = new AuthenticationContext();
        authenticationContext.setRequestTokenScopes(new ArrayList<String>() {
            {
                add("scope1");
            }
        });
        APIKeyValidator apiKeyValidator = Mockito.mock(APIKeyValidator.class);
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        Cache gatewayTokenCache = Mockito.mock(Cache.class);
        Cache invalidTokenCache = Mockito.mock(Cache.class);
        Cache gatewayKeyCache = Mockito.mock(Cache.class);
        Cache gatewayJWTTokenCache = Mockito.mock(Cache.class);
        JWTValidatorWrapper jwtValidator
                = new JWTValidatorWrapper("Unlimited", true, apiKeyValidator, false, null, jwtConfigurationDto,
                jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache, gatewayJWTTokenCache);
        try {
            Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class),
                    Mockito.anyString())).thenReturn(true);
            jwtValidator.validateScopesForGraphQLSubscriptions(apiContext, apiVersion, matchingResource,
                    signedJWTInfo, authenticationContext);
        } catch (APISecurityException e) {
            Assert.fail();
        }
        try {
            Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class),
                    Mockito.anyString())).thenReturn(false);
            jwtValidator.validateScopesForGraphQLSubscriptions(apiContext, apiVersion, matchingResource,
                    signedJWTInfo, authenticationContext);
        } catch (APISecurityException e) {
            Assert.assertEquals(e.getErrorCode(), APISecurityConstants.INVALID_SCOPE);
            String message = "User is NOT authorized to access the Resource: " + matchingResource
                    + ". Scope validation failed.";
            Assert.assertEquals(e.getMessage(), message);
        }
    }

    @Test
    public void testJWTValidatorCnfValidation() throws ParseException, APISecurityException,
            APIManagementException, IOException, CertificateException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ4NXQiOiJOVGRtWmpNNFpEazNOalkwWXpjNU1tWm1PRGd3TVRFM01XWXdOREU1TVdSbFpEZzROemM0WkE" +
                        "iLCJraWQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSm" +
                        "tPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZ19QUzI1NiIsImFsZyI6IlBTMjU2In0.eyJhdWQiOiJodHRwOi8vb3JnL" +
                        "ndzbzIuYXBpbWd0L2dhdGV3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25l" +
                        "ciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjo" +
                        "iRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQiOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3" +
                        "BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzL29hdXRoMi90b2tlbiIsImNuZiI6eyJ4NXQjU" +
                        "zI1NiI6IlQyUHJpLXYxTm50TEN3a25TazQ2SjdoeFBPYmFENHgtQm5IQzVYYVRZUlEifSwidGllckluZm8iOnt9LCJr" +
                        "ZXl0eXBlIjoiUFJPRFVDVElPTiIsInN1YnNjcmliZWRBUElzIjpbXSwiY29uc3VtZXJLZXkiOiJYZ08zOTZJSEZLN2V" +
                        "GWXlkcnFRZTRIS0d6MWthIiwiZXhwIjoxNTkwMzQyMzEzLCJpYXQiOjE1OTAzMzg3MTMsImp0aSI6ImI4OTM4NzY4LT" +
                        "IzZmQtNGRlYy04YjcwLWJlZDQ1ZWI3YzMzZCJ9.R7ARkdO5evfAyg_lQmTCWXaP5zSA26XnQMI8QHgID72xx1YCBA1x" +
                        "xQeR4Q0EDgGUna3yePhLOiYIMSVJf_VDIMjIW2pFAH1i0ETWivYrRzNpBIZC0UjPiJ-Xjw02T2Omkywgf12ff_T3nz9" +
                        "eLZqP5SQkO01gihECzXYDFD3dQDRc63bjvmK_en5my5Rt-rUYPihl0uBaGXueHTYp-zNNN8aU_k7FEZTfs-UfOw4Fhh" +
                        "noXUTclbEbABbyjDiWgyeVCVwAeGHXIMAAqVM6qKZqGRFSW-wYuJ9UCz2x4QnXLoNXkNST0ly_rbDdqEeNWed8iLTOQ" +
                        "yry3tbDvkCGBjW_9w");
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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

        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        PowerMockito.mockStatic(CertificateMgtUtils.class);
        java.security.cert.X509Certificate x509Certificate = getX509Certificate("src/test/resources/cnf/certificate.pem");
        java.security.cert.CertificateFactory certificateFactory
                = java.security.cert.CertificateFactory.getInstance("X.509");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(x509Certificate.getEncoded());
        Optional<X509Certificate> x509CertificateJava =
                Optional.of((java.security.cert.X509Certificate) certificateFactory.generateCertificate(
                        byteArrayInputStream));
        PowerMockito.when(CertificateMgtUtils.convert(x509Certificate)).thenReturn(x509CertificateJava);
        signedJWTInfo
                .setClientCertificate(getX509Certificate("src/test/resources/cnf/certificate.pem"));

        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator = new JWTValidatorWrapper("Unlimited",
                true, apiKeyValidator, false, null,
                jwtConfigurationDto, jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache,
                gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(axis2MsgCntxt.getProperty(APIMgtGatewayConstants.VALIDATED_X509_CERT)).thenReturn(x509Certificate);
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());

        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet()
                .getJWTID());
    }

    @Test
    public void testJWTValidatorCnfValidationInvalidCertificate() throws ParseException,
            APIManagementException, IOException, CertificateException, javax.security.cert.CertificateException {
        try {
            Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
            SignedJWT signedJWT =
                    SignedJWT.parse("eyJ4NXQiOiJOVGRtWmpNNFpEazNOalkwWXpjNU1tWm1PRGd3TVRFM01XWXdOREU1TVdSbFpEZzROemM" +
                            "0WkEiLCJraWQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHUXpOR00wWk" +
                            "dSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZ19QUzI1NiIsImFsZyI6IlBTMjU2In0.eyJhdWQiOiJod" +
                            "HRwOi8vb3JnLndzbzIuYXBpbWd0L2dhdGV3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNh" +
                            "dGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmx" +
                            "pbWl0ZWQiLCJuYW1lIjoiRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQiOm51bGx9LCJzY29wZSI6Im" +
                            "FtX2FwcGxpY2F0aW9uX3Njb3BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzL29hdXRoM" +
                            "i90b2tlbiIsImNuZiI6eyJ4NXQjUzI1NiI6IlQyUHJpLXYxTm50TEN3a25TazQ2SjdoeFBPYmFENHgtQm5IQzVY" +
                            "YVRZUlEifSwidGllckluZm8iOnt9LCJrZXl0eXBlIjoiUFJPRFVDVElPTiIsInN1YnNjcmliZWRBUElzIjpbXSw" +
                            "iY29uc3VtZXJLZXkiOiJYZ08zOTZJSEZLN2VGWXlkcnFRZTRIS0d6MWthIiwiZXhwIjoxNTkwMzQyMzEzLCJpYX" +
                            "QiOjE1OTAzMzg3MTMsImp0aSI6ImI4OTM4NzY4LTIzZmQtNGRlYy04YjcwLWJlZDQ1ZWI3YzMzZCJ9.R7ARkdO5" +
                            "evfAyg_lQmTCWXaP5zSA26XnQMI8QHgID72xx1YCBA1xxQeR4Q0EDgGUna3yePhLOiYIMSVJf_VDIMjIW2pFAH1" +
                            "i0ETWivYrRzNpBIZC0UjPiJ-Xjw02T2Omkywgf12ff_T3nz9eLZqP5SQkO01gihECzXYDFD3dQDRc63bjvmK_en" +
                            "5my5Rt-rUYPihl0uBaGXueHTYp-zNNN8aU_k7FEZTfs-UfOw4FhhnoXUTclbEbABbyjDiWgyeVCVwAeGHXIMAAq" +
                            "VM6qKZqGRFSW-wYuJ9UCz2x4QnXLoNXkNST0ly_rbDdqEeNWed8iLTOQyry3tbDvkCGBjW_9w");
            ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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

            SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                    signedJWT.getJWTClaimsSet());
            PowerMockito.mockStatic(CertificateMgtUtils.class);
            java.security.cert.X509Certificate x509Certificate = getX509Certificate("src/test/resources/cnf/invalid-certificate.pem");
            java.security.cert.CertificateFactory certificateFactory
                    = java.security.cert.CertificateFactory.getInstance("X.509");
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(x509Certificate.getEncoded());
            Optional<java.security.cert.X509Certificate> x509CertificateJava =
                    Optional.of((java.security.cert.X509Certificate) certificateFactory.generateCertificate(
                            byteArrayInputStream));
            PowerMockito.when(CertificateMgtUtils.convert(x509Certificate)).thenReturn(x509CertificateJava);
            signedJWTInfo
                    .setClientCertificate(getX509Certificate("src/test/resources/cnf/invalid-certificate.pem"));

            Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
            JWTValidatorWrapper jwtValidator = new JWTValidatorWrapper("Unlimited",
                    true, apiKeyValidator, false, null,
                    jwtConfigurationDto, jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache,
                    gatewayJWTTokenCache);
            MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
            org.apache.axis2.context.MessageContext axis2MsgCntxt =
                    Mockito.mock(org.apache.axis2.context.MessageContext.class);
            Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
            Mockito.when(axis2MsgCntxt.getProperty(APIMgtGatewayConstants.VALIDATED_X509_CERT)).thenReturn(x509Certificate);
            Map<String, String> headers = new HashMap<>();
            Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                    .thenReturn(headers);
            Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
            Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
            Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
            Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
            APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
            Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                    .thenReturn("true");
            jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
            APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
            apiKeyValidationInfoDTO.setApiName("api1");
            apiKeyValidationInfoDTO.setApiPublisher("admin");
            apiKeyValidationInfoDTO.setApiTier("Unlimited");
            apiKeyValidationInfoDTO.setAuthorized(true);
            Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                    .thenReturn(true);
            Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
            AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
            Mockito.verify(apiKeyValidator)
                    .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                            Mockito.anyString(), Mockito.anyString());

            Assert.assertNotNull(authenticate);
            Assert.assertEquals(authenticate.getApiName(), "api1");
            Assert.assertEquals(authenticate.getApiPublisher(), "admin");
            Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
            Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
            Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
            jwtValidator.authenticate(signedJWTInfo, messageContext);
        } catch (APISecurityException e) {
            Assert.assertEquals("Unclassified Authentication Failure", e.getMessage());
        }
    }

    @Test
    public void testJWTValidatorBypassCnfValidation() throws ParseException, APISecurityException,
            APIManagementException, IOException, CertificateException, javax.security.cert.CertificateException {

        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("carbon.super");
        SignedJWT signedJWT =
                SignedJWT.parse("eyJ4NXQiOiJOVGRtWmpNNFpEazNOalkwWXpjNU1tWm1PRGd3TVRFM01XWXdOREU1TVdSbFpEZzROemM0WkE" +
                        "iLCJraWQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSm" +
                        "tPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZ19QUzI1NiIsImFsZyI6IlBTMjU2In0.eyJhdWQiOiJodHRwOi8vb3JnL" +
                        "ndzbzIuYXBpbWd0L2dhdGV3YXkiLCJzdWIiOiJhZG1pbkBjYXJib24uc3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25l" +
                        "ciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6InJlcXVlc3RDb3VudCIsInRpZXIiOiJVbmxpbWl0ZWQiLCJuYW1lIjo" +
                        "iRGVmYXVsdEFwcGxpY2F0aW9uIiwiaWQiOjEsInV1aWQiOm51bGx9LCJzY29wZSI6ImFtX2FwcGxpY2F0aW9uX3Njb3" +
                        "BlIGRlZmF1bHQiLCJpc3MiOiJodHRwczovL2xvY2FsaG9zdDo5NDQzL29hdXRoMi90b2tlbiIsImNuZiI6eyJ4NXQjU" +
                        "zI1NiI6IlQyUHJpLXYxTm50TEN3a25TazQ2SjdoeFBPYmFENHgtQm5IQzVYYVRZUlEifSwidGllckluZm8iOnt9LCJr" +
                        "ZXl0eXBlIjoiUFJPRFVDVElPTiIsInN1YnNjcmliZWRBUElzIjpbXSwiY29uc3VtZXJLZXkiOiJYZ08zOTZJSEZLN2V" +
                        "GWXlkcnFRZTRIS0d6MWthIiwiZXhwIjoxNTkwMzQyMzEzLCJpYXQiOjE1OTAzMzg3MTMsImp0aSI6ImI4OTM4NzY4LT" +
                        "IzZmQtNGRlYy04YjcwLWJlZDQ1ZWI3YzMzZCJ9.R7ARkdO5evfAyg_lQmTCWXaP5zSA26XnQMI8QHgID72xx1YCBA1x" +
                        "xQeR4Q0EDgGUna3yePhLOiYIMSVJf_VDIMjIW2pFAH1i0ETWivYrRzNpBIZC0UjPiJ-Xjw02T2Omkywgf12ff_T3nz9" +
                        "eLZqP5SQkO01gihECzXYDFD3dQDRc63bjvmK_en5my5Rt-rUYPihl0uBaGXueHTYp-zNNN8aU_k7FEZTfs-UfOw4Fhh" +
                        "noXUTclbEbABbyjDiWgyeVCVwAeGHXIMAAqVM6qKZqGRFSW-wYuJ9UCz2x4QnXLoNXkNST0ly_rbDdqEeNWed8iLTOQ" +
                        "yry3tbDvkCGBjW_9w");
        ExtendedJWTConfigurationDto jwtConfigurationDto = new ExtendedJWTConfigurationDto();
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

        SignedJWTInfo signedJWTInfo = new SignedJWTInfo(signedJWT.getParsedString(), signedJWT,
                signedJWT.getJWTClaimsSet());
        PowerMockito.mockStatic(CertificateMgtUtils.class);
        java.security.cert.X509Certificate x509Certificate = getX509Certificate("src/test/resources/cnf/certificate.pem");
        java.security.cert.CertificateFactory certificateFactory
                = java.security.cert.CertificateFactory.getInstance("X.509");
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(x509Certificate.getEncoded());
        Optional<java.security.cert.X509Certificate> x509CertificateJava =
                Optional.of((java.security.cert.X509Certificate) certificateFactory.generateCertificate(
                        byteArrayInputStream));
        PowerMockito.when(CertificateMgtUtils.convert(x509Certificate)).thenReturn(x509CertificateJava);
        Mockito.when(jwtValidationService.validateJWTToken(signedJWTInfo)).thenReturn(jwtValidationInfo);
        JWTValidatorWrapper jwtValidator = new JWTValidatorWrapper("Unlimited",
                true, apiKeyValidator, false, null,
                jwtConfigurationDto, jwtValidationService, invalidTokenCache, gatewayTokenCache, gatewayKeyCache,
                gatewayJWTTokenCache);
        MessageContext messageContext = Mockito.mock(Axis2MessageContext.class);
        org.apache.axis2.context.MessageContext axis2MsgCntxt =
                Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("GET");
        Mockito.when(axis2MsgCntxt.getProperty(APIMgtGatewayConstants.VALIDATED_X509_CERT)).thenReturn(x509Certificate);
        Map<String, String> headers = new HashMap<>();
        Mockito.when(axis2MsgCntxt.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS))
                .thenReturn(headers);
        Mockito.when(axis2MsgCntxt.getProperty(APISecurityConstants.DISABLE_CNF_VALIDATION)).thenReturn(true);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/api1");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/pet/findByStatus");
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.JWT_AUTHENTICATION_SUBSCRIPTION_VALIDATION))
                .thenReturn("true");
        jwtValidator.setApiManagerConfiguration(apiManagerConfiguration);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApiName("api1");
        apiKeyValidationInfoDTO.setApiPublisher("admin");
        apiKeyValidationInfoDTO.setApiTier("Unlimited");
        apiKeyValidationInfoDTO.setAuthorized(true);
        Mockito.when(apiKeyValidator.validateScopes(Mockito.any(TokenValidationContext.class), Mockito.anyString()))
                .thenReturn(true);
        Mockito.when(apiKeyValidator.validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString())).thenReturn(apiKeyValidationInfoDTO);
        AuthenticationContext authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Mockito.verify(apiKeyValidator)
                .validateSubscription(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString());

        Assert.assertNull(signedJWTInfo.getClientCertificate());
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.when(gatewayTokenCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn("carbon.super");
        Mockito.when(gatewayKeyCache.get(signedJWT.getJWTClaimsSet().getJWTID())).thenReturn(jwtValidationInfo);
        authenticate = jwtValidator.authenticate(signedJWTInfo, messageContext);
        Assert.assertNotNull(authenticate);
        Assert.assertEquals(authenticate.getApiName(), "api1");
        Assert.assertEquals(authenticate.getApiPublisher(), "admin");
        Assert.assertEquals(authenticate.getConsumerKey(), jwtValidationInfo.getConsumerKey());
        Mockito.verify(jwtValidationService, Mockito.only()).validateJWTToken(signedJWTInfo);
        Mockito.verify(gatewayTokenCache, Mockito.atLeast(1)).get(signedJWT.getJWTClaimsSet()
                .getJWTID());
    }

    /**
     * Load X509Certificate from .pem file.
     *
     * @param certificatePath
     * @return
     * @throws java.security.cert.CertificateException
     * @throws FileNotFoundException
     */
    public static X509Certificate getX509Certificate(String certificatePath) throws java.security.cert.CertificateException,
            FileNotFoundException {

        final String x509 = "X.509";

        CertificateFactory certificateFactory = CertificateFactory.getInstance(x509);
        java.security.cert.X509Certificate x509Certificate = (X509Certificate) certificateFactory.
                generateCertificate(new FileInputStream(certificatePath));

        return x509Certificate;
    }
}
