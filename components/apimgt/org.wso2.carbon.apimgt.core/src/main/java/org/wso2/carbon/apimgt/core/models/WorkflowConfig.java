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

/**
 * WorkflowConfig is used to map the workflow extension related configurations.
 *
 */
public class WorkflowConfig {
    private WorkflowExecutorInfo applicationCreation;
    private WorkflowExecutorInfo productionApplicationRegistration;
    private WorkflowExecutorInfo sandboxApplicationRegistration;
    private WorkflowExecutorInfo subscriptionCreation;
    private WorkflowExecutorInfo userSignUp;
    private WorkflowExecutorInfo subscriptionDeletion;
    private WorkflowExecutorInfo applicationDeletion;
    private WorkflowExecutorInfo apiStateChange;

    public WorkflowExecutorInfo getApplicationCreation() {
        return applicationCreation;
    }

    public void setApplicationCreation(WorkflowExecutorInfo applicationCreation) {
        this.applicationCreation = applicationCreation;
    }

    public WorkflowExecutorInfo getProductionApplicationRegistration() {
        return productionApplicationRegistration;
    }

    public void setProductionApplicationRegistration(WorkflowExecutorInfo productionApplicationRegistration) {
        this.productionApplicationRegistration = productionApplicationRegistration;
    }

    public WorkflowExecutorInfo getSandboxApplicationRegistration() {
        return sandboxApplicationRegistration;
    }

    public void setSandboxApplicationRegistration(WorkflowExecutorInfo sandboxApplicationRegistration) {
        this.sandboxApplicationRegistration = sandboxApplicationRegistration;
    }

    public WorkflowExecutorInfo getSubscriptionCreation() {
        return subscriptionCreation;
    }

    public void setSubscriptionCreation(WorkflowExecutorInfo subscriptionCreation) {
        this.subscriptionCreation = subscriptionCreation;
    }

    public WorkflowExecutorInfo getUserSignUp() {
        return userSignUp;
    }

    public void setUserSignUp(WorkflowExecutorInfo userSignUp) {
        this.userSignUp = userSignUp;
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

    @Override
    public String toString() {
        return "WorkflowConfig [applicationCreation=" + applicationCreation + ", productionApplicationRegistration="
                + productionApplicationRegistration + ", sandboxApplicationRegistration="
                + sandboxApplicationRegistration + ", subscriptionCreation=" + subscriptionCreation + ", userSignUp="
                + userSignUp + ", subscriptionDeletion=" + subscriptionDeletion + ", applicationDeletion="
                + applicationDeletion + ", apiStateChange=" + apiStateChange + "]";
    }

}
