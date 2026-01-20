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
import com.google.gson.JsonSyntaxException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.gateway.ModelEndpointDTO;
import org.wso2.carbon.apimgt.api.gateway.RBPolicyConfigDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Mediator for AI API Round Robin load balancing.
 */
public class WeightedRoundRobinMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(WeightedRoundRobinMediator.class);
    private String weightedRoundRobinConfigs;

    /**
     * Initializes the mediator.
     *
     * @param synapseEnvironment Synapse environment context.
     */
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("WeightedRoundRobinMediator initialized.");
        }
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
            log.debug("WeightedRoundRobinMediator mediation started.");
        }

        DataHolder.getInstance().initCache(GatewayUtils.getAPIKeyForEndpoints(messageContext));

        RBPolicyConfigDTO endpoints;
        try {
            endpoints = new Gson().fromJson(weightedRoundRobinConfigs, RBPolicyConfigDTO.class);
        } catch (JsonSyntaxException e) {
            log.error("Failed to parse weighted round robin configuration", e);
            return false;
        }

        String apiKeyType = (String) messageContext.getProperty(APIConstants.API_KEY_TYPE);

        List<ModelEndpointDTO> selectedEndpoints = APIConstants.API_KEY_TYPE_PRODUCTION
                .equals(apiKeyType)
                ? endpoints.getProduction()
                : endpoints.getSandbox();

        if (selectedEndpoints == null || selectedEndpoints.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("RoundRobin policy is not set for " + apiKeyType + ", bypassing mediation.");
            }
            return true;
        }

        List<ModelEndpointDTO> activeEndpoints = GatewayUtils.filterActiveEndpoints(selectedEndpoints, messageContext);

        if (!activeEndpoints.isEmpty()) {
            ModelEndpointDTO nextEndpoint = getWeightedRandomEndpoint(activeEndpoints);
            Map<String, Object> roundRobinConfigs = new HashMap<>();
            roundRobinConfigs.put(AIAPIConstants.TARGET_MODEL_ENDPOINT, nextEndpoint);
            roundRobinConfigs.put(AIAPIConstants.SUSPEND_DURATION,
                    endpoints.getSuspendDuration() * AIAPIConstants.MILLISECONDS_IN_SECOND);
            messageContext.setProperty(AIAPIConstants.HANDLE_ROUTING_CONFIGS, roundRobinConfigs);
        } else {
            messageContext.setProperty(AIAPIConstants.TARGET_ENDPOINT, AIAPIConstants.REJECT_ENDPOINT);
        }
        return true;
    }

    /**
     * Selects an endpoint using a weighted random selection mechanism.
     *
     * @param endpoints List of active endpoints.
     * @return The selected ModelEndpointDTO based on weight.
     */
    private ModelEndpointDTO getWeightedRandomEndpoint(List<ModelEndpointDTO> endpoints) {

        float totalWeight = 0.0f;
        List<Float> cumulativeWeights = new ArrayList<>();

        for (ModelEndpointDTO endpoint : endpoints) {
            double weight = Math.max(endpoint.getWeight(), 0.1f);
            totalWeight += (float) weight;
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
    public String getWeightedRoundRobinConfigs() {

        return weightedRoundRobinConfigs;
    }

    /**
     * Sets the endpoint list as a JSON string.
     *
     * @param weightedRoundRobinConfigs JSON string containing endpoint details.
     */
    public void setWeightedRoundRobinConfigs(String weightedRoundRobinConfigs) {

        this.weightedRoundRobinConfigs = weightedRoundRobinConfigs;
    }

    /**
     * Destroys the mediator.
     */
    @Override
    public void destroy() {

    }
}
