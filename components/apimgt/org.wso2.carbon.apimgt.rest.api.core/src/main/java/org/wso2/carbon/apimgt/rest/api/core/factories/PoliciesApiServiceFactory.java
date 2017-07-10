package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {
    private static final PoliciesApiService service = new PoliciesApiServiceImpl();

    public static PoliciesApiService getPoliciesApi() {
        return service;
    }
}
