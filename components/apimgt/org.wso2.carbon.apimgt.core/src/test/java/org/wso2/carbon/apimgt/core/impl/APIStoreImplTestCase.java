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
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.doThrow;

/**
 * Test class for APIStore
 */
public class APIStoreImplTestCase {

    private static final String USER_NAME = "username";
    private static final String APP_NAME = "appname";
    private static final String USER_ID = "userid";
    private static final String API_ID = "apiid";
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
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null);
        List<API> apimResultsFromDAO = new ArrayList<>();
        when(apiDAO.searchAPIs(new ArrayList<>(), "admin", "pizza", 1, 2)).thenReturn(apimResultsFromDAO);
        List<API> apis = apiStore.searchAPIs("pizza", 1, 2);
        Assert.assertNotNull(apis);
        verify(apiDAO, atLeastOnce()).searchAPIs(new ArrayList<>(), "admin", "pizza", 1, 2);
    }

    @Test(description = "Search APIs with an empty query")
    public void searchAPIsEmpty() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null);
        List<API> apimResultsFromDAO = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        statuses.add(APIStatus.PUBLISHED.getStatus());
        statuses.add(APIStatus.PROTOTYPED.getStatus());
        when(apiDAO.getAPIsByStatus(statuses)).thenReturn(apimResultsFromDAO);
        List<API> apis = apiStore.searchAPIs("", 1, 2);
        Assert.assertNotNull(apis);
        verify(apiDAO, atLeastOnce()).getAPIsByStatus(statuses);
    }

    @Test(description = "Search API", expectedExceptions = APIManagementException.class)

    public void searchAPIsWithException() throws Exception {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null);
        PowerMockito.mockStatic(APIUtils.class); // TODO
        when(apiDAO.searchAPIs(new ArrayList<>(), "admin", "select *", 1, 2)).thenThrow(APIMgtDAOException.class);
        //doThrow(new Exception()).when(APIUtils).logAndThrowException(null, null, null)).
        apiStore.searchAPIs("select *", 1, 2);
    }

    @Test(description = "Retrieve an API by status")
    public void getAPIsByStatus() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null);
        List<API> expectedAPIs = new ArrayList<API>();
        when(apiDAO.getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED))).thenReturn(expectedAPIs);
        List<API> actualAPIs = apiStore.getAllAPIsByStatus(1, 2, new String[] { STATUS_CREATED, STATUS_PUBLISHED });
        Assert.assertNotNull(actualAPIs);
        verify(apiDAO, times(1)).getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED));
    }

    @Test(description = "Retrieve an application by name")
    public void testGetApplicationByName() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null ,null, null, null, null);
        Application applicationFromDAO = new Application(APP_NAME, null);
        when(applicationDAO.getApplicationByName(APP_NAME, USER_ID)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplicationByName(APP_NAME, USER_ID, GROUP_ID);
        Assert.assertNotNull(application);
        verify(applicationDAO, times(1)).getApplicationByName(APP_NAME, USER_ID);
    }

    @Test(description = "Retrieve an application by uuid")
    public void testGetApplicationByUUID() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null,null);
        Application applicationFromDAO = new Application(APP_NAME, USER_NAME);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplicationByUuid(UUID);
        Assert.assertNotNull(application);
        verify(applicationDAO, times(1)).getApplication(UUID);
    }

    @Test(description = "Add an application")
    public void testAddApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        Policy policy = mock(Policy.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setPermissionString(
                "[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\",\"SUBSCRIPTION\"]}]");
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        ApplicationCreationResponse response = apiStore.addApplication(application);
        Assert.assertNotNull(response.getApplicationUUID());
        verify(applicationDAO, times(1)).addApplication(application);
    }

    @Test(description = "Add an application with null permission String")
    public void testAddApplicationPermissionStringNull() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        Policy policy = mock(Policy.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null,workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setPermissionString(null);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        ApplicationCreationResponse applicationResponse = apiStore.addApplication(application);
        String applicationUuid = applicationResponse.getApplicationUUID();        
        Assert.assertNotNull(applicationUuid);
        verify(applicationDAO, times(1)).addApplication(application);
    }

    @Test(description = "Add an application with empty permission String")
    public void testAddApplicationPermissionStringEmpty() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        Policy policy = mock(Policy.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null,workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setPermissionString("");
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        ApplicationCreationResponse applicationResponse = apiStore.addApplication(application);
        String applicationUuid = applicationResponse.getApplicationUUID();     
        Assert.assertNotNull(applicationUuid);
        verify(applicationDAO, times(1)).addApplication(application);
    }

    @Test(description = "Add an application with invalid permission String")
    public void testAddApplicationPermissionStringInvalid() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        Policy policy = mock(Policy.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setPermissionString("[{\"groupId\": \"testGroup\",\"permission\":[\"TESTREAD\",\"TESTUPDATE\"]}]");
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        ApplicationCreationResponse applicationResponse = apiStore.addApplication(application);
        String applicationUuid = applicationResponse.getApplicationUUID();     
        Assert.assertNotNull(applicationUuid);
        verify(applicationDAO, times(1)).addApplication(application);
    }

    @Test(description = "Add subscription to an application")
    public void testAddSubscription() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        ApiDAO apiDAO = mock(ApiDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, applicationDAO, apiSubscriptionDAO, null, null, null,
                workflowDAO);
        SubscriptionResponse subscriptionResponse = apiStore.addApiSubscription(API_ID, UUID, TIER);
        String subscriptionId = subscriptionResponse.getSubscriptionUUID();
        Assert.assertNotNull(subscriptionId);
        verify(apiSubscriptionDAO, times(1))
                .addAPISubscription(subscriptionId, API_ID, UUID, TIER, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Delete subscription")
    public void testDeleteSubscription() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                null);
        apiStore.deleteAPISubscription(UUID);
        verify(apiSubscriptionDAO, times(1)).deleteAPISubscription(UUID);
    }

    @Test(description = "Get API subscriptions by application")
    public void testGetAPISubscriptionsByApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setId(UUID);
        apiStore.getAPISubscriptionsByApplication(application);
        verify(apiSubscriptionDAO, times(1)).getAPISubscriptionsByApplication(UUID);
    }

    @Test(description = "Add an application with null tier", expectedExceptions = APIManagementException.class)
    public void testAddApplicationNullTier() throws Exception {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(null);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        apiStore.addApplication(application);
    }

    @Test(description = "Add an application with null policy", expectedExceptions = APIManagementException.class)
    public void testAddApplicationNullPolicy() throws Exception {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(null);
        apiStore.addApplication(application);
    }

    @Test(description = "Add application with duplicate name",
            expectedExceptions = APIMgtResourceAlreadyExistsException.class)
    public void testAddApplicationWithDuplicateName() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(true);
        apiStore.addApplication(application);
    }

    @Test(description = "Delete application")
    public void testDeleteApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setId(UUID);
        apiStore.deleteApplication(UUID);
        verify(applicationDAO, times(1)).deleteApplication(UUID);
    }

    @Test(description = "Update an application")
    public void testUpdateApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        apiStore.updateApplication(UUID, application);
        verify(applicationDAO, times(1)).updateApplication(UUID, application);
    }

    @Test(description = "Retrieve applications")
    public void testGetApplications() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null);
        apiStore.getApplications(USER_ID, GROUP_ID);
        verify(applicationDAO, times(1)).getApplications(USER_ID);
    }

    @Test(description = "Retrieve all tags")
    public void testGetAllTags() throws APIManagementException {
        TagDAO tagDAO = mock(TagDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null,null,null,null,tagDAO, null, null);
        apiStore.getAllTags();
        verify(tagDAO, times(1)).getTags();
    }

    @Test(description = "Get all policies of a specific policy level")
    public void testGetPolicies() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME,null,null,null, policyDAO, null, null, null);
        apiStore.getPolicies(APPLICATION_POLICY_LEVEL);
        verify(policyDAO, times(1)).getPolicies(APPLICATION_POLICY_LEVEL);
    }

    @Test(description = "Get policy given policy name and policy level")
    public void testGetPolicy() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, null, null, policyDAO, null, null, null);
        apiStore.getPolicy(APPLICATION_POLICY_LEVEL, POLICY_NAME);
        verify(policyDAO, times(1)).getPolicy(APPLICATION_POLICY_LEVEL, POLICY_NAME);
    }

    @Test(description = "Retrieve labels")
    public void testGetLabelInfo() throws APIManagementException {
        LabelDAO labelDAO = mock(LabelDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, null, null, null, null, labelDAO, null);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        apiStore.getLabelInfo(labels);
        verify(labelDAO, times(1)).getLabelsByName(labels);
    }

    /**
     * Tests to catch exceptions in methods
     */

    @Test(description = "Exception when deleting subscription", expectedExceptions = APIMgtDAOException.class)
    public void testDeleteSubscriptionException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                null);
        doThrow(new APIMgtDAOException("Error occurred while deleting api subscription " + UUID))
                .when(apiSubscriptionDAO).deleteAPISubscription(UUID);
        apiStore.deleteAPISubscription(UUID);
    }

    @Test(description = "Exception when retrieving all tags", expectedExceptions = APIMgtDAOException.class)
    public void testGetAllTagsException() throws APIManagementException {
        TagDAO tagDAO = mock(TagDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, null, null, null, tagDAO, null, null);
        when(tagDAO.getTags()).thenThrow(new APIMgtDAOException("Error occurred while retrieving tags"));
        apiStore.getAllTags();
    }

    @Test(description = "Exception when getting all policies of a specific policy level",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetPoliciesException() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, null, null, policyDAO, null, null, null);
        when(policyDAO.getPolicies(APPLICATION_POLICY_LEVEL)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving policies for policy level - " + APPLICATION_POLICY_LEVEL));
        apiStore.getPolicies(APPLICATION_POLICY_LEVEL);
    }

    @Test(description = "Exception when getting policy given policy name and policy level",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetPolicyException() throws APIManagementException {
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, null, null, policyDAO, null, null, null);
        when(policyDAO.getPolicy(APPLICATION_POLICY_LEVEL, POLICY_NAME))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving policy - " + POLICY_NAME));
        apiStore.getPolicy(APPLICATION_POLICY_LEVEL, POLICY_NAME);
    }

    @Test(description = "Exception when deleting an application", expectedExceptions = APIMgtDAOException.class)
    public void testDeleteApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setId(UUID);
        doThrow(new APIMgtDAOException("Error occurred while deleting the application - " + UUID)).when(applicationDAO)
                .deleteApplication(UUID);
        apiStore.deleteApplication(UUID);
        verify(applicationDAO, times(1)).deleteApplication(UUID);
    }

    @Test(description = "Exception when retrieving an application by uuid",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetApplicationByUUIDException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null);
        when(applicationDAO.getApplication(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving application - " + UUID));
        apiStore.getApplicationByUuid(UUID);
    }

    @Test(description = "Exception when getting API subscriptions by application",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetAPISubscriptionsByApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setId(UUID);
        when(apiSubscriptionDAO.getAPISubscriptionsByApplication(application.getId())).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving subscriptions for application - " + application.getName()));
        apiStore.getAPISubscriptionsByApplication(application);
    }

    @Test(description = "Exception when retrieving APIs by status", expectedExceptions = APIMgtDAOException.class)
    public void getAPIsByStatusException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null);
        String[] statuses = { STATUS_CREATED, STATUS_PUBLISHED };
        when(apiDAO.getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED))).thenThrow(new APIMgtDAOException(
                "Error occurred while fetching APIs for the given statuses - " + Arrays.toString(statuses)));
        apiStore.getAllAPIsByStatus(1, 2, statuses);
    }

    @Test(description = "Exception when retrieving an application by name",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetApplicationByNameException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null);
        when(applicationDAO.getApplicationByName(APP_NAME, USER_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while fetching application for the given applicationName - " + APP_NAME
                        + " with groupId - " + GROUP_ID));
        apiStore.getApplicationByName(APP_NAME, USER_ID, GROUP_ID);
    }

    @Test(description = "Exception when retrieving applications", expectedExceptions = APIMgtDAOException.class)
    public void testGetApplicationsException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null);
        when(applicationDAO.getApplications(USER_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while fetching applications for the given subscriber - " + USER_ID + " with groupId - "
                        + GROUP_ID));
        apiStore.getApplications(USER_ID, GROUP_ID);
    }

    @Test(description = "Exception when updating an application", expectedExceptions = APIMgtDAOException.class)
    public void testUpdateApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        doThrow(new APIMgtDAOException("Error occurred while updating the application - " + UUID)).when(applicationDAO)
                .updateApplication(UUID, application);
        apiStore.updateApplication(UUID, application);
    }

    @Test(description = "Exception when adding an application", expectedExceptions = APIMgtDAOException.class)
    public void testAddApplicationCreationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        Policy policy = mock(Policy.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        doThrow(new APIMgtDAOException("Error occurred while creating the application - " + application.getName()))
                .when(applicationDAO).addApplication(application);
        apiStore.addApplication(application);
    }

    @Test(description = "Parse exception when adding an application", expectedExceptions = APIManagementException.class)
    public void testAddApplicationParsingException() throws Exception {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        PolicyDAO policyDAO = mock(PolicyDAO.class);
        Policy policy = mock(Policy.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, policyDAO, null, null, null);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setTier(TIER);
        application.setPermissionString("data");
        when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        when(policyDAO.getPolicy(APIMgtConstants.ThrottlePolicyConstants.APPLICATION_LEVEL, TIER)).thenReturn(policy);
        apiStore.addApplication(application);
    }

    @Test(description = "Exception when retrieving labels", expectedExceptions = APIMgtDAOException.class)
    public void testGetLabelInfoException() throws APIManagementException {
        LabelDAO labelDAO = mock(LabelDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, null, null, null, null, null, labelDAO, null);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        when(labelDAO.getLabelsByName(labels))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving label information"));
        apiStore.getLabelInfo(labels);
    }

}
