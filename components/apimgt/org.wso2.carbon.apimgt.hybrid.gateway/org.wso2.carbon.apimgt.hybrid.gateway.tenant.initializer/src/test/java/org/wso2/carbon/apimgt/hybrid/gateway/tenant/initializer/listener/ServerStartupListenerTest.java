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

package org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.listener;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.constants.Constants;
import org.wso2.carbon.apimgt.hybrid.gateway.tenant.initializer.internal.ServiceDataHolder;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.util.CommonUtil;
import org.wso2.carbon.tenant.mgt.services.TenantMgtAdminService;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import static org.mockito.Matchers.any;

/**
 * ServerStartupListener Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CarbonUtils.class, CommonUtil.class, ServerStartupListener.class, ServiceDataHolder.class,
        ServiceReferenceHolder.class, RealmService.class})
public class ServerStartupListenerTest {
    @Before
    public void setUp() throws Exception {
        TestUtil testUtil = new TestUtil();
        testUtil.setupCarbonHome();
    }

    @Test
    public void completedServerStartup() throws Exception {
        mockCommonCases();
        PowerMockito.when(CommonUtil.isDomainNameAvailable(any(String.class))).thenReturn(true);
        ServerStartupListener listener = new ServerStartupListener();
        listener.completedServerStartup();
    }

    @Test
    public void completedServerStartup_invalidDomain() throws Exception {
        mockCommonCases();
        PowerMockito.when(CommonUtil.isDomainNameAvailable(any(String.class))).thenThrow(Exception.class);
        ServerStartupListener listener = new ServerStartupListener();
        listener.completedServerStartup();
    }

    @Test
    public void completedServerStartup_domainUnavailable() throws Exception {
        mockCommonCases();
        PowerMockito.when(CommonUtil.isDomainNameAvailable(any(String.class))).thenReturn(false);
        ServerStartupListener listener = new ServerStartupListener();
        listener.completedServerStartup();
    }

    @Test
    public void completingServerStartup() throws Exception {
        ServerStartupListener listener = new ServerStartupListener();
        listener.completingServerStartup();
    }

    private void mockCommonCases() throws Exception {
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
        TenantMgtAdminService mgtAdminService = Mockito.spy(new TenantMgtAdminService());
        PowerMockito.whenNew(TenantMgtAdminService.class).withNoArguments().thenReturn(mgtAdminService);
        PowerMockito.doNothing().when(mgtAdminService).activateTenant(any(String.class));
        PowerMockito.doReturn("").when(mgtAdminService).addTenant(any(TenantInfoBean.class));
        AxisConfiguration axisConfiguration = Mockito.mock(AxisConfiguration.class);
        ConfigurationContextService contextService = Mockito.mock(ConfigurationContextService.class);
        ConfigurationContext context = Mockito.mock(ConfigurationContext.class);
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder referenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(referenceHolder);
        Mockito.when(ServiceReferenceHolder.getContextService()).thenReturn(contextService);
        Mockito.when(contextService.getServerConfigContext()).thenReturn(context);
        Mockito.when(context.getAxisConfiguration()).thenReturn(axisConfiguration);
        PowerMockito.mockStatic(RealmService.class);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceDataHolder.getRealmService()).thenReturn(realmService);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        RealmConfiguration configuration = Mockito.mock(RealmConfiguration.class);
        Mockito.when(realmService.getTenantUserRealm(any(Integer.class))).thenReturn(userRealm);
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(configuration);
        Mockito.when(configuration.getAdminUserName()).thenReturn(Constants.ADMIN_USERNAME);
        Mockito.when(configuration.getAdminPassword()).thenReturn(Constants.ADMIN_PASSWORD);
    }
}
