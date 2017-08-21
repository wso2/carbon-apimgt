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

import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.api.GatewaySourceGenerator;
import org.wso2.carbon.apimgt.core.api.IdentityProvider;
import org.wso2.carbon.apimgt.core.api.KeyManager;
import org.wso2.carbon.apimgt.core.api.WorkflowExecutor;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APICommentException;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.APIRatingException;
import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.exception.LabelException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.API.APIBuilder;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Rating;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.SubscriptionResponse;
import org.wso2.carbon.apimgt.core.models.User;
import org.wso2.carbon.apimgt.core.models.WSDLArchiveInfo;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.models.WorkflowStatus;
import org.wso2.carbon.apimgt.core.models.policy.ApplicationPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ApplicationStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.SubscriptionStatus;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.WorkflowConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationResponse;
import org.wso2.carbon.apimgt.core.workflow.ApplicationCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.ApplicationUpdateWorkflow;
import org.wso2.carbon.apimgt.core.workflow.DefaultWorkflowExecutor;
import org.wso2.carbon.apimgt.core.workflow.GeneralWorkflowResponse;
import org.wso2.carbon.apimgt.core.workflow.SubscriptionCreationWorkflow;
import org.wso2.carbon.apimgt.core.workflow.Workflow;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExtensionsConfigBuilder;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Test class for APIStore
 */
public class APIStoreImplTestCase {

    private static final String USER_NAME = "admin";
    private static final String APP_NAME = "appname";
    private static final String USER_ID = "userid";
    private static final String API_ID = "apiid";
    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";
    private static final String TIER = "gold";
    private static final String APPLICATION_POLICY_LEVEL = "application";
    private static final String POLICY_NAME = "gold";

