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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.authenticator.utils.AuthUtil;
import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;
import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.util.HashMap;
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
    public AuthResponseBean setAccessTokenData(AuthResponseBean responseBean, AccessTokenInfo accessTokenInfo) {
        responseBean.setTokenValid(true);
        responseBean.setAuthUser(accessTokenInfo.getEndUserName());
        responseBean.setScopes(accessTokenInfo.getScopes());
        responseBean.setType("Bearer");
        responseBean.setValidityPeriod(accessTokenInfo.getValidityPeriod());
        return responseBean;

    }

    /**
     * This method authenticate the user.
     *
     */
    public String getTokens(AuthResponseBean authResponseBean, String appName, String userName, String password,
            String grantType, String refreshToken, String[] scopes, long validityPeriod) throws KeyManagementException {
        //TODO - call method which provides client id and secret.
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        AccessTokenRequest accessTokenRequest = AuthUtil
                .createAccessTokenRequest(userName, password, grantType, refreshToken, null, validityPeriod, scopes,
                        consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
        KeyManager keyManager = KeyManagerHolder.getAMLoginKeyManagerInstance();
        AccessTokenInfo accessTokenInfo = keyManager.getNewApplicationAccessToken(accessTokenRequest);
        setAccessTokenData(authResponseBean, accessTokenInfo);
        authResponseBean.setAuthUser(userName);
        return accessTokenInfo.getAccessToken() + ":" + accessTokenInfo.getRefreshToken();
    }

    public void revokeAccessToken (String appName, String accessToken) throws KeyManagementException {
        Map<String, String> consumerKeySecretMap = getConsumerKeySecret(appName);
        AccessTokenRequest accessTokenRequest = AuthUtil
                .createAccessTokenRequest("", "", "", "", accessToken, 0 , new String[0],
                        consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
        KeyManager keyManager = KeyManagerHolder.getAMLoginKeyManagerInstance();
        keyManager.revokeLogInAccessToken(accessTokenRequest);
    }

    private Map<String, String> getConsumerKeySecret(String appName) throws KeyManagementException {

        HashMap<String, String> consumerKeySecretMap;
        if (!AuthUtil.getConsumerKeySecretMap().containsKey(appName)) {
            consumerKeySecretMap = new HashMap<>();
            KeyManager keyManager = KeyManagerHolder.getAMLoginKeyManagerInstance();
            OAuthAppRequest oauthAppRequest = null;

                oauthAppRequest = AuthUtil
                        .createOauthAppRequest(appName, "admin", null,
                                null);
            //for now tokenSope = null
            oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.VALIDITY_PERIOD, 3600);
            oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.APP_KEY_TYPE, "application");
            OAuthApplicationInfo oAuthApplicationInfo;
            oAuthApplicationInfo = keyManager.createApplication(oauthAppRequest);

            consumerKeySecretMap.put("CONSUMER_KEY", oAuthApplicationInfo.getClientId());
            consumerKeySecretMap.put("CONSUMER_SECRET", oAuthApplicationInfo.getClientSecret());

            AuthUtil.getConsumerKeySecretMap().put(appName, consumerKeySecretMap);
            return consumerKeySecretMap;
        } else {
            return AuthUtil.getConsumerKeySecretMap().get(appName);
        }
    }



}
