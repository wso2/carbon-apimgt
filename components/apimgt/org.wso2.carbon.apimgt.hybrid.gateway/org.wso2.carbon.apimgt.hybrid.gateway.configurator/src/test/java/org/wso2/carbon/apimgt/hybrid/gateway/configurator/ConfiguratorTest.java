/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.hybrid.gateway.configurator;

import org.apache.xalan.transformer.TransformerIdentityImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.OnPremiseGatewayConstants;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import static org.mockito.Matchers.any;

/**
 * Configurator TestCase
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({TransformerIdentityImpl.class, TransformerFactory.class})
public class ConfiguratorTest {

    private Properties gatewayProperties;
    private Properties configToolProperties;
    private String carbonConfigDirPath;
    private String gatewayConfigPath;
    private String args[] = { "test-email@test.com", "TestTenant", "TestPassword" };
    private static final String TOKEN = "token";

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
        String carbonHome = System.getProperty(ConfigConstants.CARBON_HOME);
        carbonConfigDirPath = carbonHome + File.separator + ConfigConstants.REPOSITORY_DIR + File.separator
                                      + ConfigConstants.CONF_DIR;
        gatewayConfigPath = carbonConfigDirPath + File.separator + OnPremiseGatewayConstants.CONFIG_FILE_NAME;
        gatewayProperties = Configurator.getGatewayProperties(gatewayConfigPath, args);
        String configToolPropertyFilePath = carbonConfigDirPath + File.separator +
                                                    ConfigConstants.CONFIG_TOOL_CONFIG_FILE_NAME;
        configToolProperties = Configurator.readPropertiesFromFile(configToolPropertyFilePath);
    }

    @Test
    public void setAPIMConfigurations() {
        String carbonHome = System.getProperty(ConfigConstants.CARBON_HOME);
        Configurator.setAPIMConfigurations(configToolProperties, carbonHome, gatewayProperties);
    }

    @Test
    public void updateConfigDetails() {
        String cloudConfigPath = System.getProperty(ConfigConstants.CARBON_HOME) + File.separator +
                                       ConfigConstants.RESOURCES_DIR + File.separator +
                                       ConfigConstants.CLOUD_CONFIG_FILE_NAME;
        Configurator.updateGatewayConfigDetails(cloudConfigPath, gatewayConfigPath);
    }

    @Test
    public void initializeGateway() throws Exception {
        //Collect device details
        Map<String, String> deviceDetails = Configurator.getDeviceDetails();
        String carbonFilePath = carbonConfigDirPath + File.separator + ConfigConstants.GATEWAY_CARBON_FILE_NAME;
        int port = Configurator.getGatewayPort(carbonFilePath);
        deviceDetails.put(ConfigConstants.PORT, Integer.toString(port));
        String payload = Configurator.getInitializationPayload(deviceDetails, args);
        String authHeader = Configurator.createAuthHeader(args);
        //Set initialization endpoint
        String initApiUrl = gatewayProperties.getProperty(ConfigConstants.INITIALIZATION_API_URL);
        //Update details in gateway properties file
        String gatewayConfigPath = carbonConfigDirPath + File.separator + OnPremiseGatewayConstants.CONFIG_FILE_NAME;
        Configurator.updateOnPremGatewayUniqueId(gatewayConfigPath, TOKEN);
    }

    @Test
    public void main() throws Exception {
        String carbonHome = System.getProperty(ConfigConstants.CARBON_HOME);
        setAPIMConfigurations();
        RegistryXmlConfigurator registryXmlConfigurator = new RegistryXmlConfigurator();
        TransformerIdentityImpl transformerIdentity = PowerMockito.mock(TransformerIdentityImpl.class);
        TransformerFactory transformerFactory = PowerMockito.mock(TransformerFactory.class);
        PowerMockito.mockStatic(TransformerFactory.class);
        PowerMockito.when(TransformerFactory.newInstance()).thenReturn(transformerFactory);
        PowerMockito.when(transformerFactory.newTransformer()).thenReturn(transformerIdentity);
        PowerMockito.doNothing().when(transformerIdentity).transform(any(DOMSource.class), any(StreamResult.class));
        registryXmlConfigurator.configure(carbonConfigDirPath, gatewayProperties);
        Log4JConfigurator log4JConfigurator = new Log4JConfigurator();
        log4JConfigurator.configure(carbonConfigDirPath);
        Configurator.writeConfiguredLock(carbonHome);
        //Cleaning the log4j.properties file
        PrintWriter writer = new PrintWriter(carbonHome + File.separator + ConfigConstants.REPOSITORY_DIR + File.separator + ConfigConstants.CONF_DIR + File.separator + "log4j.properties");
        writer.print("\n");
        writer.close();
    }

}
