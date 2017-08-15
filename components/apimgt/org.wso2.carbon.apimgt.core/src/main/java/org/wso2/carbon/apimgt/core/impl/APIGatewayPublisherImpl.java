/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.BlockConditions;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.PolicyValidationData;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.events.APIEvent;
import org.wso2.carbon.apimgt.core.models.events.ApplicationEvent;
import org.wso2.carbon.apimgt.core.models.events.BlockEvent;
import org.wso2.carbon.apimgt.core.models.events.EndpointEvent;
import org.wso2.carbon.apimgt.core.models.events.GatewayEvent;
import org.wso2.carbon.apimgt.core.models.events.PolicyEvent;
import org.wso2.carbon.apimgt.core.models.events.SubscriptionEvent;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import java.util.List;

/**
 * This is responsible for handling API gateway related operations
 */
public class APIGatewayPublisherImpl implements APIGateway {
    private static final Logger log = LoggerFactory.getLogger(APIGatewayPublisherImpl.class);
    private APIMConfigurations config;
    private String publisherTopic;
    private String storeTopic;
    private String throttleTopic;

    public APIGatewayPublisherImpl() {
        config = ServiceReferenceHolder.getInstance().getAPIMConfiguration();
        publisherTopic = config.getBrokerConfigurations().getPublisherTopic();
        storeTopic = config.getBrokerConfigurations().getStoreTopic();
        throttleTopic = config.getBrokerConfigurations().getThrottleTopic();
    }

    @Override
    public void addAPI(API api) throws GatewayException {

        // build the message to send
        APIEvent apiCreateEvent = new APIEvent(APIMgtConstants.GatewayEventTypes.API_CREATE);
        apiCreateEvent.setLabels(api.getLabels());
        apiCreateEvent.setApiSummary(toAPISummary(api));
        publishToPublisherTopic(apiCreateEvent);
        if (log.isDebugEnabled()) {
            log.debug("API : " + api.getName() + " created event has been successfully published to broker");
        }

    }

    @Override
    public void addCompositeAPI(CompositeAPI api) throws GatewayException {

        // build the message to send
        APIEvent gatewayDTO = new APIEvent(APIMgtConstants.GatewayEventTypes.API_CREATE);
        gatewayDTO.setLabels(api.getLabels());
        APISummary apiSummary = new APISummary();
        apiSummary.setName(api.getName());
        apiSummary.setVersion(api.getVersion());
        apiSummary.setContext(api.getContext());
        gatewayDTO.setApiSummary(apiSummary);
        publishToPublisherTopic(gatewayDTO);

    }

    @Override
    public void updateAPI(API api) throws GatewayException {

        // build the message to send
        APIEvent apiUpdateEvent = new APIEvent(APIMgtConstants.GatewayEventTypes.API_UPDATE);
        apiUpdateEvent.setLabels(api.getLabels());
        apiUpdateEvent.setApiSummary(toAPISummary(api));
        publishToPublisherTopic(apiUpdateEvent);
        if (log.isDebugEnabled()) {
            log.debug("API : " + api.getName() + " updated event has been successfully published to broker");
        }
    }

    @Override
    public void deleteAPI(API api) throws GatewayException {
        // build the message to send
        APIEvent apiDeleteEvent = new APIEvent(APIMgtConstants.GatewayEventTypes.API_DELETE);
        apiDeleteEvent.setLabels(api.getLabels());
        apiDeleteEvent.setApiSummary(toAPISummary(api));
        publishToPublisherTopic(apiDeleteEvent);
        if (log.isDebugEnabled()) {
            log.debug("API : " + api.getName() + " deleted event has been successfully published to broker");
        }
    }

    @Override
    public void deleteCompositeAPI(CompositeAPI api) throws GatewayException {
        // build the message to send
        APIEvent gatewayDTO = new APIEvent(APIMgtConstants.GatewayEventTypes.API_DELETE);
        gatewayDTO.setLabels(api.getLabels());
        APISummary apiSummary = new APISummary();
        apiSummary.setName(api.getName());
        apiSummary.setVersion(api.getVersion());
        apiSummary.setContext(api.getContext());
        gatewayDTO.setApiSummary(apiSummary);
        publishToPublisherTopic(gatewayDTO);

    }

