package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ThreatProtectionPoliciesApiServiceImpl;

public class ThreatProtectionPoliciesApiServiceFactory {
    private static final ThreatProtectionPoliciesApiService service = new ThreatProtectionPoliciesApiServiceImpl();

    public static ThreatProtectionPoliciesApiService getThreatProtectionPoliciesApi() {
        return service;
    }
}
