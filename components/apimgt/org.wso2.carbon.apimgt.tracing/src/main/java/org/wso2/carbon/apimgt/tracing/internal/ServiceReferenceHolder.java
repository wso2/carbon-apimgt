package org.wso2.carbon.apimgt.tracing.internal;

import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;

public class ServiceReferenceHolder {

    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private APIManagerConfigurationService amConfigService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigService) {
        this.amConfigService = amConfigService;
    }
}
