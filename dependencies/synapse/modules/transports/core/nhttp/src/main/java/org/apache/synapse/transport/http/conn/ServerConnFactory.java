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
package org.apache.synapse.transport.http.conn;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpRequestFactory;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.nio.DefaultNHttpServerConnection;
import org.apache.http.nio.reactor.IOSession;
import org.apache.http.nio.reactor.ssl.SSLIOSession;
import org.apache.http.nio.reactor.ssl.SSLMode;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class ServerConnFactory {

    private final HttpRequestFactory requestFactory;
    private final ByteBufferAllocator allocator;
    private final SSLContextDetails ssl;
    private final Map<InetSocketAddress, SSLContextDetails> sslByIPMap;
    private final HttpParams params;
    
    public ServerConnFactory(
            final HttpRequestFactory requestFactory,
            final ByteBufferAllocator allocator,
            final SSLContextDetails ssl,
            final Map<InetSocketAddress, SSLContextDetails> sslByIPMap,
            final HttpParams params) {
        super();
        this.requestFactory = requestFactory != null ? requestFactory : new DefaultHttpRequestFactory();
        this.allocator = allocator != null ? allocator : new HeapByteBufferAllocator();
        this.ssl = ssl;
        this.sslByIPMap = sslByIPMap != null ? new ConcurrentHashMap<InetSocketAddress, SSLContextDetails>(
            sslByIPMap) : null;
        this.params = params != null ? params : new BasicHttpParams();
    }
    
    public ServerConnFactory(
            final SSLContextDetails ssl,
            final Map<InetSocketAddress, SSLContextDetails> sslByIPMap,
            final HttpParams params) {
        this(null, null, ssl, sslByIPMap, params);
    }

    public ServerConnFactory(
            final HttpParams params) {
        this(null, null, null, null, params);
    }

    public DefaultNHttpServerConnection createConnection(final IOSession iosession) {
        SSLContextDetails customSSL = null;
        if (sslByIPMap != null) {
            customSSL = sslByIPMap.get(iosession.getLocalAddress());
        }
        if (customSSL == null) {
            customSSL = ssl;
        }
        IOSession customSession;
        if (customSSL != null) {
            customSession = new SSLIOSession(
                iosession, SSLMode.SERVER, customSSL.getContext(), customSSL.getHandler());
            iosession.setAttribute(SSLIOSession.SESSION_KEY, customSession);
        } else {
            customSession = iosession;
        }
        DefaultNHttpServerConnection conn =  LoggingUtils.createServerConnection(
                customSession, requestFactory, allocator, params);
        int timeout = HttpConnectionParams.getSoTimeout(params);
        conn.setSocketTimeout(timeout);
        return conn;
    }
    
    public Set<InetSocketAddress> getBindAddresses() {
        return sslByIPMap != null ? sslByIPMap.keySet() : Collections.<InetSocketAddress>emptySet();
    }

}
