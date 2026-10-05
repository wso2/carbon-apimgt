/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.apimgt.api.model.ConfigurationDto;
import org.wso2.carbon.apimgt.api.model.KeyManagerConnectorConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration class for the Custom Key Manager connector.
 *
 * Defines the UI configuration that appears in the WSO2 Admin Portal when adding
 * a new Key Manager of type "Custom Key Manager". This includes endpoint configurations
 * and claim mappings.
 *
 * Since this connector uses Out-of-Band (OOB) provisioning, no DCR-related
 * configurations are required.
 */
@Component(
        name = "custom.km.configuration.component",
        immediate = true,
        service = KeyManagerConnectorConfiguration.class
)
public class CustomKeyManagerConnectorConfiguration implements KeyManagerConnectorConfiguration {

    /**
     * Returns the fully qualified class name of CustomKeyManagerOAuthClient.
     */
    @Override
    public String getImplementation() {

        return CustomKeyManagerOAuthClient.class.getName();
    }

    /**
     * Returns null to use the default JWT validator.
     */
    @Override
    public String getJWTValidator() {

        return null;
    }

    /**
     * No additional connection configurations are needed for OOB mode.
     * Returns an empty list.
     */
    @Override
    public List<ConfigurationDto> getConnectionConfigurations() {

        return Collections.emptyList();
    }

    /**
     * No additional application configurations are needed for OOB mode.
     * Returns an empty list.
     */
    @Override
    public List<ConfigurationDto> getApplicationConfigurations() {

        return Collections.emptyList();
    }

    /**
     * Returns the unique identifier for this Key Manager type.
     */
    @Override
    public String getType() {

        return APIConstants.KeyManager.CUSTOM_KM_TYPE;
    }

    /**
     * Returns the display name shown in the Admin Portal.
     */
    @Override
    public String getDisplayName() {

        return APIConstants.KeyManager.CUSTOM_KM_DISPLAY_NAME;
    }

    /**
     * Returns the JWT claim name for scopes (default: "scope").
     */
    @Override
    public String getDefaultScopesClaim() {

        return APIConstants.JwtTokenConstants.SCOPE;
    }

    /**
     * Returns the JWT claim name for consumer key/client ID (default: "azp").
     */
    @Override
    public String getDefaultConsumerKeyClaim() {

        return APIConstants.JwtTokenConstants.AUTHORIZED_PARTY;
    }

    /**
     * Defines the OAuth endpoint configurations displayed in the Admin Portal:
     * - Token Endpoint - for obtaining access tokens
     * - Revoke Endpoint - for revoking tokens
     * - Authorize Endpoint - for authorization code flow
     *
     * Returns list of endpoint configuration fields.
     */
    @Override
    public List<ConfigurationDto> getEndpointConfigurations() {

        List<ConfigurationDto> configurationDtos = new ArrayList<>();
        configurationDtos.add(new ConfigurationDto("token_endpoint", "Token Endpoint", "input",
                String.format("E.g., %s/oauth2/token", APIConstants.KeyManager.DEFAULT_KEY_MANAGER_HOST),
                StringUtils.EMPTY, true, false, Collections.emptyList(), false, false));
        configurationDtos.add(new ConfigurationDto("revoke_endpoint", "Revoke Endpoint", "input",
                String.format("E.g., %s/oauth2/revoke", APIConstants.KeyManager.DEFAULT_KEY_MANAGER_HOST),
                StringUtils.EMPTY, true, false, Collections.emptyList(), false, false));
        configurationDtos.add(new ConfigurationDto("authorize_endpoint", "Authorize Endpoint", "input",
                String.format("E.g., %s/oauth2/authorize",APIConstants.KeyManager.DEFAULT_KEY_MANAGER_HOST),
                StringUtils.EMPTY, false, false, Collections.emptyList(), false, false));
        configurationDtos.add(new ConfigurationDto("display_token_endpoint", "Display Token Endpoint", "input",
                String.format("E.g., %s/oauth2/token",APIConstants.KeyManager.DEFAULT_KEY_MANAGER_HOST),
                StringUtils.EMPTY, false, false, Collections.emptyList(), false, false));
        configurationDtos.add(new ConfigurationDto("display_revoke_endpoint", "Display Revoke Endpoint", "input",
                String.format("E.g., %s/oauth2/revoke", APIConstants.KeyManager.DEFAULT_KEY_MANAGER_HOST),
                StringUtils.EMPTY, false, false, Collections.emptyList(), false, false));
        return configurationDtos;
    }
}
