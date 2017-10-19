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
import java.util.Map;
import java.util.Objects;

/**
 * Details about an Access Token.
 */
public class AccessTokenInfo {
    private boolean isTokenValid;

    private boolean isApplicationToken;

    private String consumerKey;

    private String consumerSecret;

    private String scopes;

    private String tokenState;

    private String accessToken;

    private String refreshToken;

    private String idToken;

    private long issuedTime;

    private long expiryTime;

    private long validityPeriod;

    private int errorCode;

    private String endUserName;

    private Map<String, Object> parameters = new HashMap<>();

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getScopes() {
        return scopes;
    }

    public void setScopes(String scopes) {
        this.scopes = scopes;
    }

    public String getTokenState() {
        return tokenState;
    }

    public void setTokenState(String tokenState) {
        this.tokenState = tokenState;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public long getIssuedTime() {
        return issuedTime;
    }

    public long getValidityPeriod() {
        return validityPeriod;
    }

    public boolean isTokenValid() {
        return isTokenValid;
    }

    public void setTokenValid(boolean tokenValid) {
        isTokenValid = tokenValid;
    }

    public boolean isApplicationToken() {
        return isApplicationToken;
    }

    public void setApplicationToken(boolean applicationToken) {
        isApplicationToken = applicationToken;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public void setIssuedTime(long issuedTime) {
        this.issuedTime = issuedTime;
    }

    public long getExpiryTime() {
        return expiryTime;
    }

    public void setExpiryTime(long expiryTime) {
        this.expiryTime = expiryTime;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getEndUserName() {
        return endUserName;
    }

    public void setEndUserName(String endUserName) {
        this.endUserName = endUserName;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public Object getParameter(String paramName) {
        return parameters.get(paramName);
    }

    public void addParameter(String paramName, Object paramValue) {
        parameters.put(paramName, paramValue);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccessTokenInfo)) {
            return false;
        }
        AccessTokenInfo that = (AccessTokenInfo) o;
        return isTokenValid == that.isTokenValid &&
                isApplicationToken == that.isApplicationToken &&
                issuedTime == that.issuedTime &&
                expiryTime == that.expiryTime &&
                validityPeriod == that.validityPeriod &&
                Objects.equals(consumerKey, that.consumerKey) &&
                Objects.equals(consumerSecret, that.consumerSecret) &&
                Objects.equals(scopes, that.scopes) &&
                Objects.equals(tokenState, that.tokenState) &&
                Objects.equals(accessToken, that.accessToken) &&
                Objects.equals(refreshToken, that.refreshToken) &&
                Objects.equals(idToken, that.idToken) &&
                Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(endUserName, that.endUserName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isTokenValid, isApplicationToken, consumerKey, consumerSecret, scopes, tokenState,
                accessToken, refreshToken, idToken, issuedTime, expiryTime, validityPeriod, endUserName);
    }
}
