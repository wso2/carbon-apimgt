/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.workflow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;

import java.util.List;

/**
 * This model is used to gather subscription deletion related workflow data
 */
public class SubscriptionDeletionWorkflow extends Workflow {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionDeletionWorkflow.class);

    private Subscription subscription;
    private String subscriber;
    private APISubscriptionDAO apiSubscriptionDAO;
    private APIGateway apiGateway;

    public SubscriptionDeletionWorkflow(APISubscriptionDAO apiSubscriptionDAO, WorkflowDAO workflowDAO, APIGateway
            apiGateway) {
        super(workflowDAO, Category.STORE, apiGateway);
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.apiGateway = apiGateway;
    }

    public String getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(String subscriber) {
        this.subscriber = subscriber;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public WorkflowResponse completeWorkflow(WorkflowExecutor workflowExecutor) throws APIManagementException {
        if (subscription == null) {
            // this is when complete method is executed through workflow rest api
            this.subscription = apiSubscriptionDAO.getAPISubscription(getWorkflowReference());
        }
        WorkflowResponse response = workflowExecutor.complete(this);
        setStatus(response.getWorkflowStatus());
        List<SubscriptionValidationData> subscriptionValidationDataList = null;
        if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("Subscription deletion workflow complete: Approved");
            }
            if (subscription.getApi() != null && subscription.getApplication() != null) {
                subscriptionValidationDataList = apiSubscriptionDAO
                        .getAPISubscriptionsOfAPIForValidation(subscription.getApi().getContext(), subscription.getApi()
                                .getVersion(), subscription.getApplication().getId());
            }
            apiSubscriptionDAO.deleteAPISubscription(getWorkflowReference());

        } else if (WorkflowStatus.REJECTED == response.getWorkflowStatus()) {
            if (log.isDebugEnabled()) {
                log.debug("Subscription deletion workflow complete: Rejected");
            }
        }
        updateWorkflowEntries(this);
        if (WorkflowStatus.APPROVED == response.getWorkflowStatus()) {

            if (subscriptionValidationDataList != null && !subscriptionValidationDataList.isEmpty()) {
                apiGateway.deleteAPISubscription(subscriptionValidationDataList);
                if (log.isDebugEnabled()) {
                    log.debug("Subscription for API : " + subscription.getApi().getName() + " with " +
                            "application : " + subscription.getApplication().getName() + " has been successfully " +
                            "removed from gateway");
                }
            }
        }
        return response;
    }

    @Override
    public String toString() {
        return "SubscriptionDeletionWorkflow [subscription=" + subscription + ", subscriber=" + subscriber + ']';
    }

}
