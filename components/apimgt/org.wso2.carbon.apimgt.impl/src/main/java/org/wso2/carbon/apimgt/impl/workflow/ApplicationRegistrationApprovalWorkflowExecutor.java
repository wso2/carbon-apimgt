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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

/**
 * Approval workflow for Application Registration key generation.
 */
public class ApplicationRegistrationApprovalWorkflowExecutor extends AbstractApplicationRegistrationWorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationRegistrationApprovalWorkflowExecutor.class);
    private boolean applicationAttributesVisibility = false;
    private static final String KEY_TYPE_PROPERTY = "keyType";
    private static final String APPLICATION_NAME_PROPERTY = "applicationName";
    private static final String APPLICATION_OWNER_PROPERTY = "applicationOwner";
    private static final String APPLICATION_TIER_PROPERTY = "applicationTier";
    private static final String APPLICATION_ATTRIBUTES_PROPERTY = "applicationAttributes";
    private static final String APPLICATION_DESCRIPTION_PROPERTY = "applicationDescription";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Execute the Application Creation workflow approval process.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Application Registration Approval Workflow. Workflow Reference: " + workflowDTO.getWorkflowReference());
        }

        ApplicationRegistrationWorkflowDTO appRegDTO = (ApplicationRegistrationWorkflowDTO) workflowDTO;

        Application application = appRegDTO.getApplication();

        String message = "Approve request to create " + appRegDTO.getKeyType() + " keys for " + application.getName() +
                " from application creator - " + appRegDTO.getUserName() + " with throttling tier - " + application.getTier();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties(APPLICATION_NAME_PROPERTY, application.getName());
        workflowDTO.setProperties(APPLICATION_TIER_PROPERTY, application.getTier());
        workflowDTO.setProperties(APPLICATION_OWNER_PROPERTY, appRegDTO.getUserName());
        workflowDTO.setProperties(KEY_TYPE_PROPERTY, appRegDTO.getKeyType());

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
            log.debug("Application Registration Approval Workflow executed successfully. Workflow Reference: "
                    + workflowDTO.getWorkflowReference());
        }

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the Approval workflow executor for application key generation.
     *
     * @param workFlowDTO
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workFlowDTO);
        if (log.isDebugEnabled()) {
            String logMessage = "Application Registration [Complete] Workflow Invoked. Workflow ID : " + workFlowDTO
                    .getExternalWorkflowReference() + " Workflow State : " + workFlowDTO.getStatus();
            log.debug(logMessage);
        }
        if (WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())) {
            try {
                generateKeysForApplication((ApplicationRegistrationWorkflowDTO) workFlowDTO);
            } catch (APIManagementException e) {
                String msg = "Error occurred when updating the status of the Application Registration process";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        // implemetation is not provided in this version
        return null;
    }

    /**
     * Handle cleanup task for application registration Approval workflow executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef Workflow external reference of pending workflow request
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        super.cleanUpPendingTask(workflowExtRef);
        String errorMsg = null;
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for ApplicationRegistrationApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending registration approval process message. Cause: " + axisFault
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
