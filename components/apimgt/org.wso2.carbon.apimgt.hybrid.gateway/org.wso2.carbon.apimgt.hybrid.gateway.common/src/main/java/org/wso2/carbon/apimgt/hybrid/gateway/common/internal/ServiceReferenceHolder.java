/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hybrid.gateway.common.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.hybrid.gateway.common.OnPremiseGatewayInitListener;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

/**
 * Service Reference Holder
 */
public class ServiceReferenceHolder {
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private RealmService realmService;
    private APIManagerConfigurationService amConfigurationService;
    private ConfigurationContextService configContextService;
    private RegistryService registryService;
    private List<OnPremiseGatewayInitListener> listeners = new ArrayList<>();

    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    /**
     * Method to set ConfigurationContextService
     *
     * @param configContextService configuration context service
     */
    public void setConfigContextService(ConfigurationContextService configContextService) {
        this.configContextService = configContextService;
    }

    /**
     * Method to get ConfigurationContextService
     *
     * @return configurationContextService
     */
    public ConfigurationContextService getConfigContextService() {
        return configContextService;
    }

    /**
     * Method to get Realm service
     *
     * @return Realm Service
     */
    public RealmService getRealmService() {
        return realmService;
    }

    /**
     * Method to set Realm service
     *
     * @param  realmService Realm Service
     */
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
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
     * Method to get on premise gateway listeners
     *
     * @return on premise gateway listeners
     */
    public List<OnPremiseGatewayInitListener> getListeners() {
        return listeners;
    }

    /**
     * Method to set on premise gateway listeners
     *
     * @param listeners on premise gateway listeners list
     * @return on premise gateway listeners
     */
    public void setListeners(List<OnPremiseGatewayInitListener> listeners) {
        this.listeners = listeners;
    }

    /**
     * Method to get RegistryService.
     *
     * @return registryService.
     */
    public RegistryService getRegistryService() {
        return registryService;
    }

    /**
     * Method to set registry RegistryService.
     *
     * @param service registryService.
     */
    public void setRegistryService(RegistryService service) {
        this.registryService = service;
    }
}
