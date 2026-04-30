/*
 * Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal;

import lombok.Getter;
import lombok.Setter;
import org.wso2.carbon.apimgt.governance.api.service.APIMGovernanceService;
import org.wso2.carbon.apimgt.impl.APIMDependencyConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Singleton class that holds a reference to the API Manager Configuration. This class ensures that the API Manager
 * Configuration is accessible throughout the component.
 */
public class ServiceReferenceHolder {

    /**
     * -- GETTER --
     *  Returns the singleton instance of
     * .
     *
     */
    @Getter
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfiguration apimConfiguration;
    private APIMDependencyConfigurationService dependencyConfigurationService;
    /**
     * -- GETTER --
     *  Retrieves the current instance of the RealmService.
     *
     *
     * -- SETTER --
     *  Sets the RealmService instance.
     *
     */
    @Setter
    @Getter
    private RealmService realmService;
    private APIMGovernanceService apimGovernanceService;

    /**
     * Private constructor to prevent instantiation.
     */
    private ServiceReferenceHolder() {
    }

    /**
     * Returns the current {@link APIManagerConfiguration} instance.
     *
     * @return The current {@link APIManagerConfiguration} instance, or {@code null} if not set.
     */
    public APIManagerConfiguration getAPIManagerConfiguration() {
        return apimConfiguration;
    }

    /**
     * Sets the API Manager Configuration Service. This method is used to update the internal reference to the
     * {@link APIManagerConfiguration} instance.
     *
     * @param configurationService The API Manager Configuration Service instance, or {@code null} to clear the
     *                             configuration.
     */
    public void setAPIMConfigurationService(APIManagerConfigurationService configurationService) {
        if (configurationService == null) {
            this.apimConfiguration = null;
        } else {
            this.apimConfiguration = configurationService.getAPIManagerConfiguration();
        }
    }

    /**
     * The APIM Governance Service instance.
     *
     */
    public void setAPIMGovernanceService(APIMGovernanceService service) {
        this.apimGovernanceService = service;
    }

    public APIMGovernanceService getAPIMGovernanceService() {
        return apimGovernanceService;
    }

    public void setAPIMDependencyConfigurationService(APIMDependencyConfigurationService service) {
        this.dependencyConfigurationService = service;
    }

    public APIMDependencyConfigurationService getAPIMDependencyConfigurationService() {
        return dependencyConfigurationService;
    }
}
