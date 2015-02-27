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

import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.http.HttpTestEnvironment;
import org.apache.axis2.transport.testkit.tests.Setup;

public class HttpTransportDescriptionFactory implements TransportDescriptionFactory {
    private int port;
    
    @Setup @SuppressWarnings("unused")
    private void setUp(HttpTestEnvironment env) {
        port = env.getServerPort();
    }

    public TransportInDescription createTransportInDescription() throws Exception {
        TransportInDescription desc = new TransportInDescription("http");
        desc.setReceiver(new HttpCoreNIOListener());
        desc.addParameter(new Parameter(HttpCoreNIOListener.PARAM_PORT, String.valueOf(port)));
        return desc;
    }

    public TransportOutDescription createTransportOutDescription() throws Exception {
        TransportOutDescription desc = new TransportOutDescription("http");
        desc.setSender(new HttpCoreNIOSender());
        return desc;
    }
}
