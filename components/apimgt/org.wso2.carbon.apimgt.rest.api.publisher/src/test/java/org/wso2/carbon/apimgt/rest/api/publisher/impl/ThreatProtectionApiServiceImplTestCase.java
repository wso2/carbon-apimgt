/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(PowerMockRunner.class)
@PrepareForTest({RestAPIPublisherUtil.class, RestApiUtil.class, API.class})
public class ThreatProtectionApiServiceImplTestCase {

    @Test
    public void threatProtectionApisApiIdPoliciesPolicyIdPostTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPoliciesPolicyIdPost("ANY",
                "ANY", getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionApisApiIdPoliciesPolicyIdPostExceptionTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);
        Mockito.doThrow(new APIManagementException("ERROR")).when(publisher).addThreatProtectionPolicy(
                Mockito.anyString(), Mockito.anyString());

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPoliciesPolicyIdPost("ANY",
                "ANY", getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionApisApiIdPoliciesGetTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        API api = PowerMockito.mock(API.class);
        Mockito.when(publisher.getAPIbyUUID(Mockito.anyString())).thenReturn(api);
        Set<String> set = new HashSet<>();
        set.add("AB");
        set.add("CD");
        Mockito.when(api.getThreatProtectionPolicies()).thenReturn(set);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPoliciesGet("ANY", getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        Set<String> result = (Set<String>) response.getEntity();
        Assert.assertTrue(result.contains("AB"));
        Assert.assertTrue(result.contains("CD"));
    }

    @Test
    public void threatProtectionApisApiIdPoliciesExceptionGetTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        Mockito.when(publisher.getAPIbyUUID(Mockito.anyString())).thenThrow(new APIManagementException("ERROR"));

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPoliciesGet("ANY", getRequest());

        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPoliciesGetTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        List<ThreatProtectionPolicy> list = new ArrayList<>();
        list.add(Mockito.mock(ThreatProtectionPolicy.class));
        list.add(Mockito.mock(ThreatProtectionPolicy.class));

        Mockito.when(publisher.getThreatProtectionPolicies()).thenReturn(list);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPoliciesGet(getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<ThreatProtectionPolicy> resList = (List<ThreatProtectionPolicy>) response.getEntity();
        Assert.assertEquals(2, resList.size());
    }

    @Test
    public void threatProtectionPoliciesExceptionGetTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenThrow(new APIManagementException("ERROR"));

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPoliciesGet(getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPoliciesPolicyIdGetTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        ThreatProtectionPolicy policy = Mockito.mock(ThreatProtectionPolicy.class);
        Mockito.when(publisher.getThreatProtectionPolicy(Mockito.anyString())).thenReturn(policy);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPoliciesPolicyIdGet("POLICY", getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPoliciesPolicyIdGetNullPolicyTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        Mockito.when(publisher.getThreatProtectionPolicy(Mockito.anyString())).thenReturn(null);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPoliciesPolicyIdGet("POLICY", getRequest());
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionPoliciesPolicyIdGetExceptionTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        Mockito.when(publisher.getThreatProtectionPolicy(Mockito.anyString())).thenThrow(
                new APIManagementException("ERROR"));

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionPoliciesPolicyIdGet("POLICY", getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionApisApiIdPoliciesPolicyIdDeleteTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPoliciesPolicyIdDelete(
                "API", "POLICY", getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void threatProtectionApisApiIdPoliciesPolicyIdDeleteExceptionTestCase() throws Exception {
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getLoggedInUsername(Mockito.any())).thenReturn("username");
        PowerMockito.mockStatic(RestAPIPublisherUtil.class);
        APIPublisher publisher = Mockito.mock(APIPublisher.class);
        Mockito.when(RestAPIPublisherUtil.getApiPublisher(Mockito.anyString())).thenReturn(publisher);

        Mockito.doThrow(new APIManagementException("ERROR")).when(publisher).deleteThreatProtectionPolicy(Mockito.anyString(), Mockito.anyString());

        ThreatProtectionApiServiceImpl apiService = new ThreatProtectionApiServiceImpl();
        Response response = apiService.threatProtectionApisApiIdPoliciesPolicyIdDelete(
                "API", "POLICY", getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    private Request getRequest() throws Exception {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);
        PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        return request;
    }
}
