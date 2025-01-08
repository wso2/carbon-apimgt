package org.wso2.carbon.apimgt.impl.monetization;

import org.wso2.carbon.apimgt.api.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;

public class DefaultMonetizationSubscriptionImpl implements MonetizationSubscription{

    @Override
    public void monetizeSubscription(WorkflowDTO workflowDTO, API api) throws WorkflowException {

    }

    @Override
    public void monetizeSubscription(org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, APIProduct apiProduct) throws WorkflowException {

    }

    @Override
    public void deleteMonetizedSubscription(org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, API api) throws WorkflowException {

    }

    @Override
    public void deleteMonetizedSubscription(org.wso2.carbon.apimgt.impl.dto.WorkflowDTO workflowDTO, APIProduct apiProduct) throws WorkflowException {

    }
}
