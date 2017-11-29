/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.dao.impl.ThreatProtectionDAOImpl;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.SampleTestObjectCreator;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ APIManagerFactory.class, RestApiUtil.class, DAOFactory.class })
public class ThreatProtectionApiServiceImplTestCase {

    @Test
    public void threatProtectionApisApiIdPolicyGetTestCase() throws Exception {
        PowerMockito.mockStatic(DAOFactory.class);
        ThreatProtectionDAOImpl dao = Mockito.mock(ThreatProtectionDAOImpl.class);
        Set<String> ids = new HashSet<>();
        ids.add("A");
        ids.add("B");
        Mockito.when(dao.getThreatProtectionPolicyIdsForApi("API")).thenReturn(ids);
        Mockito.when(DAOFactory.getThreatProtectionDAO()).thenReturn(dao);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPolicyGet("API", getRequest());
        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        Set<String> ids2 = (Set<String>)response.getEntity();
        Assert.assertEquals(ids.size(), ids2.size());
    }

    @Test
    public void threatProtectionApisApiIdPolicyGetExceptionTestCase() throws Exception {
        PowerMockito.mockStatic(DAOFactory.class);
        ThreatProtectionDAOImpl dao = Mockito.mock(ThreatProtectionDAOImpl.class);
        APIMgtDAOException exception = Mockito.mock(APIMgtDAOException.class);
        Mockito.when(dao.getThreatProtectionPolicyIdsForApi("API")).thenThrow(exception);
        Mockito.when(DAOFactory.getThreatProtectionDAO()).thenReturn(dao);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPolicyGet("API", getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPoliciesGetTestCase() throws Exception {
        APIMgtAdminServiceImpl apiMgtAdminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);
        PowerMockito.when(apiManagerFactory.getAPIMgtAdminService()).thenReturn(apiMgtAdminService);

        List<ThreatProtectionPolicy> list = new ArrayList<>();
        list.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());
        list.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());
        list.add(SampleTestObjectCreator.createUniqueThreatProtectionPolicy());

        PowerMockito.when(apiMgtAdminService.getThreatProtectionPolicyList()).thenReturn(list);

        ThreatProtectionApiServiceImpl threatProtectionApiService = new ThreatProtectionApiServiceImpl();
        Response response = threatProtectionApiService.threatProtectionPoliciesGet(getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());

        int apiCount = ( (ThreatProtectionPolicyListDTO) response.getEntity()).getList().size();
        Assert.assertEquals(apiCount, 3);
    }

    @Test
    public void threatProtectionPoliciesGetExceptionTestCase() throws Exception {
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory apiManagerFactory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(apiManagerFactory);

        APIManagementException apiManagementException = new APIManagementException("Error");
        PowerMockito.when(apiManagerFactory.getAPIMgtAdminService()).thenThrow(apiManagementException);

        ThreatProtectionApiServiceImpl threatProtectionApiService = new ThreatProtectionApiServiceImpl();
        Response response = threatProtectionApiService.threatProtectionPoliciesGet(getRequest());

        Assert.assertEquals(response.getStatus(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    public void threatProtectionPolicyPostTestCase() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory factory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(factory);
        Mockito.when(factory.getAPIMgtAdminService()).thenReturn(adminService);

        ThreatProtectionPolicyDTO dto = Mockito.mock(ThreatProtectionPolicyDTO.class);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPolicyPost(dto, getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPolicyPostExceptionTestCase() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory factory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(factory);
        Mockito.when(factory.getAPIMgtAdminService()).thenReturn(adminService);
        Mockito.doThrow(new APIManagementException("ERROR")).when(adminService).addThreatProtectionPolicy(Mockito.any());
        ThreatProtectionPolicyDTO dto = Mockito.mock(ThreatProtectionPolicyDTO.class);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPolicyPost(dto, getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPolicyThreatProtectionPolicyIdDeleteTestCase() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory factory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(factory);
        Mockito.when(factory.getAPIMgtAdminService()).thenReturn(adminService);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPolicyThreatProtectionPolicyIdDelete("POLICY", getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPolicyThreatProtectionPolicyIdDeleteExceptionTestCase() throws Exception {
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(APIManagerFactory.class);
        APIManagerFactory factory = Mockito.mock(APIManagerFactory.class);
        PowerMockito.when(APIManagerFactory.getInstance()).thenReturn(factory);
        Mockito.when(factory.getAPIMgtAdminService()).thenReturn(adminService);

        Mockito.doThrow(new APIManagementException("ERROR")).when(adminService).deleteThreatProtectionPolicy("POLICY");

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPolicyThreatProtectionPolicyIdDelete("POLICY", getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }



    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }
}
