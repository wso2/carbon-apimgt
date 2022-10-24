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

import io.swagger.v3.oas.models.OpenAPI;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.BDDMockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.io.File;

@RunWith(PowerMockRunner.class)
@PrepareForTest( {APIUtil.class})
public class APIMgtLatencyStatsHandlerTest {
    @Test
    public void handleRequest() throws Exception {
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        synCfg.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, "123456789");
        synCtx.setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.PUBLISHED);
        APIMgtLatencyStatsHandler apiMgtLatencyStatsHandler = new APIMgtLatencyStatsHandler();
        apiMgtLatencyStatsHandler.handleRequest(synCtx);
        long requestTime = Long.parseLong(String.valueOf(synCtx.getProperty("api.ut.requestTime")));
        Assert.assertTrue(requestTime <= System.currentTimeMillis());
    }

    @Test
    public void handleRequestWithoutSetExecutionStartTime() throws Exception {
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        synCtx.setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.PUBLISHED);
        APIMgtLatencyStatsHandler apiMgtLatencyStatsHandler = new APIMgtLatencyStatsHandler();
        apiMgtLatencyStatsHandler.handleRequest(synCtx);
        long requestTime = Long.parseLong(String.valueOf(synCtx.getProperty("api.ut.requestTime")));
        Assert.assertTrue(requestTime <= System.currentTimeMillis());
        Assert.assertTrue(Long.parseLong((String) synCtx.getProperty(APIMgtGatewayConstants
                .REQUEST_EXECUTION_START_TIME)) <= System.currentTimeMillis());
    }

    @Test
    public void handleResponse() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.given(APIUtil.isAnalyticsEnabled()).willReturn(true);
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        synCtx.setProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME, "123456789");
        APIMgtLatencyStatsHandler apiMgtLatencyStatsHandler = new APIMgtLatencyStatsHandler();
        apiMgtLatencyStatsHandler.handleResponse(synCtx);
        long backeEndLatencyTime = Long.parseLong(String.valueOf(synCtx.getProperty(APIMgtGatewayConstants
                .BACKEND_LATENCY)));
        Assert.assertTrue(backeEndLatencyTime <= System.currentTimeMillis());
        Assert.assertTrue(Long.valueOf((Long) synCtx.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME)) <=
                System.currentTimeMillis());
    }
    @Test
    public void handleResponseWhileAnalyticDisable() throws Exception {
        PowerMockito.mockStatic(APIUtil.class);
        BDDMockito.given(APIUtil.isAnalyticsEnabled()).willReturn(false);
        SynapseConfiguration synCfg = new SynapseConfiguration();
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        APIMgtLatencyStatsHandler apiMgtLatencyStatsHandler = new APIMgtLatencyStatsHandler();
        apiMgtLatencyStatsHandler.handleResponse(synCtx);
    }

    @Test
    public void handleRequestWithSwagger() throws Exception {

        String apiUUID = "414f0c50-1429-11ed-861d-0242ac120002";
        File swaggerJsonFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("swaggerEntry/openapi.json").getFile());
        String swaggerValue = FileUtils.readFileToString(swaggerJsonFile);
        Entry entry = new Entry();
        entry.setValue(swaggerValue);

        SynapseConfiguration synCfg = new SynapseConfiguration();
        synCfg.getLocalRegistry().put(apiUUID, entry);
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        synCfg.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, "123456789");
        synCtx.setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.PUBLISHED);
        APIMgtLatencyStatsHandler apiMgtLatencyStatsHandler = new APIMgtLatencyStatsHandler();
        apiMgtLatencyStatsHandler.setApiUUID(apiUUID);
        apiMgtLatencyStatsHandler.handleRequest(synCtx);
        long requestTime = Long.parseLong(String.valueOf(synCtx.getProperty("api.ut.requestTime")));
        Assert.assertTrue(requestTime <= System.currentTimeMillis());
        Assert.assertEquals(synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_STRING), swaggerValue);
    }

    @Test
    public void handleRequestWithSwaggerYaml() throws Exception {

        String apiUUID = "414f0c50-1429-11ed-861d-0242ac120002";
        File swaggerJsonFile = new File(Thread.currentThread().getContextClassLoader().
                getResource("swaggerEntry/swagger.yaml").getFile());
        String swaggerValue = FileUtils.readFileToString(swaggerJsonFile);
        Entry entry = new Entry();
        entry.setValue(swaggerValue);

        SynapseConfiguration synCfg = new SynapseConfiguration();
        synCfg.getLocalRegistry().put(apiUUID, entry);
        org.apache.axis2.context.MessageContext axisMsgCtx = new org.apache.axis2.context.MessageContext();
        AxisConfiguration axisConfig = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisConfig);
        MessageContext synCtx = new Axis2MessageContext(axisMsgCtx, synCfg,
                new Axis2SynapseEnvironment(cfgCtx, synCfg));
        synCfg.setProperty(APIMgtGatewayConstants.REQUEST_EXECUTION_START_TIME, "123456789");
        synCtx.setProperty(APIMgtGatewayConstants.API_STATUS, APIConstants.PUBLISHED);
        APIMgtLatencyStatsHandler apiMgtLatencyStatsHandler = new APIMgtLatencyStatsHandler();
        apiMgtLatencyStatsHandler.setApiUUID(apiUUID);
        apiMgtLatencyStatsHandler.handleRequest(synCtx);
        long requestTime = Long.parseLong(String.valueOf(synCtx.getProperty("api.ut.requestTime")));
        Assert.assertTrue(requestTime <= System.currentTimeMillis());
        Assert.assertEquals(synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_STRING), swaggerValue);
        OpenAPI openApi = (OpenAPI) synCtx.getProperty(APIMgtGatewayConstants.OPEN_API_OBJECT);
        String headerName = openApi.getPaths().get("/pet").getPost().getParameters().get(0).getName();
        Assert.assertEquals(headerName, "authorization");
    }
}
