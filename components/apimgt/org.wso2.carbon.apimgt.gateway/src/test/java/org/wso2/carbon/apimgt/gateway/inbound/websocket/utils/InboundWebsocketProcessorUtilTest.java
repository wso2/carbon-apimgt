/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.t
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.testng.Assert;
import org.wso2.carbon.apimgt.common.gateway.constants.GraphQLConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketUtil;
import org.wso2.carbon.apimgt.gateway.handlers.WebsocketWSClient;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.jwt.JWTValidator;
import org.wso2.carbon.apimgt.gateway.handlers.streaming.websocket.WebSocketApiConstants;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication.ApiKeyAuthenticator;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.Authentication.OAuthAuthenticator;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.publisher.ThrottleDataPublisher;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.caching.CacheProvider;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.VerbInfoDTO;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidationService;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.model.entity.API;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.agent.DataPublisher;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({InboundWebsocketProcessorUtil.class, PrivilegedCarbonContext.class,
        ServiceReferenceHolder.class, WebsocketUtil.class, ThrottleDataPublisher.class, APIUtil.class, DataHolder.class,
        OAuthAuthenticator.class, ApiKeyAuthenticator.class, CacheProvider.class, JWTClaimsSet.class, Utils.class})
public class InboundWebsocketProcessorUtilTest {

    private DataPublisher dataPublisher;
    private DataHolder dataHolder;
    List<String> keyManagers;
    String authenticationHeader;
    String apiKey;

