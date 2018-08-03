package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.EndpointsApiServiceImpl;

public class EndpointsApiServiceFactory {
    private static EndpointsApiService service;

    private static final Logger log = LoggerFactory.getLogger(EndpointsApiServiceFactory.class);

    static {
        try {
            service = new EndpointsApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing EndpointsApiService", e);
        }
    }

    public static EndpointsApiService getEndpointsApi() {
        return service;
    }
}
