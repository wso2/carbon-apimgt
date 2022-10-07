package org.wso2.carbon.apimgt.impl.dao;

import org.wso2.carbon.apimgt.api.APIManagementException;
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
}
