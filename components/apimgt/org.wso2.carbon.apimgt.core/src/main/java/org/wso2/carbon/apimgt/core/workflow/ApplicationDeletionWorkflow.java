/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;

/**
 * Application deletion model class for workflow
 */

public class ApplicationDeletionWorkflow extends Workflow {

    private static final Logger log = LoggerFactory.getLogger(ApplicationDeletionWorkflow.class);

    private Application application;
    private ApplicationDAO applicationDAO;

    public ApplicationDeletionWorkflow(ApplicationDAO applicationDAO, WorkflowDAO workflowDAO, APIGateway apiGateway) {
        super(workflowDAO, Category.STORE, apiGateway);
        this.applicationDAO = applicationDAO;
        setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION);
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor) throws APIManagementException {
        if (application == null) {
            // this is when complete method is executed through workflow rest api
            this.application = applicationDAO.getApplication(getWorkflowReference());
        }
        WorkflowResponse response = workflowExecutor.complete(this);
        setStatus(response.getWorkflowStatus());
        if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("Application Deletion workflow complete: Approved");
            }
            applicationDAO.deleteApplication(getWorkflowReference());
            try {
                getApiGateway().deleteApplication(application.getId());
            } catch (GatewayException ex) {
                // This log is not harm to therefore not rethrow
                log.warn("Failed to send the Application Deletion Event ", ex);
            }
        } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("Application Deletion workflow complete: Rejected");
            }
        }
        updateWorkflowEntries(this);
        return response;
    }

    @Override
    public String toString() {
        return "ApplicationDeletionWorkflow [application=" + application + ", toString()=" + super.toString() + ']';
    }

}
