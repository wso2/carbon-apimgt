/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.WorkflowStatus;

public class Workflow {

    private String workflowReference;

    private String workflowType;

    private WorkflowStatus status;

    private String createdTime;

    private String updatedTime;

    private String workflowDescription;

    private int tenantId;

    private String tenantDomain;

    private String externalWorkflowReference;

    private String callbackUrl;

    private int workflowId;

    private String workflowStatusDesc;

    private JSONObject metadata;

    private JSONObject properties;

    public Workflow() {
        metadata = new JSONObject();
        properties = new JSONObject();
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

    public WorkflowStatus getStatus() {
        return status;
    }

    public void setStatus(WorkflowStatus status) {
        this.status = status;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getWorkflowDescription() {
        return workflowDescription;
    }

    public void setWorkflowDescription(String workflowDescription) {
        this.workflowDescription = workflowDescription;
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

    public String getExternalWorkflowReference() {
        return externalWorkflowReference;
    }

    public void setExternalWorkflowReference(String externalWorkflowReference) {
        this.externalWorkflowReference = externalWorkflowReference;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public String getWorkflowStatusDesc() {
        return workflowStatusDesc;
    }

    public void setWorkflowStatusDesc(String workflowStatusDesc) {
        this.workflowStatusDesc = workflowStatusDesc;
    }

    public String getMetadata(String key) {
        return (String)metadata.get(key);
    }

    public void setMetadata(String key, String value) {
        metadata.put(key, value);
    }

    public String getProperties(String key) {
        return (String)properties.get(key);
    }

    public void setProperties(String key, String value) {
        properties.put(key, value);
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
        return "Workflow{" +
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
                ", workflowId=" + workflowId +
                ", workflowStatusDesc='" + workflowStatusDesc + '\'' +
                ", metadata=" + metadata +
                ", properties=" + properties +
                '}';
    }
}
