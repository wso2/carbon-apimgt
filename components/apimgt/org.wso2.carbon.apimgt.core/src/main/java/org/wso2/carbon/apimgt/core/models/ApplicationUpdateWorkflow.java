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

package org.wso2.carbon.apimgt.core.models;

import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;

/**
 * Application update model class for workflow
 */

public class ApplicationUpdateWorkflow extends Workflow {
    
    private Application existingApplication;
    private Application updatedApplication;
    
    public ApplicationUpdateWorkflow() {
       setWorkflowType(WorkflowConstants.WF_TYPE_AM_APPLICATION_UPDATE);
    }

    public Application getUpdatedApplication() {
        return updatedApplication;
    }

    public void setUpdatedApplication(Application updatedApplication) {
        this.updatedApplication = updatedApplication;
    }

    public Application getExistingApplication() {
        return existingApplication;
    }

    public void setExistingApplication(Application existingApplication) {
        this.existingApplication = existingApplication;
    }

    @Override
    public String toString() {
        return "ApplicationUpdateWorkflow [existingApplication=" + existingApplication + ", updatedApplication="
                + updatedApplication + ", toString()=" + super.toString() + "]";
    }

}
