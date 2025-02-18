package org.wso2.carbon.apimgt.impl.deployer;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.GatewayConfiguration;

public interface GatewayConfigurationService {
    void addGatewayConfiguration(String organization, String name, String type,
                                 Environment environment) throws APIManagementException;

    void updateGatewayConfiguration(String organization, String name, String type,
                                    Environment environment) throws APIManagementException;

    void removeGatewayConfiguration(String tenantDomain, String name) throws APIManagementException;
}
