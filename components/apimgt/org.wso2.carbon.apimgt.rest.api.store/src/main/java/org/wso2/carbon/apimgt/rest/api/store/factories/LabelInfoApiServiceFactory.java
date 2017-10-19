package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.LabelInfoApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.LabelInfoApiServiceImpl;

public class LabelInfoApiServiceFactory {
    private static final LabelInfoApiService service = new LabelInfoApiServiceImpl();

    public static LabelInfoApiService getLabelInfoApi() {
        return service;
    }
}
