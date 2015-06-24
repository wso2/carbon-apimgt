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

import org.apache.axiom.om.OMElement;
import org.apache.synapse.ServerManager;
import org.apache.synapse.ServerConfigurationInformation;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import samples.util.SampleAxis2Server;
import samples.util.SampleAxis2ServerManager;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public abstract class AbstractAutomationTestCase extends XMLTestCase {

    private ServerManager serverManager;

    protected void setUp() throws java.lang.Exception {
        super.setUp();
        SampleAxis2Server.main(new String[]{"-repo", "modules/samples/target/test_repos/axis2Server/",
                    "-conf", "modules/samples/target/test_repos/axis2Server/conf/axis2.xml"});
        System.setProperty("repository", "modules/samples/target/test_repos/axis2Client");
        setUpSynapseEnv();
        setUpNSContext();
    }

    protected void setUpSynapseEnv() {
        System.setProperty("port", "8280");
        System.setProperty("org.apache.xerces.xni.parser.XMLParserConfiguration",
                "org.apache.xerces.parsers.XMLGrammarCachingConfiguration");
        System.setProperty("axis2.xml", "modules/samples/target/test_repos/synapse/conf/axis2.xml");
        ServerConfigurationInformation information = new ServerConfigurationInformation();
        information.setAxis2RepoLocation(SYNAPSE_REPO);
        serverManager = new ServerManager();
        serverManager.init(information, null);
        serverManager.start();
    }

    protected void setUpNSContext() {
        Map<String, String> m = new HashMap<String, String>();
        m.put("ms", "http://services.samples");
        m.put("ns", "http://services.samples");
        NamespaceContext nsCtx = new SimpleNamespaceContext(m);
        XMLUnit.setXpathNamespaceContext(nsCtx);
    }

    protected String getStringResultOfTest(OMElement elem) throws Exception {
        OutputStream os = new ByteArrayOutputStream();
        elem.serialize(os);
        return os.toString();
    }

    protected void startCustomAxis2Server(String httpPort, String httpsPort) throws Exception {
        System.setProperty("http_port", httpPort);
        System.setProperty("https_port", httpsPort);
        SampleAxis2ServerManager.getInstance().start(new String[]{"-repo",
                "modules/samples/target/test_repos/axis2Server/",
                    "-conf", "modules/samples/target/test_repos/axis2Server/conf/axis2.xml"});
    }
    protected void stopCustomAxis2Server(String httpPort, String httpsPort) throws Exception {
        SampleAxis2ServerManager.getInstance().stop();
    }

    protected void tearDown() throws Exception {
        serverManager.stop();
        super.tearDown();
    }

    protected final String SYNAPSE_REPO = "modules/samples/target/test_repos/synapse";
    protected final String SAMPLE_CONFIG_ROOT_PATH = "repository/conf/sample/";
    protected final String SYNAPSE_BASE_URL = "http://localhost:8280/";
}
