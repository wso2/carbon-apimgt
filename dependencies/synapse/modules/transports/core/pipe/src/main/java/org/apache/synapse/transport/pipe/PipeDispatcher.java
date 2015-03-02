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
package org.apache.synapse.transport.pipe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Map;

import org.apache.axis2.transport.base.datagram.DatagramDispatcher;
import org.apache.axis2.transport.base.datagram.DatagramDispatcherCallback;

/**
 * {@link DatagramDispatcher} implementation for the pipe transport.
 */
public class PipeDispatcher implements DatagramDispatcher<PipeEndpoint> {
    private final DatagramDispatcherCallback callback;
    private final Map<PipeEndpoint,PipeEndpointListener> endpointListeners = new IdentityHashMap<PipeEndpoint,PipeEndpointListener>();
    
    public PipeDispatcher(DatagramDispatcherCallback callback) {
        this.callback = callback;
    }

	public void addEndpoint(PipeEndpoint endpoint) throws IOException {
	    File pipe = endpoint.getPipe();
	    if (!pipe.exists()) {
	        throw new FileNotFoundException(pipe.getAbsolutePath() + " not found");
	    }
	    if (pipe.isDirectory() || pipe.isFile()) {
	        throw new IOException(pipe.getAbsolutePath() + " is not a pipe");
	    }
		PipeEndpointListener listener = new PipeEndpointListener(endpoint, callback);
		new Thread(listener, "pipe:" + pipe.getAbsolutePath()).start();
		endpointListeners.put(endpoint, listener);
	}

	public void removeEndpoint(PipeEndpoint endpoint) throws IOException {
	    endpointListeners.get(endpoint).stop();
	    endpointListeners.remove(endpoint);
	}

	public void stop() throws IOException {
	    // TODO: this should not be necessary (see SYNAPSE-288)
	    while (!endpointListeners.isEmpty()) {
	        removeEndpoint(endpointListeners.keySet().iterator().next());
	    }
	}
}
