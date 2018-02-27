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
*
*/
package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;

import java.util.List;

/**
 * The interface used to manage APIs in gateway
 */
public interface APIGateway {

    /**
     * Add API in gateway
     *
     * @param api API artifact
     * @throws GatewayException If there is a failure while adding API to gateway
     */
    void addAPI(API api) throws GatewayException;

    /**
     * Add CompositeAPI in gateway
     *
     * @param api CompositeAPI artifact
     * @throws GatewayException If there is a failure while adding CompositeAPI to gateway
     */
    void addCompositeAPI(CompositeAPI api) throws GatewayException;

    /**
     * Update API in gateway
     *
     * @param api API artifact
     * @throws GatewayException If there is a failure to update API in gateway
     */
    void updateAPI(API api) throws GatewayException;

    /**
     * Delete API in gateway
     *
     * @param api API artifact
     * @throws GatewayException If there is a failure to delete API in gateway
     */
    void deleteAPI(API api) throws GatewayException;

    /**
     * Delete CompositeAPI in gateway
     *
     * @param api API artifact
     * @throws GatewayException If there is a failure to delete API in gateway
     */
    void deleteCompositeAPI(CompositeAPI api) throws GatewayException;

    /**
     * Add API subscription to gateway
     *
     * @param subscriptionValidationDataList Subscription validation details
     * @throws GatewayException If there is a failure to update subscription
     */
    void addAPISubscription(List<SubscriptionValidationData> subscriptionValidationDataList) throws GatewayException;

    /**
     * Update API subscription status in gateway
     *
     * @param subscriptionValidationDataList Subscription validation details
     * @throws GatewayException If there is a failure to update subscription status
     */
    void updateAPISubscriptionStatus(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException;

    /**
     * Delete API subscription from gateway
     *
     * @param subscriptionValidationDataList Subscription validation details
     * @throws GatewayException If there is a failure to update subscription
     */
    void deleteAPISubscription(List<SubscriptionValidationData> subscriptionValidationDataList) throws GatewayException;

    /**
     * Add endpoint to gateway
     *
     * @param endpoint Endpoint artifact
     * @throws GatewayException If there is a failure to add endpoint to gateways
     */
    void addEndpoint(Endpoint endpoint) throws GatewayException;

    /**
     * Update endpoint in gateways
     *
     * @param endpoint Endpoint artifact
     * @throws GatewayException If there is a failure to update endpoint in gateway
     */
    void updateEndpoint(Endpoint endpoint) throws GatewayException;

    /**
     * Delete endpoint in gateway
     *
     * @param endpoint Endpoint artifact
     * @throws GatewayException If there is a failure to delete endpoint in gateway
     */
    void deleteEndpoint(Endpoint endpoint) throws GatewayException;

    /**
     * Publish the lifecycle state change of an API to gateway
     *
     * @param api    API artifact
     * @param status Target lifecycle status to be changed
     * @throws GatewayException If there is a failure to notify state change to gateway
     */
    void changeAPIState(API api, String status) throws GatewayException;

    /**
     * Publish Application Creation into Gateway
     *
     * @param application {@link Application}
     * @throws GatewayException If there is a failure in notifying creation to gateway
     */
    void addApplication(Application application) throws GatewayException;

    /**
     * Publish the update application change to Gateway
     *
     * @param application {@link Application}
     * @throws GatewayException If there is a failure in notifying update to gateway
     */
    void updateApplication(Application application) throws GatewayException;


    /**
     * Publish the delete application change to Gateway
     *
     * @param applicationId applicationId of application
     * @throws GatewayException If there is a failure in notifying deletion to gateway
     */
    void deleteApplication(String applicationId) throws GatewayException;

    /**
     * Publish policy add event to Gateway
     *
     * @param policyValidationData policy Data
     * @throws GatewayException If there is a failure in notifying add policy to gateway
     */
    void addPolicy(PolicyValidationData policyValidationData) throws GatewayException;

    /**
     * Publish policy update event to gateway
     *
     * @param policyValidationData policy Data
     * @throws GatewayException If there is a failure in notifying update policy to gateway
     */
    void updatePolicy(PolicyValidationData policyValidationData) throws GatewayException;

    /**
     * Publish policy delete event to gateway
     *
     * @param policyValidationData policy Data
     * @throws GatewayException If there is a failure in notifying delete policy to gateway
     */
    void deletePolicy(PolicyValidationData policyValidationData) throws GatewayException;

    /**
     * Publish Block condition Add event to Gateway
     *
     * @param blockConditions block condition data
     * @throws GatewayException If there is a failure in notifying add block condition to gateway
     */
    void addBlockCondition(BlockConditions blockConditions) throws GatewayException;

    /**
     * Publish Block condition Update event to Gateway
     *
     * @param blockConditions block condition data
     * @throws GatewayException If there is a failure in notifying update block condition to gateway
     */
    void updateBlockCondition(BlockConditions blockConditions) throws GatewayException;

    /**
     * Publish Block condition Delete event to Gateway
     *
     * @param blockConditions block condition data
     * @throws GatewayException If there is a failure in notifying update block condition to gateway
     */
    void deleteBlockCondition(BlockConditions blockConditions) throws GatewayException;

    /**
     * Publish Threat Protection Policy add event to gateway
     *
     * @param policy ThreatProtectionPolicy, see {@link ThreatProtectionPolicy}
     * @throws GatewayException if there is a failure in notifying event to gateway
     */
    void addThreatProtectionPolicy(ThreatProtectionPolicy policy) throws GatewayException;

    /**
     * Publish Threat Protection Policy delete event to gateway
     *
     * @param policy ThreatProtectionPolicy, see {@link ThreatProtectionPolicy}
     * @throws GatewayException if there is a failure in notifying event to gateway
     */
    void deleteThreatProtectionPolicy(ThreatProtectionPolicy policy) throws GatewayException;

    /**
     * Publish Threat Protection Policy update event to gateway
     *
     * @param policy ThreatProtectionPolicy, see {@link ThreatProtectionPolicy}
     * @throws GatewayException if there is a failure in notifying event to gateway
     */
    void updateThreatProtectionPolicy(ThreatProtectionPolicy policy) throws GatewayException;

    /**
     * Startup a new Gateway in Container Management System
     *
     * @param label Auto-generated label of the API
     * @param api   API
     * @throws ContainerBasedGatewayException If there is a failure in creating the container based gateway
     */
    void createContainerBasedGateway(String label, API api) throws ContainerBasedGatewayException;

    /**
     * Remove existing Gateway from the container Management System
     *
     * @param label auto-generated label of the original API
     * @param api   API
     * @throws ContainerBasedGatewayException If there is a failure in removing the container based gateway
     */
    void removeContainerBasedGateway(String label, API api) throws ContainerBasedGatewayException;

}
