/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.models.SubscriptionWorkflow;
import org.wso2.carbon.apimgt.core.models.Workflow;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;

/**
 * Creates workflow with a given workflow type.
 */
public class WorkflowExecutorFactory {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutorFactory.class);

    private static final WorkflowExecutorFactory instance = new WorkflowExecutorFactory();

    private WorkflowExecutorFactory() {
    }

    public static WorkflowExecutorFactory getInstance() {
        return instance;
    }


    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType)
            throws WorkflowException {
        WorkflowConfigHolder holder = null;
        try {
            holder = this.getWorkflowConfigurations();
            return holder.getWorkflowExecutor(workflowExecutorType);
        } catch (WorkflowException e) {
            handleException("Error while creating WorkFlowDTO for " + workflowExecutorType, e);
        } 
        return null;
    }

    /**
     * Create a DTO object related to a given workflow type.
     *
     * @param workflowType Type of the workflow.
     */
    public Workflow createWorkflow(String workflowType) {
        Workflow workflow = null;
        if (WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equals(workflowType)) {
            workflow = new ApplicationCreationWorkflow();
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(workflowType)) {
            workflow = new SubscriptionWorkflow();
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_USER_SIGNUP.equals(workflowType)) {
            //workflowDTO = new WorkflowDTO();
            //workflowDTO.setWorkflowType(wfType);
            workflow = new Workflow(); //TODO use correct workflow when implementing
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_API_STATE.equals(workflowType)) {
            //workflowDTO = new APIStateWorkflowDTO();
            //workflowDTO.setWorkflowType(wfType);
            workflow = new Workflow();  //TODO use correct workflow when implementing
            workflow.setWorkflowType(workflowType);
        }
        
        return workflow;
    }

    public WorkflowConfigHolder getWorkflowConfigurations() throws WorkflowException {

        // TODO stop loading every time and move it to a static variable and do a check
        WorkflowConfigHolder workflowConfig = new WorkflowConfigHolder();
        workflowConfig.load();
        return workflowConfig;

    }

    private void handleException(String msg, Exception e) throws WorkflowException {
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }


}
