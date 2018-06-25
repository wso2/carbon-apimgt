package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ApisApiServiceImpl;

public class ApisApiServiceFactory {
    private static ApisApiService service;

    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceFactory.class);

    static {
        try {
            service = new ApisApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing ApisApiService", e);
        }
    }

    public static ApisApiService getApisApi() {
        return service;
    }
}
