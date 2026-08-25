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

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Base for GCP OAuth2 access-token providers: owns the caching, proactive refresh and thread-safety, and
 * delegates only the "obtain a fresh token" step to the concrete credential source.
 * <p>
 * Two sources exist:
 * <ul>
 *     <li>{@link GCPServiceAccountTokenProvider} - mints a token from an uploaded service-account key via the
 *     JWT-bearer assertion grant.</li>
 *     <li>{@link GCPMetadataTokenProvider} - fetches a token for the gateway's attached GCP identity
 *     (Application Default Credentials / Workload Identity) from the GCP metadata server, no key required.</li>
 * </ul>
 * The cached token is refreshed shortly before expiry, so an in-flight request never carries an
 * about-to-expire token.
 */
public abstract class GCPAccessTokenProvider {

    protected static final int CONNECT_TIMEOUT_MS = 10000;
    protected static final int READ_TIMEOUT_MS = 10000;
    // Used only if a token response omits expires_in (GCP tokens live ~1 hour).
    private static final long DEFAULT_TOKEN_LIFETIME_SECONDS = 3600L;
    // Refresh a few minutes before expiry so an in-flight request never carries an about-to-expire token.
    private static final long REFRESH_SKEW_SECONDS = 300L;

    // Immutable token + expiry snapshot published via a single volatile reference, so the lock-free fast
    // path reads a consistent pair (never a token paired with a mismatched expiry).
    private volatile CachedToken cached;

    /**
     * Returns a valid access token, obtaining a fresh one only when the cached token is missing or near expiry.
     * <p>
     * The still-valid case is served on a lock-free fast path (a single volatile read), so cache hits never
     * contend on the lock even under high concurrency; only an actual refresh takes the lock.
     *
     * @return a valid OAuth2 access token.
     * @throws IOException if obtaining a fresh token fails.
     */
    public String getAccessToken() throws IOException {

        long now = System.currentTimeMillis() / 1000L;
        CachedToken current = cached;
        if (current != null && now < (current.expiryEpochSeconds - REFRESH_SKEW_SECONDS)) {
            return current.token;
        }
        return refreshToken(now);
    }

    /**
     * Refreshes the cached token under the lock. Re-checks first so that if another thread refreshed while
     * this one was waiting for the lock, the token source is not contacted again.
     */
    private synchronized String refreshToken(long now) throws IOException {

        CachedToken current = cached;
        if (current != null && now < (current.expiryEpochSeconds - REFRESH_SKEW_SECONDS)) {
            return current.token;
        }
        JSONObject tokenResponse = fetchToken();
        String accessToken = tokenResponse.optString("access_token", null);
        if (StringUtils.isEmpty(accessToken)) {
            throw new IOException("The GCP token response did not contain an access_token.");
        }
        long expiresIn = tokenResponse.optLong("expires_in", DEFAULT_TOKEN_LIFETIME_SECONDS);
        this.cached = new CachedToken(accessToken, now + expiresIn);
        return accessToken;
    }

    /**
     * Obtains a fresh raw token response ({@code access_token} + {@code expires_in}) from the credential source.
     *
     * @return the token endpoint / metadata-server JSON response.
     * @throws IOException if the request fails.
     */
    protected abstract JSONObject fetchToken() throws IOException;

    protected static String readAll(InputStream stream) throws IOException {

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[1024];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    /**
     * Immutable snapshot of the cached token and its expiry, published atomically via a single volatile
     * reference so a reader never sees a token paired with a mismatched expiry.
     */
    private static final class CachedToken {

        private final String token;
        private final long expiryEpochSeconds;

        CachedToken(String token, long expiryEpochSeconds) {

            this.token = token;
            this.expiryEpochSeconds = expiryEpochSeconds;
        }
    }
}
