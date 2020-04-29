package org.wso2.carbon.apimgt.impl.keymgt;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;

public class KeyManagerConfigurationServiceImpl implements KeyManagerConfigurationService {

    @Override
    public void addKeyManagerConfiguration(String tenantDomain,String name,String type, KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException {

        KeyManagerHolder.addKeyManagerConfiguration(tenantDomain,name,type, keyManagerConfiguration);
    }

    @Override
    public void updateKeyManagerConfiguration(String tenantDomain,String name,String type,
                                              KeyManagerConfiguration keyManagerConfiguration,boolean enabled)
            throws APIManagementException {

        KeyManagerHolder.updateKeyManagerConfiguration(tenantDomain,name,type,keyManagerConfiguration,enabled);

    }

    @Override
    public void removeKeyManagerConfiguration(String tenantDomain, String name) {

        KeyManagerHolder.removeKeyManagerConfiguration(tenantDomain, name);
    }
}
