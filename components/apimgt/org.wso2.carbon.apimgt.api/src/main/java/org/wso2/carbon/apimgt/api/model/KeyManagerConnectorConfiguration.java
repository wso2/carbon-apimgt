/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.APIConstants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This Interface providing functionality to register KeyManagerConnector Related Configurations
 */
public interface KeyManagerConnectorConfiguration {

    /**
     * This method returns the KeyManager implementation class name
     *
     * @return keymanager implementation class name
     */
    public String getImplementation();

    /**
     * This method returns JWTValidator class name if defined.
     *
     * @return JWTValidator class name
     */
    public String getJWTValidator();

    /**
     * This method returns the Configurations related to keymanager registration
     *
     * @return
     */
    public List<ConfigurationDto> getConnectionConfigurations();

    /**
     * This method returns the Configurations related to Oauth Application Creation
     *
     * @return
     */
    public List<ConfigurationDto> getApplicationConfigurations();

    /**
     * This method used to get Type
     */
    public String getType();

    /**
     * This method used to get Disaply Name
     */
    public default String getDisplayName() {

        return getType();
    }

    public default String getDefaultScopesClaim() {

        return "";
    }

    public default String getDefaultConsumerKeyClaim() {

        return "";
    }
    
    /**
     * This method returns keymanager endpoint configurations.
     */
    public default List<ConfigurationDto> getEndpointConfigurations() {

        List<ConfigurationDto> configurationDtos = new ArrayList<>();
        configurationDtos.add(new ConfigurationDto("client_registration_endpoint", "Client Registration Endpoint",
                "input", String.format("E.g.,%s/client-registration/v0.17/register",
                APIConstants.DEFAULT_KEY_MANAGER_HOST), "", true, false, Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("introspection_endpoint", "Introspection Endpoint", "input",
                String.format("E.g., %s/oauth2/introspect", APIConstants.DEFAULT_KEY_MANAGER_HOST), "", true, false, Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("token_endpoint", "Token Endpoint", "input",
                String.format("E.g., %s/oauth2/token", APIConstants.DEFAULT_KEY_MANAGER_HOST), ""
                , true, false, Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("revoke_endpoint", "Revoke Endpoint", "input",
                String.format("E.g., %s/oauth2/revoke", APIConstants.DEFAULT_KEY_MANAGER_HOST), "", true, false,
                Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("userinfo_endpoint", "UserInfo Endpoint", "input",
                String.format("E.g., %s/oauth2/userinfo", APIConstants.DEFAULT_KEY_MANAGER_HOST), "", false, false,
                Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("authorize_endpoint", "Authorize Endpoint", "input",
                String.format("E.g., %s/oauth2/authorize",APIConstants.DEFAULT_KEY_MANAGER_HOST), "", false, false, Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("display_token_endpoint", "Display Token Endpoint", "input",
                String.format("E.g., %s/oauth2/token",APIConstants.DEFAULT_KEY_MANAGER_HOST), "", false, false, Collections.EMPTY_LIST, false));
        configurationDtos.add(new ConfigurationDto("display_revoke_endpoint", "Display Revoke Endpoint", "input",
                String.format("E.g., %s/oauth2/authorize", APIConstants.DEFAULT_KEY_MANAGER_HOST), "", false, false,
                Collections.EMPTY_LIST, false));
        return configurationDtos;
    }
}
