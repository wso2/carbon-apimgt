package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.ThreatProtectionPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.ThreatProtectionPoliciesApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

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
