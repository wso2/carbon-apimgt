/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.List;

/**
 * WS workflow executor for application delete action
 */
public class ApplicationDeletionSimpleWorkflowExecutor extends WorkflowExecutor {


    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return null;
    }

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException {
        super.execute(workflowDTO);
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException {
        super.complete(workflowDTO);
    }

}
