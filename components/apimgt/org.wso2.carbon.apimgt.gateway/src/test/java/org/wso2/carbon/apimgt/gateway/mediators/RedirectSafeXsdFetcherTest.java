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

import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Regression tests for the redirect-safe fetch that closes the xsdURL redirect SSRF bypass.
 *
 * <p>Stands up a real loopback HTTP server where an allow-listed "edge" path 302-redirects to a "secret"
 * path the policy blocks, and asserts the secret target is NEVER contacted (the redirect Location is
 * re-validated, not auto-followed). Also covers the resolver path and direct/allowed/blocked/non-http.
 */
public class RedirectSafeXsdFetcherTest {

    private static HttpServer server;
    private static String base;

    // Force the JDK's built-in SchemaFactory (test classpath also has xerces lacking ACCESS_EXTERNAL_*).
    private static final String SCHEMA_FACTORY_KEY =
            "javax.xml.validation.SchemaFactory:" + W3C_XML_SCHEMA_NS_URI;
    private static final String JDK_SCHEMA_FACTORY =
            "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory";

    /** Paths the HTTP server actually served (thread-safe). */
    private static final List<String> hits = Collections.synchronizedList(new ArrayList<>());

    private static final String CLEAN_XSD =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                    + "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
                    + "<xsd:element name=\"note\" type=\"xsd:string\"/></xsd:schema>";

    /** Policy that blocks any URL containing "secret"; everything else is allowed. */
    private static final RemoteUrlValidator BLOCK_SECRET = url -> {
        if (url.contains("secret")) {
            throw new APIManagementException("blocked by policy: " + url);
        }
    };

    /** Policy that allows everything (for the payload-cannot-fetch test). */
    private static final RemoteUrlValidator ALLOW_ALL = url -> { };

