package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ThreatProtectionApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ThreatProtectionApiServiceImpl;

public class ThreatProtectionApiServiceFactory {
    private static final ThreatProtectionApiService service = new ThreatProtectionApiServiceImpl();

    public static ThreatProtectionApiService getThreatProtectionApi() {
        return service;
    }
}
