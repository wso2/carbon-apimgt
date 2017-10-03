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

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;

public class ServiceReferenceHolderTest {

    private static RegistryService registryService;
    private static APIManagerConfigurationService amConfigurationService;
    private static RealmService realmService;

    private ServiceReferenceHolder serviceReferenceHolder = ServiceReferenceHolder.getInstance();

    @BeforeClass
    public static void setUp() {
        registryService = Mockito.mock(RegistryService.class);
        amConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        realmService = Mockito.mock(RealmService.class);
    }

    @Test
    public void testRegistryService() {
        serviceReferenceHolder.setRegistryService(registryService);
        Assert.assertEquals(registryService, serviceReferenceHolder.getRegistryService());
    }

    @Test
    public void testAPIManagerConfigurationService() throws Exception {
        serviceReferenceHolder.setAPIManagerConfigurationService(amConfigurationService);
        Assert.assertEquals(amConfigurationService, serviceReferenceHolder.getAPIManagerConfigurationService());
    }

    @Test
    public void getRealmService() throws Exception {
        serviceReferenceHolder.setRealmService(realmService);
        Assert.assertEquals(realmService, serviceReferenceHolder.getRealmService());
    }

}