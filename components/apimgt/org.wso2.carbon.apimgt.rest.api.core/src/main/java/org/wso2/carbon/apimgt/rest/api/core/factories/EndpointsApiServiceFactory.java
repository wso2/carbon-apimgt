package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.EndpointsApiServiceImpl;

public class EndpointsApiServiceFactory {
    private static final EndpointsApiService service = new EndpointsApiServiceImpl();

    public static EndpointsApiService getEndpointsApi() {
        return service;
    }
}
