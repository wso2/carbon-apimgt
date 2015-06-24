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

package org.apache.synapse.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

public final class ClasspathURLStreamHandler extends URLStreamHandler {

    public URLConnection openConnection(URL url) {
        return new URLConnectionImpl(url);
    }

    private static final class URLConnectionImpl extends URLConnection {

        public URLConnectionImpl(URL url) {
            super(url);
        }

        public void connect() {}

        public InputStream getInputStream() throws IOException {
            if (url == null) {
                throw new MalformedURLException("Null or empty classpath URL");
            } else if (url.getHost() != null) {
                throw new MalformedURLException("No host available in classpath URLs");
            }
            InputStream is = ClasspathURLStreamHandler.class.getClassLoader().
                    getResourceAsStream(url.getFile());
            if (is == null) {
                throw new IOException("Classpath resource not found: " + url);
            }
            return is;
        }

        public OutputStream getOutputStream() {
            throw new UnsupportedOperationException();
        }
    }
}