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

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.TestUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.exceptions.AuthenticationException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.RealmConfiguration;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.mockito.Matchers.any;

/**
 * AuthenticatorUtil Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({PrivilegedCarbonContext.class, CarbonContext.class})
public class AuthenticatorUtilTest {

    @Before
    public void setUp() throws Exception {
        TestUtil util = new TestUtil();
        util.setupCarbonHome();
    }

    @Test
    public void authorizeUser() throws Exception {
        List<String> authorization = new ArrayList<>();
        authorization.add("OGpvbmExakBnb29nbC5pZ2cuYml6QGNjYzIyMjI6QW1hbmRhMTI=");
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.doReturn(authorization).when(httpHeaders).getRequestHeader("Authorization");

        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);

        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.doReturn(true).when(userStoreManager).authenticate(any(String.class), any(String.class));

        RealmConfiguration realmConfiguration = Mockito.mock(RealmConfiguration.class);
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        Mockito.doReturn("admin").when(realmConfiguration).getAdminRoleName();

        String[] userRoles = new String[2];
        userRoles[0] = "admin";
        userRoles[1] = "publisher";
        Mockito.doReturn(userRoles).when(userStoreManager).getRoleListOfUser(any(String.class));

        AuthDTO response = AuthenticatorUtil.authorizeUser(httpHeaders);
        Assert.assertEquals(Response.Status.OK, response.getResponseStatus());
    }

    @Test
    public void authorizeUser_unauthroizedUser() throws Exception {
        List<String> authorization = new ArrayList<>();
        authorization.add("OGpvbmExakBnb29nbC5pZ2cuYml6QGNjYzIyMjI6QW1hbmRhMTI=");
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.doReturn(authorization).when(httpHeaders).getRequestHeader("Authorization");

        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);

        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenReturn(userStoreManager);
        Mockito.doReturn(true).when(userStoreManager).authenticate(any(String.class), any(String.class));

        RealmConfiguration realmConfiguration = Mockito.mock(RealmConfiguration.class);
        Mockito.when(userRealm.getRealmConfiguration()).thenReturn(realmConfiguration);
        Mockito.doReturn("admin").when(realmConfiguration).getAdminRoleName();

        String[] userRoles = new String[2];
        userRoles[0] = "subscriber";
        userRoles[1] = "publisher";

        Mockito.doReturn(userRoles).when(userStoreManager).getRoleListOfUser(any(String.class));

        AuthDTO response = AuthenticatorUtil.authorizeUser(httpHeaders);
        Assert.assertEquals(Response.Status.UNAUTHORIZED, response.getResponseStatus());
    }

    @Test(expected = AuthenticationException.class)
    public void authorizeUser_throwsException() throws Exception {
        List<String> authorization = new ArrayList<>();
        authorization.add("OGpvbmExakBnb29nbC5pZ2cuYml6QGNjYzIyMjI6QW1hbmRhMTI=");
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Mockito.doReturn(authorization).when(httpHeaders).getRequestHeader("Authorization");

        PrivilegedCarbonContext privilegedCarbonContext = Mockito.mock(PrivilegedCarbonContext.class);
        PowerMockito.mockStatic(PrivilegedCarbonContext.class);
        PowerMockito.when(PrivilegedCarbonContext.getThreadLocalCarbonContext()).thenReturn(privilegedCarbonContext);

        UserStoreManager userStoreManager = Mockito.mock(UserStoreManager.class);
        UserRealm userRealm = Mockito.mock(UserRealm.class);
        CarbonContext carbonContext = Mockito.mock(CarbonContext.class);
        PowerMockito.mockStatic(CarbonContext.class);
        PowerMockito.when(CarbonContext.getThreadLocalCarbonContext()).thenReturn(carbonContext);
        Mockito.when(carbonContext.getUserRealm()).thenReturn(userRealm);
        Mockito.when(userRealm.getUserStoreManager()).thenThrow(UserStoreException.class);

        AuthenticatorUtil.authorizeUser(httpHeaders);
    }
}
