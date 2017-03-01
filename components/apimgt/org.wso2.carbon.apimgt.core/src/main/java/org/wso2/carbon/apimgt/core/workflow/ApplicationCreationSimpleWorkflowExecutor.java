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
import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.Workflow;
//import java.util.Observable;

/**
 * This is a simple work flow extension to have Application creation process
 */
public class ApplicationCreationSimpleWorkflowExecutor { //extends Observable {
    private static final Log log = LogFactory.getLog(ApplicationCreationSimpleWorkflowExecutor.class);

    /**
     * Execute the workflow executor
     *
     * @param workFlow
     * @throws WorkflowException
     */

    public WorkflowResponse execute(Workflow workFlow) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Executing Application creation Workflow..");
        }
        workFlow.setStatus(WorkflowStatus.APPROVED);
        complete(workFlow);
        return new GeneralWorkflowResponse();
    }

    /**
     * Complete the external process status
     * Based on the workflow status we will update the status column of the
     * Application table
     *
     * @param workFlow - Workflow
     */
    public void complete(Workflow workFlow) throws WorkflowException {
        if (log.isDebugEnabled()) {
            log.info("Complete  Application creation Workflow..");
        }

        //set change
        //setChanged();
        //notify observers for change
        //notifyObservers(workFlow);
    }
}
