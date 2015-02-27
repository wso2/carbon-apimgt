package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import samples.userguide.StockQuoteClient;

/**
 *
 */
public class SynapseSample_112_Integration extends AbstractAutomationTestCase {

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_1.xml");
        System.setProperty("addurl", "http://localhost:9000/services/SimpleStockQuoteService");
        System.setProperty("mode", "placeorder");
        super.setUp();
    }

    public void testSample() throws Exception {
        StockQuoteClient.executeTestClient();
        // todo: how to test this (fire and forget)
    }
}
