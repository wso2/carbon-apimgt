package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ApplicationsApiServiceImpl;

public class ApplicationsApiServiceFactory {
    private static ApplicationsApiService service;

    private static final Logger log = LoggerFactory.getLogger(ApplicationsApiServiceFactory.class);

    static {
        try {
            service = new ApplicationsApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing ApplicationsApiService", e);
        }
    }

    public static ApplicationsApiService getApplicationsApi() {
        return service;
    }
}
