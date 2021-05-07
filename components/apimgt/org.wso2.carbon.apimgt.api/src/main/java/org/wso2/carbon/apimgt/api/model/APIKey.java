/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.api.model;

public class APIKey {
    private String mappingId;
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String type;
    private String tokenScope;
    private long validityPeriod;
    private String createdDate;
    private String authUser;
    private String authorizedDomains;
    private String state;
    private String callbackUrl;
    private String grantTypes;
    private Object additionalProperties;
    private String keyManager;
    private String appMetaData;
    private String createMode;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
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

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String userId) {
        this.authUser = userId;
    }
    public long getValidityPeriod() {
        return validityPeriod;
    }

    public void setValidityPeriod(long validityPeriod) {
        this.validityPeriod = validityPeriod;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
    public String getTokenScope() {
        return tokenScope;
    }

    public void setTokenScope(String tokenScope) {
        this.tokenScope = tokenScope;
    }

    public String getAuthorizedDomains() {
        return authorizedDomains;
    }

    public void setAuthorizedDomains(String authorizedDomains) {
        this.authorizedDomains = authorizedDomains;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public String getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(String grantTypes) {
        this.grantTypes = grantTypes;
    }

	public Object getAdditionalProperties() {
		return additionalProperties;
	}

	public void setAdditionalProperties(Object additionalProperties) {
		this.additionalProperties = additionalProperties;
	}

    public void setKeyManager(String keyManager) {
        this.keyManager = keyManager;
    }

    public String getKeyManager() {
        return keyManager;
    }

    public String getMappingId() {

        return mappingId;
    }

    public void setMappingId(String mappingId) {

        this.mappingId = mappingId;
    }

    public String getAppMetaData() {

        return appMetaData;
    }

    public void setAppMetaData(String appMetaData) {

        this.appMetaData = appMetaData;
    }

    public String getCreateMode() {
        return createMode;
    }

    public void setCreateMode(String createMode) {
        this.createMode = createMode;
    }
}
