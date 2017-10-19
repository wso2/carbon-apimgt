/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.impl;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

public class ServiceReferenceHolderMockCreator {
    private ConfigurationServiceMockCreator configurationServiceMockCreator = new ConfigurationServiceMockCreator();
    private static ConfigurationContextServiceMockCreator configurationContextServiceMockCreator =
            new ConfigurationContextServiceMockCreator();
    private ServiceReferenceHolder serviceReferenceHolder;
    private RealmServiceMockCreator realmServiceMockCreator;
    private RegistryServiceMockCreator registryServiceMockCreator;

    public ServiceReferenceHolderMockCreator(int tenantId) throws RegistryException, UserStoreException {
        serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        realmServiceMockCreator = new RealmServiceMockCreator(tenantId);

        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).
                thenReturn(configurationServiceMockCreator.getMock());
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmServiceMockCreator.getMock());


        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
    }

    public ServiceReferenceHolder getMock() {
        return serviceReferenceHolder;
    }

    public void initRegistryServiceMockCreator(boolean isResourceExists, Object content) throws RegistryException {
        registryServiceMockCreator = new RegistryServiceMockCreator(isResourceExists, content);
        Mockito.when(serviceReferenceHolder.getRegistryService()).thenReturn(registryServiceMockCreator.getMock());
    }

    public static void initContextService() {
        //PowerMockito.mockStatic(ServiceReferenceHolder.class);
        Mockito.when(ServiceReferenceHolder.getContextService()).
                thenReturn(configurationContextServiceMockCreator.getMock());
    }

    public ConfigurationContextServiceMockCreator getConfigurationContextServiceMockCreator() {
        return configurationContextServiceMockCreator;
    }

}
