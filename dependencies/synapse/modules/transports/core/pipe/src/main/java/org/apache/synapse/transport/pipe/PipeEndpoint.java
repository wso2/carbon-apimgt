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

import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.ParameterInclude;
import org.apache.axis2.transport.base.ParamUtils;
import org.apache.axis2.transport.base.datagram.DatagramEndpoint;

/**
 * Pipe endpoint description.
 */
public class PipeEndpoint extends DatagramEndpoint {
	private File pipe;
	private Protocol protocol;

	public File getPipe() {
		return pipe;
	}

	public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public boolean loadConfiguration(ParameterInclude params) throws AxisFault {
        String name = ParamUtils.getOptionalParam(params, PipeConstants.NAME_KEY);
        if (name == null) {
            return false;
        }
        pipe = new File(name);
        return super.loadConfiguration(params);
    }

    @Override
	public EndpointReference[] getEndpointReferences(AxisService service, String ip) {
		return new EndpointReference[] { new EndpointReference("pipe://" + pipe.getAbsolutePath()
		        + "?contentType=" + getContentType()) };
	}
}
