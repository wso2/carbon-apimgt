package org.apache.synapse.samples.n2n;

import org.apache.synapse.SynapseConstants;
import org.apache.axis2.engine.ListenerManager;
import samples.userguide.LoadbalanceFailoverClient;

/**
 *
 */
public class SynapseSample_54_Integration extends AbstractAutomationTestCase {

    ListenerManager listenerManager = null;

    protected void setUp() throws Exception {
        System.setProperty(SynapseConstants.SYNAPSE_XML, SAMPLE_CONFIG_ROOT_PATH + "synapse_sample_54.xml");
        System.setProperty("addurl", SYNAPSE_BASE_URL);
        System.setProperty("symbol", "IBM");
        System.setProperty("mode", "quote");
        System.setProperty("repository", "modules/samples/target/test_repos/axis2Client");
        setUpNSContext();
        System.setProperty("test_mode", "true");
        System.setProperty("i", "3");
    }

    public void testSample() throws Exception {
        setUpSynapseEnv();
        startCustomAxis2Server("9001", "9005");
        startCustomAxis2Server("9002", "9006");
        startCustomAxis2Server("9003", "9007");
        String resultString = (new LoadbalanceFailoverClient()).sessionlessClient();
        assertTrue(resultString.contains("9001"));
        assertTrue(resultString.contains("9002"));
        assertTrue(resultString.contains("9003"));        
        resultString = (new LoadbalanceFailoverClient()).sessionlessClient();
        assertTrue(resultString.contains("9001"));
        assertTrue(resultString.contains("9002"));
        assertFalse(resultString.contains("9003"));
    }
}
