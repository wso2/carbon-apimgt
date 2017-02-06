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

import org.wso2.carbon.apimgt.authenticator.utils.bean.AuthResponseBean;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * This method authenticate the user.
 *
 */
public class IntrospectService {

    /**
     * This method authenticate the user.
     *
     */
    public AuthResponseBean getAccessTokenData(String userName, String password, String[] scope) {
        AuthResponseBean responseBean = new AuthResponseBean();
        responseBean.setAuthUser(userName);
        responseBean.setCreatedDate(new Date().toString());
        responseBean.setScopes(scope);
        responseBean.setType("Bearer");
        responseBean.setValidityPeriod(3600);
        return responseBean;

    }

    /**
     * This method authenticate the user.
     *
     */
    public String getAccessToken(String userName, String password, String[] scopes) {
        return generateAccessToken(userName, password, scopes);
    }

    private String generateAccessToken(String userName, String password, String[] scopes) {
        Map<String, String> keys = getConsumerKeySecret("publisher");
        Map.Entry entry = keys.entrySet().iterator().next();
        String key = (String) entry.getKey();
        String secret = (String) entry.getValue();
        return kmTokenEndpoint(key, secret, userName, password, scopes);
    }

    private Map<String, String> getConsumerKeySecret(String appName) {
        Map<String, Map<String, String>> appKeys = new HashMap<>();
        Map<String, String> keys = new HashMap<>();
        keys.put("publisher", "ewewer-4324-fwefwe");
        appKeys.put("publisher", keys);
        return appKeys.get(appName);
    }

    private String kmTokenEndpoint(String key, String secret, String username, String password, String[] scopes) {
        String accessToken = key + secret + username + password;
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * accessToken.length());
            salt.append(accessToken.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }
}