    @BeforeTest
    public void setup() throws Exception {
        WorkflowExtensionsConfigBuilder.build(new ConfigProvider() {

            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
                T workflowConfig = (T) new WorkflowConfig();
                return workflowConfig;
            }

            @Override
            public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
                // TODO Auto-generated method stub
                return null;
            }
        });

        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);
    }

    @Test(description = "Test getting composite API by UUID")
    public void testGetCompositeAPIbyId() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        CompositeAPI api = SampleTestObjectCreator.createUniqueCompositeAPI().build();
        String apiId = api.getId();
        APIStore apiStore = getApiStoreImpl(apiDAO);
        Mockito.when(apiDAO.getCompositeAPI(apiId)).thenReturn(api);
        CompositeAPI apiReturned = apiStore.getCompositeAPIbyId(apiId);
        Mockito.verify(apiDAO, Mockito.times(1)).getCompositeAPI(apiId);
        Assert.assertEquals(apiReturned, api);

        //Error path
        Mockito.when(apiDAO.getCompositeAPI(apiId)).thenThrow(APIMgtDAOException.class);
        try {
            apiStore.getCompositeAPIbyId(apiId);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while retrieving API with id " + apiId);
        }
    }

    @Test(description = "Search APIs with a search query")
    public void searchAPIs() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        List<API> apimResultsFromDAO = new ArrayList<>();
        Mockito.when(apiDAO.searchAPIs(new HashSet<>(), "admin", "pizza",
                1, 2)).thenReturn(apimResultsFromDAO);
        List<API> apis = apiStore.searchAPIs("pizza", 1, 2);
        Assert.assertNotNull(apis);
        Mockito.verify(apiDAO, Mockito.atLeastOnce()).searchAPIs(APIUtils.getAllRolesOfUser("admin"),
                "admin", "pizza", 1, 2);
    }

    @Test(description = "Search APIs with an empty query")
    public void searchAPIsEmpty() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        List<API> apimResultsFromDAO = new ArrayList<>();
        List<String> statuses = new ArrayList<>();
        statuses.add(APIStatus.PUBLISHED.getStatus());
        statuses.add(APIStatus.PROTOTYPED.getStatus());
        Mockito.when(apiDAO.getAPIsByStatus(statuses)).thenReturn(apimResultsFromDAO);
        List<API> apis = apiStore.searchAPIs("", 1, 2);
        Assert.assertNotNull(apis);
        Mockito.verify(apiDAO, Mockito.atLeastOnce()).getAPIsByStatus(APIUtils.getAllRolesOfUser("admin"),
                statuses);
    }

    @Test(description = "Search API", expectedExceptions = APIManagementException.class)
    public void searchAPIsWithException() throws Exception {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        PowerMockito.mockStatic(APIUtils.class); // TODO
        Mockito.when(apiDAO.searchAPIs(APIUtils.getAllRolesOfUser("admin"), "admin",
                "select *", 1, 2)).thenThrow(APIMgtDAOException
                .class);
        //doThrow(new Exception()).when(APIUtils).logAndThrowException(null, null, null)).
        apiStore.searchAPIs("select *", 1, 2);
    }

    @Test(description = "Retrieve an API by status")
    public void getAPIsByStatus() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        List<API> expectedAPIs = new ArrayList<API>();
        Mockito.when(apiDAO.getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED))).
                thenReturn(expectedAPIs);
        List<API> actualAPIs = apiStore.getAllAPIsByStatus(1, 2, new String[] {STATUS_CREATED, STATUS_PUBLISHED});
        Assert.assertNotNull(actualAPIs);
        Mockito.verify(apiDAO, Mockito.times(1)).
                getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED));
    }

    @Test(description = "Retrieve a WSDL of an API")
    public void testGetAPIWSDL() throws APIManagementException, IOException {
        final String labelName = "SampleLabel";

        Label label = SampleTestObjectCreator.createLabel(labelName).build();
        Set<String> labels = new HashSet<>();
        labels.add(label.getName());
        API api = SampleTestObjectCreator.createDefaultAPI().labels(labels).build();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO, labelDAO);
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Mockito.when(labelDAO.getLabelByName(labelName)).thenReturn(label);
        Mockito.when(apiDAO.getWSDL(api.getId()))
                .thenReturn(new String(SampleTestObjectCreator.createDefaultWSDL11Content()));
        String expectedEndpoint = SampleTestObjectCreator.ACCESS_URL + labelName
                + (api.getContext().startsWith("/") ? api.getContext() : "/" + api.getContext());
        String updatedWSDL = apiStore.getAPIWSDL(api.getId(), label.getName());
        Assert.assertTrue(updatedWSDL.contains(expectedEndpoint));
    }

    @Test(description = "Retrieve a WSDL archive of an API")
    public void testGetAPIWSDLArchive() throws APIManagementException, IOException {
        final String labelName = "SampleLabel";

        Label label = SampleTestObjectCreator.createLabel(labelName).build();
        Set<String> labels = new HashSet<>();
        labels.add(label.getName());
        API api = SampleTestObjectCreator.createDefaultAPI().labels(labels).build();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO, labelDAO);
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Mockito.when(labelDAO.getLabelByName(labelName)).thenReturn(label);
        Mockito.when(apiDAO.getWSDLArchive(api.getId()))
                .thenReturn(SampleTestObjectCreator.createDefaultWSDL11ArchiveInputStream());
        String expectedEndpoint = SampleTestObjectCreator.ACCESS_URL + labelName
                + (api.getContext().startsWith("/") ? api.getContext() : "/" + api.getContext());
        WSDLArchiveInfo archiveInfo = apiStore.getAPIWSDLArchive(api.getId(), label.getName());
        Assert.assertNotNull(archiveInfo);
        Assert.assertNotNull(archiveInfo.getWsdlInfo());
        Assert.assertNotNull(archiveInfo.getWsdlInfo().getEndpoints());
        Map<String, String> endpoints = archiveInfo.getWsdlInfo().getEndpoints();
        Assert.assertTrue(endpoints.containsValue(expectedEndpoint));
        Assert.assertFalse(endpoints.containsValue(SampleTestObjectCreator.ORIGINAL_ENDPOINT_STOCK_QUOTE));
        Assert.assertFalse(endpoints.containsValue(SampleTestObjectCreator.ORIGINAL_ENDPOINT_WEATHER));
    }

    @Test(description = "Add Composite API")
    public void testAddCompositeApi() throws APIManagementException {
        CompositeAPI.Builder apiBuilder = SampleTestObjectCreator.createUniqueCompositeAPI();

        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        IdentityProvider idp = Mockito.mock(IdentityProvider.class);
        KeyManager km = Mockito.mock(KeyManager.class);
        APIStore apiStore = getApiStoreImpl(idp, km, apiDAO, apiSubscriptionDAO, gatewaySourceGenerator, apiGateway);
        apiStore.addCompositeApi(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addApplicationAssociatedAPI(apiBuilder.build());
    }

    @Test(description = "Update Composite API")
    public void testUpdateCompositeApi() throws APIManagementException {
        // Add a new Composite API
        CompositeAPI.Builder apiBuilder = SampleTestObjectCreator.createUniqueCompositeAPI();

        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        IdentityProvider idp = Mockito.mock(IdentityProvider.class);
        APIStore apiStore = getApiStoreImpl(idp, null, apiDAO, apiSubscriptionDAO, gatewaySourceGenerator, apiGateway);

        String ballerinaImpl = "Ballerina";

        apiStore.addCompositeApi(apiBuilder);

        CompositeAPI createdAPI = apiBuilder.build();

        // Update existing Composite API
        apiBuilder = SampleTestObjectCreator.createUniqueCompositeAPI();
        apiBuilder.id(createdAPI.getId());
        apiBuilder.name(createdAPI.getName());
        apiBuilder.provider(createdAPI.getProvider());
        apiBuilder.version(createdAPI.getVersion());
        apiBuilder.context(createdAPI.getContext());

        Mockito.when(apiDAO.getCompositeAPI(apiBuilder.getId())).thenReturn(createdAPI);
        Mockito.when(apiDAO.getCompositeAPIGatewayConfig(apiBuilder.getId())).thenReturn(
                new ByteArrayInputStream(ballerinaImpl.getBytes(StandardCharsets.UTF_8)));
        Mockito.when(gatewaySourceGenerator.getGatewayConfigFromSwagger(Matchers.anyString(), Matchers.anyString())).
                thenReturn(ballerinaImpl);

        apiStore.updateCompositeApi(apiBuilder);

        CompositeAPI updatedAPI = apiBuilder.build();
        Assert.assertEquals(updatedAPI.getId(), createdAPI.getId());
        Assert.assertEquals(updatedAPI.getName(), createdAPI.getName());
        Assert.assertEquals(updatedAPI.getProvider(), createdAPI.getProvider());
        Assert.assertEquals(updatedAPI.getVersion(), createdAPI.getVersion());
        Assert.assertEquals(updatedAPI.getContext(), createdAPI.getContext());
    }

    @Test(description = "Create new Composite API version")
    public void testCreateNewCompositeApiVersion() throws APIManagementException {
        // Add a new Composite API
        CompositeAPI.Builder apiBuilder = SampleTestObjectCreator.createUniqueCompositeAPI();

        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        GatewaySourceGenerator gatewaySourceGenerator = Mockito.mock(GatewaySourceGenerator.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        IdentityProvider idp = Mockito.mock(IdentityProvider.class);
        APIStore apiStore = getApiStoreImpl(idp, null, apiDAO, apiSubscriptionDAO, gatewaySourceGenerator, apiGateway);

        apiStore.addCompositeApi(apiBuilder);

        CompositeAPI createdAPI = apiBuilder.build();

        // Create new API version
        String newVersion = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getCompositeAPI(apiBuilder.getId())).thenReturn(createdAPI);

        apiStore.createNewCompositeApiVersion(createdAPI.getId(), newVersion);

        final ArgumentCaptor<CompositeAPI> captor = ArgumentCaptor.forClass(CompositeAPI.class);
        Mockito.verify(apiDAO, Mockito.times(2)).addApplicationAssociatedAPI(captor.capture());

        CompositeAPI newAPIVersion = captor.getValue();
        Assert.assertEquals(newAPIVersion.getVersion(), newVersion);
        Assert.assertNotEquals(newAPIVersion.getId(), createdAPI.getId());
        Assert.assertEquals(newAPIVersion.getCopiedFromApiId(), createdAPI.getId());

    }

    @Test(description = "Retrieve an application by name")
    public void testGetApplicationByName() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, null);
        Mockito.when(applicationDAO.getApplicationByName(APP_NAME, USER_ID)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplicationByName(APP_NAME, USER_ID);
        Assert.assertNotNull(application);
        Mockito.verify(applicationDAO, Mockito.times(1)).getApplicationByName(APP_NAME, USER_ID);
    }

    @Test(description = "Retrieve an application by uuid")
    public void testGetApplicationByUUID() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, USER_NAME);
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplicationByUuid(UUID);
        Assert.assertNotNull(application);
        Mockito.verify(applicationDAO, Mockito.times(1)).getApplication(UUID);
    }

    @Test(description = "Add an application")
    public void testAddApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO, apiGateway);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setPermissionString(
                "[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\",\"SUBSCRIPTION\"]}]");
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER))
                .thenReturn
                        (policy);
        ApplicationCreationResponse response = apiStore.addApplication(application);
        Assert.assertNotNull(response.getApplicationUUID());
        Mockito.verify(applicationDAO, Mockito.times(1)).addApplication(application);
    }

    @Test(description = "Add an application with null permission String")
    public void testAddApplicationPermissionStringNull() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO, apiGateway);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setPermissionString(null);
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER))
                .thenReturn
                        (policy);
        ApplicationCreationResponse applicationResponse = apiStore.addApplication(application);
        String applicationUuid = applicationResponse.getApplicationUUID();
        Assert.assertNotNull(applicationUuid);
        Mockito.verify(applicationDAO, Mockito.times(1)).addApplication(application);
    }

    @Test(description = "Add an application with empty permission String")
    public void testAddApplicationPermissionStringEmpty() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO, apiGateway);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setPermissionString("");
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER))
                .thenReturn
                        (policy);
        ApplicationCreationResponse applicationResponse = apiStore.addApplication(application);
        String applicationUuid = applicationResponse.getApplicationUUID();
        Assert.assertNotNull(applicationUuid);
        Mockito.verify(applicationDAO, Mockito.times(1)).addApplication(application);
    }

    @Test(description = "Add an application with invalid permission String")
    public void testAddApplicationPermissionStringInvalid() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO, apiGateway);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setPermissionString("[{\"groupId\": \"testGroup\",\"permission\":[\"TESTREAD\",\"TESTUPDATE\"]}]");
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER))
                .thenReturn
                        (policy);
        ApplicationCreationResponse applicationResponse = apiStore.addApplication(application);
        String applicationUuid = applicationResponse.getApplicationUUID();
        Assert.assertNotNull(applicationUuid);
        Mockito.verify(applicationDAO, Mockito.times(1)).addApplication(application);
    }

    @Test(description = "Add subscription to an application")
    public void testAddSubscription() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = new SubscriptionPolicy(UUID, TIER);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, TIER))
                .thenReturn(policy);
        APIStore apiStore = getApiStoreImpl(apiDAO, applicationDAO, apiSubscriptionDAO, workflowDAO, apiGateway,
                policyDAO);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String apiId = api.getId();
        Application application = new Application("TestApp", USER_ID);
        application.setId(UUID);

        Mockito.when(apiDAO.getAPI(apiId)).thenReturn(api);
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(application);

        SubscriptionResponse subscriptionResponse = apiStore.addApiSubscription(apiId, UUID, TIER);
        String subscriptionId = subscriptionResponse.getSubscriptionUUID();
        Assert.assertNotNull(subscriptionId);

        // before workflow add subscription with blocked state
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).addAPISubscription(subscriptionId, apiId, UUID, policy
                        .getUuid(),
                APIMgtConstants.SubscriptionStatus.ON_HOLD);
        // after workflow change the state
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).updateSubscriptionStatus(subscriptionId,
                APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Add subscription without a valid app", expectedExceptions = APIManagementException.class)
    public void testAddSubscriptionForInvalidApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO, applicationDAO, apiSubscriptionDAO, workflowDAO, apiGateway);

        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String apiId = api.getId();
