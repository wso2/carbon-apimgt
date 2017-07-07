package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {
    private static final ExportApiService service = new ExportApiServiceImpl();

    public static ExportApiService getExportApi() {
        return service;
    }
}
