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
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.apimgt.core.auth;

import org.junit.Assert;
import org.mockito.Mockito;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.configuration.models.APIMConfigurations;
import org.wso2.carbon.apimgt.core.configuration.models.KeyMgtConfigurations;
import org.wso2.carbon.apimgt.core.impl.WSO2ISKeyManagerImpl;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.config.provider.ConfigProvider;

public class ScopeRegistrationServiceStubFactoryTest {

    @Test
    public void testGetScopeRegistration() throws Exception {
        ScopeRegistration scopeRegistration = ScopeRegistrationServiceStubFactory.getScopeRegistration();
        Assert.assertTrue(scopeRegistration instanceof DefaultScopeRegistrationImpl);
    }

    @Test
    public void testGetScopeRegistrationForWso2Is() throws Exception {
        KeyMgtConfigurations keyManagerConfiguration = new KeyMgtConfigurations();
        keyManagerConfiguration.setKeyManagerImplClass(WSO2ISKeyManagerImpl.class.getCanonicalName());
        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        APIMConfigurations apimConfigurations = new APIMConfigurations();
        apimConfigurations.setKeyManagerConfigs(keyManagerConfiguration);
        Mockito.when(configProvider.getConfigurationObject(APIMConfigurations.class)).thenReturn(apimConfigurations);
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);
        ScopeRegistration scopeRegistration = ScopeRegistrationServiceStubFactory.getScopeRegistration();
        Assert.assertTrue(scopeRegistration instanceof WSO2ISScopeRegistrationImpl);
    }
}
