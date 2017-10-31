/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.AccessTokenInfo;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;
import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

import java.util.Collections;
import java.util.List;

public class SampleWorkFlowExecutor extends WorkflowExecutor {

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_API_STATE;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
        return Collections.emptyList();
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        WorkflowResponse workflowResponse = complete(workflowDTO);
        if(workflowDTO instanceof ApplicationRegistrationWorkflowDTO) {
            OAuthApplicationInfo oAuthApplicationInfo = new OAuthApplicationInfo();
            AccessTokenInfo accessTokenInfo = new AccessTokenInfo();
            ((ApplicationRegistrationWorkflowDTO) workflowDTO).setApplicationInfo(oAuthApplicationInfo);
            ((ApplicationRegistrationWorkflowDTO) workflowDTO).setAccessTokenInfo(accessTokenInfo);
        }
        return workflowResponse;
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        return new GeneralWorkflowResponse();
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        if("test_wf_ref".equals(workflowExtRef)) {
            throw new WorkflowException("Wrong workflow external reference provided.");
        }
    }
}
