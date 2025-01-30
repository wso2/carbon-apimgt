/*
 * Copyright (c) 2024 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.utils.URIBuilder;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.api.APIConstants;
import org.wso2.carbon.apimgt.api.LLMProviderConfiguration;
import org.wso2.carbon.apimgt.api.LLMProviderService;
import org.wso2.carbon.apimgt.api.model.LLMProviderInfo;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

/**
 * ApiKeyMediator is responsible for handling API key injection into
 * requests. It supports adding API keys as either HTTP headers or query parameters
 * based on the provided identifier type.
 */
public class AIAPIMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(AIAPIMediator.class);
    private String llmProviderId;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        log.debug("AI API Mediator init");
    }

    @Override
    public void destroy() {

    }

    /**
     * Executes the mediation logic for injecting the API key into the request.
     *
     * @param messageContext Synapse message context for the current request.
     * @return true if mediation is successful, false otherwise.
     */
    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("AI API Mediator is invoked...");
        }
        LLMProviderInfo provider = DataHolder.getInstance().getLLMProviderConfigurations(this.llmProviderId);
        if (provider == null) {
            log.error("No LLM provider found for provider ID: " + llmProviderId);
            return false;
        }
        LLMProviderConfiguration providerConfiguration = provider.getConfigurations();
        LLMProviderService llmProviderService = ServiceReferenceHolder.getInstance()
                .getLLMProviderService(providerConfiguration.getConnectorType());
        if (llmProviderService == null) {
            log.error("LLM provider service not found for the provider with ID: " + llmProviderId);
            return false;
        }
        Map<String, String> metadataMap = new HashMap<>();

        metadataMap.put(APIConstants.AIAPIConstants.NAME, provider.getName());
        metadataMap.put(APIConstants.AIAPIConstants.API_VERSION, provider.getApiVersion());
        ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .setProperty(APIConstants.AIAPIConstants.AI_API_REQUEST_METADATA,
                        metadataMap);
        return true;
    }

    @Override
    public boolean isContentAware() {

        return false;
    }

    public String getLlmProviderId() {

        return llmProviderId;
    }

    public void setLlmProviderId(String llmProviderId) {

        this.llmProviderId = llmProviderId;
    }
}
