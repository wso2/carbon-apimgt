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

package org.wso2.carbon.apimgt.impl.wsdl;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PolicyGatedSchemaFetcherTest {

    // allowed fetch returns body
    @Test
    public void fetchesAllowedUrl() throws Exception {
        HttpServer s = server(ctx -> respond(ctx, 200, "HELLO"));
        try {
            String body = read(new PolicyGatedSchemaFetcher("carbon.super", (u, t) -> { /* allow all */ })
                    .fetch(base(s) + "/x.xsd"));
            assertEquals("HELLO", body);
        } finally {
            s.stop(0);
        }
    }

    // untrusted host: validator throws -> propagates, host never fetched
    @Test(expected = APIManagementException.class)
    public void blocksUntrustedHost() throws Exception {
        new PolicyGatedSchemaFetcher("carbon.super",
                (u, t) -> { throw new APIManagementException(ExceptionCodes.UNTRUSTED_URL); })
                .fetch("http://169.254.169.254/x.xsd");
    }

    // size cap actually rejects oversized bodies (inject a small cap; do not loosen prod default)
    @Test
    public void rejectsOversizedBody() throws Exception {
        HttpServer s = server(ctx -> respond(ctx, 200, "X".repeat(10_000)));
        try {
            InputStream in = new PolicyGatedSchemaFetcher("carbon.super", (u, t) -> {}, 100L)
                    .fetch(base(s) + "/big.xsd");
            try {
                read(in);
                fail("expected size-limit failure");
            } catch (Exception expected) {
                // SizeLimitedInputStream aborts past the cap
            }
        } finally {
            s.stop(0);
        }
    }

    // slow host: a finite read timeout aborts the fetch quickly instead of blocking indefinitely
    @Test
    public void readTimeoutAbortsSlowFetch() throws Exception {
        HttpServer s = server(ctx -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            respond(ctx, 200, "SLOW");
        });
        try {
            long start = System.nanoTime();
            try {
                new PolicyGatedSchemaFetcher("carbon.super", (u, t) -> { /* allow all */ }, 1_000_000L, 1000, 200)
                        .fetch(base(s) + "/slow.xsd");
                fail("expected read timeout");
            } catch (IOException expected) {
                long elapsedMillis = (System.nanoTime() - start) / 1_000_000L;
                assertTrue("fetch should abort well before the server responds, took " + elapsedMillis + "ms",
                        elapsedMillis < 1500);
            }
        } finally {
            s.stop(0);
        }
    }

    // ---- test helpers ----

    private static HttpServer server(HttpHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", handler);
        server.start();
        return server;
    }

    private static String base(HttpServer s) {
        return "http://127.0.0.1:" + s.getAddress().getPort();
    }

    private static void respond(HttpExchange ctx, int status, String body) {
        try {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            ctx.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = ctx.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            ctx.close();
        }
    }

    private static String read(InputStream in) throws IOException {
        try {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }
}
