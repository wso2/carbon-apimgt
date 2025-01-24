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

package org.wso2.carbon.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SubscriptionWorkflowDTO;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.monetization.AbstractMonetization;
import org.wso2.carbon.apimgt.impl.monetization.MonetizationSubscription;

import java.util.List;

public class SubscriptionCreationSimpleWorkflowExecutor extends SubscriptionWorkflowExecutor {

    private static final Log log = LogFactory.getLog(SubscriptionCreationSimpleWorkflowExecutor.class);

    @Override
    public String getWorkflowType() {
        return WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION;
    }

    @Override
    public List<WorkflowDTO> getWorkflowDetails(String workflowStatus) throws WorkflowException {
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

    /**
     * This method executes subscription creation simple workflow and return workflow response back to the caller
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response back to the caller
     * @throws WorkflowException Thrown when the workflow execution was not fully performed
     */
    @Override
    public WorkflowResponse execute(WorkflowDTO workflowDTO) throws WorkflowException {

        SubscriptionWorkflowDTO subsWorkflowDTO = (SubscriptionWorkflowDTO) workflowDTO;
        workflowDTO.setProperties("apiName", subsWorkflowDTO.getApiName());
        workflowDTO.setProperties("apiVersion", subsWorkflowDTO.getApiVersion());
        workflowDTO.setProperties("subscriber", subsWorkflowDTO.getSubscriber());
        workflowDTO.setProperties("applicationName", subsWorkflowDTO.getApplicationName());
        super.execute(workflowDTO);

        workflowDTO.setStatus(WorkflowStatus.APPROVED);
        WorkflowResponse workflowResponse = complete(workflowDTO);

        return workflowResponse;
    }

    /**
     * This method completes subscription creation simple workflow and return workflow response back to the caller
     *
     * @param workflowDTO The WorkflowDTO which contains workflow contextual information related to the workflow
     * @return workflow response back to the caller
     * @throws WorkflowException
     */
    @Override
    public WorkflowResponse complete(WorkflowDTO workflowDTO) throws WorkflowException {

        workflowDTO.setUpdatedTime(System.currentTimeMillis());
        super.complete(workflowDTO);
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

        API api = null;
        APIProduct product = null;

        try {
            apiMgtDAO.updateSubscriptionStatus(Integer.parseInt(workflowDTO.getWorkflowReference()),
                    APIConstants.SubscriptionStatus.UNBLOCKED);

            ApiTypeWrapper apiTypeWrapper = getAPIorAPIProductwithWorkflowDTO(workflowDTO);
            Tier tier = getAPIorAPIProductTier(apiTypeWrapper, workflowDTO);
            boolean isApiProduct = apiTypeWrapper.isAPIProduct();
            boolean isMonetizationEnabled = false;
            MonetizationSubscription subscriptionImpl = getMonetizationSubscriptionClass();
            if (isApiProduct) {
                product = apiTypeWrapper.getApiProduct();
                isMonetizationEnabled = product.getMonetizationStatus();
                if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    subscriptionImpl.monetizeSubscription(workflowDTO, product);
                }
            } else {
                api = apiTypeWrapper.getApi();
                isMonetizationEnabled = api.getMonetizationStatus();
                if (isMonetizationEnabled && APIConstants.COMMERCIAL_TIER_PLAN.equals(tier.getTierPlan())) {
                    subscriptionImpl.monetizeSubscription(workflowDTO, api);
                }
            }

        } catch (APIManagementException e) {
            log.error("Could not complete subscription creation workflow", e);
            throw new WorkflowException("Could not complete subscription creation workflow", e);
        }
        return new GeneralWorkflowResponse();
    }
}
