/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.gateway.RBEndpointDTO;
import org.wso2.carbon.apimgt.api.gateway.RBEndpointsPolicyDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.wso2.carbon.apimgt.impl.APIConstants.API_KEY_TYPE;
import static org.wso2.carbon.apimgt.impl.APIConstants.API_KEY_TYPE_PRODUCTION;

/**
 * Mediator for AI API Round Robin load balancing.
 */
public class WeightedRoundRobinMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(WeightedRoundRobinMediator.class);
    private String endpointList;

    /**
     * Initializes the mediator.
     *
     * @param synapseEnvironment Synapse environment context.
     */
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("AIAPIRoundRobinMediator initialized.");
        }
    }

    /**
     * Destroys the mediator.
     */
    @Override
    public void destroy() {

    }

    /**
     * Processes the message and selects an appropriate endpoint for load balancing.
     *
     * @param messageContext Synapse message context.
     * @return Always returns true to continue message mediation.
     */
    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("AIAPIRoundRobinMediator mediation started.");
        }

        RBEndpointsPolicyDTO endpoints = new Gson().fromJson(endpointList, RBEndpointsPolicyDTO.class);
        RBEndpointDTO nextEndpoint = getNextEndpoint(endpoints, messageContext);

        if (nextEndpoint != null) {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT, nextEndpoint.getEndpointId());
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_MODEL, nextEndpoint.getModel());
            messageContext.setProperty(APIConstants.AIAPIConstants.SUSPEND_DURATION, endpoints.getSuspendDuration());
        } else {
            messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT,
                    APIConstants.AIAPIConstants.REJECT_ENDPOINT);
        }
        return true;
    }

    /**
     * Retrieves the next available endpoint based on the round-robin load balancing policy.
     *
     * @param endpoints      RBEndpointsPolicyDTO object containing endpoint configurations.
     * @param messageContext Synapse message context.
     * @return The selected RBEndpointDTO, or null if no active endpoints are available.
     */
    public RBEndpointDTO getNextEndpoint(RBEndpointsPolicyDTO endpoints, MessageContext messageContext) {

        List<RBEndpointDTO> productionEndpoints = endpoints.getProduction();
        List<RBEndpointDTO> sandboxEndpoints = endpoints.getSandbox();

        List<RBEndpointDTO> selectedEndpoints = API_KEY_TYPE_PRODUCTION.equals(messageContext.getProperty(API_KEY_TYPE))
                ? productionEndpoints
                : sandboxEndpoints;

        if (selectedEndpoints == null || selectedEndpoints.isEmpty()) {
            return null;
        }

        List<RBEndpointDTO> activeEndpoints = new ArrayList<>();
        for (RBEndpointDTO endpoint : selectedEndpoints) {
            if (!DataHolder.getInstance()
                    .isEndpointSuspended(endpoint.getEndpointId() + "_" + endpoint.getModel())) {
                activeEndpoints.add(endpoint);
            }
        }

        if (activeEndpoints.isEmpty()) {
            return null;
        }
        return getWeightedRandomEndpoint(activeEndpoints);
    }

    /**
     * Selects an endpoint using a weighted random selection mechanism.
     *
     * @param endpoints List of active endpoints.
     * @return The selected RBEndpointDTO based on weight.
     */
    private RBEndpointDTO getWeightedRandomEndpoint(List<RBEndpointDTO> endpoints) {

        float totalWeight = 0.0f;
        List<Float> cumulativeWeights = new ArrayList<>();

        for (RBEndpointDTO endpoint : endpoints) {
            double weight = Math.max(endpoint.getWeight(), 0.1f);
            totalWeight += weight;
            cumulativeWeights.add(totalWeight);
        }

        Random random = new Random();
        float randomValue = random.nextFloat() * totalWeight;

        for (int i = 0; i < cumulativeWeights.size(); i++) {
            if (randomValue < cumulativeWeights.get(i)) {
                return endpoints.get(i);
            }
        }
        return endpoints.get(0);
    }

    /**
     * Retrieves the endpoint list in JSON format.
     *
     * @return The endpoint list as a JSON string.
     */
    public String getEndpointList() {

        return endpointList;
    }

    /**
     * Sets the endpoint list as a JSON string.
     *
     * @param endpointList JSON string containing endpoint details.
     */
    public void setEndpointList(String endpointList) {

        this.endpointList = endpointList;
    }
}