    @BeforeClass
    public static void startServer() throws IOException {
        System.setProperty(SCHEMA_FACTORY_KEY, JDK_SCHEMA_FACTORY);
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int port = server.getAddress().getPort();
        base = "http://127.0.0.1:" + port;
        server.createContext("/", exchange -> {
            String path = exchange.getRequestURI().getPath();
            hits.add(path);
            if ("/redirect-to-internal.xsd".equals(path)) {
                exchange.getResponseHeaders().set("Location", base + "/secret.xsd");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }
            if ("/edge-to-allowed.xsd".equals(path)) {
                exchange.getResponseHeaders().set("Location", base + "/allowed.xsd");
                exchange.sendResponseHeaders(302, -1);
                exchange.close();
                return;
            }
            byte[] body = CLEAN_XSD.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/xml");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });
        server.start();
    }

    @AfterClass
    public static void stopServer() {
        if (server != null) {
            server.stop(0);
        }
        System.clearProperty(SCHEMA_FACTORY_KEY);
    }

    private static String read(RedirectSafeXsdFetcher.Result r) throws IOException {
        return new String(r.body, StandardCharsets.UTF_8);
    }

    @Test
    public void testDirectAllowedFetchReturnsBytes() throws Exception {
        hits.clear();
        RedirectSafeXsdFetcher.Result r = RedirectSafeXsdFetcher.fetch(base + "/allowed.xsd", BLOCK_SECRET);
        assertTrue("expected the clean XSD body", read(r).contains("xsd:schema"));
        assertEquals(base + "/allowed.xsd", r.finalUrl);
        assertTrue(hits.contains("/allowed.xsd"));
    }

    @Test
    public void testDirectBlockedThrowsAndIsNeverFetched() {
        hits.clear();
        try {
            RedirectSafeXsdFetcher.fetch(base + "/secret.xsd", BLOCK_SECRET);
            fail("expected XsdRefBlockedException");
        } catch (XsdRefBlockedException expected) {
        } catch (IOException e) {
            fail("expected a block, got IOException: " + e);
        }
        assertFalse("blocked URL must never be fetched", hits.contains("/secret.xsd"));
    }

    @Test(expected = XsdRefBlockedException.class)
    public void testNonHttpSchemeThrows() throws Exception {
        RedirectSafeXsdFetcher.fetch("file:///etc/passwd", BLOCK_SECRET);
    }

    @Test
    public void testRedirectToBlockedHostIsRefusedAndNeverContacted() {
        hits.clear();
        try {
            RedirectSafeXsdFetcher.fetch(base + "/redirect-to-internal.xsd", BLOCK_SECRET);
            fail("expected the redirect to the blocked host to be refused");
        } catch (XsdRefBlockedException expected) {
            // 302 Location re-validated, rejected
        } catch (IOException e) {
            fail("expected a block, got IOException: " + e);
        }
        assertTrue("the allow-listed edge is fetched", hits.contains("/redirect-to-internal.xsd"));
        assertFalse("the redirect target must NEVER be contacted (this is the bypass)",
                hits.contains("/secret.xsd"));
    }

    @Test
    public void testRedirectToAllowedHostIsFollowed() throws Exception {
        hits.clear();
        RedirectSafeXsdFetcher.Result r =
                RedirectSafeXsdFetcher.fetch(base + "/edge-to-allowed.xsd", BLOCK_SECRET);
        assertEquals(base + "/allowed.xsd", r.finalUrl);
        assertTrue(read(r).contains("xsd:schema"));
        assertTrue(hits.contains("/edge-to-allowed.xsd"));
        assertTrue(hits.contains("/allowed.xsd"));
    }

    @Test
    public void testResolverRefusesRedirectToBlocked() {
        hits.clear();
        AccessControlledXmlResolver resolver = new AccessControlledXmlResolver(BLOCK_SECRET);
        try {
            resolver.resolveResource(W3C_XML_SCHEMA_NS_URI, null, null, base + "/redirect-to-internal.xsd", null);
            fail("expected XsdRefBlockedException");
        } catch (XsdRefBlockedException expected) {
        }
        assertFalse("the redirect target must NEVER be contacted via the resolver either",
                hits.contains("/secret.xsd"));
    }

    @Test
    public void testResolverReturnsFetchedBytesForAllowed() throws Exception {
        hits.clear();
        AccessControlledXmlResolver resolver = new AccessControlledXmlResolver(BLOCK_SECRET);
        org.w3c.dom.ls.LSInput input =
                resolver.resolveResource(W3C_XML_SCHEMA_NS_URI, null, null, base + "/allowed.xsd", null);
        assertNotNull("a permitted reference must be returned as a populated LSInput", input);
        try (InputStream in = input.getByteStream()) {
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(content.contains("xsd:schema"));
        }
        assertEquals(base + "/allowed.xsd", input.getSystemId());
        assertTrue(hits.contains("/allowed.xsd"));
    }

    private static BufferedInputStream payload(String xml) {
        return new BufferedInputStream(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void testValidateXsdAndPayloadRefusesRedirectToBlocked() {
        hits.clear();
        try {
            XMLSchemaValidator.validateXsdAndPayload(
                    base + "/redirect-to-internal.xsd", BLOCK_SECRET, payload("<note>hi</note>"));
            fail("expected APIMThreatAnalyzerException for the redirect to a blocked host");
        } catch (APIMThreatAnalyzerException expected) {
        }
        assertFalse("the redirect target must never be contacted via validateSchema either",
                hits.contains("/secret.xsd"));
    }

    @Test
    public void testValidateXsdAndPayloadAllowsCleanSchemaAndValidatesPayload() throws Exception {
        hits.clear();
        boolean ok = XMLSchemaValidator.validateXsdAndPayload(
                base + "/allowed.xsd", BLOCK_SECRET, payload("<note>hi</note>"));
        assertTrue("a permitted xsdURL with a conforming payload should validate", ok);
        assertTrue(hits.contains("/allowed.xsd"));
    }

    @Test
    public void testPayloadCannotFetchAnExternalDtd() {
        hits.clear();
        // step (C) disables external resolution: DTD must never be fetched
        String evilPayload = "<?xml version=\"1.0\"?>"
                + "<!DOCTYPE note SYSTEM \"" + base + "/payload-evil.dtd\"><note>hi</note>";
        try {
            XMLSchemaValidator.validateXsdAndPayload(base + "/allowed.xsd", ALLOW_ALL, payload(evilPayload));
        } catch (APIMThreatAnalyzerException ignored) {
        }
        assertFalse("the payload's external DTD must never be fetched (step C)",
                hits.contains("/payload-evil.dtd"));
    }
}
