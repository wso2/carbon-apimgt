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

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.ParameterInclude;

/**
 * Datagram stream protocol implementation.
 * <p>
 * This interface is used to define protocols that encapsulate sequences of
 * datagrams into streams.
 * <p>
 * Note that implementations of this interface are supposed to be stateless.
 * The decoding is handled by {@link ProtocolDecoder} implementations which
 * are statefull.
 */
public interface Protocol {
    /**
     * Initialize this protocol implementation using a given set of
     * parameters.
     * 
     * @param paramInclude the set of parameters to use
     * @throws AxisFault if the protocol implementation failed to initialize
     */
    void init(ParameterInclude paramInclude) throws AxisFault;
    
    /**
     * Create a new protocol decoder for this protocol implementation.
     * 
     * @return the protocol decoder
     */
    ProtocolDecoder createProtocolDecoder();
}
