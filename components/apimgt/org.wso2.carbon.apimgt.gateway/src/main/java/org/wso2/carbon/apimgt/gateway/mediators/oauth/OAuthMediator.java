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
import org.apache.commons.codec.binary.Base64;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.conf.OAuthEndpoint;
import org.wso2.carbon.apimgt.gateway.utils.RedisCache;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConstants.OAuthConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * OAuth Mediator for generating OAuth tokens for invoking service endpoints secured with OAuth
 */
public class OAuthMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(OAuthMediator.class);
    private JSONObject oAuthEndpointSecurityProperties;
    public static RedisCache redisCache;
    public static boolean isRedisEnabled = false;
    public static OAuthEndpoint oAuthEndpoint;

    // Interface methods are being implemented here
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        oAuthEndpointSecurityProperties = getOAuthEndpointSecurityProperties();
        if (oAuthEndpointSecurityProperties != null) {
            isRedisEnabled = ((Boolean) oAuthEndpointSecurityProperties
                    .get(APIConstants.IS_REDIS_ENABLED));
            if (isRedisEnabled) {
                String redisHost = (String) oAuthEndpointSecurityProperties.get(APIConstants.REDIS_HOST);
                String redisPort = (String) oAuthEndpointSecurityProperties.get(APIConstants.REDIS_PORT);
                if (oAuthEndpointSecurityProperties.containsKey(APIConstants.REDIS_PASSWORD)) {
                    char[] redisPassword = (char[]) oAuthEndpointSecurityProperties
                            .get(APIConstants.REDIS_PASSWORD);
                    redisCache = new RedisCache(redisHost, Integer.valueOf(redisPort), redisPassword);
                } else {
                    redisCache = new RedisCache(redisHost, Integer.valueOf(redisPort), null);
                }
            }
        }
    }

    @Override
    public void destroy() {
        if (isRedisEnabled) {
            redisCache.stopRedisCacheSession();
        }
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        if(log.isDebugEnabled()) {
            log.debug("OAuth Mediator is invoked...");
        }

        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
        CountDownLatch latch = new CountDownLatch(1);
        try {
            String username = null;
            String password = null;

            String uniqueIdentifier = (String) messageContext.getProperty(OAuthConstants.UNIQUE_IDENTIFIER);
            String tokenApiUrl = (String) messageContext.getProperty(OAuthConstants.TOKEN_API_URL);
            String clientId = (String) messageContext.getProperty(OAuthConstants.OAUTH_CLIENT_ID);
            String clientSecret = (String) messageContext.getProperty(OAuthConstants.OAUTH_CLIENT_SECRET);
            String grantType = (String) messageContext.getProperty(OAuthConstants.GRANT_TYPE);
            String customParametersString = (String) messageContext.getProperty(OAuthConstants.OAUTH_CUSTOM_PARAMETERS);

            JSONParser parser = new JSONParser();
            JSONObject customParameters = null;
            if (customParametersString != null && !customParametersString.equals("")) {
                customParameters = (JSONObject) parser.parse(customParametersString);
            }

            if (grantType.equals("PASSWORD")) {
                String usernamePassword;
                String unpw;
                unpw = (String) messageContext.getProperty(OAuthConstants.OAUTH_USERNAMEPASSWORD);

                if (unpw != null) {
                    usernamePassword = new String(Base64.decodeBase64(unpw.getBytes()));
                    String[] usernamePasswordSplit = usernamePassword.split(":");
                    username = usernamePasswordSplit[0];
                    password = usernamePasswordSplit[1];
                }
            }

            String decryptedClientSecret = new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret));

            oAuthEndpoint = new OAuthEndpoint();
            oAuthEndpoint.setId(uniqueIdentifier);
            oAuthEndpoint.setTokenApiUrl(tokenApiUrl);
            oAuthEndpoint.setClientId(clientId);
            oAuthEndpoint.setClientSecret(decryptedClientSecret);
            oAuthEndpoint.setUsername(username);
            if (password != null) {
                oAuthEndpoint.setPassword(password.toCharArray());
            }
            oAuthEndpoint.setGrantType(grantType);
            oAuthEndpoint.setCustomParameters(customParameters);

            if (oAuthEndpoint != null) {
                try {
                    OAuthTokenGenerator.checkTokenValidity(oAuthEndpoint, latch);
                    latch.await();
                } catch(InterruptedException | APISecurityException e) {
                    log.error("Could not generate access token...", e);
                }
            }

            TokenResponse tokenResponse;
            if (isRedisEnabled) {
                tokenResponse = redisCache.getTokenResponseById(oAuthEndpoint.getId());
            } else {
                tokenResponse = TokenCache.getInstance().getTokenMap().get(oAuthEndpoint.getId());
            }
            if (tokenResponse != null) {
                String accessToken = tokenResponse.getAccessToken();
                Map<String, Object> transportHeaders = (Map<String, Object>) ((Axis2MessageContext) messageContext)
                        .getAxis2MessageContext().getProperty("TRANSPORT_HEADERS");
                transportHeaders.put("Authorization", "Bearer " + accessToken);
                log.debug("Access token set: " + accessToken);
            } else {
                log.debug("Token Response is empty...");
            }
        } catch (CryptoException e) {
            log.error(" Error occurred when decrypting the client key and client secret", e);
        } catch (ParseException e) {
            log.error("Failed to parse OAuth Custom Parameters", e);
        }
        return true;
    }

    /**
     * This method returns the OAuthEndpointSecurity Properties from the API Manager Configuration
     * @return JSONObject OAuthEndpointSecurity properties
     */
    public static JSONObject getOAuthEndpointSecurityProperties() {
        APIManagerConfiguration configuration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String redisHost = configuration.getFirstProperty(APIConstants.CONFIG_REDIS_HOST);
        String redisPort = configuration.getFirstProperty(APIConstants.CONFIG_REDIS_PORT);
        String redisPassword = configuration.getFirstProperty(APIConstants.CONFIG_REDIS_PASSWORD);

        JSONObject configProperties = new JSONObject();

        if(StringUtils.isNotBlank(redisHost)
                && StringUtils.isNotBlank(redisPort)) {
            configProperties.put(APIConstants.IS_REDIS_ENABLED, true);
            configProperties.put(APIConstants.REDIS_HOST, redisHost);
            configProperties.put(APIConstants.REDIS_PORT, redisPort);
            if (StringUtils.isNotBlank(redisPassword)) {
                configProperties.put(APIConstants.REDIS_PASSWORD, redisPassword);
            }
            return configProperties;
        }
        return null;
    }
}
