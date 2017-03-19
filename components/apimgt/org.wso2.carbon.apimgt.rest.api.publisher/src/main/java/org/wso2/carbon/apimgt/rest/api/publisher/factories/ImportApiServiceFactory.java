package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ImportApiServiceImpl;

public class ImportApiServiceFactory {
    private static final ImportApiService service = new ImportApiServiceImpl();

    public static ImportApiService getImportApi() {
        return service;
    }
}
