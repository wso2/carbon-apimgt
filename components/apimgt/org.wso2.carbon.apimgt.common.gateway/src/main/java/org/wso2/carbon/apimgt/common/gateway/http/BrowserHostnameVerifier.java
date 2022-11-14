/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.common.gateway.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.util.InetAddressUtils;

import java.security.cert.Certificate;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import javax.naming.InvalidNameException;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

/**
 * Implement the BrowserHostnameVerifier as the apache http-component library deprecated the existing one.
 */
public class BrowserHostnameVerifier implements HostnameVerifier {

    enum HostNameType {

        IPv4(7), IPv6(7), DNS(2);

        final int subjectType;

        HostNameType(final int subjectType) {
            this.subjectType = subjectType;
        }
    }

    private final Log log = LogFactory.getLog(getClass());
    private static final HostnameVerifier defaultHostNameVerifier = new DefaultHostnameVerifier();

    @Override
    public boolean verify(String hostname, SSLSession session) {
        if (defaultHostNameVerifier.verify(hostname, session)) {
            return true;
        }
        final HostNameType hostType = determineHostFormat(hostname);
        if (hostType != HostNameType.DNS) {
            return false;
        }
        final Certificate[] certs;
        try {
            certs = session.getPeerCertificates();
            final X509Certificate x509 = (X509Certificate) certs[0];
            verify(hostname, x509);
            return true;
        } catch (SSLException e) {
            log.debug(e.getMessage(), e);
            return false;
        }
    }

    public void verify(
            final String host, final X509Certificate cert) throws SSLException {
        final List<String> subjectAlts = getSubjectAltNames(cert);
        if (!subjectAlts.isEmpty()) {
            matchDNSName(host, subjectAlts);
        } else {
            // CN matching has been deprecated by rfc2818 and can be used
            // as fallback only when no subjectAlts are available
            final X500Principal subjectPrincipal = cert.getSubjectX500Principal();
            final String cn = extractCN(subjectPrincipal.getName(X500Principal.RFC2253));
            if (cn == null) {
                throw new SSLException("Certificate subject for <" + host + "> doesn't contain " +
                        "a common name and does not have alternative names");
            }
            matchCN(host, cn);
        }
    }

    static void matchDNSName(final String host, final List<String> subjectAlts) throws SSLException {
        final String normalizedHost = host.toLowerCase(Locale.ROOT);
        for (final String subjectAlt : subjectAlts) {
            final String normalizedSubjectAlt = subjectAlt.toLowerCase(Locale.ROOT);
            if (matchIdentity(normalizedHost, normalizedSubjectAlt)) {
                return;
            }
        }
        throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match any " +
                "of the subject alternative names: " + subjectAlts);
    }

    static List<String> getSubjectAltNames(final X509Certificate cert) {
        try {
            final Collection<List<?>> entries = cert.getSubjectAlternativeNames();
            if (entries == null) {
                return Collections.emptyList();
            }
            final List<String> result = new ArrayList<>();
            for (final List<?> entry : entries) {
                final Integer type = entry.size() >= 2 ? (Integer) entry.get(0) : null;
                if (type != null) {
                    if (type == 2) {
                        final Object o = entry.get(1);
                        if (o instanceof String) {
                            result.add((String) o);
                        }
                    }
                }
            }
            return result;
        } catch (final CertificateParsingException ignore) {
            return Collections.emptyList();
        }
    }

    static HostNameType determineHostFormat(final String host) {
        if (InetAddressUtils.isIPv4Address(host)) {
            return HostNameType.IPv4;
        }
        String s = host;
        if (s.startsWith("[") && s.endsWith("]")) {
            s = host.substring(1, host.length() - 1);
        }
        if (InetAddressUtils.isIPv6Address(s)) {
            return HostNameType.IPv6;
        }
        return HostNameType.DNS;
    }

    private static boolean matchIdentity(final String host, final String identity) {

        // RFC 2818, 3.1. Server Identity
        // "...Names may contain the wildcard
        // character * which is considered to match any single domain name
        // component or component fragment..."
        // Based on this statement presuming only singular wildcard is legal
        final int asteriskIdx = identity.indexOf('*');
        if (asteriskIdx != -1) {
            final String prefix = identity.substring(0, asteriskIdx);
            final String suffix = identity.substring(asteriskIdx + 1);
            if (!prefix.isEmpty() && !host.startsWith(prefix)) {
                return false;
            }
            return suffix.isEmpty() || host.endsWith(suffix);
            // Additional sanity checks on content selected by wildcard can be done here
        }
        return host.equalsIgnoreCase(identity);
    }

    static String extractCN(final String subjectPrincipal) throws SSLException {
        if (subjectPrincipal == null) {
            return null;
        }
        try {
            final LdapName subjectDN = new LdapName(subjectPrincipal);
            final List<Rdn> rdns = subjectDN.getRdns();
            for (int i = rdns.size() - 1; i >= 0; i--) {
                final Rdn rds = rdns.get(i);
                final Attributes attributes = rds.toAttributes();
                final Attribute cn = attributes.get("cn");
                if (cn != null) {
                    try {
                        final Object value = cn.get();
                        if (value != null) {
                            return value.toString();
                        }
                    } catch (final NoSuchElementException | NamingException ignore) {
                        // ignore exception
                    }
                }
            }
            return null;
        } catch (final InvalidNameException e) {
            throw new SSLException(subjectPrincipal + " is not a valid X500 distinguished name");
        }
    }

    static void matchCN(final String host, final String cn) throws SSLException {
        final String normalizedHost = host.toLowerCase(Locale.ROOT);
        final String normalizedCn = cn.toLowerCase(Locale.ROOT);
        if (!matchIdentity(normalizedHost, normalizedCn)) {
            throw new SSLPeerUnverifiedException("Certificate for <" + host + "> doesn't match " +
                    "common name of the certificate subject: " + cn);
        }
    }
}
