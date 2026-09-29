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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIFileUtil;
import org.xml.sax.InputSource;

import javax.wsdl.xml.WSDLLocator;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link WSDLLocator} that gates every nested WSDL 1.1 schema reference ({@code xsd:import},
 * {@code xsd:include}, {@code xsd:redefine} {@code schemaLocation}) discovered by WSDL4J while parsing a
 * WSDL 1.1 document.
 * <p>
 * A reference is classified as:
 * <ul>
 *     <li><b>REMOTE</b> — absolute {@code http}/{@code https}, or relative resolved against a remote
 *     {@code parentLocation} — routed through the injected {@link RemoteSchemaFetcher}, which validates the
 *     URL against the network-security access-control policy before fetching it.</li>
 *     <li><b>LOCAL</b> — relative resolved against a local/archive {@code parentLocation} — contained to the
 *     extracted WSDL archive root via {@link APIFileUtil#resolveFilePath(String, String)}.</li>
 *     <li><b>LOCAL-ABSOLUTE</b> — a {@code file:} URI or an absolute filesystem path — always blocked; there
 *     is nothing safe to anchor an absolute local path to.</li>
 * </ul>
 * A blocked reference is never surfaced as a fetch of the blocked target: it is recorded (for later
 * reporting via {@link #hasBlockedReferences()} / {@link #getBlockedReferences()}) and a harmless empty
 * schema stub is returned instead, so the WSDL4J parse degrades gracefully (the blocked types are simply
 * omitted) rather than aborting outright — {@link #getImportInputSource(String, String)} never returns
 * {@code null}.
 */
public class AccessControlledWSDLLocator implements WSDLLocator {

    private static final Logger log = LoggerFactory.getLogger(AccessControlledWSDLLocator.class);

    static final String EMPTY_SCHEMA_STUB = "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"/>";

    private final String archiveRoot;
    private final String baseUri;
    private final RemoteSchemaFetcher fetcher;
    private final List<String> blockedReferences = new ArrayList<>();

    private String latestImportURI;

    public AccessControlledWSDLLocator(String archiveRootOrNull, String baseUriOrNull, String tenantDomain) {
        this(archiveRootOrNull, baseUriOrNull, new PolicyGatedSchemaFetcher(tenantDomain));
    }

    AccessControlledWSDLLocator(String archiveRootOrNull, String baseUriOrNull, RemoteSchemaFetcher fetcher) {
        this.archiveRoot = archiveRootOrNull;
        this.baseUri = baseUriOrNull;
        this.fetcher = fetcher;
    }

    @Override
    public InputSource getBaseInputSource() {
        // Defensive: the readWSDL(WSDLLocator, Element) overload this locator is installed through never calls it.
        InputSource source = new InputSource(new StringReader(EMPTY_SCHEMA_STUB));
        source.setSystemId(baseUri);
        return source;
    }

    @Override
    public InputSource getImportInputSource(String parentLocation, String importLocation) {
        Classification classification = classify(parentLocation, importLocation);
        switch (classification.type) {
        case REMOTE:
            return fetchRemote(classification.effective);
        case LOCAL:
            return readLocal(parentLocation, classification.effective);
        case LOCAL_ABSOLUTE:
        default:
            recordBlocked(importLocation);
            return stub();
        }
    }

    @Override
    public String getBaseURI() {
        return baseUri;
    }

    @Override
    public String getLatestImportURI() {
        return latestImportURI;
    }

    @Override
    public void close() {
    }

    /** @return true if at least one nested reference was blocked by the policy during parsing. */
    public boolean hasBlockedReferences() {
        return !blockedReferences.isEmpty();
    }

    /** @return the nested reference locations that were blocked (for user feedback / logging). */
    public List<String> getBlockedReferences() {
        return blockedReferences;
    }

    /**
     * Classifies {@code importLocation} relative to {@code parentLocation} to decide the routing.
     */
    private Classification classify(String parentLocation, String importLocation) {
        if (isRemoteAbsolute(importLocation)) {
            return new Classification(Type.REMOTE, importLocation);
        }
        if (isLocalAbsolute(importLocation)) {
            return new Classification(Type.LOCAL_ABSOLUTE, importLocation);
        }
        if (isRemoteAbsolute(parentLocation)) {
            String effective = resolveUri(parentLocation, importLocation);
            // Re-check the scheme on the FINAL effective URL: a relative ref carrying its own absolute
            // non-http(s) scheme (ftp:/jar:/file:) is unchanged by URI#resolve and must not inherit REMOTE.
            if (effective == null || !isRemoteAbsolute(effective)) {
                return new Classification(Type.LOCAL_ABSOLUTE, importLocation);
            }
            return new Classification(Type.REMOTE, effective);
        }
        return new Classification(Type.LOCAL, importLocation);
    }

    private InputSource fetchRemote(String effectiveUrl) {
        try {
            java.io.InputStream stream = fetcher.fetch(effectiveUrl);
            latestImportURI = effectiveUrl;
            InputSource source = new InputSource(stream);
            source.setSystemId(effectiveUrl);
            return source;
        } catch (APIManagementException e) {
            // Policy block (expected): record it and degrade to a harmless stub rather than aborting the parse.
            recordBlocked(effectiveUrl);
            return stub();
        } catch (IOException e) {
            // Transport error (timeout, 404), not a policy block: re-thrown (wrapped, no checked exceptions) so a
            // broken reference fails the parse instead of silently "validating" with missing types.
            throw new SchemaResolutionRuntimeException(e);
        }
    }

    private InputSource readLocal(String parentLocation, String relativeRef) {
        if (archiveRoot == null) {
            // No archive root to anchor a relative local ref to (e.g. a pasted single WSDL) -> block.
            recordBlocked(relativeRef);
            return stub();
        }
        Path root = Paths.get(archiveRoot).toAbsolutePath().normalize();
        Path candidate;
        try {
            // Resolve against the referring doc's dir (parentLocation); parentDirWithin clamps it to the archive root.
            Path parentDir = parentDirWithin(parentLocation, root);
            candidate = parentDir.resolve(relativeRef).normalize();
        } catch (InvalidPathException e) {
            // relativeRef is not a valid filesystem path fragment -> treat as a (non-fetch) block.
            recordBlocked(relativeRef);
            return stub();
        }
        try {
            // Containment check via APIFileUtil.resolveFilePath: an escaping candidate is ".."-prefixed and rejected.
            String rel = root.relativize(candidate).toString();
            Path resolved = APIFileUtil.resolveFilePath(archiveRoot, rel);
            latestImportURI = resolved.toUri().toString();
            InputSource source = new InputSource(Files.newInputStream(resolved));
            source.setSystemId(latestImportURI);
            return source;
        } catch (APIManagementException | IllegalArgumentException e) {
            // Containment rejection (escape / incompatible root): fail-closed (non-fetch) block, recorded + stubbed.
            recordBlocked(relativeRef);
            return stub();
        } catch (IOException e) {
            // Unreadable in-archive file: not a policy block, so re-thrown (like fetchRemote) rather than stubbed.
            throw new SchemaResolutionRuntimeException(e);
        }
    }

    /**
     * Derives the directory to resolve a relative local reference against: the directory of the REFERRING
     * document ({@code parentLocation}), contained to the archive {@code root}. {@code parentLocation} may be
     * a plain filesystem path (the top-level document base set by the processor) or a {@code file:} URI (a
     * previously-resolved import's systemId — see {@code latestImportURI}). If it is blank, unparseable, has
     * no parent, or resolves OUTSIDE the archive root, the archive root itself is returned — a defensive
     * clamp so {@code parentLocation} can never widen the resolution base beyond the archive.
     */
    private static Path parentDirWithin(String parentLocation, Path root) {
        if (parentLocation == null || parentLocation.trim().isEmpty()) {
            return root;
        }
        try {
            Path parentPath = (schemeOf(parentLocation) != null)
                    ? Paths.get(URI.create(parentLocation))
                    : Paths.get(parentLocation);
            Path parentDir = parentPath.getParent();
            if (parentDir == null) {
                return root;
            }
            parentDir = parentDir.toAbsolutePath().normalize();
            if (!parentDir.startsWith(root)) {
                return root;
            }
            return parentDir;
        } catch (RuntimeException e) {
            // Unparseable/non-local parentLocation: fall back to the archive root rather than trusting it.
            return root;
        }
    }

    private void recordBlocked(String ref) {
        if (log.isDebugEnabled()) {
            log.debug("Blocked WSDL schema reference: " + ref);
        }
        blockedReferences.add(ref);
        // WSDL4J looks up getLatestImportURI() in a Hashtable after every import, including blocked ones,
        // and a null key throws NPE — so this must be set on every block path or the first block breaks the parse.
        latestImportURI = (ref != null) ? ref : "urn:apim:blocked-reference";
    }

    private static InputSource stub() {
        return new InputSource(new StringReader(EMPTY_SCHEMA_STUB));
    }

    private static boolean isRemoteAbsolute(String location) {
        if (location == null) {
            return false;
        }
        String scheme = schemeOf(location);
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private static boolean isLocalAbsolute(String location) {
        if (location == null) {
            return false;
        }
        String scheme = schemeOf(location);
        if ("file".equalsIgnoreCase(scheme)) {
            return true;
        }
        try {
            return java.nio.file.Paths.get(location).isAbsolute();
        } catch (java.nio.file.InvalidPathException e) {
            return false;
        }
    }

    private static String schemeOf(String location) {
        try {
            return new URI(location).getScheme();
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * Resolves {@code importLocation} against a remote {@code parentLocation}, returning the fully-resolved
     * absolute URI string, or {@code null} if resolution fails.
     */
    private static String resolveUri(String parentLocation, String importLocation) {
        try {
            return new URI(parentLocation).resolve(importLocation).toString();
        } catch (URISyntaxException | IllegalArgumentException e) {
            return null;
        }
    }

    private enum Type {
        REMOTE, LOCAL, LOCAL_ABSOLUTE
    }

    private static final class Classification {
        private final Type type;
        private final String effective;

        private Classification(Type type, String effective) {
            this.type = type;
            this.effective = effective;
        }
    }
}
