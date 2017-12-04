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
package org.wso2.carbon.apimgt.core.models;

import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

/**
 * WorkflowConfig is used to map the workflow extension related configurations.
 *
 */
@Configuration (namespace = "wso2.workflowextensions", description = "workflow executor configurations")
public class WorkflowConfig {
    @Element (description = "executor for application creation workflow")
    private WorkflowExecutorInfo applicationCreation;
    @Element (description = "executor for subscription creation workflow")
    private WorkflowExecutorInfo subscriptionCreation;
    @Element (description = "executor for subscription deletion workflow")
    private WorkflowExecutorInfo subscriptionDeletion;
    @Element (description = "executor for application deletion workflow")
    private WorkflowExecutorInfo applicationDeletion;
    @Element (description = "executor for state change workflow")
    private WorkflowExecutorInfo apiStateChange;
    @Element (description = "executor for application update workflow")
    private WorkflowExecutorInfo applicationUpdate;
    
    public WorkflowConfig() {
        
        applicationCreation = new WorkflowExecutorInfo();
        applicationCreation.setExecutor(APIMgtConstants.WF_DEFAULT_WF_EXEC);

        apiStateChange = new WorkflowExecutorInfo();
        apiStateChange.setExecutor(APIMgtConstants.WF_DEFAULT_WF_EXEC);

        applicationDeletion = new WorkflowExecutorInfo();
        applicationDeletion.setExecutor(APIMgtConstants.WF_DEFAULT_WF_EXEC);

        subscriptionCreation = new WorkflowExecutorInfo();
        subscriptionCreation.setExecutor(APIMgtConstants.WF_DEFAULT_WF_EXEC);

        subscriptionDeletion = new WorkflowExecutorInfo();
        subscriptionDeletion.setExecutor(APIMgtConstants.WF_DEFAULT_WF_EXEC);
        
        applicationUpdate = new WorkflowExecutorInfo();
        applicationUpdate.setExecutor(APIMgtConstants.WF_DEFAULT_WF_EXEC);

    }

    public WorkflowExecutorInfo getApplicationCreation() {
        return applicationCreation;
    }

    public void setApplicationCreation(WorkflowExecutorInfo applicationCreation) {
        this.applicationCreation = applicationCreation;
    }

    public WorkflowExecutorInfo getSubscriptionCreation() {
        return subscriptionCreation;
    }

    public void setSubscriptionCreation(WorkflowExecutorInfo subscriptionCreation) {
        this.subscriptionCreation = subscriptionCreation;
    }

    public WorkflowExecutorInfo getSubscriptionDeletion() {
        return subscriptionDeletion;
    }

    public void setSubscriptionDeletion(WorkflowExecutorInfo subscriptionDeletion) {
        this.subscriptionDeletion = subscriptionDeletion;
    }

    public WorkflowExecutorInfo getApplicationDeletion() {
        return applicationDeletion;
    }

    public void setApplicationDeletion(WorkflowExecutorInfo applicationDeletion) {
        this.applicationDeletion = applicationDeletion;
    }

    public WorkflowExecutorInfo getApiStateChange() {
        return apiStateChange;
    }

    public void setApiStateChange(WorkflowExecutorInfo apiStateChange) {
        this.apiStateChange = apiStateChange;
    }

    public WorkflowExecutorInfo getApplicationUpdate() {
        return applicationUpdate;
    }

    public void setApplicationUpdate(WorkflowExecutorInfo applicationUpdate) {
        this.applicationUpdate = applicationUpdate;
    }

    @Override
    public String toString() {
        return "WorkflowConfig [applicationCreation=" + applicationCreation + ", subscriptionCreation="
                + subscriptionCreation + ", subscriptionDeletion=" + subscriptionDeletion + ", applicationDeletion="
                + applicationDeletion + ", apiStateChange=" + apiStateChange + ", applicationUpdate="
                + applicationUpdate + "]";
    }    
}
