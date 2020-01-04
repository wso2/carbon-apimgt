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

import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.base.MultitenantConstants;

@RunWith(PowerMockRunner.class)
@PrepareForTest({JWTValidator.class, GatewayUtils.class})
public class JWTValidatorTest {
    private JWTValidator jwtValidator;
    private MessageContext messageContext;
    private org.apache.axis2.context.MessageContext axis2MsgCntxt;
    private JSONObject payload;
    private String validJwtToken;

    @Before
    public void setup() throws Exception {
        // JSON payload of the token
        payload = new JSONObject(
                "{\n" +
                        "          \"sub\": \"admin@carbon.super\",\n" +
                        "          \"iss\": \"https://localhost:9443/oauth2/token\",\n" +
                        "          \"tierInfo\": {\n" +
                        "            \"Unlimited\": {\n" +
                        "              \"stopOnQuotaReach\": true,\n" +
                        "              \"spikeArrestLimit\": 0,\n" +
                        "              \"spikeArrestUnit\": null\n" +
                        "            }\n" +
                        "          },\n" +
                        "          \"keytype\": \"PRODUCTION\",\n" +
                        "          \"subscribedAPIs\": [\n" +
                        "            {\n" +
                        "              \"subscriberTenantDomain\": \"carbon.super\",\n" +
                        "              \"name\": \"PizzaShackAPI\",\n" +
                        "              \"context\": \"/pizzashack/1.0.0\",\n" +
                        "              \"publisher\": \"admin\",\n" +
                        "              \"version\": \"1.0.0\",\n" +
                        "              \"subscriptionTier\": \"Unlimited\"\n" +
                        "            }\n" +
                        "          ],\n" +
                        "          \"aud\": \"http://org.wso2.apimgt/gateway\",\n" +
                        "          \"application\": {\n" +
                        "            \"owner\": \"admin\",\n" +
                        "            \"tier\": \"Unlimited\",\n" +
                        "            \"name\": \"DefaultApplication\",\n" +
                        "            \"id\": 1\n" +
                        "          },\n" +
                        "          \"scope\": \"am_application_scope default\",\n" +
                        "          \"consumerKey\": \"U6Sjm1pawuc6K0mx5Hc9je5PTN8a\",\n" +
                        "          \"exp\": 1563032691,\n" +
                        "          \"iat\": 1563029091,\n" +
                        "          \"jti\": \"3f31a2db-39f9-4a00-a314-f548c8e657e2\"\n" +
                        "        }"
        );

        // Encoded jwt token
        validJwtToken = "eyJ4NXQiOiJOVEF4Wm1NeE5ETXlaRGczTVRVMVpHTTBNekV6T0RKaFpXSTRORE5sWkRVMU9HRmtOakZpTVEiL" +
                "CJraWQiOiJOVEF4Wm1NeE5ETXlaRGczTVRVMVpHTTBNekV6T0RKaFpXSTRORE5sWkRVMU9HRmtOakZpTVEiLCJhbGciO" +
                "iJSUzI1NiJ9" +
                ".ewogICJzdWIiOiAiYWRtaW5AY2FyYm9uLnN1cGVyIiwKICAiaXNzIjogImh0dHBzOi8vbG9j" +
                "YWxob3N0Ojk0NDMvb2F1dGgyL3Rva2VuIiwKICAidGllckluZm8iOiB7CiAgICAiVW5saW1pdGVkIjogewogICAgICAic" +
                "3RvcE9uUXVvdGFSZWFjaCI6IHRydWUsCiAgICAgICJzcGlrZUFycmVzdExpbWl0IjogMCwKICAgICAgInNwaWtlQXJyZX" +
                "N0VW5pdCI6IG51bGwKICAgIH0KICB9LAogICJrZXl0eXBlIjogIlBST0RVQ1RJT04iLAogICJzdWJzY3JpYmVkQVBJcyI" +
                "6IFsKICAgIHsKICAgICAgInN1YnNjcmliZXJUZW5hbnREb21haW4iOiAiY2FyYm9uLnN1cGVyIiwKICAgICAgIm5hbWUi" +
                "OiAiUGl6emFTaGFja0FQSSIsCiAgICAgICJjb250ZXh0IjogIi9waXp6YXNoYWNrLzEuMC4wIiwKICAgICAgInB1Ymxpc" +
                "2hlciI6ICJhZG1pbiIsCiAgICAgICJ2ZXJzaW9uIjogIjEuMC4wIiwKICAgICAgInN1YnNjcmlwdGlvblRpZXIiOiAiVW" +
                "5saW1pdGVkIgogICAgfQogIF0sCiAgImF1ZCI6ICJodHRwOi8vb3JnLndzbzIuYXBpbWd0L2dhdGV3YXkiLAogICJhcHB" +
                "saWNhdGlvbiI6IHsKICAgICJvd25lciI6ICJhZG1pbiIsCiAgICAidGllciI6ICJVbmxpbWl0ZWQiLAogICAgIm5hbWUi" +
                "OiAiRGVmYXVsdEFwcGxpY2F0aW9uIiwKICAgICJpZCI6IDEKICB9LAogICJzY29wZSI6ICJhbV9hcHBsaWNhdGlvbl9zY" +
                "29wZSBkZWZhdWx0IiwKICAiY29uc3VtZXJLZXkiOiAiVTZTam0xcGF3dWM2SzBteDVIYzlqZTVQVE44YSIsCiAgImV4cC" +
                "I6IDE1NjMwMzI2OTEsCiAgImdyYW50VHlwZSI6ICJjbGllbnRfY3JlZGVudGlhbHMiLAogICJpYXQiOiAxNTYzMDI5MDk" +
                "xLAogICJqdGkiOiAiM2YzMWEyZGItMzlmOS00YTAwLWEzMTQtZjU0OGM4ZTY1N2UyIgp9" +
                ".ghi";

        jwtValidator = PowerMockito.mock(JWTValidator.class);
        PowerMockito.when(jwtValidator, "authenticate",
                Mockito.any(), Mockito.any(), Mockito.any()).thenCallRealMethod();

        messageContext = Mockito.mock(Axis2MessageContext.class);
        axis2MsgCntxt = Mockito.mock(org.apache.axis2.context.MessageContext.class);
        Mockito.when(((Axis2MessageContext) messageContext).getAxis2MessageContext()).thenReturn(axis2MsgCntxt);
        Mockito.when(messageContext.getProperty(RESTConstants.REST_API_CONTEXT)).thenReturn("/pizzashack/1.0.0");
        Mockito.when(messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION)).thenReturn("1.0.0");
        Mockito.when(messageContext.getProperty(APIConstants.API_ELECTED_RESOURCE)).thenReturn("/menu");
        Mockito.when(axis2MsgCntxt.getProperty(Constants.Configuration.HTTP_METHOD)).thenReturn("get");
    }

    @Test
    public void testAuthenticationWithInvalidJwtToken2() throws Exception {
        // Invalid token payload
        initMocks();
        String invalidJwtToken = "aasdasd.xxx#.sad";
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.eq(invalidJwtToken.split(".")),
                Mockito.any())).thenReturn(false);

        try {
            jwtValidator.authenticate(invalidJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationSignatureVerificationFailure() throws Exception {
        // Token signature verification failure
        initMocks();
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.eq(validJwtToken.split(".")), Mockito.any()))
                .thenReturn(false);

        try {
            jwtValidator.authenticate(validJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationWithExpiredJwtToken() throws Exception {
        // Expired token
        initMocks();
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(), Mockito.any())).thenReturn(true);
        PowerMockito.when(jwtValidator, "checkTokenExpiration",
                Mockito.any(), Mockito.any(), Mockito.any()).thenThrow(
                new APISecurityException(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED, "JWT token is expired"));

        try {
            jwtValidator.authenticate(validJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_ACCESS_TOKEN_EXPIRED, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationWithScopeFailure() throws Exception {
        // Token does not have the scopes required to access the resource
        initMocks();
        PowerMockito.when(GatewayUtils.verifyTokenSignature(Mockito.any(), Mockito.any())).thenReturn(true);
        PowerMockito.when(jwtValidator, "validateScopes",
                Mockito.any(), Mockito.any(), Mockito.any()).thenThrow(new APISecurityException(
                APISecurityConstants.INVALID_SCOPE, "Scope validation failed"));

        try {
            jwtValidator.authenticate(validJwtToken, messageContext, null);
            Assert.fail();
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.INVALID_SCOPE, e.getErrorCode());
        }
    }

    @Test
    public void testAuthenticationWithAPISubscriptionFailure() throws Exception {
        // Owner of the token is not subscribed to access the resource
        try {
            GatewayUtils.validateAPISubscription("/unsubscribedPizzashack/1.0.0","1.0.0", payload,
                    validJwtToken.split("\\."), true);
        } catch (APISecurityException e) {
            Assert.assertEquals(APISecurityConstants.API_AUTH_FORBIDDEN, e.getErrorCode());
        }
    }

    @Test
    public void testGenerateAuthContext() throws Exception {

        String[] splitToken = validJwtToken.split("\\.");
        JSONObject api = new JSONObject("{\n" +
                "              \"subscriberTenantDomain\": \"carbon.super\",\n" +
                "              \"name\": \"PizzaShackAPI\",\n" +
                "              \"context\": \"/pizzashack/1.0.0\",\n" +
                "              \"publisher\": \"admin\",\n" +
                "              \"version\": \"1.0.0\",\n" +
                "              \"subscriptionTier\": \"Unlimited\"\n" +
                "            }\n");
        JSONObject tierInfo = new JSONObject("{\n" +
                "              \"stopOnQuotaReach\": true,\n" +
                "              \"spikeArrestLimit\": 0,\n" +
                "              \"spikeArrestUnit\": null\n" +
                "            }\n");

            AuthenticationContext authenticationContext = GatewayUtils.generateAuthenticationContext(splitToken[2],
                    payload, api, null, APIConstants.UNLIMITED_TIER, true);

        Assert.assertTrue(authenticationContext.isAuthenticated());
        Assert.assertEquals(splitToken[2], authenticationContext.getApiKey());
        Assert.assertEquals(payload.getString(APIConstants.JwtTokenConstants.KEY_TYPE),
                authenticationContext.getKeyType());
        Assert.assertEquals(payload.getString(APIConstants.JwtTokenConstants.SUBJECT),
                authenticationContext.getUsername());
        Assert.assertEquals(APIConstants.UNLIMITED_TIER, authenticationContext.getApiTier());

        JSONObject applicationObj = (JSONObject) payload.get(APIConstants.JwtTokenConstants.APPLICATION);
        Assert.assertEquals(String.valueOf(applicationObj.getInt(APIConstants.JwtTokenConstants.APPLICATION_ID)),
                authenticationContext.getApplicationId());
        Assert.assertEquals(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_NAME),
                authenticationContext.getApplicationName());
        Assert.assertEquals(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_TIER),
                authenticationContext.getApplicationTier());

        Assert.assertEquals(applicationObj.getString(APIConstants.JwtTokenConstants.APPLICATION_OWNER),
                authenticationContext.getSubscriber());
        Assert.assertEquals(payload.getString(APIConstants.JwtTokenConstants.CONSUMER_KEY),
                authenticationContext.getConsumerKey());

        Assert.assertEquals(api.getString(APIConstants.JwtTokenConstants.SUBSCRIPTION_TIER),
                authenticationContext.getTier());
        Assert.assertEquals(api.getString(APIConstants.JwtTokenConstants.SUBSCRIBER_TENANT_DOMAIN),
                authenticationContext.getSubscriberTenantDomain());

        Assert.assertEquals(tierInfo.getBoolean(APIConstants.JwtTokenConstants.STOP_ON_QUOTA_REACH),
                authenticationContext.isStopOnQuotaReach());
        Assert.assertEquals(tierInfo.getInt(APIConstants.JwtTokenConstants.SPIKE_ARREST_LIMIT),
                authenticationContext.getSpikeArrestLimit());
        Assert.assertNull(authenticationContext.getSpikeArrestUnit());
    }

    public void initMocks() {
        PowerMockito.mockStatic(GatewayUtils.class);
        PowerMockito.when(GatewayUtils.isGatewayTokenCacheEnabled()).thenReturn(true);
        PowerMockito.when(GatewayUtils.getTenantDomain()).thenReturn(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
    }
}
