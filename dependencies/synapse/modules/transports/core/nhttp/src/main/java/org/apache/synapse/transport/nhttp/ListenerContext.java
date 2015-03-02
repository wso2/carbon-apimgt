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

package org.apache.synapse.transport.nhttp;

import java.net.InetAddress;

import org.apache.axis2.description.TransportInDescription;
import org.apache.synapse.commons.evaluators.Parser;
import org.apache.synapse.commons.executors.PriorityExecutor;

/**
 * This class is being used to hold the different runtime objects used by the Listeners
 */
public class ListenerContext {
    
    /** The Axis2 Transport In Description for the transport */
    private final TransportInDescription transportIn;
    /** This will execute the requests based on calculate priority */
    private final PriorityExecutor executor;
    /** parser for calculating the priority of incoming messages */
    private final Parser parser;
    /** if false we won't dispatch to axis2 service in case of rest scenarios */
    private final boolean restDispatching;
    /** WSDL processor for Get requests*/
    private final HttpGetRequestProcessor httpGetRequestProcessor;
    /** The hostname */
    private final String hostname;
    /** The port to listen on, defaults to 8280 */
    private final int port;
    /** address to bind to */
    private final InetAddress bindAddress;
    
    ListenerContext(
            final TransportInDescription transportIn,
            final PriorityExecutor executor,
            final Parser parser,
            final boolean restDispatching,
            final HttpGetRequestProcessor httpGetRequestProcessor,
            final String hostname,
            final int port,
            final InetAddress bindAddress) {
        this.transportIn = transportIn;
        this.executor = executor;
        this.parser = parser;
        this.restDispatching = restDispatching;
        this.httpGetRequestProcessor = httpGetRequestProcessor;
        this.hostname = hostname;
        this.port = port;
        this.bindAddress = bindAddress;
    }

    public TransportInDescription getTransportIn() {
        return transportIn;
    }

    public PriorityExecutor getExecutor() {
        return executor;
    }

    public Parser getParser() {
        return parser;
    }

    public boolean isRestDispatching() {
        return restDispatching;
    }

    public HttpGetRequestProcessor getHttpGetRequestProcessor() {
        return httpGetRequestProcessor;
    }

    public int getPort() {
        return port;
    }

    public String getHostname() {
        return hostname;
    }

    public InetAddress getBindAddress() {
        return bindAddress;
    }

}