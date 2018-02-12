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

package org.wso2.carbon.apimgt.micro.gateway.tenant.initializer.listener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.micro.gateway.tenant.initializer.constants.Constants;
import org.wso2.carbon.apimgt.micro.gateway.tenant.initializer.internal.ServiceDataHolder;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.utils.CarbonUtils;

import static org.mockito.Matchers.any;

/**
 * ServerStartupListener Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, CommonUtil.class, ServerStartupListener.class, ServiceDataHolder.class})
public class ServerStartupListenerTest {
    @Test
    public void completedServerStartup() throws Exception {
        ServiceDataHolder serviceDataHolder = Mockito.mock(ServiceDataHolder.class);
        PowerMockito.mockStatic(ServiceDataHolder.class);
        APIManagerConfigurationService apimConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apimConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceDataHolder.getInstance()).thenReturn(serviceDataHolder);
        Mockito.when(serviceDataHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigService);
        Mockito.when(apimConfigService.getAPIManagerConfiguration()).thenReturn(apimConfig);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_USERNAME))
                .thenReturn(Constants.DEFAULT_KEY_VALIDATOR_USERNAME);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_PASSWORD))
                .thenReturn(Constants.DEFAULT_KEY_VALIDATOR_PASSWORD);
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.isDomainNameAvailable(any(String.class))).thenReturn(true);
        TenantMgtAdminService mgtAdminService = Mockito.spy(new TenantMgtAdminService());
        PowerMockito.whenNew(TenantMgtAdminService.class).withNoArguments().thenReturn(mgtAdminService);
        PowerMockito.doNothing().when(mgtAdminService).activateTenant(any(String.class));
        PowerMockito.doReturn("").when(mgtAdminService).addTenant(any(TenantInfoBean.class));
        ServerStartupListener listener = new ServerStartupListener();
        listener.completedServerStartup();
    }

    @Test
    public void completedServerStartup_invalidDomain() throws Exception {
        ServiceDataHolder serviceDataHolder = Mockito.mock(ServiceDataHolder.class);
        PowerMockito.mockStatic(ServiceDataHolder.class);
        APIManagerConfigurationService apimConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apimConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceDataHolder.getInstance()).thenReturn(serviceDataHolder);
        Mockito.when(serviceDataHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigService);
        Mockito.when(apimConfigService.getAPIManagerConfiguration()).thenReturn(apimConfig);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_USERNAME))
                .thenReturn(Constants.DEFAULT_KEY_VALIDATOR_USERNAME);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_PASSWORD))
                .thenReturn(Constants.DEFAULT_KEY_VALIDATOR_PASSWORD);
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.isDomainNameAvailable(any(String.class))).thenThrow(Exception.class);

        TenantMgtAdminService mgtAdminService = Mockito.spy(new TenantMgtAdminService());
        PowerMockito.whenNew(TenantMgtAdminService.class).withNoArguments().thenReturn(mgtAdminService);
        PowerMockito.doNothing().when(mgtAdminService).activateTenant(any(String.class));
        PowerMockito.doReturn("").when(mgtAdminService).addTenant(any(TenantInfoBean.class));

        ServerStartupListener listener = new ServerStartupListener();
        listener.completedServerStartup();
    }

    @Test
    public void completedServerStartup_domainUnavailable() throws Exception {
        ServiceDataHolder serviceDataHolder = Mockito.mock(ServiceDataHolder.class);
        PowerMockito.mockStatic(ServiceDataHolder.class);
        APIManagerConfigurationService apimConfigService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration apimConfig = Mockito.mock(APIManagerConfiguration.class);
        PowerMockito.when(ServiceDataHolder.getInstance()).thenReturn(serviceDataHolder);
        Mockito.when(serviceDataHolder.getAPIManagerConfigurationService()).thenReturn(apimConfigService);
        Mockito.when(apimConfigService.getAPIManagerConfiguration()).thenReturn(apimConfig);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_USERNAME))
                .thenReturn(Constants.DEFAULT_KEY_VALIDATOR_USERNAME);
        Mockito.when(apimConfig.getFirstProperty(Constants.KEY_VALIDATOR_PASSWORD))
                .thenReturn(Constants.DEFAULT_KEY_VALIDATOR_PASSWORD);
        PowerMockito.mockStatic(CommonUtil.class);
        PowerMockito.when(CommonUtil.isDomainNameAvailable(any(String.class))).thenReturn(false);

        TenantMgtAdminService mgtAdminService = Mockito.spy(new TenantMgtAdminService());
        PowerMockito.whenNew(TenantMgtAdminService.class).withNoArguments().thenReturn(mgtAdminService);
        PowerMockito.doNothing().when(mgtAdminService).activateTenant(any(String.class));
        PowerMockito.doReturn("").when(mgtAdminService).addTenant(any(TenantInfoBean.class));

        ServerStartupListener listener = new ServerStartupListener();
        listener.completedServerStartup();
    }

    @Test
    public void completingServerStartup() throws Exception {
        ServerStartupListener listener = new ServerStartupListener();
        listener.completingServerStartup();
    }
}
