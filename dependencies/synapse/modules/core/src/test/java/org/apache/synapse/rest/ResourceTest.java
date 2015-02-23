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
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.core.axis2.MessageContextCreatorForAxis2;
import org.apache.synapse.core.axis2.SynapseMessageReceiver;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.apache.synapse.mediators.transform.XSLTMediator;
import org.apache.synapse.rest.dispatch.URITemplateHelper;

public class ResourceTest extends RESTMediationTestCase {

    public void testQueryParams() throws Exception {
        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false,
                "/test/admin?a=5&b=10&user=bar", "GET");
        handler.process(synCtx);
        assertEquals("5", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "a"));
        assertEquals("10", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "b"));
        assertEquals("bar", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "user"));

        synCtx = getMessageContext(synapseConfig, false, "/test/admin?a=5", "GET");
        handler.process(synCtx);
        assertEquals("5", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "a"));

        synCtx = getMessageContext(synapseConfig, false, "/test/admin?a=Hello%20World&b=10&c=/foo/bar", "GET");
        handler.process(synCtx);
        assertEquals("Hello World", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "a"));
        assertEquals("10", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "b"));
        assertEquals("/foo/bar", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "c"));
    }
    
    public void testQueryParamWithURL() throws Exception {
    	API api = new API("TestAPI", "/test");
    	Resource resource = new Resource();
    	api.addResource(resource);
    	 
    	SynapseConfiguration synapseConfig = new SynapseConfiguration();
    	synapseConfig.addAPI(api.getName(), api);
    	
    	RESTRequestHandler handler = new RESTRequestHandler();
    	       
    	MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/admin?a=http://test.com", "GET");
    	handler.process(synCtx);
    	//verify query parameters with URLs as values
    	assertEquals("http://test.com", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "a"));
    	                
    	synCtx = getMessageContext(synapseConfig, false, "/test/admin?a=http://test.com&b=10&c=/foo/bar", "GET");
    	handler.process(synCtx);
    	//verify query parameters with URLs as values
    	assertEquals("http://test.com", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "a"));
    	assertEquals("10", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "b"));
    	assertEquals("/foo/bar", synCtx.getProperty(RESTConstants.REST_QUERY_PARAM_PREFIX + "c"));
    }

    public void testFaultSequence() throws Exception {
        API api = new API("TestAPI", "/test");
        Resource resource = new Resource();
        resource.setDispatcherHelper(new URITemplateHelper("/~{user}"));
        SequenceMediator inSequence = getTestSequence("seq.in", "seq.in.value");
        ((PropertyMediator) inSequence.getChild(0)).setScope("axis2");
        XSLTMediator xsltMediator = new XSLTMediator();
        xsltMediator.setXsltKey(new Value("/bogus/key"));
        inSequence.addChild(xsltMediator);
        resource.setInSequence(inSequence);
        SequenceMediator faultSequence = getTestSequence("seq.fault", "seq.fault.value");
        ((PropertyMediator) faultSequence.getChild(0)).setScope("axis2");
        resource.setFaultSequence(faultSequence);
        api.addResource(resource);

        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(api.getName(), api);
        synapseConfig.addSequence("main", getTestSequence("main.in", "main.value"));
        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test/~foo", "GET");
        MessageContextCreatorForAxis2.setSynConfig(synapseConfig);
        MessageContextCreatorForAxis2.setSynEnv(synCtx.getEnvironment());

        org.apache.axis2.context.MessageContext mc = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        mc.setConfigurationContext(((Axis2SynapseEnvironment) synCtx.getEnvironment()).getAxis2ConfigurationContext());
        new SynapseMessageReceiver().receive(mc);
        assertEquals("seq.in.value", mc.getProperty("seq.in"));
        assertEquals("seq.fault.value", mc.getProperty("seq.fault"));
    }
}
