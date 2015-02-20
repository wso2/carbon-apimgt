package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import org.apache.axis2.AxisFault;
import samples.userguide.StockQuoteClient;

/**
 *
 */
public class SynapseSample_102_Integration extends AbstractAutomationTestCase {

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_102.xml");
        super.setUp();
    }

    public void testSample() throws Exception {
        System.setProperty("trpurl", "http://localhost:8280/services/StockQuoteProxy");
        try {
            getStringResultOfTest(StockQuoteClient.executeTestClient());
        } catch (AxisFault f) {
            assertEquals("The service cannot be found for the endpoint reference (EPR) " +
                    "/services/StockQuoteProxy", f.getReason());
        }

        System.setProperty("trpurl", "https://localhost:8243/services/StockQuoteProxy");
        String resultString = getStringResultOfTest(StockQuoteClient.executeTestClient());
        assertXpathExists("ns:getQuoteResponse", resultString);
        assertXpathExists("ns:getQuoteResponse/ns:return", resultString);
    }
}
