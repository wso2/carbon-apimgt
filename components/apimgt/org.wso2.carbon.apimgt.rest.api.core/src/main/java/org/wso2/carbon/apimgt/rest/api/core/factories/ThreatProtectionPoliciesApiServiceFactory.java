package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.wso2.carbon.apimgt.rest.api.core.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ThreatProtectionPoliciesApiServiceImpl;

public class ThreatProtectionPoliciesApiServiceFactory {
    private static final ThreatProtectionPoliciesApiService service = new ThreatProtectionPoliciesApiServiceImpl();

    public static ThreatProtectionPoliciesApiService getThreatProtectionPoliciesApi() {
        return service;
    }
}
