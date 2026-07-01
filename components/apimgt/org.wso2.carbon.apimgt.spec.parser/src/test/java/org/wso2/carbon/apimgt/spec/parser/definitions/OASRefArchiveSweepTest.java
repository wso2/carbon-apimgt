/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.sun.net.httpserver.HttpServer;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.OASParserOptions;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Executors;

import static org.junit.Assert.fail;

public class OASRefArchiveSweepTest {

    private HttpServer server;
    private Path root;

    @Before public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        root = Files.createTempDirectory("ref-arc");
    }
    @After public void tearDown() throws Exception {
        server.stop(0);
        Files.walk(root).sorted((a, b) -> b.compareTo(a)).forEach(p -> p.toFile().delete());
    }

    private OASParserOptions opts() {
        OASParserOptions o = new OASParserOptions();
        o.setRefValidationTenantDomain("carbon.super");
        o.setRefValidator((url, tenant) -> {
            if (url.contains("blocked.invalid")) {
                throw new APIManagementException("blocked: " + url);
            }
        });
        o.setHttpClientProvider((protocol, port) -> HttpClients.custom().disableRedirectHandling().build());
        return o;
    }

    @Test
    public void remoteRefHiddenInBundledLocalFileIsCaught() throws Exception {
        Files.write(root.resolve("swagger.yaml"),
            "openapi: 3.0.1\ncomponents:\n  schemas:\n    A: { $ref: './internal.yaml' }\n"
                    .getBytes(StandardCharsets.UTF_8));
        Files.write(root.resolve("internal.yaml"),
            "openapi: 3.0.1\ncomponents:\n  schemas:\n    B: { $ref: 'http://blocked.invalid/x.yaml' }\n"
                    .getBytes(StandardCharsets.UTF_8));
        try {
            OASParserUtil.validateArchiveRemoteRefs(root.toFile(), opts());
            fail("expected block on remote ref inside a bundled local file");
        } catch (APIManagementException expected) {
        }
    }

    @Test
    public void cleanArchivePasses() throws Exception {
        Files.write(root.resolve("swagger.yaml"),
            "openapi: 3.0.1\ncomponents:\n  schemas:\n    A: { $ref: './internal.yaml' }\n"
                    .getBytes(StandardCharsets.UTF_8));
        Files.write(root.resolve("internal.yaml"),
            "openapi: 3.0.1\ncomponents: {}\n".getBytes(StandardCharsets.UTF_8));
        OASParserUtil.validateArchiveRemoteRefs(root.toFile(), opts());
    }

    @Test
    public void remoteRefInNonStandardExtensionFileIsCaught() throws Exception {
        // local $ref resolves regardless of extension
        Files.write(root.resolve("swagger.yaml"),
            "openapi: 3.0.1\ncomponents:\n  schemas:\n    A: { $ref: './internal.def' }\n"
                    .getBytes(StandardCharsets.UTF_8));
        Files.write(root.resolve("internal.def"),
            "openapi: 3.0.1\ncomponents:\n  schemas:\n    B: { $ref: 'http://blocked.invalid/x.yaml' }\n"
                    .getBytes(StandardCharsets.UTF_8));
        try {
            OASParserUtil.validateArchiveRemoteRefs(root.toFile(), opts());
            fail("expected block on remote ref inside a non-standard-extension bundled file");
        } catch (APIManagementException expected) {
        }
    }
}
