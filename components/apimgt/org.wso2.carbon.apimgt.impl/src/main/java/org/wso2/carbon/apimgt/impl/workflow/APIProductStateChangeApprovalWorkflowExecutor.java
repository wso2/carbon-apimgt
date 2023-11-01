/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.cleanupPendingTasksByWorkflowReference;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.completeStateChangeWorkflow;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.getSelectedStatesToApprove;
import static org.wso2.carbon.apimgt.impl.workflow.WorkflowUtils.setWorkflowParameters;

public class APIProductStateChangeApprovalWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(APIProductStateChangeApprovalWorkflowExecutor.class);
    private String stateList;

    public String getStateList() {

        return stateList;
    }

    public void setStateList(String stateList) {

        this.stateList = stateList;
    }

    @Override
    public String getWorkflowType() {

        return WorkflowConstants.WF_TYPE_AM_API_PRODUCT_STATE;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {

        return Collections.emptyList();
    }

    /**
     * Execute the API Product state change workflow approval process.
     *
     * @param workflowDTO WorkflowDTO object
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Executing API Product State change Workflow.");
        }
        if (stateList != null) {
            Map<String, List<String>> stateActionMap = getSelectedStatesToApprove(stateList);
            APIStateWorkflowDTO apiStateWorkFlowDTO = (APIStateWorkflowDTO) workflowDTO;

            if (stateActionMap.containsKey(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase()) &&
                    stateActionMap.get(apiStateWorkFlowDTO.getApiCurrentState().toUpperCase())
                            .contains(apiStateWorkFlowDTO.getApiLCAction())) {
                setWorkflowParameters(apiStateWorkFlowDTO);
                super.execute(workflowDTO);
            } else {
                // For any other states, act as simple workflow executor.
                workflowDTO.setStatus(WorkflowStatus.APPROVED);
                // calling super.complete() instead of complete() to act as the simple workflow executor
                super.complete(workflowDTO);
            }
        } else {
            String msg =
                    "State change list is not provided. Please check \"StateList\" property in the workflow configuration.";
            log.error(msg);
            throw new WorkflowException(msg);
        }
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the API Product state change workflow approval process.
     *
     * @param workflowDTO Workflow DTO object
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Completing API Product State change Workflow..");
        }
        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        completeStateChangeWorkflow(workflowDTO);
        return new GeneralWorkflowResponse();
    }


    /**
     * Handle cleanup task for api product state change workflow Approval executor.
     * Use workflow external reference  to delete the pending workflow request
     *
     * @param workflowExtRef External Workflow Reference of pending workflow process
     */
    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {

        if (log.isDebugEnabled()) {
            log.debug("Starting cleanup task for APIStateChangeWSWorkflowExecutor for :" + workflowExtRef);
        }
        super.cleanUpPendingTask(workflowExtRef);
        cleanupPendingTasksByWorkflowReference(workflowExtRef);
    }
}
