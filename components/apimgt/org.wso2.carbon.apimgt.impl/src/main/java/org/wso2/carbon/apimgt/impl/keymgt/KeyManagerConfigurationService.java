package org.wso2.carbon.apimgt.impl.keymgt;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;

public interface KeyManagerConfigurationService {

    void addKeyManagerConfiguration(String tenantDomain, String name, String type,
                                    KeyManagerConfiguration keyManagerConfiguration) throws
            APIManagementException;

    void updateKeyManagerConfiguration(String tenantDomain, String name, String type,
                                       KeyManagerConfiguration keyManagerConfiguration)
            throws APIManagementException;

    void removeKeyManagerConfiguration(String tenantDomain, String name) throws APIManagementException;
}
