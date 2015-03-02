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

import junit.framework.TestCase;
import org.apache.axis2.Constants;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.TestUtils;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.mediators.builtin.PropertyMediator;
import org.apache.synapse.transport.nhttp.NhttpConstants;

import java.util.HashMap;
import java.util.Map;

public abstract class RESTMediationTestCase extends TestCase {

    protected MessageContext getMessageContext(SynapseConfiguration synapseConfig, boolean https,
                                               String url, String method) throws Exception {
        MessageContext synCtx = TestUtils.createSynapseMessageContext("<foo/>", synapseConfig);
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();
        if (https) {
            msgCtx.setIncomingTransportName("https");
        } else {
            msgCtx.setIncomingTransportName("http");
        }
        msgCtx.setProperty(Constants.Configuration.HTTP_METHOD, method);
        msgCtx.setProperty(Constants.Configuration.TRANSPORT_IN_URL, url);
        msgCtx.setProperty(NhttpConstants.REST_URL_POSTFIX, url.substring(1));
        return synCtx;
    }

    protected void addHttpHeader(String name, String value, MessageContext synCtx) {
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).
                getAxis2MessageContext();
        Object obj = msgCtx.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (obj != null) {
            ((Map) obj).put(name, value);
        } else {
            Map map = new HashMap();
            map.put(name, value);
            msgCtx.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, map);
        }
    }

    protected SequenceMediator getTestSequence(String name, String value) {
        SequenceMediator seq = new SequenceMediator();
        PropertyMediator prop = new PropertyMediator();
        prop.setName(name);
        prop.setValue(value);
        seq.addChild(prop);
        return seq;
    }
}
