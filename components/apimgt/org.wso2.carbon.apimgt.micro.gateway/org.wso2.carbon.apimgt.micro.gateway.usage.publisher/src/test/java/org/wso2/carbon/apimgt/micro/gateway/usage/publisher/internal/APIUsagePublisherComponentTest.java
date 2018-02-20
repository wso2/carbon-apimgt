/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.micro.gateway.usage.publisher.internal;

import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * APIUsagePublisherComponent test class
 *
 */
public class APIUsagePublisherComponentTest {

    @Test
    public void activate() throws Exception {
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        ComponentContext componentContext = Mockito.mock(ComponentContext.class);
        BundleContext bundleContext = Mockito.mock(BundleContext.class);
        Mockito.when(componentContext.getBundleContext()).thenReturn(bundleContext);
        serviceComponent.activate(componentContext);
    }

    @Test
    public void deactivate() throws Exception {
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        ComponentContext componentContext = Mockito.mock(ComponentContext.class);
        serviceComponent.deactivate(componentContext);
    }

    @Test
    public void setAPIManagerConfigurationService() throws Exception {
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        serviceComponent.setAPIManagerConfigurationService(apiManagerConfigurationService);
    }

    @Test
    public void unsetAPIManagerConfigurationService() throws Exception {
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        APIManagerConfigurationService apiManagerConfigurationService = Mockito
                .mock(APIManagerConfigurationService.class);
        serviceComponent.unsetAPIManagerConfigurationService(apiManagerConfigurationService);
    }

    @Test
    public void setRealmService() throws Exception {
        RealmService realmService = Mockito.mock(RealmService.class);
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        serviceComponent.setRealmService(realmService);
    }

    @Test
    public void unsetRealmService() throws Exception {
        RealmService realmService = Mockito.mock(RealmService.class);
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        serviceComponent.unsetRealmService(realmService);
    }

    @Test
    public void setTaskService() throws Exception {
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        TaskService taskService = Mockito.mock(TaskService.class);
        serviceComponent.setTaskService(taskService);
    }

    @Test
    public void unsetTaskService() throws Exception {
        APIUsagePublisherComponent serviceComponent = new APIUsagePublisherComponent();
        TaskService taskService = Mockito.mock(TaskService.class);
        serviceComponent.unsetTaskService(taskService);
    }

}
