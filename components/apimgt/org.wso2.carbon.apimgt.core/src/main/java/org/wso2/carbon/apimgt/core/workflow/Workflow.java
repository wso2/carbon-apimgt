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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the model that will be used for storing workflow related contextual information.
 */
public abstract class Workflow {

    private static final Logger log = LoggerFactory.getLogger(Workflow.class);

    private String workflowReference;
    private String workflowType;
    private final Category category;

    //Used to hold the status of the workflow. When a workflow is initially executed, it will be in the CREATED state.
    //It will then move to the APPROVED or REJECTED states depending on the output of the workflow execution.
    private WorkflowStatus status;
    private Instant createdTime;
    private Instant updatedTime;

    //Holds the workflow description. This is used for having a human readable description from the output of the
    //workflow execution. Ex: If an approval was rejected, why was it so.
    private String workflowDescription;
    private String externalWorkflowReference;
    private String callbackURL;
    private String createdBy;
    private WorkflowDAO workflowDAO;
    private APIGateway apiGateway;

    public Workflow(WorkflowDAO workflowDAO, Category category, APIGateway apiGateway) {
        this.workflowDAO = workflowDAO;
        this.category = category;
        this.apiGateway = apiGateway;
    }

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


    public Instant getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Instant createdTime) {
        this.createdTime = createdTime;
    }

    public Instant getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Instant updatedTime) {
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

    public Category getCategory() {
        return category;
    }

    /**
     * Persist workflow status after the workflow is completed
     *
     * @param workflow Workflow  information
     * @throws APIManagementException if error occurred while updating workflow status
     */
    protected void updateWorkflowEntries(Workflow workflow) throws APIManagementException {
        this.updatedTime = Instant.now();
        try {
            workflowDAO.updateWorkflowStatus(workflow);
            // TODO stats stuff
        } catch (APIMgtDAOException e) {
            String message = "Error while updating workflow entry";
            log.error(message, e);
            throw new APIManagementException(message, e, ExceptionCodes.APIMGT_DAO_EXCEPTION);
        }
    }

    /**
     * Complete workflow after the approval
     *
     * @param workflowExecutor Workflow executor
     * @return Workflow Response object
     * @throws APIManagementException if error occurred while completing the workflow
     */
    public abstract WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor) throws APIManagementException;

    @Override
    public String toString() {
        return "Workflow [workflowReference=" + workflowReference + ", workflowType=" + workflowType + ", status="
                + status + ", createdTime=" + createdTime + ", updatedTime=" + updatedTime + ", workflowDescription="
                + workflowDescription + ", externalWorkflowReference=" + externalWorkflowReference + ", callbackURL="
                + callbackURL + ", createdBy=" + createdBy + ", attributes=" + attributes + ']';
    }

    /**
     * Workflow category which depends on where the workflow is applicable to.
     */
    public enum Category {
        STORE, PUBLISHER
    }

    public APIGateway getApiGateway() {
        return apiGateway;
    }
}
