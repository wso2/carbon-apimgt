/*
 * Copyright (c) 2026 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;

/**
 * Injects a Google Cloud OAuth2 bearer token into outbound requests to Vertex AI backends.
 * <p>
 * This mediator is the GCP counterpart of {@link AWSSigV4Signer}: where AWS SigV4 signs every request
 * from static credentials, GCP uses a short-lived OAuth2 access token. The token acquisition, caching
 * and lazy refresh are handled by a {@link GCPAccessTokenProvider} - a JDK-only implementation (no
 * third-party client library) - chosen at init time based on the configuration:
 * <ul>
 *     <li>When a service-account key is configured, {@link GCPServiceAccountTokenProvider} mints the
 *     token from that key via the JWT Bearer assertion grant.</li>
 *     <li>When no key is configured, {@link GCPMetadataTokenProvider} uses the gateway's attached GCP
 *     identity (Application Default Credentials / Workload Identity) via the metadata server - only
 *     valid when the gateway runs on GCP.</li>
 * </ul>
 * A single provider instance is created at mediator init time and reused, hitting the token source only
 * when the cached token is missing or near expiry. The resulting token is set as
 * {@code Authorization: Bearer <token>}.
 */
public class GCPOAuth2TokenInjector extends AbstractMediator implements ManagedLifecycle {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String DEFAULT_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private String serviceAccountKey;
    private String scope;

    private volatile GCPAccessTokenProvider tokenProvider;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        String appliedScope = StringUtils.isNotEmpty(scope) ? scope : DEFAULT_SCOPE;
        if (StringUtils.isNotEmpty(serviceAccountKey)) {
            try {
                this.tokenProvider = new GCPServiceAccountTokenProvider(serviceAccountKey, appliedScope);
            } catch (IllegalArgumentException e) {
                throw new SynapseException("Error while initializing GCP service-account credentials for "
                        + "GCPOAuth2TokenInjector. Verify the service-account key JSON is valid.", e);
            }
        } else {
            // No service-account key configured: fall back to the gateway's attached GCP identity
            // (Application Default Credentials / Workload Identity) via the metadata server. This is only
            // valid when the gateway runs on GCP compute with a service account attached to the workload.
            this.tokenProvider = new GCPMetadataTokenProvider(appliedScope);
            if (log.isDebugEnabled()) {
                log.debug("No GCP service-account key configured; using the gateway's attached GCP identity "
                        + "(metadata server) for GCPOAuth2TokenInjector.");
            }
        }
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("GCPOAuth2TokenInjector is invoked...");
        }
        try {
            // getAccessToken() is synchronized inside the provider and only performs a network round-trip
            // to the token source (Google token endpoint or metadata server) when the cached token is
            // missing or near expiry.
            String accessToken = tokenProvider.getAccessToken();
            if (StringUtils.isEmpty(accessToken)) {
                throw new SynapseException("Failed to obtain a GCP access token for the request.");
            }
            Map<String, Object> transportHeaders = getTransportHeaders(messageContext);
            transportHeaders.put(AUTHORIZATION_HEADER, BEARER + accessToken);
            if (log.isDebugEnabled()) {
                log.debug("GCP bearer token set: " + GatewayUtils.getMaskedToken(accessToken));
            }
            return true;
        } catch (Throwable e) {
            // Catch Throwable (not just IOException) so class-loading Errors and any runtime exception from
            // the token provider surface in the log with a full stack trace instead of Synapse's generic
            // "Error occurred in the mediation of the class mediator". Rethrown, never swallowed.
            log.error("GCPOAuth2TokenInjector failed while generating/injecting the GCP OAuth2 access token", e);
            throw new SynapseException("Error while generating the GCP OAuth2 access token: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getTransportHeaders(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2Ctx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Object headers = axis2Ctx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (headers instanceof Map) {
            return (Map<String, Object>) headers;
        }
        Map<String, Object> transportHeaders = new HashMap<>();
        axis2Ctx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, transportHeaders);
        return transportHeaders;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isContentAware() {

        return false;
    }

    public String getServiceAccountKey() {

        return serviceAccountKey;
    }

    public void setServiceAccountKey(String serviceAccountKey) {

        this.serviceAccountKey = serviceAccountKey;
    }

    public String getScope() {

        return scope;
    }

    public void setScope(String scope) {

        this.scope = scope;
    }
}
