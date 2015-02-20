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

package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import org.apache.axis2.AxisFault;
import samples.userguide.StockQuoteClient;

/**
 *
 */
public class SynapseSample_5_Integration extends AbstractAutomationTestCase {

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_5.xml");
        System.setProperty("addurl", "http://localhost:9000/services/SimpleStockQuoteService");
        System.setProperty("trpurl", SYNAPSE_BASE_URL);
        super.setUp();
    }

    public void testSample() throws Exception {
        System.setProperty("symbol", "MSFT");
        try {
            getStringResultOfTest(StockQuoteClient.executeTestClient());
        } catch (AxisFault f) {
            assertEquals("java.net.UnknownHostException: bogus", f.getReason());
        }

        System.setProperty("symbol", "SUN");
        try {
            getStringResultOfTest(StockQuoteClient.executeTestClient());
        } catch (AxisFault f) {
            assertEquals("java.net.ConnectException: Connection refused", f.getReason());
        }
    }
}