/*        Application application = new Application("TestApp", USER_ID);
        application.setId(UUID);*/

        Mockito.when(apiDAO.getAPI(apiId)).thenReturn(api);
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(null);

        SubscriptionResponse subscriptionResponse = apiStore.addApiSubscription(apiId, UUID, TIER);
        String subscriptionId = subscriptionResponse.getSubscriptionUUID();
        Assert.assertNotNull(subscriptionId);

        // subscription should not be added
        Mockito.verify(apiSubscriptionDAO, Mockito.times(0)).addAPISubscription(subscriptionId, apiId, UUID, TIER,
                APIMgtConstants.SubscriptionStatus.ON_HOLD);
    }

    @Test(description = "Add subscription without a valid api", expectedExceptions = APIManagementException.class)
    public void testAddSubscriptionForInvalidAPI() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO, applicationDAO, apiSubscriptionDAO, workflowDAO, apiGateway);

        Application application = new Application("TestApp", USER_ID);
        application.setId(UUID);

        Mockito.when(apiDAO.getAPI(API_ID)).thenReturn(null);
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(application);

        SubscriptionResponse subscriptionResponse = apiStore.addApiSubscription(API_ID, UUID, TIER);
        String subscriptionId = subscriptionResponse.getSubscriptionUUID();
        Assert.assertNotNull(subscriptionId);

        // subscription should not be added
        Mockito.verify(apiSubscriptionDAO, Mockito.times(0)).addAPISubscription(subscriptionId, API_ID, UUID, TIER,
                APIMgtConstants.SubscriptionStatus.ON_HOLD);
    }

    @Test(description = "Delete subscription")
    public void testDeleteSubscription() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, apiSubscriptionDAO, workflowDAO, apiGateway);

        Application application = SampleTestObjectCreator.createDefaultApplication();
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        Subscription subscription = new Subscription(UUID, application, api, new SubscriptionPolicy("Gold"));
        Mockito.when(apiSubscriptionDAO.getAPISubscription(UUID)).thenReturn(subscription);
        apiStore.deleteAPISubscription(UUID);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).deleteAPISubscription(UUID);
    }

    @Test(description = "Delete subscription with on_hold state")
    public void testDeleteSubscriptionWithOnholdState() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, apiSubscriptionDAO, workflowDAO, apiGateway);

        Application application = SampleTestObjectCreator.createDefaultApplication();
        APIBuilder builder = SampleTestObjectCreator.createDefaultAPI();
        API api = builder.build();
        Subscription subscription = new Subscription(UUID, application, api, new SubscriptionPolicy("Gold"));
        subscription.setStatus(SubscriptionStatus.ON_HOLD);

        String externalRef = java.util.UUID.randomUUID().toString();
        Mockito.when(workflowDAO.getExternalWorkflowReferenceForPendingTask(subscription.getId(),
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION)).thenReturn(externalRef);

        Mockito.when(apiSubscriptionDAO.getAPISubscription(UUID)).thenReturn(subscription);
        apiStore.deleteAPISubscription(UUID);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).deleteAPISubscription(UUID);
        Mockito.verify(workflowDAO, Mockito.times(1)).getExternalWorkflowReferenceForPendingTask(subscription.getId(),
                WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
    }

    @Test(description = "Get API subscriptions by application")
    public void testGetAPISubscriptionsByApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, apiSubscriptionDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setId(UUID);
        apiStore.getAPISubscriptionsByApplication(application);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).getAPISubscriptionsByApplication(UUID);
    }

    @Test(description = "Add an application with null tier", expectedExceptions = APIManagementException.class)
    public void testAddApplicationNullTier() throws Exception {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(null));
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        apiStore.addApplication(application);
    }

    @Test(description = "Add an application with null policy", expectedExceptions = APIManagementException.class)
    public void testAddApplicationNullPolicy() throws Exception {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER))
                .thenReturn(null);
        apiStore.addApplication(application);
    }

    @Test(description = "Add application with duplicate name",
            expectedExceptions = APIMgtResourceAlreadyExistsException.class)
    public void testAddApplicationWithDuplicateName() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(true);
        apiStore.addApplication(application);
    }

    @Test(description = "Delete application")
    public void testDeleteApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO subscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, subscriptionDAO, workflowDAO, apiGateway);
        Application application = SampleTestObjectCreator.createDefaultApplication();
        application.setId(UUID);
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(application);
        apiStore.deleteApplication(UUID);
        Mockito.verify(applicationDAO, Mockito.times(1)).deleteApplication(UUID);
    }

    @Test(description = "Delete application which is in on_hold state")
    public void testDeleteApplicationWithOnHoldState() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO subscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, subscriptionDAO, workflowDAO, apiGateway);
        Application application = SampleTestObjectCreator.createDefaultApplication();
        application.setStatus(APIMgtConstants.ApplicationStatus.APPLICATION_ONHOLD);
        application.setId(UUID);
        String externalRef = java.util.UUID.randomUUID().toString();
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(application);
        Mockito.when(workflowDAO.getExternalWorkflowReferenceForPendingTask(application.getId(),
                WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION)).thenReturn(externalRef);
        apiStore.deleteApplication(UUID);
        Mockito.verify(applicationDAO, Mockito.times(1)).deleteApplication(UUID);
        Mockito.verify(workflowDAO, Mockito.times(1)).getExternalWorkflowReferenceForPendingTask(application.getId(),
                WorkflowConstants.WF_TYPE_AM_APPLICATION_CREATION);
    }

    @Test(description = "Update an application")
    public void testUpdateApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO, apiGateway);
        Application existingApplication = SampleTestObjectCreator.createDefaultApplication();
        String appUUID = existingApplication.getUuid();
        existingApplication.setStatus(ApplicationStatus.APPLICATION_APPROVED);
        Mockito.when(applicationDAO.getApplication(appUUID)).thenReturn(existingApplication);

        //Updating the existing application
        Application updatedApplication = SampleTestObjectCreator.createDefaultApplication();
        updatedApplication.setDescription("updated description");
        ApplicationPolicy applicationPolicy = SampleTestObjectCreator.createDefaultApplicationPolicy();
        applicationPolicy.setPolicyName(TIER);
        updatedApplication.setPolicy(applicationPolicy);
        updatedApplication.setStatus(ApplicationStatus.APPLICATION_APPROVED);

        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application,
                applicationPolicy.getPolicyName())).thenReturn(applicationPolicy);

        apiStore.updateApplication(appUUID, updatedApplication);
        Mockito.verify(applicationDAO, Mockito.times(1)).updateApplication(appUUID, updatedApplication);

        //Error
        //APIMgtDAOException
        Mockito.doThrow(APIMgtDAOException.class).when(applicationDAO).updateApplication(appUUID, updatedApplication);
        try {
            apiStore.updateApplication(appUUID, updatedApplication);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Error occurred while updating the application - " + appUUID);
        }

        //Error path
        //When specified tier in the updated application is invalid
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application,
                applicationPolicy.getPolicyName())).thenReturn(null);
        try {
            apiStore.updateApplication(appUUID, updatedApplication);
        } catch (APIManagementException e) {
            Assert.assertEquals(e.getMessage(), "Specified tier " + applicationPolicy + " is invalid");
        }
    }

    @Test(description = "Retrieve applications")
    public void testGetApplications() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        apiStore.getApplications(USER_ID);
        Mockito.verify(applicationDAO, Mockito.times(1)).getApplications(USER_ID);
    }

    @Test(description = "Retrieve all tags")
    public void testGetAllTags() throws APIManagementException {
        TagDAO tagDAO = Mockito.mock(TagDAO.class);
        APIStore apiStore = getApiStoreImpl(tagDAO);
        apiStore.getAllTags();
        Mockito.verify(tagDAO, Mockito.times(1)).getTags();
    }

    @Test(description = "Get all policies of a specific policy level")
    public void testGetPolicies() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIStore apiStore = getApiStoreImpl(policyDAO);
        apiStore.getPolicies(APIMgtAdminService.PolicyLevel.application);
        Mockito.verify(policyDAO, Mockito.times(1)).getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application);
    }

    @Test(description = "Get policy given policy name and policy level")
    public void testGetPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIStore apiStore = getApiStoreImpl(policyDAO);
        apiStore.getPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
        Mockito.verify(policyDAO, Mockito.times(1)).getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application,
                POLICY_NAME);
    }

    @Test(description = "Retrieve labels")
    public void testGetLabelInfo() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIStore apiStore = getApiStoreImpl(labelDAO);
        List<Label> labelList = new ArrayList<>();
        List<String> labelNames = new ArrayList<>();
        Label label = SampleTestObjectCreator.createLabel("Public").build();
        labelList.add(label);
        labelNames.add(label.getName());
        Mockito.when(labelDAO.getLabelsByName(labelNames)).thenReturn(labelList);
        List<Label> returnedLabels = apiStore.getLabelInfo(labelNames, USER_NAME);
        Assert.assertEquals(returnedLabels, labelList);
        Mockito.verify(labelDAO, Mockito.times(1)).getLabelsByName(labelNames);
    }

    @Test(description = "Add comment")
    public void testAddComment() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        apiStore.addComment(comment, api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).addComment(comment, api.getId());
    }

    @Test(description = "Get comment")
    public void testGetComment() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(comment.getUuid(), api.getId())).thenReturn(comment);
        apiStore.getCommentByUUID(comment.getUuid(), api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getCommentByUUID(comment.getUuid(), api.getId());
    }

    @Test(description = "Get all comments for an api")
    public void testGetAllCommentsForApi() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        List<Comment> commentList = new ArrayList<>();
        Comment comment1 = SampleTestObjectCreator.createDefaultComment(api.getId());
        Comment comment2 = SampleTestObjectCreator.createDefaultComment(api.getId());
        commentList.add(comment1);
        commentList.add(comment2);
        Mockito.when(apiDAO.getCommentsForApi(api.getId())).thenReturn(commentList);
        List<Comment> commentListFromDB = apiStore.getCommentsForApi(api.getId());
        Assert.assertNotNull(commentListFromDB);
        Assert.assertEquals(commentList.size(), commentListFromDB.size());
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getCommentsForApi(api.getId());
    }

    @Test(description = "Delete comment")
    public void testDeleteComment() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(comment.getUuid(), api.getId())).thenReturn(comment);
        apiStore.deleteComment(comment.getUuid(), api.getId(), "admin");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).deleteComment(comment.getUuid(), api.getId());
    }

    @Test(description = "Update comment")
    public void testUpdateComment() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(UUID, api.getId())).thenReturn(comment);
        apiStore.updateComment(comment, UUID, api.getId(), "admin");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).updateComment(comment, UUID, api.getId());
    }

    @Test(description = "Add rating")
    public void testAddRating() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Rating rating = SampleTestObjectCreator.createDefaultRating(api.getId());
        Mockito.when(apiDAO.getRatingByUUID(api.getId(), rating.getUuid())).thenReturn(rating);
        apiStore.addRating(api.getId(), rating);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).addRating(api.getId(), rating);
    }

    @Test(description = "Update rating")
    public void testUpdateRating() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Rating ratingFromDB = SampleTestObjectCreator.createDefaultRating(api.getId());
        Rating ratingFromPayload = SampleTestObjectCreator.createDefaultRating(api.getId());
        ratingFromPayload.setRating(3);
        Mockito.when(apiDAO.getRatingByUUID(api.getId(), ratingFromDB.getUuid())).thenReturn(ratingFromDB);
        apiStore.updateRating(api.getId(), ratingFromDB.getUuid(), ratingFromPayload);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).updateRating(api.getId(), ratingFromDB.getUuid(), ratingFromPayload);
    }

    @Test(description = "Get rating for api from user")
    public void testGetRatingForApiFromUser() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Rating ratingFromDB = SampleTestObjectCreator.createDefaultRating(api.getId());
        Mockito.when(apiDAO.getRatingByUUID(api.getId(), ratingFromDB.getUuid())).thenReturn(ratingFromDB);
        apiStore.getRatingForApiFromUser(api.getId(), "john");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getUserRatingForApiFromUser(api.getId(), "john");
    }

    @Test(description = "Get rating from rating id")
    public void testGetRatingByUUID() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        String randomRatingUUID = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getRatingByUUID(api.getId(), randomRatingUUID))
                .thenReturn(SampleTestObjectCreator.createDefaultRating(api.getId()));
        apiStore.getRatingByUUID(api.getId(), randomRatingUUID);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getRatingByUUID(api.getId(), randomRatingUUID);
    }

    @Test(description = "Get average rating for a given api")
    public void testGetAverageRating() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        apiStore.getAvgRating(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getAverageRating(api.getId());
    }

    @Test(description = "Get list of ratings for a given api")
    public void testGetRatingsListForApi() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        apiStore.getRatingsListForApi(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(api.getId());
        Mockito.verify(apiDAO, Mockito.times(1)).getRatingsListForApi(api.getId());
    }


    /**
     * Tests to catch exceptions in methods
     */

    @Test(description = "Add rating exception in dao", expectedExceptions = APIRatingException.class)
    public void testAddRatingDaoException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Rating rating = SampleTestObjectCreator.createDefaultRating(api.getId());
        Mockito.when(apiDAO.getRatingByUUID(api.getId(), rating.getUuid())).thenReturn(rating);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while adding rating for api "))
                .when(apiDAO).addRating(api.getId(), rating);
        apiStore.addRating(api.getId(), rating);
    }

    @Test(description = "Exception in dao when retrieving a specific comment",
            expectedExceptions = APIManagementException.class)
    public void testGetCommentDaoException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        String randomUUIDForComment = java.util.UUID.randomUUID().toString();
        Mockito.doThrow(new APIMgtDAOException("Error occurred while retrieving comment " + randomUUIDForComment))
                .when(apiDAO).getCommentByUUID(randomUUIDForComment, api.getId());
        apiStore.getCommentByUUID(randomUUIDForComment, api.getId());
    }

    @Test(description = "Comment not found in db when retrieving comment",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testGetCommentMissingException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        String randomUUIDForComment = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getCommentByUUID(api.getId(), randomUUIDForComment)).thenReturn(null);
        apiStore.getCommentByUUID(randomUUIDForComment, api.getId());
    }

    @Test(description = "Exception in dao when adding a comment", expectedExceptions = APIManagementException.class)
    public void testAddCommentException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.doThrow(new APIMgtDAOException("Error occurred while adding comment for api id " + api.getId()))
                .when(apiDAO).addComment(comment, api.getId());
        apiStore.addComment(comment, api.getId());
    }

    @Test(description = "Exception in dao when deleting a specific comment",
            expectedExceptions = APICommentException.class)
    public void testDeleteCommentDaoException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(comment.getUuid(), api.getId())).thenReturn(comment);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while deleting comment " + comment.getUuid()))
                .when(apiDAO).deleteComment(comment.getUuid(), api.getId());
        apiStore.deleteComment(comment.getUuid(), api.getId(), "admin");
    }

    @Test(description = "check if user is comment moderator while delete comment",
            expectedExceptions = APICommentException.class)
    public void testCommentModeratorWhileDeleteComment() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(comment.getUuid(), api.getId())).thenReturn(comment);
        apiStore.deleteComment(comment.getUuid(), api.getId(), "john");
    }


    @Test(description = "Comment not found in db when retrieving comment before delete",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testDeleteCommentMissingException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        String randomUUIDForComment = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getCommentByUUID(randomUUIDForComment, api.getId())).thenReturn(null);
        apiStore.deleteComment(randomUUIDForComment, api.getId(), "admin");
    }

    @Test(description = "Exception in dao when updating a specific comment",
            expectedExceptions = APICommentException.class)
    public void testUpdateCommentDaoException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(comment.getUuid(), api.getId())).thenReturn(comment);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while updating comment " + comment.getUuid()))
                .when(apiDAO).updateComment(comment, comment.getUuid(), api.getId());
        apiStore.updateComment(comment, comment.getUuid(), api.getId(), "admin");
    }

    @Test(description = "Comment not found in db when retrieving comment before update",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testUpdateCommentMissingException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Comment comment = SampleTestObjectCreator.createDefaultComment(api.getId());
        Mockito.when(apiDAO.getCommentByUUID(comment.getUuid(), api.getId())).thenReturn(null);
        apiStore.updateComment(comment, comment.getUuid(), api.getId(), "admin");
    }

    @Test(description = "Exception in dao when retrieving all comments",
            expectedExceptions = APIManagementException.class)
    public void testGetAllCommentsForApiException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        Mockito.when(apiDAO.getAPI(api.getId())).thenReturn(api);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while retrieving all comments for api " + api.getId()))
                .when(apiDAO).getCommentsForApi(api.getId());
        apiStore.getCommentsForApi(api.getId());
    }

    @Test(description = "Exception when retrieving a specific comment due to missing api",
            expectedExceptions = APIManagementException.class)
    public void testGetCommentApiMissingException()
            throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        String randomUUIDForApi = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getAPI(randomUUIDForApi)).thenReturn(null);
        String randomUUIDForComment = java.util.UUID.randomUUID().toString();
        apiStore.getCommentByUUID(randomUUIDForComment, randomUUIDForApi);
    }

    @Test(description = "Exception when adding a comment due to missing api",
            expectedExceptions = APIManagementException.class)
    public void testAddCommentApiMissingException()
            throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        String randomUUIDForApi = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getAPI(randomUUIDForApi)).thenReturn(null);
        Comment comment = SampleTestObjectCreator.createDefaultComment(randomUUIDForApi);
        apiStore.addComment(comment, randomUUIDForApi);
    }

    @Test(description = "Exception when deleting a comment due to missing api",
            expectedExceptions = APIManagementException.class)
    public void testDeleteCommentApiMissingException()
            throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        String randomUUIDForApi = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getAPI(randomUUIDForApi)).thenReturn(null);
        String randomUUIDForComment = java.util.UUID.randomUUID().toString();
        apiStore.deleteComment(randomUUIDForComment, randomUUIDForApi, "admin");
    }

    @Test(description = "Exception when updating a comment due to missing api",
            expectedExceptions = APIManagementException.class)
    public void testUpdateCommentApiMissingException()
            throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        String randomUUIDForApi = java.util.UUID.randomUUID().toString();
        Mockito.when(apiDAO.getAPI(randomUUIDForApi)).thenReturn(null);
        Comment comment = SampleTestObjectCreator.createDefaultComment(randomUUIDForApi);
        apiStore.updateComment(comment, comment.getUuid(), randomUUIDForApi, "admin");
    }


    @Test(description = "Exception when deleting subscription", expectedExceptions = APIManagementException.class)
    public void testDeleteSubscriptionException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, apiSubscriptionDAO);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while deleting api subscription " + UUID))
                .when(apiSubscriptionDAO).deleteAPISubscription(UUID);
        apiStore.deleteAPISubscription(UUID);
    }

    @Test(description = "Exception when retrieving all tags", expectedExceptions = APIManagementException.class)
    public void testGetAllTagsException() throws APIManagementException {
        TagDAO tagDAO = Mockito.mock(TagDAO.class);
        APIStore apiStore = getApiStoreImpl(tagDAO);
        Mockito.when(tagDAO.getTags()).thenThrow(new APIMgtDAOException("Error occurred while retrieving tags"));
        apiStore.getAllTags();
    }

    @Test(description = "Exception when getting all policies of a specific policy level",
            expectedExceptions = APIManagementException.class)
    public void testGetPoliciesException() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIStore apiStore = getApiStoreImpl(policyDAO);
        Mockito.when(policyDAO.getPoliciesByLevel(APIMgtAdminService.PolicyLevel.application)).
                thenThrow(new APIMgtDAOException(
                        "Error occurred while retrieving policies for policy level - " + APPLICATION_POLICY_LEVEL));
        apiStore.getPolicies(APIMgtAdminService.PolicyLevel.application);
    }

    @Test(description = "Exception when getting policy given policy name and policy level",
            expectedExceptions = APIManagementException.class)
    public void testGetPolicyException() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIStore apiStore = getApiStoreImpl(policyDAO);
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, POLICY_NAME))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving policy - " + POLICY_NAME));
        apiStore.getPolicy(APIMgtAdminService.PolicyLevel.application, POLICY_NAME);
    }

    @Test(description = "Exception when deleting an application", expectedExceptions = APIManagementException.class)
    public void testDeleteApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setId(UUID);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while deleting the application - " + UUID)).when
                (applicationDAO)
                .deleteApplication(UUID);
        apiStore.deleteApplication(UUID);
        Mockito.verify(applicationDAO, Mockito.times(1)).deleteApplication(UUID);
    }

    @Test(description = "Exception when retrieving an application by uuid",
            expectedExceptions = APIManagementException.class)
    public void testGetApplicationByUUIDException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Mockito.when(applicationDAO.getApplication(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving application - " + UUID));
        apiStore.getApplicationByUuid(UUID);
    }

    @Test(description = "Exception when getting API subscriptions by application",
            expectedExceptions = APIManagementException.class)
    public void testGetAPISubscriptionsByApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, apiSubscriptionDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setId(UUID);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsByApplication(application.getId())).thenThrow(new
                APIMgtDAOException(
                "Error occurred while retrieving subscriptions for application - " + application.getName()));
        apiStore.getAPISubscriptionsByApplication(application);
    }

    @Test(description = "Exception when retrieving APIs by status", expectedExceptions = APIManagementException.class)
    public void getAPIsByStatusException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIStore apiStore = getApiStoreImpl(apiDAO);
        String[] statuses = {STATUS_CREATED, STATUS_PUBLISHED};
        Mockito.when(apiDAO.getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED))).
                thenThrow(new APIMgtDAOException(
                        "Error occurred while fetching APIs for the given statuses - " + Arrays.toString(statuses)));
        apiStore.getAllAPIsByStatus(1, 2, statuses);
    }

    @Test(description = "Exception when retrieving an application by name",
            expectedExceptions = APIManagementException.class)
    public void testGetApplicationByNameException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Mockito.when(applicationDAO.getApplicationByName(APP_NAME, USER_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while fetching application for the given applicationName - " + APP_NAME));
        apiStore.getApplicationByName(APP_NAME, USER_ID);
    }

    @Test(description = "Exception when retrieving applications", expectedExceptions = APIManagementException.class)
    public void testGetApplicationsException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Mockito.when(applicationDAO.getApplications(USER_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while fetching applications for the given subscriber - " + USER_ID));
        apiStore.getApplications(USER_ID);
    }

    @Test(description = "Exception when updating an application", expectedExceptions = APIManagementException.class)
    public void testUpdateApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while updating the application - " + UUID)).when
                (applicationDAO)
                .updateApplication(UUID, application);
        apiStore.updateApplication(UUID, application);
    }

    @Test(description = "Exception when adding an application", expectedExceptions = APIManagementException.class)
    public void testAddApplicationCreationException() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER)).thenReturn
                (policy);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while creating the application - " + application
                .getName()))
                .when(applicationDAO).addApplication(application);
        apiStore.addApplication(application);
    }

    @Test(description = "Parse exception when adding an application", expectedExceptions = APIManagementException.class)
    public void testAddApplicationParsingException() throws Exception {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setPermissionString("data");
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER)).thenReturn
                (policy);
        apiStore.addApplication(application);
    }

    @Test(description = "Exception when retrieving labels", expectedExceptions = LabelException.class)
    public void testGetLabelInfoException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIStore apiStore = getApiStoreImpl(labelDAO);
        List<String> labels = new ArrayList<>();
        labels.add("label");
        Mockito.when(labelDAO.getLabelsByName(labels))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving label information"));
        apiStore.getLabelInfo(labels, USER_NAME);
    }

    // End of exception testing

    @Test(description = "Exception when completing application creation workflow without a reference",
            expectedExceptions = APIManagementException.class)
    public void testCompleteApplicationWorkflowWithoutReference() throws Exception {

        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, workflowDAO);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        WorkflowExecutor executor = new DefaultWorkflowExecutor();
        Workflow workflow = new ApplicationCreationWorkflow(applicationDAO, workflowDAO, apiGateway);
        workflow.setWorkflowReference(null);
        apiStore.completeWorkflow(executor, workflow);
    }

    @Test(description = "Test Application workflow rejection")
    public void testAddApplicationWorkflowReject() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO, apiGateway);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setPermissionString(
                "[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\",\"SUBSCRIPTION\"]}]");
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER))
                .thenReturn
                        (policy);

        apiStore.addApplication(application);
        DefaultWorkflowExecutor executor = Mockito.mock(DefaultWorkflowExecutor.class);
        Workflow workflow = new ApplicationCreationWorkflow(applicationDAO, workflowDAO, apiGateway);
        workflow.setWorkflowReference(application.getId());

        WorkflowResponse response = new GeneralWorkflowResponse();
        response.setWorkflowStatus(WorkflowStatus.REJECTED);

        Mockito.when(executor.complete(workflow)).thenReturn(response);
        apiStore.completeWorkflow(executor, workflow);

        Mockito.verify(applicationDAO, Mockito.times(1)).updateApplicationState(application.getId(), "REJECTED");
    }

    @Test(description = "Test Application update workflow reject")
    public void testAddApplicationUpdateWorkflowReject() throws APIManagementException {
        /*
         * This test is to validate the rollback the application to its previous state for application
         * update request rejection
         */
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        APIStore apiStore = getApiStoreImpl(applicationDAO, policyDAO, workflowDAO);
        Application application = new Application(APP_NAME, USER_NAME);
        application.setStatus(ApplicationStatus.APPLICATION_APPROVED);
        application.setPolicy(new ApplicationPolicy(TIER));
        application.setId(UUID);
        application.setPermissionString(
                "[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\",\"SUBSCRIPTION\"]}]");
        Mockito.when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
        Mockito.when(policyDAO.getPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.application, TIER)).thenReturn
                (policy);


        //following section mock the workflow callback api
        DefaultWorkflowExecutor executor = Mockito.mock(DefaultWorkflowExecutor.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        Workflow workflow = new ApplicationUpdateWorkflow(applicationDAO, workflowDAO, apiGateway);
        workflow.setWorkflowReference(application.getId());
        workflow.setExternalWorkflowReference(UUID);

        //validate the rejection flow

        //here we assume the application is an approve state before update
        //this attribute is set internally based on the workflow data
        workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_EXISTIN_APP_STATUS,
                ApplicationStatus.APPLICATION_APPROVED);

        WorkflowResponse response = new GeneralWorkflowResponse();
        response.setWorkflowStatus(WorkflowStatus.REJECTED);

        Mockito.when(executor.complete(workflow)).thenReturn(response);
        apiStore.completeWorkflow(executor, workflow);

        Mockito.verify(applicationDAO, Mockito.times(1)).updateApplicationState(application.getId(),
                ApplicationStatus.APPLICATION_APPROVED);

        //here we assume the application is an rejected state before update. 
        workflow.setAttribute(WorkflowConstants.ATTRIBUTE_APPLICATION_EXISTIN_APP_STATUS,
                ApplicationStatus.APPLICATION_REJECTED);

        apiStore.completeWorkflow(executor, workflow);
        Mockito.verify(applicationDAO, Mockito.times(1)).updateApplicationState(application.getId(),
                ApplicationStatus.APPLICATION_REJECTED);


    }

    @Test(description = "Test Subscription workflow rejection")
    public void testAddSubscriptionWorkflowReject() throws APIManagementException {
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIGateway apiGateway = Mockito.mock(APIGateway.class);
        WorkflowDAO workflowDAO = Mockito.mock(WorkflowDAO.class);
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = new SubscriptionPolicy(UUID, TIER);
        APIStore apiStore = getApiStoreImpl(apiDAO, applicationDAO, apiSubscriptionDAO, workflowDAO, apiGateway,
                policyDAO);
        Mockito.when(policyDAO.getSimplifiedPolicyByLevelAndName(APIMgtAdminService.PolicyLevel.subscription, TIER))
                .thenReturn(policy);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String apiId = api.getId();
        Application application = new Application("TestApp", USER_ID);
        application.setId(UUID);

        Mockito.when(apiDAO.getAPI(apiId)).thenReturn(api);
        Mockito.when(applicationDAO.getApplication(UUID)).thenReturn(application);

        SubscriptionResponse response = apiStore.addApiSubscription(apiId, UUID, TIER);

        DefaultWorkflowExecutor executor = Mockito.mock(DefaultWorkflowExecutor.class);
        Workflow workflow = new SubscriptionCreationWorkflow(apiSubscriptionDAO, workflowDAO, apiGateway);
        workflow.setWorkflowType(APIMgtConstants.WorkflowConstants.WF_TYPE_AM_SUBSCRIPTION_CREATION);
        workflow.setWorkflowReference(response.getSubscriptionUUID());

        WorkflowResponse workflowResponse = new GeneralWorkflowResponse();
        workflowResponse.setWorkflowStatus(WorkflowStatus.REJECTED);

        Mockito.when(executor.complete(workflow)).thenReturn(workflowResponse);
        apiStore.completeWorkflow(executor, workflow);

        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).updateSubscriptionStatus(response.getSubscriptionUUID(),
                SubscriptionStatus.REJECTED);
    }

    @Test(description = "Event Observers registration and removal")
    public void testObserverRegistration() throws APIManagementException {

        EventLogger observer = new EventLogger();

        APIStoreImpl apiStore = getApiStoreImpl();

        apiStore.registerObserver(new EventLogger());

        Map<String, EventObserver> observers = apiStore.getEventObservers();
        Assert.assertEquals(observers.size(), 1);

        apiStore.removeObserver(observers.get(observer.getClass().getName()));

        Assert.assertEquals(observers.size(), 0);

    }

    @Test(description = "Event Observers for event listening")
    public void testObserverEventListener() throws APIManagementException {

        EventLogger observer = Mockito.mock(EventLogger.class);

        APIStoreImpl apiStore = getApiStoreImpl();
        apiStore.registerObserver(observer);

        Event event = Event.APP_CREATION;
        String username = USER_NAME;
        Map<String, String> metaData = new HashMap<>();
        ZonedDateTime eventTime = ZonedDateTime.now(ZoneOffset.UTC);
        apiStore.notifyObservers(event, username, eventTime, metaData);

        Mockito.verify(observer, Mockito.times(1)).captureEvent(event, username, eventTime, metaData);

    }

    @Test(description = "User Self Signup")
    public void testSelfSignUp() throws Exception {
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIStoreImpl apiStore = getApiStoreImpl(identityProvider);
        User user = new User();
        Mockito.doNothing().when(identityProvider).registerUser(user);
        apiStore.selfSignUp(user);
    }

    @Test(description = "User Self Signup Error Case", expectedExceptions = IdentityProviderException.class)
    public void testSelfSignUpErrorCase() throws Exception {
        IdentityProvider identityProvider = Mockito.mock(IdentityProvider.class);
        APIStoreImpl apiStore = getApiStoreImpl(identityProvider);
        User user = new User();
        Mockito.doThrow(IdentityProviderException.class).when(identityProvider).registerUser(user);
        apiStore.selfSignUp(user);
    }

    private APIStoreImpl getApiStoreImpl(ApiDAO apiDAO) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, null, null, null, null, null, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApiDAO apiDAO, LabelDAO labelDAO) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, null, null, null, null, labelDAO, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(IdentityProvider idp, KeyManager keyManager, ApiDAO apiDAO,
                                         APISubscriptionDAO apiSubscriptionDAO,
                                         GatewaySourceGenerator gatewaySourceGenerator, APIGateway apiGateway) {
        return new APIStoreImpl(USER_NAME, idp, keyManager, apiDAO, null, apiSubscriptionDAO, null,
                null, null, null, gatewaySourceGenerator, apiGateway);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, null, null, null, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, PolicyDAO policyDAO, WorkflowDAO workflowDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, policyDAO, null, null, workflowDAO,
                null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, PolicyDAO policyDAO, WorkflowDAO workflowDAO,
                                         APIGateway apiGateway) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, policyDAO, null, null, workflowDAO,
                null, apiGateway);
    }

    private APIStoreImpl getApiStoreImpl(ApiDAO apiDAO, ApplicationDAO applicationDAO,
                                         APISubscriptionDAO apiSubscriptionDAO, WorkflowDAO workflowDAO) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, applicationDAO, apiSubscriptionDAO, null, null, null,
                workflowDAO, null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, APISubscriptionDAO apiSubscriptionDAO,
                                         WorkflowDAO workflowDAO, APIGateway apiGateway) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                workflowDAO, null, apiGateway);
    }

    private APIStoreImpl getApiStoreImpl(ApiDAO apiDAO, ApplicationDAO applicationDAO,
                                         APISubscriptionDAO apiSubscriptionDAO, WorkflowDAO workflowDAO,
                                         APIGateway apiGateway) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, applicationDAO, apiSubscriptionDAO, null, null, null,
                workflowDAO, null, apiGateway);
    }

    private APIStoreImpl getApiStoreImpl(ApiDAO apiDAO, GatewaySourceGenerator gatewaySourceGenerator,
                                         APIGateway apiGateway) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, null, null, null, null, null, null,
                gatewaySourceGenerator, apiGateway);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, APISubscriptionDAO apiSubscriptionDAO,
                                         WorkflowDAO workflowDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                workflowDAO, null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, APISubscriptionDAO apiSubscriptionDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, WorkflowDAO workflowDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, null, null, null, workflowDAO,
                null, null);
    }

    private APIStoreImpl getApiStoreImpl(TagDAO tagDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, null, null, null, tagDAO, null, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(PolicyDAO policyDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, null, null, policyDAO, null, null, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(LabelDAO labelDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, null, null, null, null, labelDAO, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(ApplicationDAO applicationDAO, PolicyDAO policyDAO) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, policyDAO, null, null, null,
                null, null);
    }

    private APIStoreImpl getApiStoreImpl() {
        return new APIStoreImpl(USER_NAME, null, null, null, null, null, null, null, null, null, null, null);
    }

    private APIStoreImpl getApiStoreImpl(IdentityProvider identityProvider) {
        return new APIStoreImpl(USER_NAME, identityProvider, null, null, null, null, null, null, null, null,
                null, null);
    }

    private APIStore getApiStoreImpl(ApplicationDAO applicationDAO, WorkflowDAO workflowDAO, APIGateway apiGateway) {
        return new APIStoreImpl(USER_NAME, null, null, null, applicationDAO, null, null, null, null, workflowDAO, null,
                apiGateway);
    }

    private APIStore getApiStoreImpl(ApiDAO apiDAO, ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, WorkflowDAO workflowDAO, APIGateway apiGateway, PolicyDAO policyDAO) {
        return new APIStoreImpl(USER_NAME, null, null, apiDAO, applicationDAO, apiSubscriptionDAO, policyDAO,
                null, null, workflowDAO, null, apiGateway);
    }
}
