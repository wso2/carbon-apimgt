package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ThreatProtectionPoliciesApiServiceImpl;

public class ThreatProtectionPoliciesApiServiceFactory {
    private static ThreatProtectionPoliciesApiService service;

    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionPoliciesApiServiceFactory.class);

    static {
        try {
            service = new ThreatProtectionPoliciesApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing ThreatProtectionPoliciesApiService", e);
        }
    }

    public static ThreatProtectionPoliciesApiService getThreatProtectionPoliciesApi() {
        return service;
    }
}
