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
import org.wso2.carbon.apimgt.api.gateway.FailoverPolicyConfigDTO;
import org.wso2.carbon.apimgt.api.gateway.ModelEndpointDTO;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.api.APIConstants.AIAPIConstants;

import java.util.List;

/**
 * Mediator for AI API Failover policy.
 */
public class FailoverMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(FailoverMediator.class);
    private String failoverConfigs;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        if (log.isDebugEnabled()) {
            log.debug("FallbackDecisionMediator: Initialized.");
        }
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("FailoverMediator mediation started.");
        }

        DataHolder.getInstance().initCache(GatewayUtils.getAPIKeyForEndpoints(messageContext));

        FailoverPolicyConfigDTO policyConfig = new Gson().fromJson(failoverConfigs, FailoverPolicyConfigDTO.class);
        List<ModelEndpointDTO> activeEndpoints = GatewayUtils.getActiveEndpoints(policyConfig, messageContext);

        ModelEndpointDTO targetEndpointModel =
                APIConstants.API_KEY_TYPE_PRODUCTION.equals(messageContext.getProperty(APIConstants.API_KEY_TYPE))
                        ? policyConfig.getProduction().getTargetModelEndpoint()
                        : policyConfig.getSandbox().getTargetModelEndpoint();

        messageContext.setProperty(AIAPIConstants.FAILOVER_TARGET_ENDPOINT, targetEndpointModel.getEndpointId());
        messageContext.setProperty(AIAPIConstants.FAILOVER_TARGET_MODEL, targetEndpointModel.getModel());
        messageContext.setProperty(AIAPIConstants.FAILOVER_ENDPOINTS, activeEndpoints);
        messageContext.setProperty(AIAPIConstants.SUSPEND_DURATION,
                policyConfig.getSuspendDuration() * 1000);
        messageContext.setProperty(AIAPIConstants.ENDPOINT_TIMEOUT,
                policyConfig.getRequestTimeout() * 1000);

        return true;
    }

    @Override
    public boolean isContentAware() {

        return false;
    }

    public String getFailoverConfigs() {

        return failoverConfigs;
    }

    public void setFailoverConfigs(String failoverConfigs) {

        this.failoverConfigs = failoverConfigs;
    }
}
