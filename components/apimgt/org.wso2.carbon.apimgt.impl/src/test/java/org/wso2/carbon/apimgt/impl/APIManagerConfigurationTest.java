/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Test;
import org.testng.Assert;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Environment;

import javax.xml.stream.XMLStreamException;

import java.util.Map;

public class APIManagerConfigurationTest {

    @Test
    public void testPublisherProperties() throws XMLStreamException {
        String publisherConfig = "<EventPublisherConfiguration>"
                                 + "<Properties>\n"
                                 + "<Property name=\"testProp\">testVal</Property>\n"
                                 + "</Properties>\n"
                                 + "</EventPublisherConfiguration>";
        OMElement element = AXIOMUtil.stringToOM(publisherConfig);
        APIManagerConfiguration config = new APIManagerConfiguration();
        Map<String, String> extractedProperties = config.extractPublisherProperties(element);
        Assert.assertTrue(extractedProperties.containsKey("testProp"));
    }

    @Test
    public void testPublisherPropertiesUndefined() throws XMLStreamException {
        String publisherConfig = "<EventPublisherConfiguration>"
                                 + "<Properties>\n"
                                 + "</Properties>\n"
                                 + "</EventPublisherConfiguration>";
        OMElement element = AXIOMUtil.stringToOM(publisherConfig);
        APIManagerConfiguration config = new APIManagerConfiguration();
        Map<String, String> extractedProperties = config.extractPublisherProperties(element);
        Assert.assertTrue(extractedProperties.isEmpty());

    }

    @Test
    public void testEnvironmentsConfigProvided() throws XMLStreamException, APIManagementException {
        String envConfig = "<Environment type=\"hybrid\" api-console=\"true\" isDefault=\"true\">\n" +
                "                <Name>Default</Name>\n" +
                "                <DisplayName></DisplayName>\n" +
                "                <GatewayType>Regular</GatewayType>\n" +
                "                <Description>This is a hybrid gateway that handles both production and sandbox token traffic.</Description>\n" +
                "                <!-- Server URL of the API gateway -->\n" +
                "                <ServerURL>https://localhost:9440/services/</ServerURL>\n" +
                "                <!-- Admin username for the API gateway. -->\n" +
                "                <Username>${admin.username}</Username>\n" +
                "                <!-- Admin password for the API gateway.-->\n" +
                "                <Password>${admin.password}</Password>\n" +
                "                <!-- Provider Vendor of the API gateway.-->\n" +
                "                <Provider>wso2</Provider>\n" +
                "                <!-- Endpoint URLs for the APIs hosted in this API gateway.-->\n" +
                "                <GatewayEndpoint>https://localhost:9440,http://localhost:9440</GatewayEndpoint>\n" +
                "                <!-- Additional properties for External Gateways -->\n" +
                "                <!-- Endpoint URLs of the WebSocket APIs hosted in this API Gateway -->\n" +
                "                <GatewayWSEndpoint>ws://localhost:9099,wss://localhost:8099</GatewayWSEndpoint>\n" +
                "                <!-- Endpoint URLs of the WebSub APIs hosted in this API Gateway -->\n" +
                "                <GatewayWebSubEndpoint>http://localhost:9021,https://localhost:8021</GatewayWebSubEndpoint>\n" +
                "                <VirtualHosts>\n" +
                "                </VirtualHosts>\n" +
                "            </Environment>";
        OMElement element = AXIOMUtil.stringToOM(envConfig);
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.setEnvironmentConfig(element);
        Map<String, Environment> environmentsList = config.getGatewayEnvironments();
        Assert.assertFalse(environmentsList.isEmpty());
        Environment defaultEnv = environmentsList.get("Default");
        Assert.assertEquals(defaultEnv.getProvider(), "wso2");
        Assert.assertTrue(defaultEnv.getAdditionalProperties().isEmpty());
    }

    @Test
    public void testEnvironmentsConfigWithAdditionalProperties() throws XMLStreamException, APIManagementException {
        String envConfig = "<Environment type=\"hybrid\" api-console=\"true\" isDefault=\"true\">\n" +
                "                <Name>Default</Name>\n" +
                "                <DisplayName></DisplayName>\n" +
                "                <GatewayType>Regular</GatewayType>\n" +
                "                <Description>This is a hybrid gateway that handles both production and sandbox token traffic.</Description>\n" +
                "                <!-- Server URL of the API gateway -->\n" +
                "                <ServerURL>https://localhost:9440/services/</ServerURL>\n" +
                "                <!-- Admin username for the API gateway. -->\n" +
                "                <Username>${admin.username}</Username>\n" +
                "                <!-- Admin password for the API gateway.-->\n" +
                "                <Password>${admin.password}</Password>\n" +
                "                <!-- Provider Vendor of the API gateway.-->\n" +
                "                <Provider>wso2</Provider>\n" +
                "                <!-- Endpoint URLs for the APIs hosted in this API gateway.-->\n" +
                "                <GatewayEndpoint>https://localhost:9440,http://localhost:9440</GatewayEndpoint>\n" +
                "                <!-- Additional properties for External Gateways -->\n" +
                "                <!-- Endpoint URLs of the WebSocket APIs hosted in this API Gateway -->\n" +
                "                <GatewayWSEndpoint>ws://localhost:9099,wss://localhost:8099</GatewayWSEndpoint>\n" +
                "                <!-- Endpoint URLs of the WebSub APIs hosted in this API Gateway -->\n" +
                "                <GatewayWebSubEndpoint>http://localhost:9021,https://localhost:8021</GatewayWebSubEndpoint>\n" +
                "                <Properties>\n" +
                "                    <Property name=\"Organization\">WSO2</Property>\n" +
                "                    <Property name=\"DisplayName\">Development Environment</Property>\n" +
                "                    <Property name=\"DevAccountName\">dev-1</Property>\n" +
                "                </Properties>\n" +
                "                <VirtualHosts>\n" +
                "                </VirtualHosts>\n" +
                "            </Environment>";
        OMElement element = AXIOMUtil.stringToOM(envConfig);
        APIManagerConfiguration config = new APIManagerConfiguration();
        config.setEnvironmentConfig(element);
        Map<String, Environment> environmentsList = config.getGatewayEnvironments();
        Assert.assertFalse(environmentsList.isEmpty());
        Environment defaultEnv = environmentsList.get("Default");
        Assert.assertFalse(defaultEnv.getAdditionalProperties().isEmpty());
    }
}
