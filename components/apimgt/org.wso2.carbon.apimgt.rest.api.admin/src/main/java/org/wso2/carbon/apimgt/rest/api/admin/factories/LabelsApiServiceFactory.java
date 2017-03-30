package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.LabelsApiServiceImpl;

public class LabelsApiServiceFactory {
    private static final LabelsApiService service = new LabelsApiServiceImpl();

    public static LabelsApiService getLabelsApi() {
        return service;
    }
}
