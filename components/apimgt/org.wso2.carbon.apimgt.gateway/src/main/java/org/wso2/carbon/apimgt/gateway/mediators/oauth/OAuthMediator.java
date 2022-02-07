/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators.oauth;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.conf.OAuthEndpoint;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * OAuth Mediator for generating OAuth tokens for invoking service endpoints secured with OAuth.
 */
public class OAuthMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(OAuthMediator.class);
    public OAuthEndpoint oAuthEndpoint;
    private String tokenEndpointUrl;
    private String uniqueIdentifier;
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String customParameters;
    private String username;
    private String password;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        JSONParser parser = new JSONParser();
        JSONObject customParameterJson = null;
        if (StringUtils.isNotEmpty(customParameters)) {
            try {
                customParameterJson = (JSONObject) parser.parse(customParameters);
            } catch (ParseException e) {
                log.error("Error while parsing custom parameters", e);
            }
        }
        oAuthEndpoint = new OAuthEndpoint();
        oAuthEndpoint.setId(uniqueIdentifier);
        oAuthEndpoint.setTokenApiUrl(tokenEndpointUrl);
        oAuthEndpoint.setClientId(clientId);
        oAuthEndpoint.setClientSecret(clientSecret);
        oAuthEndpoint.setGrantType(grantType);
        oAuthEndpoint.setCustomParameters(customParameterJson);
        if (APIConstants.GRANT_TYPE_PASSWORD.equalsIgnoreCase(grantType)) {
            if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
                log.warn("User Credentials are empty OAuthMediator will not work properly.");
            } else {
                oAuthEndpoint.setPassword(password.toCharArray());
                oAuthEndpoint.setUsername(username);
            }
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("OAuth Mediator is invoked...");
        }

        CountDownLatch latch = new CountDownLatch(1);

        TokenResponse tokenResponse = null;
        if (oAuthEndpoint != null) {
            try {
                tokenResponse = OAuthTokenGenerator.generateToken(oAuthEndpoint, latch);
                latch.await();
            } catch (InterruptedException | APISecurityException e) {
                log.error("Could not generate access token...", e);
            }
        }

        if (tokenResponse != null) {
            String accessToken = tokenResponse.getAccessToken();
            Map<String, Object> transportHeaders = (Map<String, Object>) ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext().getProperty("TRANSPORT_HEADERS");
            transportHeaders.put("Authorization", "Bearer " + accessToken);
            if (log.isDebugEnabled()) {
                log.debug("Access token set: " + GatewayUtils.getMaskedToken(accessToken));
            }
        } else {
            log.debug("Token Response is empty...");
        }
        messageContext.setProperty(APIMgtGatewayConstants.OAUTH_ENDPOINT_INSTANCE, oAuthEndpoint);
        return true;
    }

    @Override
    public boolean isContentAware() {
        return false;
    }

    public String getTokenEndpointUrl() {

        return tokenEndpointUrl;
    }

    public void setTokenEndpointUrl(String tokenEndpointUrl) {

        this.tokenEndpointUrl = tokenEndpointUrl;
    }

    public String getUniqueIdentifier() {

        return uniqueIdentifier;
    }

    public void setUniqueIdentifier(String uniqueIdentifier) {

        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getClientId() {

        return clientId;
    }

    public void setClientId(String clientId) {

        this.clientId = clientId;
    }

    public String getClientSecret() {

        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {

        this.clientSecret = clientSecret;
    }

    public String getGrantType() {

        return grantType;
    }

    public void setGrantType(String grantType) {

        this.grantType = grantType;
    }

    public String getCustomParameters() {

        return customParameters;
    }

    public void setCustomParameters(String customParameters) {

        this.customParameters = customParameters;
    }

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }
}
