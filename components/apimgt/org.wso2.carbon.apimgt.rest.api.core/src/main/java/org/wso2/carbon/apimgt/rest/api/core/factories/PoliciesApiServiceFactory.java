package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.PoliciesApiServiceImpl;

public class PoliciesApiServiceFactory {
    private static PoliciesApiService service;

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiServiceFactory.class);

    static {
        try {
            service = new PoliciesApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing PoliciesApiService", e);
        }
    }



    public static PoliciesApiService getPoliciesApi() {
        return service;
    }
}
