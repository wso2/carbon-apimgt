/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.handlers.common;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;

public class SynapsePropertiesHandlerTest {
    @Test
    public void handleRequest() throws Exception {
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        System.setProperty("http.nio.port","8280");
        System.setProperty("https.nio.port","8243");
        System.setProperty(APIConstants.KEYMANAGER_PORT,"9443");
        System.setProperty(APIConstants.KEYMANAGER_HOSTNAME,"api.wso2.com");
        SynapsePropertiesHandler synapsePropertiesHandler = new SynapsePropertiesHandler();
        synapsePropertiesHandler.handleRequest(synCtx);
        Assert.assertEquals(synCtx.getProperty("http.nio.port"),"8280");
        Assert.assertEquals(synCtx.getProperty("https.nio.port"),"8243");
        Assert.assertEquals(synCtx.getProperty("keyManager.port"),"9443");
        Assert.assertEquals(synCtx.getProperty("keyManager.hostname"),"api.wso2.com");

    }

}