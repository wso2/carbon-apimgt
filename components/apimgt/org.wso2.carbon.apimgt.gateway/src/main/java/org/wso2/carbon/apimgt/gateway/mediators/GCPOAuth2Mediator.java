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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

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
 *     <li>When no key is configured, {@link GCPMetadataTokenProvider} uses the gateway's
 *     attached GCP identity (Application Default Credentials / Workload Identity) via the metadata
 *     server - only valid when the gateway runs on GCP.</li>
 * </ul>
 * The service-account key is delivered to the mediator as a single base64 {@code serviceAccountKey}
 * property, set by the endpoint sequence: the whole base64 key as a literal in normal mode, or a
 * pipe-joined {@code concat} of {@code wso2:vault-lookup} results in secure-vault mode - so this mediator
 * is agnostic to which mode is in effect. Because the value is a per-request property (an expression in
 * secure-vault mode), the provider is built lazily on the first request and cached; when the property is
 * absent the mediator is keyless and uses the metadata provider. The reassembled key is handled as a byte
 * stream and wiped after use, never held as an immutable {@code String}.
 */
public class GCPOAuth2Mediator extends AbstractMediator implements ManagedLifecycle {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String DEFAULT_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
    // Carries the built token provider from this (request-flow) mediator to the response-flow
    // GCPOAuth2ResponseMediator, so a backend 401 can invalidate the exact cached token that was injected.
    public static final String GCP_TOKEN_PROVIDER_PROPERTY = "GCP_TOKEN_PROVIDER_INSTANCE";

    private String serviceAccountKey;
    private String scope;

    private volatile GCPAccessTokenProvider tokenProvider;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {

        // Provider selection is deferred to the first request: in secure-vault mode the serviceAccountKey
        // property is an expression resolved per-request, so it is not yet available at init time. Deciding
        // keyless-vs-key here would misread a not-yet-resolved vault key as keyless.
    }

    @Override
    public boolean mediate(MessageContext messageContext) {

        if (log.isDebugEnabled()) {
            log.debug("GCPOAuth2Mediator is invoked...");
        }
        try {
            GCPAccessTokenProvider provider = tokenProvider;
            if (provider == null) {
                provider = buildProvider();
            }
            // Expose the provider so the response-flow mediator can invalidate this cached token on a backend 401.
            messageContext.setProperty(GCP_TOKEN_PROVIDER_PROPERTY, provider);
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
        } catch (Exception e) {
            // Wrap any failure from provider construction or token acquisition (IOException, IllegalArgumentException,
            // SynapseException, etc.) with a full stack trace instead of Synapse's generic "Error occurred in the
            // mediation of the class mediator". Rethrown, never swallowed. JVM Errors are intentionally not caught.
            log.error("GCPOAuth2Mediator failed while generating/injecting the GCP OAuth2 access token", e);
            throw new SynapseException("Error while generating the GCP OAuth2 access token: " + e.getMessage(), e);
        }
    }

    /**
     * Builds the token provider once and caches it (double-checked under the monitor). When a service-account
     * key is present it is reassembled into a {@code byte[]} and streamed into the provider - never held as an
     * immutable {@code String} - and the buffer is wiped as soon as the provider has consumed it. When no key
     * is configured the keyless metadata provider is used instead.
     */
    private synchronized GCPAccessTokenProvider buildProvider() {

        if (tokenProvider != null) {
            return tokenProvider;
        }
        if (StringUtils.isEmpty(serviceAccountKey)) {
            this.tokenProvider = new GCPMetadataTokenProvider(appliedScope());
            if (log.isDebugEnabled()) {
                log.debug("No GCP service-account key configured; using the gateway's attached GCP identity "
                        + "(metadata server) for GCPOAuth2Mediator.");
            }
            return tokenProvider;
        }
        byte[] keyBytes = reassembleServiceAccountKey(serviceAccountKey);
        GCPServiceAccountTokenProvider provider;
        try (InputStream keyStream = new ByteArrayInputStream(keyBytes)) {
            provider = new GCPServiceAccountTokenProvider(keyStream, appliedScope());
        } catch (IllegalArgumentException e) {
            throw new SynapseException("Error while initializing GCP service-account credentials for "
                    + "GCPOAuth2Mediator. Verify the service-account key JSON is valid.", e);
        } catch (IOException e) {
            throw new SynapseException("Error while reading the reassembled GCP service-account key.", e);
        } finally {
            // Wipe the plaintext key bytes as soon as the provider has parsed them.
            Arrays.fill(keyBytes, (byte) 0);
        }
        // The token endpoint can come from the (user-supplied) key JSON, so gate it with the network
        // access-control policy before the signed assertion is ever POSTed there - a hostile or mistyped
        // token_uri must not redirect the assertion to an internal or attacker-chosen host. Cache the
        // provider only after the endpoint has passed the policy.
        validateTokenEndpoint(provider.getTokenUri());
        this.tokenProvider = provider;
        return tokenProvider;
    }

    /**
     * Gates the resolved token endpoint with the network access-control policy
     * ({@link APIUtil#validateRemoteURL(String, String)}). The policy is off unless configured; when enabled it
     * rejects hosts outside the allow-list and (optionally) private / link-local ranges, so a {@code token_uri}
     * taken from a user-supplied key cannot be used to mount an SSRF against the gateway.
     *
     * @param tokenUri the resolved token endpoint the exchange would contact.
     */
    private void validateTokenEndpoint(String tokenUri) {

        try {
            validateRemoteUrl(tokenUri);
        } catch (APIManagementException e) {
            throw new SynapseException("The GCP token endpoint (" + tokenUri + ") is not permitted by the "
                    + "network access-control policy.", e);
        }
    }

    /**
     * Seam over {@link APIUtil#validateRemoteURL(String, String)} - the network access-control policy check for
     * the current tenant. Overridable (and it resolves the tenant itself) so unit tests can drive the mediator
     * without the {@code PrivilegedCarbonContext} / tenant-config / caching runtime the real lookup requires.
     *
     * @param url the token endpoint to validate.
     * @throws APIManagementException if the policy rejects the URL.
     */
    protected void validateRemoteUrl(String url) throws APIManagementException {

        APIUtil.validateRemoteURL(url, GatewayUtils.getTenantDomain());
    }

    /**
     * Reassembles the delivered {@code serviceAccountKey} value into the raw key bytes. The value is the base64
     * of the key JSON: in normal mode a single base64 literal (no {@code '|'}); in secure-vault mode the
     * {@code concat} of {@code wso2:vault-lookup} results, i.e. the base64 split into pipe-separated chunks that
     * synapse rejoins before the mediator runs (base64 never contains {@code '|'}, so it is a safe delimiter).
     * Split on {@code '|'}, concatenate the chunks, then base64-decode.
     */
    private byte[] reassembleServiceAccountKey(String pipeJoinedBase64) {

        // Assemble the base64 as bytes (not an immutable String) so the encoded key can be wiped after decoding.
        ByteArrayOutputStream base64 = new ByteArrayOutputStream();
        for (String chunk : pipeJoinedBase64.split("\\|", -1)) {
            if (chunk.isEmpty()) {
                throw new SynapseException("Empty GCP service-account key chunk "
                        + "(a secure-vault lookup returned empty).");
            }
            byte[] chunkBytes = chunk.getBytes(StandardCharsets.US_ASCII);
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
