package org.wso2.carbon.apimgt.rest.api.authenticator.configuration;

import org.wso2.carbon.apimgt.rest.api.authenticator.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.rest.api.authenticator.internal.ServiceReferenceHolder;

/**
 * Utility class for get configuration
 */
public class APIMConfigurationService {
    private static APIMConfigurationService apimConfigurationService = new APIMConfigurationService();
    private APIMConfigurations apimConfigurations;

    private APIMConfigurationService() {
        apimConfigurations = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
    }

    public static APIMConfigurationService getInstance() {
        return apimConfigurationService;
    }

    public APIMConfigurations getApimConfigurations() {
        return apimConfigurations;
    }
}
