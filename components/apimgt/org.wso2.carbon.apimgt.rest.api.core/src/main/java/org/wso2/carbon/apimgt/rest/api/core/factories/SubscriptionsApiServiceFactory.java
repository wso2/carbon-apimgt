package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.SubscriptionsApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.SubscriptionsApiServiceImpl;

public class SubscriptionsApiServiceFactory {
    private static SubscriptionsApiService service;

    private static final Logger log = LoggerFactory.getLogger(SubscriptionsApiServiceFactory.class);

    static {
        try {
            service = new SubscriptionsApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing SubscriptionsApiService", e);
        }
    }

    public static SubscriptionsApiService getSubscriptionsApi() {
        return service;
    }
}
