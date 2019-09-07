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

package org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.internal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.hybrid.gateway.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.common.util.HttpRequestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.constants.Constants;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * APIUsagePublisherComponent test class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, APIManagerConfiguration.class, ServiceReferenceHolder.class,
        APIManagerConfigurationService.class, HttpRequestUtil.class, RealmService.class})
public class APIUsagePublisherComponentTest {
    public static final String CARBON_HOME = "carbon.home";
    public static final String CARBON_CONFIGS_PATH = "/repository/conf";

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void activate() throws Exception {
        String carbonHome = System.getProperty(Constants.CARBON_HOME);
        PowerMockito.mockStatic(CarbonUtils.class);
        PowerMockito.when(CarbonUtils.getCarbonHome()).thenReturn(carbonHome);
        String carbonConfigPath = System.getProperty(CARBON_HOME) + CARBON_CONFIGS_PATH;
        PowerMockito.when(CarbonUtils.getCarbonConfigDirPath()).thenReturn(carbonConfigPath);
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
