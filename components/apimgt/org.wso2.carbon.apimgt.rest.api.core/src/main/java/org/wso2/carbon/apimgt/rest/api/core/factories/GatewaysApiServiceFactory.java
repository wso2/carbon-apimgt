package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.GatewaysApiServiceImpl;

public class GatewaysApiServiceFactory {
    private static GatewaysApiService service;

    private static final Logger log = LoggerFactory.getLogger(GatewaysApiServiceFactory.class);

    static {
        try {
            service = new GatewaysApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing GatewaysApiService", e);
        }
    }

    public static GatewaysApiService getGatewaysApi() {
        return service;
    }
}
