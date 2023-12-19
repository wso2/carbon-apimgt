/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIRevisionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

/**
 * This is a simple work flow extension to have API revision deployment process
 */
public class APIRevisionDeploymentSimpleWorkflowExecutor extends WorkflowExecutor {
    private static final Log log = LogFactory.getLog(APIRevisionDeploymentSimpleWorkflowExecutor.class);
    private static final String ENVIRONMENT_PROPERTY = "environment";

    @Override public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT;
    }

    /**
     * Execute the API Revision Deployment workflow simple process.
     *
     * @param workFlowDTO WorkflowDTO object
     * @return WorkflowResponse object
     * @throws WorkflowException if failed to execute the workflow
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workFlowDTO) throws WorkflowException {
        APIRevisionWorkflowDTO revisionWorkFlowDTO = (APIRevisionWorkflowDTO) workFlowDTO;

        if (log.isDebugEnabled()) {
            log.debug("Executing API Revision Deployment Workflow: " + revisionWorkFlowDTO.getWorkflowReference());
        }

        workFlowDTO.setStatus(WorkflowStatus.APPROVED);
        workFlowDTO.setMetadata(ENVIRONMENT_PROPERTY, revisionWorkFlowDTO.getEnvironment());
        complete(workFlowDTO);
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the API Revision Deployment simple workflow process.
     *
     * @param workFlowDTO WorkflowDTO object
     * @return WorkflowResponse object
     * @throws WorkflowException if failed to complete the workflow
     */
    @Override public WorkflowResponse complete(WorkflowDTO workFlowDTO) throws WorkflowException {
        APIRevisionWorkflowDTO revisionWorkFlowDTO = (APIRevisionWorkflowDTO) workFlowDTO;

        if (log.isDebugEnabled()) {
            log.debug("Complete  API Revision Deployment Workflow: " + revisionWorkFlowDTO.getWorkflowReference());
        }

        String status = mapWorkflowStatusToAPIRevisionStatus(workFlowDTO.getStatus());

        ApiMgtDAO dao = ApiMgtDAO.getInstance();

        try {
            dao.updateAPIRevisionDeploymentStatus(workFlowDTO.getWorkflowReference(), status,
                    revisionWorkFlowDTO.getEnvironment());
        } catch (APIManagementException e) {
            String msg = "Error occurred when updating the status of the API revision: "
                    + revisionWorkFlowDTO.getWorkflowReference() + " deployment process";
            log.error(msg, e);
            throw new WorkflowException(msg, e);
        }
        return new GeneralWorkflowResponse();
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
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
