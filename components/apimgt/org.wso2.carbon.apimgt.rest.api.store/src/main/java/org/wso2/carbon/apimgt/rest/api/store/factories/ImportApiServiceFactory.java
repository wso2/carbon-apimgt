package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.ImportApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.ImportApiServiceImpl;

public class ImportApiServiceFactory {
    private static final ImportApiService service = new ImportApiServiceImpl();

    public static ImportApiService getImportApi() {
        return service;
    }
}
