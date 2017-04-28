package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ThrottlingPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ThrottlingPoliciesApiServiceImpl;

public class ThrottlingPoliciesApiServiceFactory {
    private static final ThrottlingPoliciesApiService service = new ThrottlingPoliciesApiServiceImpl();

    public static ThrottlingPoliciesApiService getThrottlingPoliciesApi() {
        return service;
    }
}
