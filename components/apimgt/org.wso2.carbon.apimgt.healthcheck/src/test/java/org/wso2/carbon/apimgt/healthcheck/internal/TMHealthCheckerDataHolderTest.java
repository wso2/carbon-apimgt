/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.healthcheck.internal;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.core.conf.Configuration;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.conf.DataReceiver;
import org.wso2.carbon.event.stream.core.EventStreamService;

import java.util.Collections;

/**
 * Every reference {@link TMHealthCheckerServiceComponent} binds here is {@code ReferencePolicy.DYNAMIC}: when one
 * is rebound to a replacement, Declarative Services calls the bind method for the replacement before it calls the
 * unbind method for the one being replaced. These tests simulate exactly that ordering directly against the data
 * holder (the component itself needs a running OSGi runtime to exercise, this does not).
 */
public class TMHealthCheckerDataHolderTest {

    @After
    public void clearServices() {

        TMHealthCheckerDataHolder.setAPIManagerConfigurationService(null);
        TMHealthCheckerDataHolder.setEventStreamService(null);
        TMHealthCheckerDataHolder.setDataBridgeReceiverService(null);
    }

    @Test
    public void keepsTheReplacementApiManagerConfigurationServiceWhenTheOldOneIsUnboundAfterIt() {

        APIManagerConfigurationService first = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfigurationService second = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration secondConfig = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(second.getAPIManagerConfiguration()).thenReturn(secondConfig);

        TMHealthCheckerDataHolder.setAPIManagerConfigurationService(first);
        TMHealthCheckerDataHolder.setAPIManagerConfigurationService(second);
        TMHealthCheckerDataHolder.unsetAPIManagerConfigurationService(first);

        Assert.assertSame(secondConfig, TMHealthCheckerDataHolder.getTrafficManagerConfiguration());
    }

    @Test
    public void clearsTheApiManagerConfigurationServiceWhenTheOneCurrentlyHeldIsUnbound() {

        APIManagerConfigurationService service = Mockito.mock(APIManagerConfigurationService.class);
        TMHealthCheckerDataHolder.setAPIManagerConfigurationService(service);

        TMHealthCheckerDataHolder.unsetAPIManagerConfigurationService(service);

        Assert.assertNull(TMHealthCheckerDataHolder.getTrafficManagerConfiguration());
    }

    @Test
    public void keepsTheReplacementEventStreamServiceWhenTheOldOneIsUnboundAfterIt() {

        EventStreamService first = Mockito.mock(EventStreamService.class);
        EventStreamService second = Mockito.mock(EventStreamService.class);

        TMHealthCheckerDataHolder.setEventStreamService(first);
        TMHealthCheckerDataHolder.setEventStreamService(second);
        TMHealthCheckerDataHolder.unsetEventStreamService(first);

        Assert.assertSame(second, TMHealthCheckerDataHolder.getEventStreamService());
    }

    @Test
    public void clearsTheEventStreamServiceWhenTheOneCurrentlyHeldIsUnbound() {

        EventStreamService service = Mockito.mock(EventStreamService.class);
        TMHealthCheckerDataHolder.setEventStreamService(service);

        TMHealthCheckerDataHolder.unsetEventStreamService(service);

        Assert.assertNull(TMHealthCheckerDataHolder.getEventStreamService());
    }

    @Test
    public void keepsTheReplacementDataBridgeReceiverServiceWhenTheOldOneIsUnboundAfterIt() {

        DataBridgeReceiverService first = Mockito.mock(DataBridgeReceiverService.class);
        DataBridgeReceiverService second = Mockito.mock(DataBridgeReceiverService.class);
        Mockito.when(second.getInitialConfig()).thenReturn(binaryReceiverConfig("tcpReceiverThreadPoolSize", "77"));

        TMHealthCheckerDataHolder.setDataBridgeReceiverService(first);
        TMHealthCheckerDataHolder.setDataBridgeReceiverService(second);
        TMHealthCheckerDataHolder.unsetDataBridgeReceiverService(first);

        Assert.assertEquals(Integer.valueOf(77),
                TMHealthCheckerDataHolder.getReceiverThreadPoolSize("tcpReceiverThreadPoolSize"));
    }

    @Test
    public void clearsTheDataBridgeReceiverServiceWhenTheOneCurrentlyHeldIsUnbound() {

        DataBridgeReceiverService service = Mockito.mock(DataBridgeReceiverService.class);
        Mockito.when(service.getInitialConfig()).thenReturn(binaryReceiverConfig("tcpReceiverThreadPoolSize", "77"));
        TMHealthCheckerDataHolder.setDataBridgeReceiverService(service);

        TMHealthCheckerDataHolder.unsetDataBridgeReceiverService(service);

        Assert.assertNull(TMHealthCheckerDataHolder.getReceiverThreadPoolSize("tcpReceiverThreadPoolSize"));
    }

    private static DataBridgeConfiguration binaryReceiverConfig(String configName, String value) {

        Configuration configuration = new Configuration();
        configuration.setName(configName);
        configuration.setValue(value);
        DataReceiver receiver = new DataReceiver();
        receiver.setName("Binary");
        receiver.setConfigurations(Collections.singletonList(configuration));
        DataBridgeConfiguration configurationHolder = new DataBridgeConfiguration();
        configurationHolder.setDataReceivers(Collections.singletonList(receiver));
        return configurationHolder;
    }
}
