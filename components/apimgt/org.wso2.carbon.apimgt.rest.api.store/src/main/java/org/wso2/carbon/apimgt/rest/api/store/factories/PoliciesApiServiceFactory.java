package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {
    private final static PoliciesApiService service = new PoliciesApiServiceImpl();

    public static PoliciesApiService getPoliciesApi() {
        return service;
    }
}