    @Before
    public void init() throws APIManagementException {
        System.setProperty("carbon.home", "jhkjn");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        dataHolder = Mockito.mock(DataHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        JWTValidationService jwtValidationService = Mockito.mock(JWTValidationService.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        PowerMockito.when(serviceReferenceHolder.getJwtValidationService()).thenReturn(jwtValidationService);
        Mockito.when(jwtValidationService.getKeyManagerNameIfJwtValidatorExist(Mockito.anyObject()))
                .thenReturn("KeyManager");
        PowerMockito.mockStatic(ThrottleDataPublisher.class);
        dataPublisher = Mockito.mock(DataPublisher.class);
        ThrottleDataPublisher throttleDataPublisher = Mockito.mock(ThrottleDataPublisher.class);
        Mockito.when(serviceReferenceHolder.getThrottleDataPublisher()).thenReturn(throttleDataPublisher);
        PowerMockito.when(ThrottleDataPublisher.getDataPublisher()).thenReturn(dataPublisher);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(serviceReferenceHolder.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.mockStatic(WebsocketUtil.class);
        PowerMockito.mockStatic(DataHolder.class);
        Mockito.when(DataHolder.getInstance()).thenReturn(dataHolder);
        keyManagers = Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS);
        authenticationHeader = "Bearer eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHU"
                + "XpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16WXhNbUZrT0dZd01XSTBaV05tTkRjeE5HW"
                + "XdZbU00WlRBM01XSTJOREF6WkdRek5HTTBaR1JsTmpKa09ERmtaRFJpT1RGa01XRmhNelUyWkdWbE5nX1JTMjU2IiwiYWxnIjoiU"
                + "lMyNTYifQ.eyJzdWIiOiJhZG1pbiIsImF1dCI6IkFQUExJQ0FUSU9OIiwiYXVkIjoiT2s3eVN6amdPVnVkdzVBRkdBbUlVYTl1WV"
                + "h3YSIsIm5iZiI6MTY2Mjk4MjkzOSwiYXpwIjoiT2s3eVN6amdPVnVkdzVBRkdBbUlVYTl1WVh3YSIsInNjb3BlIjoiZGVmYXVsdC"
                + "IsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dGgyXC90b2tlbiIsImV4cCI6MTY2Mjk4NjUzOSwiaWF0IjoxNj"
                + "YyOTgyOTM5LCJqdGkiOiI2NjMyNDUxMC0yOTVhLTQyMTAtODc4Mi0xNzMwMWY4N2UxYzYifQ.boseFmeSEzUCXRS1erAjL4Sdd2k"
                + "q2VOisx9EjOH2il-UtSqaCgfCzjZoi9QhwokKA98oT65X9U2yeptQC5GQnSJ8nx_wKywGYbCBL-aO6lo53uf_AHxPWWRkUAAD9Od"
                + "cReHIYTC7kHmozvGGSBl2aul_c7-ND1twPF8N3cXfdJMrdlL0i-fE5D39BUS4RkLstbrLVPNDJ-HQAJ8AR0UN7dDEnQYwiaTXTMM"
                + "EgIGtk-PF1o8a9Rao_HPdiM0v9xiuZUXWBVqGPgnJkXH2tq_EZwY3sFzuvW_jBE84cvyD9w_wU0f89sIC8RHhc0L17riSA-21yKO"
                + "6twHWjeAgZe_Kdg";
        apiKey = "eyJ4NXQiOiJPREUzWTJaaE1UQmpNRE00WlRCbU1qQXlZemxpWVRJMllqUmhZVFpsT0dJeVptVXhOV0UzWVE9PSIsImtpZCI" +
                "6ImdhdGV3YXlfY2VydGlmaWNhdGVfYWxpYXMiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBjYXJib24uc" +
                "3VwZXIiLCJhcHBsaWNhdGlvbiI6eyJvd25lciI6ImFkbWluIiwidGllclF1b3RhVHlwZSI6bnVsbCwidGllciI6IlVubGltaXRlZCIs" +
                "Im5hbWUiOiJEZWZhdWx0QXBwbGljYXRpb24iLCJpZCI6MSwidXVpZCI6IjA2MzRkMGI0LWRmMGEtNGMzZS04ZmY2LWRmODNhOTAzYTl" +
                "mNiJ9LCJpc3MiOiJodHRwczpcL1wvbG9jYWxob3N0Ojk0NDNcL29hdXRoMlwvdG9rZW4iLCJ0aWVySW5mbyI6eyJBc3luY1VubGltaX" +
                "RlZCI6eyJ0aWVyUXVvdGFUeXBlIjoiZXZlbnRDb3VudCIsImdyYXBoUUxNYXhDb21wbGV4aXR5IjowLCJncmFwaFFMTWF4RGVwdGgiO" +
                "jAsInN0b3BPblF1b3RhUmVhY2giOnRydWUsInNwaWtlQXJyZXN0TGltaXQiOjAsInNwaWtlQXJyZXN0VW5pdCI6bnVsbH19LCJrZXl0" +
                "eXBlIjoiU0FOREJPWCIsInBlcm1pdHRlZFJlZmVyZXIiOiIiLCJzdWJzY3JpYmVkQVBJcyI6W3sic3Vic2NyaWJlclRlbmFudERvbWF" +
                "pbiI6ImNhcmJvbi5zdXBlciIsIm5hbWUiOiJDaGF0cyIsImNvbnRleHQiOiJcL2NoYXRzXC8xLjAuMCIsInB1Ymxpc2hlciI6ImFkbW" +
                "luIiwidmVyc2lvbiI6IjEuMC4wIiwic3Vic2NyaXB0aW9uVGllciI6IkFzeW5jVW5saW1pdGVkIn1dLCJ0b2tlbl90eXBlIjoiYXBpS" +
                "2V5IiwicGVybWl0dGVkSVAiOiIiLCJpYXQiOjE2OTM5OTk3MDcsImp0aSI6IjhjY2M1YzBlLWFlODMtNGM4MS1hN2JmLTVjMmNiYTc0" +
                "ZmM1OCJ9.VYkMrt6vs82V1cVJ-ChFcgBfej3m8P22lmnG0_Q1g_fox0ZeJklWhjtsI8dxyTJ0tRx57dg1dOFQD-VbRTnoxOmWDnZdxB" +
                "_cGQfrn-2A72HBTMx-lAaMGAi0Gi9OjXBa8J2ilc7qBgL4au4HVfSyOxpAJIDgwPnjIYjDovnYQPMhZemaOfKTbfReU3g_w8MBKLN20" +
                "hjZT02gKpwyak1LnXG4ulKi-A0qZlm2VSArpwF73x9vK0rIiWT17UR43IJNoDAmXjG76lwmNeIIiDhyqRBLDdRwmxFLlac6KsqeESMh" +
                "xoklqH1_0x4VyUG-JYlJMQVFd5FmWmnjjF4eJZwSaQ==";
    }

    @Test
    public void testDoThrottleSuccessForGraphQL() throws ParseException {

        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling("Gold");
        verbInfoDTO.setRequestKey("liftStatusChange");
        String operationId = "1";
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApplicationTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        apiKeyValidationInfoDTO.setSubscriber("admin");
        apiKeyValidationInfoDTO.setApiName("GraphQLAPI");
        apiKeyValidationInfoDTO.setApplicationId("12");
        inboundMessageContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        inboundMessageContext.setApiContext("/graphql");
        inboundMessageContext.setVersion("1.0.0");
        inboundMessageContext.setUserIP("198.162.10.2");
        inboundMessageContext.setInfoDTO(apiKeyValidationInfoDTO);

        String subscriptionLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + inboundMessageContext.getApiContext() + ":" + inboundMessageContext.getVersion();
        String applicationLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + apiKeyValidationInfoDTO.getSubscriber() + "@" + apiKeyValidationInfoDTO.getSubscriberTenantDomain();
        PowerMockito.when(WebsocketUtil.isThrottled(verbInfoDTO.getRequestKey(), subscriptionLevelThrottleKey,
                applicationLevelThrottleKey)).thenReturn(false);
        Mockito.when(dataPublisher.tryPublish(Mockito.anyObject())).thenReturn(true);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO, inboundMessageContext,
                        operationId);
        Assert.assertFalse(inboundProcessorResponseDTO.isError());
        Assert.assertNull(inboundProcessorResponseDTO.getErrorMessage());
        Assert.assertFalse(inboundProcessorResponseDTO.isCloseConnection());
    }

    @Test
    public void testDoThrottleFail() throws ParseException {
        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        int msgSize = 100;
        VerbInfoDTO verbInfoDTO = new VerbInfoDTO();
        verbInfoDTO.setThrottling("Gold");
        verbInfoDTO.setRequestKey("liftStatusChange");
        String operationId = "1";
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();
        apiKeyValidationInfoDTO.setApplicationTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setTier(APIConstants.UNLIMITED_TIER);
        apiKeyValidationInfoDTO.setSubscriberTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        apiKeyValidationInfoDTO.setSubscriber("admin");
        apiKeyValidationInfoDTO.setApiName("GraphQLAPI");
        apiKeyValidationInfoDTO.setApplicationId("12");
        inboundMessageContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        inboundMessageContext.setApiContext("/graphql");
        inboundMessageContext.setVersion("1.0.0");
        inboundMessageContext.setUserIP("198.162.10.2");
        inboundMessageContext.setInfoDTO(apiKeyValidationInfoDTO);

        String subscriptionLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + inboundMessageContext.getApiContext() + ":" + inboundMessageContext.getVersion();
        String applicationLevelThrottleKey = apiKeyValidationInfoDTO.getApplicationId() + ":"
                + apiKeyValidationInfoDTO.getSubscriber() + "@" + apiKeyValidationInfoDTO.getSubscriberTenantDomain();
        Mockito.when(dataPublisher.tryPublish(Mockito.anyObject())).thenReturn(true);

        PowerMockito.when(WebsocketUtil.isThrottled(verbInfoDTO.getRequestKey(), subscriptionLevelThrottleKey,
                applicationLevelThrottleKey)).thenReturn(true);
        InboundProcessorResponseDTO inboundProcessorResponseDTO =
                InboundWebsocketProcessorUtil.doThrottleForGraphQL(msgSize, verbInfoDTO, inboundMessageContext,
                        operationId);
        Assert.assertTrue(inboundProcessorResponseDTO.isError());
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorMessage(),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
        Assert.assertEquals(inboundProcessorResponseDTO.getErrorCode(),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR);
        Assert.assertFalse(inboundProcessorResponseDTO.isCloseConnection());

        JSONParser jsonParser = new JSONParser();
        JSONObject errorJson = (JSONObject) jsonParser.parse(inboundProcessorResponseDTO.getErrorResponseString());
        org.junit.Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_TYPE),
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_TYPE_ERROR);
        org.junit.Assert.assertEquals(errorJson.get(GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_ID), "1");
        JSONObject payload = (JSONObject) ((JSONArray) errorJson.get(
                GraphQLConstants.SubscriptionConstants.PAYLOAD_FIELD_NAME_PAYLOAD)).get(0);
        org.junit.Assert.assertEquals(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_MESSAGE),
                WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR_MESSAGE);
        org.junit.Assert.assertEquals(String.valueOf(payload.get(WebSocketApiConstants.FrameErrorConstants.ERROR_CODE)),
                String.valueOf(WebSocketApiConstants.FrameErrorConstants.THROTTLED_OUT_ERROR));
    }

    @Test
    public void isAuthenticatedJWTForOAuth() throws Exception {
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        inboundMessageContext.getRequestHeaders().put(WebsocketUtil.authorizationHeader, authenticationHeader);
        inboundMessageContext.setAuthenticator(new OAuthAuthenticator());
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(InboundWebsocketProcessorUtil.class, "validateAuthenticationContext"))
                .toReturn(true);
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext);
        Assert.assertTrue(inboundMessageContext.isJWTToken());
    }

    @Test
    public void isAuthenticatedJWTForOAuthWhenOauthIsDisabled() throws Exception {
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        inboundMessageContext.getRequestHeaders().put(WebsocketUtil.authorizationHeader, authenticationHeader);
        inboundMessageContext.setAuthenticator(new OAuthAuthenticator());
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(InboundWebsocketProcessorUtil.class, "validateAuthenticationContext"))
                .toReturn(true);
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.API_SECURITY_API_KEY);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        Assert.assertFalse(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext));
    }

    @Test
    public void isAuthenticatedOpaqueForOAuth() throws Exception {
        String apiKey = "5ccc069c403ebaf9f0171e9517f40e41";
        String authenticationHeader = "Bearer " + apiKey;
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.setAuthenticator(new OAuthAuthenticator());
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        inboundMessageContext.getRequestHeaders().put(WebsocketUtil.authorizationHeader, authenticationHeader);
        WebsocketWSClient websocketWSClient = Mockito.mock(WebsocketWSClient.class);
        PowerMockito.whenNew(WebsocketWSClient.class).withNoArguments().thenReturn(websocketWSClient);
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = Mockito.mock(APIKeyValidationInfoDTO.class);
        Mockito.when(websocketWSClient.getAPIKeyData(inboundMessageContext.getApiContext(),
                        inboundMessageContext.getVersion(), apiKey, inboundMessageContext.getTenantDomain(), keyManagers))
                .thenReturn(apiKeyValidationInfoDTO);
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext);
        Assert.assertFalse(inboundMessageContext.isJWTToken());
    }

    @Test
    public void authenticateTokenForOAuth() throws Exception {
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.setJWTToken(true);
        inboundMessageContext.getRequestHeaders().put(WebsocketUtil.authorizationHeader, authenticationHeader);
        inboundMessageContext.setAuthenticator(new OAuthAuthenticator());
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(InboundWebsocketProcessorUtil.class, "validateAuthenticationContext"))
                .toReturn(true);
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        InboundProcessorResponseDTO responseDTO = InboundWebsocketProcessorUtil.authenticateToken(
                inboundMessageContext);
        Assert.assertFalse(responseDTO.isError());
    }

    @Test
    public void authenticateTokenFailureForOAuth() throws Exception {
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.setJWTToken(true);
        inboundMessageContext.getRequestHeaders().put(WebsocketUtil.authorizationHeader, authenticationHeader);
        inboundMessageContext.setAuthenticator(new OAuthAuthenticator());
        JWTValidator jwtValidator = Mockito.mock(JWTValidator.class);
        PowerMockito.whenNew(JWTValidator.class).withAnyArguments().thenReturn(jwtValidator);
        PowerMockito.stub(PowerMockito.method(InboundWebsocketProcessorUtil.class, "validateAuthenticationContext"))
                .toReturn(false);
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        InboundProcessorResponseDTO responseDTO = InboundWebsocketProcessorUtil.authenticateToken(
                inboundMessageContext);
        Assert.assertTrue(responseDTO.isError());
    }

    @Test
    public void isAuthenticatedForAPIKey() throws Exception {
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        InboundProcessorResponseDTO InboundProcessorResponseDTO = new InboundProcessorResponseDTO();
        Mockito.when(dataHolder.getKeyManagersFromUUID(inboundMessageContext.getElectedAPI().getUuid()))
                .thenReturn(keyManagers);
        inboundMessageContext.getRequestHeaders().put(APIConstants.API_KEY_HEADER_QUERY_PARAM, apiKey);
        inboundMessageContext.setAuthenticator(new ApiKeyAuthenticator());
        PowerMockito.stub(PowerMockito.method(ApiKeyAuthenticator.class, "authenticate",
                        InboundMessageContext.class))
                .toReturn(InboundProcessorResponseDTO);
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.API_SECURITY_API_KEY);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        Assert.assertTrue(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext));
    }

    @Test
    public void isAuthenticatedForAPIKeyWhenAPIKeyIsDisabled() throws Exception {
        InboundMessageContext inboundMessageContext = createWebSocketApiMessageContext();
        inboundMessageContext.getRequestHeaders().put(APIConstants.API_KEY_HEADER_QUERY_PARAM, apiKey);
        inboundMessageContext.setAuthenticator(new ApiKeyAuthenticator());
        List<String> securitySchemeList = new ArrayList<>();
        securitySchemeList.add(APIConstants.DEFAULT_API_SECURITY_OAUTH2);
        PowerMockito.stub(PowerMockito.method(Utils.class, "getSecuritySchemeOfWebSocketAPI"))
                .toReturn(securitySchemeList);
        Assert.assertFalse(InboundWebsocketProcessorUtil.isAuthenticated(inboundMessageContext));
    }

    private InboundMessageContext createWebSocketApiMessageContext() {
        API websocketAPI = new API(UUID.randomUUID().toString(), 1, "admin", "WSAPI", "1.0.0",
                "/wscontext", "Unlimited", APIConstants.API_TYPE_WS, APIConstants.PUBLISHED_STATUS,
                false);
        InboundMessageContext inboundMessageContext = new InboundMessageContext();
        inboundMessageContext.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        inboundMessageContext.setElectedAPI(websocketAPI);
        return inboundMessageContext;
    }
}
