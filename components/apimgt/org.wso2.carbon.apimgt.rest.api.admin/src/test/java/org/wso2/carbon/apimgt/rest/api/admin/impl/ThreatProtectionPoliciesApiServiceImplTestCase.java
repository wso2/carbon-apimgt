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
 */

package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThreatProtectionPolicyDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ThreatProtectionPoliciesApiServiceImplTestCase {

    @Test
    public void testThreatProtectionPoliciesGet() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        List<ThreatProtectionPolicy> policyList = new ArrayList<>();
        policyList.add(createThreatProtectionPolicy());
        policyList.add(createThreatProtectionPolicy());
        policyList.add(createThreatProtectionPolicy());

        Mockito.when(adminService.getThreatProtectionPolicyList()).thenReturn(policyList);

        ThreatProtectionPoliciesApiServiceImpl apiService = new ThreatProtectionPoliciesApiServiceImpl(adminService);
        Response response = apiService.threatProtectionPoliciesGet(getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        //Error path
        Mockito.when(adminService.getThreatProtectionPolicyList()).thenThrow(APIManagementException.class);
        response = apiService.threatProtectionPoliciesGet(getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testThreatProtectionPoliciesPost() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        ThreatProtectionPolicyDTO dto = createThreatProtectionPolicyDTO();
        ThreatProtectionPoliciesApiServiceImpl apiService = new ThreatProtectionPoliciesApiServiceImpl(adminService);
        Response response = apiService.threatProtectionPoliciesPost(dto, getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        //Error path
        Mockito.doThrow(APIManagementException.class).when(adminService).addThreatProtectionPolicy(Mockito.any());
        response = apiService.threatProtectionPoliciesPost(createThreatProtectionPolicyDTO(), getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testThreatProtectionPoliciesThreatProtectionPolicyIdDelete() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        ThreatProtectionPoliciesApiServiceImpl apiService = new ThreatProtectionPoliciesApiServiceImpl(adminService);
        Response response = apiService.threatProtectionPoliciesThreatProtectionPolicyIdDelete("ID",
                getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        //Error path
        Mockito.doThrow(APIManagementException.class).when(adminService).deleteThreatProtectionPolicy(
                Mockito.anyString());
        response = apiService.threatProtectionPoliciesThreatProtectionPolicyIdDelete("ID",
                getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    @Test
    public void testThreatProtectionPoliciesThreatProtectionPolicyIdGet() throws Exception {
        APIMgtAdminService adminService = Mockito.mock(APIMgtAdminService.class);

        ThreatProtectionPolicy policy = createThreatProtectionPolicy();
        Mockito.when(adminService.getThreatProtectionPolicy(policy.getUuid())).thenReturn(policy);

        ThreatProtectionPoliciesApiServiceImpl apiService = new ThreatProtectionPoliciesApiServiceImpl(adminService);
        Response response = apiService.threatProtectionPoliciesThreatProtectionPolicyIdGet(policy.getUuid(),
                getRequest());
        Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        ThreatProtectionPolicyDTO returnedPolicy = (ThreatProtectionPolicyDTO) response.getEntity();
        Assert.assertEquals(policy.getUuid(), returnedPolicy.getUuid());
        Assert.assertEquals(policy.getName(), returnedPolicy.getName());
        Assert.assertEquals(policy.getType(), returnedPolicy.getType());

        //Error path
        Mockito.when(adminService.getThreatProtectionPolicy(policy.getUuid())).thenThrow(APIManagementException.class);
        response = apiService.threatProtectionPoliciesThreatProtectionPolicyIdGet(policy.getUuid(), getRequest());
        Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    }

    private ThreatProtectionPolicyDTO createThreatProtectionPolicyDTO() {
        ThreatProtectionPolicyDTO policyDTO = new ThreatProtectionPolicyDTO();
        policyDTO.setName("TEST-POLICY");
        policyDTO.setType("XML");
        policyDTO.setUuid(UUID.randomUUID().toString());
        return policyDTO;
    }

    private ThreatProtectionPolicy createThreatProtectionPolicy() {
        ThreatProtectionPolicy policy = new ThreatProtectionPolicy();
        policy.setName("TEST-POLICY");
        policy.setType("XML");
        policy.setUuid(UUID.randomUUID().toString());
        return policy;
    }

    private Request getRequest() throws Exception {
        return Mockito.mock(Request.class);
    }
}
