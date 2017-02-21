package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {
    private final static ExportApiService service = new ExportApiServiceImpl();

    public static ExportApiService getExportApi() {
        return service;
    }
}
