package org.wso2.carbon.apimgt.impl.monetization;

import org.wso2.carbon.apimgt.api.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

public interface MonetizationSubscription {

    /**
     * Implements monetization logic in workflow.
     *
     * @param workflowDTO - The WorkflowDTO which contains workflow contextual information related to the workflow.
     * @param api - The API that is being subscribed to
     * @throws WorkflowException - Thrown when the workflow execution was not fully performed.
     */
    public void monetizeSubscription(WorkflowDTO workflowDTO, API api) throws WorkflowException;

    /**
     * This method executes monetization related functions in the subscription creation workflow
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @param apiProduct - API product to which the subscription relates to
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    public void monetizeSubscription(org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, APIProduct apiProduct)
            throws WorkflowException;

    /**
     * This method executes monetization related functions in the subscription deletion workflow
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @param api - The API that is being subscribed to
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    public void deleteMonetizedSubscription(org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, API api) throws WorkflowException;

    /**
     * This method executes monetization related functions in the subscription deletion workflow
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @param apiProduct - API product to which the subscription relates to
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    public void deleteMonetizedSubscription(org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, APIProduct apiProduct)
            throws WorkflowException;

}
