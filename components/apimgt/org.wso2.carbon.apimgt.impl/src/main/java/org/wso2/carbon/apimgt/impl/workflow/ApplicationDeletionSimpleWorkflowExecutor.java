/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * WS workflow executor for application delete action
 */
public class ApplicationDeletionSimpleWorkflowExecutor extends WorkflowExecutor {
    private static final Log log = LogFactory.getLog(ApplicationDeletionSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {

        // implemetation is not provided in this version
        return null;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workflowDTO);
        super.publishEvents(workflowDTO);
        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        ApplicationWorkflowDTO applicationWorkflowDTO = (ApplicationWorkflowDTO) workflowDTO;
        Application application = applicationWorkflowDTO.getApplication();
        String errorMsg = null;

        try {
            apiMgtDAO.deleteApplication(application);
        } catch (APIManagementException e) {
            if (e.getMessage() == null) {
                errorMsg = "Couldn't complete simple application deletion workflow for application: " + application
                        .getName();
            } else {
                errorMsg = e.getMessage();
            }
            throw new WorkflowException(errorMsg, e);
        }

        return new GeneralWorkflowResponse();
    }

}
