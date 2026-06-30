/*
 *   Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class OASArchiveRefTest {
    @Test
    public void testArchiveBlockedRefNotFetched() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/internal.yaml", ex -> { hits.incrementAndGet();
            byte[] b = "type: object".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length); ex.getResponseBody().write(b); ex.close(); });
        server.start();
        int port = server.getAddress().getPort();
        File zip = File.createTempFile("ssrf-arch", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            zos.putNextEntry(new ZipEntry("api/swagger.yaml"));
            String master = "openapi: 3.0.0\ninfo: {title: t, version: '1.0'}\npaths:\n  /a:\n    get:\n" +
                "      responses:\n        '200':\n          description: ok\n          content:\n" +
                "            application/json:\n              schema:\n" +
                "                $ref: 'http://127.0.0.1:" + port + "/internal.yaml'\n";
            zos.write(master.getBytes(StandardCharsets.UTF_8)); zos.closeEntry();
        }
        try {
            OASParserOptions opts = new OASParserOptions();
            opts.setRefValidationTenantDomain("carbon.super");
            opts.setRefValidator((url, t) -> {
                if (url.contains("127.0.0.1")) {
                    throw new APIManagementException("blocked " + url, ExceptionCodes.UNTRUSTED_URL);
                }
            });
            try (FileInputStream fis = new FileInputStream(zip)) {
                OASParserUtil.extractAndValidateOpenAPIArchive(fis, false, opts);
                Assert.fail("expected UNTRUSTED_URL from Layer-1 ref validation on the blocked archive ref");
            } catch (APIManagementException e) {
                Assert.assertEquals(ExceptionCodes.UNTRUSTED_URL.getErrorCode(),
                        e.getErrorHandler().getErrorCode());
            }
            Assert.assertEquals(0, hits.get());
        } finally {
            server.stop(0);
            zip.delete();
        }
    }

    @Test
    public void remoteRefInBundledLocalFileIsBlocked() throws Exception {
        // remote $ref nested in a bundled local sibling
        AtomicInteger hits = new AtomicInteger();
        com.sun.net.httpserver.HttpServer server =
                com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/secret", ex -> { hits.incrementAndGet();
            byte[] b = "type: object".getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(200, b.length); ex.getResponseBody().write(b); ex.close(); });
        server.start();
        int port = server.getAddress().getPort();
        File zip = File.createTempFile("ssrf-arch-nested", ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip))) {
            zos.putNextEntry(new ZipEntry("api/swagger.yaml"));
            String master = "openapi: 3.0.0\ninfo: {title: t, version: '1.0'}\npaths:\n  /a:\n    get:\n" +
                "      responses:\n        '200':\n          description: ok\n          content:\n" +
                "            application/json:\n              schema:\n" +
                "                $ref: './internal.yaml'\n";
            zos.write(master.getBytes(StandardCharsets.UTF_8)); zos.closeEntry();
            zos.putNextEntry(new ZipEntry("api/internal.yaml"));
            String internal = "type: object\nproperties:\n  x:\n    $ref: 'http://127.0.0.1:" + port + "/secret'\n";
            zos.write(internal.getBytes(StandardCharsets.UTF_8)); zos.closeEntry();
        }
        try {
            OASParserOptions opts = new OASParserOptions();
            opts.setRefValidationTenantDomain("carbon.super");
            opts.setRefValidator((url, t) -> {
                if (url.contains("127.0.0.1")) {
                    throw new APIManagementException("blocked " + url, ExceptionCodes.UNTRUSTED_URL);
                }
            });
            try (FileInputStream fis = new FileInputStream(zip)) {
                OASParserUtil.extractAndValidateOpenAPIArchive(fis, false, opts);
                Assert.fail("expected UNTRUSTED_URL from the remote $ref inside the bundled local file");
            } catch (APIManagementException e) {
                Assert.assertEquals(ExceptionCodes.UNTRUSTED_URL.getErrorCode(),
                        e.getErrorHandler().getErrorCode());
            }
            Assert.assertEquals(0, hits.get());
        } finally {
            server.stop(0);
            zip.delete();
        }
    }
}
