package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.LabelsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.LabelsApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

public class LabelsApiServiceFactory {
    private static LabelsApiService service;

    private static final Logger log = LoggerFactory.getLogger(LabelsApiServiceFactory.class);

    static {
        try {
            service = new LabelsApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing LabelsApiService", e);
        }
    }

    public static LabelsApiService getLabelsApi() {
        return service;
    }
}
