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

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * Singleton class that holds a reference to the API Manager Configuration. This class ensures that the API Manager
 * Configuration is accessible throughout the component.
 */
public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfiguration apimConfiguration;
    private RealmService realmService;

    /**
     * Returns the singleton instance of {@code ServiceReferenceHolder}.
     *
     * @return The singleton instance of {@code ServiceReferenceHolder}.
     */
    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

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
     * Retrieves the current instance of the RealmService.
     *
     * @return the RealmService instance
     */
    public RealmService getRealmService() {
        return realmService;
    }

    /**
     * Sets the RealmService instance.
     *
     * @param realmService the RealmService to be set
     */
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
}
