package org.wso2.carbon.apimgt.rest.api.publisher.factories;

import org.wso2.carbon.apimgt.rest.api.publisher.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.impl.ThreatProtectionPoliciesApiServiceImpl;

public class ThreatProtectionPoliciesApiServiceFactory {
    private static final ThreatProtectionPoliciesApiService service = new ThreatProtectionPoliciesApiServiceImpl();

    public static ThreatProtectionPoliciesApiService getThreatProtectionPoliciesApi() {
        return service;
    }
}
