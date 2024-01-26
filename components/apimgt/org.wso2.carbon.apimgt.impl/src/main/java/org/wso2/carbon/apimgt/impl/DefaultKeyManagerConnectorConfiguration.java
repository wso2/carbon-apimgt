/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidatorImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component(
        name = "default.km.configuration.component",
        immediate = true,
        service = KeyManagerConnectorConfiguration.class
)
/**
 *  This is to register default key manager as connector
 */
public class DefaultKeyManagerConnectorConfiguration implements KeyManagerConnectorConfiguration {

    @Override
    public String getImplementation() {

        return AMDefaultKeyManagerImpl.class.getName();
    }

    @Override
    public String getJWTValidator() {

        return JWTValidatorImpl.class.getName();
    }

    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {

        return Collections.emptyList();
    }

    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {
        List<ConfigurationDto> applicationConfigurationsList = new ArrayList();
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.APPLICATION_ACCESS_TOKEN_EXPIRY_TIME,
                        "Application Access Token Expiry Time ", "input", "Type Application Access Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.USER_ACCESS_TOKEN_EXPIRY_TIME,
                        "User Access Token Expiry Time ", "input", "Type User Access Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.REFRESH_TOKEN_EXPIRY_TIME,
                        "Refresh Token Expiry Time ", "input", "Type Refresh Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));
        applicationConfigurationsList
                .add(new ConfigurationDto(APIConstants.KeyManager.ID_TOKEN_EXPIRY_TIME,
                        "Id Token Expiry Time", "input", "Type ID Token Expiry Time " +
                        "in seconds ", APIConstants.KeyManager.NOT_APPLICABLE_VALUE, false, false,
                        Collections.EMPTY_LIST, false));

        ConfigurationDto configurationDtoPkceMandatory = new ConfigurationDto(APIConstants.KeyManager.PKCE_MANDATORY,
                "Enable PKCE", "checkbox", "Enable PKCE", String.valueOf(false), false, false,
                Collections.EMPTY_LIST, false);
        applicationConfigurationsList.add(configurationDtoPkceMandatory);

        ConfigurationDto configurationDtoPkcePlainText = new ConfigurationDto(APIConstants.KeyManager.PKCE_SUPPORT_PLAIN,
                "Support PKCE Plain text", "checkbox", "S256 is recommended, plain text too can be used."
                , String.valueOf(false), false, false, Collections.EMPTY_LIST, false);
        applicationConfigurationsList.add(configurationDtoPkcePlainText);

        ConfigurationDto configurationDtoBypassClientCredentials = new ConfigurationDto(APIConstants.KeyManager.BYPASS_CLIENT_CREDENTIALS,
                "Public client", "checkbox", "Allow authentication without the client secret."
                , String.valueOf(false), false, false, Collections.EMPTY_LIST, false);
        applicationConfigurationsList.add(configurationDtoBypassClientCredentials);
        return applicationConfigurationsList;
    }

    @Override
    public String getType() {

        return APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE;
    }

    @Override
    public String getDefaultScopesClaim() {

        return APIConstants.JwtTokenConstants.SCOPE;
    }

    @Override
    public String getDefaultConsumerKeyClaim() {

        return APIConstants.JwtTokenConstants.AUTHORIZED_PARTY;
    }
}
