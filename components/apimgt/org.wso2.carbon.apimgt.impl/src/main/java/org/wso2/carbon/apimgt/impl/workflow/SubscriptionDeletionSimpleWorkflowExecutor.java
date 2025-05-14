/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.monetization.AbstractMonetization;
import org.wso2.carbon.apimgt.impl.monetization.MonetizationSubscription;

import java.util.List;

/**
 * Simple workflow executor for subscription delete action
 */
public class SubscriptionDeletionSimpleWorkflowExecutor extends SubscriptionWorkflowExecutor {
    private static final Log log = LogFactory.getLog(SubscriptionDeletionSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_DELETION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {

        // implemetation is not provided in this version
        return null;
    }

    /**
     * Returns an instance of MonetizationSubscription to be called within complete for adding or removing monetized subscriptions
     *
     * @return an instance of MonetizationSubscription
     * @throws APIManagementException due to it calling the getMonetizationImplClass method
     */
    @Override
    public MonetizationSubscription getMonetizationSubscriptionClass() throws APIManagementException {
        AbstractMonetization monetizationImpl = (AbstractMonetization) super.getMonetizationImplClass();
        MonetizationSubscription subscriptionImpl = monetizationImpl.getMonetizationSubscriptionClass();
        return subscriptionImpl;
    }

    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {
        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        complete(workflowDTO);
        return new GeneralWorkflowResponse();
    }

    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        SubscriptionWorkflowDTO subWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        String errorMsg = null;
        API api = null;
        APIProduct product = null;

        try {
            APIIdentifier identifier = new APIIdentifier(subWorkflowDTO.getApiProvider(),
                    subWorkflowDTO.getApiName(), subWorkflowDTO.getApiVersion());
            identifier.setId(Integer.parseInt(subWorkflowDTO.getMetadata(WorkflowConstants.PayloadConstants.API_ID)));
            apiMgtDAO.removeSubscription(identifier, ((SubscriptionWorkflowDTO) workflowDTO).getApplicationId());

            ApiTypeWrapper apiTypeWrapper = getAPIorAPIProductwithWorkflowDTO(workflowDTO);
            Tier tier = getAPIorAPIProductTier(apiTypeWrapper, workflowDTO);
            boolean isApiProduct = apiTypeWrapper.isAPIProduct();
            boolean isMonetizationEnabled = false;
            MonetizationSubscription subscriptionImpl = getMonetizationSubscriptionClass();
            if (isApiProduct) {
                product = apiTypeWrapper.getApiProduct();
                isMonetizationEnabled = product.getMonetizationStatus();
                if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    subscriptionImpl.deleteMonetizedSubscription(workflowDTO, product);
                }
            } else {
                api = apiTypeWrapper.getApi();
                isMonetizationEnabled = api.getMonetizationStatus();
                if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    subscriptionImpl.deleteMonetizedSubscription(workflowDTO, api);
                }
            }

        } catch (APIManagementException e) {
            errorMsg = "Could not complete subscription deletion workflow for api: " + subWorkflowDTO.getApiName();
            throw new WorkflowException(errorMsg, e);
        }
        return new GeneralWorkflowResponse();
    }
}
