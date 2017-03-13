package org.wso2.carbon.apimgt.rest.api.store.factories;

import org.wso2.carbon.apimgt.rest.api.store.WorkflowsApiService;
import org.wso2.carbon.apimgt.rest.api.store.impl.WorkflowsApiServiceImpl;

public class WorkflowsApiServiceFactory {
    private static final WorkflowsApiService service = new WorkflowsApiServiceImpl();

    public static WorkflowsApiService getWorkflowsApi() {
        return service;
    }
}
