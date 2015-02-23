package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import samples.userguide.StockQuoteClient;

/**
 *
 */
public class SynapseSample_503_Integration extends AbstractAutomationTestCase {

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_503.xml");
        System.setProperty("addurl", "http://localhost:9000/services/SimpleStockQuoteService");
        System.setProperty("trpurl", SYNAPSE_BASE_URL);
        System.setProperty("mode", "customquote");
        super.setUp();
    }

    public void testSample() throws Exception {
        String resultString = getStringResultOfTest(StockQuoteClient.executeTestClient());
        assertXpathExists("ms:CheckPriceResponse", resultString);
        assertXpathExists("ms:CheckPriceResponse/ms:Price", resultString);
    }
}
