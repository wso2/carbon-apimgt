/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.apache.synapse.rest;

import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.rest.version.DefaultStrategy;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;

public class APIDispatcherTest extends RESTMediationTestCase {

    private static final String TEST_API = "TestAPI";
    private static final String TEST_API_VERSION = "1.0.0";

    public void testGeneralAPIDispatch() throws Exception {
        API api = new API(TEST_API, "/");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        synCtx = getMessageContext(synapseConfig, false, "/", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        synCtx = getMessageContext(synapseConfig, false, "/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
    }

    public void testBasicAPIDispatch() throws Exception {
        API api = new API(TEST_API, "/test");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        // Messages with '/test' context should be dispatched
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        synCtx = getMessageContext(synapseConfig, false, "/test/", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        synCtx = getMessageContext(synapseConfig, false, "/test?a=5", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        // Messages WITHOUT the '/test' context should NOT be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/foo/test/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        synCtx = getMessageContext(synapseConfig, false, "/test1/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
    }

    public void testResponseDispatch() throws Exception {
        API api = new API(TEST_API, "/test");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        // Messages with '/test' context should ne dispatched
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        synCtx.setResponse(true);
        assertFalse(handler.process(synCtx));

        synCtx.setProperty(RESTConstants.SYNAPSE_REST_API, TEST_API);
        assertTrue(handler.process(synCtx));
    }

    public void testHostBasedAPIDispatch() throws Exception {
        API api = new API(TEST_API, "/test");
        api.setHost("synapse.apache.org");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        // Messages that don't have the proper host set should not be dispatched
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        // Messages with the correct host should be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/test/", "GET");
        addHttpHeader(HTTP.TARGET_HOST, "synapse.apache.org", synCtx);
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        // API should be able to infer the default HTTP port
        api.setPort(80);
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        // Messages with an incorrect port number should not be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=5", "GET");
        addHttpHeader(HTTP.TARGET_HOST, "synapse.apache.org:8280", synCtx);
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        // Messages with the correct port number should be dispatched
        api.setPort(8280);
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        api.setPort(443);
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=5", "GET");
        addHttpHeader(HTTP.TARGET_HOST, "synapse.apache.org", synCtx);
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        // API should accurately infer the default HTTPS port
        synCtx = getMessageContext(synapseConfig, true, "/test/foo/bar?a=5", "GET");
        addHttpHeader(HTTP.TARGET_HOST, "synapse.apache.org", synCtx);
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
    }

    public void testMultipleAPIDispatch() throws Exception {
        String apiName1 = "TestAPI1";
        String apiName2 = "TestAPI2";
        String apiName3 = "TestAPI3";

        API api1 = new API(apiName1, "/test");
        API api2 = new API(apiName2, "/dictionary");
        api2.setHost("synapse.apache.org");
        API api3 = new API(apiName3, "/foo/bar");

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(apiName1, api1);
        synapseConfig.addAPI(apiName2, api2);
        synapseConfig.addAPI(apiName3, api3);

        RESTRequestHandler handler = new RESTRequestHandler();
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertEquals(apiName1, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat", "GET");
        addHttpHeader(HTTP.TARGET_HOST, "synapse.apache.org", synCtx);
        handler.process(synCtx);
        assertEquals(apiName2, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        synCtx = getMessageContext(synapseConfig, false, "/foo/bar/index.jsp?user=test", "GET");
        handler.process(synCtx);
        assertEquals(apiName3, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));

        synCtx = getMessageContext(synapseConfig, false, "/foo/index.jsp?user=test", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
    }

    public void testAPIDefaultVersionBasedDispatch() throws Exception {
        API api = new API(TEST_API, "/test");
        api.setVersionStrategy(new DefaultStrategy(api));
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        // Messages with '/test' context should ne dispatched
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals("",synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        synCtx = getMessageContext(synapseConfig, false, "/test/", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals("",synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals("",synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        synCtx = getMessageContext(synapseConfig, false, "/test?a=5", "GET");
        handler.process(synCtx);
        assertEquals(TEST_API, synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals("",synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        // Messages WITHOUT the '/test' context should NOT be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/foo/test/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test1/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
    }

    public void testAPIURLVersionBasedDispatch() throws Exception {
        API api = new API(TEST_API, "/test");
        api.setVersionStrategy(new URLBasedVersionStrategy(api,TEST_API_VERSION,null));
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        // Messages with '/test' context should NOT be dispatched
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0", "GET");
        handler.process(synCtx);
        assertEquals(api.getName(), synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals(TEST_API_VERSION,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0/", "GET");
        handler.process(synCtx);
        assertEquals(api.getName(), synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals(TEST_API_VERSION,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertEquals(api.getName(), synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals(TEST_API_VERSION,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0?a=5", "GET");
        handler.process(synCtx);
        assertEquals(api.getName(), synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals(TEST_API_VERSION,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
        
        //Message with '/test' context & URL as a Query Parameter should be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0?a=http://localhost.com", "GET");
        handler.process(synCtx);
        assertEquals(api.getName(), synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertEquals(TEST_API_VERSION,synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        // Messages WITHOUT the '/test' context should NOT be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/foo/test/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        // Messages WITHOUT the '/test' context and proper version should NOT be dispatched
        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.1/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test/2.0/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));

        synCtx = getMessageContext(synapseConfig, false, "/test/2.0.0.0/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API));
        assertNull(synCtx.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION));
    }

}