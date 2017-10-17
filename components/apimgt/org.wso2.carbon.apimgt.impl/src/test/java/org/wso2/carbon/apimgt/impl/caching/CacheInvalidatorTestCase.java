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
package org.wso2.carbon.apimgt.impl.caching;

import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class, ApiMgtDAO.class, KeyManagerHolder.class, LogFactory.class})
public class CacheInvalidatorTestCase {

    private int appId = 444;

    @Test
    public void testinvalidateCacheForAppWhenGatewayNotSet() {
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);

        APIManagerConfigurationService apiManagerConfigurationService = Mockito.mock(APIManagerConfigurationService.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(apiManagerConfigurationService);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(apiManagerConfigurationService.getAPIManagerConfiguration()).thenReturn(apiManagerConfiguration);

        CacheInvalidator cacheInvalidator = CacheInvalidator.getInstance();
        cacheInvalidator.invalidateCacheForApp(appId);
    }
}
