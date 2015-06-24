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

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.rest.dispatch.URLMappingHelper;

public class URLMappingBasedDispatcherTest extends RESTMediationTestCase {

    private static final String PROP_NAME = "prop.name";
    private static final String PROP_VALUE = "prop.value";

    public void testDefaultResourceDispatch() throws Exception {

        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URLMappingHelper("/"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));
    }

    public void testPathBasedDispatch() throws Exception {

        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URLMappingHelper("/foo/bar/*"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/index.jsp", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=b", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/baz?a=b", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/?a=b", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bars?a=b", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }

    public void testExtensionBasedDispatch() throws Exception {

        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URLMappingHelper("*.jsp"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/index.jsp", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/welcome.jsp", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/index.jsp?a=5&b=10", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/index.html", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }

    public void testExactMatchBasedDispatch() throws Exception {
        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URLMappingHelper("/foo/bar"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=5", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar?a=5&name=test", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/index.html", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }

    public void testMultipleResourceDispatch() throws Exception {
        API api = new API("TestAPI", "/test");

        Resource resource1 = new Resource();
        resource1.setDispatcherHelper(new URLMappingHelper("/foo/*"));
        resource1.setInSequence(getTestSequence(PROP_NAME, "resource1"));

        Resource resource2 = new Resource();
        resource2.setDispatcherHelper(new URLMappingHelper("/foo/bar/*"));
        resource2.setInSequence(getTestSequence(PROP_NAME, "resource2"));

        Resource resource3 = new Resource();
        resource3.setDispatcherHelper(new URLMappingHelper("*.jsp"));
        resource3.setInSequence(getTestSequence(PROP_NAME, "resource3"));

        api.addResource(resource1);
        api.addResource(resource2);
        api.addResource(resource3);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/foo/", "GET");
        handler.process(synCtx);
        assertEquals("resource1", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/index.html?a=5", "GET");
        handler.process(synCtx);
        assertEquals("resource1", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bars", "GET");
        handler.process(synCtx);
        assertEquals("resource1", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/index.jsp", "GET");
        handler.process(synCtx);
        assertEquals("resource1", synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/", "GET");
        handler.process(synCtx);
        assertEquals("resource2", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/index.html?a=5", "GET");
        handler.process(synCtx);
        assertEquals("resource2", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/hello", "GET");
        handler.process(synCtx);
        assertEquals("resource2", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar/index.jsp", "GET");
        handler.process(synCtx);
        assertEquals("resource2", synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/index.jsp", "GET");
        handler.process(synCtx);
        assertEquals("resource3", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/hello/index.jsp?a=5", "GET");
        handler.process(synCtx);
        assertEquals("resource3", synCtx.getProperty(PROP_NAME));
        synCtx = getMessageContext(synapseConfig, false, "/test/foolish/bars/index.jsp", "GET");
        handler.process(synCtx);
        assertEquals("resource3", synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/foolish/index.html", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }

    public void testResponseDispatch() throws Exception {
        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URLMappingHelper("/foo/bar/*"));
        resource.setOutSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/foo/bar", "GET");
        synCtx.setProperty(RESTConstants.SYNAPSE_REST_API, api.getName());
        synCtx.setResponse(true);
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx.setProperty(RESTConstants.SYNAPSE_RESOURCE, resource.getName());
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));
    }
}
