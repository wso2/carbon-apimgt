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

package org.wso2.carbon.apimgt.core.models;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the model that will be used for storing workflow related contextual information.
 */
public class Workflow {

    private String workflowReference;

    private String workflowType;

    //Used to hold the status of the workflow. When a workflow is initially executed, it will be in the CREATED state.
    //It will then move to the APPROVED or REJECTED states depending on the output of the workflow execution.
    private WorkflowStatus status;

    private LocalDateTime createdTime;

    private LocalDateTime updatedTime;

    //Holds the workflow description. This is used for having a human readable description from the output of the
    //workflow execution. Ex: If an approval was rejected, why was it so.
    private String workflowDescription;

    private String externalWorkflowReference;

    private String callbackURL;
    
    private String createdBy;
    
    //to pass any additional parameters. This can be used to pass parameters to the executor's complete() method
    private Map<String, String> attributes = new HashMap<>();

    public String getCallBack() {
        return callbackURL;
    }

    public void setCallBack(String callbackUrl) {
        this.callbackURL = callbackUrl;
    }

    /**
     * Returns the status of the Workflow.
     * @return - Enum, the workflow status (Ex: CREATED, APPROVED, REJECTED)
     */
    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    /**
     * Returns the workflow description.
     * @return - The workflow description.
     */
    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
    }

    public String getWorkflowReference() {
        return workflowReference;
    }

    public void setWorkflowReference(String workflowReference) {
        this.workflowReference = workflowReference;
    }

    public String getWorkflowType() {
        return workflowType;
    }

    public void setWorkflowType(String workflowType) {
        this.workflowType = workflowType;
    }

    /**
     * Returns the reference id of the workflow.
     * @return - The workflow reference id.
     */
    public String getExternalWorkflowReference() {
        return externalWorkflowReference;
    }

    public void setExternalWorkflowReference(String externalWorkflowReference) {
        this.externalWorkflowReference = externalWorkflowReference;
    }


    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(LocalDateTime updatedTime) {
        this.updatedTime = updatedTime;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
    
    public String getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, String value) {
        this.attributes.put(key, value);
    }


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @Override
    public String toString() {
        return "Workflow [workflowReference=" + workflowReference + ", workflowType=" + workflowType + ", status="
                + status + ", createdTime=" + createdTime + ", updatedTime=" + updatedTime + ", workflowDescription="
                + workflowDescription + ", externalWorkflowReference=" + externalWorkflowReference + ", callbackURL="
                + callbackURL + ", createdBy=" + createdBy + ", attributes=" + attributes + "]";
    }   
    
}
