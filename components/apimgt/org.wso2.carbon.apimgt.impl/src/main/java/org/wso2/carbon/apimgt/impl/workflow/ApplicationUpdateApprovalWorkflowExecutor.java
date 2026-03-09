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
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.extractCustomAttributeDiffs;

/**
 * Approval workflow for Application Update
 */
public class ApplicationUpdateApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationUpdateApprovalWorkflowExecutor.class);
    private static final ApiMgtDAO dao = ApiMgtDAO.getInstance();
    private boolean applicationAttributesVisibility = false;
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
    private static final String APPLICATION_DESCRIPTION_PROPERTY = "applicationDescription";
    private static final String APPLICATION_NAME_LABEL = "Application Name";
    private static final String TIER_LABEL = "Tier";
    private static final String DESCRIPTION_LABEL = "Description";
    private static final String GROUPS_LABEL = "Groups";
    private static final String SHARING_WITH_ORGANIZATION_LABEL = "Sharing with the organization";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TENANT_DOMAIN_PROPERTY = "tenantDomain";
    private static final String GROUP_ID_PROPERTY = "groupId";
    private static final String SHARED_ORGANIZATION_PROPERTY = "sharedOrganization";

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
            log.debug("Executing Application Update Approval Workflow. Workflow Reference: " + workflowDTO.getWorkflowReference());
        }
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application pendingApplication = applicationWorkflowDTO.getApplication();
        Application existingApplication = applicationWorkflowDTO.getExistingApplication();

        workflowDTO.setProperties(APPLICATION_NAME_PROPERTY, existingApplication.getName());
        workflowDTO.setProperties(APPLICATION_TIER_PROPERTY, existingApplication.getTier());
        workflowDTO.setProperties(APPLICATION_OWNER_PROPERTY, existingApplication.getOwner());
        workflowDTO.setProperties(TENANT_DOMAIN_PROPERTY, applicationWorkflowDTO.getTenantDomain());

        if (StringUtils.isNotBlank(existingApplication.getGroupId())) {
            workflowDTO.setProperties(GROUP_ID_PROPERTY, existingApplication.getGroupId());
        }

        if (StringUtils.isNotBlank(existingApplication.getSharedOrganization())) {
            workflowDTO.setProperties(SHARED_ORGANIZATION_PROPERTY, existingApplication.getSharedOrganization());
        }

        if (StringUtils.isNotBlank(existingApplication.getDescription())) {
            workflowDTO.setProperties(APPLICATION_DESCRIPTION_PROPERTY, existingApplication.getDescription());
        }

        List<Map<String, String>> applicationUpdateDiffs = new ArrayList<>();

        compareAndAddToUpdateDiffs(applicationUpdateDiffs, APPLICATION_NAME_LABEL,
                existingApplication.getName(), pendingApplication.getName());

        compareAndAddToUpdateDiffs(applicationUpdateDiffs, TIER_LABEL,
                existingApplication.getTier(), pendingApplication.getTier());

        compareAndAddToUpdateDiffs(applicationUpdateDiffs, DESCRIPTION_LABEL,
                existingApplication.getDescription(), pendingApplication.getDescription());

        compareAndAddToUpdateDiffs(applicationUpdateDiffs, GROUPS_LABEL,
                existingApplication.getGroupId(), pendingApplication.getGroupId());

        // Special case: since the shared organization (getSharedOrganization) is an uuid when
        // "Sharing with the organization" is enabled
        compareAndAddToUpdateDiffs(applicationUpdateDiffs, SHARING_WITH_ORGANIZATION_LABEL,
                getShareWithOrganizationStatus(existingApplication.getSharedOrganization()),
                getShareWithOrganizationStatus(pendingApplication.getSharedOrganization()));

        Map<String, String> existingApplicationAttributes = existingApplication.getApplicationAttributes();
        Map<String, String> pendingApplicationAttributes = pendingApplication.getApplicationAttributes();

        List<Map<String, String>> attributeDiffs =
                extractCustomAttributeDiffs(existingApplicationAttributes, pendingApplicationAttributes);

        if (applicationAttributesVisibility && !attributeDiffs.isEmpty()) {
            applicationUpdateDiffs.addAll(attributeDiffs);
        }

        String message = String.format(
                "Approve update request for application '%s' submitted by user: %s.",
                existingApplication.getName(),
                applicationWorkflowDTO.getUserName()
        );

        // When applicationAttributesVisibility is disabled (default), updates to custom
        // application attributes are not included in the "updates" list displayed in
        // the Admin Portal approval task. If attribute changes exist but are not added
        // due to this configuration, append a note to the workflow description so that
        // the approver is aware that attribute updates are present but not displayed.
        if (!applicationAttributesVisibility && !attributeDiffs.isEmpty()) {
            message += " Note that updates to application attributes are present, but they are not "
                    + "displayed here because applicationAttributesVisibility is currently disabled "
                    + "in the Admin Portal.";
        }

        String applicationUpdateDiffJson;
        if (!applicationUpdateDiffs.isEmpty()) {
            try {
                applicationUpdateDiffJson = OBJECT_MAPPER.writeValueAsString(applicationUpdateDiffs);
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

        if (StringUtils.isNotBlank(pendingApplication.getGroupId())) {
            workflowDTO.setMetadata(REQUESTED_GROUP_IDS_PROPERTY, pendingApplication.getGroupId());
        }

        String requestedCustomAttributes;
        if (pendingApplicationAttributes != null && !pendingApplicationAttributes.isEmpty()) {
            try {
                requestedCustomAttributes = OBJECT_MAPPER.writeValueAsString(pendingApplicationAttributes);
            } catch (JsonProcessingException e) {
                String msg = "Failed to serialize requested custom attributes of application";
                log.error(msg, e);
                throw new WorkflowException(msg, e);
            }
            workflowDTO.setMetadata(REQUESTED_CUSTOM_ATTRIBUTES_PROPERTY, requestedCustomAttributes);
        }

        WorkflowUtils.populateApplicationAttributes(workflowDTO, existingApplication, applicationAttributesVisibility);

        workflowDTO.setWorkflowDescription(message);

        super.execute(workflowDTO);
        if (log.isDebugEnabled()) {
            log.debug("Application Update Approval Workflow executed successfully. Workflow Reference: "
                    + workflowDTO.getWorkflowReference());
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

