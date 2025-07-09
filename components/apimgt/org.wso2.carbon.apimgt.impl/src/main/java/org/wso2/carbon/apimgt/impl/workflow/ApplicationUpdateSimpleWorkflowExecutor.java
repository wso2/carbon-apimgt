/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

/**
 * Simple workflow for Application update process. This is the default workflow.
 */
public class ApplicationUpdateSimpleWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(ApplicationUpdateSimpleWorkflowExecutor.class);


    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE;
    }

    /**
     * This method executes application update simple workflow
     *
     * @param workFlowDTO - The WorkflowDTO which contains workflow contextual information related to the workflow.
     * @return
     * @throws WorkflowException
     */
    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Executing Application Update Workflow..");
        }
        workFlowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workFlowDTO);
        return new GeneralWorkflowResponse();
    }

    /**
     * Completes the application update approval workflow
     *
     * @param workflowDTO - The WorkflowDTO which contains workflow contextual information related to the workflow.
     * @return
     * @throws WorkflowException
     */
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.debug("Complete  Application Update Workflow..");
        }
        String status = null;

        if (WorkflowStatus.APPROVED.equals(workflowDTO.getStatus())) {
            status = APIConstants.ApplicationStatus.APPLICATION_APPROVED;
        } else if (WorkflowStatus.CREATED.equals(workflowDTO.getStatus())) {
            status = APIConstants.ApplicationStatus.APPLICATION_CREATED;
        } else if (WorkflowStatus.REJECTED.equals(workflowDTO.getStatus())) {
            status = APIConstants.ApplicationStatus.UPDATE_REJECTED;
        }
        ApiMgtDAO dao = ApiMgtDAO.getInstance();
        try {
            ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
            dao.updateApplicationStatus(Integer.parseInt(workflowDTO.getWorkflowReference()), status);
            dao.updateApplication(applicationWorkflowDTO.getApplication());
        } catch (APIManagementException e) {
            String msg = "Error occurred when updating Application update process";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }
}
