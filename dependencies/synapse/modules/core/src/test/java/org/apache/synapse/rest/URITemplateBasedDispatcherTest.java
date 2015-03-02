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
import org.apache.synapse.rest.dispatch.URITemplateHelper;

public class URITemplateBasedDispatcherTest extends RESTMediationTestCase {

    private static final String PROP_NAME = "prop.name";
    private static final String PROP_VALUE = "prop.value";

    public void testBasicTemplateDispatch1() throws Exception {
        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URITemplateHelper("/~{user}"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/~foo", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));
        assertEquals("foo", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "user"));

        synCtx = getMessageContext(synapseConfig, false, "/test/foo", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/test/~foo/bar", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }

    public void testBasicTemplateDispatch2() throws Exception {

        API api = new API("TestAPI", "/");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URITemplateHelper("/dictionary/{char}/{word}"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));
        assertEquals("c", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "char"));
        assertEquals("cat", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "word"));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/d/dog/", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));
        assertEquals("d", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "char"));
        assertEquals("dog", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "word"));

        synCtx = getMessageContext(synapseConfig, false, "/test/c/cat", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/c", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }

    public void testDefaultDispatch() throws Exception {

        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URITemplateHelper("/"));
        resource.setInSequence(getTestSequence(PROP_NAME, PROP_VALUE));
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        assertEquals(PROP_VALUE, synCtx.getProperty(PROP_NAME));
    }

    public void testMultipleResourceDispatch() throws Exception {

        API api = new API("TestAPI", "/");
        Resource resource1 = new Resource();
        resource1.setDispatcherHelper(new URITemplateHelper("/dictionary/{char}/{word}"));
        resource1.setInSequence(getTestSequence(PROP_NAME, "r1"));
        api.addResource(resource1);

        Resource resource2 = new Resource();
        resource2.setDispatcherHelper(new URITemplateHelper("/dictionary/{char}"));
        resource2.setInSequence(getTestSequence(PROP_NAME, "r2"));
        api.addResource(resource2);

        Resource resource3 = new Resource();
        resource3.setDispatcherHelper(new URITemplateHelper("/dictionary/{char}{#ref}"));
        resource3.setInSequence(getTestSequence(PROP_NAME, "r3"));
        api.addResource(resource3);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat", "GET");
        handler.process(synCtx);
        assertEquals("r1", synCtx.getProperty(PROP_NAME));
        assertEquals("c", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "char"));
        assertEquals("cat", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "word"));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/d", "GET");
        handler.process(synCtx);
        assertEquals("r2", synCtx.getProperty(PROP_NAME));
        assertEquals("d", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "char"));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/e#test", "GET");
        handler.process(synCtx);
        assertEquals("r3", synCtx.getProperty(PROP_NAME));
        assertEquals("e", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "char"));
        assertEquals("test", synCtx.getProperty(RESTConstants.REST_URI_VARIABLE_PREFIX + "ref"));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat/test", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat#ref", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));

        synCtx = getMessageContext(synapseConfig, false, "/dictionary/c/cat?a=5", "GET");
        handler.process(synCtx);
        assertNull(synCtx.getProperty(PROP_NAME));
    }
}
