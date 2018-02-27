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
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;

/**
 * Creates workflow with a given workflow type.
 */
public class WorkflowExecutorFactory {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutorFactory.class);

    private static final WorkflowExecutorFactory instance = new WorkflowExecutorFactory();

    private WorkflowConfigHolder holder = null;

    private WorkflowExecutorFactory() {
    }

    public static WorkflowExecutorFactory getInstance() {
        return instance;
    }


    public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType)
            throws WorkflowException {

        try {
            if (holder == null) {
                holder = new WorkflowConfigHolder();
                holder.load();
            }
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
     * @return WorkFlow instance
     */
    public Workflow createWorkflow(String workflowType) throws APIMgtDAOException {
        Workflow workflow = null;
        if (WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION.equals(workflowType)) {
            workflow = new ApplicationCreationWorkflow(DAOFactory.getApplicationDAO(), DAOFactory.getWorkflowDAO(),
                    APIManagerFactory.getInstance().getApiGateway());
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_DELETION.equals(workflowType)) {
            workflow = new ApplicationDeletionWorkflow(DAOFactory.getApplicationDAO(), DAOFactory.getWorkflowDAO(),
                    APIManagerFactory.getInstance().getApiGateway());
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(workflowType)) {
            workflow = new SubscriptionCreationWorkflow(DAOFactory.getAPISubscriptionDAO(), DAOFactory.getWorkflowDAO(),
                    APIManagerFactory.getInstance().getApiGateway());
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION.equals(workflowType)) {
            workflow = new SubscriptionDeletionWorkflow(DAOFactory.getAPISubscriptionDAO(), DAOFactory.getWorkflowDAO(),
                    APIManagerFactory.getInstance().getApiGateway());
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_API_STATE.equals(workflowType)) {
            workflow = new APIStateChangeWorkflow(DAOFactory.getApiDAO(), DAOFactory.getAPISubscriptionDAO(),
                    DAOFactory.getWorkflowDAO(), APIManagerFactory.getInstance().geApiLifecycleManager(),
                    APIManagerFactory.getInstance().getApiGateway(), DAOFactory.getLabelDAO());
            workflow.setWorkflowType(workflowType);
        } else if (WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE.equals(workflowType)) {
            workflow = new ApplicationUpdateWorkflow(DAOFactory.getApplicationDAO(), DAOFactory.getWorkflowDAO(),
                    APIManagerFactory.getInstance().getApiGateway());
            workflow.setWorkflowType(workflowType);
        } else {
            throw new APIMgtDAOException("Invalid workflow type: " + workflowType + " specified",
                    ExceptionCodes.WORKFLOW_INVALID_WFTYPE);
        }
        return workflow;
    }

    private void handleException(String msg, Exception e) throws WorkflowException {
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }


}
