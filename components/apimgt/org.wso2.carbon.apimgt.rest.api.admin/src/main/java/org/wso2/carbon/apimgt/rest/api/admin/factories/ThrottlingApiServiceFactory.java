package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ThrottlingApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ThrottlingApiServiceImpl;

public class ThrottlingApiServiceFactory {
    private static final ThrottlingApiService service = new ThrottlingApiServiceImpl();

    public static ThrottlingApiService getThrottlingApi() {
        return service;
    }
}
