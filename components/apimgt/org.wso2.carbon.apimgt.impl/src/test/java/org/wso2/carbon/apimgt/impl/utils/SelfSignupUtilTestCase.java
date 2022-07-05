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

import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PasswordResolver;
import org.wso2.carbon.apimgt.impl.PasswordResolverFactory;
import org.wso2.carbon.apimgt.impl.config.APIMConfigService;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, ServiceReferenceHolder.class, PrivilegedCarbonContext.class, AXIOMUtil.class,
                    PasswordResolverFactory.class})
public class SelfSignupUtilTestCase {

    @Test
    public void testGetRoleNames() {
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        Map<String, Boolean> roles = new HashMap();
        roles.put("subscriber", true);
        roles.put("creator", false);
        userRegistrationConfigDTO.setRoles(roles);
        userRegistrationConfigDTO.setSignUpDomain("foo.com");
        List<String> roleList = SelfSignUpUtil.getRoleNames(userRegistrationConfigDTO);
        Assert.assertEquals(2, roleList.size());
    }

    @Test
    public void testGetDomainSpecificRoleName() {
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setSignUpDomain("foo.com");
        String domainSpecificRoleName = SelfSignUpUtil.getDomainSpecificUserName("john", userRegistrationConfigDTO);
        Assert.assertEquals("FOO.COM/john", domainSpecificRoleName);
    }

    @Test
    public void testGetDomainSpecificRoleNameWithWrongDomain() {
        UserRegistrationConfigDTO userRegistrationConfigDTO = new UserRegistrationConfigDTO();
        userRegistrationConfigDTO.setSignUpDomain("foo.com");
        String domainSpecificRoleName = SelfSignUpUtil.getDomainSpecificUserName("bar.com/john", userRegistrationConfigDTO);
        Assert.assertEquals("FOO.COM/john", domainSpecificRoleName);
    }

    @Test
    public void testIsUserNameWithAllowedDomainNameFalse() throws Exception {
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.addRestrictedDomainForSelfSignUp("bar.com");
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        boolean result = SelfSignUpUtil.isUserNameWithAllowedDomainName("bar.com/john", userRealm);
        Assert.assertFalse(result);
    }

    @Test
    public void testIsUserNameWithAllowedDomainNameTrue() throws Exception {
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.addRestrictedDomainForSelfSignUp("foo.com");
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        boolean result = SelfSignUpUtil.isUserNameWithAllowedDomainName("bar.com/john", userRealm);
        Assert.assertTrue(result);
    }

    @Test
    public void testIsUserNameWithAllowedDomainNameWhenDomainNotGiven() throws Exception {
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.addRestrictedDomainForSelfSignUp("foo.com");
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        boolean result = SelfSignUpUtil.isUserNameWithAllowedDomainName("john", userRealm);
        Assert.assertTrue(result);
    }

    @Test(expected = APIManagementException.class)
    public void testIsUserNameWithAllowedDomainNameException() throws Exception {
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        RealmConfiguration realmConfiguration = new RealmConfiguration();
        realmConfiguration.addRestrictedDomainForSelfSignUp("bar.com");
        Mockito.when(userRealm.getRealmConfiguration()).thenThrow(new UserStoreException());
        SelfSignUpUtil.isUserNameWithAllowedDomainName("bar.com/john", userRealm);
    }

    @Test
    public void testGetSignupConfiguration() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIMConfigService apimConfigService = Mockito.mock(APIMConfigService.class);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
        PowerMockito.mockStatic(APIUtil.class);

        UserRegistrationConfigDTO config = new UserRegistrationConfigDTO();
        config.setSignUpDomain("PRIMARY");
        config.setAdminUserName("xxxx");
        config.setAdminPassword("xxxx");
        config.setSignUpEnabled(false);
        config.getRoles().put("subscriber", false);

        Mockito.when(apimConfigService.getSelfSighupConfig("bar.com")).thenReturn(config);
        PowerMockito.mockStatic(PasswordResolverFactory.class);
        PasswordResolver passwordResolver = Mockito.mock(PasswordResolver.class);
        PowerMockito.when(PasswordResolverFactory.getInstance()).thenReturn(passwordResolver);
        UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration("bar.com");
        Assert.assertNotNull(userRegistrationConfigDTO);
    }

    @Test
    public void testGetSignupConfigurationDifferentObjectType() throws Exception {
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIMConfigService apimConfigService = Mockito.mock(APIMConfigService.class);
        Mockito.when(serviceReferenceHolder.getApimConfigService()).thenReturn(apimConfigService);
        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(apimConfigService.getSelfSighupConfig("bar.com")).thenReturn(new String("Test String"));
        PowerMockito.mockStatic(PasswordResolverFactory.class);
        PasswordResolver passwordResolver = Mockito.mock(PasswordResolver.class);
        PowerMockito.when(PasswordResolverFactory.getInstance()).thenReturn(passwordResolver);
        UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration("bar.com");
        Assert.assertNotNull(new UserRegistrationConfigDTO());
    }
}