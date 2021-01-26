/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * Data holder for tenant initialization service component
 */
public class ServiceDataHolder {
    private static final ServiceDataHolder instance = new ServiceDataHolder();
    private ConfigurationContextService configurationContextService;
    private APIManagerConfigurationService amConfigurationService;
    private RealmService realmService;

    private ServiceDataHolder() {
    }

    public static ServiceDataHolder getInstance() {
        return instance;
    }

    /**
     * Method to get ConfigurationContextService
     *
     * @return configurationContextService
     */
    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    /**
     * Method to set ConfigurationContextService
     *
     * @param ccService configuration context service
     */
    public void setConfigurationContextService(ConfigurationContextService ccService) {
        this.configurationContextService = ccService;
    }

    /**
     * Method to get APIManagerConfigurationService
     *
     * @return  API Manager Configuration Service
     */
    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    /**
     * Method to set APIManagerConfigurationService
     *
     * @param amConfigurationService API Manager Configuration Service
     */
    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
    }

    /**
     * Method to get RealmService.
     *
     * @return RealmService.
     */
    public RealmService getRealmService() {
        return realmService;
    }

    /**
     * Method to set RealmService.
     *
     * @param service RealmService.
     */
    public void setRealmService(RealmService service) {
        this.realmService = service;
    }

}
