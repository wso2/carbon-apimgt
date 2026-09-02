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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
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
 * third-party client library):
 * <ul>
 *     <li>When a service-account key is configured, {@link GCPServiceAccountTokenProvider} mints the
 *     token from that key via the JWT Bearer assertion grant.</li>
 *     <li>When no key is configured (no chunks), {@link GCPMetadataTokenProvider} uses the gateway's
 *     attached GCP identity (Application Default Credentials / Workload Identity) via the metadata
 *     server - only valid when the gateway runs on GCP.</li>
 * </ul>
 * The service-account key is delivered to the mediator as ordered base64 chunks via synapse
 * message-context properties ({@code <prefix><index>}), reassembled here into the full key. The chunk
 * values are set by the endpoint sequence: literal values in normal mode, or {@code wso2:vault-lookup}
 * results in secure-vault mode - so this mediator is agnostic to which mode is in effect. When key chunks
 * are present the provider is built lazily on the first request (the chunks are per-request properties)
 * and cached; the keyless (metadata) provider is built eagerly at init time. The reassembled key is
 * handled as a byte stream and wiped after use, never held as an immutable {@code String}.
 */
public class GCPOAuth2Mediator extends AbstractMediator implements ManagedLifecycle {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String DEFAULT_SCOPE = "https://www.googleapis.com/auth/cloud-platform";

    private String serviceAccountKeyChunkPrefix;
    private int serviceAccountKeyChunkCount;
    private String scope;

    private volatile GCPAccessTokenProvider tokenProvider;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        // Keyless: no key chunks are configured, so use the gateway's attached GCP identity (ADC / Workload
        // Identity) via the metadata server - this can be resolved eagerly. When key chunks ARE configured they
        // arrive as per-request properties (literal values in normal mode, vault-lookups in secure-vault mode),
        // so the service-account provider is built lazily on the first request and cached.
        if (serviceAccountKeyChunkCount == 0) {
            this.tokenProvider = new GCPMetadataTokenProvider(appliedScope());
            if (log.isDebugEnabled()) {
                log.debug("No GCP service-account key configured; using the gateway's attached GCP identity "
                        + "(metadata server) for GCPOAuth2Mediator.");
            }
        }
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("GCPOAuth2Mediator is invoked...");
        }
        try {
            GCPAccessTokenProvider provider = tokenProvider;
            if (provider == null) {
                provider = buildProviderFromChunks(messageContext);
            }
            // getAccessToken() is synchronized inside the provider and only performs a network round-trip
            // to the token source (Google token endpoint or metadata server) when the cached token is
            // missing or near expiry.
            String accessToken = provider.getAccessToken();
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
            log.error("GCPOAuth2Mediator failed while generating/injecting the GCP OAuth2 access token", e);
            throw new SynapseException("Error while generating the GCP OAuth2 access token: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the service-account token provider once from the ordered key chunks and caches it (double-checked
     * under the monitor). The key is reassembled into a {@code byte[]} and streamed into the provider - never
     * held as an immutable {@code String} - and the buffer is wiped as soon as the provider has consumed it.
     */
    private synchronized GCPAccessTokenProvider buildProviderFromChunks(MessageContext messageContext) {

        if (tokenProvider != null) {
            return tokenProvider;
        }
        byte[] keyBytes = reassembleServiceAccountKey(messageContext);
        try (InputStream keyStream = new ByteArrayInputStream(keyBytes)) {
            this.tokenProvider = new GCPServiceAccountTokenProvider(keyStream, appliedScope());
        } catch (IllegalArgumentException e) {
            throw new SynapseException("Error while initializing GCP service-account credentials for "
                    + "GCPOAuth2Mediator. Verify the service-account key JSON is valid.", e);
        } catch (IOException e) {
            throw new SynapseException("Error while reading the reassembled GCP service-account key.", e);
        } finally {
            // Wipe the plaintext key bytes as soon as the provider has parsed them.
            Arrays.fill(keyBytes, (byte) 0);
        }
        return tokenProvider;
    }

    /**
     * Reassembles the ordered base64 chunk properties into the raw service-account key bytes. Each property's
     * value is already resolved - a literal (normal mode) or a {@code wso2:vault-lookup} result (secure-vault
     * mode) - so this is mode-agnostic. The chunks are base64 fragments of the key: concatenate then decode.
     */
    private byte[] reassembleServiceAccountKey(MessageContext messageContext) {

        // Assemble the base64 as bytes (not an immutable String) so the encoded key can be wiped after decoding.
        ByteArrayOutputStream base64 = new ByteArrayOutputStream();
        for (int i = 0; i < serviceAccountKeyChunkCount; i++) {
            Object chunk = messageContext.getProperty(serviceAccountKeyChunkPrefix + i);
            if (chunk == null || chunk.toString().isEmpty()) {
                throw new SynapseException("Missing GCP service-account key chunk " + i
                        + " (secure-vault lookup or chunk property returned empty).");
            }
            byte[] chunkBytes = chunk.toString().getBytes(StandardCharsets.US_ASCII);
            base64.write(chunkBytes, 0, chunkBytes.length);
        }
        byte[] base64Bytes = base64.toByteArray();
        try {
            return Base64.getDecoder().decode(base64Bytes);
        } finally {
            // Wipe the assembled base64-encoded key so it does not linger in the heap.
            Arrays.fill(base64Bytes, (byte) 0);
        }
    }

    private String appliedScope() {

        return StringUtils.isNotEmpty(scope) ? scope : DEFAULT_SCOPE;
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

    public String getServiceAccountKeyChunkPrefix() {

        return serviceAccountKeyChunkPrefix;
    }

    public void setServiceAccountKeyChunkPrefix(String serviceAccountKeyChunkPrefix) {

        this.serviceAccountKeyChunkPrefix = serviceAccountKeyChunkPrefix;
    }

    public int getServiceAccountKeyChunkCount() {

        return serviceAccountKeyChunkCount;
    }

    /**
     * Synapse sets class-mediator properties as strings; parse the count here so the setter is robust
     * regardless of how the value is supplied.
     */
    public void setServiceAccountKeyChunkCount(String serviceAccountKeyChunkCount) {

        this.serviceAccountKeyChunkCount = StringUtils.isNotEmpty(serviceAccountKeyChunkCount)
                ? Integer.parseInt(serviceAccountKeyChunkCount) : 0;
    }

    public String getScope() {

        return scope;
    }

    public void setScope(String scope) {

        this.scope = scope;
    }
}
