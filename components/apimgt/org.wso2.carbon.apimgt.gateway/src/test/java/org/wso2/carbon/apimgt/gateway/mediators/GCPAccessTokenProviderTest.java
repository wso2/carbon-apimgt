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

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit tests for the caching / proactive-refresh contract in {@link GCPAccessTokenProvider}, which both the
 * service-account and metadata token providers inherit.
 * <p>
 * A tiny test subclass supplies a canned {@link GCPAccessTokenProvider#fetchToken()} and counts invocations,
 * so the cache/refresh behaviour is exercised without any network access.
 */
public class GCPAccessTokenProviderTest {

    /**
     * Test double: returns a caller-controlled token response and records how many times the (network) fetch
     * was actually performed.
     */
    private static class StubTokenProvider extends GCPAccessTokenProvider {

        private final AtomicInteger fetchCount = new AtomicInteger(0);
        private final long expiresIn;
        private final boolean includeExpiresIn;
        private final boolean includeAccessToken;
        private IOException toThrow;
        private long fetchDelayMillis;

        StubTokenProvider(long expiresIn, boolean includeExpiresIn, boolean includeAccessToken) {
            this.expiresIn = expiresIn;
            this.includeExpiresIn = includeExpiresIn;
            this.includeAccessToken = includeAccessToken;
        }

        @Override
        protected JSONObject fetchToken() throws IOException {
            if (toThrow != null) {
                throw toThrow;
            }
            if (fetchDelayMillis > 0) {
                try {
                    Thread.sleep(fetchDelayMillis);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException(e);
                }
            }
            int n = fetchCount.incrementAndGet();
            JSONObject response = new JSONObject();
            if (includeAccessToken) {
                response.put("access_token", "token-" + n);
            }
            if (includeExpiresIn) {
                response.put("expires_in", expiresIn);
            }
            return response;
        }

        int fetchCount() {
            return fetchCount.get();
        }
    }

    @Test
    public void testTokenIsCachedWithinItsLifetime() throws Exception {

        StubTokenProvider provider = new StubTokenProvider(3600L, true, true);

        String first = provider.getAccessToken();
        String second = provider.getAccessToken();

        Assert.assertEquals("token-1", first);
        Assert.assertEquals("A cached token must be returned without re-fetching", "token-1", second);
        Assert.assertEquals("A valid cached token must be fetched only once", 1, provider.fetchCount());
    }

    @Test
    public void testTokenIsRefreshedWhenWithinExpirySkew() throws Exception {

        // expires_in (100s) is below the 300s refresh skew, so the cached token is always considered
        // about-to-expire and every call must re-fetch.
        StubTokenProvider provider = new StubTokenProvider(100L, true, true);

        String first = provider.getAccessToken();
        String second = provider.getAccessToken();

        Assert.assertEquals("token-1", first);
        Assert.assertEquals("A token inside the refresh skew must be re-fetched", "token-2", second);
        Assert.assertEquals(2, provider.fetchCount());
    }

    @Test
    public void testExpiryDefaultsWhenResponseOmitsExpiresIn() throws Exception {

        // No expires_in -> falls back to the 1-hour default, so the token is cached (not re-fetched).
        StubTokenProvider provider = new StubTokenProvider(0L, false, true);

        provider.getAccessToken();
        provider.getAccessToken();

        Assert.assertEquals("Missing expires_in must default to a cacheable lifetime", 1, provider.fetchCount());
    }

    @Test
    public void testMissingAccessTokenIsRejected() {

        StubTokenProvider provider = new StubTokenProvider(3600L, true, false);

        try {
            provider.getAccessToken();
            Assert.fail("Expected an IOException when the response has no access_token");
        } catch (IOException e) {
            Assert.assertTrue("Message should mention the missing access_token: " + e.getMessage(),
                    e.getMessage().contains("access_token"));
        }
    }

    @Test
    public void testFetchFailurePropagates() {

        StubTokenProvider provider = new StubTokenProvider(3600L, true, true);
        provider.toThrow = new IOException("token endpoint unreachable");

        try {
            provider.getAccessToken();
            Assert.fail("Expected the fetch IOException to propagate");
        } catch (IOException e) {
            Assert.assertEquals("token endpoint unreachable", e.getMessage());
        }
    }

    @Test
    public void testConcurrentCallsMintTheTokenOnce() throws Exception {

        // The refresh is synchronized and re-checks the cache after taking the lock: with many threads racing
        // for the first (still-uncached) token, exactly one fetch must happen and every caller must observe the
        // same cached value. A fetch delay makes the threads pile up on the lock so the race is real.
        StubTokenProvider provider = new StubTokenProvider(3600L, true, true);
        provider.fetchDelayMillis = 50;

        int threadCount = 20;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        Set<String> tokens = new HashSet<>();
        try {
            Future<String>[] futures = new Future[threadCount];
            for (int i = 0; i < threadCount; i++) {
                futures[i] = pool.submit((Callable<String>) () -> {
                    startGate.await();
                    return provider.getAccessToken();
                });
            }
            startGate.countDown();
            for (Future<String> future : futures) {
                tokens.add(future.get(10, TimeUnit.SECONDS));
            }
        } finally {
            pool.shutdownNow();
        }

        Assert.assertEquals("The token must be minted exactly once under concurrency",
                1, provider.fetchCount());
        Assert.assertEquals("Every thread must observe the same cached token", 1, tokens.size());
    }
}
