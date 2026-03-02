/*
 *
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.*;

/**
 * Approval workflow for Application Update
 */
public class ApplicationUpdateApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationUpdateApprovalWorkflowExecutor.class);
    private static final ApiMgtDAO dao = ApiMgtDAO.getInstance();
    private boolean applicationAttributesVisibility = true;
    private static final String APPLICATION_NAME_PROPERTY = "applicationName";
    private static final String APPLICATION_OWNER_PROPERTY = "applicationOwner";
    private static final String APPLICATION_TIER_PROPERTY = "applicationTier";
    private static final String UPDATES_PROPERTY = "updates";
    private static final String REQUESTED_APPLICATION_NAME_PROPERTY = "requestedApplicationName";
    private static final String REQUESTED_TIER_PROPERTY = "requestedTier";
    private static final String REQUESTED_DESCRIPTION_PROPERTY = "requestedDescription";
    private static final String REQUESTED_SHARED_ORGANIZATION_PROPERTY = "requestedSharedOrganization";
    private static final String REQUESTED_GROUP_IDS_PROPERTY = "requestedGroupIDs";
    private static final String REQUESTED_CUSTOM_ATTRIBUTES_PROPERTY = "requestedCustomAttributes";
    private static final String EXISTING_APPLICATION_ATTRIBUTES_PROPERTY = "existingApplicationAttributes";
    private static final String APPLICATION_DESCRIPTION_PROPERTY = "applicationDescription";
    private static final String APPLICATION_NAME_LABEL = "Application Name";
    private static final String TIER_LABEL = "Tier";
    private static final String DESCRIPTION_LABEL = "Description";
    private static final String GROUPS_LABEL = "Groups";
    private static final String SHARING_WITH_ORGANIZATION_LABEL = "Sharing with the organization";


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
            log.debug("Executing Application Update Approval Workflow. " + "Workflow Reference: " + workflowDTO.getWorkflowReference());
        }
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application pendingApplication = applicationWorkflowDTO.getApplication();
        Application existingApplication = applicationWorkflowDTO.getExistingApplication();

        workflowDTO.setProperties(APPLICATION_NAME_PROPERTY, existingApplication.getName());
        workflowDTO.setProperties(APPLICATION_TIER_PROPERTY, existingApplication.getTier());
        workflowDTO.setProperties(APPLICATION_OWNER_PROPERTY, existingApplication.getOwner());

        if (!StringUtils.isNotBlank(existingApplication.getDescription())) {
            workflowDTO.setProperties(APPLICATION_DESCRIPTION_PROPERTY, existingApplication.getDescription());
        }

        List<Map<String, String>> applicationUpdateDiffs = new ArrayList<>();

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, APPLICATION_NAME_LABEL,
                existingApplication.getName(), pendingApplication.getName());

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, TIER_LABEL,
                existingApplication.getTier(), pendingApplication.getTier());

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, DESCRIPTION_LABEL,
                existingApplication.getDescription(), pendingApplication.getDescription());

        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, GROUPS_LABEL,
                existingApplication.getGroupId(), pendingApplication.getGroupId());

        // Special case: since the shared organization (getSharedOrganization) is an uuid when
        // "Sharing with the organization" is enabled
        compareAndAddToApplicationUpdateDiffs(applicationUpdateDiffs, SHARING_WITH_ORGANIZATION_LABEL,
                getShareWithOrganizationStatus(existingApplication.getSharedOrganization()),
                getShareWithOrganizationStatus(pendingApplication.getSharedOrganization()));

        applicationUpdateDiffs.addAll(extractCustomAttributeDiffs
                (existingApplication.getApplicationAttributes(), pendingApplication.getApplicationAttributes()));

        String applicationUpdateDiffJson;
        ObjectMapper objectMapper = new ObjectMapper();

        if (!applicationUpdateDiffs.isEmpty()) {
            try {
                applicationUpdateDiffJson = objectMapper.writeValueAsString(applicationUpdateDiffs);
            } catch (JsonProcessingException e) {
                String msg = "Failed to serialize application update differences to JSON";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
            workflowDTO.setProperties(UPDATES_PROPERTY, applicationUpdateDiffJson);
        }

        workflowDTO.setMetadata(REQUESTED_APPLICATION_NAME_PROPERTY, pendingApplication.getName());
        workflowDTO.setMetadata(REQUESTED_TIER_PROPERTY, pendingApplication.getTier());
        workflowDTO.setMetadata(REQUESTED_DESCRIPTION_PROPERTY, pendingApplication.getDescription());
        workflowDTO.setMetadata(REQUESTED_SHARED_ORGANIZATION_PROPERTY, pendingApplication.getSharedOrganization());

        if (pendingApplication.getGroupId() != null) {
            workflowDTO.setMetadata(REQUESTED_GROUP_IDS_PROPERTY, pendingApplication.getGroupId());
        }

        String requestedCustomAttributes;
        if (!pendingApplication.getApplicationAttributes().isEmpty()) {
            try {
                requestedCustomAttributes = objectMapper.writeValueAsString(pendingApplication.getApplicationAttributes());
            } catch (JsonProcessingException e) {
                String msg = "Failed to serialize requested custom attributes of application";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
            workflowDTO.setMetadata(REQUESTED_CUSTOM_ATTRIBUTES_PROPERTY, requestedCustomAttributes);
        }

        String message = "Approve update request for application '" + pendingApplication.getName() +
                "' submitted by user: " + applicationWorkflowDTO.getUserName();

        if (applicationAttributesVisibility && existingApplication.getApplicationAttributes() != null && !existingApplication.getApplicationAttributes().isEmpty()) {
            try {
                workflowDTO.setProperties(EXISTING_APPLICATION_ATTRIBUTES_PROPERTY, objectMapper.writeValueAsString(existingApplication.getApplicationAttributes()));
            } catch (JsonProcessingException e) {
                String msg = "Failed to serialize custom attributes of application";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
        }

        workflowDTO.setWorkflowDescription(message);

        super.execute(workflowDTO);
        if (log.isDebugEnabled()) {
            log.debug("Application Update Approval Workflow executed successfully for application: " + existingApplication.getName() + " by owner: " + existingApplication.getOwner());
        }

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
                application.setName(workFlowDTO.getMetadata(REQUESTED_APPLICATION_NAME_PROPERTY));
                application.setTier(workFlowDTO.getMetadata(REQUESTED_TIER_PROPERTY));
                application.setDescription(workFlowDTO.getMetadata(REQUESTED_DESCRIPTION_PROPERTY));

                if (workFlowDTO.getMetadata().containsKey(REQUESTED_CUSTOM_ATTRIBUTES_PROPERTY)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> applicationAttributes =
                            objectMapper.readValue(workFlowDTO.getMetadata(REQUESTED_CUSTOM_ATTRIBUTES_PROPERTY), Map.class);
                    application.setApplicationAttributes(applicationAttributes);
                }

                if (workFlowDTO.getMetadata().containsKey(REQUESTED_GROUP_IDS_PROPERTY)) {
                    application.setGroupId(workFlowDTO.getMetadata(REQUESTED_GROUP_IDS_PROPERTY));
                }

                application.setSharedOrganization(workFlowDTO.getMetadata(REQUESTED_SHARED_ORGANIZATION_PROPERTY));
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.APPLICATION_APPROVED);
                dao.updateApplication(application);
            } else {
                dao.updateApplicationStatus(application.getId(), APIConstants.ApplicationStatus.UPDATE_REJECTED);
            }

        } catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the Application creation with workflow ID :" + workFlowDTO
                    .getWorkflowReference();
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        } catch (JsonProcessingException e) {
            String msg = "Error while parsing custom attributes from workflow metadata";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
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

    public boolean getApplicationAttributesVisibility() {

        return applicationAttributesVisibility;
    }

    public void setApplicationAttributesVisibility(boolean applicationAttributesVisibility) {

        this.applicationAttributesVisibility = applicationAttributesVisibility;
    }
}

