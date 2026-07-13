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

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.xml.sax.InputSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AccessControlledWSDLLocatorTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    // local ref inside archive resolves to the real file
    @Test
    public void resolvesInArchiveRelativeRef() throws Exception {
        File root = tmp.getRoot();
        File child = new File(root, "types.xsd");
        Files.writeString(child.toPath(), "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='inarchive'/>");
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(
                root.getAbsolutePath(), new File(root, "service.wsdl").getAbsolutePath(),
                (u) -> { throw new AssertionError("no remote"); });
        InputSource is = loc.getImportInputSource(new File(root, "service.wsdl").getAbsolutePath(), "types.xsd");
        assertTrue(read(is).contains("inarchive"));
        assertFalse(loc.hasBlockedReferences());
    }

    // traversal escaping the archive is blocked (recorded + stub, no exception, no file read)
    @Test
    public void blocksArchiveEscape() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(
                tmp.getRoot().getAbsolutePath(), null, (u) -> { throw new AssertionError("no remote"); });
        InputSource is = loc.getImportInputSource(tmp.getRoot().getAbsolutePath(), "../../../../etc/hostname");
        assertTrue(read(is).contains("xsd:schema"));      // harmless stub
        assertTrue(loc.hasBlockedReferences());
    }

    // absolute file: ref is blocked
    @Test
    public void blocksAbsoluteFileRef() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(
                tmp.getRoot().getAbsolutePath(), null, (u) -> { throw new AssertionError("no remote"); });
        loc.getImportInputSource(tmp.getRoot().getAbsolutePath(), "file:///etc/passwd");
        assertTrue(loc.hasBlockedReferences());
        assertTrue(loc.getBlockedReferences().contains("file:///etc/passwd"));
    }

    // no archive root: any local ref blocked
    @Test
    public void blocksLocalRefWhenNoArchiveRoot() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, null,
                (u) -> { throw new AssertionError("no remote"); });
        loc.getImportInputSource(null, "sibling.xsd");
        assertTrue(loc.hasBlockedReferences());
        assertTrue(loc.getBlockedReferences().contains("sibling.xsd"));
    }

    // A relative import whose scheme is not http/https (e.g. ftp:) is already an absolute URI, so URI#resolve
    // returns it unchanged; it must be blocked (not inherit the remote parent) and the fetcher must never run.
    @Test
    public void blocksNonHttpSchemeUnderRemoteParent() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, "http://h/dir/root.wsdl",
                (u) -> {
                    throw new AssertionError("fetcher must not be invoked for a non-http/https scheme: " + u);
                });
        InputSource is = loc.getImportInputSource("http://h/dir/root.wsdl", "ftp://internal/x.xsd");
        assertTrue(read(is).contains("xsd:schema"));   // harmless stub
        assertTrue(loc.hasBlockedReferences());
        assertTrue(loc.getBlockedReferences().contains("ftp://internal/x.xsd"));
    }

    // Same gap, another absolute scheme: "jar:http://internal/x.jar!/y.xsd" is opaque-but-absolute, so it
    // also resolves ~unchanged against an http/https parent and must be blocked rather than fetched.
    @Test
    public void blocksJarSchemeUnderRemoteParent() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, "http://h/dir/root.wsdl",
                (u) -> {
                    throw new AssertionError("fetcher must not be invoked for a non-http/https scheme: " + u);
                });
        InputSource is = loc.getImportInputSource("http://h/dir/root.wsdl", "jar:http://internal/x.jar!/y.xsd");
        assertTrue(read(is).contains("xsd:schema"));   // harmless stub
        assertTrue(loc.hasBlockedReferences());
    }

    // A genuine (non-policy) fetch failure must propagate out of getImportInputSource rather than
    // silently degrading to a stub -- it must NOT be recorded as a blocked reference either.
    @Test
    public void propagatesGenuineFetchFailure() {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, null,
                (u) -> { throw new IOException("boom"); });
        try {
            loc.getImportInputSource(null, "http://trusted.example/a.xsd");
            fail("expected getImportInputSource to propagate the genuine fetch failure");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        assertFalse(loc.hasBlockedReferences());
    }

    // A reference that resolves safely INSIDE the archive root but does not exist on
    // disk is a genuine failure (Files.newInputStream throws), not a policy block -- it must propagate too.
    @Test
    public void propagatesMissingContainedLocalFile() {
        File root = tmp.getRoot();
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(
                root.getAbsolutePath(), new File(root, "service.wsdl").getAbsolutePath(),
                (u) -> { throw new AssertionError("no remote"); });
        try {
            loc.getImportInputSource(new File(root, "service.wsdl").getAbsolutePath(), "present.xsd");
            fail("expected getImportInputSource to propagate the missing-file failure");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
        assertFalse(loc.hasBlockedReferences());
    }

    // remote allowed: delegates to fetcher, records latestImportURI, not blocked
    @Test
    public void fetchesAllowedRemoteRef() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, null,
                (u) -> new ByteArrayInputStream(
                        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='remote'/>".getBytes()));
        InputSource is = loc.getImportInputSource(null, "http://trusted.example/a.xsd");
        assertTrue(read(is).contains("remote"));
        assertEquals("http://trusted.example/a.xsd", loc.getLatestImportURI());
        assertFalse(loc.hasBlockedReferences());
    }

    // remote blocked: fetcher throws -> recorded + stub, no exception out of getImportInputSource
    @Test
    public void blocksUntrustedRemoteRef() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, null,
                (u) -> { throw new APIManagementException(ExceptionCodes.UNTRUSTED_URL); });
        InputSource is = loc.getImportInputSource(null, "http://169.254.169.254/x.xsd");
        assertTrue(read(is).contains("xsd:schema"));   // stub
        assertTrue(loc.hasBlockedReferences());
    }

    // Every block path must set latestImportURI to a non-null value -- WSDL4J's parseSchema looks it
    // up in a java.util.Hashtable after each import, and a null key NPEs there.
    @Test
    public void blockSetsNonNullLatestImportURI() throws Exception {
        AccessControlledWSDLLocator remoteBlocked = new AccessControlledWSDLLocator(null, null,
                (u) -> { throw new APIManagementException(ExceptionCodes.UNTRUSTED_URL); });
        assertEquals(null, remoteBlocked.getLatestImportURI());
        remoteBlocked.getImportInputSource(null, "http://169.254.169.254/x.xsd");
        assertTrue(remoteBlocked.getLatestImportURI() != null);

        AccessControlledWSDLLocator archiveEscape = new AccessControlledWSDLLocator(
                tmp.getRoot().getAbsolutePath(), null, (u) -> { throw new AssertionError("no remote"); });
        archiveEscape.getImportInputSource(tmp.getRoot().getAbsolutePath(), "../x");
        assertTrue(archiveEscape.getLatestImportURI() != null);
    }

    // multi-level chaining: latestImportURI feeds the next parentLocation
    @Test
    public void chainsNestedRelativeRefViaLatestImportURI() throws Exception {
        // level-1 relative remote via remote base, then a second relative resolved against latestImportURI
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, "http://h/dir/root.wsdl",
                (u) -> new ByteArrayInputStream(
                        "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema'/>".getBytes()));
        loc.getImportInputSource("http://h/dir/root.wsdl", "a/one.xsd");
        assertEquals("http://h/dir/a/one.xsd", loc.getLatestImportURI());
        loc.getImportInputSource(loc.getLatestImportURI(), "../two.xsd");
        assertEquals("http://h/dir/two.xsd", loc.getLatestImportURI());
    }

    // A relative ref is resolved against the REFERRING document's directory (parentLocation's parent),
    // not the fixed archive root. With a same-named decoy at the root, the SUBDIR sibling must win.
    @Test
    public void resolvesRefRelativeToParentDir() throws Exception {
        File root = tmp.getRoot();
        File subdir = new File(root, "sub");
        assertTrue(subdir.mkdirs());
        Files.writeString(new File(subdir, "sibling.xsd").toPath(),
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='insubdir'/>");
        // decoy with the same name at the archive root -- must NOT be the one resolved
        Files.writeString(new File(root, "sibling.xsd").toPath(),
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='atroot'/>");
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(
                root.getAbsolutePath(), new File(subdir, "a.xsd").getAbsolutePath(),
                (u) -> { throw new AssertionError("no remote"); });
        InputSource is = loc.getImportInputSource(new File(subdir, "a.xsd").getAbsolutePath(), "sibling.xsd");
        String content = read(is);
        assertTrue("must resolve the SUBDIR sibling, not the root decoy", content.contains("insubdir"));
        assertFalse(content.contains("atroot"));
        assertFalse(loc.hasBlockedReferences());
    }

    // A scheme-relative ref ("//somehost/x.xsd") under a remote parent must be blocked, not read as a local
    // file: Paths.get reports it absolute, so classify() marks it LOCAL_ABSOLUTE and blocks before the fetcher.
    @Test
    public void blocksSchemeRelativeAuthorityRefUnderRemoteParent() throws Exception {
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(null, "http://h/dir/root.wsdl",
                (u) -> {
                    throw new AssertionError("fetcher must not be invoked for a scheme-relative ref: " + u);
                });
        InputSource is = loc.getImportInputSource("http://h/dir/root.wsdl", "//somehost/x.xsd");
        assertTrue(read(is).contains("xsd:schema"));   // harmless stub, not a local file's content
        assertTrue(loc.hasBlockedReferences());
        assertTrue(loc.getBlockedReferences().contains("//somehost/x.xsd"));
    }

    // After ONE reference is blocked, a second, unrelated, legitimate reference resolved via the SAME locator
    // instance must still resolve correctly -- the block must not leave state that corrupts a later lookup.
    @Test
    public void blockedReferenceDoesNotCorruptSubsequentSiblingResolution() throws Exception {
        File root = tmp.getRoot();
        Files.writeString(new File(root, "sibling.xsd").toPath(),
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' id='sibling'/>");
        AccessControlledWSDLLocator loc = new AccessControlledWSDLLocator(
                root.getAbsolutePath(), new File(root, "service.wsdl").getAbsolutePath(),
                (u) -> { throw new AssertionError("no remote"); });

        // First import: a traversal outside the archive root -> blocked.
        InputSource blocked = loc.getImportInputSource(
                new File(root, "service.wsdl").getAbsolutePath(), "../../../../etc/hostname");
        assertTrue(read(blocked).contains("xsd:schema"));   // harmless stub
        assertTrue(loc.hasBlockedReferences());

        // Second import: an unrelated, legitimate in-archive sibling -> must still resolve to the real file.
        InputSource legit = loc.getImportInputSource(
                new File(root, "service.wsdl").getAbsolutePath(), "sibling.xsd");
        assertTrue("the blocked first import must not corrupt resolution of the unrelated second import",
                read(legit).contains("id='sibling'"));
        assertEquals(1, loc.getBlockedReferences().size());
        assertTrue(loc.getBlockedReferences().get(0).contains("etc/hostname"));
    }

    // ---- test helpers ----

    private static String read(InputSource is) throws IOException {
        if (is.getCharacterStream() != null) {
            return IOUtils.toString(is.getCharacterStream());
        }
        return IOUtils.toString(is.getByteStream(), StandardCharsets.UTF_8);
    }
}
