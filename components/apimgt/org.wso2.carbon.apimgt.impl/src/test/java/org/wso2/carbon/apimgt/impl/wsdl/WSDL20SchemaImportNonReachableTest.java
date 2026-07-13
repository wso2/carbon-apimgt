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
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

/**
 * Regression + documentation test: proves that WSDL 2.0's nested {@code <xsd:import>} /
 * {@code <xsd:schema schemaLocation=...>} inside {@code <types>} is NOT reachable through
 * {@link WSDL20ProcessorImpl}, unlike the WSDL 1.1 path (covered/gated in
 * {@link WSDL11ProcessorAccessControlIntegrationTest}).
 * <p>
 * Why: {@link WSDL20ProcessorImpl#initPath(String)} (and the other entry points) parse the WSDL
 * file into a DOM {@code Document} and then build Woden's {@code WSDLSource} from the raw DOM
 * element via {@code wsdlSource.setSource(domElement)} -- {@code setBaseURI(...)} is never called
 * (see {@code WSDL20ProcessorImpl#getWSDLSourceFromDocument}). With a {@code null} document base
 * URI, Apache Woden aborts inline-schema parsing (WSDL521, "Missing base URI") before it ever
 * walks the {@code <types>} content to discover a nested {@code xsd:import}/{@code xsd:include}.
 * Consequently, an untrusted nested {@code schemaLocation} is never dereferenced -- no
 * outbound fetch happens, and no {@code AccessControlledUriResolver} gating is even needed for
 * this vector.
 * <p>
 * This is a genuinely different situation to WSDL 1.1: WSDL4J's WSDL 1.1 reader DID walk into
 * inline schemas and fetch nested {@code xsd:import} locations, which is exactly the vector
 * {@link AccessControlledWSDLLocator} was built to gate. For WSDL 2.0 there is nothing to gate
 * for the nested-schema vector because Woden never reaches it; this test locks that in as a
 * regression guard (if a future Woden/library upgrade starts resolving base URIs and reaching the
 * nested import, this test will fail with a nonzero canary-hit count and must be re-evaluated).
 * <p>
 * Absolute {@code wsdl:import}/{@code wsdl:include} elements (a different vector, resolved by
 * Woden's own {@code URIResolver} before any DOM/base-URI concern) ARE reachable and remain gated
 * by {@link AccessControlledUriResolver} -- see {@link WSDL20ProcessorImplResolverTest} -- and are
 * unchanged/out of scope here.
 */
public class WSDL20SchemaImportNonReachableTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static final String XSD_BODY =
            "<xsd:schema targetNamespace=\"urn:c\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/>";

    // initPath() reads size-limit config via ServiceReferenceHolder and calls resolveTenantDomain(), which reads
    // PrivilegedCarbonContext and throws outside a tenant flow -- needed even though the resolver is never reached.
    @Before
    public void wireApiManagerConfigurationService() {
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(new APIManagerConfiguration()));
        ServiceReferenceHolder.getInstance().setAPIMConfigService(new NoOpApimConfigService());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234); // super tenant, no resolution
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
    }

    @After
    public void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    /** Trivial no-op {@link APIMConfigService}: no tenant config stored for any organization. */
    private static final class NoOpApimConfigService implements APIMConfigService {
        @Override
        public void addExternalStoreConfig(String organization, String externalStoreConfig) { }

        @Override
        public void updateExternalStoreConfig(String organization, String externalStoreConfig) { }

        @Override
        public String getExternalStoreConfig(String organization) {
            return null;
        }

        @Override
        public void addTenantConfig(String organization, String tenantConfig) { }

        @Override
        public String getTenantConfig(String organization) {
            return null;
        }

        @Override
        public void updateTenantConfig(String organization, String tenantConfig) { }

        @Override
        public String getWorkFlowConfig(String organization) {
            return null;
        }

        @Override
        public void updateWorkflowConfig(String organization, String workflowConfig) { }

        @Override
        public void addWorkflowConfig(String organization, String workflowConfig) { }

        @Override
        public String getGAConfig(String organization) {
            return null;
        }

        @Override
        public void updateGAConfig(String organization, String gaConfig) { }

        @Override
        public void addGAConfig(String organization, String gaConfig) { }

        @Override
        public Object getSelfSighupConfig(String organization) {
            return null;
        }

        @Override
        public void updateSelfSighupConfig(String organization, String selfSignUpConfig) { }

        @Override
        public void addSelfSighupConfig(String organization, String selfSignUpConfig) { }
    }

    // ---- fixture helpers -------------------------------------------------

    /**
     * A valid WSDL 2.0 document (namespace {@code http://www.w3.org/ns/wsdl}) whose {@code <types>}
     * contains an inline schema with a nested {@code xsd:import} at the given schemaLocation --
     * modelled on the earlier manual PoC fixture (scratch-wsdl-poc/arch/w20_http-import/service.wsdl).
     */
    private static String wsdl20WithXsdImport(String schemaLocation) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<description xmlns=\"http://www.w3.org/ns/wsdl\"\n"
                + "             xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "             xmlns:tns=\"urn:poc\"\n"
                + "             targetNamespace=\"urn:poc\">\n"
                + "  <types>\n"
                + "    <xsd:schema targetNamespace=\"urn:poc\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "      <xsd:import namespace=\"urn:c\" schemaLocation=\"" + schemaLocation + "\"/>\n"
                + "    </xsd:schema>\n"
                + "  </types>\n"
                + "</description>\n";
    }

    private File extractedArchiveWith(String wsdlXml) throws IOException {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        Files.write(new File(dir, "service.wsdl").toPath(), wsdlXml.getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    private HttpServer server(HttpHandler handler) throws IOException {
        HttpServer httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        httpServer.createContext("/", handler);
        httpServer.start();
        return httpServer;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private static int port(HttpServer httpServer) {
        return httpServer.getAddress().getPort();
    }

    // ---- test -------------------------------------------------------

    /**
     * initPath: a WSDL 2.0 archive whose inline-schema {@code xsd:import} points at a canary HTTP
     * server must NEVER cause that canary to be hit -- Woden aborts inline-schema parsing
     * (WSDL521, missing base URI) before it discovers the nested import. It is fine (expected) for
     * {@code initPath} to return {@code false} / set an error here; the only thing under test is
     * that the nested schemaLocation is never dereferenced.
     */
    @Test
    public void wsdl20NestedSchemaImportNotReachable() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        HttpServer canary = server(ctx -> {
            hits.incrementAndGet();
            respond(ctx, 200, XSD_BODY);
        });
        File dir = extractedArchiveWith(wsdl20WithXsdImport("http://127.0.0.1:" + port(canary) + "/c.xsd"));
        try {
            WSDL20ProcessorImpl p = new WSDL20ProcessorImpl();
            p.initPath(dir.getAbsolutePath()); // may return false / set an error (WSDL521) -- that's fine
            assertEquals("WSDL 2.0 nested xsd:import must not be fetched (null base URI, "
                    + "Woden aborts inline-schema parsing before discovering it)", 0, hits.get());
        } finally {
            canary.stop(0);
        }
    }
}
