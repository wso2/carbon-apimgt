/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold store/publisher application configurations.
 */
@Configuration(namespace = "wso2.carbon.apimgt.application", description = "APIM Store/Publisher Configuration Parameters")
public class APIMAppConfigurations {
    @Element(description = "APIM Base URL")
    private String apimBaseUrl = "https://localhost:9292/";
    @Element(description = "Authorization Endpoint")
    private String authorizationEndpoint = "https://localhost:9443/oauth2/authorize";
    @Element(description = "SSO Enabled or not")
    private boolean ssoEnabled = false;

    public String getApimBaseUrl() {
        return apimBaseUrl;
    }

    public void setApimBaseUrl(String apimBaseUrl) {
        this.apimBaseUrl = apimBaseUrl;
    }

    public String getAuthorizationEndpoint() {
        return authorizationEndpoint;
    }

    public void setAuthorizationEndpoint(String authorizationEndpoint) {
        this.authorizationEndpoint = authorizationEndpoint;
    }

    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    public void setSsoEnabled(boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }
}
