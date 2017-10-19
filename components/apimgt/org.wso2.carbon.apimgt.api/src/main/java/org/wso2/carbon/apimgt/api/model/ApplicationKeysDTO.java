/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.apimgt.api.model;

/**
 *
 */
public class ApplicationKeysDTO {
    private String applicationAccessToken;
    private String consumerKey;
    private String consumerSecret;
    private String[] accessAllowDomains;
    private String validityTime;
    private String tokenScope;
    private String grantTypes;
    private String callbackUrl;
    
    public String getApplicationAccessToken() {
        return applicationAccessToken;
    }

    public void setApplicationAccessToken(String applicationAccessToken) {
        this.applicationAccessToken = applicationAccessToken;
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

    public String[] getAccessAllowDomains() {
        return accessAllowDomains;
    }

    public void setAccessAllowDomains(String[] accessAllowDomains) {
        this.accessAllowDomains = accessAllowDomains;
    }

	public String getValidityTime() {
	    return validityTime;
    }

	public void setValidityTime(String validityTime) {
	    this.validityTime = validityTime;
    }

    public String getTokenScope() {
        return tokenScope;
    }

    public void setTokenScope(String tokenScope) {
        this.tokenScope = tokenScope;
    }

    public String getGrantTypes() {
        return grantTypes;
    }

    public void setGrantTypes(String grantTypes) {
        this.grantTypes = grantTypes;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }
}
