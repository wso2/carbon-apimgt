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

package org.apache.synapse.transport.passthru;

import org.apache.http.nio.NHttpConnection;
import org.apache.synapse.transport.passthru.config.SourceConfiguration;

import java.nio.ByteBuffer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class represents the information about a TCP Connection at a given point in time.
 * In a Single TCP Connection there can be multiple HTTP Requests.
 */
public class SourceContext {
    public static final String CONNECTION_INFORMATION = "CONNECTION_INFORMATION";

    private SourceConfiguration sourceConfiguration;

    private ProtocolState state = ProtocolState.REQUEST_READY;

    private SourceRequest request;

    private SourceResponse response;

    /** Mark the connection to be shut down after the current request-response is completed. */
    private boolean shutDown = false;

    private Pipe reader;

    private Pipe writer;

    private Lock lock = new ReentrantLock();

    public SourceContext(SourceConfiguration sourceConfiguration) {
        this.sourceConfiguration = sourceConfiguration;
    }

    public ProtocolState getState() {
        return state;
    }

    public void setState(ProtocolState state) {
        this.state = state;
    }

    public SourceRequest getRequest() {
        return request;
    }

    public void setRequest(SourceRequest request) {
        this.request = request;
    }

    public SourceResponse getResponse() {
        return response;
    }

    public void setResponse(SourceResponse response) {
        this.response = response;
    }

    /**
     * Reset the resources associated with this context
     */
    public void reset() {
		reset(false);
	}

    /**
     * Reset the resources associated with this context
     *
     * @param isError whether an error is causing this shutdown of the connection.
     *                It is very important to set this flag correctly.
     *                When an error causing the shutdown of the connections we should not
     *                release associated writer buffer to the pool as it might lead into
     *                situations like same buffer is getting released to both source and target
     *                buffer factories
     */
    public void reset(boolean isError) {
		this.request = null;
		this.response = null;
		this.state = ProtocolState.REQUEST_READY;

		if (writer != null) {
			if (!isError) {      // If there is an error we do not release the buffer to the factory
                ByteBuffer buffer = writer.getBuffer();
				sourceConfiguration.getBufferFactory().release(buffer);
			}
		}

		this.reader = null;
		this.writer = null;
	}

    public Lock getLock() {
        return lock;
    }

    public boolean isShutDown() {
        return shutDown;
    }

    public void setShutDown(boolean shutDown) {
        this.shutDown = shutDown;
    }

    public Pipe getReader() {
        return reader;
    }

    public void setReader(Pipe reader) {
        this.reader = reader;
    }

    public Pipe getWriter() {
        return writer;
    }

    public void setWriter(Pipe writer) {
        this.writer = writer;
    }

    public static void create(NHttpConnection conn, ProtocolState state,
                              SourceConfiguration configuration) {
        SourceContext info = new SourceContext(configuration);

        conn.getContext().setAttribute(CONNECTION_INFORMATION, info);

        info.setState(state);
    }

    public static void updateState(NHttpConnection conn, ProtocolState state) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setState(state);
        }  else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static boolean assertState(NHttpConnection conn, ProtocolState state) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null && info.getState() == state;

    }

    public static ProtocolState getState(NHttpConnection conn) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getState() : null;
    }

    public static void setRequest(NHttpConnection conn, SourceRequest request) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setRequest(request);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static void setResponse(NHttpConnection conn, SourceResponse response) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        if (info != null) {
            info.setResponse(response);
        } else {
            throw new IllegalStateException("Connection information should be present");
        }
    }

    public static SourceRequest getRequest(NHttpConnection conn) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getRequest() : null;
    }

    public static SourceResponse getResponse(NHttpConnection conn) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getResponse() : null;
    }

    public static SourceContext get(NHttpConnection conn) {
        return (SourceContext) conn.getContext().getAttribute(CONNECTION_INFORMATION);
    }

    public static Lock getLock(NHttpConnection conn) {
        SourceContext info = (SourceContext)
                conn.getContext().getAttribute(CONNECTION_INFORMATION);

        return info != null ? info.getLock() : null;
    }
}
