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

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * JAXP integration test that empirically answers two questions:
 * 1. Is the LSResourceResolver consulted for nested xsd:import references?
 * 2. Is the LSResourceResolver consulted for external DTDs declared in an XSD?
 *
 * The test starts a real HTTP server on an ephemeral port, exercises
 * SchemaFactory.newSchema() with the production wiring, and inspects both which
 * URLs the resolver saw and which paths the HTTP server actually served.
 */
public class XmlSchemaResolverAccessControlIntegrationTest {

    private static HttpServer server;
    private static int port;

    /** Paths actually fetched by the HTTP server (thread-safe). */
    private static final List<String> serverHits = Collections.synchronizedList(new ArrayList<>());

    /**
     * The JAXP SchemaFactory service-loader key for W3C XML Schema.
     * We force the JDK's built-in Xerces implementation because:
     * (a) the test classpath contains old standalone xerces jars (2.12.0) that preempt
     *     the JDK's impl via ServiceLoader but do NOT support the ACCESS_EXTERNAL_SCHEMA /
     *     ACCESS_EXTERNAL_DTD properties (pre-JAXP 1.5); and
     * (b) the production validateSchema() code sets those properties without catching
     *     SAXNotRecognizedException — it therefore MUST run on the JDK's built-in
     *     com.sun.org.apache.xerces.internal impl (or another JAXP 1.5-capable impl).
     * Pinning to the JDK impl in tests mirrors production reality.
     */
    private static final String SCHEMA_FACTORY_KEY =
            "javax.xml.validation.SchemaFactory:" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
    private static final String JDK_SCHEMA_FACTORY =
            "com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory";

    @BeforeClass
    public static void startServer() throws IOException {
        System.setProperty(SCHEMA_FACTORY_KEY, JDK_SCHEMA_FACTORY);

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        port = server.getAddress().getPort();

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String path = exchange.getRequestURI().getPath();
                serverHits.add(path);

                byte[] body = buildResponse(path);
                int status = (body != null) ? 200 : 404;
                if (body == null) {
                    body = ("Not found: " + path).getBytes(StandardCharsets.UTF_8);
                }
                exchange.getResponseHeaders().set("Content-Type", "application/xml");
                exchange.sendResponseHeaders(status, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            }

            private byte[] buildResponse(String path) {
                String content;
                switch (path) {
                    case "/main.xsd":
                        content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                "            targetNamespace=\"urn:test:main\"\n" +
                                "            xmlns:imp=\"urn:test:imported\">\n" +
                                "  <xsd:import namespace=\"urn:test:imported\"\n" +
                                "              schemaLocation=\"http://127.0.0.1:" + port + "/imported.xsd\"/>\n" +
                                "  <xsd:element name=\"root\" type=\"xsd:string\"/>\n" +
                                "</xsd:schema>\n";
                        break;
                    case "/imported.xsd":
                        content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                "            targetNamespace=\"urn:test:imported\">\n" +
                                "  <xsd:element name=\"imported\" type=\"xsd:string\"/>\n" +
                                "</xsd:schema>\n";
                        break;
                    case "/main-with-dtd.xsd":
                        content = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                "<!DOCTYPE xsd:schema SYSTEM \"http://127.0.0.1:" + port + "/evil.dtd\">\n" +
                                "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n" +
                                "            targetNamespace=\"urn:test:dtd\">\n" +
                                "  <xsd:element name=\"root\" type=\"xsd:string\"/>\n" +
                                "</xsd:schema>\n";
                        break;
                    case "/evil.dtd":
                        content = "<!-- dtd -->";
                        break;
                    default:
                        return null;
                }
                return content.getBytes(StandardCharsets.UTF_8);
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

    private void newSchema(String schemaPath, RemoteUrlValidator policy) throws Exception {
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        sf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "http,https");
        sf.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "http,https");
        sf.setResourceResolver(new AccessControlledXmlResolver(policy));
        sf.newSchema(new URL("http://127.0.0.1:" + port + "/" + schemaPath));
    }

    @Test
    public void testImportResolverConsultedAndFetchedWhenAllowed() throws Exception {
        serverHits.clear();
        List<String> validatedUrls = Collections.synchronizedList(new ArrayList<>());

        RemoteUrlValidator recordOnly = validatedUrls::add;

        newSchema("main.xsd", recordOnly);

        assertTrue(
                "Resolver was not consulted for /imported.xsd. Validated URLs: " + validatedUrls,
                validatedUrls.stream().anyMatch(u -> u.contains("/imported.xsd")));

        assertTrue(
                "Server did not receive a request for /imported.xsd. Server hits: " + serverHits,
                serverHits.contains("/imported.xsd"));
    }

    @Test
    public void testImportBlockedPreventsFetch() throws Exception {
        serverHits.clear();

        RemoteUrlValidator blockImported = url -> {
            if (url.contains("imported.xsd")) {
                throw new APIManagementException("Access-control block: " + url);
            }
        };

        Exception caught = null;
        try {
            newSchema("main.xsd", blockImported);
            fail("newSchema should have thrown when the import was blocked");
        } catch (Exception e) {
            caught = e;
        }

        assertNotNull("newSchema should have thrown", caught);
        assertNotNull(
                "Expected XsdRefBlockedException in the cause chain, but got: " + caught,
                XMLSchemaValidator.unwrapBlockedRef(caught));

        assertFalse(
                "Server fetched /imported.xsd even though the resolver blocked it. Server hits: "
                        + serverHits,
                serverHits.contains("/imported.xsd"));
    }

    // Expected outcome: external DTD must NOT be fetched; if it is, set ACCESS_EXTERNAL_DTD="" to fail closed.
    @Test
    public void testExternalDtdBehavior() throws Exception {
        serverHits.clear();

        RemoteUrlValidator blockDtd = url -> {
            if (url.contains("evil.dtd")) {
                throw new APIManagementException("Access-control block: " + url);
            }
        };

        Exception caught = null;
        try {
            newSchema("main-with-dtd.xsd", blockDtd);
        } catch (Exception e) {
            caught = e;
        }

        assertFalse(
                "ACCESS-CONTROL GAP DETECTED: /evil.dtd was fetched even though the resolver should have " +
                "blocked it. The LSResourceResolver is NOT consulted for external DTDs under " +
                "ACCESS_EXTERNAL_DTD=\"http,https\". Production code must set " +
                "ACCESS_EXTERNAL_DTD=\"\" to fail closed. Server hits: " + serverHits,
                serverHits.contains("/evil.dtd"));

        assertNotNull("newSchema should have thrown for the DTD-bearing XSD", caught);

        assertNotNull(
                "Expected XsdRefBlockedException in the cause chain for the DTD block, but got: "
                        + caught,
                XMLSchemaValidator.unwrapBlockedRef(caught));
    }
}
