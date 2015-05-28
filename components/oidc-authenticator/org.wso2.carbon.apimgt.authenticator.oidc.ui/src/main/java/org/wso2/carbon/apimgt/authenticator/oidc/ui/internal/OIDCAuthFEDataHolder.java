/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.authenticator.oidc.ui.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * This class is used as the Singleton data holder for OIDC Authentication FE module.
 */
public class OIDCAuthFEDataHolder {
    private static OIDCAuthFEDataHolder instance = new OIDCAuthFEDataHolder();

    private RealmService realmService;
    private RegistryService registryService;
    private ConfigurationContextService configurationContextService;
    private CarbonSSOSessionManager carbonSSOSessionManager;

    private OIDCAuthFEDataHolder(){
    }

    public static OIDCAuthFEDataHolder getInstance() {
        return instance;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    public CarbonSSOSessionManager getCarbonSSOSessionManager() {
        return carbonSSOSessionManager;
    }

    public void setCarbonSSOSessionManager(CarbonSSOSessionManager carbonSSOSessionManager) {
        this.carbonSSOSessionManager = carbonSSOSessionManager;
    }
}
