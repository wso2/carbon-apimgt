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

package org.wso2.carbon.apimgt.gateway;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.rest.RESTConstants;

import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static MessageContext getMessageContext(String context, String version, String apiKey) {
        MessageContext synCtx = getMessageContext(context, version);
        org.apache.axis2.context.MessageContext axisMsgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        Map<String,String> headers = new HashMap<String, String>();
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        axisMsgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
        return synCtx;
    }

    public static MessageContext getMessageContext(String context, String version) {
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        axisMsgCtx.setIncomingTransportName("http");
        axisMsgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, "/test/1.0.0/search.atom");
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        synCtx.setProperty(RESTConstants.REST_API_CONTEXT, context);
        synCtx.setProperty(RESTConstants.SYNAPSE_REST_API_VERSION, version);
        return synCtx;
    }


}
