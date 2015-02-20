/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.synapse.transport.passthru.connections;

import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.nio.NHttpClientConnection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This stores connections for a particular host + port.
 */
public class HostConnections {
    private static final Log log = LogFactory.getLog(HostConnections.class);
    // route
    private final HttpRoute route;
    // maximum number of connections allowed for this host + port
    private final int maxSize;
    // number of awaiting connections
    private int pendingConnections;
    // list of free connections available
    private List<NHttpClientConnection> freeConnections = new ArrayList<NHttpClientConnection>();
    // list of connections in use
    private List<NHttpClientConnection> busyConnections = new ArrayList<NHttpClientConnection>();

    private Lock lock = new ReentrantLock();

    public HostConnections(HttpRoute route, int maxSize) {
        if (log.isDebugEnabled()) {
            log.debug("Creating new connection pool: " + route);
        }
        this.route = route;
        this.maxSize = maxSize;
    }

    /**
     * Get a connection for the host:port
     *
     * @return a connection
     */
    public NHttpClientConnection getConnection() {
        lock.lock();
        try {
            if (freeConnections.size() > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Returning an existing free connection " + route);
                }
                NHttpClientConnection conn = freeConnections.get(0);
                freeConnections.remove(conn);
                busyConnections.add(conn);
                return conn;
            }
        } finally {
            lock.unlock();
        }
        return null;
    }

    public void release(NHttpClientConnection conn) {
        conn.getMetrics().reset();
        HttpContext ctx = conn.getContext();
        ctx.removeAttribute(ExecutionContext.HTTP_REQUEST);
        ctx.removeAttribute(ExecutionContext.HTTP_RESPONSE);

        lock.lock();
        try {
            if (busyConnections.remove(conn)) {
                freeConnections.add(conn);
            } else {
                log.error("Attempted to releaseConnection connection not in the busy list");
            }
        } finally {
            lock.unlock();
        }
    }

    public void forget(NHttpClientConnection conn) {
        lock.lock();
        try {
            if (!freeConnections.remove(conn)) {
                busyConnections.remove(conn);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addConnection(NHttpClientConnection conn) {
        if (log.isDebugEnabled()) {
            log.debug("New connection " + route + " is added to the free list");
        }
        lock.lock();
        try {
            freeConnections.add(conn);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates that a connection has been successfully established with a remote server
     * as notified by the session request call back.
     */
    public synchronized void pendingConnectionSucceeded() {
        lock.lock();
        try {
            pendingConnections--;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Keep track of the number of times connections to this host:port has failed
     * consecutively
     */
    public void pendingConnectionFailed() {
        lock.lock();
        try {
            pendingConnections--;
        } finally {
            lock.unlock();
        }
    }    

    public HttpRoute getRoute() {
        return route;
    }

    public boolean canHaveMoreConnections() {
        return busyConnections.size() + pendingConnections < maxSize;
    }
}
