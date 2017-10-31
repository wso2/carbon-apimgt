package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ApisApiServiceImpl;

public class ApisApiServiceFactory {
    private static final ApisApiService service = new ApisApiServiceImpl();

    public static ApisApiService getApisApi() {
        return service;
    }
}
