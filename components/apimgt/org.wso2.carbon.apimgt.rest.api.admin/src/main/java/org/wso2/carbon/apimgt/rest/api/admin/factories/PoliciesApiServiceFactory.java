package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {
    private static final PoliciesApiService service = new PoliciesApiServiceImpl();

    public static PoliciesApiService getPoliciesApi() {
        return service;
    }
}
