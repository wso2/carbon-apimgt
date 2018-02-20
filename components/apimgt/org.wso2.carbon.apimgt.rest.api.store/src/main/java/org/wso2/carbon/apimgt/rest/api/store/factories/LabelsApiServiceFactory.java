package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.LabelsApiServiceImpl;

public class LabelsApiServiceFactory {
    private static final LabelsApiService service = new LabelsApiServiceImpl();

    public static LabelsApiService getLabelsApi() {
        return service;
    }
}
