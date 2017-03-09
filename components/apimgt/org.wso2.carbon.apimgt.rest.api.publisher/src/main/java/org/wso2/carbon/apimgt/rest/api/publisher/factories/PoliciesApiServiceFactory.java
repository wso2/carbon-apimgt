package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {
    private static final PoliciesApiService service = new PoliciesApiServiceImpl();

    public static PoliciesApiService getPoliciesApi() {
        return service;
    }
}
