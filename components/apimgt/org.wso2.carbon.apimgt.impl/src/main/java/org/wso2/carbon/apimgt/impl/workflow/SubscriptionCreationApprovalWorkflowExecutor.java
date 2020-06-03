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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

/**
 * Approval workflow for API Subscription.
 */
public class SubscriptionCreationApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(SubscriptionCreationApprovalWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    /**
     * Execute the Application Creation workflow approval process.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing Subscription Creation Webservice Workflow.. ");
        }
        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String message = "Approve API " + subsWorkflowDTO.getApiName() + " - " + subsWorkflowDTO.getApiVersion() +
                " subscription creation request from subscriber - " + subsWorkflowDTO.getSubscriber() +
                " for the application - " + subsWorkflowDTO.getApplicationName();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties("apiName", subsWorkflowDTO.getApiName());
        workflowDTO.setProperties("apiVersion", subsWorkflowDTO.getApiVersion());
        workflowDTO.setProperties("subscriber", subsWorkflowDTO.getSubscriber());
        workflowDTO.setProperties("applicationName", subsWorkflowDTO.getApplicationName());
        super.execute(workflowDTO);

        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the Approval workflow executor for Subscription creation.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        log.info("Subscription Creation [Complete] Workflow Invoked. Workflow ID : " + workflowDTO
                .getExternalWorkflowReference() + "Workflow State : " + workflowDTO.getStatus());

        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.UNBLOCKED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.REJECTED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription creation workflow", e);
                throw new WorkflowException("Could not complete subscription creation workflow", e);
            }
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Handle cleanup task for subscription creation Approval workflow executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef Workflow external reference of pending workflow request
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        String errorMsg = null;
        super.cleanUpPendingTask(workflowExtRef);
        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for SubscriptionCreationApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending subscription approval process message. cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }
}
