package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.SelfSignupApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.SelfSignupApiServiceImpl;

public class SelfSignupApiServiceFactory {
    private static final SelfSignupApiService service = new SelfSignupApiServiceImpl();

    public static SelfSignupApiService getSelfSignupApi() {
        return service;
    }
}
