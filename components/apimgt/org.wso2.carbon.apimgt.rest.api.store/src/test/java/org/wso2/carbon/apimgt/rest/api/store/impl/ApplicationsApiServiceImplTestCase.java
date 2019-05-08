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

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.rest.api.store.ApplicationsApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationScopeDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor({"org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil",
        "org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils", 
        "org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils"})
@PrepareForTest({ RestApiUtil.class, RestAPIStoreUtils.class,
        org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils.class })
public class ApplicationsApiServiceImplTestCase {
    private final String ADMIN_USERNAME = "admin";
    private ApplicationsApiService applicationsApiService;
    private APIConsumer apiConsumer;
    private final String NON_EXISTING_APPLICATION = "NON_EXISTING";

    @Before
    public void init() {
        applicationsApiService = new ApplicationsApiServiceImpl();
        apiConsumer = Mockito.mock(APIConsumer.class);
    }

    /**
     * This test case tests the functionality of applicationsApplicationScopeGet, when the application does not exist.
     * Checks whether the request does not throw any run time exceptions, even when the application does not exist.
     *
     * @throws Exception Exception.
     */
    @Test()
    public void testapplicationsApplicationScopesGetForNotExistingApplication() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.doReturn(ADMIN_USERNAME).when(RestApiUtil.class, "getLoggedInUsername");
        PowerMockito.doReturn(apiConsumer).when(RestApiUtil.class, "getConsumer", ADMIN_USERNAME);
        Mockito.doReturn(null).when(apiConsumer).getApplicationByUUID(NON_EXISTING_APPLICATION);
        applicationsApiService.applicationsScopesApplicationIdGet(NON_EXISTING_APPLICATION, false, null, null);
    }

    /**
     * This method tests the functionality of applicationsApplicationScopeGet, when the user is not authorized to
     * view the application.
     *
     * @throws Exception Exception.
     */
    @Test
    public void testapplicationsApplicationScopeGetForUnAuthorizedUser() throws Exception {
        Application application = new Application(1);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(RestAPIStoreUtils.class);
        PowerMockito.doReturn(ADMIN_USERNAME).when(RestApiUtil.class, "getLoggedInUsername");
        PowerMockito.doReturn(apiConsumer).when(RestApiUtil.class, "getConsumer", ADMIN_USERNAME);
        Mockito.doReturn(application).when(apiConsumer).getApplicationByUUID(NON_EXISTING_APPLICATION);
        PowerMockito.doReturn(false).when(RestAPIStoreUtils.class, "isUserAccessAllowedForApplication", application);
        applicationsApiService.applicationsScopesApplicationIdGet(NON_EXISTING_APPLICATION, false, null, null);
    }

    /**
     * This method tests the functionality of applicationsApplicationScopeGet, when all the relevant conditions are met.
     *
     * @throws Exception Exception.
     */
    @Test
    public void testapplicationsApplicationScopeGet() throws Exception {
        Application application = new Application(1);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.mockStatic(RestAPIStoreUtils.class);
        PowerMockito.mockStatic(org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils.class);
        PowerMockito.doReturn(ADMIN_USERNAME).when(RestApiUtil.class, "getLoggedInUsername");
        PowerMockito.doReturn(apiConsumer).when(RestApiUtil.class, "getConsumer", ADMIN_USERNAME);
        Mockito.doReturn(application).when(apiConsumer).getApplicationByUUID(NON_EXISTING_APPLICATION);
        PowerMockito.doReturn(true).when(RestAPIStoreUtils.class, "isUserAccessAllowedForApplication", application);
        applicationsApiService.applicationsScopesApplicationIdGet(NON_EXISTING_APPLICATION, false, null, null);
        ScopeListDTO scopeListDTO = new ScopeListDTO();
        ApplicationScopeDTO applicationScopeDTO = new ApplicationScopeDTO();
        applicationScopeDTO.setKey("admin");
        List<ApplicationScopeDTO> applicationScopeDTOList = new ArrayList<>();
        applicationScopeDTOList.add(applicationScopeDTO);
        scopeListDTO.setList(applicationScopeDTOList);
        PowerMockito.doReturn(scopeListDTO)
                .when(org.wso2.carbon.apimgt.rest.api.store.utils.RestAPIStoreUtils.class, "getScopesForApplication", Mockito.anyString(),
                        Mockito.any(Application.class), Mockito.anyBoolean());
        Response response = applicationsApiService
                .applicationsScopesApplicationIdGet(NON_EXISTING_APPLICATION, false, null, null);
        Assert.assertEquals("Scope retrieval did not succeed with the correct inputs", response.getStatus(),
                Response.Status.OK.getStatusCode());
    }

    /**
     * This method tests the functionality of applicationsApplicationScopeGet, when there is an exception thrown.
     */
    @Test
    public void testapplicationsApplicationScopeGetNegativeScenario1() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.doReturn(ADMIN_USERNAME).when(RestApiUtil.class, "getLoggedInUsername");
        PowerMockito.doThrow(new APIManagementException("error"))
                .when(RestApiUtil.class, "getConsumer", ADMIN_USERNAME);
        Response response = applicationsApiService
                .applicationsScopesApplicationIdGet(NON_EXISTING_APPLICATION, false, null, null);
        Assert.assertNull("API call succeeded for wrong inputs", response);

        PowerMockito.doThrow(new APIManagementException("MultiTenantUserAdmin"))
                .when(RestApiUtil.class, "getConsumer", ADMIN_USERNAME);
        response = applicationsApiService
                .applicationsScopesApplicationIdGet(NON_EXISTING_APPLICATION, false, null, null);
        Assert.assertNull("API call succeeded for wrong inputs", response);
    }

}
