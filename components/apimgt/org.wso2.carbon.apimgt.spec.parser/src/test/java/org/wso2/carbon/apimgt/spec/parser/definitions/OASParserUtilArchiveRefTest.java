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
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Tests that {@link OASParserUtil#extractAndValidateOpenAPIArchive} routes embedded remote {@code $ref}s in the
 * archive master document through the network access-control safe URL resolver for OAS 3 archives, so a blocked
 * remote reference is never fetched, while local sibling references inside the archive still resolve.
 */
public class OASParserUtilArchiveRefTest {

    private HttpServer server;
    private int serverPort;
    private final AtomicInteger requestCount = new AtomicInteger(0);

    private static final String FRAGMENT_SCHEMA =
            "type: object\n"
            + "properties:\n"
            + "  name:\n"
            + "    type: string\n";

    @Before
    public void startServer() throws IOException {
        requestCount.set(0);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        // Bind the root context so ANY request path to the loopback server is counted.
        server.createContext("/", exchange -> {
            requestCount.incrementAndGet();
            byte[] body = FRAGMENT_SCHEMA.getBytes(StandardCharsets.UTF_8);
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
     * A network access-control policy that mirrors the safe-resolver setup used by the inline/URL OAS3 tests:
     * access control enabled and the loopback host explicitly blocked.
     */
    private OASParserOptions blockLoopbackOptions() {
        OASParserOptions options = new OASParserOptions();
        options.setNetworkAccessControlEnabled(true);
        options.setRemoteRefBlockList(Arrays.asList("127.0.0.1"));
        return options;
    }

    private byte[] buildZip(Map<String, String> entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, String> entry : entries.entrySet()) {
                zos.putNextEntry(new ZipEntry(entry.getKey()));
                zos.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        }
        return baos.toByteArray();
    }

    @Test
    public void testArchiveRemoteRefIsBlockedAndNotFetched() throws Exception {
        String master =
                "openapi: 3.0.0\n"
                + "info:\n"
                + "  title: Archive Remote Ref API\n"
                + "  version: 1.0.0\n"
                + "paths:\n"
                + "  /pets:\n"
                + "    get:\n"
                + "      summary: list\n"
                + "      responses:\n"
                + "        '200':\n"
                + "          description: OK\n"
                + "          content:\n"
                + "            application/json:\n"
                + "              schema:\n"
                + "                $ref: 'http://127.0.0.1:" + serverPort + "/fragment.yaml'\n";

        Map<String, String> entries = new LinkedHashMap<>();
        // Single root folder as required by the extractor.
        entries.put("archive/swagger.yaml", master);
        byte[] zipBytes = buildZip(entries);

        try {
            OASParserUtil.extractAndValidateOpenAPIArchive(
                    new ByteArrayInputStream(zipBytes), false, blockLoopbackOptions());
            Assert.fail("An archive whose master carries a blocked remote $ref must be rejected");
        } catch (APIManagementException e) {
            Assert.assertEquals("A blocked remote $ref must surface as UNTRUSTED_URL_IN_DEFINITION",
                    ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), e.getErrorHandler().getErrorCode());
        }

        Assert.assertEquals("A blocked remote $ref in an archived OpenAPI master must NOT be fetched "
                        + "(zero HTTP requests expected against the loopback server)", 0, requestCount.get());
    }

    @Test
    public void testArchiveLocalSiblingRefStillResolves() throws Exception {
        String master =
                "openapi: 3.0.0\n"
                + "info:\n"
                + "  title: Archive Local Ref API\n"
                + "  version: 1.0.0\n"
                + "paths:\n"
                + "  /pets:\n"
                + "    get:\n"
                + "      summary: list\n"
                + "      responses:\n"
                + "        '200':\n"
                + "          description: OK\n"
                + "          content:\n"
                + "            application/json:\n"
                + "              schema:\n"
                + "                $ref: './definitions.yaml#/Pet'\n";
        String definitions =
                "Pet:\n"
                + "  type: object\n"
                + "  properties:\n"
                + "    id:\n"
                + "      type: integer\n"
                + "    name:\n"
                + "      type: string\n";

        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("archive/swagger.yaml", master);
        entries.put("archive/definitions.yaml", definitions);
        byte[] zipBytes = buildZip(entries);

        APIDefinitionValidationResponse response = OASParserUtil.extractAndValidateOpenAPIArchive(
                new ByteArrayInputStream(zipBytes), false, blockLoopbackOptions());

        Assert.assertEquals("A local sibling $ref inside the archive must resolve from disk without any HTTP fetch",
                0, requestCount.get());
        Assert.assertTrue("A multi-file archive whose master references a local sibling file must still validate",
                response.isValid());
    }
}
