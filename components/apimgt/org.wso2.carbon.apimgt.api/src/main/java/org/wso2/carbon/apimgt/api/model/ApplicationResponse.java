package org.wso2.carbon.apimgt.api.model;

import org.wso2.carbon.apimgt.api.WorkflowResponse;

public class ApplicationResponse {
    private String applicationStatus = null;
    private String applicationUUID = null;
    private WorkflowResponse workflowResponse = null;

    public ApplicationResponse(String applicationStatus, String applicationUUID, WorkflowResponse workflowResponse) {
        this.applicationStatus = applicationStatus;
        this.applicationUUID = applicationUUID;
        this.workflowResponse = workflowResponse;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public String getApplicationUUID() {
        return applicationUUID;
    }

    public WorkflowResponse getWorkflowResponse() {
        return workflowResponse;
    }
}
