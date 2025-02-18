package org.wso2.carbon.apimgt.impl.deployer;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.GatewayConfiguration;
import org.wso2.carbon.apimgt.impl.factory.GatewayHolder;

public class GatewayConfigurationServiceImpl implements GatewayConfigurationService {
    @Override
    public void addGatewayConfiguration(String organization, String name, String type,
                                        GatewayConfiguration gatewayConfiguration) throws APIManagementException {
        String internKey = this.getClass().getName().concat(organization).concat(name);
        synchronized (internKey.intern()) {
            GatewayHolder.addGatewayConfiguration(organization, name, type, gatewayConfiguration);
        }
    }

    @Override
    public void updateGatewayConfiguration(String organization, String name, String type,
                                           GatewayConfiguration gatewayConfiguration) throws APIManagementException {
        String internKey = this.getClass().getName().concat(organization).concat(name);
        synchronized (internKey.intern()) {
            GatewayHolder.updateGatewayConfiguration(organization, name, type, gatewayConfiguration);
        }
    }

    @Override
    public void removeGatewayConfiguration(String tenantDomain, String name) throws APIManagementException {
        String internKey = this.getClass().getName().concat(tenantDomain).concat(name);
        synchronized (internKey.intern()) {
            GatewayHolder.removeGatewayConfiguration(tenantDomain, name);
        }
    }
}
