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
package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ServiceReferenceHolder.class})
public class SelfSignupUtilTestCase {

    @Test
    public void testGetSignupConfiguration() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIMConfigService apimConfigService = Mockito.mock(APIMConfigService.class);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
        UserRegistrationConfigDTO config = new UserRegistrationConfigDTO();
        config.getRoles().add("Internal/subscriber");
        Mockito.when(apimConfigService.getSelfSighupConfig("bar.com")).thenReturn(config);
        UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration("bar.com");
        Assert.assertEquals(userRegistrationConfigDTO, config);
    }

    @Test
    public void testGetSignupConfigurationDifferentObjectType() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIMConfigService apimConfigService = Mockito.mock(APIMConfigService.class);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
        Mockito.when(apimConfigService.getSelfSighupConfig("bar.com")).thenReturn("Test String");
        UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration("bar.com");
        Assert.assertTrue(userRegistrationConfigDTO instanceof UserRegistrationConfigDTO);
        Assert.assertEquals(userRegistrationConfigDTO.getRoles().size(), 0);
    }
}