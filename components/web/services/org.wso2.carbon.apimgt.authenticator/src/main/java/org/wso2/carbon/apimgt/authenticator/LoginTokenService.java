/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.authenticator;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.core.api.APIDefinition;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.impl.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.models.Scope;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * This method authenticate the user.
 *
 */
public class LoginTokenService {

    private static final Logger log = LoggerFactory.getLogger(LoginTokenService.class);

    /**
     * This method authenticate the user.
     *
     */
    public AuthResponseBean setAccessTokenData(AuthResponseBean responseBean, AccessTokenInfo accessTokenInfo)
            throws KeyManagementException {
        responseBean.setTokenValid(true);
        if (accessTokenInfo.getIdToken() != null) {
            responseBean.setAuthUser(getUsernameFromJWT(accessTokenInfo.getIdToken()));
        }
        responseBean.setScopes(accessTokenInfo.getScopes());
        responseBean.setType("Bearer");
        responseBean.setValidityPeriod(accessTokenInfo.getValidityPeriod());
        responseBean.setIdToken(accessTokenInfo.getIdToken());
        return responseBean;
    }

    /**
     * This method authenticate the user.
     *
     */
    public String getTokens(AuthResponseBean authResponseBean, String appName, String userName, String password,
            String grantType, String refreshToken, long validityPeriod) throws KeyManagementException {
        String scopes = "";
        try {
            String publisherRestAPI = RestApiUtil.getPublisherRestAPIResource();
            String storeRestAPI = RestApiUtil.getStoreRestAPIResource();
            String adminRestAPI = RestApiUtil.getAdminRestAPIResource();
            APIDefinition apiDefinitionFromSwagger20 = new APIDefinitionFromSwagger20();
            Map<String, Scope> publisherScopes = apiDefinitionFromSwagger20.getScopes(publisherRestAPI);
            Map<String, Scope> storeScopes = apiDefinitionFromSwagger20.getScopes(storeRestAPI);
            Map<String, Scope> adminScopes = apiDefinitionFromSwagger20.getScopes(adminRestAPI);
            final StringBuffer allScopes = new StringBuffer();
            publisherScopes.keySet().forEach(key -> {
                allScopes.append(key).append(" ");
            });
            storeScopes.keySet().forEach(key -> {
                allScopes.append(key).append(" ");
            });
            adminScopes.keySet().forEach(key -> {
                allScopes.append(key).append(" ");
            });
            scopes = allScopes.toString();
        } catch (APIManagementException e) {
            throw new KeyManagementException("Error while reading scopes", e);
        }
        //set openid scope
        if (StringUtils.isEmpty(scopes)) {
            scopes = KeyManagerConstants.OPEN_ID_CONNECT_SCOPE;
        } else {
            scopes = scopes + ' ' + KeyManagerConstants.OPEN_ID_CONNECT_SCOPE;
        }

        //TODO - implement a storing mechanism for client_id and secret
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        AccessTokenRequest accessTokenRequest = AuthUtil
                .createAccessTokenRequest(userName, password, grantType, refreshToken, null, validityPeriod, scopes,
                        consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
        AccessTokenInfo accessTokenInfo = APIManagerFactory.getInstance().getKeyManager()
                .getNewAccessToken(accessTokenRequest);
        setAccessTokenData(authResponseBean, accessTokenInfo);
        return accessTokenInfo.getAccessToken() + ":" + accessTokenInfo.getRefreshToken();
    }

    public void revokeAccessToken(String appName, String accessToken) throws KeyManagementException {
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        APIManagerFactory.getInstance().getKeyManager().revokeAccessToken(accessToken,
                consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
    }

    private Map<String, String> getConsumerKeySecret(String appName) throws KeyManagementException {

        HashMap<String, String> consumerKeySecretMap;
        if (!AuthUtil.getConsumerKeySecretMap().containsKey(appName)) {
            consumerKeySecretMap = new HashMap<>();
            List<String> grantTypes = new ArrayList<>();
            grantTypes.add(KeyManagerConstants.PASSWORD_GRANT_TYPE);
            grantTypes.add(KeyManagerConstants.REFRESH_GRANT_TYPE);
            OAuthAppRequest oauthAppRequest = ApplicationUtils.createOauthAppRequest(appName,
                    "http://temporary.callback/url", grantTypes);

            oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.VALIDITY_PERIOD, 3600);
            oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.APP_KEY_TYPE, "application");
            OAuthApplicationInfo oAuthApplicationInfo;
            oAuthApplicationInfo = APIManagerFactory.getInstance().getKeyManager().createApplication(oauthAppRequest);

            consumerKeySecretMap.put("CONSUMER_KEY", oAuthApplicationInfo.getClientId());
            consumerKeySecretMap.put("CONSUMER_SECRET", oAuthApplicationInfo.getClientSecret());

            AuthUtil.getConsumerKeySecretMap().put(appName, consumerKeySecretMap);
            return consumerKeySecretMap;
        } else {
            return AuthUtil.getConsumerKeySecretMap().get(appName);
        }
    }

    private String getUsernameFromJWT(String jwt) throws KeyManagementException {
        if (jwt != null && jwt.contains(".")) {
            String[] jwtParts = jwt.split("\\.");
            JWTTokenPayload jwtHeader = new Gson().fromJson(new String(Base64.getDecoder().decode(jwtParts[1]),
                    StandardCharsets.UTF_8), JWTTokenPayload.class);

            //removing "@carbon.super" part explicitly (until IS side is fixed to drop it)
            String username = jwtHeader.getSub();
            username = username.replace("@carbon.super", "");
            return username;
        } else {
            log.error("JWT Parsing failed. Invalid JWT: " + jwt);
            throw new KeyManagementException("JWT Parsing failed. Invalid JWT.");
        }
    }

    /**
     * Represents Payload of JWT
     */
    private class JWTTokenPayload {
        private String sub;
        private String iss;
        private String exp;
        private String iat;
        private String[] aud;

        public String getSub() {
            return sub;
        }

        public String getIss() {
            return iss;
        }

        public String getExp() {
            return exp;
        }

        public String getIat() {
            return iat;
        }

        public String[] getAud() {
            return aud;
        }
    }
}
