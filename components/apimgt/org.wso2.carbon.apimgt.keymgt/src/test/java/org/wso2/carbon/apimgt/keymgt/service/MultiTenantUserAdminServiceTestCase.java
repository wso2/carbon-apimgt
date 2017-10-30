/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.keymgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

/**
 * This is a test case for {@link MultiTenantUserAdminService}
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( {IdentityTenantUtil.class, PrivilegedCarbonContext.class, APIKeyMgtDataHolder.class})
public class MultiTenantUserAdminServiceTestCase {
    private static final String ADMIN_USER = "admin";
    private static final Log log = LogFactory.getLog(MultiTenantUserAdminServiceTestCase.class);
    private MultiTenantUserAdminService multiTenantUserAdminService;
    private PrivilegedCarbonContext privilegedCarbonContext;
    private UserRealm userRealm;
    private UserStoreManager userStoreManager;
    private RealmService realmService;

    @Before
    public void init() {
        multiTenantUserAdminService = new MultiTenantUserAdminService();
        //Need to set carbon home to initialize the PrivilegedCarbonContext.
        System.setProperty("carbon.home", ".");
        privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        userRealm = Mockito.mock(UserRealm.class);
        userStoreManager = Mockito.mock(UserStoreManager.class);
        realmService = Mockito.mock(RealmService.class);
    }

    /**
     * This is a test case to test GetUserRoleList method.
     */
    @Test
    public void testGetUserRoleList() throws Exception {
        log.info("Running the testGetUserRoleList test case.");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(IdentityTenantUtil.class);
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        Mockito.doReturn(userRealm).when(realmService).getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID);
        Mockito.doReturn(userStoreManager).when(userRealm).getUserStoreManager();
        Mockito.doReturn(new String[] { ADMIN_USER }).when(userStoreManager).getRoleListOfUser(ADMIN_USER);
        Mockito.doReturn(MultitenantConstants.SUPER_TENANT_ID).when(privilegedCarbonContext).getTenantId();
        PowerMockito.doReturn(privilegedCarbonContext)
                .when(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext");
        PowerMockito.doReturn(realmService).when(APIKeyMgtDataHolder.class, "getRealmService");
        PowerMockito.doReturn(MultitenantConstants.SUPER_TENANT_ID)
                .when(IdentityTenantUtil.class, "getTenantIdOfUser", ADMIN_USER);
        String[] userRoleList = multiTenantUserAdminService.getUserRoleList(ADMIN_USER);
        Assert.assertEquals("UserRoleList retrieval returned wrong list of user roles", 1, userRoleList.length);
        Assert.assertEquals("UserRoleList contains different list of roles", userRoleList[0], ADMIN_USER);

        // Checking the behaviour of the MultiTenantUserAdmin Service when it is accessed as a tenant admin.
        Mockito.doReturn(1).when(privilegedCarbonContext).getTenantId();
        userRoleList = multiTenantUserAdminService.getUserRoleList(ADMIN_USER);
        Assert.assertEquals("UserRoleList retrieval from MultiTenantAdminService works for all the tenants", 0,
                userRoleList.length);
        log.info("Successfully completed testGetUserRoleList test case.");
    }

    /**
     * This is a test case to test the behaviour of testGetUserRoleList under negative circumstances.
     * @throws Exception Exception.
     */
    @Test(expected = APIKeyMgtException.class)
    public void testGetUserRoleListNegativeScenario1() throws Exception {
        log.info("Running testGetUserListNegativeScenario1.");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(IdentityTenantUtil.class);
        PowerMockito.doReturn(privilegedCarbonContext)
                .when(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext");
        Mockito.doReturn(MultitenantConstants.SUPER_TENANT_ID).when(privilegedCarbonContext).getTenantId();
        PowerMockito.doThrow(new IdentityRuntimeException("Identity run-time exception")).when(IdentityTenantUtil
                .class, "getTenantIdOfUser", ADMIN_USER);
        multiTenantUserAdminService.getUserRoleList(ADMIN_USER);
        log.info("Successfully completed testGetUserListNegativeScenario1 test case.");
    }

    /**
     * This is a test case to test the behaviour of testGetUserRoleList under negative circumstances.
     * @throws Exception Exception.
     */
    @Test(expected = APIKeyMgtException.class)
    public void testGetUserRoleListNegativeScenario2() throws Exception {
        log.info("Running testGetUserListNegativeScenario2.");
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(IdentityTenantUtil.class);
        PowerMockito.mockStatic(APIKeyMgtDataHolder.class);
        PowerMockito.doReturn(privilegedCarbonContext)
                .when(PrivilegedCarbonContext.class, "getThreadLocalCarbonContext");
        PowerMockito.doReturn(realmService).when(APIKeyMgtDataHolder.class, "getRealmService");
        Mockito.doReturn(MultitenantConstants.SUPER_TENANT_ID).when(privilegedCarbonContext).getTenantId();
        PowerMockito.doReturn(MultitenantConstants.SUPER_TENANT_ID)
                .when(IdentityTenantUtil.class, "getTenantIdOfUser", ADMIN_USER);
        Mockito.doThrow(new UserStoreException()).when(realmService).getTenantUserRealm(Mockito.anyInt());
        multiTenantUserAdminService.getUserRoleList(ADMIN_USER);
        log.info("Successfully completed testGetUserListNegativeScenario2 test case.");
    }
}