    @Override
    public void addAPISubscription(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException {
        SubscriptionEvent subscriptionAddEvent = new SubscriptionEvent(APIMgtConstants.GatewayEventTypes
                .SUBSCRIPTION_CREATE);
        subscriptionAddEvent.setSubscriptionsList(subscriptionValidationDataList);
        publishToStoreTopic(subscriptionAddEvent);
        if (log.isDebugEnabled()) {
            log.debug("Subscription created event has been successfully published to broker");
        }
    }

    @Override
    public void updateAPISubscriptionStatus(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException {
        SubscriptionEvent subscriptionBlockEvent = new SubscriptionEvent(APIMgtConstants.GatewayEventTypes
                .SUBSCRIPTION_STATUS_CHANGE);
        subscriptionBlockEvent.setSubscriptionsList(subscriptionValidationDataList);
        publishToStoreTopic(subscriptionBlockEvent);
        if (log.isDebugEnabled()) {
            log.debug("Subscription updated event has been successfully published to broker");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAPISubscription(List<SubscriptionValidationData> subscriptionValidationDataList) throws
            GatewayException {
        SubscriptionEvent subscriptionDeleteEvent = new SubscriptionEvent(
                APIMgtConstants.GatewayEventTypes.SUBSCRIPTION_DELETE);
        subscriptionDeleteEvent.setSubscriptionsList(subscriptionValidationDataList);
        publishToStoreTopic(subscriptionDeleteEvent);
        if (log.isDebugEnabled()) {
            log.debug("Subscription deleted event has been successfully published to broker");
        }
    }

    @Override
    public void addEndpoint(Endpoint endpoint) throws GatewayException {

        EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_CREATE);
        dto.setEndpoint(endpoint);
        publishToPublisherTopic(dto);

    }

    @Override
    public void updateEndpoint(Endpoint endpoint) throws GatewayException {
        EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_UPDATE);
        dto.setEndpoint(endpoint);
        publishToPublisherTopic(dto);

    }

    @Override
    public void deleteEndpoint(Endpoint endpoint) throws GatewayException {

        EndpointEvent dto = new EndpointEvent(APIMgtConstants.GatewayEventTypes.ENDPOINT_DELETE);
        dto.setEndpoint(endpoint);
        publishToPublisherTopic(dto);
    }

    /**
     * Publish event to publisher topic
     *
     * @param gatewayDTO gateway data transfer object
     * @throws GatewayException If there is a failure to publish to gateway
     */
    private void publishToPublisherTopic(GatewayEvent gatewayDTO) throws GatewayException {

        BrokerUtil.publishToTopic(publisherTopic, gatewayDTO);
        if (log.isDebugEnabled()) {
            log.debug("Gateway event : " + gatewayDTO.getEventType() + " has been published to publisher topic : " +
                    publisherTopic);
        }
    }

    /**
     * Publish event to store topic
     *
     * @param gatewayDTO gateway data transfer object
     * @throws GatewayException If there is a failure to publish to gateway
     */
    private void publishToStoreTopic(GatewayEvent gatewayDTO) throws GatewayException {
        BrokerUtil.publishToTopic(storeTopic, gatewayDTO);
        if (log.isDebugEnabled()) {
            log.debug("Gateway event : " + gatewayDTO.getEventType() + " has been published to store topic : " +
                    storeTopic);
        }
    }

    /**
     * Publish event to throttle topic
     *
     * @param gatewayDTO gateway data transfer object
     * @throws GatewayException If there is a failure to publish to gateway
     */
    private void publishToThrottleTopic(GatewayEvent gatewayDTO) throws GatewayException {
        BrokerUtil.publishToTopic(throttleTopic, gatewayDTO);
        if (log.isDebugEnabled()) {
            log.debug("Gateway event : " + gatewayDTO.getEventType() + " has been published to store topic : " +
                    storeTopic);
        }
    }

    @Override
    public void changeAPIState(API api, String status) throws GatewayException {
        //create the message to be sent to the gateway. This contains the basic details of the API and target
        //lifecycle state.
        APIEvent gatewayDTO = new APIEvent(APIMgtConstants.GatewayEventTypes.API_STATE_CHANGE);
        gatewayDTO.setLabels(api.getLabels());
        gatewayDTO.setApiSummary(toAPISummary(api));
        publishToPublisherTopic(gatewayDTO);

    }

    @Override
    public void addApplication(Application application) throws GatewayException {
        if (application != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_CREATE);
            applicationEvent.setApplicationId(application.getId());
            applicationEvent.setName(application.getName());
            applicationEvent.setThrottlingTier(application.getPolicy().getUuid());
            applicationEvent.setSubscriber(application.getCreatedUser());
            publishToStoreTopic(applicationEvent);
            if (log.isDebugEnabled()) {
                log.debug("Application : " + application.getName() + " created event has been successfully published " +
                        "to broker");
            }
        }
    }


    @Override
    public void updateApplication(Application application) throws GatewayException {
        if (application != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_UPDATE);
            applicationEvent.setApplicationId(application.getId());
            applicationEvent.setName(application.getName());
            applicationEvent.setThrottlingTier(application.getPolicy().getUuid());
            applicationEvent.setSubscriber(application.getCreatedUser());
            publishToStoreTopic(applicationEvent);
            if (log.isDebugEnabled()) {
                log.debug("Application : " + application.getName() + " updated event has been successfully published " +
                        "to broker");
            }
        }
    }

