package org.wso2.apk.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Workflow;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;

public interface WorkflowDAO {

    /**
     * Returns a workflow object for a given internal workflow reference and the workflow type.
     *
     * @param workflowReference
     * @param workflowType
     * @return
     * @throws APIManagementException
     */
    WorkflowDTO retrieveWorkflowFromInternalReference(String workflowReference, String workflowType)
            throws APIManagementException;

    /**
     * Retries the WorkflowExternalReference for a subscription.
     *
     * @param subscriptionId ID of the subscription
     * @return External workflow reference for the subscription <code>subscriptionId</code>
     * @throws APIManagementException
     */
    String getExternalWorkflowReferenceForSubscription(int subscriptionId) throws APIManagementException;

    /**
     * Get the Pending workflow Requests using WorkflowType for a particular tenant
     *
     * @param workflowType Type of the workflow pending request
     * @param status       workflow status of workflow pending request
     * @param tenantDomain tenantDomain of the user
     * @return List of workflow pending request
     * @throws APIManagementException
     */
    Workflow[] getWorkflows(String workflowType, String status, String tenantDomain) throws APIManagementException;

    /**
     * Get the Pending workflow Request using ExternalWorkflowReference for a particular tenant
     *
     * @param externalWorkflowRef of pending workflow request
     * @param status              workflow status of workflow pending process
     * @param tenantDomain        tenant domain of user
     * @return workflow pending request
     */
    Workflow getWorkflowReferenceByExternalWorkflowReferenceID(String externalWorkflowRef, String status,
                                                               String tenantDomain) throws APIManagementException;
}
