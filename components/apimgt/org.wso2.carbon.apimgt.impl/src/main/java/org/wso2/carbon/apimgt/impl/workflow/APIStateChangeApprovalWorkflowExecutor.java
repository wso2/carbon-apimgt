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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Approval workflow for API state change.
 */
public class APIStateChangeApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(APIStateChangeWSWorkflowExecutor.class);
    private String stateList;

    public String getStateList() {
        return stateList;
    }

    public void setStateList(String stateList) {
        this.stateList = stateList;
    }

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_API_STATE;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return Collections.emptyList();
    }

    /**
     * Execute the API state change workflow approval process.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing API State change Workflow.");
        }
        if (stateList != null) {
            Map<String, List<String>> stateActionMap = getSelectedStatesToApprove();
            APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;

            if (stateActionMap.containsKey(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                    && stateActionMap.get(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                    .contains(apiStateWorkFlowDTO.getApiLCAction())) {

                String callBackURL = apiStateWorkFlowDTO.getCallbackUrl();
                String message = "Approval request for API state change action '" + apiStateWorkFlowDTO.getApiLCAction()
                        + "' from '" + apiStateWorkFlowDTO.getApiCurrentState() + "' state for the API '"
                        + apiStateWorkFlowDTO.getApiName() + " : " + apiStateWorkFlowDTO.getApiVersion() + "' by "
                        + apiStateWorkFlowDTO.getApiProvider() + "";
                apiStateWorkFlowDTO.setWorkflowDescription(message);
                apiStateWorkFlowDTO.setMetadata("CurrentState", apiStateWorkFlowDTO.getApiCurrentState());
                apiStateWorkFlowDTO.setMetadata("Action", apiStateWorkFlowDTO.getApiLCAction());
                apiStateWorkFlowDTO.setMetadata("ApiName", apiStateWorkFlowDTO.getApiName());
                apiStateWorkFlowDTO.setMetadata("ApiVersion", apiStateWorkFlowDTO.getApiVersion());
                apiStateWorkFlowDTO.setMetadata("ApiProvider", apiStateWorkFlowDTO.getApiProvider());
                apiStateWorkFlowDTO.setMetadata("Invoker", apiStateWorkFlowDTO.getInvoker());
                apiStateWorkFlowDTO.setMetadata("TenantId", String.valueOf(apiStateWorkFlowDTO.getTenantId()));

                apiStateWorkFlowDTO.setProperties("application", apiStateWorkFlowDTO.getApiLCAction());
                apiStateWorkFlowDTO.setProperties("apiName", apiStateWorkFlowDTO.getApiName());
                apiStateWorkFlowDTO.setProperties("apiVersion", apiStateWorkFlowDTO.getApiVersion());
                apiStateWorkFlowDTO.setProperties("apiProvider", apiStateWorkFlowDTO.getApiProvider());
                super.execute(workflowDTO);
            } else {
                // For any other states, act as simple workflow executor.
                workflowDTO.setStatus(WorkflowStatus.APPROVED);
                // calling super.complete() instead of complete() to act as the simpleworkflow executor
                super.complete(workflowDTO);
            }
        } else {
            String msg = "State change list is not provided. Please check <stateList> element in workflow-extensions.xml";
            log.error(msg);
            throw new WorkflowException(msg);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the API state change workflow approval process.
     *
     * @param workflowDTO
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Completing API State change Workflow..");
        }
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        String externalWorkflowRef = workflowDTO.getExternalWorkflowReference();
        try {
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            Workflow workflow = apiMgtDAO.getworkflowReferenceByExternalWorkflowReference(externalWorkflowRef);
            String apiName = workflow.getMetadata("ApiName");
            String action = workflow.getMetadata("Action");
            String providerName = workflow.getMetadata("ApiProvider");
            String version = workflow.getMetadata("ApiVersion");
            String invoker = workflow.getMetadata("Invoker");
            String currentStatus = workflow.getMetadata("CurrentState");
            int tenantId = workflowDTO.getTenantId();
            try {
                //tenant flow is already started from the rest api service impl. no need to start from here
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(invoker);
                Registry registry = ServiceReferenceHolder.getInstance().getRegistryService()
                        .getGovernanceUserRegistry(invoker, tenantId);
                APIIdentifier apiIdentifier = new APIIdentifier(providerName, apiName, version);
                GenericArtifact apiArtifact = APIUtil.getAPIArtifact(apiIdentifier, registry);
                if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
                    String targetStatus;
                    apiArtifact.invokeAction(action, APIConstants.API_LIFE_CYCLE);
                    targetStatus = apiArtifact.getLifecycleState();
                    if (!currentStatus.equals(targetStatus)) {
                        apiMgtDAO.recordAPILifeCycleEvent(apiIdentifier, currentStatus.toUpperCase(),
                                targetStatus.toUpperCase(), invoker, tenantId);
                    }
                    if (log.isDebugEnabled()) {
                        String logMessage = "API Status changed successfully. API Name: " + apiIdentifier.getApiName()
                                + ", API Version " + apiIdentifier.getVersion() + ", New Status : " + targetStatus;
                        log.debug(logMessage);
                    }
                }
            } catch (RegistryException e) {
                String errorMsg = "Could not complete api state change workflow";
                log.error(errorMsg, e);
                throw new WorkflowException(errorMsg, e);
            }
        } catch (APIManagementException e) {
            String errorMsg = "Could not complete api state change workflow";
            log.error(errorMsg, e);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Handle cleanup task for api state change workflow Approval executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef External Workflow Reference of pending workflow process
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for APIStateChangeWSWorkflowExecutor for :" + workflowExtRef);
        }
        String errorMsg;
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

    /**
     * Read the user provided lifecycle states for the approval task. These are provided in the workflow-extension.xml
     */
    private Map<String, List<String>> getSelectedStatesToApprove() {
        Map<String, List<String>> stateAction = new HashMap<String, List<String>>();
        // exract selected states from stateList and populate the map
        if (stateList != null) {
            // list will be something like ' Created:Publish,Created:Deploy as a Prototype,Published:Block ' String
            // It will have State:action pairs
            String[] statelistArray = stateList.split(",");
            for (int i = 0; i < statelistArray.length; i++) {
                String[] stateActionArray = statelistArray[i].split(":");
                if (stateAction.containsKey(stateActionArray[0].toUpperCase())) {
                    ArrayList<String> actionList = (ArrayList<String>) stateAction
                            .get(stateActionArray[0].toUpperCase());
                    actionList.add(stateActionArray[1]);
                } else {
                    ArrayList<String> actionList = new ArrayList<String>();
                    actionList.add(stateActionArray[1]);
                    stateAction.put(stateActionArray[0].toUpperCase(), actionList);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("selected states: " + stateAction.toString());
        }
        return stateAction;
    }
}
