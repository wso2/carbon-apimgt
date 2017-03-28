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

package org.wso2.carbon.apimgt.core.impl;

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.SubscriptionValidationData;
import org.wso2.carbon.apimgt.core.models.policy.Policy;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class APIMgtAdminServiceImplTestCase {

    private static final Integer LIMIT = 2;
    private static final String API_VERSION = "1.0.0";
    private static final String API_CONTEXT = "/testContext";
    private static final String POLICY_LEVEL = "policyLevel";
    private static final String POLICY_NAME = "policyName";

    @Test(description = "Get api subscriptions")
    public void testGetAPISubscriptions() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIMgtAdminServiceImpl adminService = new APIMgtAdminServiceImpl(apiSubscriptionDAO, null, null);
        when(apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(LIMIT))
                .thenReturn(new ArrayList<SubscriptionValidationData>());
        adminService.getAPISubscriptions(LIMIT);
        verify(apiSubscriptionDAO, times(1)).getAPISubscriptionsOfAPIForValidation(LIMIT);
    }

    @Test(description = "Get api subscriptions of API")
    public void testGetAPISubscriptionsOfApi() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIMgtAdminServiceImpl adminService = new APIMgtAdminServiceImpl(apiSubscriptionDAO, null, null);
        when(apiSubscriptionDAO.getAPISubscriptionsOfAPIForValidation(API_CONTEXT, API_VERSION))
                .thenReturn(new ArrayList<SubscriptionValidationData>());
        adminService.getAPISubscriptionsOfApi(API_CONTEXT, API_VERSION);
        verify(apiSubscriptionDAO, times(1)).getAPISubscriptionsOfAPIForValidation(API_CONTEXT, API_VERSION);
    }

    @Test(description = "Get policy")
    public void testGetPolicy() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = new APIMgtAdminServiceImpl(null, policyDAO, null);
        Policy policy = mock(Policy.class);
        when(policyDAO.getPolicy(POLICY_LEVEL, POLICY_NAME)).thenReturn(policy);
        adminService.getPolicy(POLICY_LEVEL, POLICY_NAME);
        verify(policyDAO, times(1)).getPolicy(POLICY_LEVEL, POLICY_NAME);
    }

    @Test(description = "Get all policies by level")
    public void testGetAllPoliciesByLevel() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = new APIMgtAdminServiceImpl(null, policyDAO, null);
        Policy policy = mock(Policy.class);
        List<Policy> policyList = new ArrayList<>();
        policyList.add(policy);
        when(policyDAO.getPolicies(POLICY_LEVEL)).thenReturn(policyList);
        adminService.getAllPoliciesByLevel(POLICY_LEVEL);
        verify(policyDAO, times(1)).getPolicies(POLICY_LEVEL);
    }

    @Test(description = "Add policy")
    public void testAddPolicy() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIMgtAdminServiceImpl adminService = new APIMgtAdminServiceImpl(null, policyDAO, null);
        Policy policy = mock(Policy.class);
        adminService.addPolicy(POLICY_LEVEL, policy);
        verify(policyDAO, times(1)).addPolicy(POLICY_LEVEL, policy);
    }

    @Test(description = "Get API Info")
    public void testGetAPIInfo() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIMgtAdminServiceImpl adminService = new APIMgtAdminServiceImpl(null, null, apiDAO);
        List<API> apiList = SampleTestObjectCreator.createMockAPIList();
        when(apiDAO.getAPIs()).thenReturn(apiList);
        adminService.getAPIInfo();
        verify(apiDAO, times(1)).getAPIs();
    }

}
