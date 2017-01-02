package org.wso2.carbon.apimgt.core;
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

import org.wso2.carbon.apimgt.core.util.KeyManagerConstants;
import org.wso2.carbon.kernel.annotations.Configuration;
import org.wso2.carbon.kernel.annotations.Element;

/**
 * Class to hold APIM configuration parameters and generate yaml file
 * TODO refactor class when kernal is updated to 5.2.0
 */
@Configuration(namespace = "wso2.carbon.kmgt", description = "Key Management Configuration Parameters")
public class KeyMgtConfigurations {

    @Element(description = "DCR Endpoint URL")
    private String dcrEndpoint;
    @Element(description = "Token Endpoint URL")
    private String tokenEndpoint;
    @Element(description = "Revoke Endpoint URL")
    private String revokeEndpoint;
    @Element(description = "Introspect Endpoint URL")
    private String introspectEndpoint;
    @Element(description = "OAuth app validity period")
    private String validityPeriod;


    public KeyMgtConfigurations() {
        dcrEndpoint = System.getProperty(KeyManagerConstants.DCR_ENDPOINT, "http://localhost:9763/identity/connect/register");
        tokenEndpoint = System.getProperty(KeyManagerConstants.TOKEN_ENDPOINT, "https://localhost:9443/oauth2/token");
        revokeEndpoint = System.getProperty(KeyManagerConstants.REVOKE_ENDPOINT, "https://localhost:9443/oauth2/revoke");
        introspectEndpoint = System.getProperty(KeyManagerConstants.INTROSPECT_ENDPOINT, "http://localhost:9763/oauth2/introspect");
        validityPeriod = System.getProperty(KeyManagerConstants.VALIDITY_PERIOD, KeyManagerConstants.DEFAULT_VALIDITY_PERIOD);
    }

    @Element(description = "Key Management Implementation class")
    private String keyManagerClientImpl = "org.wso2.carbon.apimgt.core.impl.AMDefaultKeyManagerImpl";

    public String getKeyManagerClientImpl() {
        return keyManagerClientImpl;
    }

    public String getDcrurl() {
        return dcrEndpoint;
    }

    public String getTokenIntrospectURL() {
        return introspectEndpoint;
    }

    public String getTokenURL() {
        return tokenEndpoint;
    }

    public String getRevokeURL() {
        return revokeEndpoint;
    }

    public String getValidityPeriod() {
        return validityPeriod;
    }
}
