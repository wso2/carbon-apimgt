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

package org.wso2.carbon.apimgt.rest.api.common.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.jwt.JWTValidator;
import org.wso2.carbon.apimgt.rest.api.common.RestAPIAuthenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class implemented for retrieving APIM configurations related to REST APIs
 */
public class ServiceReferenceHolder {

    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfiguration apimConfiguration;

    private Map<String, JWTValidator> jwtValidatorMap;

    private List<RestAPIAuthenticator> authenticators = new ArrayList<>();

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    private ServiceReferenceHolder() {
    }

    public APIManagerConfiguration getAPIMConfiguration() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API Manager configuration");
        }
        if (apimConfiguration == null) {
            log.warn("API Manager configuration is null");
        }
        return apimConfiguration;
    }

    public void setAPIMConfigurationService(APIManagerConfigurationService configurationService) {
        if (configurationService == null) {
            log.warn("API Manager configuration service is null, setting configuration to null");
            this.apimConfiguration = null;
        } else {
            log.info("Setting API Manager configuration service");
            this.apimConfiguration = configurationService.getAPIManagerConfiguration();
            if (this.apimConfiguration != null) {
                log.info("Successfully set API Manager configuration");
            } else {
                log.warn("API Manager configuration is null from configuration service");
            }
        }
    }

    public Map<String, JWTValidator> getJwtValidatorMap() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving JWT validator map");
        }
        return jwtValidatorMap;
    }

    public void setJwtValidatorMap(Map<String, JWTValidator> jwtValidatorMap) {
        if (log.isDebugEnabled()) {
            log.debug("Setting JWT validator map");
        }
        this.jwtValidatorMap = jwtValidatorMap;
        if (jwtValidatorMap == null) {
            log.warn("JWT validator map is set to null");
        } else {
            log.info("Successfully set JWT validator map with " + jwtValidatorMap.size() + " validators");
        }
    }

    public void addAuthenticator(RestAPIAuthenticator authenticator) {
        if (authenticator == null) {
            log.warn("Attempted to add null authenticator");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Adding REST API authenticator: " + authenticator.getClass().getSimpleName());
        }
        this.authenticators.add(authenticator);
        log.info("Added REST API authenticator. Total authenticators: " + authenticators.size());
    }

    public void removeAuthenticator(RestAPIAuthenticator authenticator) {
        if (authenticator == null) {
            log.warn("Attempted to remove null authenticator");
            return;
        }
        if (log.isDebugEnabled()) {
            log.debug("Removing REST API authenticator: " + authenticator.getClass().getSimpleName());
        }
        boolean removed = this.authenticators.remove(authenticator);
        if (removed) {
            log.info("Removed REST API authenticator. Total authenticators: " + authenticators.size());
        } else {
            log.warn("Failed to remove REST API authenticator - not found in list");
        }
    }

    public List<RestAPIAuthenticator> getAuthenticators() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving REST API authenticators. Count: " + authenticators.size());
        }
        return authenticators;
    }
}
