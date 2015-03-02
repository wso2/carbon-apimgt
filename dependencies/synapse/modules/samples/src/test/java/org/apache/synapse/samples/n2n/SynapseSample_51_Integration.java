package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import org.apache.axiom.om.OMElement;
import samples.userguide.MTOMSwAClient;

/**
 *
 */
public class SynapseSample_51_Integration extends AbstractAutomationTestCase {

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_51.xml");
        super.setUp();
    }

    public void testSample() throws Exception {
        System.setProperty("opt_mode", "mtom");
        OMElement response = MTOMSwAClient.sendUsingMTOM(
                "./../../repository/conf/sample/resources/mtom/asf-logo.gif", "http://localhost:8280/services/MTOMSwASampleService");
//        assertXpathExists("ns:getQuoteResponse", resultString);
//        assertXpathExists("ns:getQuoteResponse/ns:return", resultString);
    }
}
