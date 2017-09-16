package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.SdkGenApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.SdkGenApiServiceImpl;

public class SdkGenApiServiceFactory {
    private static final SdkGenApiService service = new SdkGenApiServiceImpl();

    public static SdkGenApiService getSdkGenApi() {
        return service;
    }
}
