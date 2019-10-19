package org.wso2.carbon.throttle.service.factories;

import org.wso2.carbon.throttle.service.ApikeyApiService;
import org.wso2.carbon.throttle.service.impl.ApikeyApiServiceImpl;

public class ApikeyApiServiceFactory {

    private final static ApikeyApiService service = new ApikeyApiServiceImpl();

    public static ApikeyApiService getApikeyApi()
    {
        return service;
    }
}
