package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ResourcesApiServiceImpl;

public class ResourcesApiServiceFactory {
    private static ResourcesApiService service;

    private static final Logger log = LoggerFactory.getLogger(ResourcesApiServiceFactory.class);

    static {
        try {
            service = new ResourcesApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing ResourcesApiService", e);
        }
    }

    public static ResourcesApiService getResourcesApi() {
        return service;
    }
}
