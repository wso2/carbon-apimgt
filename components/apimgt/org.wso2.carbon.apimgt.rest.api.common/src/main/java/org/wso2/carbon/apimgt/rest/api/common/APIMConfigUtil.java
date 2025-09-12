/*
 *
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.rest.api.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.common.gateway.dto.TokenIssuerDto;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;

import java.util.List;
import java.util.Map;

public class APIMConfigUtil {

    private static final Log log = LogFactory.getLog(APIMConfigUtil.class);
    private static APIManagerConfiguration configuration = getApiManagerConfiguration();

    /**
     * This is to get JWT audiences with basepaths from deployment.toml file
     * @return List of Audiences with basepath
     */
    public static Map<String, List<String>> getRestApiJWTAuthAudiences() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving REST API JWT authentication audiences from configuration");
        }
        if (configuration == null) {
            log.warn("API Manager configuration is null when retrieving JWT auth audiences");
            return null;
        }
        Map<String, List<String>> audiences = configuration.getRestApiJWTAuthAudiences();
        if (audiences == null) {
            log.warn("JWT auth audiences configuration is null");
        }
        return audiences;
    }

    /**
     * This is to get JWT issuer details from deployment.toml file
     * @return Map<issuer, tokenIssuerDto>
     */
    public static Map<String, TokenIssuerDto> getTokenIssuerMap() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving token issuer map from JWT configuration");
        }
        if (configuration == null) {
            log.warn("API Manager configuration is null when retrieving token issuer map");
            return null;
        }
        if (configuration.getJwtConfigurationDto() == null) {
            log.warn("JWT configuration DTO is null when retrieving token issuer map");
            return null;
        }
        return configuration.getJwtConfigurationDto().getTokenIssuerDtoMap();
    }

    /**
     * @return APIManagerConfiguration
     */
    private static APIManagerConfiguration getApiManagerConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API Manager configuration from service reference holder");
        }
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        if (config == null) {
            log.error("Failed to retrieve API Manager configuration from service reference holder");
        } else {
            log.info("Successfully retrieved API Manager configuration");
        }
        return config;
    }

    public static  Map<String, JWTValidator> getJWTValidatorMap (){
        if (log.isDebugEnabled()) {
            log.debug("Retrieving JWT validator map from service reference holder");
        }
        Map<String, JWTValidator> validatorMap = ServiceReferenceHolder.getInstance().getJwtValidatorMap();
        if (validatorMap == null) {
            log.warn("JWT validator map is null from service reference holder");
        }
        return validatorMap;
    }
}
