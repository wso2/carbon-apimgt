package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.WorkflowsApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;

public class WorkflowsApiServiceFactory {
    private static WorkflowsApiService service;

    private static final Logger log = LoggerFactory.getLogger(WorkflowsApiServiceFactory.class);

    static {
        try {
            service = new WorkflowsApiServiceImpl(RestApiUtil.getAPIMgtAdminService());
        } catch (APIManagementException e) {
            log.error("Error when initializing WorkflowsApiService", e);
        }
    }

    public static WorkflowsApiService getWorkflowsApi() {
        return service;
    }
}
