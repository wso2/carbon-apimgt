/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Fetches an XSD document (the top-level {@code xsdURL}, a nested xsd:import/include/redefine, or an
 * external DTD) with HTTP redirect following <b>disabled</b>, re-validating <b>every</b> redirect
 * {@code Location} through the network access-control policy before following it.
 *
 * <p>This closes a redirect to a disallowed host: if the document is fetched by the JDK Xerces loader
 * (via {@code newSchema(new URL(..))} or by returning {@code null} from an
 * {@link org.w3c.dom.ls.LSResourceResolver}), {@code HttpURLConnection} follows {@code 30x} redirects by
 * default, so an allow-listed host that redirects to a link-local/internal address such as
 * {@code 127.0.0.1}/{@code 169.254.169.254} would be
 * fetched without re-validation. Routing every fetch through this class makes the validated URL and the
 * fetched URL the same at every hop. Mirrors the redirect-disabled, re-validating client the OpenAPI
 * {@code $ref} crawl uses ({@code APIUtil.getCrawlHttpClient}), kept self-contained in the gateway module.
 */
final class RedirectSafeXsdFetcher {

    /** Maximum number of redirects to follow; each one is re-validated. */
    static final int MAX_REDIRECTS = 5;
    /** Defensive cap on the fetched document size (XSDs/DTDs are small). */
    static final int MAX_BYTES = 10 * 1024 * 1024;
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 10000;

    private RedirectSafeXsdFetcher() {
    }

    /** The retrieved document bytes and the final URL they were actually retrieved from. */
    static final class Result {
        final byte[] body;
        final String finalUrl;

        Result(byte[] body, String finalUrl) {
            this.body = body;
            this.finalUrl = finalUrl;
        }
    }

    /**
     * Validates and fetches {@code url}, re-validating every redirect {@code Location} before following
     * it. Only http/https are permitted.
     *
     * @throws XsdRefBlockedException (unchecked) if {@code url} or any redirect target is rejected by the
     *                                policy or uses a non-http(s) scheme — the caller must fail closed.
     * @throws IOException            on a network/protocol error (no Location, bad status, too many hops,
     *                                oversized body).
     */
    static Result fetch(String url, RemoteUrlValidator policy) throws IOException {
        String current = url;
        for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
            assertAllowed(current, policy);                 // re-validate before every hop
            HttpURLConnection conn = (HttpURLConnection) new URL(current).openConnection();
            conn.setInstanceFollowRedirects(false);         // never auto-follow a redirect
            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
            conn.setReadTimeout(READ_TIMEOUT_MS);
            conn.setRequestMethod("GET");
            try {
                int code = conn.getResponseCode();
                if (code >= 300 && code < 400) {
                    String location = conn.getHeaderField("Location");
                    if (location == null) {
                        throw new IOException("Redirect (" + code + ") with no Location header from " + current);
                    }
                    current = resolveLocation(current, location);
                    continue;
                }
                if (code != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Unexpected HTTP status " + code + " fetching " + current);
                }
                try (InputStream in = conn.getInputStream()) {
                    return new Result(readBounded(in), current);
                }
            } finally {
                conn.disconnect();
            }
        }
        throw new IOException("Too many redirects (> " + MAX_REDIRECTS + ") fetching " + url);
    }

    /** Scheme + policy check; throws {@link XsdRefBlockedException} (unchecked) on any violation. */
    private static void assertAllowed(String url, RemoteUrlValidator policy) {
        if (!AccessControlledXmlResolver.isHttpOrHttps(url)) {
            throw new XsdRefBlockedException("Blocked XSD reference with a non-HTTP(S) scheme: " + url);
        }
        try {
            policy.validate(url);
        } catch (APIManagementException e) {
            throw new XsdRefBlockedException(
                    "Blocked XSD reference not permitted by the network access-control policy: " + url, e);
        }
    }

    /** Resolves a possibly-relative redirect {@code Location} against the current absolute URL. */
    private static String resolveLocation(String currentUrl, String location) throws IOException {
        try {
            return new URI(currentUrl).resolve(location.trim()).toString();
        } catch (URISyntaxException | RuntimeException e) {
            throw new IOException("Malformed redirect Location '" + location + "' from " + currentUrl);
        }
    }

    private static byte[] readBounded(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int total = 0;
        int n;
        while ((n = in.read(buf)) != -1) {
            total += n;
            if (total > MAX_BYTES) {
                throw new IOException("XSD document exceeds the " + MAX_BYTES + "-byte limit");
            }
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}
