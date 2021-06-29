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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.OAuthClient;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.client.TokenResponse;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.conf.OAuthEndpoint;
import org.wso2.carbon.apimgt.gateway.utils.redis.RedisCacheUtils;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * OAuthTokenGenerator class to check validity of tokens, request for tokens
 * and add tokens to in-memory cache or redis cache
 */
public class OAuthTokenGenerator {
    private static final Log log = LogFactory.getLog(OAuthTokenGenerator.class);

    /**
     * Method to check for and refresh expired/generate new access tokens
     * @param oAuthEndpoint OAuthEndpoint object for token endpoint properties
     * @param latch CountDownLatch for blocking call when OAuth API is invoked
     * @throws APISecurityException In the event of errors when generating new token
     */
    public static void generateToken(OAuthEndpoint oAuthEndpoint, CountDownLatch latch)
            throws APISecurityException {
        try {
            TokenResponse previousResponse = null;
            if (OAuthMediator.isRedisEnabled) {
                Object previousResponseObject = RedisCacheUtils.getInstance().getObject(oAuthEndpoint.getId(), TokenResponse.class);
                if (previousResponseObject != null) {
                    previousResponse = (TokenResponse) previousResponseObject;
                }
            } else {
                previousResponse = TokenCache.getInstance().getTokenMap().get(oAuthEndpoint.getId());
            }
            if (previousResponse != null) {
                long validTill = previousResponse.getValidTill();
                long currentTimeInSeconds = System.currentTimeMillis() / 1000;
                long timeDifference = validTill - currentTimeInSeconds;

                if (timeDifference <= 1) {
                    if (previousResponse.getRefreshToken() != null) {
                        addTokenToCache(oAuthEndpoint, previousResponse.getRefreshToken());
                    } else {
                        addTokenToCache(oAuthEndpoint, null);
                    }
                }
            } else {
                addTokenToCache(oAuthEndpoint, null);
            }
        } catch (IOException e) {
            log.error("Error while generating OAuth Token" + getEndpointId(oAuthEndpoint));
            throw new APISecurityException(APISecurityConstants.API_AUTH_INVALID_CREDENTIALS,
                    APISecurityConstants.API_AUTH_INVALID_CREDENTIALS_MESSAGE, e);
        } catch (APIManagementException e) {
            log.error("Could not retrieve OAuth Token" + getEndpointId(oAuthEndpoint));
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while retrieving OAuth token", e);
        } catch (ParseException e) {
            log.error("Could not retrieve OAuth Token" + getEndpointId(oAuthEndpoint));
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while parsing OAuth Token endpoint response", e);
        }
        if (latch != null) {
            latch.countDown();
        }
    }

    /**
     * Method to request for access token and add the generated token into
     * in-memory cache or redis cache
     * @param oAuthEndpoint OAuthEndpoint object for token endpoint properties
     * @param refreshToken Refresh token if exists
     * @throws IOException In the event of errors with HttpClient connections
     * @throws APIManagementException In the event of errors when accessing the token endpoint url
     */
    private static void addTokenToCache(OAuthEndpoint oAuthEndpoint, String refreshToken)
            throws IOException, APIManagementException, ParseException {
        TokenResponse tokenResponse = OAuthClient.generateToken(oAuthEndpoint.getTokenApiUrl(),
                oAuthEndpoint.getClientId(), oAuthEndpoint.getClientSecret(), oAuthEndpoint.getUsername(),
                oAuthEndpoint.getPassword(), oAuthEndpoint.getGrantType(), oAuthEndpoint.getCustomParameters(),
                refreshToken);
        assert tokenResponse != null;
        if (tokenResponse.getExpiresIn() != null) {
            if (OAuthMediator.isRedisEnabled) {
                RedisCacheUtils.getInstance().addObject(oAuthEndpoint.getId(), tokenResponse);
            } else {
                TokenCache.getInstance().getTokenMap().put(oAuthEndpoint.getId(), tokenResponse);
            }
        }
    }

    /**
     * Method to construct string for logging
     * @param oAuthEndpoint OAuthEndpoint object for token endpoint properties
     * @return string containing token endpoint url for logging errors
     */
    private static String getEndpointId(OAuthEndpoint oAuthEndpoint) {
        return "[url] " + oAuthEndpoint.getTokenApiUrl();
    }
}
