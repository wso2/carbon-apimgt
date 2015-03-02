package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import org.apache.axis2.AxisFault;
import samples.userguide.StockQuoteClient;

/**
 *
 */
public class SynapseSample_600_Integration extends AbstractAutomationTestCase {

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_600.xml");
        System.setProperty("addurl", SYNAPSE_BASE_URL);
        System.setProperty("mode", "quote");
        System.setProperty("symbol", "IBM");
        super.setUp();
    }

    public void testSample() throws Exception {
        getStringResultOfTest(StockQuoteClient.executeTestClient());
        getStringResultOfTest(StockQuoteClient.executeTestClient());
        getStringResultOfTest(StockQuoteClient.executeTestClient());
        String resultString = getStringResultOfTest(StockQuoteClient.executeTestClient());
        assertXpathExists("ns:getQuoteResponse", resultString);
        assertXpathExists("ns:getQuoteResponse/ns:return", resultString);

        try {
            getStringResultOfTest(StockQuoteClient.executeTestClient());
        } catch (AxisFault f) {
            assertEquals("**Access Denied**", f.getReason());
        }
    }
}
