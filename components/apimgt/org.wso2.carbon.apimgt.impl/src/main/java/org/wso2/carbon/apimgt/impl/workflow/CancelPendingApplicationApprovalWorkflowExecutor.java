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
 * Simple workflow executor for application delete action
 */
public class CancelPendingApplicationApprovalWorkflowExecutor extends ApplicationDeletionSimpleWorkflowExecutor {

    private String serviceEndpoint;

    private String username;

    private String password;

    private String contentType;

    private static final Log log = LogFactory.getLog(ApplicationCreationWSWorkflowExecutor.class);

    @Override
    public void execute(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workflowDTO);
        super.publishEvents(workflowDTO);
    }

    @Override
    public void complete(WorkflowDTO workflowDTO) throws WorkflowException {
    }

    public String getServiceEndpoint() {
        return serviceEndpoint;
    }

    public void setServiceEndpoint(String serviceEndpoint) {
        this.serviceEndpoint = serviceEndpoint;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
