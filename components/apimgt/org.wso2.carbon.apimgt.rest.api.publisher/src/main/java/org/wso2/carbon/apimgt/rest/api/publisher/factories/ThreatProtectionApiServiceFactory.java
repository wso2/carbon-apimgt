package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ThreatProtectionApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ThreatProtectionApiServiceImpl;

public class ThreatProtectionApiServiceFactory {
    private static final ThreatProtectionApiService service = new ThreatProtectionApiServiceImpl();

    public static ThreatProtectionApiService getThreatProtectionApi() {
        return service;
    }
}
