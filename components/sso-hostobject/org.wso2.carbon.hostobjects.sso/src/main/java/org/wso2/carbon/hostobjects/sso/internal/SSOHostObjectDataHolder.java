package org.wso2.carbon.hostobjects.sso.internal;

import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;


public class SSOHostObjectDataHolder {
    private static SSOHostObjectDataHolder instance = new SSOHostObjectDataHolder();


    private ConfigurationContextService configurationContextService;

    private SSOHostObjectDataHolder(){
    }

    public static SSOHostObjectDataHolder getInstance() {
        return instance;
    }


    public ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }


}