    @Override
    public void deleteApplication(String applicationId) throws GatewayException {
        if (applicationId != null) {
            ApplicationEvent applicationEvent = new ApplicationEvent(APIMgtConstants.GatewayEventTypes
                    .APPLICATION_DELETE);
            applicationEvent.setApplicationId(applicationId);
            publishToStoreTopic(applicationEvent);
            if (log.isDebugEnabled()) {
                log.debug("Application : " + applicationId + " deleted event has been successfully published " +
                        "to broker");
            }
        }
    }

    @Override
    public void addPolicy(PolicyValidationData policyValidationData) throws GatewayException {
        if (policyValidationData != null) {
            PolicyEvent policyEvent = new PolicyEvent(APIMgtConstants.GatewayEventTypes.POLICY_CREATE);
            policyEvent.setId(policyValidationData.getId());
            policyEvent.setName(policyValidationData.getName());
            policyEvent.setStopOnQuotaReach(policyValidationData.isStopOnQuotaReach());
            publishToThrottleTopic(policyEvent);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + policyValidationData.getName() + " add event has been successfully published " +
                        "to broker");
            }
        }
    }

    @Override
    public void updatePolicy(PolicyValidationData policyValidationData) throws GatewayException {
        if (policyValidationData != null) {
            PolicyEvent policyEvent = new PolicyEvent(APIMgtConstants.GatewayEventTypes.POLICY_UPDATE);
            policyEvent.setId(policyValidationData.getId());
            policyEvent.setName(policyValidationData.getName());
            policyEvent.setStopOnQuotaReach(policyValidationData.isStopOnQuotaReach());
            publishToThrottleTopic(policyEvent);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + policyValidationData.getName() + " update event has been successfully " +
                        "published " +
                        "to broker");
            }
        }
    }

    @Override
    public void deletePolicy(PolicyValidationData policyValidationData) throws GatewayException {
        if (policyValidationData != null) {
            PolicyEvent policyEvent = new PolicyEvent(APIMgtConstants.GatewayEventTypes.POLICY_DELETE);
            policyEvent.setId(policyValidationData.getId());
            policyEvent.setName(policyValidationData.getName());
            policyEvent.setStopOnQuotaReach(policyValidationData.isStopOnQuotaReach());
            publishToThrottleTopic(policyEvent);
            if (log.isDebugEnabled()) {
                log.debug("Policy : " + policyValidationData.getName() + " delete event has been successfully " +
                        "published " +
                        "to broker");
            }
        }
    }

    @Override
    public void addBlockCondition(BlockConditions blockConditions) throws GatewayException {
        if (blockConditions != null) {
            BlockEvent blockEvent = new BlockEvent(APIMgtConstants.GatewayEventTypes.BLOCK_CONDITION_ADD);
            blockEvent.setConditionId(blockConditions.getConditionId());
            blockEvent.setUuid(blockConditions.getUuid());
            blockEvent.setConditionType(blockConditions.getConditionType());
            blockEvent.setEnabled(blockConditions.isEnabled());
            blockEvent.setConditionValue(blockConditions.getConditionValue());
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP.equals(blockConditions
                    .getConditionType())) {
                blockEvent.setFixedIp(APIUtils.ipToLong(blockConditions.getConditionValue()));
            }
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE.equals(blockConditions
                    .getConditionType())) {
                blockEvent.setStartingIP(APIUtils.ipToLong(blockConditions.getStartingIP()));
                blockEvent.setEndingIP(APIUtils.ipToLong(blockConditions.getEndingIP()));
            }
            publishToThrottleTopic(blockEvent);
            if (log.isDebugEnabled()) {
                log.debug("BlockCondition : " + blockConditions.getUuid() + " add event has been successfully " +
                        "published " + "to broker");
            }
        }
    }

    @Override
    public void updateBlockCondition(BlockConditions blockConditions) throws GatewayException {
        if (blockConditions != null) {
            BlockEvent blockEvent = new BlockEvent(APIMgtConstants.GatewayEventTypes.BLOCK_CONDITION_ADD);
            blockEvent.setConditionId(blockConditions.getConditionId());
            blockEvent.setUuid(blockConditions.getUuid());
            blockEvent.setConditionType(blockConditions.getConditionType());
            blockEvent.setEnabled(blockConditions.isEnabled());
            blockEvent.setConditionValue(blockConditions.getConditionValue());
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP.equals(blockConditions
                    .getConditionType())) {
                blockEvent.setFixedIp(APIUtils.ipToLong(blockConditions.getConditionValue()));
            }
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE.equals(blockConditions
                    .getConditionType())) {
                blockEvent.setStartingIP(APIUtils.ipToLong(blockConditions.getStartingIP()));
                blockEvent.setEndingIP(APIUtils.ipToLong(blockConditions.getEndingIP()));
            }
            publishToThrottleTopic(blockEvent);
            if (log.isDebugEnabled()) {
                log.debug("BlockCondition : " + blockConditions.getUuid() + " update event has been successfully " +
                        "published " + "to broker");
            }
        }
    }

    @Override
    public void deleteBlockCondition(BlockConditions blockConditions) throws GatewayException {
        if (blockConditions != null) {
            BlockEvent blockEvent = new BlockEvent(APIMgtConstants.GatewayEventTypes.BLOCK_CONDITION_ADD);
            blockEvent.setConditionId(blockConditions.getConditionId());
            blockEvent.setUuid(blockConditions.getUuid());
            blockEvent.setConditionType(blockConditions.getConditionType());
            blockEvent.setEnabled(blockConditions.isEnabled());
            blockEvent.setConditionValue(blockConditions.getConditionValue());
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITIONS_IP.equals(blockConditions
                    .getConditionType())) {
                blockEvent.setFixedIp(APIUtils.ipToLong(blockConditions.getConditionValue()));
            }
            if (APIMgtConstants.ThrottlePolicyConstants.BLOCKING_CONDITION_IP_RANGE.equals(blockConditions
                    .getConditionType())) {
                blockEvent.setStartingIP(APIUtils.ipToLong(blockConditions.getStartingIP()));
                blockEvent.setEndingIP(APIUtils.ipToLong(blockConditions.getEndingIP()));
            }
            publishToThrottleTopic(blockEvent);
            if (log.isDebugEnabled()) {
                log.debug("BlockCondition : " + blockConditions.getUuid() + " delete event has been successfully " +
                        "published " + "to broker");
            }
        }
    }

    /**
     * Convert API definition into APISummary
     *
     * @param api API definition
     * @return The summary of the API
     */
    private APISummary toAPISummary(API api) {
        APISummary apiSummary = new APISummary();
        apiSummary.setId(api.getId());
        apiSummary.setName(api.getName());
        apiSummary.setVersion(api.getVersion());
        apiSummary.setContext(api.getContext());
        apiSummary.setLifeCycleStatus(api.getLifeCycleStatus());
        apiSummary.setLifeCycleStatus(api.getLifeCycleStatus());
        apiSummary.setCreatedTime(api.getCreatedTime());
        apiSummary.setLastUpdatedTime(api.getLastUpdatedTime());
        apiSummary.setSecurityScheme(api.getSecurityScheme());
        return apiSummary;
    }

}
