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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.PasswordResolver;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.PasswordResolverFactory;
import org.wso2.carbon.apimgt.impl.dto.UserRegistrationConfigDTO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import static org.wso2.carbon.base.CarbonBaseConstants.CARBON_HOME;

@RunWith(PowerMockRunner.class)
@PrepareForTest({APIUtil.class, ServiceReferenceHolder.class, PrivilegedCarbonContext.class, AXIOMUtil.class,
                    PasswordResolverFactory.class})
public class SelfSignupUtilTestCase {

    private  Registry registry = Mockito.mock(Registry.class);



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
    public void testGetSelfSignupConfigFromRegistry() throws Exception {

        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("foo.com");
        Mockito.when(privilegedCarbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE)).thenReturn(registry);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("foo.com")).thenReturn(4444);

        PowerMockito.mockStatic(APIUtil.class);

        Mockito.when(registry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)).thenReturn(true);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getContent()).thenReturn("wsdl".getBytes());
        Mockito.when(registry.get(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)).thenReturn(resource);
        OMElement omElement = Mockito.mock(OMElement.class);
        Mockito.when(omElement.getFirstChildWithName(Matchers.any(QName.class))).thenReturn(omElement);
        PowerMockito.mockStatic(AXIOMUtil.class);
        Mockito.when(omElement.getChildrenWithLocalName(APIConstants.SELF_SIGN_UP_REG_ROLE_ELEM)).thenReturn(Mockito.mock(Iterator.class));
        PowerMockito.when(AXIOMUtil.stringToOM("wsdl")).thenReturn(omElement);
        PowerMockito.mockStatic(PasswordResolverFactory.class);
        PasswordResolver passwordResolver = Mockito.mock(PasswordResolver.class);
        PowerMockito.when(PasswordResolverFactory.getInstance()).thenReturn(passwordResolver);
        UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration("foo.com");

        Assert.assertNotNull(userRegistrationConfigDTO);
    }



    @Test
    public void testGetSelfSignupConfigFromRegistryTenant() throws Exception {
        System.setProperty(CARBON_HOME, "");
        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);
        Mockito.when(privilegedCarbonContext.getTenantDomain()).thenReturn("foo.com");
        Mockito.when(privilegedCarbonContext.getRegistry(RegistryType.SYSTEM_GOVERNANCE)).thenReturn(registry);

        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        RealmService realmService = Mockito.mock(RealmService.class);
        Mockito.when(serviceReferenceHolder.getRealmService()).thenReturn(realmService);
        TenantManager tenantManager = Mockito.mock(TenantManager.class);
        Mockito.when(realmService.getTenantManager()).thenReturn(tenantManager);
        Mockito.when(tenantManager.getTenantId("foo.com")).thenReturn(4444);

        PowerMockito.mockStatic(APIUtil.class);
        Mockito.when(registry.resourceExists(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)).thenReturn(true);
        Resource resource = Mockito.mock(Resource.class);
        Mockito.when(resource.getContent()).thenReturn("wsdl".getBytes());
        Mockito.when(registry.get(APIConstants.SELF_SIGN_UP_CONFIG_LOCATION)).thenReturn(resource);
        OMElement omElement = Mockito.mock(OMElement.class);
        Mockito.when(omElement.getFirstChildWithName(Matchers.any(QName.class))).thenReturn(omElement);
        PowerMockito.mockStatic(AXIOMUtil.class);
        Mockito.when(omElement.getChildrenWithLocalName(APIConstants.SELF_SIGN_UP_REG_ROLE_ELEM)).thenReturn(Mockito.mock(Iterator.class));
        PowerMockito.when(AXIOMUtil.stringToOM("wsdl")).thenReturn(omElement);
        PowerMockito.mockStatic(PasswordResolverFactory.class);
        PasswordResolver passwordResolver = Mockito.mock(PasswordResolver.class);
        PowerMockito.when(PasswordResolverFactory.getInstance()).thenReturn(passwordResolver);
        UserRegistrationConfigDTO userRegistrationConfigDTO = SelfSignUpUtil.getSignupConfiguration("bar.com");
        Assert.assertNotNull(userRegistrationConfigDTO);
        PowerMockito.verifyStatic(PrivilegedCarbonContext.class);
        PrivilegedCarbonContext.endTenantFlow();
    }
}