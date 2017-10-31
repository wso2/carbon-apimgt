package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.LabelsApiServiceImpl;

public class LabelsApiServiceFactory {
    private static final LabelsApiService service = new LabelsApiServiceImpl();

    public static LabelsApiService getLabelsApi() {
        return service;
    }
}
