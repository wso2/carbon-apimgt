package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ApisApiServiceImpl;

public class ApisApiServiceFactory {
    private static final ApisApiService service = new ApisApiServiceImpl();

    public static ApisApiService getApisApi() {
        return service;
    }
}
