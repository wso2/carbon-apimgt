/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import com.google.common.io.Files;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExtensionsConfigBuilder;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.io.File;
import java.util.Map;

public class APIGatewayPublisherImplTestCase {

    @BeforeClass
    void init() {
        File temp = Files.createTempDir();
        temp.deleteOnExit();
        System.setProperty("gwHome", temp.getAbsolutePath());
        //Set the resource path, where contain composer test JS
        System.setProperty("carbon.home", new File("src/test/resources").getAbsolutePath());

        WorkflowExtensionsConfigBuilder.build(new ConfigProvider() {
            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
                T workflowConfig = (T) new WorkflowConfig();
                return workflowConfig;
            }

            @Override
            public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
                return null;
            }
        });
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);
    }

    @Test(description = "Publish API artifacts to the Gateway")
    public void testPublishToGateway() throws GatewayException {
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();

        apiGatewayPublisher.addAPI(api);

    }

    @Test(description = "Publish API artifacts with gwHome == null")
    public void testPublishToGatewayWithNogwHome() throws GatewayException {
        System.clearProperty("gwHome");
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        apiGatewayPublisher.addAPI(api);
    }

    @Test(description = "Publish API artifacts with API in defaultVerison")
    public void testPublishToGatewayWithDefaultVersion()
            throws GatewayException {
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        API api = SampleTestObjectCreator.createUniqueAPI().gatewayConfig(configString).build();
        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        apiGatewayPublisher.addAPI(api);
    }

}
