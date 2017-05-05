package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.BlacklistApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.BlacklistApiServiceImpl;

public class BlacklistApiServiceFactory {
    private static final BlacklistApiService service = new BlacklistApiServiceImpl();

    public static BlacklistApiService getBlacklistApi() {
        return service;
    }
}
