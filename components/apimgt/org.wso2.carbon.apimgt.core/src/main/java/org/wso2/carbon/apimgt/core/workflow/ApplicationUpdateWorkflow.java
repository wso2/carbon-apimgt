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
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;

import java.time.Instant;

/**
 * Application update model class for workflow
 */

public class ApplicationUpdateWorkflow extends Workflow {

    private static final Logger log = LoggerFactory.getLogger(ApplicationCreationWorkflow.class);

    private Application existingApplication;
    private Application updatedApplication;
    private ApplicationDAO applicationDAO;

    public ApplicationUpdateWorkflow(ApplicationDAO applicationDAO, WorkflowDAO workflowDAO, APIGateway apiGateway) {
        super(workflowDAO, Category.STORE, apiGateway);
        this.applicationDAO = applicationDAO;
        setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);
    }

    public Application getUpdatedApplication() {
        return updatedApplication;
    }

    public void setUpdatedApplication(Application updatedApplication) {
        this.updatedApplication = updatedApplication;
    }

    public Application getExistingApplication() {
        return existingApplication;
    }

    public void setExistingApplication(Application existingApplication) {
        this.existingApplication = existingApplication;
    }

    public WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor) throws APIManagementException {
        String appId = getWorkflowReference();
        String name = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_NAME);
        String updatedUser = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_UPDATEDBY);
        String applicationId = getWorkflowReference();
        String tier = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_TIER);
        String policyId = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_POLICY_ID);
        String description = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_DESCRIPTION);
        String permission = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_PERMISSION);

        Application application = new Application(name, updatedUser);
        application.setPolicy(new ApplicationPolicy(policyId, tier));
        application.setDescription(description);
        application.setId(applicationId);
        application.setUpdatedUser(updatedUser);
        application.setPermissionString(permission);
        application.setUpdatedTime(Instant.now());

        if (existingApplication == null && updatedApplication == null) {
            // this is when complete method is executed through workflow rest api
            existingApplication = applicationDAO.getApplication(appId);
            updatedApplication = application;
        }
        WorkflowResponse response = workflowExecutor.complete(this);

        setStatus(response.getWorkflowStatus());
        if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("Application update workflow complete: Approved");
            }
            application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_APPROVED);
            applicationDAO.updateApplication(appId, application);
            try {
                getApiGateway().updateApplication(application);
            } catch (GatewayException ex) {
                // This log is not harm to therefore not rethrow
                log.warn("Failed to send the Application Update Event ", ex);
            }
        } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("Application update workflow complete: Rejected");
            }
            String existingAppStatus = getAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_EXISTIN_APP_STATUS);
            applicationDAO.updateApplicationState(appId, existingAppStatus);
        }
        updateWorkflowEntries(this);
        return response;
    }

    @Override
    public String toString() {
        return "ApplicationUpdateWorkflow [existingApplication=" + existingApplication + ", updatedApplication="
                + updatedApplication + ", toString()=" + super.toString() + ']';
    }

}
