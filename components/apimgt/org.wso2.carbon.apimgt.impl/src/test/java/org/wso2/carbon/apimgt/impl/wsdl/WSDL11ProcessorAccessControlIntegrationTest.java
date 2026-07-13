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
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * End-to-end network access-control wiring tests for {@link WSDL11ProcessorImpl}: proves that every entry point
 * (init(byte[]), init(URL), initPath) routes nested WSDL 1.1 schema imports through
 * {@link AccessControlledWSDLLocator} rather than the raw WSDL4J default locator.
 * <p>
 * No PowerMock, no network-security policy configuration is applied here: the block proofs rely on
 * references that {@link org.wso2.carbon.apimgt.impl.utils.APIFileUtil#resolveFilePath(String, String)} /
 * the locator's own classification reject unconditionally (local traversal, file: URIs, pasted-relative
 * refs with no archive root) -- independent of any policy state. The remote-allowed test relies on
 * {@code validateRemoteURL} being a no-op when no policy is configured (backwards-compat), proving the
 * wiring still resolves legitimate remote schemas.
 */
public class WSDL11ProcessorAccessControlIntegrationTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private static final String XSD_BODY =
            "<xsd:schema targetNamespace=\"urn:c\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/>";

    // Wire the real collaborators the init paths need outside OSGi: an empty APIManagerConfiguration, a started tenant
    // flow for resolveTenantDomain, and a no-op APIMConfigService so validateRemoteURL takes its no-policy no-op path.
    @Before
    public void wireApiManagerConfigurationService() {
        ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(
                new APIManagerConfigurationServiceImpl(new APIManagerConfiguration()));
        ServiceReferenceHolder.getInstance().setAPIMConfigService(new NoOpApimConfigService());
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(-1234); // super tenant, no resolution
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain("carbon.super");
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

    @After
    public void endTenantFlow() {
        PrivilegedCarbonContext.endTenantFlow();
    }

    // ---- fixture helpers -------------------------------------------------

    private static String wsdlWithXsdImport(String schemaLocation) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"\n"
                + "                  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "                  xmlns:tns=\"urn:poc\"\n"
                + "                  targetNamespace=\"urn:poc\">\n"
                + "  <wsdl:types>\n"
                + "    <xsd:schema targetNamespace=\"urn:poc\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "      <xsd:import namespace=\"urn:c\" schemaLocation=\"" + schemaLocation + "\"/>\n"
                + "    </xsd:schema>\n"
                + "  </wsdl:types>\n"
                + "  <wsdl:message name=\"req\"/>\n"
                + "  <wsdl:portType name=\"pt\"/>\n"
                + "</wsdl:definitions>\n";
    }

    /** A minimal WSDL 1.1 document with no {@code xsd:import} at all -- nothing for the locator to gate. */
    private static final String PLAIN_WSDL_NO_IMPORTS =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"\n"
                    + "                  xmlns:tns=\"urn:poc\"\n"
                    + "                  targetNamespace=\"urn:poc\">\n"
                    + "  <wsdl:message name=\"req\"/>\n"
                    + "  <wsdl:portType name=\"pt\"/>\n"
                    + "</wsdl:definitions>\n";

    private static String wsdlWithTwoXsdImports(String firstSchemaLocation, String secondNamespace,
            String secondSchemaLocation) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"\n"
                + "                  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"\n"
                + "                  xmlns:tns=\"urn:poc\"\n"
                + "                  targetNamespace=\"urn:poc\">\n"
                + "  <wsdl:types>\n"
                + "    <xsd:schema targetNamespace=\"urn:poc\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "      <xsd:import namespace=\"urn:c\" schemaLocation=\"" + firstSchemaLocation + "\"/>\n"
                + "      <xsd:import namespace=\"" + secondNamespace + "\" schemaLocation=\""
                + secondSchemaLocation + "\"/>\n"
                + "    </xsd:schema>\n"
                + "  </wsdl:types>\n"
                + "  <wsdl:message name=\"req\"/>\n"
                + "  <wsdl:portType name=\"pt\"/>\n"
                + "</wsdl:definitions>\n";
    }

    private File extractedArchiveWith(String wsdlXml) throws IOException {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        Files.write(new File(dir, "service.wsdl").toPath(), wsdlXml.getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    private File extractedArchiveWithSiblingXsd() throws IOException {
        File dir = extractedArchiveWith(wsdlWithXsdImport("types.xsd"));
        String typesXsd =
                "<xsd:schema targetNamespace=\"urn:c\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/>";
        Files.write(new File(dir, "types.xsd").toPath(), typesXsd.getBytes(StandardCharsets.UTF_8));
        return dir;
    }

    /** A standalone XSD (targetNamespace urn:c) that itself imports another schema. */
    private static String schemaImporting(String importNamespace, String schemaLocation) {
        return "<xsd:schema targetNamespace=\"urn:c\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n"
                + "  <xsd:import namespace=\"" + importNamespace + "\" schemaLocation=\"" + schemaLocation + "\"/>\n"
                + "</xsd:schema>";
    }

    /** A leaf XSD with no nested imports. */
    private static String leafSchema(String namespace) {
        return "<xsd:schema targetNamespace=\"" + namespace + "\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/>";
    }

    private static void write(File file, String content) throws IOException {
        Files.write(file.toPath(), content.getBytes(StandardCharsets.UTF_8));
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

    // ---- tests -------------------------------------------------------

    // initPath: local traversal xsd:import is blocked end-to-end -> UNTRUSTED_URL, file NOT read
    @Test
    public void archiveTraversalSchemaImportBlocked() throws Exception {
        File dir = extractedArchiveWith(wsdlWithXsdImport("../../../../../../etc/hostname"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        boolean ok = p.initPath(dir.getAbsolutePath());
        assertFalse(ok);
        assertTrue(p.hasError());
        // UNTRUSTED_URL_IN_DEFINITION surfaced via reportBlockedReferencesIfAny
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
    }

    // initPath: remote xsd:import IS fetched when no policy configured (no-op) -> wiring resolves remote schemas
    @Test
    public void archiveRemoteSchemaImportFetchedWhenAllowed() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        HttpServer canary = server(ctx -> { hits.incrementAndGet(); respond(ctx, 200, XSD_BODY); });
        File dir = extractedArchiveWith(wsdlWithXsdImport("http://127.0.0.1:" + port(canary) + "/c.xsd"));
        try {
            WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
            p.initPath(dir.getAbsolutePath());
            assertTrue("no policy -> remote schema fetched (backwards-compat)", hits.get() >= 1);
            assertFalse(p.hasError());
        } finally {
            canary.stop(0);
        }
    }

    // initPath: legitimate in-archive relative schema still resolves (no error)
    @Test
    public void archiveLocalSchemaImportStillWorks() throws Exception {
        File dir = extractedArchiveWithSiblingXsd();   // service.wsdl + types.xsd, schemaLocation="types.xsd"
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        assertTrue(p.initPath(dir.getAbsolutePath()));
        assertFalse(p.hasError());
    }

    // init(byte[]): pasted WSDL with a local (relative) xsd:import has no archive root -> blocked -> UNTRUSTED_URL
    @Test
    public void pastedLocalSchemaImportBlocked() throws Exception {
        byte[] wsdl = wsdlWithXsdImport("types.xsd").getBytes(StandardCharsets.UTF_8);
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        p.init(wsdl);
        assertTrue(p.hasError());
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
    }

    // A bare Thread does not inherit the @Before tenant flow (non-inheritable ThreadLocal); resolveTenantDomain's
    // NPE-fallback must let init(byte[]) still succeed when there is no xsd:import to gate.
    @Test
    public void initSucceedsOnThreadWithoutTenantFlow() throws Exception {
        byte[] wsdl = PLAIN_WSDL_NO_IMPORTS.getBytes(StandardCharsets.UTF_8);
        AtomicReference<Boolean> result = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(() -> {
            try {
                WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
                result.set(p.init(wsdl));
                if (p.hasError()) {
                    failure.set(new AssertionError("expected no error; got errorCode="
                            + p.getError().getErrorCode()));
                }
            } catch (Throwable t) {
                failure.set(t);
            }
        });
        worker.start();
        worker.join();
        assertNull("init() must not throw / error out when there is no CarbonContext on the current thread",
                failure.get());
        assertTrue("no xsd:import to block -> init should succeed via the tenant-domain fallback",
                Boolean.TRUE.equals(result.get()));
    }

    // Within one WSDL, a blocked first xsd:import must not corrupt a legitimate second sibling import:
    // the block is recorded (UNTRUSTED_URL) but the import degrades to a stub, so the WSDL still parses.
    @Test
    public void siblingImportIndependentOfEarlierBlockedImport() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "service.wsdl"),
                wsdlWithTwoXsdImports("../../../../../../etc/hostname", "urn:d", "legit.xsd"));
        write(new File(dir, "legit.xsd"), leafSchema("urn:d"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        boolean ok = p.initPath(dir.getAbsolutePath());
        assertFalse(ok);
        assertTrue(p.hasError());
        // UNTRUSTED_URL_IN_DEFINITION surfaced from the first import
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
        // the WSDL still parsed: the blocked FIRST import did not abort the whole parse, so the SECOND,
        // unrelated sibling import was not corrupted by it.
        assertNotNull(p.getWSDLDefinition());
    }

    // init(URL): remote WSDL whose xsd:import is a file: ref -> LOCAL_ABSOLUTE -> blocked -> UNTRUSTED_URL, file NOT read
    @Test
    public void remoteWsdlWithFileSchemaImportBlocked() throws Exception {
        HttpServer wsdlHost = server(ctx -> respond(ctx, 200, wsdlWithXsdImport("file:///etc/hostname")));
        try {
            WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
            p.init(new URL("http://127.0.0.1:" + port(wsdlHost) + "/service.wsdl"));
            assertTrue(p.hasError());
            assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
        } finally {
            wsdlHost.stop(0);
        }
    }

    // SOAP-to-REST extractor: legit in-archive schema still extracts types (schema content NOT disabled)
    @Test
    public void soapToRestUnaffectedForLocalSchema() throws Exception {
        File dir = extractedArchiveWithSiblingXsd();
        WSDL11SOAPOperationExtractor ex = new WSDL11SOAPOperationExtractor();
        assertTrue(ex.initPath(dir.getAbsolutePath()));
        assertFalse(ex.hasError());
    }

    // init(URL) with a file: URL has relative refs to the WSDL's OWN directory: a sibling schema
    // (schemaLocation="types.xsd") must resolve because the file: URL's parent dir is the containment root, not null.
    @Test
    public void fileUrlWsdlWithSiblingSchemaResolves() throws Exception {
        File dir = extractedArchiveWithSiblingXsd();   // writes service.wsdl + types.xsd (schemaLocation="types.xsd")
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        boolean ok = p.init(new File(dir, "service.wsdl").toURI().toURL());
        assertTrue("sibling schema should resolve via document-dir containment, not be blocked", ok);
        assertFalse(p.hasError());
    }

    // A schema in a subdirectory (xsd/a.xsd) importing a SIBLING (b.xsd) must resolve relative to its
    // OWN directory (xsd/), not the fixed archive root. Before the fix this looked for root/b.xsd and failed.
    @Test
    public void nestedSubdirCrossImportResolves() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "service.wsdl"), wsdlWithXsdImport("xsd/a.xsd"));
        File xsd = new File(dir, "xsd");
        assertTrue(xsd.mkdirs());
        write(new File(xsd, "a.xsd"), schemaImporting("urn:d", "b.xsd"));
        write(new File(xsd, "b.xsd"), leafSchema("urn:d"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        assertTrue(p.initPath(dir.getAbsolutePath()));
        assertFalse(p.hasError());
    }

    // A "../"-ref from a subdirectory schema that lands back INSIDE the archive (xsd/a.xsd -> ../common/c.xsd)
    // must resolve. Before the fix it was resolved against the archive root, escaped it, and was wrongly blocked.
    @Test
    public void dotDotRefWithinArchiveResolves() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "service.wsdl"), wsdlWithXsdImport("xsd/a.xsd"));
        File xsd = new File(dir, "xsd");
        File common = new File(dir, "common");
        assertTrue(xsd.mkdirs());
        assertTrue(common.mkdirs());
        write(new File(xsd, "a.xsd"), schemaImporting("urn:d", "../common/c.xsd"));
        write(new File(common, "c.xsd"), leafSchema("urn:d"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        assertTrue(p.initPath(dir.getAbsolutePath()));
        assertFalse(p.hasError());
    }

    // A WSDL located in a subdirectory (wsdl/service.wsdl) importing a sibling schema (types.xsd) must
    // resolve relative to the WSDL's own directory. Before the fix this looked for root/types.xsd and failed.
    @Test
    public void wsdlInSubdirSiblingResolves() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        File wsdlDir = new File(dir, "wsdl");
        assertTrue(wsdlDir.mkdirs());
        write(new File(wsdlDir, "service.wsdl"), wsdlWithXsdImport("types.xsd"));
        write(new File(wsdlDir, "types.xsd"), leafSchema("urn:c"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        assertTrue(p.initPath(dir.getAbsolutePath()));
        assertFalse(p.hasError());
    }

    // A nested-subdir schema whose import escapes the archive root
    // (xsd/a.xsd -> ../../../../etc/hostname) must STILL be blocked -> UNTRUSTED_URL, nothing outside read.
    @Test
    public void archiveEscapeStillBlocked() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "service.wsdl"), wsdlWithXsdImport("xsd/a.xsd"));
        File xsd = new File(dir, "xsd");
        assertTrue(xsd.mkdirs());
        write(new File(xsd, "a.xsd"), schemaImporting("urn:d", "../../../../etc/hostname"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        boolean ok = p.initPath(dir.getAbsolutePath());
        assertFalse(ok);
        assertTrue(p.hasError());
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
    }

    // A schema reference that resolves INSIDE the archive but is absent on disk is a GENUINE failure, not a
    // policy block: it must map to CANNOT_PROCESS_WSDL_CONTENT (900676) and NOT escape initPath as a RuntimeException.
    @Test
    public void missingContainedSchemaMapsToCannotProcess() throws Exception {
        File dir = extractedArchiveWith(wsdlWithXsdImport("missing.xsd"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        boolean ok = p.initPath(dir.getAbsolutePath());   // must NOT throw
        assertFalse(ok);
        assertTrue(p.hasError());
        assertEquals(ExceptionCodes.CANNOT_PROCESS_WSDL_CONTENT.getErrorCode(), p.getError().getErrorCode());
    }

    // A multi-WSDL archive where ONE file has a blocked (traversal) import and another is clean: the whole
    // import is reported UNTRUSTED_URL, yet the clean file still parses (the blocked ref degrades to a stub).
    @Test
    public void multiFileArchiveOneFileBlocked() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "clean.wsdl"), wsdlWithXsdImport("types.xsd"));
        write(new File(dir, "types.xsd"), leafSchema("urn:c"));
        write(new File(dir, "evil.wsdl"), wsdlWithXsdImport("../../../../../../etc/hostname"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        boolean ok = p.initPath(dir.getAbsolutePath());
        assertFalse(ok);
        assertTrue(p.hasError());
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
        // getWSDLDefinition() is protected but this test is in-package: proves at least one WSDL parsed.
        assertNotNull(p.getWSDLDefinition());
    }

    // init(URL) on a file: WSDL whose xsd:import is a traversal ("../../..") must be blocked.
    @Test
    public void fileUrlTraversalBlocked() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "service.wsdl"), wsdlWithXsdImport("../../../../../../etc/hostname"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        p.init(new File(dir, "service.wsdl").toURI().toURL());
        assertTrue(p.hasError());
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
    }

    // init(URL) on a file: WSDL whose xsd:import is an ABSOLUTE local path ("/etc/hostname") must be
    // blocked (LOCAL_ABSOLUTE classification).
    @Test
    public void fileUrlAbsoluteBlocked() throws Exception {
        File dir = tmp.newFolder("archive-" + System.nanoTime());
        write(new File(dir, "service.wsdl"), wsdlWithXsdImport("/etc/hostname"));
        WSDL11ProcessorImpl p = new WSDL11ProcessorImpl();
        p.init(new File(dir, "service.wsdl").toURI().toURL());
        assertTrue(p.hasError());
        assertEquals(ExceptionCodes.UNTRUSTED_URL_IN_DEFINITION.getErrorCode(), p.getError().getErrorCode());
    }
}
