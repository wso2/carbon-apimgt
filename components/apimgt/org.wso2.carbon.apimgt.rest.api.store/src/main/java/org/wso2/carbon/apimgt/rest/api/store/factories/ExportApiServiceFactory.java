package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {
    private static final ExportApiService service = new ExportApiServiceImpl();

    public static ExportApiService getExportApi() {
        return service;
    }
}
