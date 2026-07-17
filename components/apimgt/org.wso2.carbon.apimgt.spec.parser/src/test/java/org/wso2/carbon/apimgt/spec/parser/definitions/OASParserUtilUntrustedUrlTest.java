/*
 *   Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *   WSO2 LLC. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Locks the exact swagger-parser message substrings that {@link OASParserUtil#isUntrustedUrlInDefinition} relies on
 * to recognise a remote {@code $ref} that the safe URL resolver refused under the network access control policy. A
 * swagger-parser bump that reworded any of these messages would silently break the mapping (a blocked ref would no
 * longer be classified as untrusted-in-definition); these tests fail loudly if that happens.
 */
public class OASParserUtilUntrustedUrlTest {

    /**
     * The seven message fragments emitted by the swagger-parser safe URL resolver when it refuses a remote ref.
     * Kept in lock-step with {@link OASParserUtil#isUntrustedUrlInDefinition}.
     */
    private static final String[] UNTRUSTED_URL_FRAGMENTS = {
            "is restricted. URL [",
            "is part of the explicit denylist",
            "does not use a supported protocol. URL [",
            "Failed to resolve IP from hostname. Hostname [",
            "Failed to get hostname from URL. URL [",
            "Failed to create new URL with IP.",
            "Failed to parse URL. URL ["
    };

    private HttpServer server;
    private int serverPort;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    @Before
    public void startServer() throws IOException {
        requestCount.set(0);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // Serve a valid schema fragment for ANY path so that, if the ref were NOT blocked, resolution would succeed
        // (and the block-detection test would fail clearly rather than through an unrelated connection error).
        server.createContext("/", exchange -> {
            requestCount.incrementAndGet();
            byte[] body = ("{\"type\":\"object\",\"properties\":{\"name\":{\"type\":\"string\"}}}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
        serverPort = server.getAddress().getPort();
    }

    @After
    public void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    /**
     * Each of the seven locked fragments, wrapped in realistic surrounding text, must be classified as an
     * untrusted-URL-in-definition signal.
     */
    @Test
    public void testEachFragmentIsClassifiedAsUntrusted() {
        for (String fragment : UNTRUSTED_URL_FRAGMENTS) {
            String message = "Resolution failed: the referenced host " + fragment + " http://example.com/ref.yaml]";
            Assert.assertTrue("Fragment must be recognised as an untrusted-URL-in-definition signal: " + fragment,
                    OASParserUtil.isUntrustedUrlInDefinition(message));
        }
    }

    /**
     * A null message and an unrelated parser message must NOT be classified as untrusted-URL-in-definition signals.
     */
    @Test
    public void testUnrelatedMessagesAreNotClassifiedAsUntrusted() {
        Assert.assertFalse("A null message must not be classified as untrusted",
                OASParserUtil.isUntrustedUrlInDefinition(null));
        Assert.assertFalse("An unrelated parser message must not be classified as untrusted",
                OASParserUtil.isUntrustedUrlInDefinition("attribute swagger or openapi should present"));
    }

    /**
     * Drives the real pinned swagger-parser against an OpenAPI 2.0 definition whose remote {@code $ref} targets a
     * blocked loopback host. The only way this surfaces as {@link ExceptionCodes#UNTRUSTED_URL_IN_DEFINITION} is if
     * the library's block message still matches one of the locked fragments - so a parser bump that reworded the
     * message breaks this test, and no HTTP request must reach the loopback server.
     */
    @Test
    public void testRealParserBlockedRemoteRefMapsToUntrusted() throws Exception {
        String swagger20 =
                "swagger: \"2.0\"\n"
                + "info:\n"
                + "  title: Blocked Remote Ref API\n"
                + "  version: 1.0.0\n"
                + "paths:\n"
                + "  /pets:\n"
                + "    get:\n"
                + "      responses:\n"
                + "        '200':\n"
                + "          description: OK\n"
                + "          schema:\n"
                + "            $ref: 'http://127.0.0.1:" + serverPort + "/fragment.yaml'\n";

        OASParserOptions options = new OASParserOptions();
        options.setNetworkAccessControlEnabled(true);
        options.setRemoteRefBlockList(Arrays.asList("127.0.0.1"));

        try {
            new OAS2Parser().validateAPIDefinition(swagger20, false, options);
            Assert.fail("An OAS 2.0 definition carrying a blocked remote $ref must be rejected");
        } catch (APIManagementException e) {
            Assert.assertNotNull("The rejection must carry an error handler", e.getErrorHandler());
            Assert.assertEquals("A blocked remote $ref must surface as UNTRUSTED_URL_IN_DEFINITION",
                    ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), e.getErrorHandler().getErrorCode());
        }

        Assert.assertEquals("A blocked remote $ref must NOT be fetched (zero HTTP requests expected)",
                0, requestCount.get());
    }
}
