package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.BlacklistApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.BlacklistApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

public class BlacklistApiServiceFactory {
    private static BlacklistApiService service;

    private static final Logger log = LoggerFactory.getLogger(BlacklistApiServiceFactory.class);

    static {
        try {
            service = new BlacklistApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing BlacklistApiService", e);
        }
    }

    public static BlacklistApiService getBlacklistApi() {
        return service;
    }
}
