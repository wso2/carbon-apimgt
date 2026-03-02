/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl.workflow;

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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;
import java.util.Map;

/**
 * Approval workflow for Application Creation.
 */
public class ApplicationCreationApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationCreationApprovalWorkflowExecutor.class);
    private boolean applicationAttributesVisibility = true;
    private static final String APPLICATION_NAME_PROPERTY = "applicationName";
    private static final String APPLICATION_OWNER_PROPERTY = "applicationOwner";
    private static final String APPLICATION_TIER_PROPERTY = "applicationTier";
    private static final String APPLICATION_ATTRIBUTES_PROPERTY = "applicationAttributes";
    private static final String APPLICATION_DESCRIPTION_PROPERTY = "applicationDescription";

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION;
    }

    /**
     * Execute the Application Creation workflow approval process.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Application Creation Approval Workflow. " + "Workflow Reference: " + workflowDTO.getWorkflowReference());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        ApplicationWorkflowDTO appWorkFlowDTO = (ApplicationWorkflowDTO) workflowDTO;

        Application application = appWorkFlowDTO.getApplication();
        String message = "Approve application " + application.getName() + " creation request from application creator - "
                + appWorkFlowDTO.getUserName() + " with throttling tier - " + application.getTier();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties(APPLICATION_NAME_PROPERTY, application.getName());
        workflowDTO.setProperties(APPLICATION_TIER_PROPERTY, application.getTier());
        workflowDTO.setProperties(APPLICATION_OWNER_PROPERTY, appWorkFlowDTO.getUserName());

        if (!StringUtils.isNotBlank(application.getDescription())) {
            workflowDTO.setProperties(APPLICATION_DESCRIPTION_PROPERTY, application.getDescription());
        }

        if (applicationAttributesVisibility && application.getApplicationAttributes() != null && !application.getApplicationAttributes().isEmpty()) {
                try {
                    workflowDTO.setProperties(APPLICATION_ATTRIBUTES_PROPERTY, objectMapper.writeValueAsString(application.getApplicationAttributes()));
                } catch (JsonProcessingException e) {
                    String msg = "Failed to serialize custom attributes of application ";
                    log.error(msg, e);
                    log.error(msg + application.getName() + ", error : " + e.getMessage());
                    throw new WorkflowException(msg, e);
                }
        }

        super.execute(workflowDTO);
        if (log.isDebugEnabled()) {
            log.debug("Application Creation Approval Workflow executed successfully for application: " + application.getName());
        }

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the Application creation approval workflow peocess.
     *
     * @param workFlowDTO
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {

        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            if (dao.getApplicationById(Integer.parseInt(workFlowDTO.getWorkflowReference())) != null) {
                super.complete(workFlowDTO);
                if (log.isDebugEnabled()) {
                    String logMessage = "Application Creation [Complete] Workflow Invoked. Workflow ID : " + workFlowDTO
                            .getExternalWorkflowReference() + " Workflow State : " + workFlowDTO.getStatus();
                    log.debug(logMessage);
                }
                String status = null;
                if (WorkflowStatus.CREATED.equals(workFlowDTO.getStatus())) {
                    status = APIConstants.ApplicationStatus.APPLICATION_CREATED;
                } else if (WorkflowStatus.REJECTED.equals(workFlowDTO.getStatus())) {
                    status = APIConstants.ApplicationStatus.APPLICATION_REJECTED;
                } else if (WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())) {
                    status = APIConstants.ApplicationStatus.APPLICATION_APPROVED;
                }

                try {
                    dao.updateApplicationStatus(Integer.parseInt(workFlowDTO.getWorkflowReference()), status);
                } catch (APIManagementException e) {
                    String msg = "Error occurred when updating the status of the Application creation process";
                    log.error(msg, e);
                    throw new WorkflowException(msg, e);
                }
            } else {
                String msg = "Application does not exist";
                throw new WorkflowException(msg);
            }
        } catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        // implemetation is not provided in this version
        return null;
    }

    /**
     * Handle cleanup task for application creation Approval workflow executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef Workflow external reference of pending workflow request
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        String errorMsg;
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for ApplicationCreationApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending application approval process message. cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }

    public boolean getApplicationAttributesVisibility() {

        return applicationAttributesVisibility;
    }

    public void setApplicationAttributesVisibility(boolean applicationAttributesVisibility) {

        this.applicationAttributesVisibility = applicationAttributesVisibility;
    }
}
