/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.gateway.security.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.exception.APIKeyMgtException;
import org.wso2.carbon.apimgt.gateway.models.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.gateway.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.gateway.models.TokenValidationContext;
import org.wso2.carbon.apimgt.gateway.utils.KeyManagerConstants;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * This is the default key validation handler which will communicate with WSO2 IS
 */
public class DefaultKeyValidationHandler implements KeyValidationHandler {
    static final Logger LOG = LoggerFactory.getLogger(DefaultKeyValidationHandler.class);

    @Override
    public boolean validateToken(TokenValidationContext tokenValidationContext) throws APIKeyMgtException {
        AccessTokenInfo tokenInfo = getTokenMetadata(tokenValidationContext.getAccessToken());
        setValuesForOAuth2ValidationContext(tokenValidationContext, tokenInfo);
        return tokenInfo.isTokenValid();
    }

    @Override
    public boolean validateScopes(TokenValidationContext tokenValidationContext) throws APIKeyMgtException {
        return false;
    }

    @Override
    public boolean generateConsumerToken(TokenValidationContext tokenValidationContext) throws APIKeyMgtException {
        return false;
    }

    private AccessTokenInfo getTokenMetadata(String accessToken) throws APIKeyMgtException {
        AccessTokenInfo tokenInfo = new AccessTokenInfo();
        URL url;
        HttpURLConnection urlConn = null;
        try {
            String introspectEndpoint = System.getProperty("introspectEndpoint",
                    "http://localhost:9763/oauth2/introspect");
            url = new URL(introspectEndpoint);
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoOutput(true);
            urlConn.setRequestMethod("POST");
            urlConn.getOutputStream()
                    .write(("token=" + accessToken).getBytes("UTF-8"));
            String responseStr = new String(IOUtils.toByteArray(urlConn.getInputStream()), "UTF-8");
            JsonParser parser = new JsonParser();
            JsonObject jObj = parser.parse(responseStr).getAsJsonObject();
            boolean active = jObj.getAsJsonPrimitive("active").getAsBoolean();
            if (active) {
                String consumerKey = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_ID).getAsString();
                String endUser = jObj.getAsJsonPrimitive(KeyManagerConstants.USERNAME).getAsString();
                long exp = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_EXP_TIME).getAsLong();
                long issuedTime = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH2_TOKEN_ISSUED_TIME).getAsLong();
                String scopes = jObj.getAsJsonPrimitive(KeyManagerConstants.OAUTH_CLIENT_SCOPE).getAsString();
                if (scopes != null) {
                    String[] scopesArray = scopes.split("\\s+");
                    tokenInfo.setScopes(scopesArray);
                }
                tokenInfo.setTokenValid(true);
                tokenInfo.setAccessToken(accessToken);
                tokenInfo.setConsumerKey(consumerKey);
                tokenInfo.setEndUserName(endUser);
                tokenInfo.setIssuedTime(issuedTime);

                // Convert Expiry Time to milliseconds.
                if (exp == Long.MAX_VALUE) {
                    tokenInfo.setValidityPeriod(Long.MAX_VALUE);
                } else {
                    tokenInfo.setValidityPeriod(exp * 1000);
                }

            } else {

                tokenInfo.setTokenValid(false);
                LOG.error("Invalid OAuth Token. ");
                tokenInfo.setErrorcode(KeyManagerConstants.KeyValidationStatus.API_AUTH_INVALID_CREDENTIALS);
                return tokenInfo;


            }


        } catch (IOException e) {
            String msg = "Error while connecting to token introspect endpoint.";
            LOG.error(msg, e);
            throw new APIKeyMgtException(msg, e);
        } catch (JsonSyntaxException e) {
            String msg = "Error while processing the response returned from token introspect endpoint.";
            LOG.error(msg, e);
            throw new APIKeyMgtException(msg, e);
        } finally {
            if (urlConn != null) {
                urlConn.disconnect();
            }
        }

        return tokenInfo;
    }

    private void setValuesForOAuth2ValidationContext(TokenValidationContext validationContext,
                                                     AccessTokenInfo tokenInfo) {
        // Setting TokenInfo in validationContext. Methods down in the chain can use TokenInfo.
        validationContext.setTokenInfo(tokenInfo);

        APIKeyValidationInfoDTO apiKeyValidationInfoDTO = new APIKeyValidationInfoDTO();

        if (!tokenInfo.isTokenValid()) {
            apiKeyValidationInfoDTO.setAuthorized(false);
            if (tokenInfo.getErrorcode() > 0) {
                apiKeyValidationInfoDTO.setValidationStatus(tokenInfo.getErrorcode());
            } else {
                apiKeyValidationInfoDTO.setValidationStatus(KeyManagerConstants
                        .KeyValidationStatus.API_AUTH_GENERAL_ERROR);
            }
        }

        apiKeyValidationInfoDTO.setAuthorized(tokenInfo.isTokenValid());
        apiKeyValidationInfoDTO.setEndUserName(tokenInfo.getEndUserName());
        apiKeyValidationInfoDTO.setConsumerKey(tokenInfo.getConsumerKey());
        apiKeyValidationInfoDTO.setIssuedTime(tokenInfo.getIssuedTime());
        apiKeyValidationInfoDTO.setValidityPeriod(tokenInfo.getValidityPeriod());

        Set<String> scopeSet = new HashSet<String>(Arrays.asList(tokenInfo.getScopes()));
        apiKeyValidationInfoDTO.setScopes(scopeSet);

        validationContext.setValidationInfoDTO(apiKeyValidationInfoDTO);
    }
}
