package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ThreatProtectionApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ThreatProtectionApiServiceImpl;

public class ThreatProtectionApiServiceFactory {
    private static final ThreatProtectionApiService service = new ThreatProtectionApiServiceImpl();

    public static ThreatProtectionApiService getThreatProtectionApi() {
        return service;
    }
}
