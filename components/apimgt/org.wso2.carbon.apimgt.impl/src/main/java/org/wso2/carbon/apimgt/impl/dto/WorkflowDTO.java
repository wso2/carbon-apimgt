/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.dto;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowStatus;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the DTO that will be used for storing workflow related contextual information.
 */
public class WorkflowDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String workflowReference;

    private String workflowType;

    //Used to hold the status of the workflow. When a workflow is initially executed, it will be in the CREATED state.
    //It will then move to the APPROVED or REJECTED states depending on the output of the workflow execution.
    private WorkflowStatus status;

    private long createdTime;

    private long updatedTime;

    //Holds the workflow description. This is used for having a human readable description from the output of the
    //workflow execution. Ex: If an approval was rejected, why was it so.
    private String workflowDescription;

    private int tenantId;

    private String tenantDomain;

    //Holds the workflow reference id. This reference is used to have a reference from the entity that initiated the
    // workflow to the actual workflow itself.
    private String externalWorkflowReference;

    private String callbackUrl;

    private JSONObject metadata;

    private JSONObject properties;

    public WorkflowDTO(){
        metadata = new JSONObject();
        properties = new JSONObject();
    }

    public String getProperties(String key) {
        return properties.get(key).toString();
    }

    public void setProperties(String key, String value) {
        properties.put(key, value);
    }

    //to pass any additional parameters. This can be used to pass parameters to the executor's complete() method
    private Map<String, String> attributes = new HashMap<String, String>();

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
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
     * Returns the reference id of the workflow.
     * @return - The workflow reference id.
     */
    public String getExternalWorkflowReference() {
        return externalWorkflowReference;
    }

    public void setExternalWorkflowReference(String externalWorkflowReference) {
        this.externalWorkflowReference = externalWorkflowReference;
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

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public String getMetadata(String key) {
        return metadata.get(key).toString();
    }

    public void setMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public JSONObject getMetadata() {
        return metadata;
    }

    public void setMetadata(JSONObject metadata) {
        this.metadata = metadata;
    }

    public JSONObject getProperties() {
        return properties;
    }

    public void setProperties(JSONObject properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "WorkflowDTO{" +
                "workflowReference='" + workflowReference + '\'' +
                ", workflowType='" + workflowType + '\'' +
                ", status=" + status +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                ", workflowDescription='" + workflowDescription + '\'' +
                ", tenantId=" + tenantId +
                ", tenantDomain='" + tenantDomain + '\'' +
                ", externalWorkflowReference='" + externalWorkflowReference + '\'' +
                ", callbackUrl='" + callbackUrl + '\'' +
                ", metadata=" + metadata +
                ", properties=" + properties +
                ", attributes=" + attributes +
                '}';
    }
}
