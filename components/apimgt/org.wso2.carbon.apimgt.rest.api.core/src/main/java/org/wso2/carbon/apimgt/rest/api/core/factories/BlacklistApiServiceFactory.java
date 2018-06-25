package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.BlacklistApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.BlacklistApiServiceImpl;

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
