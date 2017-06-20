package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.WsdlsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.WsdlsApiServiceImpl;

public class WsdlsApiServiceFactory {
    private static final WsdlsApiService service = new WsdlsApiServiceImpl();

    public static WsdlsApiService getWsdlsApi() {
        return service;
    }
}
