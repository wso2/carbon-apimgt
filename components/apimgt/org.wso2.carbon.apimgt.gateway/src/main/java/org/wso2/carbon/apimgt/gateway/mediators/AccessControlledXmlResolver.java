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

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;

/**
 * An {@link LSResourceResolver} that enforces the network access-control policy on
 * every external reference resolved while an XSD is compiled — nested
 * xsd:import/include/redefine and external DTDs. Only http/https are permitted; each
 * reference is checked via the injected {@link RemoteUrlValidator}. On any violation it
 * throws {@link XsdRefBlockedException} to abort compilation (fail closed). On success it
 * fetches the reference itself through {@link RedirectSafeXsdFetcher} (every redirect
 * re-validated) and returns a {@link BytesLSInput} over the already-retrieved bytes, so
 * Xerces never opens its own (redirect-following) connection for the reference.
 */
public class AccessControlledXmlResolver implements LSResourceResolver {

    private final RemoteUrlValidator validator;

    public AccessControlledXmlResolver(RemoteUrlValidator validator) {
        this.validator = validator;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId,
                                   String systemId, String baseURI) {
        String absoluteUrl = toAbsoluteUrl(systemId, baseURI);
        if (absoluteUrl == null) {
            throw new XsdRefBlockedException(
                    "Blocked XSD reference with an unresolvable system id: " + systemId);
        }
        RedirectSafeXsdFetcher.Result result;
        try {
            result = RedirectSafeXsdFetcher.fetch(absoluteUrl, validator);
        } catch (IOException e) {
            throw new XsdRefFetchException(
                    "Error fetching XSD reference " + absoluteUrl + ": " + e.getMessage(), e);
        }
        // systemId AND baseURI = the final post-redirect URL so further relative refs resolve correctly
        return new BytesLSInput(result.body, result.finalUrl, publicId, result.finalUrl);
    }

    /**
     * Resolves a possibly-relative systemId against its baseURI into an absolute URL
     * string, or returns {@code null} if it cannot be resolved to an absolute URL.
     */
    static String toAbsoluteUrl(String systemId, String baseURI) {
        if (systemId == null) {
            return null;
        }
        try {
            URI ref = new URI(systemId.trim());
            if (ref.isAbsolute()) {
                return ref.toString();
            }
            if (baseURI != null) {
                URI resolved = new URI(baseURI.trim()).resolve(ref);
                return resolved.isAbsolute() ? resolved.toString() : null;
            }
            return null;
        } catch (URISyntaxException e) {
            return null;
        }
    }

    /**
     * @return {@code true} only if the URL parses and has an http or https scheme.
     */
    static boolean isHttpOrHttps(String url) {
        try {
            String scheme = new URI(url).getScheme();
            if (scheme == null) {
                return false;
            }
            scheme = scheme.toLowerCase(Locale.ROOT);
            return "http".equals(scheme) || "https".equals(scheme);
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /**
     * Minimal {@link LSInput} that hands the parser the already-retrieved bytes of a nested reference,
     * so Xerces never opens its own (redirect-following) connection for it.
     */
    static final class BytesLSInput implements LSInput {
        private final byte[] bytes;
        private String systemId;
        private String publicId;
        private String baseURI;

        BytesLSInput(byte[] bytes, String systemId, String publicId, String baseURI) {
            this.bytes = bytes;
            this.systemId = systemId;
            this.publicId = publicId;
            this.baseURI = baseURI;
        }

        @Override
        public InputStream getByteStream() {
            return new ByteArrayInputStream(bytes);
        }

        @Override
        public void setByteStream(InputStream byteStream) {
        }

        @Override
        public Reader getCharacterStream() {
            return null;
        }

        @Override
        public void setCharacterStream(Reader characterStream) {
        }

        @Override
        public String getStringData() {
            return null;
        }

        @Override
        public void setStringData(String stringData) {
        }

        @Override
        public String getSystemId() {
            return systemId;
        }

        @Override
        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        @Override
        public String getPublicId() {
            return publicId;
        }

        @Override
        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        @Override
        public String getBaseURI() {
            return baseURI;
        }

        @Override
        public void setBaseURI(String baseURI) {
            this.baseURI = baseURI;
        }

        @Override
        public String getEncoding() {
            return null;
        }

        @Override
        public void setEncoding(String encoding) {
        }

        @Override
        public boolean getCertifiedText() {
            return false;
        }

        @Override
        public void setCertifiedText(boolean certifiedText) {
        }
    }
}
