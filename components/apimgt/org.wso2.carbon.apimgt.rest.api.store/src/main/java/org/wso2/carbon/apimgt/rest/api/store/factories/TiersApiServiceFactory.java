package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.TiersApiServiceImpl;

public class TiersApiServiceFactory {
    private final static TiersApiService service = new TiersApiServiceImpl();

    public static TiersApiService getTiersApi() {
        return service;
    }
}
