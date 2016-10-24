package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.TiersApiServiceImpl;

public class TiersApiServiceFactory {
    private final static TiersApiService service = new TiersApiServiceImpl();

    public static TiersApiService getTiersApi() {
        return service;
    }
}
