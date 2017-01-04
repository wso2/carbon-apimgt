/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test class for APIStore
 *
 */
public class APIStoreImplTestCase {

    private static final String USER_NAME = "username";
    private static final String APP_NAME = "appname";
    private static final String USER_ID = "userid";
    private static final String GROUP_ID = "groupdid";
    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";
    private static final String TIER = "gold";
    private static final String APPLICATION_POLICY_LEVEL = "application";
    private static final String POLICY_NAME = "gold";

    @Test(description = "Search APIs with a search query")
    public void searchAPIs() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null);
        List<API> apimResultsFromDAO = new ArrayList<>();
        when(apiDAO.searchAPIs("pizza")).thenReturn(apimResultsFromDAO);
        List<API> apis = apiStore.searchAPIs("pizza", 1, 2);
        Assert.assertNotNull(apis);
        verify(apiDAO, atLeastOnce()).searchAPIs("pizza");
    }

    @Test(description = "Search APIs with an empty query")
    public void searchAPIsEmpty() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null);
        List<API> apimResultsFromDAO = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        statuses.add("PUBLISHED");
        statuses.add("PROTOTYPED");
        when(apiDAO.getAPIsByStatus(statuses)).thenReturn(apimResultsFromDAO);
        List<API> apis = apiStore.searchAPIs("", 1, 2);
        Assert.assertNotNull(apis);
        verify(apiDAO, atLeastOnce()).getAPIsByStatus(statuses);
    }

    @Test(description = "Search API", expectedExceptions = APIManagementException.class)

    public void searchAPIsWithException() throws Exception {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null);
        PowerMockito.mockStatic(APIUtils.class); // TODO
        when(apiDAO.searchAPIs("select *")).thenThrow(APIMgtDAOException.class);
        //doThrow(new Exception()).when(APIUtils).logAndThrowException(null, null, null)).
        apiStore.searchAPIs("select *", 1, 2);
    }

    @Test(description = "Retrieve an API by status")
    public void getAPIsByStatus() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null);
        List<API> expectedAPIs = new ArrayList<API>();
        when(apiDAO.getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED))).thenReturn(expectedAPIs);
        List<API> actualAPIs = apiStore.getAllAPIsByStatus(1, 2, new String[] { STATUS_CREATED, STATUS_PUBLISHED });
        Assert.assertNotNull(actualAPIs);
        verify(apiDAO, times(1)).getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED));
    }

    @Test(description = "Retrieve an application by name")
    public void testGetApplicationByName() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null ,null, null);
        Application applicationFromDAO = new Application(APP_NAME, null);
        when(applicationDAO.getApplicationByName(USER_ID, APP_NAME)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplicationByName(USER_ID, APP_NAME, GROUP_ID);
        Assert.assertNotNull(application);
        verify(applicationDAO, times(1)).getApplicationByName(USER_ID, APP_NAME);
    }

    @Test(description = "Add an application")
    public void testAddApplication() throws  APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        Policy policy = mock(Policy.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        String applicationUuid = apiStore.addApplication(application);
        Assert.assertNotNull(applicationUuid);
        verify(applicationDAO, times(1)).addApplication(application);
    }

    @Test(description = "Add application with duplicate name",
            expectedExceptions = APIMgtResourceAlreadyExistsException.class)
    public void testAddApplicationWithDuplicateName() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(true);
        apiStore.addApplication(application);
    }

    @Test(description = "Delete application")
    public void testDeleteApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setId(UUID);
        apiStore.deleteApplication(UUID);
        verify(applicationDAO, times(1)).deleteApplication(UUID);
    }

    @Test(description = "Update an application")
    public void testUpdateApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        apiStore.updateApplication(UUID, application);
        verify(applicationDAO, times(1)).updateApplication(UUID, application);
    }

    @Test(description = "Retrieve all tags")
    public void testGetAllTags() throws APIManagementException {
        TagDAO tagDAO = mock(TagDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null,null,null,null,tagDAO);
        apiStore.getAllTags();
        verify(tagDAO, times(1)).getTags();
    }

    @Test(description = "Get all policies of a specific policy level")
    public void testGetPolicies() throws APIManagementException{
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME,null,null,null, policyDAO, null);
        apiStore.getPolicies(APPLICATION_POLICY_LEVEL);
        verify(policyDAO, times(1)).getPolicies(APPLICATION_POLICY_LEVEL);
    }

    @Test(description = "Get policy given policy name and policy level")
    public void testGetPolicy() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME,null,null,null, policyDAO, null);
        apiStore.getPolicy(APPLICATION_POLICY_LEVEL,POLICY_NAME);
        verify(policyDAO, times(1)).getPolicy(APPLICATION_POLICY_LEVEL,POLICY_NAME);
    }

}
