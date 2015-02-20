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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.nio.NHttpServerConnection;
import org.apache.synapse.transport.passthru.SourceContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Keeps track of the connections coming in to the transport.
 */
public class SourceConnections {
    private static Log log = LogFactory.getLog(SourceConnections.class);

    /** The pool of connections in use */
    private volatile List<NHttpServerConnection> busyConnections = new ArrayList<NHttpServerConnection>();

    /** The pool of connections that are not being used */
    private volatile List<NHttpServerConnection> freeConnections = new ArrayList<NHttpServerConnection>();

    /** Lock for synchronizing the access to the pools */
    private final Lock lock = new ReentrantLock();

    /**
     * Add a connection to the pool.
     *
     * @param conn connection to be added
     */
    public void addConnection(NHttpServerConnection conn) {
        lock.lock();
        try {
            freeConnections.add(conn);
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method should be called when ever a connection being used for processing
     * a request-response.
     *
     * @param conn the connection to be used
     */
    public void useConnection(NHttpServerConnection conn) {
        lock.lock();
        try {
            boolean freeConnection = freeConnections.remove(conn);
            if (freeConnection) {
                busyConnections.add(conn);
            } else {
                if (busyConnections.contains(conn)) {
                    throw new IllegalStateException("The connection is busy. " +
                            "Cannot use it for new request");
                } else {
                    throw new IllegalStateException("Trying to use a connection " +
                            "which is not in free connections " + conn);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * This method should be called after a connection is being used for a request-response.
     *
     * @param conn the connection being used
     */
    public void releaseConnection(NHttpServerConnection conn) {
        lock.lock();
        try {
            SourceContext.get(conn).reset();

            if (busyConnections.remove(conn)) {
                freeConnections.add(conn);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Trying to finish using a connection " +
                            "which is not in busy connections " + conn);
                }
            }
        } finally {
            lock.unlock();
        }
    }

	/**
	 * Shutdown a connection
	 * 
	 * @param conn the connection that needs to be shut down
	 */
	public void shutDownConnection(NHttpServerConnection conn) {
		shutDownConnection(conn, false);
	}
    
    /**
     * Shutdown a connection
     *
     * @param conn the connection that needs to be shut down
     * @param isError whether an error is causing this shutdown of the connection
     *                It is very important to set this flag correctly.
     *                When an error causing the shutdown of the connections we should not
     *                release associated writer buffer to the pool as it might lead into
     *                situations like same buffer is getting released to both source and target
     *                buffer factories
     */
    public void shutDownConnection(NHttpServerConnection conn, boolean isError) {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down connection forcefully " + conn);
        }
        lock.lock();
        try {
            SourceContext.get(conn).reset(isError);

            if (!busyConnections.remove(conn)) {
                freeConnections.remove(conn);
            }

            try {
                conn.shutdown();
            } catch (IOException ignored) {
            }
        } finally {
            lock.unlock();
        }
    }


    /**
     * Close a connection gracefully.
     *
     * @param conn the connection that needs to be closed.
     */
    public void closeConnection(NHttpServerConnection conn) {
        if (log.isDebugEnabled()) {
            log.debug("Shutting down connection forcefully " + conn);
        }
        lock.lock();
        try {
            SourceContext.get(conn).reset();

            if (!busyConnections.remove(conn)) {
                freeConnections.remove(conn);
            }

            try {
                conn.close();
            } catch (IOException ignored) {
            }
        } finally {
            lock.unlock();
        }
    }

    public void destroy() {
        for (NHttpServerConnection conn : freeConnections) {
            shutDownConnection(conn);
        }

        // for all the busy connections we have to notify that their cannot
        // be anymore requests over them
        for (NHttpServerConnection conn : busyConnections) {
            SourceContext.get(conn).setShutDown(true);
        }
    }
}
