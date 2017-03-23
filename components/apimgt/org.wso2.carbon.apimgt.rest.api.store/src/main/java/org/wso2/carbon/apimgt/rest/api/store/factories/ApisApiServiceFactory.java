package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.ApisApiServiceImpl;

public class ApisApiServiceFactory {
    private static final ApisApiService service = new ApisApiServiceImpl();

    public static ApisApiService getApisApi() {
        return service;
    }
}
