package org.wso2.carbon.apimgt.rest.api.admin.factories;

import org.wso2.carbon.apimgt.rest.api.admin.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.impl.WorkflowsApiServiceImpl;

public class WorkflowsApiServiceFactory {
    private static final WorkflowsApiService service = new WorkflowsApiServiceImpl();

    public static WorkflowsApiService getWorkflowsApi() {
        return service;
    }
}
