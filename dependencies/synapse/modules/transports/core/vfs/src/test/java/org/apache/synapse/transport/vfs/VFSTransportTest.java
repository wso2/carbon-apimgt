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

package org.apache.synapse.transport.vfs;

import java.io.File;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.transport.testkit.ManagedTestSuite;
import org.apache.axis2.transport.testkit.TransportTestSuiteBuilder;
import org.apache.axis2.transport.testkit.axis2.client.AxisAsyncTestClient;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisAsyncEndpoint;
import org.apache.axis2.transport.testkit.axis2.endpoint.AxisEchoEndpoint;
import org.apache.axis2.transport.testkit.axis2.endpoint.ContentTypeServiceConfigurator;

/**
 * TransportListenerTestTemplate implementation for the VFS transport.
 */
public class VFSTransportTest extends TestCase {
    public static TestSuite suite() throws Exception {
        // TODO: the VFS listener doesn't like reuseResources == true...
        ManagedTestSuite suite = new ManagedTestSuite(VFSTransportTest.class, false);
        
        // Since VFS has no Content-Type header, SwA is not supported.
        suite.addExclude("(test=AsyncSwA)");
        
        TransportTestSuiteBuilder builder = new TransportTestSuiteBuilder(suite);
        
        ContentTypeServiceConfigurator cfgtr = new ContentTypeServiceConfigurator("transport.vfs.ContentType");
        
        builder.addEnvironment(new VFSTestEnvironment(new File("target/vfs3")),
                new VFSTransportDescriptionFactory());
        
        builder.addAsyncChannel(new VFSAsyncFileChannel("req/in"));
        
        builder.addAxisAsyncTestClient(new AxisAsyncTestClient());
        builder.addByteArrayAsyncTestClient(new VFSAsyncClient());
        
        builder.addAxisAsyncEndpoint(new AxisAsyncEndpoint(), cfgtr);
        builder.addByteArrayAsyncEndpoint(new VFSMockAsyncEndpoint());
        
        builder.addRequestResponseChannel(new VFSRequestResponseFileChannel("req/in", "req/out"));
        
        builder.addByteArrayRequestResponseTestClient(new VFSRequestResponseClient());
        
        builder.addEchoEndpoint(new AxisEchoEndpoint(), cfgtr);
        
        builder.build();
        
//        suite.addTest(new MinConcurrencyTest(server, new AsyncChannel[] { new VFSFileChannel("req/in1"), new VFSFileChannel("req/in2") }, 1, true, env, tdf));
        return suite;
    }
}
