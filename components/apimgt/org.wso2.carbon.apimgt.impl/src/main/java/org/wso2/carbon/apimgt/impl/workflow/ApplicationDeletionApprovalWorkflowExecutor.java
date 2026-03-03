/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.*;

public class ApplicationDeletionApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationDeletionApprovalWorkflowExecutor.class);
    private boolean applicationAttributesVisibility = false;
    private static final String APPLICATION_NAME_PROPERTY = "applicationName";
    private static final String APPLICATION_OWNER_PROPERTY = "applicationOwner";
    private static final String APPLICATION_TIER_PROPERTY = "applicationTier";
    private static final String APPLICATION_STATUS_PROPERTY = "applicationStatus";
    private static final String APPLICATION_ATTRIBUTES_PROPERTY = "applicationAttributes";
    private static final String APPLICATION_DESCRIPTION_PROPERTY = "applicationDescription";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String getWorkflowType() {

        return WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Application Deletion Approval Workflow. Workflow Reference: " + workflowDTO.getWorkflowReference());
        }

        ApplicationWorkflowDTO appWorkFlowDTO = (ApplicationWorkflowDTO) workflowDTO;

        Application application = appWorkFlowDTO.getApplication();
        String message = "Approve application " + application.getName() + " delete request from application creator - "
                + appWorkFlowDTO.getUserName() + " with throttling tier - " + application.getTier();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties(APPLICATION_NAME_PROPERTY, application.getName());
        workflowDTO.setProperties(APPLICATION_TIER_PROPERTY, application.getTier());
        workflowDTO.setProperties(APPLICATION_OWNER_PROPERTY, appWorkFlowDTO.getUserName());
        workflowDTO.setMetadata(APPLICATION_STATUS_PROPERTY,application.getStatus());

        if (StringUtils.isNotBlank(application.getDescription())) {
            workflowDTO.setProperties(APPLICATION_DESCRIPTION_PROPERTY, application.getDescription());
        }

        if (applicationAttributesVisibility && application.getApplicationAttributes() != null && !application.getApplicationAttributes().isEmpty()) {
            try {
                workflowDTO.setProperties(APPLICATION_ATTRIBUTES_PROPERTY, OBJECT_MAPPER.writeValueAsString(application.getApplicationAttributes()));
            } catch (JsonProcessingException e) {
                String msg = "Failed to serialize custom attributes of application";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
        }

        super.execute(workflowDTO);
        if (log.isDebugEnabled()) {
            log.debug("Application Deletion Approval Workflow executed successfully. Workflow Reference: "
                    + workflowDTO.getWorkflowReference());
        }

        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        String errorMsg = null;
        super.complete(applicationWorkflowDTO);

        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            try {
                Application application = apiMgtDAO.getApplicationById(Integer.parseInt(applicationWorkflowDTO.getWorkflowReference()));
                ((ApplicationWorkflowDTO) workflowDTO).setApplication(application);
                apiMgtDAO.deleteApplication(application);
            } catch (APIManagementException e) {
                if (e.getMessage() == null) {
                    errorMsg = "Couldn't complete simple application deletion workflow for application: ";
                } else {
                    errorMsg = e.getMessage();
                }
                throw new WorkflowException(errorMsg, e);
            }
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
            try {
                if (applicationWorkflowDTO.getMetadata() != null &&
                        applicationWorkflowDTO.getMetadata().containsKey("applicationStatus")) {
                    apiMgtDAO.updateApplicationStatus(Integer.parseInt(applicationWorkflowDTO.getWorkflowReference()),
                            applicationWorkflowDTO.getMetadata("applicationStatus"));
                } else {
                    apiMgtDAO.updateApplicationStatus(Integer.parseInt(applicationWorkflowDTO.getWorkflowReference()),
                            APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                }
            } catch (APIManagementException e) {
                if (e.getMessage() == null) {
                    errorMsg = "Couldn't complete simple application deletion workflow for application: ";
                } else {
                    errorMsg = e.getMessage();
                }
                throw new WorkflowException(errorMsg, e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {

        return Collections.emptyList();
    }

    public boolean getApplicationAttributesVisibility() {
        return applicationAttributesVisibility;
    }

    public void setApplicationAttributesVisibility(boolean applicationAttributesVisibility) {
        this.applicationAttributesVisibility = applicationAttributesVisibility;
    }
}
