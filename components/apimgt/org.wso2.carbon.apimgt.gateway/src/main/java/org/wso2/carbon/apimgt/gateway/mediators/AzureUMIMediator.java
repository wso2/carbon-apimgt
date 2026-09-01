/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.AzureUmiTokenProvider;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * AzureUMIMediator injects an Azure Workload Identity (UMI) Bearer token into every
 * outgoing backend request. It is inserted into the Synapse sequence at API-deploy time
 * when the endpoint security type is {@code "umi"}.
 *
 * <p>The mediator eagerly initialises an {@link AzureUmiTokenProvider} in {@link #init},
 * so misconfigured env vars or a missing scope are caught at API-deploy time rather than
 * on the first live request. The provider (and the Azure Identity SDK behind it) handles
 * token caching and proactive refresh automatically.
 *
 * <p>Required AKS Workload Identity environment variables must be injected into the
 * gateway pod by the mutating webhook:
 * <ul>
 *   <li>AZURE_TENANT_ID</li>
 *   <li>AZURE_CLIENT_ID</li>
 *   <li>AZURE_FEDERATED_TOKEN_FILE</li>
 * </ul>
 *
 * <p>The token scope defaults to {@code https://ai.azure.com/.default} (set in
 * {@code default.json}) and can be overridden per-deployment via
 * {@code apim.ai.azure_umi.scope} in {@code deployment.toml}.
 */
public class AzureUMIMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(AzureUMIMediator.class);

    private AzureUmiTokenProvider tokenProvider;

    /**
     * Eagerly creates and initialises the {@link AzureUmiTokenProvider} so that missing
     * env vars or an invalid scope are caught at API-deploy time.
     * Synapse calls this method in a single-threaded deployment context before any
     * concurrent {@link #mediate} calls, so no synchronisation is required.
     */
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        log.debug("AzureUMIMediator: init");
        try {
            String scope = ServiceReferenceHolder.getInstance()
                    .getApiManagerConfigurationService().getAPIManagerConfiguration()
                    .getFirstProperty(APIConstants.AI.AZURE_UMI_SCOPE);
            AzureUmiTokenProvider provider = new AzureUmiTokenProvider();
            provider.init(Collections.singletonMap(APIConstants.AI.AZURE_UMI_SCOPE_KEY, scope));
            tokenProvider = provider;
        } catch (APIManagementException e) {
            // Re-throw as unchecked — a misconfigured mediator must not silently pass deployment.
            throw new RuntimeException("AzureUMIMediator: failed to initialise token provider — " + e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
        if (tokenProvider != null) {
            try {
                tokenProvider.close();
            } catch (IOException e) {
                log.warn("AzureUMIMediator: error closing token provider during destroy", e);
            }
            tokenProvider = null;
        }
    }

    /**
     * Injects an Azure UMI Bearer token as the {@code Authorization} header on
     * the outgoing backend request.
     *
     * @param messageContext Synapse message context for the current request.
     * @return {@code true} if the token was successfully injected; {@code false} on error
     *         (which causes Synapse to mark mediation as failed).
     */
    @Override
    public boolean mediate(MessageContext messageContext) {

        log.debug("AzureUMIMediator: injecting UMI Bearer token");
        try {
            String token = tokenProvider.getAccessToken();

            @SuppressWarnings("unchecked")
            Map<String, Object> transportHeaders =
                    (Map<String, Object>) ((Axis2MessageContext) messageContext)
                            .getAxis2MessageContext()
                            .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (transportHeaders == null) {
                transportHeaders = new HashMap<>();
                ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                        .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
            }

            transportHeaders.put(APIConstants.AUTHORIZATION_HEADER_DEFAULT,
                    APIConstants.AUTHORIZATION_BEARER + token);

            log.debug("AzureUMIMediator: Authorization header injected successfully");
            return true;

        } catch (APIManagementException e) {
            log.error("AzureUMIMediator: failed to acquire UMI token — " + e.getMessage(), e);
            org.apache.axis2.context.MessageContext axis2MC =
                    ((Axis2MessageContext) messageContext).getAxis2MessageContext();
            axis2MC.setProperty(PassThroughConstants.MESSAGE_BUILDER_INVOKED, Boolean.TRUE);
            try {
                RelayUtils.discardRequestMessage(axis2MC);
            } catch (AxisFault axisFault) {
                log.error("AzureUMIMediator: error discarding request message", axisFault);
            }
            Utils.send(messageContext, HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return false;
        }
    }

    @Override
    public boolean isContentAware() {
        return false;
    }
}
