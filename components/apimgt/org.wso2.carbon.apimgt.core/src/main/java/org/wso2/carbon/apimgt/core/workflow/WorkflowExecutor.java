/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.core.workflow;

import org.wso2.carbon.apimgt.core.exception.WorkflowException;
import org.wso2.carbon.apimgt.core.models.Workflow;

import java.util.Observable;
import java.util.UUID;

/**
 * This is the workflow interface.
 */
public abstract class WorkflowExecutor extends Observable {

    protected String callbackEndPoint;

    /**
     * Implements the workflow execution logic.
     *
     * @param workflow - The Workflow model which contains workflow contextual information related to the workflow.
     * @throws WorkflowException - Thrown when the workflow execution was not fully performed.
     */
    public WorkflowResponse execute(Workflow workflow) throws WorkflowException {
        return new GeneralWorkflowResponse();
    }

    /**
     * Implements the workflow completion logic.
     *
     * @param workflow - The Workflow model which contains workflow contextual information related to the workflow.
     * @throws WorkflowException - Thrown when the workflow completion was not fully performed.
     */
    public void complete(Workflow workflow) throws WorkflowException {
    }

    /**
     * Method generates and returns UUID
     *
     * @return UUID
     */
    public String generateUUID() {
        return UUID.randomUUID().toString();
    }

    public String getCallbackEndPoint() {
        return callbackEndPoint;
    }

    public void setCallbackEndPoint(String callbackEndPoint) {
        this.callbackEndPoint = callbackEndPoint;
    }
}
