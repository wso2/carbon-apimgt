/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.mediators.transform.url;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * This class represents a URI, fragmented into 7 major components. These components are
 * namely scheme, user info, host, port, path, query and ref. Out of these seven components,
 * port is an integer whereas all other components are strings. These fragments can be
 * combined to form a valid URI according to RFC-2396. This collection strictly deals
 * with URI fragments and not URL fragments. Therefore this abstraction can be used to
 * represent any URI, URL or URN thus allowing room for a wider range of usecases.
 */
public class URIFragments {

    public static final int FULL_URI    = -2;
    public static final int PORT        = -1;

    public static final int PROTOCOL    = 0;
    public static final int USER_INFO   = 1;
    public static final int HOST        = 2;
    public static final int PATH        = 3;
    public static final int QUERY       = 4;
    public static final int REF         = 5;

    private int port = -1;
    // Using an array is lightweight and enables fast lookup through array indexing
    private String[] fragments = new String[6];

    public URIFragments() {

    }

    public URIFragments(URI uri) {
        setFragments(uri);
    }

    /**
     * Break down the given URI into fragments and reinitialize the current
     * fragments set
     *
     * @param uri the URI to be assigned to the fragments
     */
    public void setFragments(URI uri) {
        fragments[PROTOCOL] = uri.getScheme();
        fragments[USER_INFO] = uri.getUserInfo();
        fragments[HOST] = uri.getHost();
        // getPath method returns empty string when a path is not present
        // Better to set 'null' instead
        fragments[PATH] = ("".equals(uri.getPath()) ? null : uri.getPath());
        fragments[QUERY] = uri.getQuery();
        fragments[REF] = uri.getFragment();
        port = uri.getPort();
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public void setStringFragment(int index, String value) {
        fragments[index] = value;
    }

    public String getStringFragment(int index) {
        return fragments[index];
    }

    /**
     * Construct a valid URI by combining the current fragment values
     *
     * @return a valid URI instance
     * @throws URISyntaxException if the fragments form a malformed URI
     */
    public URI toURI() throws URISyntaxException {
        return new URI(
                fragments[PROTOCOL],
                fragments[USER_INFO],
                fragments[HOST],
                port,
                fragments[PATH],
                fragments[QUERY],
                fragments[REF]);
    }

    /**
     * Construct a valid URI string by combining the current fragment values
     *
     * @return a string representation of a valid URI
     * @throws URISyntaxException if the fragments form a malformed URI
     */
    public String toURIString() throws URISyntaxException {
        return toURI().toString();
    }
}
