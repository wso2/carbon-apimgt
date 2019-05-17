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

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api;

import org.json.simple.JSONArray;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dao.OnPremiseGatewayDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.exceptions.AuthenticationException;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils.AuthDTO;
import org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils.AuthenticatorUtil;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dao.UploadedUsageFileInfoDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.usage.publisher.dto.UploadedFileInfoDTO;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;


import static org.mockito.Matchers.any;

/**
 * LifecycleEventPublishingService Test Class
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({AuthenticatorUtil.class, OnPremiseGatewayDAO.class, UploadedUsageFileInfoDAO.class})
public class MicroGatewayRestAPITest {
    private final String tenantDomain = "testORG";
    private final String apiId = "user@gmail.com-AT-testORG-PhoneVerification-1.0.0";
    public final String fileNameheader = "FileName";

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void getUpdatedApis() throws Exception {
        AuthDTO authDTO = Mockito.mock(AuthDTO.class);
        PowerMockito.mockStatic(AuthenticatorUtil.class);
        PowerMockito.when(AuthenticatorUtil.authorizeUser(any(HttpHeaders.class))).thenReturn(authDTO);
        Mockito.when(authDTO.isAuthenticated()).thenReturn(true);
        Mockito.when(authDTO.getTenantDomain()).thenReturn(tenantDomain);
        Mockito.when(authDTO.getResponseStatus()).thenReturn(Response.Status.UNAUTHORIZED);

        OnPremiseGatewayDAO onPremiseGatewayDao = Mockito.mock(OnPremiseGatewayDAO.class);
        PowerMockito.mockStatic(OnPremiseGatewayDAO.class);
        PowerMockito.when(OnPremiseGatewayDAO.getInstance()).thenReturn(onPremiseGatewayDao);

        JSONArray apiIds = new JSONArray();
        apiIds.add(apiId);
        Mockito.when(onPremiseGatewayDao.getAPIPublishEvents(any(String.class))).thenReturn(apiIds);

        MicroGatewayRestAPI microGwAPI = new MicroGatewayRestAPI();
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Response response = microGwAPI.getUpdatedApis(httpHeaders);

        Assert.assertEquals(200, response.getStatus());
    }

    @Test
    public void uploadFile() throws Exception {
        AuthDTO authDTO = Mockito.mock(AuthDTO.class);
        PowerMockito.mockStatic(AuthenticatorUtil.class);
        PowerMockito.when(AuthenticatorUtil.authorizeUser(any(HttpHeaders.class))).thenReturn(authDTO);
        Mockito.when(authDTO.isAuthenticated()).thenReturn(true);
        Mockito.when(authDTO.getTenantDomain()).thenReturn(tenantDomain);
        Mockito.when(authDTO.getResponseStatus()).thenReturn(Response.Status.UNAUTHORIZED);
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());
        String uploadedFileName = "api-usage-data.dat.1518413760007.zip";

        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        List<String> fileNameHeader = new ArrayList<>();
        fileNameHeader.add(uploadedFileName);
        Mockito.when(httpHeaders.getRequestHeader(fileNameheader)).thenReturn(fileNameHeader);
        PowerMockito.spy(UploadedUsageFileInfoDAO.class);
        PowerMockito.doNothing()
                .when(UploadedUsageFileInfoDAO.class, "persistFileUpload", any(UploadedFileInfoDTO.class),
                        any(InputStream.class));
        MicroGatewayRestAPI microGwAPI = new MicroGatewayRestAPI();
        Response response = microGwAPI.uploadFile(anyInputStream, httpHeaders);
        Assert.assertEquals(201, response.getStatus());
    }

    @Test
    public void getUpdatedApis_UnauthorizedUser() throws Exception {
        AuthDTO authDTO = Mockito.mock(AuthDTO.class);
        PowerMockito.mockStatic(AuthenticatorUtil.class);
        PowerMockito.when(AuthenticatorUtil.authorizeUser(any(HttpHeaders.class))).thenReturn(authDTO);
        Mockito.when(authDTO.isAuthenticated()).thenReturn(false);
        Mockito.when(authDTO.getResponseStatus()).thenReturn(Response.Status.UNAUTHORIZED);
        Mockito.when(authDTO.getMessage()).thenReturn("Unauthorized user.");

        OnPremiseGatewayDAO onPremiseGatewayDao = Mockito.mock(OnPremiseGatewayDAO.class);
        PowerMockito.mockStatic(OnPremiseGatewayDAO.class);
        PowerMockito.when(OnPremiseGatewayDAO.getInstance()).thenReturn(onPremiseGatewayDao);

        JSONArray apiIds = new JSONArray();
        apiIds.add(apiId);
        Mockito.when(onPremiseGatewayDao.getAPIPublishEvents(any(String.class))).thenReturn(apiIds);

        MicroGatewayRestAPI lifeCyclePubServ = new MicroGatewayRestAPI();
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Response response = lifeCyclePubServ.getUpdatedApis(httpHeaders);

        Assert.assertEquals(401, response.getStatus());
    }

    @Test
    public void getUpdatedApis_Exception() throws Exception {
        AuthenticationException exception = Mockito.mock(AuthenticationException.class);
        PowerMockito.mockStatic(AuthenticatorUtil.class);
        PowerMockito.when(AuthenticatorUtil.authorizeUser(any(HttpHeaders.class))).thenThrow(exception);

        MicroGatewayRestAPI lifeCyclePubServ = new MicroGatewayRestAPI();
        HttpHeaders httpHeaders = Mockito.mock(HttpHeaders.class);
        Response response = lifeCyclePubServ.getUpdatedApis(httpHeaders);
        Assert.assertEquals(500, response.getStatus());
    }
}
