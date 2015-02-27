/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.synapse.transport.nhttp;

import java.io.IOException;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.DefaultNHttpClientConnection;
import org.apache.http.impl.nio.reactor.AbstractIODispatch;
import org.apache.http.nio.NHttpClientEventHandler;
import org.apache.http.nio.reactor.IOSession;
import org.apache.synapse.transport.http.conn.ClientConnFactory;
import org.apache.synapse.transport.http.conn.LoggingUtils;

class ClientIODispatch extends AbstractIODispatch<DefaultNHttpClientConnection> {

    private final NHttpClientEventHandler handler;
    private final ClientConnFactory connFactory;

    public ClientIODispatch(
            final NHttpClientEventHandler handler,
            final ClientConnFactory connFactory) {
        super();
        this.handler = LoggingUtils.decorate(handler);
        this.connFactory = connFactory;
    }

    @Override
    protected DefaultNHttpClientConnection createConnection(final IOSession session) {
        Axis2HttpRequest axis2Req = (Axis2HttpRequest) session.getAttribute(IOSession.ATTACHMENT_KEY);
        HttpRoute route = axis2Req.getRoute();
        return this.connFactory.createConnection(session, route);
    }

    @Override
    protected void onConnected(final DefaultNHttpClientConnection conn) {
        Axis2HttpRequest axis2Req = (Axis2HttpRequest) conn.getContext().getAttribute(IOSession.ATTACHMENT_KEY);
        try {
            this.handler.connected(conn, axis2Req);
        } catch (final Exception ex) {
            this.handler.exception(conn, ex);
        }
    }

    @Override
    protected void onClosed(final DefaultNHttpClientConnection conn) {
        this.handler.closed(conn);
    }

    @Override
    protected void onException(final DefaultNHttpClientConnection conn, final IOException ex) {
        this.handler.exception(conn, ex);
    }

    @Override
    protected void onInputReady(final DefaultNHttpClientConnection conn) {
        conn.consumeInput(this.handler);
    }

    @Override
    protected void onOutputReady(final DefaultNHttpClientConnection conn) {
        conn.produceOutput(this.handler);
    }

    @Override
    protected void onTimeout(final DefaultNHttpClientConnection conn) {
        try {
            this.handler.timeout(conn);
        } catch (final Exception ex) {
            this.handler.exception(conn, ex);
        }
    }

}
