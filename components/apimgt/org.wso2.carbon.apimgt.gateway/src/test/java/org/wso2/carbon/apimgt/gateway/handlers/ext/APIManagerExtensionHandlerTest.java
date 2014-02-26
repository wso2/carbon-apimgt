/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.ext;

import junit.framework.TestCase;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.DropMediator;
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.RESTRequestHandler;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.wso2.carbon.apimgt.gateway.TestUtils;

public class APIManagerExtensionHandlerTest {   // extends TestCase
    /*
    public void testRequestInterception() throws Exception {
        MessageContext msgCtx = TestUtils.getMessageContext("/test", "1.0.0", "MyTestKey");
        SynapseConfiguration config = msgCtx.getEnvironment().getSynapseConfiguration();

        SequenceMediator seq = getTestSequence("TestAPI:v1.0.0--In", "TestProperty");
        config.addSequence(seq.getName(), seq);

        API api = getTestAPI();
        config.addAPI(api.getName(), api);

        RESTRequestHandler handler = new RESTRequestHandler();
        handler.process(msgCtx);

        assertEquals("TestValue", msgCtx.getProperty("TestProperty"));
    }

    public void testResponseInterception() throws Exception {
        MessageContext msgCtx = TestUtils.getMessageContext("/test", "1.0.0", "MyTestKey");
        SynapseConfiguration config = msgCtx.getEnvironment().getSynapseConfiguration();
        msgCtx.setResponse(true);

        SequenceMediator seq = getTestSequence("TestAPI:v1.0.0--Out", "TestProperty");
        config.addSequence(seq.getName(), seq);

        API api = getTestAPI();
        config.addAPI(api.getName(), api);
        msgCtx.setProperty(RESTConstants.SYNAPSE_REST_API, api.getName());

        RESTRequestHandler handler = new RESTRequestHandler();
        handler.process(msgCtx);

        assertEquals("TestValue", msgCtx.getProperty("TestProperty"));
    }

    private SequenceMediator getTestSequence(String name, String property) {
        SequenceMediator seq = new SequenceMediator();
        seq.setName(name);
        PropertyMediator mediator = new PropertyMediator();
        mediator.setName(property);
        mediator.setValue("TestValue");
        seq.addChild(mediator);
        return seq;
    }

    private API getTestAPI() {
        API api = new API("TestAPI", "/test");
        api.setVersionStrategy(new URLBasedVersionStrategy(api, "1.0.0", ""));
        Resource resource = new Resource();
        SequenceMediator inSeq = new SequenceMediator();
        inSeq.addChild(new DropMediator());
        resource.setInSequence(inSeq);
        api.addResource(resource);
        api.addHandler(new APIManagerExtensionHandler());
        return api;
    }
    */

}
