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
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.exception.ContainerBasedGatewayException;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.util.BrokerUtil;

import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIManagerFactory.class, BrokerUtil.class})
public class APIGatewayPublisherImplTestCase {

    @Test
    public void testCreateContainerGateway() throws ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        Mockito.when(apiManagerFactory.getContainerBasedGatewayGenerator()).thenReturn(containerBasedGatewayGenerator);
        apiGatewayPublisher.createContainerBasedGateway("label");
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(1)).createContainerGateway("label");
    }

    @Test
    public void testDeleteAPIWhenAPIHasOwnGateway() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.mockStatic(BrokerUtil.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        Mockito.when(apiManagerFactory.getContainerBasedGatewayGenerator()).thenReturn(containerBasedGatewayGenerator);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(1)).removeContainerBasedGateway("label");
    }

    @Test
    public void testDeleteAPIWhenAPIDoesNotHaveOwnGateway() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.mockStatic(BrokerUtil.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        Mockito.when(apiManagerFactory.getContainerBasedGatewayGenerator()).thenReturn(containerBasedGatewayGenerator);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(false).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(0)).removeContainerBasedGateway("label");
    }

    @Test
    public void testDeleteAPIWhenLabelsAreNull() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.mockStatic(BrokerUtil.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        Mockito.when(apiManagerFactory.getContainerBasedGatewayGenerator()).thenReturn(containerBasedGatewayGenerator);
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(null).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(0)).removeContainerBasedGateway("label");
    }

    @Test
    public void testDeleteAPIWhenLabelAreEmpty() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.mockStatic(BrokerUtil.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        ContainerBasedGatewayGenerator containerBasedGatewayGenerator = Mockito
                .mock(ContainerBasedGatewayGenerator.class);
        Mockito.when(apiManagerFactory.getContainerBasedGatewayGenerator()).thenReturn(containerBasedGatewayGenerator);
        List<String> labels = new ArrayList<>();
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
        Mockito.verify(containerBasedGatewayGenerator, Mockito.times(0)).removeContainerBasedGateway("label");
    }

    @Test(expected = GatewayException.class)
    public void testDeleteAPIForException() throws GatewayException, ContainerBasedGatewayException {

        APIGatewayPublisherImpl apiGatewayPublisher = new APIGatewayPublisherImpl();
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        PowerMockito.mockStatic(BrokerUtil.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        Mockito.when(apiManagerFactory.getContainerBasedGatewayGenerator())
                .thenThrow(ContainerBasedGatewayException.class);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        API api = SampleTestObjectCreator.createDefaultAPI().lifeCycleStatus(APIStatus.PUBLISHED.getStatus())
                .hasOwnGateway(true).labels(labels).build();
        apiGatewayPublisher.deleteAPI(api);
    }

}


