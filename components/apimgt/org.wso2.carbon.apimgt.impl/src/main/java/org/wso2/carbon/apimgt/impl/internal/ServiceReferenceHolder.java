/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.indexing.service.TenantIndexingLoader;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private RegistryService registryService;
    private APIManagerConfigurationService amConfigurationService;
    private RealmService realmService;
    private static UserRealm userRealm;
    private TenantIndexingLoader indexLoader;
    private OutputEventAdapterService outputEventAdapterService;

    public static ConfigurationContextService getContextService() {
        return contextService;
    }

    public static void setContextService(ConfigurationContextService contextService) {
        ServiceReferenceHolder.contextService = contextService;
    }

    private static ConfigurationContextService contextService;
    private ServiceReferenceHolder() {
    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public RegistryService getRegistryService() {
        return registryService;
    }

    public void setRegistryService(RegistryService registryService) {
        this.registryService = registryService;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        this.amConfigurationService = amConfigurationService;
    }

    public RealmService getRealmService() {
        return realmService;
    }

    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }
    public static void setUserRealm(UserRealm realm) {
        userRealm = realm;
    }

    public static UserRealm getUserRealm() {
        return userRealm;
    }

    public void setIndexLoaderService(TenantIndexingLoader indexLoader) {
        this.indexLoader = indexLoader;
    }

    public TenantIndexingLoader getIndexLoaderService(){
        return indexLoader;
    }

    public OutputEventAdapterService getOutputEventAdapterService() {
        return outputEventAdapterService;
    }

    public void setOutputEventAdapterService(OutputEventAdapterService outputEventAdapterService) {
        this.outputEventAdapterService = outputEventAdapterService;
    }
}
