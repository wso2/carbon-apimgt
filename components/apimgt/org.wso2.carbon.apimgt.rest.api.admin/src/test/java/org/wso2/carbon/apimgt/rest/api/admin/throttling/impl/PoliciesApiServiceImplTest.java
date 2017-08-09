/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 * /
 */

package org.wso2.carbon.apimgt.rest.api.admin.throttling.impl;

import org.junit.Assert;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.admin.dto.AdvancedThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ApplicationThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomRuleDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.SubscriptionThrottlePolicyDTO;
import org.wso2.carbon.apimgt.rest.api.admin.impl.PoliciesApiServiceImpl;
import org.wso2.carbon.apimgt.rest.api.admin.mappings.AdvancedThrottlePolicyMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.exception.APIMgtSecurityException;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RestApiUtil.class)
public class PoliciesApiServiceImplTest {
    private final static Logger logger = LoggerFactory.getLogger(PoliciesApiServiceImplTest.class);

    @Test
    public void policiesThrottlingAdvancedGetTest() throws APIManagementException, NotFoundException {
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        APIPolicy policy1 = new APIPolicy("samplePolicy1");
        APIPolicy policy2 = new APIPolicy("samplePolicy2");
        List<APIPolicy> policies = new ArrayList<>();
        policies.add(policy1);
        policies.add(policy2);
        Mockito.doReturn(policies).doThrow(new IllegalArgumentException()).when(adminService).getApiPolicies();

        Response response = policiesApiService.policiesThrottlingAdvancedGet(null, null,
                                                                             null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingAdvancedPolicyIdDeleteTest()  throws APIManagementException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        Mockito.doNothing().doThrow(new IllegalArgumentException()).when(adminService)
                .deletePolicyByUuid(uuid, APIMgtAdminService.PolicyLevel.api);
        Response response = policiesApiService.policiesThrottlingAdvancedPolicyIdDelete(uuid, null,
                                                                                        null,
                                                                    getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingAdvancedPolicyIdGetTest() throws APIManagementException, NotFoundException  {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        APIPolicy policy1 = new APIPolicy(uuid, "samplePolicy1");
        Mockito.doReturn(policy1).doThrow(new IllegalArgumentException()).when(adminService).getApiPolicyByUuid(uuid);

        Response response = policiesApiService.policiesThrottlingAdvancedPolicyIdGet(null, null,
                                                                             null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingAdvancedPolicyIdPutTest() throws APIManagementException, NotFoundException   {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        AdvancedThrottlePolicyDTO dto = new AdvancedThrottlePolicyDTO();

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        APIPolicy policy1 = new APIPolicy(uuid, "samplePolicy1");
        Mockito.doReturn(policy1).doThrow(new IllegalArgumentException()).when(adminService).getApiPolicyByUuid(uuid);

        Response response = policiesApiService.policiesThrottlingAdvancedPolicyIdPut(uuid, dto, null,
                                                                                     null, null,
                                                                                     getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingAdvancedPostTest()    throws APIManagementException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        AdvancedThrottlePolicyDTO dto = new AdvancedThrottlePolicyDTO();
        String uuid = UUID.randomUUID().toString();
        dto.setPolicyId(uuid);

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        APIPolicy policy1 = AdvancedThrottlePolicyMappingUtil.fromAdvancedPolicyDTOToPolicy(dto);
        Mockito.doReturn(uuid).doThrow(new IllegalArgumentException()).when(adminService).addApiPolicy(policy1);

        Response response = policiesApiService.policiesThrottlingAdvancedPost(dto, null, getRequest());
        Assert.assertEquals(response.getStatus(), 201);
    }

    @Test
    public void policiesThrottlingApplicationGetTest()   throws APIManagementException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();

        APIMgtAdminServiceImpl adminService = Mockito.mock(APIMgtAdminServiceImpl.class);
        PowerMockito.mockStatic(RestApiUtil.class);
        PowerMockito.when(RestApiUtil.getAPIMgtAdminService()).thenReturn(adminService);
        ApplicationPolicy policy1 = new ApplicationPolicy("sampleAppPolicy1");
        ApplicationPolicy policy2 = new ApplicationPolicy("sampleAppPolicy2");
        List<ApplicationPolicy> policies = new ArrayList<>();
        policies.add(policy1);
        policies.add(policy2);
        Mockito.doReturn(policies).doThrow(new IllegalArgumentException()).when(adminService).getApplicationPolicies();

        Response response = policiesApiService.policiesThrottlingApplicationGet(null, null,
                                                                                null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingApplicationPolicyIdDeleteTest() throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        Response response = policiesApiService.policiesThrottlingApplicationPolicyIdDelete(uuid, null,
                                                                                        null,
                                                                                        getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingApplicationPolicyIdGetTest()  throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        Response response = policiesApiService.policiesThrottlingApplicationPolicyIdGet(uuid, null,
                                                                                        null,
                                                                                        getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingApplicationPolicyIdPutTest()   throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        ApplicationThrottlePolicyDTO dto = new ApplicationThrottlePolicyDTO();
        Response response = policiesApiService.policiesThrottlingApplicationPolicyIdPut(uuid, dto, null,
                                                                                        null,
                                                                    null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingCustomGetTest() throws APIMgtSecurityException, NotFoundException  {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        Response response = policiesApiService.policiesThrottlingCustomGet(null, null,
                                                                           null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingCustomPostTest()  throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        CustomRuleDTO dto = new CustomRuleDTO();
        Response response = policiesApiService.policiesThrottlingCustomPost(dto, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingCustomRuleIdDeleteTest()  throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        Response response = policiesApiService.policiesThrottlingCustomRuleIdDelete(uuid, null,
                                                                                    null,
                                                                                    getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingCustomRuleIdGetTest() throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        Response response = policiesApiService.policiesThrottlingCustomRuleIdGet(uuid, null,
                                                                                 null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingCustomRuleIdPutTest() throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        CustomRuleDTO dto = new CustomRuleDTO();
        Response response = policiesApiService.policiesThrottlingCustomRuleIdPut(uuid, dto,null,
                                                                                 null,
                                                             null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingApplicationPostTest() throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        ApplicationThrottlePolicyDTO dto = new ApplicationThrottlePolicyDTO();
        Response response = policiesApiService.policiesThrottlingApplicationPost(dto, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingSubscriptionGetTest() throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        Response response = policiesApiService.policiesThrottlingSubscriptionGet(null, null,
                                                                                 null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingSubscriptionPolicyIdDeleteTest()  throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        Response response = policiesApiService.policiesThrottlingSubscriptionPolicyIdDelete(uuid, null,
                                                                                            null,
                                                                        getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingSubscriptionPolicyIdGetTest() throws APIMgtSecurityException, NotFoundException  {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        Response response = policiesApiService.policiesThrottlingSubscriptionPolicyIdGet(uuid, null,
                                                                                         null,
                                                                                         getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingSubscriptionPolicyIdPutTest() throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        String uuid = UUID.randomUUID().toString();
        SubscriptionThrottlePolicyDTO dto = new SubscriptionThrottlePolicyDTO();
        Response response = policiesApiService.policiesThrottlingSubscriptionPolicyIdPut(uuid, dto, null,
                                                                                         null,
                                                                     null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void policiesThrottlingSubscriptionPostTest()    throws APIMgtSecurityException, NotFoundException {
        printTestMethodName();
        PoliciesApiServiceImpl policiesApiService = new PoliciesApiServiceImpl();
        SubscriptionThrottlePolicyDTO dto = new SubscriptionThrottlePolicyDTO();
        Response response = policiesApiService.policiesThrottlingSubscriptionPost(dto, null, getRequest());
        Assert.assertEquals(response.getStatus(), 200);
    }

    private Request getRequest() throws APIMgtSecurityException {
        CarbonMessage carbonMessage = Mockito.mock(CarbonMessage.class);
        Request request = new Request(carbonMessage);

        try {
            PowerMockito.whenNew(Request.class).withArguments(carbonMessage).thenReturn(request);
        } catch (Exception e) {
            throw new APIMgtSecurityException("Error while mocking Request Object ", e);
        }
        return request;
    }

    private static void printTestMethodName() {
        logger.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                    " ------------------");
    }
}
