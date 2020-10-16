package org.wso2.carbon.apimgt.throttle.policy.deployer;

import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

/**
 * Class for keeping service references.
 */
public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfiguration apimConfiguration;

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    private ServiceReferenceHolder() {
    }


    public APIManagerConfiguration getAPIMConfiguration() {
        return apimConfiguration;
    }

    public void setAPIMConfigurationService(APIManagerConfigurationService configurationService) {
        if (configurationService == null) {
            this.apimConfiguration = null;
        } else {
            this.apimConfiguration = configurationService.getAPIManagerConfiguration();
        }
    }

}
