/*
* Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.impl.factory;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.HashSet;
import java.util.Set;

public class KeyManagerHolderTestCase {


    @Test
    public void testInitializeKeymanagerClassGiven() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT))
                .thenReturn("org.wso2.carbon.apimgt.impl.factory.FakeKeyManagerForTest");
        Set<String> configSet = new HashSet<String>();
        configSet.add("conf1");
        configSet.add("APIKeyManager.Configuration.abc");
        Mockito.when(apiManagerConfiguration.getConfigKeySet()).thenReturn(configSet);
        KeyManagerHolder.initializeKeyManager(apiManagerConfiguration);
        Assert.assertNotNull(KeyManagerHolder.getKeyManagerInstance());
    }

    @Test(expected = APIManagementException.class)
    public void testInitializeKeymanagerUnkownClassGiven() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT))
                .thenReturn("org.wso2.carbon.apimgt.impl.factory.FakeClass");
        KeyManagerHolder.initializeKeyManager(apiManagerConfiguration);
    }

    @Test(expected = APIManagementException.class)
    public void testInitializeKeymanagerInterfaceGivenInsteadClass() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT))
                .thenReturn("org.wso2.carbon.apimgt.impl.factory.FakeKeyManagerInterfaceForTest");
        KeyManagerHolder.initializeKeyManager(apiManagerConfiguration);
    }

    @Test(expected = APIManagementException.class)
    public void testInitializeKeymanagerGivenInaccessibleClass() throws Exception {
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfiguration.getFirstProperty(APIConstants.KEY_MANAGER_CLIENT))
                .thenReturn("org.wso2.carbon.apimgt.impl.factory.FakeKeyManagerWithPrivateConstructorForTest");
        KeyManagerHolder.initializeKeyManager(apiManagerConfiguration);
    }
}
