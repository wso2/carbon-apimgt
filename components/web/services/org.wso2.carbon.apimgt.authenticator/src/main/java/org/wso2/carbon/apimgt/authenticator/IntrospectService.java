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

import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.KeyManagementException;
import org.wso2.carbon.apimgt.core.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.core.models.AccessTokenInfo;
import org.wso2.carbon.apimgt.core.models.AccessTokenRequest;


import org.wso2.carbon.apimgt.core.models.OAuthAppRequest;
import org.wso2.carbon.apimgt.core.models.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.core.util.ApplicationUtils;
import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;

import java.util.HashMap;
import java.util.Map;


/**
 * This method authenticate the user.
 *
 */
public class IntrospectService {

    private static final Logger log = LoggerFactory.getLogger(IntrospectService.class);

    /**
     * This method authenticate the user.
     *
     */
    public AuthResponseBean setAccessTokenData(AuthResponseBean responseBean, AccessTokenInfo accessTokenInfo) {
        responseBean.setTokenValid(true);
        responseBean.setAccessToken(accessTokenInfo.getAccessToken());
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
    public String getAccessToken(AuthResponseBean authResponseBean, String userName, String password, String[] scopes)
            throws APIManagementException {
        //TODO - call method which provides client id and secret.

        Map<String, String> consumerKeySecretMap = getConsumerKeySecret("publisher");
        AccessTokenRequest accessTokenRequest = AuthUtil
                .createAccessTokenRequest(userName, password, "password", scopes,
                        consumerKeySecretMap.get("CONSUMER_KEY"), consumerKeySecretMap.get("CONSUMER_SECRET"));
        try {
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            AccessTokenInfo accessTokenInfo = keyManager.getNewApplicationAccessToken(accessTokenRequest);
            setAccessTokenData(authResponseBean, accessTokenInfo);
            return accessTokenInfo.getAccessToken();
        } catch (KeyManagementException e) {
            log.error("Error while getting access token for user " + userName, e);
        }
        return null;
    }

    /*private String generateAccessToken(String userName, String password, String[] scopes) {
        Map<String, String> keys = getConsumerKeySecret("publisher");
        Map.Entry entry = keys.entrySet().iterator().next();
        String key = (String) entry.getKey();
        String secret = (String) entry.getValue();
        return kmTokenEndpoint(key, secret, userName, password, scopes);
    }*/

    /**
     * This method will return map of consumer key and secret
     * @return Consumer key and secret.
     */


    private Map<String, String> getConsumerKeySecret(String appName) throws APIManagementException {

        HashMap<String, String> consumerKeySecretMap;
        if (AuthUtil.getConsumerKeySecretMap() == null) {
            consumerKeySecretMap = new HashMap<>();
            KeyManager keyManager = KeyManagerHolder.getAMLoginKeyManagerInstance();
            OAuthAppRequest oauthAppRequest = null;

                oauthAppRequest = ApplicationUtils
                        .createOauthAppRequest(appName, "ADMIN", null,
                                null);
            //for now tokenSope = null
            oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.VALIDITY_PERIOD, 3600);
            oauthAppRequest.getOAuthApplicationInfo().addParameter(KeyManagerConstants.APP_KEY_TYPE, "application");
            OAuthApplicationInfo oAuthApplicationInfo;
            oAuthApplicationInfo = keyManager.createApplication(oauthAppRequest);

            consumerKeySecretMap.put("CONSUMER_KEY", oAuthApplicationInfo.getClientId());
            consumerKeySecretMap.put("CONSUMER_SECRET", oAuthApplicationInfo.getClientSecret());

            AuthUtil.setConsumerKeySecretMap(consumerKeySecretMap);
        }

        return AuthUtil.getConsumerKeySecretMap();

//        Map<String, Map<String, String>> appKeys = new HashMap<>();
//        Map<String, String> keys = new HashMap<>();
//        keys.put("publisher", "ewewer-4324-fwefwe");
//        appKeys.put("publisher", keys);
//        return appKeys.get(appName);
    }

//    private String kmTokenEndpoint(String key, String secret, String username, String password, String[] scopes) {
//        String accessToken = key + secret + username + password;
//        StringBuilder salt = new StringBuilder();
//        Random rnd = new Random();
//        while (salt.length() < 18) {
//            int index = (int) (rnd.nextFloat() * accessToken.length());
//            salt.append(accessToken.charAt(index));
//        }
//        String saltStr = salt.toString();
//        return saltStr;
//    }
}
