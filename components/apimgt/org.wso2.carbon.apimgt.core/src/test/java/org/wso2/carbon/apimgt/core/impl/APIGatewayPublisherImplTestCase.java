/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.Test;
import org.mockito.Mockito;
import org.testng.Assert;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.Broker;
import org.wso2.carbon.apimgt.core.configuration.models.ContainerBasedGatewayConfiguration;
import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.util.ArrayList;
import java.util.List;

public class APIGatewayPublisherImplTestCase {

    @Test
    public void testCreateContainerGateway() throws ContainerBasedGatewayException, ConfigurationException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        apiGatewayPublisher.setContainerBasedGatewayGenerator(containerBasedGatewayGenerator);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiGatewayPublisher.createContainerBasedGateway("label", api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(1)).createContainerGateway("label", api);
    }

    @Test
    public void testGetContainerBasedGatewayGenerator() throws ContainerBasedGatewayException, ConfigurationException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();

        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ContainerBasedGatewayConfiguration containerBasedGatewayConfig = new ContainerBasedGatewayConfiguration();
        Mockito.when(configProvider.getConfigurationObject(ContainerBasedGatewayConfiguration.class))
                .thenReturn(containerBasedGatewayConfig);
        ContainerBasedGatewayConfigBuilder.build(configProvider);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator =
                apiGatewayPublisher.getContainerBasedGatewayGenerator();
        Assert.assertNotNull(containerBasedGatewayGenerator);
    }

    @Test(expected = ContainerBasedGatewayException.class)
    public void testGetContainerBasedGatewayGeneratorForException() throws ContainerBasedGatewayException,
            ConfigurationException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();

        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ContainerBasedGatewayConfiguration containerBasedGatewayConfig = new ContainerBasedGatewayConfiguration();
        containerBasedGatewayConfig.setImplClass("org.wso2.invalid.class.name");
        Mockito.when(configProvider.getConfigurationObject(ContainerBasedGatewayConfiguration.class))
                .thenReturn(containerBasedGatewayConfig);
        ContainerBasedGatewayConfigBuilder.build(configProvider);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator =
                apiGatewayPublisher.getContainerBasedGatewayGenerator();
        Assert.assertNotNull(containerBasedGatewayGenerator);
    }

    @Test
    public void testDeleteAPIWhenAPIHasOwnGateway() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        Broker broker = Mockito.mock(BrokerImpl.class, Mockito.RETURNS_DEEP_STUBS);
        BrokerUtil.initialize(broker);
        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        apiGatewayPublisher.setContainerBasedGatewayGenerator(containerBasedGatewayGenerator);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(1)).removeContainerBasedGateway("label", api);
    }

    @Test
    public void testDeleteAPIWhenAPIDoesNotHaveOwnGateway() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        Broker broker = Mockito.mock(BrokerImpl.class, Mockito.RETURNS_DEEP_STUBS);
        BrokerUtil.initialize(broker);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        apiGatewayPublisher.setContainerBasedGatewayGenerator(containerBasedGatewayGenerator);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(false).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(0)).removeContainerBasedGateway("label", api);
    }

    @Test
    public void testDeleteAPIWhenLabelsAreNull() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        Broker broker = Mockito.mock(BrokerImpl.class, Mockito.RETURNS_DEEP_STUBS);
        BrokerUtil.initialize(broker);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        apiGatewayPublisher.setContainerBasedGatewayGenerator(containerBasedGatewayGenerator);
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(null).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(0)).removeContainerBasedGateway("label", api);
    }

    @Test
    public void testDeleteAPIWhenLabelAreEmpty() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        Broker broker = Mockito.mock(BrokerImpl.class, Mockito.RETURNS_DEEP_STUBS);
        BrokerUtil.initialize(broker);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        apiGatewayPublisher.setContainerBasedGatewayGenerator(containerBasedGatewayGenerator);
        List<String> labels = new ArrayList<>();
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(0)).removeContainerBasedGateway("label", api);
    }

    @Test(expected = GatewayException.class)
    public void testDeleteAPIForException() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        Broker broker = Mockito.mock(BrokerImpl.class, Mockito.RETURNS_DEEP_STUBS);
        BrokerUtil.initialize(broker);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        apiGatewayPublisher.setContainerBasedGatewayGenerator(containerBasedGatewayGenerator);
        Mockito.doThrow(ContainerBasedGatewayException.class).when(containerBasedGatewayGenerator)
                .removeContainerBasedGateway(Mockito.any(), Mockito.any());
        List<String> labels = new ArrayList<>();
        labels.add("label");
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
    }

}


