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

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class OASRefCrawlTest {

    private HttpServer server;
    private String base;                 // http://127.0.0.1:<port>
    private final ConcurrentHashMap<String, Integer> hits = new ConcurrentHashMap<>();

    @Before
    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        base = "http://127.0.0.1:" + server.getAddress().getPort();
    }

    @After
    public void stop() {
        server.stop(0);
    }

    private void serve(String path, int status, String body, String location) {
        server.createContext(path, ex -> {
            hits.merge(path, 1, Integer::sum);
            if (location != null) {
                ex.getResponseHeaders().add("Location", location);
            }
            byte[] b = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
            ex.sendResponseHeaders(status, b.length == 0 ? -1 : b.length);
            if (b.length > 0) {
                try (OutputStream os = ex.getResponseBody()) {
                    os.write(b);
                }
            }
            ex.close();
        });
    }

    /** Options whose validator throws UNTRUSTED_URL when allow.test(url) is false; provider returns a redirect-disabled client. */
    private OASParserOptions opts(Predicate<String> allow) {
        OASParserOptions o = new OASParserOptions();
        o.setRefValidationTenantDomain("carbon.super");
        o.setRefValidator((url, tenant) -> {
            if (!allow.test(url)) {
                throw new APIManagementException("blocked: " + url);
            }
        });
        o.setHttpClientProvider((protocol, port) -> HttpClients.custom().disableRedirectHandling().build());
        return o;
    }

    private String refDoc(String refUrl) {
        return "openapi: 3.0.1\ncomponents:\n  schemas:\n    X: { $ref: '" + refUrl + "' }\n";
    }

    @Test
    public void allowedTransitiveChainIsFetched() throws Exception {
        serve("/a.yaml", 200, refDoc(base + "/b.yaml"), null);
        serve("/b.yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
        OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/a.yaml"), null, opts(u -> true));
        assertEquals(Integer.valueOf(1), hits.get("/a.yaml"));
        assertEquals(Integer.valueOf(1), hits.get("/b.yaml"));
    }

    @Test
    public void transitiveBlockedRefThrowsAndIsNeverFetched() {
        serve("/a.yaml", 200, refDoc("http://blocked.invalid/evil.yaml"), null);
        try {
            OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/a.yaml"), null,
                    opts(u -> !u.contains("blocked.invalid")));
            fail("expected APIManagementException");
        } catch (APIManagementException expected) {
            assertEquals(Integer.valueOf(1), hits.get("/a.yaml"));
        }
    }

    @Test
    public void relativeRefUnderRemoteRootIsResolvedAndValidated() {
        serve("/dir/main.yaml", 200, refDoc("./inner.yaml"), null);     // relative ref
        serve("/dir/inner.yaml", 200, refDoc("http://blocked.invalid/x"), null);
        try {
            OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/dir/main.yaml"), null,
                    opts(u -> !u.contains("blocked.invalid")));
            fail("expected block on transitive ref reached via a relative ref");
        } catch (APIManagementException expected) {
            assertEquals(Integer.valueOf(1), hits.get("/dir/main.yaml"));
            assertEquals(Integer.valueOf(1), hits.get("/dir/inner.yaml"));
        }
    }

    @Test
    public void cycleTerminates() throws Exception {
        serve("/a.yaml", 200, refDoc(base + "/b.yaml"), null);
        serve("/b.yaml", 200, refDoc(base + "/a.yaml"), null);     // cycle
        OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/a.yaml"), null, opts(u -> true));
        assertEquals(Integer.valueOf(1), hits.get("/a.yaml"));
        assertEquals(Integer.valueOf(1), hits.get("/b.yaml"));
    }

    @Test
    public void redirectToBlockedHostIsReValidatedAndThrows() {
        serve("/r.yaml", 302, null, "http://blocked.invalid/x.yaml");
        try {
            OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/r.yaml"), null,
                    opts(u -> !u.contains("blocked.invalid")));
            fail("expected block on redirect target");
        } catch (APIManagementException expected) {
            assertEquals(Integer.valueOf(1), hits.get("/r.yaml"));
        }
    }

    @Test
    public void caseDistinctPathsAreBothCrawled() throws Exception {
        // case-sensitive paths must not be deduped (under-validation)
        serve("/A.yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
        serve("/a.yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
        String root = "openapi: 3.0.1\ncomponents:\n  schemas:\n"
                + "    U: { $ref: '" + base + "/A.yaml' }\n"
                + "    L: { $ref: '" + base + "/a.yaml' }\n";
        OASParserUtil.validateRemoteRefsRecursively(root, null, opts(u -> true));
        assertEquals("upper-case path must be fetched", Integer.valueOf(1), hits.get("/A.yaml"));
        assertEquals("lower-case path must be fetched", Integer.valueOf(1), hits.get("/a.yaml"));
    }

    @Test
    public void hostIsDedupedCaseInsensitively() throws Exception {
        // scheme+host case-folded, so case-different host dedups to one fetch
        serve("/x.yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
        int port = server.getAddress().getPort();
        String root = "openapi: 3.0.1\ncomponents:\n  schemas:\n"
                + "    A: { $ref: 'http://127.0.0.1:" + port + "/x.yaml' }\n"
                + "    B: { $ref: 'HTTP://127.0.0.1:" + port + "/x.yaml' }\n";
        OASParserUtil.validateRemoteRefsRecursively(root, null, opts(u -> true));
        assertEquals("host/scheme case variants must dedup to one fetch", Integer.valueOf(1), hits.get("/x.yaml"));
    }

    @Test
    public void wideFanOutIsFullyCrawled() throws Exception {
        // no total-ref cap (matching swagger-parser)
        StringBuilder sb = new StringBuilder("openapi: 3.0.1\ncomponents:\n  schemas:\n");
        for (int i = 0; i < 150; i++) {
            serve("/n" + i + ".yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
            sb.append("    S").append(i).append(": { $ref: '").append(base).append("/n").append(i).append(".yaml' }\n");
        }
        OASParserUtil.validateRemoteRefsRecursively(sb.toString(), null, opts(u -> true));
        for (int i = 0; i < 150; i++) {
            assertEquals("ref n" + i + " must be fetched (no ref-count cap)", Integer.valueOf(1),
                    hits.get("/n" + i + ".yaml"));
        }
    }

    @Test
    public void deepRefChainIsFullyCrawled() throws Exception {
        // no depth cap (matching swagger-parser)
        int depth = 25;
        for (int i = 0; i < depth; i++) {
            serve("/c" + i + ".yaml", 200, refDoc(base + "/c" + (i + 1) + ".yaml"), null);
        }
        serve("/c" + depth + ".yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);   // leaf
        OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/c0.yaml"), null, opts(u -> true));
        assertEquals(Integer.valueOf(1), hits.get("/c0.yaml"));
        assertEquals("a chain deeper than the old depth-10 cap must be fully followed", Integer.valueOf(1),
                hits.get("/c" + depth + ".yaml"));
    }

    @Test
    public void oversizedRefBodyFailsClosed() {
        String big = "openapi: 3.0.1\ncomponents: {}\n# " + new String(new char[4000]).replace('\0', 'x') + "\n";
        serve("/big.yaml", 200, big, null);
        OASParserOptions o = opts(u -> true);
        o.setRefFetchMaxFileSize("0.001");   // ~1048 bytes; body exceeds
        try {
            OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/big.yaml"), null, o);
            fail("expected fail-closed on oversized ref body");
        } catch (APIManagementException expected) {
            assertTrue("message should explain the size limit",
                    expected.getMessage() != null && expected.getMessage().contains("exceeds the size limit"));
            assertEquals("oversized ref is still fetched (blocked during read, not pre-fetch)",
                    Integer.valueOf(1), hits.get("/big.yaml"));
        }
    }

    @Test
    public void refBodyWithinConfiguredCapIsFetched() throws Exception {
        serve("/small.yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
        OASParserOptions o = opts(u -> true);
        o.setRefFetchMaxFileSize("0.001");   // ~1048 bytes; body under cap
        OASParserUtil.validateRemoteRefsRecursively(refDoc(base + "/small.yaml"), null, o);
        assertEquals(Integer.valueOf(1), hits.get("/small.yaml"));
    }

    @Test
    public void redirectTargetIsDedupedAgainstDirectRef() throws Exception {
        serve("/leaf.yaml", 200, "openapi: 3.0.1\ncomponents: {}\n", null);
        serve("/r.yaml", 302, null, base + "/leaf.yaml");
        String root = "openapi: 3.0.1\ncomponents:\n  schemas:\n"
                + "    R: { $ref: '" + base + "/r.yaml' }\n"
                + "    L: { $ref: '" + base + "/leaf.yaml' }\n";
        OASParserUtil.validateRemoteRefsRecursively(root, null, opts(u -> true));
        assertEquals(Integer.valueOf(1), hits.get("/r.yaml"));
        assertEquals("redirect target must not be re-fetched as a direct ref",
                Integer.valueOf(1), hits.get("/leaf.yaml"));
    }
}
