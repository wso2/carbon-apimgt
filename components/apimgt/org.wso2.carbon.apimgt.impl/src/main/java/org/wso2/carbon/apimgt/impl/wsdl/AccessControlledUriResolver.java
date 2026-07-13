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

import org.apache.woden.WSDLException;
import org.apache.woden.internal.resolver.SimpleURIResolver;
import org.apache.woden.resolver.URIResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Woden {@link URIResolver} that gates every remote ({@code http}/{@code https}) nested WSDL/XSD
 * reference ({@code wsdl:import}, {@code xsd:import}, {@code xsd:include}) through the network-security
 * policy. Local and catalog references (e.g. Woden's bundled XML-Schema resources served by
 * {@link SimpleURIResolver} as {@code jar:} URIs) are delegated unchanged. A reference whose target is
 * rejected by the policy is redirected to a harmless local empty stub so the parser never opens a
 * connection to the blocked host.
 * <p>
 * Note: returning {@code null} or throwing from a Woden resolver does NOT stop the fetch — Woden falls
 * back to the raw URI. Only redirecting to a different URI prevents the outbound request.
 */
public class AccessControlledUriResolver implements URIResolver {

    private static final Logger log = LoggerFactory.getLogger(AccessControlledUriResolver.class);

    private static final String STUB_WSDL_RESOURCE = "/wsdl/blocked-reference.wsdl";
    private static final String STUB_XSD_RESOURCE = "/wsdl/blocked-reference.xsd";
    // Non-remote fallbacks used only if the bundled stub cannot be located: Woden opens these, so the
    // worst case is a local parse failure (fail-closed), never a fetch of the blocked host.
    private static final URI FALLBACK_WSDL_STUB = URI.create("file:/apim-blocked-reference.wsdl");
    private static final URI FALLBACK_XSD_STUB = URI.create("file:/apim-blocked-reference.xsd");

    private final URIResolver delegate;
    private final String tenantDomain;
    private final List<String> blockedReferences = new ArrayList<>();

    public AccessControlledUriResolver(String tenantDomain) throws WSDLException {
        this(new SimpleURIResolver(), tenantDomain);
    }

    AccessControlledUriResolver(URIResolver delegate, String tenantDomain) {
        this.delegate = delegate;
        this.tenantDomain = tenantDomain;
    }

    @Override
    public URI resolveURI(URI uri) throws WSDLException, IOException {
        URI resolved = delegate.resolveURI(uri);
        // What Woden will actually open: the delegate's result, or the original URI if unmapped.
        URI effective = (resolved != null) ? resolved : uri;
        if (isRemote(effective)) {
            try {
                APIUtil.validateRemoteURL(effective.toString(), tenantDomain);
            } catch (APIManagementException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Blocked WSDL/XSD nested reference by network security policy: " + effective, e);
                }
                blockedReferences.add(effective.toString());
                return stubFor(effective);
            }
        }
        return resolved;
    }

    /** @return true if at least one remote reference was blocked by the policy during resolution. */
    public boolean hasBlockedReferences() {
        return !blockedReferences.isEmpty();
    }

    /** @return the remote reference URLs that were blocked by the policy (for user feedback / logging). */
    public List<String> getBlockedReferences() {
        return blockedReferences;
    }

    private static boolean isRemote(URI uri) {
        String scheme = uri.getScheme();
        return scheme != null && ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme));
    }

    /**
     * Resolves the blocked reference to a bundled, non-remote stub URI. Never throws, never returns
     * {@code null}, and never returns a remote URI, so a resolver return can never cause Woden to fall
     * back to fetching the blocked host. If the bundled resource cannot be located/converted, a
     * hardcoded non-remote fallback is returned (fail-closed).
     */
    private static URI stubFor(URI blocked) {
        String path = blocked.getPath();
        boolean xsd = path != null && path.toLowerCase().endsWith(".xsd");
        String resource = xsd ? STUB_XSD_RESOURCE : STUB_WSDL_RESOURCE;
        URI fallback = xsd ? FALLBACK_XSD_STUB : FALLBACK_WSDL_STUB;
        try {
            URL url = AccessControlledUriResolver.class.getResource(resource);
            if (url != null) {
                return url.toURI();
            }
            log.warn("Blocked-reference stub resource not found on classpath: " + resource
                    + " — using non-remote fallback to keep the block fail-closed");
        } catch (URISyntaxException e) {
            log.warn("Could not convert blocked-reference stub resource to a URI: " + resource
                    + " — using non-remote fallback", e);
        }
        return fallback;
    }
}
