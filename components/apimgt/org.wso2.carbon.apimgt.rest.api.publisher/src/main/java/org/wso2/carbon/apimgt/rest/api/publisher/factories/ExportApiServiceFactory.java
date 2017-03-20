package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {
    private static final ExportApiService service = new ExportApiServiceImpl();

    public static ExportApiService getExportApi() {
        return service;
    }
}
