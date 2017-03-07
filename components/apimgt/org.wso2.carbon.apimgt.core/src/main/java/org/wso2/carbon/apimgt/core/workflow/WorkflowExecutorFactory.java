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
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.models.Workflow;

import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

//
//import org.wso2.carbon.apimgt.impl.APIConstants;
//import org.wso2.carbon.apimgt.impl.dto.ApplicationRegistrationWorkflowDTO;
//import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
//import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
//import org.wso2.carbon.apimgt.core.models.Workflow;
//import org.wso2.carbon.context.PrivilegedCarbonContext;
//import org.wso2.carbon.registry.core.exceptions.RegistryException;

//import javax.cache.Cache;
//import javax.cache.Caching;

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

    //    private static void handleException(String msg) throws WorkflowException {
    //        log.error(msg);
    //        throw new WorkflowException(msg);
    //    }
    //
    //    private static void handleException(String msg, Exception e) throws WorkflowException {
    //        log.error(msg, e);
    //        throw new WorkflowException(msg, e);
    //    }
    //
        public WorkflowExecutor getWorkflowExecutor(String workflowExecutorType)
                throws WorkflowException, NoSuchMethodException, InvocationTargetException {
            WorkflowConfigHolder holder = null;
            try {
                holder = this.getWorkflowConfigurations();
                return holder.getWorkflowExecutor(workflowExecutorType);
            } catch (WorkflowException e) {
                handleException("Error while creating WorkFlowDTO for " + workflowExecutorType, e);
            } catch (FileNotFoundException e) {
                log.error("File not found.", e);
            }
            return null;
        }

    /**
     * Create a DTO object related to a given workflow type.
     *
     * @param wfType Type of the workflow.
     */
    public Workflow createWorkflow(String wfType) {
        Workflow workflow = null;
        if (wfType.equals("AM_APPLICATION_CREATION")) {
            workflow = new ApplicationCreationWorkflow();
            workflow.setWorkflowType(wfType);
        }
        //        }else if(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_PRODUCTION.equals(wfType)){
        //            workflowDTO = new ApplicationRegistrationWorkflowDTO();
        //            ((ApplicationRegistrationWorkflow)workflowDTO).setKeyType(APIConstants.API_KEY_TYPE_PRODUCTION);
        //            workflowDTO.setWorkflowType(wfType);
        //        }else if(WorkflowConstants.WF_TYPE_AM_APPLICATION_REGISTRATION_SANDBOX.equals(wfType)){
        //            workflowDTO = new ApplicationRegistrationWorkflowDTO();
        //            ((ApplicationRegistrationWorkflowDTO)workflowDTO).setKeyType(APIConstants.API_KEY_TYPE_SANDBOX);
        //            workflowDTO.setWorkflowType(wfType);
        //        }else if(WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION.equals(wfType)){
        //            workflowDTO = new SubscriptionWorkflowDTO();
        //            workflowDTO.setWorkflowType(wfType);
        //        }else if(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP.equals(wfType)){
        //            workflowDTO = new WorkflowDTO();
        //            workflowDTO.setWorkflowType(wfType);
        //        }else if(WorkflowConstants.WF_TYPE_AM_API_STATE.equals(wfType)){
        //            workflowDTO = new APIStateWorkflowDTO();
        //            workflowDTO.setWorkflowType(wfType);
        //        }

        return workflow;
    }

    public WorkflowConfigHolder getWorkflowConfigurations()
            throws WorkflowException, FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        WorkflowConfigHolder workflowConfig = new WorkflowConfigHolder();

        try {
            workflowConfig.load();
            return workflowConfig;
        } catch (WorkflowException e) {
            handleException("Error occurred while creating workflow configurations", e);
        }
        return null;
    }

    private static void handleException(String msg, Exception e) throws WorkflowException {
        log.error(msg, e);
        throw new WorkflowException(msg, e);
    }
    
    public static void executeWorkflow(){
        //call the executors
        //call the notify 
    }
    
    public static void completeWorkflow(){
        //call the executors
        //call the notify 
    }

}
