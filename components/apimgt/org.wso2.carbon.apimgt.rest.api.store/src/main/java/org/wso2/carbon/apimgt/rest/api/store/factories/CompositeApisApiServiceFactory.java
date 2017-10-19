package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.CompositeApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.CompositeApisApiServiceImpl;

public class CompositeApisApiServiceFactory {
    private static final CompositeApisApiService service = new CompositeApisApiServiceImpl();

    public static CompositeApisApiService getCompositeApisApi() {
        return service;
    }
}
