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
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mediator for AI API Round Robin load balancing.
 */
public class RoundRobinMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(RoundRobinMediator.class);
    private String roundRobinConfigs;
    private final AtomicInteger counter = new AtomicInteger(0);

    /**
     * Initializes the mediator.
     *
     * @param synapseEnvironment Synapse environment context.
     */
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("RoundRobinMediator initialized.");
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
            log.debug("RoundRobinMediator mediation started.");
        }

        DataHolder.getInstance().initCache(GatewayUtils.getAPIKeyForEndpoints(messageContext));

        RBEndpointsPolicyDTO endpoints = new Gson().fromJson(roundRobinConfigs, RBEndpointsPolicyDTO.class);
        List<RBEndpointDTO> activeEndpoints = GatewayUtils.getActiveEndpoints(endpoints, messageContext);

        if (activeEndpoints != null && !activeEndpoints.isEmpty()) {
            RBEndpointDTO nextEndpoint = getRoundRobinEndpoint(activeEndpoints);
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
     * Selects an endpoint using a round-robin selection mechanism.
     *
     * @param endpoints List of active endpoints.
     * @return The selected RBEndpointDTO based on round-robin order.
     */
    private RBEndpointDTO getRoundRobinEndpoint(List<RBEndpointDTO> endpoints) {

        int index = counter.getAndIncrement() % endpoints.size();
        return endpoints.get(index);
    }

    /**
     * Retrieves the endpoint list in JSON format.
     *
     * @return The endpoint list as a JSON string.
     */
    public String getRoundRobinConfigs() {

        return roundRobinConfigs;
    }

    /**
     * Sets the endpoint list as a JSON string.
     *
     * @param roundRobinConfigs JSON string containing endpoint details.
     */
    public void setRoundRobinConfigs(String roundRobinConfigs) {

        this.roundRobinConfigs = roundRobinConfigs;
    }

    /**
     * Destroys the mediator.
     */
    @Override
    public void destroy() {

    }
}
