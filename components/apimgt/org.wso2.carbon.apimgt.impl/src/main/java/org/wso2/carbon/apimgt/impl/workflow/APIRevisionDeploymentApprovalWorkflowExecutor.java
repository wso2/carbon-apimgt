/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRevisionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

/**
 * Approval workflow for API Revision Deployment.
 */
public class APIRevisionDeploymentApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(APIRevisionDeploymentApprovalWorkflowExecutor.class);

    private static final String REVISION_ID_PROPERTY = "revisionId";
    private static final String API_NAME_PROPERTY = "apiName";
    private static final String API_VERSION_PROPERTY = "apiVersion";
    private static final String ENVIRONMENT_PROPERTY = "environment";
    private static final String USERNAME_PROPERTY = "userName";
    private static final String API_PROVIDER_PROPERTY = "apiProvider";
    private static final String API_ID_PROPERTY = "apiId";

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT;
    }

    /**
     * Execute the API Revision Deployment workflow approval process.
     *
     * @param workflowDTO WorkflowDTO object
     * @return WorkflowResponse object
     * @throws WorkflowException if failed to execute the workflow
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        APIRevisionWorkflowDTO revisionWorkFlowDTO = (APIRevisionWorkflowDTO) workflowDTO;
        APIRevision revision = revisionWorkFlowDTO.getAPIRevision();

        if (log.isDebugEnabled()) {
            log.debug("Executing API Revision Deployment Workflow: " + revisionWorkFlowDTO.getWorkflowReference());
        }

        String message = "Approve revision " + revision.getId() + " deployment request from the user "
                + revisionWorkFlowDTO.getUserName() + " for the environment " + revisionWorkFlowDTO.getEnvironment()
                + " of the API " + revisionWorkFlowDTO.getApiName();
        workflowDTO.setWorkflowDescription(message);

        // set properties for Admin UI rendering
        workflowDTO.setProperties(REVISION_ID_PROPERTY, String.valueOf(revision.getId()));
        workflowDTO.setProperties(API_NAME_PROPERTY, revisionWorkFlowDTO.getApiName());
        workflowDTO.setProperties(API_VERSION_PROPERTY, revisionWorkFlowDTO.getApiVersion());
        workflowDTO.setProperties(ENVIRONMENT_PROPERTY, revisionWorkFlowDTO.getEnvironment());
        workflowDTO.setProperties(USERNAME_PROPERTY, revisionWorkFlowDTO.getUserName());

        // set properties for Admin backend logic
        workflowDTO.setMetadata(REVISION_ID_PROPERTY, revisionWorkFlowDTO.getRevisionId());
        workflowDTO.setMetadata(ENVIRONMENT_PROPERTY, revisionWorkFlowDTO.getEnvironment());
        workflowDTO.setMetadata(USERNAME_PROPERTY, revisionWorkFlowDTO.getUserName());
        workflowDTO.setMetadata(API_PROVIDER_PROPERTY, revisionWorkFlowDTO.getApiProvider());
        workflowDTO.setMetadata(API_ID_PROPERTY, revision.getApiUUID());

        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the API Revision Deployment approval workflow process.
     *
     * @param workFlowDTO WorkflowDTO object
     * @return WorkflowResponse object
     * @throws WorkflowException if failed to complete the workflow
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {

        workFlowDTO.setUpdatedTime(System.currentTimeMillis());
        String revisionId = workFlowDTO.getWorkflowReference();

        if (log.isDebugEnabled()) {
            log.debug("Complete  API Revision Deployment Workflow: " + revisionId);
        }

        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        WorkflowStatus revisionWFState = workFlowDTO.getStatus();
        String environment = workFlowDTO.getMetadata(ENVIRONMENT_PROPERTY);
        try {
            if (dao.getAPIRevisionDeploymentByNameAndRevsionID(environment, revisionId) != null) {
                super.complete(workFlowDTO);
                if (log.isDebugEnabled()) {
                    String logMessage = "API Revision Deployment [Complete] Workflow Invoked. Workflow ID : "
                            + workFlowDTO.getExternalWorkflowReference() + " Workflow State : " + revisionWFState;
                    log.debug(logMessage);
                }
                String status = mapWorkflowStatusToAPIRevisionStatus(revisionWFState);
                try {
                    dao.updateAPIRevisionDeploymentStatus(revisionId, status, environment);
                } catch (APIManagementException e) {
                    String msg = "Error occurred when updating the status of the API Revision: " + revisionId
                            + " for the API: " + workFlowDTO.getMetadata(API_NAME_PROPERTY) + "-"
                            + workFlowDTO.getMetadata(API_VERSION_PROPERTY);
                    log.error(msg, e);
                    throw new WorkflowException(msg, e);
                }
            } else {
                String msg = "Revision Id: " + revisionId + "does not exist";
                throw new WorkflowException(msg);
            }
        } catch (APIManagementException e) {
            String msg = "Error occurred when retrieving the API Revision Deployment with workflow ID :"
                    + workFlowDTO.getWorkflowReference();
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
     * Handle cleanup task for API Revision Deployment Approval workflow executor.
     * Use workflow external reference to delete the pending workflow request
     *
     * @param workflowExtRef Workflow external reference of pending workflow request
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        String errorMsg;
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for APIRevisionDeploymentApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending api revision deployment process message. cause: "
                    + axisFault.getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }

    /**
     * Return the status of the workflowDTO
     *
     * @param workflowStatus - status of the workflow
     * @return status of the workflowDTO
     */
    private String mapWorkflowStatusToAPIRevisionStatus(WorkflowStatus workflowStatus) {
        switch (workflowStatus) {
        case CREATED:
            return APIConstants.APIRevisionStatus.API_REVISION_CREATED;
        case APPROVED:
            return APIConstants.APIRevisionStatus.API_REVISION_APPROVED;
        case REJECTED:
            return APIConstants.APIRevisionStatus.API_REVISION_REJECTED;
        default:
            // Handle other cases if necessary
            return null;
        }
    }
}
