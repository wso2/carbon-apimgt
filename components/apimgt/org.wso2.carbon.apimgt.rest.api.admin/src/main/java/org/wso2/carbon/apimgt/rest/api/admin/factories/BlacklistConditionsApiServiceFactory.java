package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.BlacklistConditionsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.BlacklistConditionsApiServiceImpl;

public class BlacklistConditionsApiServiceFactory {
    private static final BlacklistConditionsApiService service = new BlacklistConditionsApiServiceImpl();

    public static BlacklistConditionsApiService getBlacklistConditionsApi() {
        return service;
    }
}
