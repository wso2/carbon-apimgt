package org.wso2.carbon.apimgt.impl.deployer;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.factory.GatewayHolder;

public class GatewayConfigurationServiceImpl implements GatewayConfigurationService {
    @Override
    public void addGatewayConfiguration(String organization, String name, String type,
                                        Environment environment) throws APIManagementException {
        String internKey = this.getClass().getName().concat(organization).concat(name);
        synchronized (internKey.intern()) {
            GatewayHolder.addGatewayConfiguration(organization, name, type, environment);
        }
    }

    @Override
    public void updateGatewayConfiguration(String organization, String name, String type,
                                           Environment environment) throws APIManagementException {
        String internKey = this.getClass().getName().concat(organization).concat(name);
        synchronized (internKey.intern()) {
            GatewayHolder.updateGatewayConfiguration(organization, name, type, environment);
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
