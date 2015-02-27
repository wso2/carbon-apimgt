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

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.ContentEncoder;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.synapse.transport.nhttp.Axis2HttpRequest;
import org.apache.synapse.transport.nhttp.ClientHandler;

/**
 * Decorator class intended to transparently extend an {@link NHttpClientEventHandler}
 * with basic event logging capabilities using Commons Logging.
 */
class LoggingNHttpClientHandler implements NHttpClientEventHandler {

    private final Log log;
    private final NHttpClientEventHandler handler;

    public LoggingNHttpClientHandler(
            final Log log,
            final NHttpClientEventHandler handler) {
        super();
        if (handler == null) {
            throw new IllegalArgumentException("HTTP client handler may not be null");
        }
        this.handler = handler;
        this.log = LogFactory.getLog(handler.getClass());
    }

    public void connected(
            final NHttpClientConnection conn,
            final Object attachment) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Connected (" + attachment + ")");
        }
        this.handler.connected(conn, attachment);
    }

    public void closed(final NHttpClientConnection conn) {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Closed");
        }
        this.handler.closed(conn);
    }

    public void exception(final NHttpClientConnection conn, final Exception ex) {
        this.log.error(conn + ": " + ex.getMessage(), ex);
        this.handler.exception(conn, ex);
    }

    public void requestReady(
            final NHttpClientConnection conn) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Request ready" + getRequestMessageID(conn));
        }
        this.handler.requestReady(conn);
    }

    public void outputReady(
            final NHttpClientConnection conn,
            final ContentEncoder encoder) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Output ready" + getRequestMessageID(conn));
        }
        this.handler.outputReady(conn, encoder);
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Content encoder " + encoder);
        }
    }

    public void responseReceived(
            final NHttpClientConnection conn) throws IOException, HttpException {
        HttpResponse response = conn.getHttpResponse();
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": "
                    + response.getStatusLine() + getRequestMessageID(conn));
        }
        this.handler.responseReceived(conn);
    }

    public void inputReady(
            final NHttpClientConnection conn,
            final ContentDecoder decoder) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Input ready" + getRequestMessageID(conn));
        }
        this.handler.inputReady(conn, decoder);
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Content decoder " + decoder);
        }
    }

    public void timeout(
            final NHttpClientConnection conn) throws IOException, HttpException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": Timeout" + getRequestMessageID(conn));
        }
        this.handler.timeout(conn);
    }

    public void endOfInput(
            final NHttpClientConnection conn) throws IOException {
        if (this.log.isDebugEnabled()) {
            this.log.debug(conn + ": End of input" + getRequestMessageID(conn));
        }
        this.handler.endOfInput(conn);
    }

    private static String getRequestMessageID(final NHttpClientConnection conn) {
        Axis2HttpRequest axis2Request = (Axis2HttpRequest)
                conn.getContext().getAttribute(ClientHandler.AXIS2_HTTP_REQUEST);
        if (axis2Request != null) {
            return " [Request Message ID : " + axis2Request.getMsgContext().getMessageID() + "]";
        }
        return "";
    }

}