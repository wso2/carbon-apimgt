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

import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.TargetResponse;

/**
 * Response-flow companion to {@link GCPOAuth2Mediator}: when a Vertex AI backend rejects the injected GCP access
 * token with HTTP 401, this evicts that cached token so the next request mints a fresh one, instead of the
 * time-based cache continuing to serve the now-rejected token until its scheduled refresh window.
 * <p>
 * This is the gateway equivalent of the 401 handling in google-auth's {@code HttpCredentialsAdapter} (which
 * refreshes the credential on a 401), narrowed to the WSO2 response-flow idiom of {@link
 * org.wso2.carbon.apimgt.gateway.mediators.oauth.OAuthResponseMediator}: it invalidates reactively but does NOT
 * rewrite the response or auto-retry the request in place - the current request still returns its 401; only the
 * next one self-heals. It runs only in GCP (Vertex) endpoint sequences (emitted GCP-gated by the AI endpoint
 * template), so non-GCP AI providers are unaffected.
 */
public class GCPOAuth2ResponseMediator extends AbstractMediator implements ManagedLifecycle {

    // The pass-through transport stashes the backend response here; the same property OAuthResponseMediator reads.
    private static final String TARGET_RESPONSE_PROPERTY = "pass-through.Target-Response";

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (messageContext == null) {
            return true;
        }
        Object responseObject = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                .getProperty(TARGET_RESPONSE_PROPERTY);
        if (!(responseObject instanceof TargetResponse)) {
            return true;
        }
        if (((TargetResponse) responseObject).getStatus() != HttpStatus.SC_UNAUTHORIZED) {
            return true;
        }
        // The backend rejected the token (401). Invalidate the exact provider that minted it (published on the
        // message context by GCPOAuth2Mediator) so the next request re-mints, then let the 401 response flow on
        // unchanged - no fault injection, no in-place retry.
        Object providerObject = messageContext.getProperty(GCPOAuth2Mediator.GCP_TOKEN_PROVIDER_PROPERTY);
        if (providerObject instanceof GCPAccessTokenProvider) {
            ((GCPAccessTokenProvider) providerObject).invalidate();
            if (log.isDebugEnabled()) {
                log.debug("Vertex backend returned 401; invalidated the cached GCP access token so the next "
                        + "request mints a fresh one.");
            }
        }
        return true;
    }

    @Override
    public boolean isContentAware() {

        return false;
    }
}
