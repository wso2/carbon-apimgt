package org.wso2.carbon.apimgt.core.configuration.models;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold key manager configurations
 */
@Configuration(description = "Key Management Configurations")
public class KeyMgtConfigurations {

    @Element(description = "Key Manager Implementation class")
    private String keyManagerImplClass = "org.wso2.carbon.apimgt.core.impl.DefaultKeyManagerImpl";
    @Element(description = "DCR Endpoint URL")
    private String dcrEndpoint = "http://localhost:9763/identity/connect/register";
    @Element(description = "Token Endpoint URL")
    private String tokenEndpoint = "https://localhost:9443/oauth2/token";
    @Element(description = "Revoke Endpoint URL")
    private String revokeEndpoint = "https://localhost:9443/oauth2/revoke";
    @Element(description = "Introspect Endpoint URL")
    private String introspectEndpoint = "http://localhost:9763/oauth2/introspect";
    @Element(description = "OAuth app validity period")
    private long defaultTokenValidityPeriod = 3600L;
    @Element(description = "Key manager Credentials")
    private CredentialConfigurations keyManagerCredentials = new CredentialConfigurations();

    public String getKeyManagerImplClass() {
        return keyManagerImplClass;
    }

    public String getDcrEndpoint() {
        return dcrEndpoint;
    }

    public String getTokenEndpoint() {
        return tokenEndpoint;
    }

    public String getRevokeEndpoint() {
        return revokeEndpoint;
    }

    public String getIntrospectEndpoint() {
        return introspectEndpoint;
    }

    public long getDefaultTokenValidityPeriod() {
        return defaultTokenValidityPeriod;
    }

    public CredentialConfigurations getKeyManagerCredentials() {
        return keyManagerCredentials;
    }
}
