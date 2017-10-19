package org.wso2.carbon.apimgt.rest.api.webserver.factories;

import org.wso2.carbon.apimgt.rest.api.webserver.PublisherApiService;
import org.wso2.carbon.apimgt.rest.api.webserver.impl.PublisherApiServiceImpl;

public class PublisherApiServiceFactory {
    private static final PublisherApiService service = new PublisherApiServiceImpl();

    public static PublisherApiService getPublisherApi() {
        return service;
    }
}
