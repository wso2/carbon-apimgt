package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.BlacklistApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.BlacklistApiServiceImpl;

public class BlacklistApiServiceFactory {
    private static final BlacklistApiService service = new BlacklistApiServiceImpl();

    public static BlacklistApiService getBlacklistApi() {
        return service;
    }
}
