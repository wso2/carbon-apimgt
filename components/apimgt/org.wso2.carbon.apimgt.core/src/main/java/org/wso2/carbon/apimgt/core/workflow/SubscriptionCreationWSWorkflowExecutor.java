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

package org.wso2.carbon.apimgt.core.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;

/**
 * This workflow connects to external bpmn process engine
 */
public class SubscriptionCreationWSWorkflowExecutor implements WorkflowExecutor {
    private static final Log log = LogFactory.getLog(SubscriptionCreationWSWorkflowExecutor.class);
    
    private String processDefinitionKey;
    private String username;
    private String password;

    /**
     * Execute the workflow executor
     *
     * @param workFlow
     * @throws WorkflowException
     */

    public WorkflowResponse execute(Workflow workFlow) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Executing Subscription creation WS Workflow..");
        }
    
        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        //set the state to approved
        workflowResponse.setWorkflowStatus(WorkflowStatus.CREATED);
        
        //TODO add BPMN rest calls
        log.info("======= Defined Property: processDefinitionKey - " + getProcessDefinitionKey() + ", username -"
                + getUsername());
        log.info("======= WF external ref: " + workFlow.getExternalWorkflowReference());
        
        
        return workflowResponse;
    }

    /**
     * Complete the external process status
     * Based on the workflow status we will update the status column of the
     * Application table
     *
     * @param workFlow - Workflow
     */
    public WorkflowResponse complete(Workflow workFlow) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Complete  Subscription creation WS Workflow..");
        }
        
        log.info("======= WF Complete() =====" + workFlow.toString()); 
        
        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(workFlow.getStatus());
        return workflowResponse;
    }

    @Override
    public void cleanUpPendingTask(String workflowExtRef) throws WorkflowException {
        
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey) {
        this.processDefinitionKey = processDefinitionKey;
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
    
}
