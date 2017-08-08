package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.GatewaysApiServiceImpl;

public class GatewaysApiServiceFactory {
    private static final GatewaysApiService service = new GatewaysApiServiceImpl();

    public static GatewaysApiService getGatewaysApi() {
        return service;
    }
}
