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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.transport.testkit.ManagedTestSuite;
import org.apache.axis2.transport.testkit.axis2.TransportDescriptionFactory;
import org.apache.axis2.transport.testkit.http.HttpTransportTestSuiteBuilder;

public class HttpCoreNIOListenerTest extends TestCase {
    public static TestSuite suite() throws Exception {
        ManagedTestSuite suite = new ManagedTestSuite(HttpCoreNIOListenerTest.class);
        
        // These tests don't work because of a problem similar to SYNAPSE-418
        suite.addExclude("(test=EchoXML)");
        
        TransportDescriptionFactory tdfNIO = new HttpTransportDescriptionFactory();
        
        HttpTransportTestSuiteBuilder builder = new HttpTransportTestSuiteBuilder(suite, tdfNIO);
        
        builder.addAxisTestClientConfigurator(new HttpAxisTestClientConfigurator(false));
        builder.addAxisTestClientConfigurator(new HttpAxisTestClientConfigurator(true));
        
        //builder.build();
        
        return suite;
    }
}
