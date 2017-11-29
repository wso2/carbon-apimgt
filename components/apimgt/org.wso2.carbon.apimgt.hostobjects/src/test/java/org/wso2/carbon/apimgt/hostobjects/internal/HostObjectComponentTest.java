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
package org.wso2.carbon.apimgt.hostobjects.internal;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

public class HostObjectComponentTest {

    HostObjectComponent hostObjectComponent = new HostObjectComponent();

    @Test
    public void testSetAPIManagerConfigurationService() {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(configurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);
        hostObjectComponent.setAPIManagerConfigurationService(configurationService);
        Assert.assertEquals(ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService(), configurationService);
    }

    @Test
    public void testUnsetAPIManagerConfigurationService() {
        APIManagerConfigurationService configurationService = Mockito.mock(APIManagerConfigurationService.class);
        hostObjectComponent.unsetAPIManagerConfigurationService(configurationService);
        // Nothing to assert :(
    }

    @Test
    public void testSetDataSourceService() {
        DataSourceService dataSourceService = Mockito.mock(DataSourceService.class);
        hostObjectComponent.setDataSourceService(dataSourceService);
        Assert.assertEquals(hostObjectComponent.getDataSourceService(), dataSourceService);
    }

    @Test
    public void testUnsetDataSourceService() {
        DataSourceService dataSourceService = Mockito.mock(DataSourceService.class);
        hostObjectComponent.unsetDataSourceService(dataSourceService);
        // Nothing to assert
    }

    @Test
    public void testSetConfigurationContextService() throws APIManagementException {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        ConfigurationContext context = new ConfigurationContext(axisConfiguration);
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        Mockito.when(configurationContextService.getClientConfigContext()).thenReturn(context);
        hostObjectComponent.setConfigurationContextService(configurationContextService);
        // Nothing to assert
    }

    @Test
    public void testUnsetConfigurationContextService() {
        AxisConfiguration axisConfiguration = new AxisConfiguration();
        ConfigurationContextService configurationContextService = Mockito.mock(ConfigurationContextService.class);
        hostObjectComponent.unsetConfigurationContextService(configurationContextService);
        // Nothing to assert
    }

    @Test
    public void testSetRealmService() {
        RealmService realmService = Mockito.mock(RealmService.class);
        hostObjectComponent.setRealmService(realmService);
        Assert.assertEquals(ServiceReferenceHolder.getInstance().getRealmService(), realmService);
    }

    @Test
    public void testUnsetRealmService() {
        RealmService realmService = Mockito.mock(RealmService.class);
        hostObjectComponent.unsetRealmService(realmService);
        Assert.assertEquals(null, ServiceReferenceHolder.getInstance().getRealmService());
    }

    @Test
    public void testSetRegistryService() {
        RegistryService registryService = Mockito.mock(RegistryService.class);
        hostObjectComponent.setRegistryService(registryService);
        Assert.assertEquals(ServiceReferenceHolder.getInstance().getRegistryService(), registryService);
    }

    @Test
    public void testUnsetRegistryService() {
        RegistryService registryService = Mockito.mock(RegistryService.class);
        hostObjectComponent.unsetRegistryService(registryService);
        Assert.assertEquals(null, ServiceReferenceHolder.getInstance().getRegistryService());
    }
}
