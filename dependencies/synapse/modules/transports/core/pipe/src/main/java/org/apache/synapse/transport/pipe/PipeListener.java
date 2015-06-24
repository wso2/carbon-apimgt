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

import java.io.IOException;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.base.ManagementSupport;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.datagram.AbstractDatagramTransportListener;
import org.apache.axis2.transport.base.datagram.DatagramDispatcherCallback;

/**
 * Transport listener for UNIX pipes.
 * <p>
 * The pipe transport listener can be enabled in the Axis configuration as follows:
 * <pre>
 * &lt;transportReceiver name="pipe" class="org.apache.synapse.transport.pipe.PipeListener">
 *   &lt;parameter name="protocol">...&lt;/parameter>
 *   <em>other protocol specific parameters</em>
 * &lt;/transportReceiver></pre>
 * A {@link Protocol} implementation must be specified using the <tt>protocol</tt>
 * parameter. This determines how the stream is decomposed into individual messages.
 * The configuration must be completed by other parameters as required by the
 * protocol implementation.
 * <p>
 * In addition, services accepting messages using this transport must be configured with the
 * following parameters:
 * <dl>
 *   <dt>transport.pipe.name</dt>
 *   <dd>The name (path) of the UNIX pipe to read from (required).</dd>
 *   <dt>transport.pipe.contentType</dt>
 *   <dd>The content type of the messages received (required). This setting
 *       is used to select the appropriate message builder.</dd>
 * </dl>
 */
public class PipeListener extends AbstractDatagramTransportListener<PipeEndpoint> implements ManagementSupport {
    private Protocol protocol;
    
    @Override
    protected void doInit() throws AxisFault {
        TransportInDescription transportIn = getTransportInDescription();
        String protocolClassName = ParamUtils.getRequiredParam(transportIn, "protocol");
        Class<? extends Protocol> protocolClass;
        try {
            protocolClass = Thread.currentThread().getContextClassLoader().loadClass(protocolClassName).asSubclass(Protocol.class);
        } catch (ClassNotFoundException ex) {
            throw new AxisFault("Unable to load the protocol implementation '" + protocolClassName + "'");
        } catch (ClassCastException ex) {
            throw new AxisFault("The protocol implementation " + protocolClassName + " doesn't extend " + Protocol.class.getName());
        }
        Protocol protocol;
        try {
            protocol = protocolClass.newInstance();
        } catch (Exception ex) {
            throw new AxisFault("Couldn't instantiate " + protocolClassName);
        }
        protocol.init(transportIn);
        this.protocol = protocol;
    }

    @Override
    protected PipeDispatcher createDispatcher(DatagramDispatcherCallback callback) throws IOException {
        return new PipeDispatcher(callback);
    }

    @Override
    protected PipeEndpoint doCreateEndpoint() {
        PipeEndpoint endpoint = new PipeEndpoint();
        endpoint.setProtocol(protocol);
        return endpoint;
    }
}
