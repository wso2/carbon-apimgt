/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.configuration.models;

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold identity provider configurations
 */
@Configuration(description = "Identity Provider Configurations")
public class IdentityProviderConfigurations {

    @Element(description = "Identity Provider Implementation class")
    private String identityProviderImplClass = "org.wso2.carbon.apimgt.core.impl.DefaultIdentityProviderImpl";
    @Element(description = "IDP Base URL")
    private String identityProviderBaseUrl = "https://localhost:9443";
    @Element(description = "Identity Provider Credentials")
    private CredentialConfigurations identityProviderCredentials = new CredentialConfigurations();
    @Element(description = "Alias of Identity Provider Certificate in Client Trust Store")
    private String idpCertAlias = "wso2carbon";

    public String getIdentityProviderImplClass() {
        return identityProviderImplClass;
    }

    public void setIdentityProviderImplClass(String identityProviderImplClass) {
        this.identityProviderImplClass = identityProviderImplClass;
    }

    public String getIdentityProviderBaseUrl() {
        return identityProviderBaseUrl;
    }

    public void setIdentityProviderBaseUrl(String identityProviderBaseUrl) {
        this.identityProviderBaseUrl = identityProviderBaseUrl;
    }

    public CredentialConfigurations getIdentityProviderCredentials() {
        return identityProviderCredentials;
    }

    public void setIdentityProviderCredentials(CredentialConfigurations identityProviderCredentials) {
        this.identityProviderCredentials = identityProviderCredentials;
    }

    public String getIdpCertAlias() {
        return idpCertAlias;
    }

    public void setIdpCertAlias(String idpCertAlias) {
        this.idpCertAlias = idpCertAlias;
    }
}
