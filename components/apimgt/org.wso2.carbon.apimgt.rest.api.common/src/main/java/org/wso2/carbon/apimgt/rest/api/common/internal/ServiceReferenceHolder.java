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
        return apimConfiguration;
    }

    public void setAPIMConfigurationService(APIManagerConfigurationService configurationService) {
        if (configurationService == null) {
            this.apimConfiguration = null;
        } else {
            this.apimConfiguration = configurationService.getAPIManagerConfiguration();
        }
    }

    public Map<String, JWTValidator> getJwtValidatorMap() {
        return jwtValidatorMap;
    }

    public void setJwtValidatorMap(Map<String, JWTValidator> jwtValidatorMap) {
        this.jwtValidatorMap = jwtValidatorMap;
    }

    public void addAuthenticator(RestAPIAuthenticator authenticator) {
        this.authenticators.add(authenticator);
    }

    public void removeAuthenticator(RestAPIAuthenticator authenticator) {
        this.authenticators.remove(authenticator);
    }

    public List<RestAPIAuthenticator> getAuthenticators() {
        return authenticators;
    }
}
