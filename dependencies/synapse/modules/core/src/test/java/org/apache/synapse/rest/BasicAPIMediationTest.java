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

package org.apache.synapse.rest;

import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.version.URLBasedVersionStrategy;
import org.apache.synapse.transport.nhttp.NhttpConstants;

public class BasicAPIMediationTest extends RESTMediationTestCase {

    private static final String TEST_API = "TestAPI";

    public void testRestURLPostfix1() throws Exception {
        API api = new API(TEST_API, "/test");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "");

        synCtx = getMessageContext(synapseConfig, false, "/test/me/now", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "/me/now");

        synCtx = getMessageContext(synapseConfig, false, "/test?a=5", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "?a=5");

        api.setVersionStrategy(new URLBasedVersionStrategy(api, "1.0.0", null));
        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0?a=5", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "?a=5");

        synCtx = getMessageContext(synapseConfig, false, "/test/1.0.0/foo?a=5", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "/foo?a=5");
    }

    public void testRestURLPostfix2() throws Exception {
        API api = new API(TEST_API, "/");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/test", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "/test");

        synCtx = getMessageContext(synapseConfig, false, "/test/me/now", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "/test/me/now");

        synCtx = getMessageContext(synapseConfig, false, "/test?a=5", "GET");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "/test?a=5");
    }

    public void testRestURLPostfix3() throws Exception {
        API api = new API(TEST_API, "/services/Foo");
        SynapseConfiguration synapseConfig = new SynapseConfiguration();
        synapseConfig.addAPI(TEST_API, api);

        RESTRequestHandler handler = new RESTRequestHandler();

        MessageContext synCtx = getMessageContext(synapseConfig, false, "/services/Foo/test", "GET");
        // When the service path is in the URL, NHTTP transport removes that portion
        // from the postfix
        ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                setProperty(NhttpConstants.REST_URL_POSTFIX, "/test");
        handler.process(synCtx);
        checkRestURLPostfix(synCtx, "/test");
    }
    
    private void checkRestURLPostfix(MessageContext synCtx, String restURLPostfix) {
        String actual = (String) ((Axis2MessageContext) synCtx).getAxis2MessageContext().
                getProperty(NhttpConstants.REST_URL_POSTFIX);
        assertEquals(restURLPostfix, actual);
    }
}
