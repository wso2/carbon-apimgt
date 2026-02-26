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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.*;

/**
 * Approval workflow for API Subscription Tier Update.
 */
public class SubscriptionUpdateApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(SubscriptionUpdateApprovalWorkflowExecutor.class);
    private static final String API_NAME_PROPERTY = "apiName";
    private static final String API_VERSION_PROPERTY = "apiVersion";
    private static final String SUBSCRIBER_PROPERTY = "subscriber";
    private static final String APPLICATION_NAME_PROPERTY = "applicationName";
    private static final String SUBSCRIPTION_TIER_PROPERTY = "Subscription Tier";
    private static final String UPDATES_PROPERTY = "updates";

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_UPDATE;
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
            log.debug("Executing Subscription Update Webservice Workflow.. ");
        }
        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String message = "Approve API " + subsWorkflowDTO.getApiName() + " - " + subsWorkflowDTO.getApiVersion() +
                " subscription update request from subscriber - " + subsWorkflowDTO.getSubscriber() +
                " for the application - " + subsWorkflowDTO.getApplicationName();
        workflowDTO.setWorkflowDescription(message);
        workflowDTO.setProperties(API_NAME_PROPERTY, subsWorkflowDTO.getApiName());
        workflowDTO.setProperties(API_VERSION_PROPERTY, subsWorkflowDTO.getApiVersion());
        workflowDTO.setProperties(APPLICATION_NAME_PROPERTY, subsWorkflowDTO.getApplicationName());
        workflowDTO.setProperties(SUBSCRIPTION_TIER_PROPERTY, subsWorkflowDTO.getTierName());
        workflowDTO.setProperties(SUBSCRIBER_PROPERTY, subsWorkflowDTO.getSubscriber());

        List<Map<String, String>> subscriptionUpdateDiffs = new ArrayList<>();

        compareAndAddToSubscriptionUpdateDiffs(subscriptionUpdateDiffs, SUBSCRIPTION_TIER_PROPERTY,
                subsWorkflowDTO.getTierName(), subsWorkflowDTO.getRequestedTierName());

        ObjectMapper objectMapper = new ObjectMapper();
        if (!subscriptionUpdateDiffs.isEmpty()) {
            try {
                workflowDTO.setProperties(UPDATES_PROPERTY, objectMapper.writeValueAsString(subscriptionUpdateDiffs));
            } catch (JsonProcessingException e) {
                throw new WorkflowException(
                        "Failed to serialize subscription update differences to JSON", e);
            }
        }

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
        if (log.isDebugEnabled()) {
            String logMessage = "Subscription Update [Complete] Workflow Invoked. Workflow ID : " + workflowDTO
                    .getExternalWorkflowReference() + " Workflow State : " + workflowDTO.getStatus();
            log.debug(logMessage);
        }
        SubscriptionWorkflowDTO subscriptionWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatusAndTier(Integer.parseInt(subscriptionWorkflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.UNBLOCKED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription update workflow", e);
                throw new WorkflowException("Could not complete subscription update workflow", e);
            }
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            try {
                apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(subscriptionWorkflowDTO.getWorkflowReference()),
                        APIConstants.SubscriptionStatus.UNBLOCKED);
            } catch (APIManagementException e) {
                log.error("Could not complete subscription update workflow", e);
                throw new WorkflowException("Could not complete subscription update workflow", e);
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
            log.debug("Starting cleanup task for SubscriptionUpdateApprovalWorkflowExecutor for :" + workflowExtRef);
        }
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            apiMgtDAO.deleteWorkflowRequest(workflowExtRef);
        } catch (APIManagementException axisFault) {
            errorMsg = "Error sending out cancel pending subscription update approval process message. cause: " + axisFault
                    .getMessage();
            throw new WorkflowException(errorMsg, axisFault);
        }
    }
}
