package org.wso2.carbon.apimgt.rest.api.core.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ExportApiService;
import org.wso2.carbon.apimgt.rest.api.core.impl.ExportApiServiceImpl;

public class ExportApiServiceFactory {
    private static ExportApiService service;
    private static final Logger log = LoggerFactory.getLogger(ExportApiServiceFactory.class);

    static {
        try {
            service = new ExportApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing ExportApiService", e);
        }
    }

    public static ExportApiService getExportApi() {
        return service;
    }
}
