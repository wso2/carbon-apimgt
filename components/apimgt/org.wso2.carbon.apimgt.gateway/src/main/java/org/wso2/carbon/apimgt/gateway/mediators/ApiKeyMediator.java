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
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.mediators.oauth.OAuthMediator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * ApiKeyMediator is responsible for handling API key injection into
 * requests. It supports adding API keys as either HTTP headers or query parameters
 * based on the provided identifier type.
 */
public class ApiKeyMediator extends AbstractMediator implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(ApiKeyMediator.class);
    private String apiKeyIdentifier;
    private String apiKeyValue;
    private String apiKeyIdentifierType;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        log.debug("ApiKey Mediator init");
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
            log.debug("ApiKey Mediator is invoked...");
        }
        if (apiKeyIdentifier != null && apiKeyValue != null) {
            try {
                org.apache.axis2.context.MessageContext axCtx = ((Axis2MessageContext) messageContext)
                        .getAxis2MessageContext();
                if (APIConstants.AIAPIConstants.API_KEY_IDENTIFIER_TYPE_HEADER
                        .equals(apiKeyIdentifierType)) {
                    Map<String, Object> transportHeaders =
                            (Map<String, Object>) ((Axis2MessageContext) messageContext)
                                    .getAxis2MessageContext()
                                    .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                    transportHeaders.put(apiKeyIdentifier, apiKeyValue);
                } else if (APIConstants.AIAPIConstants.API_KEY_IDENTIFIER_TYPE_QUERY_PARAMETER
                        .equals(apiKeyIdentifierType)) {
                    URI updatedFullPath =
                            new URIBuilder((String) axCtx.getProperty(APIMgtGatewayConstants.REST_URL_POSTFIX))
                                    .addParameter(apiKeyIdentifier, apiKeyValue).build();
                    axCtx.setProperty(APIMgtGatewayConstants.REST_URL_POSTFIX, updatedFullPath.toString());
                }
            } catch (URISyntaxException e) {
                log.error("Error occurred during parsing query parameters for AI API.");
            }
        }
        return true;
    }

    @Override
    public boolean isContentAware() {

        return false;
    }

    public String getApiKeyIdentifier() {

        return apiKeyIdentifier;
    }

    public void setApiKeyIdentifier(String apiKeyIdentifier) {

        this.apiKeyIdentifier = apiKeyIdentifier;
    }

    public String getApiKeyValue() {

        return apiKeyValue;
    }

    public void setApiKeyValue(String apiKeyValue) {

        this.apiKeyValue = apiKeyValue;
    }

    public String getApiKeyIdentifierType() {

        return apiKeyIdentifierType;
    }

    public void setApiKeyIdentifierType(String apiKeyIdentifierType) {

        this.apiKeyIdentifierType = apiKeyIdentifierType;
    }
}
