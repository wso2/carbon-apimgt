/*
 * Copyright (c) 2025 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
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
public class AIAPIRoundRobinMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(AIAPIRoundRobinMediator.class);
    private String endpointList;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("AIAPIRoundRobinMediator initialized.");
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("AIAPIRoundRobinMediator mediation started.");
        }
        endpointList = "{"
                + "\"production\": ["
                + "  {\"model\": \"gpt-4o-mini\", \"endpointId\": \"668b4278-665e-47df-9897-89490903de6c\", " +
                "\"weight\": 0.5},"
                + "  {\"model\": \"gpt-35-turbo\", \"endpointId\": \"299beac1-ec9e-4a9b-b9fd-8be60cdbaeda\", " +
                "\"weight\": 0.5}"
                + "],"
                + "\"sandbox\": ["
                + "  {\"model\": \"gpt-4o\", \"endpointId\": \"299beac1-ec9e-3a9b-b9fd-8be60cdbaeda\", \"weight\": 1}"
                + "],"
                + "\"suspendDuration\": 60"
                + "}";

        RBEndpointsPolicyDTO endpoints = new Gson().fromJson(endpointList, RBEndpointsPolicyDTO.class);
        RBEndpointDTO nextEndpoint = getNextEndpoint(endpoints, messageContext);
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_ENDPOINT, nextEndpoint.getEndpointId());
        messageContext.setProperty(APIConstants.AIAPIConstants.TARGET_MODEL, nextEndpoint.getModel());
        messageContext.setProperty(APIConstants.AIAPIConstants.SUSPEND_DURATION, endpoints.getSuspendDuration());
        return true;
    }

    /**
     * Retrieves the next available endpoint based on load balancing policy.
     *
     * @param endpoints      RBEndpointsPolicyDTO Object.
     * @param messageContext Synapse message context.
     * @return Selected RBEndpointDTO.
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
            if (!DataHolder.getInstance().isEndpointSuspended(endpoint.getEndpointId()
                    + "_" + endpoint.getModel())) {
                activeEndpoints.add(endpoint);
            }
        }
        if (activeEndpoints.isEmpty()) {
            return null;
        }
        return getWeightedRandomEndpoint(activeEndpoints);
    }

    /**
     * Selects an endpoint using weighted random selection.
     *
     * @param endpoints List of active endpoints.
     * @return Selected RBEndpointDTO.
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
     * Retrieves the endpoint list.
     *
     * @return Endpoint list as a JSON string.
     */
    public String getEndpointList() {

        return endpointList;
    }

    /**
     * Sets the endpoint list.
     *
     * @param endpointList Endpoint list as a JSON string.
     */
    public void setEndpointList(String endpointList) {

        this.endpointList = endpointList;
    }
}
