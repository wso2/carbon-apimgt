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

package org.wso2.carbon.apimgt.core.models;

import java.util.HashMap;

/**
 * Representation of a Token Generation Request.
 */
public class AccessTokenRequest {
    private String clientId;
    private String clientSecret;
    private String grantType;
    private String[] scopes = null;
    private String callbackURI;
    private String resourceOwnerUsername;
    private String resourceOwnerPassword;
    private String refreshToken;
    private long validityPeriod;
    private String tokenToRevoke;

    // This map can be used to store additional properties not captured by above list of fields.
    private HashMap<String, Object> requestParameters = new HashMap<String, Object>();


    public String getTokenToRevoke() {
        return tokenToRevoke;
    }

    public void setTokenToRevoke(String tokenToRevoke) {
        this.tokenToRevoke = tokenToRevoke;
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

    public String[] getScopes() {
        if (scopes != null) {
            return scopes.clone();
        } else {
            return new String[0];
        }
    }

    public void setScopes(String[] scope) {
        this.scopes = scope.clone();
    }

    public String getCallbackURI() {
        return callbackURI;
    }

    public void setCallbackURI(String callbackURI) {
        this.callbackURI = callbackURI;
    }

    public long getValidityPeriod() {
        return this.validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getResourceOwnerUsername() {
        return resourceOwnerUsername;
    }

    public void setResourceOwnerUsername(String resourceOwnerUsername) {
        this.resourceOwnerUsername = resourceOwnerUsername;
    }

    public String getResourceOwnerPassword() {
        return resourceOwnerPassword;
    }

    public void setResourceOwnerPassword(String resourceOwnerPassword) {
        this.resourceOwnerPassword = resourceOwnerPassword;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void addRequestParam(String paramName, Object paramValue) {
        requestParameters.put(paramName, paramValue);
    }

    public Object getRequestParam(String key) {
        return requestParameters.get(key);
    }


}

