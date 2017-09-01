package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.SdkGenLanguagesApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.SdkGenLanguagesApiServiceImpl;

public class SdkGenLanguagesApiServiceFactory {
    private static final SdkGenLanguagesApiService service = new SdkGenLanguagesApiServiceImpl();

    public static SdkGenLanguagesApiService getSdkGenLanguagesApi() {
        return service;
    }
}
