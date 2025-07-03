/*
 * Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.*;

/**
 * Approval workflow for Application Update
 */
public class ApplicationUpdateApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationUpdateApprovalWorkflowExecutor.class);
    private static final ApiMgtDAO dao = ApiMgtDAO.getInstance();
    ;

    @Override
    public String getWorkflowType() {
        return null;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    /**
     * Execute the Application Update workflow approval process
     *
     * @param workflowDTO - The WorkflowDTO which contains workflow contextual information related to the workflow.
     * @return
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Application Update Approval Workflow.. ");
        }
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application pendingApplication = applicationWorkflowDTO.getApplication();
        Application existingApplication = applicationWorkflowDTO.getExistingApplication();

        workflowDTO.setProperties("applicationName", existingApplication.getName());
        workflowDTO.setProperties("userName", existingApplication.getOwner());
        workflowDTO.setProperties("applicationTier", existingApplication.getTier());

        List<Map<String, String>> applicationUpdateDiffs = new ArrayList<>();

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, "Application Name",
                existingApplication.getName(), pendingApplication.getName());

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, "Tier",
                existingApplication.getTier(), pendingApplication.getTier());

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, "Description",
                existingApplication.getDescription(), pendingApplication.getDescription());

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, "Groups",
                existingApplication.getGroupId(), pendingApplication.getGroupId());

        // Special case: since the shared organization (getSharedOrganization) is an uuid when
        // "Sharing with the organization" is enabled
        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, "Sharing with the organization",
                getShareWithOrganizationStatus(existingApplication.getSharedOrganization()),
                getShareWithOrganizationStatus(pendingApplication.getSharedOrganization()));

        applicationUpdateDiffs.addAll(extractCustomAttributeDiffs
                (existingApplication.getApplicationAttributes(), pendingApplication.getApplicationAttributes()));

        String applicationUpdateDiffJson;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            applicationUpdateDiffJson = objectMapper.writeValueAsString(applicationUpdateDiffs);
        } catch (JsonProcessingException e) {
            String msg = "Failed to serialize application update differences to JSON";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        workflowDTO.setProperties("applicationUpdateDiff", applicationUpdateDiffJson);

        workflowDTO.setMetadata("requestedApplicationName", pendingApplication.getName());
        workflowDTO.setMetadata("requestedTier", pendingApplication.getTier());
        workflowDTO.setMetadata("requestedDescription", pendingApplication.getDescription());
        workflowDTO.setMetadata("requestedSharedOrganization", pendingApplication.getSharedOrganization());

        if (pendingApplication.getGroupId() != null) {
            workflowDTO.setMetadata("requestedGroupIDs", pendingApplication.getGroupId());
        }

        String requestedCustomAttributes;
        try {
            requestedCustomAttributes = objectMapper.writeValueAsString(pendingApplication.getApplicationAttributes());
        } catch (JsonProcessingException e) {
            String msg = "Failed to serialize requested custom attributes of application";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        workflowDTO.setMetadata("requestedCustomAttributes", requestedCustomAttributes);


        String message = "Approve update request for application '" + pendingApplication.getName() +
                "' submitted by user: " + applicationWorkflowDTO.getUserName();
        workflowDTO.setWorkflowDescription(message);

        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the application update approval workflow process
     *
     * @param workFlowDTO - The WorkflowDTO which contains workflow contextual information related to the workflow.
     * @return
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workFlowDTO);
        try {
            Application application = dao.getApplicationById(Integer.parseInt(workFlowDTO.getWorkflowReference()));
            if (WorkflowStatus.APPROVED.equals(workFlowDTO.getStatus())) {
                application.setStatus(APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                application.setName(workFlowDTO.getMetadata("requestedApplicationName"));
                application.setTier(workFlowDTO.getMetadata("requestedTier"));
                application.setDescription(workFlowDTO.getMetadata("requestedDescription"));

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, String> applicationAttributes =
                        objectMapper.readValue(workFlowDTO.getMetadata("requestedCustomAttributes"), Map.class);
                application.setApplicationAttributes(applicationAttributes);

                if (workFlowDTO.getMetadata().containsKey("requestedGroupIDs")) {
                    application.setGroupId(workFlowDTO.getMetadata("requestedGroupIDs"));
                }

                application.setSharedOrganization(workFlowDTO.getMetadata("requestedSharedOrganization"));
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                dao.updateApplication(application);
            } else {
                ;
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.UPDATE_REJECTED);
            }

        } catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return new GeneralWorkflowResponse();
    }

    /**
     * Handle cleanup task for application update Approval workflow executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef
     * @throws WorkflowException
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        String errorMsg;
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for ApplicationUpdateApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        try {
            dao.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending application update approval process message. cause: " +
                    axisFault.getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }
}

