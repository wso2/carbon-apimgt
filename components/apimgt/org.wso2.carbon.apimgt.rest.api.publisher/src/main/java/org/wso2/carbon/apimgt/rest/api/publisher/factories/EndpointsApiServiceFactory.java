package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.EndpointsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.EndpointsApiServiceImpl;

public class EndpointsApiServiceFactory {
    private static final EndpointsApiService service = new EndpointsApiServiceImpl();

    public static EndpointsApiService getEndpointsApi() {
        return service;
    }
}
