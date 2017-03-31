/***********************************************************************************************************************
 * *
 * *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * *
 * *   WSO2 Inc. licenses this file to you under the Apache License,
 * *   Version 2.0 (the "License"); you may not use this file except
 * *   in compliance with the License.
 * *   You may obtain a copy of the License at
 * *
 * *     http://www.apache.org/licenses/LICENSE-2.0
 * *
 * *  Unless required by applicable law or agreed to in writing,
 * *  software distributed under the License is distributed on an
 * *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * *  KIND, either express or implied.  See the License for the
 * *  specific language governing permissions and limitations
 * *  under the License.
 * *
 */

package org.wso2.carbon.apimgt.core.impl;

import com.google.common.io.Files;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.api.EventObserver;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APIStatus;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.models.Event;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.models.UriTemplate;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.lcm.core.beans.AvailableTransitionBean;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class APIPublisherImplTestCase {
    private static final String user = "admin";
    private static final String TIER = "Gold";
    private static final String API_ID = "apiId";
    private static final String DOC_ID = "docId";
    private static final String POLICY_LEVEL = "policyLevel";
    private static final String POLICY_NAME = "policyName";
    private static final String SUB_ID = "subId";
    private static final String ENDPOINT_ID = "endpointId";
    private static final String QUERY_STRING = "queryString";

    @BeforeClass
    void init() {
        File temp = Files.createTempDir();
        temp.deleteOnExit();
        System.setProperty("gwHome", temp.getAbsolutePath());
        //Set the resource path, where contain composer test JS
        System.setProperty("carbon.home", new File("src/test/resources").getAbsolutePath());
    }

    @Test(description = "Test add api with production endpoint")
    public void testAddApi() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id("")
                .endpoint(SampleTestObjectCreator.getMockEndpointMap());
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        String endpointId = apiBuilder.getEndpoint().get("production");
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with sandbox endpoint")
    public void testAddApiSandboxEndpoint() throws APIManagementException, LifecycleException {
        Map<String, String> endpointMap = new HashMap<>();
        endpointMap.put("sandbox", UUID.randomUUID().toString());
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.endpoint(endpointMap);
        uriTemplateBuilder.templateId("getApisApiIdGet");
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy("Unlimited");
        uriTemplateBuilder.httpVerb("GET");
        uriTemplateMap.put("getApisApiIdGet", uriTemplateBuilder.build());
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().id("").endpoint(endpointMap)
                .uriTemplates(uriTemplateMap);
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        String endpointId = apiBuilder.getEndpoint().get("sandbox");
        Endpoint endpoint = new Endpoint.Builder().id(endpointId).name("testEndpoint").build();
        Mockito.when(apiDAO.getEndpoint(endpointId)).thenReturn(endpoint);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api when uri templates are empty")
    public void testAddApiWithEmptyUriTemplate() throws APIManagementException, LifecycleException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().uriTemplates(new HashMap<>());
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with empty templateId and api definition")
    public void testAddApiWithEmptyTemplateIdAndApiDefinition() throws APIManagementException, LifecycleException {
        Map<String, UriTemplate> uriTemplateMap = new HashMap();
        UriTemplate.UriTemplateBuilder uriTemplateBuilder = new UriTemplate.UriTemplateBuilder();
        uriTemplateBuilder.endpoint(Collections.emptyMap());
        uriTemplateBuilder.templateId("");
        uriTemplateBuilder.uriTemplate("/apis/");
        uriTemplateBuilder.authType(APIMgtConstants.AUTH_APPLICATION_LEVEL_TOKEN);
        uriTemplateBuilder.policy("Unlimited");
        uriTemplateBuilder.httpVerb("GET");
        uriTemplateMap.put("", uriTemplateBuilder.build());
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI().uriTemplates(uriTemplateMap)
                .apiDefinition("");
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Updating API Gateway Config")
    public void testUpdateApiGatewayConfig() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.updateApiGatewayConfig(uuid, configString);
        Mockito.verify(apiDAO, Mockito.times(1)).updateGatewayConfig(uuid, configString, user);
    }

    @Test(description = "Exception updating Gateway Config for API", expectedExceptions = APIManagementException.class)
    public void testUpdateApiGatewayConfigException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new APIMgtDAOException("Couldn't update configuration for apiId " + uuid)).when(apiDAO)
                .updateGatewayConfig(uuid, configString, user);
        apiPublisher.updateApiGatewayConfig(uuid, configString);
    }

    @Test(description = "Test add api with duplicate context", expectedExceptions = APIManagementException.class)
    public void testAddApiWithDuplicateContext() throws APIManagementException, LifecycleException {
        /**
         * This method check by adding duplicate api context
         */
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("weather")).thenReturn(true);
        Mockito.when(apiDAO.isAPINameExists("WeatherAPI", user)).thenReturn(false);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with duplicate name", expectedExceptions = APIManagementException.class)
    public void testAddApiWithDuplicateName() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("weather")).thenReturn(false);
        Mockito.when(apiDAO.isAPINameExists("WeatherAPI", user)).thenReturn(true);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with API Lifecycle failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    public void testAddAPILifecycleFailure() throws LifecycleException, APIManagementException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenThrow(new LifecycleException("Couldn't add api lifecycle"));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
    }

    @Test(description = "Get API with valid APIID")
    public void testGetApi() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.getAPIbyUUID(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
    }

    @Test(description = "Delete API with zero Subscriptions")
    public void testDeleteApiWithZeroSubscriptions() throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.deleteAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(lifecycleId);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI(uuid);
    }

    @Test(description = "Exception when getting api subscription count by API",
            expectedExceptions = APIManagementException.class)
    public void testGetAPISubscriptionCountByAPI() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve Subscriptions for API " + uuid));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null, null, null,
                null);
        apiPublisher.getAPISubscriptionCountByAPI(uuid);

    }

    @Test(description = "Error occurred while deleting API with zero subscriptions",
            expectedExceptions = APIManagementException.class)
    public void testDeleteApiWithZeroSubscriptionsException()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while deleting the API with id" + uuid)).when(apiDAO)
                .deleteAPI(uuid);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Search APIs")
    public void testSearchAPIs() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        List<API> apimResultsFromDAO = new ArrayList<>();
        Mockito.when(apiDAO.searchAPIs(new ArrayList<>(), user, QUERY_STRING, 1, 2)).thenReturn(apimResultsFromDAO);
        List<API> apis = apiPublisher.searchAPIs(2, 1, QUERY_STRING);
        Assert.assertNotNull(apis);
        Mockito.verify(apiDAO, Mockito.atLeastOnce()).searchAPIs(APIUtils.getAllRolesOfUser(user),
                user, QUERY_STRING, 1, 2);
    }

    @Test(description = "Search APIs with null query string")
    public void testSearchAPIsWithNullQuery() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        List<API> apimResultsFromDAO = new ArrayList<>();
        Mockito.when(apiDAO.searchAPIs(new ArrayList<>(), user, null, 1, 2)).thenReturn(apimResultsFromDAO);
        apiPublisher.searchAPIs(2, 1, null);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPIs();
    }

    @Test(description = "Exception when searching APIs", expectedExceptions = APIManagementException.class)
    public void testSearchAPIsException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.searchAPIs(APIUtils.getAllRolesOfUser(user), user, QUERY_STRING, 1, 2))
                .thenThrow(new APIMgtDAOException("Error occurred while Searching the API with query pizza"));
        apiPublisher.searchAPIs(2, 1, QUERY_STRING);
    }

    @Test(description = "Get APIs by provider")
    public void testGetAPIsByProvider() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPIsForProvider(user)).thenReturn(new ArrayList<API>());
        apiPublisher.getAPIsByProvider(user);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPIsForProvider(user);
    }

    @Test(description = "Exception when get APIs by provider", expectedExceptions = APIManagementException.class)
    public void testGetAPIsByProviderException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPIsForProvider(user))
                .thenThrow(new APIMgtDAOException("Unable to fetch APIs of " + user));
        apiPublisher.getAPIsByProvider(user);
    }

    @Test(description = "Error occurred while disassociating the API with Lifecycle when deleting the API",
            expectedExceptions = APIManagementException.class)
    public void testDeleteApiWithZeroSubscriptionsLifeCycleException()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Error occurred while Disassociating the API with Lifecycle id " + uuid))
                .when(apiLifecycleManager).removeLifecycle(lifecycleId);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Delete API with Subscriptions", expectedExceptions = ApiDeleteFailureException.class)
    public void testDeleteApiWithSubscriptions() throws LifecycleException, APIManagementException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String uuid = apiBuilder.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(2L);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Test UpdateAPI with Status unchanged")
    public void testUpdateAPIWithStatusUnchanged() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
        Mockito.when(apiDAO.isAPIContextExists(api.getContext())).thenReturn(true);
        String configString = SampleTestObjectCreator.createSampleGatewayConfig();
        Mockito.when(apiDAO.getGatewayConfig(uuid)).thenReturn(configString);
        apiPublisher.updateAPI(api.lifeCycleStatus(APIStatus.CREATED.getStatus()).id(uuid));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, api.lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with Status changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithStatusChanged() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.lifeCycleStatus(APIStatus.PUBLISHED.getStatus()));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid,
                new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with context changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithContextChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.context("test"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid,
                new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with api name changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithNameChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.name("testApi"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid,
                new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with version changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithVersionChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.version("1.1.0"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid,
                new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Test UpdateAPI with provider changed", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithProviderChange() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.provider("testProvider"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid,
                new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus(APIStatus.CREATED.getStatus()).build());
    }

    @Test(description = "Exception when updating API", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.doThrow(new APIMgtDAOException("Error occurred while updating the API - " + api.getName())).when(apiDAO)
                .updateAPI(uuid, api.build());
        apiPublisher.updateAPI(api);
    }

    @Test(description = "Parse exception when updating API", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIParseException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI().permission("data");
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api);
    }

    @Test(description = "Test UpdateAPI with Status unchanged but different context")
    public void testUpdateAPIWithStatusUnchangedButDifferentContext() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.context("testContext"));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid, api.build());
    }

    @Test(description = "Test UpdateAPI with Status unchanged but context exist",
            expectedExceptions = APIManagementException.class)
    public void testUpdateAPIWithStatusUnchangedWhenContextExists() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        Mockito.when(apiDAO.isAPIContextExists("testContext")).thenReturn(true);
        apiPublisher.updateAPI(api.context("testContext"));
    }

    @Test(description = "Test UpdateAPI with null api returned by uuid",
            expectedExceptions = APIManagementException.class)
    public void testUpdateNullAPI() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        apiPublisher.updateAPI(api);
    }

    @Test(description = "Update api status")
    public void testUpdateAPIStatus() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api)).thenReturn(lifecycleState);
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), new HashMap<>());
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api);
    }

    @Test(description = "Update api status", expectedExceptions = { APIManagementException.class })
    public void testUpdateAPIStatusWhileAPINotAvailable() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), uuid, user, api))
                .thenReturn(SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId));
        Mockito.doThrow(new APIMgtDAOException("Couldn't change the status of api ID " + uuid)).when(apiDAO)
                .changeLifeCycleStatus(uuid, APIStatus.PUBLISHED.getStatus());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), Collections.emptyMap());
    }

    @Test(description = "Update api status",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    public void testUpdateAPIStatusWhileGettingDBFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenThrow(new APIMgtDAOException("Couldn't Create connection"));
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), uuid, user, api))
                .thenReturn(SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), Collections.emptyMap());
    }

    @Test(description = "Update api status with deprecating previous versions and not require re-subscriptions")
    public void testUpdateAPIStatusDeprecatePreviousVersionsAndNotRequireReSubscription()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, applicationDAO, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null);

        API previousApi = SampleTestObjectCreator.createDefaultAPI().build();
        String previousApiUUID = previousApi.getId();
        String lifecycleIdPrevious = previousApi.getLifecycleInstanceId();
        LifecycleState previousLifecycleState = SampleTestObjectCreator
                .getMockLifecycleStateObject(lifecycleIdPrevious);
        List<AvailableTransitionBean> list = new ArrayList<>();
        AvailableTransitionBean bean = new AvailableTransitionBean("Deprecate", APIStatus.DEPRECATED.getStatus());
        list.add(bean);
        previousLifecycleState.setAvailableTransitionBeanList(list);
        Mockito.when(apiLifecycleManager.getCurrentLifecycleState(lifecycleIdPrevious))
                .thenReturn(previousLifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.PUBLISHED.getStatus(), APIStatus.DEPRECATED.getStatus(),
                        lifecycleIdPrevious, user, previousApi)).thenReturn(previousLifecycleState);
        previousLifecycleState.setState(APIStatus.DEPRECATED.getStatus());

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        api.setCopiedFromApiId(previousApiUUID);
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api)).thenReturn(lifecycleState);
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());

        Mockito.when(apiDAO.getAPI(previousApiUUID)).thenReturn(previousApi);
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);

        Application application = SampleTestObjectCreator.createDefaultApplication();

        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription(previousApiUUID, application, previousApi, TIER);
        subscriptions.add(subscription);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsByAPI(previousApiUUID)).thenReturn(subscriptions);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api);
    }

    @Test(description = "Update api status with re-subscriptions")
    public void testUpdateAPIStatusRequireReSubscription() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, applicationDAO, apiSubscriptionDAO, null,
                apiLifecycleManager, null, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api)).thenReturn(lifecycleState);
        lifecycleState.setState(APIStatus.PUBLISHED.getStatus());
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.REQUIRE_RE_SUBSCRIPTIONS, true);
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api);
    }

    @Test(description = "Update checklist item")
    public void testUpdateCheckListItem() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);
        apiPublisher.updateCheckListItem(uuid, APIStatus.CREATED.getStatus(), checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .checkListItemEvent(lifecycleId, APIStatus.CREATED.getStatus(),
                        APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);
    }

    @Test(description = "Create new  API version with valid APIID")
    public void testCreateNewAPIVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        String newUUid = apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
        Assert.assertNotEquals(uuid, newUUid);
    }

    @Test(description = "Create new  API version when API definition is empty")
    public void testCreateNewAPIVersionWithEmptyAPIDefinition() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().apiDefinition("").build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        String newUUid = apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
        Assert.assertNotEquals(uuid, newUUid);
    }

    @Test(description = "Create new  API version with invalid APIID",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testCreateNewAPIVersionWithInvalidUUID() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI("xxxxxx")).thenReturn(null);
        apiPublisher.createNewAPIVersion("xxxxxx", "2.0.0");
    }

    @Test(description = "Create new  API version with empty APIID")
    void testCreateNewAPIVersionWithEmptyUUID() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        try {
            apiPublisher.createNewAPIVersion(null, "2.0.0");
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("API ID cannot be empty"));
        }
    }

    @Test(description = "Create new  API version with invalid API version")
    void testCreateNewAPIVersionWithEmptyVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        try {
            apiPublisher.createNewAPIVersion(uuid, null);
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("New API version cannot be empty"));
        }
    }

    @Test(description = "Create new  API version with previous API version")
    void testCreateNewAPIVersionWithPreviousVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        try {
            apiPublisher.createNewAPIVersion(uuid, api.getVersion());
        } catch (APIManagementException e) {
            Assert.assertTrue(e.getMessage().contains("cannot be same as the previous version"));
        }
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    public void testCreateNewAPIVersionAndCheckNewApiLifecycleAddFailure()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenThrow(new LifecycleException(""));
        apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    public void testCreateNewAPIVersionAndGetAPIByUuidFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        Mockito.when(apiDAO.getAPI("yyyyy")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.createNewAPIVersion("yyyyy", "2.0.0");
    }

    @Test(description = "Check if api exist with valid uuid")
    public void testCheckIfAPIExistForValidUuid() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("zzzzz")).thenReturn(SampleTestObjectCreator.getMockApiSummaryObject());
        Assert.assertTrue(apiPublisher.checkIfAPIExists("zzzzz"));
    }

    @Test(description = "Check if api exist with invalid uuid")
    public void testCheckIfAPIExistForInValidUuid() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("aaaaa")).thenReturn(null);
        Assert.assertFalse(apiPublisher.checkIfAPIExists("aaaaa"));
    }

    @Test(description = "Check if api exist with invalid uuid",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    public void testCheckIfAPIExistWhileGettingJDBCConnectionFailure() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("bbbbb")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.checkIfAPIExists("bbbbb");
    }

    @Test(description = "Add Documentation Info")
    public void testAddDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentInfo(API_ID, documentInfo);
    }

    @Test(description = "Document already exists error when adding Documentation Info",
            expectedExceptions = APIManagementException.class)
    public void testAddDocumentationInfoDocAlreadyExists() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.isDocumentExist(API_ID, documentInfo)).thenReturn(true);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Unable to add documentation info", expectedExceptions = APIManagementException.class)
    public void testUnableToAddDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation")).when(apiDAO)
                .addDocumentInfo(API_ID, documentInfo);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Parse exception when adding documentation info",
            expectedExceptions = APIManagementException.class)
    public void testAddDocumentationInfoJsonParseException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id("")
                .permission("data").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Remove Documentation Info")
    public void testRemoveDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.removeDocumentation(DOC_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteDocument(DOC_ID);
    }

    @Test(description = "Exception when removing Documentation Info", expectedExceptions = APIManagementException.class)
    public void testRemoveDocumentationInfoException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation with file")).when(apiDAO)
                .deleteDocument(DOC_ID);
        apiPublisher.removeDocumentation(DOC_ID);
    }

    @Test(description = "Upload Documentation File")
    public void testUploadDocumentationFile() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.uploadDocumentationFile(DOC_ID, null, "testDoc");
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentFileContent(DOC_ID, null, "testDoc", user);
    }

    @Test(description = "Exception when uploading Documentation File",
            expectedExceptions = APIManagementException.class)
    public void testUploadDocumentationFileException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation with file")).when(apiDAO)
                .addDocumentFileContent(DOC_ID, null, "testDoc", user);
        apiPublisher.uploadDocumentationFile(DOC_ID, null, "testDoc");
    }

    @Test(description = "Add documentation inline content")
    public void testAddDocumentationContent() throws APIManagementException, IOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        String inlineContent = SampleTestObjectCreator.createDefaultInlineDocumentationContent();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.addDocumentationContent(DOC_ID, inlineContent);
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentInlineContent(DOC_ID, inlineContent, user);
    }

    @Test(description = "Update Documentation Info")
    public void testUpdateDocumentation() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
        Mockito.verify(apiDAO, Mockito.times(1)).updateDocumentInfo(API_ID, documentInfo, user);
    }

    @Test(description = "Documentation already exists error when updating Documentation Info",
            expectedExceptions = APIManagementException.class)
    public void testUpdateDocumentationDocAlreadyExists() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        Mockito.when(apiDAO.isDocumentExist(API_ID, documentInfo)).thenReturn(true);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Unable to update documentation info", expectedExceptions = APIManagementException.class)
    public void testUnableToUpdateDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = SampleTestObjectCreator.createDefaultDocumentationInfo();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation")).when(apiDAO)
                .updateDocumentInfo(API_ID, documentInfo, user);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Parse exception when updating documentation info",
            expectedExceptions = APIManagementException.class)
    public void testUpdateDocumentationInfoJsonParseException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id("")
                .permission("data").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Exception when updating api status", expectedExceptions = APIManagementException.class)
    public void testUpdateAPIStatusException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent(APIStatus.CREATED.getStatus(), APIStatus.PUBLISHED.getStatus(), lifecycleId,
                        user, api)).thenThrow(new LifecycleException("Couldn't change the status of api ID " + uuid));
        apiPublisher.updateAPIStatus(uuid, APIStatus.PUBLISHED.getStatus(), new HashMap<>());
    }

    @Test(description = "Exception when updating checklist item", expectedExceptions = APIManagementException.class)
    public void testUpdateCheckListItemException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.checkListItemEvent(lifecycleId, APIStatus.CREATED.getStatus(),
                APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true))
                .thenThrow(new LifecycleException("Couldn't get the lifecycle status of api ID " + uuid));
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put(APIMgtConstants.DEPRECATE_PREVIOUS_VERSIONS, true);
        apiPublisher.updateCheckListItem(uuid, APIStatus.CREATED.getStatus(), checklist);
    }

    @Test(description = "Get lifecycle events list of an API")
    public void testGetLifeCycleEvents() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        List<LifecycleHistoryBean> lifecycleHistoryBeanList = new ArrayList<>();
        LifecycleHistoryBean bean = new LifecycleHistoryBean();
        bean.setPreviousState(APIStatus.CREATED.getStatus());
        bean.setPostState(APIStatus.DEPRECATED.getStatus());
        bean.setUser(user);
        lifecycleHistoryBeanList.add(bean);
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doReturn(lifecycleHistoryBeanList).when(apiLifecycleManager).getLifecycleHistory(lifecycleId);
        apiPublisher.getLifeCycleEvents(uuid);
    }

    @Test(description = "Exception finding APISummary Resource when getting lifecycle events list of an API",
            expectedExceptions = APIManagementException.class)
    public void testGetLifeCycleEventsExceptionFindingApiSummaryResource()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPISummary(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't find APISummary Resource for ID " + API_ID));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.getLifeCycleEvents(API_ID);
    }

    @Test(description = "Exception finding API LifeCycle History when getting lifecycle events list of an API",
            expectedExceptions = APIManagementException.class)
    public void testGetLifeCycleEventsExceptionFindingAPILifeCycleHistory()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Couldn't find APILifecycle History for ID " + uuid))
                .when(apiLifecycleManager).getLifecycleHistory(lifecycleId);
        apiPublisher.getLifeCycleEvents(uuid);
    }

    @Test(description = "Get api lifecycle data")
    public void testGetAPILifeCycleData() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        LifecycleState bean = new LifecycleState();
        bean.setState(APIStatus.CREATED.getStatus());
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doReturn(bean).when(apiLifecycleManager).getCurrentLifecycleState(lifecycleId);
        apiPublisher.getAPILifeCycleData(uuid);
    }

    @Test(description = "Get api lifecycle data for a null api",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testGetAPILifeCycleDataForNullAPI() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary(API_ID)).thenReturn(null);
        apiPublisher.getAPILifeCycleData(API_ID);
    }

    @Test(description = "Could not retrieve api summary when Getting api lifecycle data",
            expectedExceptions = APIManagementException.class)
    public void testGetAPILifeCycleDataExceptionWhenRetrievingAPISummary()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve API Summary for " + API_ID));
        apiPublisher.getAPILifeCycleData(API_ID);
    }

    @Test(description = "Could not retrieve api lifecycle when Getting api lifecycle data",
            expectedExceptions = APIManagementException.class)
    public void testGetAPILifeCycleDataExceptionWhenRetrievingAPILifeCycle()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Couldn't retrieve API Lifecycle for " + uuid)).when(apiLifecycleManager)
                .getCurrentLifecycleState(lifecycleId);
        apiPublisher.getAPILifeCycleData(uuid);
    }

    @Test(description = "Save thumbnail image for API")
    public void testSaveThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        InputStream image = SampleTestObjectCreator.createDefaultThumbnailImage();
        apiPublisher.saveThumbnailImage(API_ID, image, "png");
        Mockito.verify(apiDAO, Mockito.times(1)).updateImage(API_ID, image, "png", user);
    }

    @Test(description = "Exception when saving thumbnail image for API",
            expectedExceptions = APIManagementException.class)
    public void testSaveThumbnailImageException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Couldn't save the thumbnail image")).when(apiDAO)
                .updateImage(API_ID, null, "jpeg", user);
        apiPublisher.saveThumbnailImage(API_ID, null, "jpeg");
    }

    @Test(description = "Get thumbnail image for API")
    public void testGetThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getImage(uuid)).thenReturn(null);
        apiPublisher.getThumbnailImage(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getImage(uuid);
    }

    @Test(description = "Exception when getting thumbnail image for API",
            expectedExceptions = APIManagementException.class)
    public void testGetThumbnailImageException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getImage(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve thumbnail for api " + API_ID));
        apiPublisher.getThumbnailImage(API_ID);
    }

    @Test(description = "Retrieving all labels")
    public void testGetAllLabels() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, null, null, labelDAO, null);
        Mockito.when(labelDAO.getLabels()).thenReturn(new ArrayList<Label>());
        apiPublisher.getAllLabels();
        Mockito.verify(labelDAO, Mockito.times(1)).getLabels();
    }

    @Test(description = "Exception when retrieving all labels", expectedExceptions = APIManagementException.class)
    public void testGetAllLabelsException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, null, null, labelDAO, null);
        Mockito.when(labelDAO.getLabels()).thenThrow(new APIMgtDAOException("Error occurred while retrieving labels"));
        apiPublisher.getAllLabels();
    }

    @Test(description = "Update subscription status")
    public void testUpdateSubscriptionStatus() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null,
                null);
        apiPublisher.updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1))
                .updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Error when updating subscription status", expectedExceptions = APIManagementException.class)
    public void testUpdateSubscriptionStatusException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null,
                null);
        Mockito.doThrow(new APIMgtDAOException("", new Throwable())).when(apiSubscriptionDAO)
                .updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
        apiPublisher.updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Update subscription policy")
    public void testUpdateSubscriptionPolicy() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null,
                null);
        apiPublisher.updateSubscriptionPolicy(SUB_ID, "test policy");
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).updateSubscriptionPolicy(SUB_ID, "test policy");
    }

    @Test(description = "Error when updating subscription policy", expectedExceptions = APIManagementException.class)
    public void testUpdateSubscriptionPolicyException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null,
                null);
        Mockito.doThrow(new APIMgtDAOException("", new Throwable())).when(apiSubscriptionDAO)
                .updateSubscriptionPolicy(SUB_ID, "test policy");
        apiPublisher.updateSubscriptionPolicy(SUB_ID, "test policy");
    }

    @Test(description = "Get last updated time of endpoint")
    public void testGetLastUpdatedTimeOfEndpoint() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID)).thenReturn("2017-03-19T13:45:30");
        apiPublisher.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).getLastUpdatedTimeOfEndpoint(ENDPOINT_ID);
    }

    @Test(description = "Exception when getting last updated time of endpoint",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfEndpointException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last update time of the endpoint with id " + ENDPOINT_ID));
        apiPublisher.getLastUpdatedTimeOfEndpoint(ENDPOINT_ID);
    }

    @Test(description = "Get last updated time of Throttling Policy")
    public void testGetLastUpdatedTimeOfThrottlingPolicy() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, policyDAO, null, null, null);
        Mockito.when(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(POLICY_LEVEL, POLICY_NAME))
                .thenReturn("2017-03-19T13:45:30");
        apiPublisher.getLastUpdatedTimeOfThrottlingPolicy(POLICY_LEVEL, POLICY_NAME);
        Mockito.verify(policyDAO, Mockito.times(1)).getLastUpdatedTimeOfThrottlingPolicy(POLICY_LEVEL, POLICY_NAME);
    }

    @Test(description = "Get last updated time of Gateway Config")
    public void testGetLastUpdatedTimeOfGatewayConfig() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getLastUpdatedTimeOfGatewayConfig(API_ID)).thenReturn("2017-03-19T13:45:30");
        apiPublisher.getLastUpdatedTimeOfGatewayConfig(API_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).getLastUpdatedTimeOfGatewayConfig(API_ID);
    }

    @Test(description = "Exception when getting last updated time of Gateway Config",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfGatewayConfigException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getLastUpdatedTimeOfGatewayConfig(API_ID)).thenThrow(new APIMgtDAOException(
                "Error occurred while retrieving the last update time of the gateway configuration of API with id "
                        + API_ID));
        apiPublisher.getLastUpdatedTimeOfGatewayConfig(API_ID);
    }

    @Test(description = "Exception when getting last updated time of Throttling Policy",
            expectedExceptions = APIManagementException.class)
    public void testGetLastUpdatedTimeOfThrottlingPolicyException() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, policyDAO, null, null, null);
        Mockito.when(policyDAO.getLastUpdatedTimeOfThrottlingPolicy(POLICY_LEVEL, POLICY_NAME)).thenThrow(
                new APIMgtDAOException(
                        "Error while retrieving last updated time of policy :" + POLICY_LEVEL + "/" + POLICY_LEVEL));
        apiPublisher.getLastUpdatedTimeOfThrottlingPolicy(POLICY_LEVEL, POLICY_NAME);
    }

    @Test(description = "Register gateway labels")
    public void testRegisterGatewayLabels() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        System.setProperty(APIMgtConstants.OVERWRITE_LABELS, "true");
        List<Label> labels = new ArrayList<>();
        Label label1 = SampleTestObjectCreator.createLabel("testLabel1").build();
        Label label2 = SampleTestObjectCreator.createLabel("testLabel2").build();
        labels.add(label1);
        List<String> labelNames = new ArrayList<>();
        labelNames.add(label1.getName());
        List<Label> existingLabels = new ArrayList<>();
        existingLabels.add(label1);
        existingLabels.add(label2);
        Mockito.when(labelDAO.getLabelsByName(labelNames)).thenReturn(existingLabels);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, null, null, labelDAO, null);
        apiPublisher.registerGatewayLabels(labels);
        Mockito.verify(labelDAO, Mockito.times(1)).addLabels(labels);
    }

    @Test(description = "Exception when registering gateway labels", expectedExceptions = APIManagementException.class)
    public void testRegisterGatewayLabelsException() throws APIManagementException {
        LabelDAO labelDAO = Mockito.mock(LabelDAO.class);
        System.setProperty(APIMgtConstants.OVERWRITE_LABELS, "false");
        List<Label> labels = new ArrayList<>();
        Label label = SampleTestObjectCreator.createLabel("testLabel1").build();
        labels.add(label);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, null, null, labelDAO, null);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while adding label information")).when(labelDAO)
                .addLabels(labels);
        apiPublisher.registerGatewayLabels(labels);
    }

    @Test(description = "Get all policies by level")
    public void testGetAllPoliciesByLevel() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        List<Policy> policies = new ArrayList<>();
        Policy policy = Mockito.mock(Policy.class);
        policy.setPolicyName(POLICY_NAME);
        policies.add(policy);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, policyDAO, null, null, null);
        Mockito.when(policyDAO.getPolicies(POLICY_LEVEL)).thenReturn(policies);
        apiPublisher.getAllPoliciesByLevel(POLICY_LEVEL);
        Mockito.verify(policyDAO, Mockito.times(1)).getPolicies(POLICY_LEVEL);
    }

    @Test(description = "Get all policy by name")
    public void testGetPolicyByName() throws APIManagementException {
        PolicyDAO policyDAO = Mockito.mock(PolicyDAO.class);
        Policy policy = Mockito.mock(Policy.class);
        policy.setPolicyName(POLICY_NAME);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, policyDAO, null, null, null);
        Mockito.when(policyDAO.getPolicy(POLICY_LEVEL, POLICY_NAME)).thenReturn(policy);
        apiPublisher.getPolicyByName(POLICY_LEVEL, POLICY_NAME);
        Mockito.verify(policyDAO, Mockito.times(1)).getPolicy(POLICY_LEVEL, POLICY_NAME);
    }

    @Test(description = "Save swagger definition for API")
    public void testSaveSwagger20Definition() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.saveSwagger20Definition(uuid, SampleTestObjectCreator.apiDefinition);
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateSwaggerDefinition(uuid, SampleTestObjectCreator.apiDefinition, user);
    }

    @Test(description = "Exception when saving swagger definition for API",
            expectedExceptions = APIManagementException.class)
    public void testSaveSwagger20DefinitionException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new APIMgtDAOException("Couldn't update the Swagger Definition")).when(apiDAO)
                .updateSwaggerDefinition(uuid, SampleTestObjectCreator.apiDefinition, user);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        apiPublisher.saveSwagger20Definition(uuid, SampleTestObjectCreator.apiDefinition);
    }

    @Test(description = "Event Observers registration and removal")
    public void testObserverRegistration() throws APIManagementException {

        EventLogger observer = new EventLogger();

        APIPublisherImpl apiPub = new APIPublisherImpl(user, null, null, null, null, null, null, null);

        apiPub.registerObserver(new EventLogger());

        Map<String, EventObserver> observers = apiPub.getEventObservers();
        Assert.assertEquals(observers.size(), 1);

        apiPub.removeObserver(observers.get(observer.getClass().getName()));

        Assert.assertEquals(observers.size(), 0);

    }

    @Test(description = "Event Observers for event listening")
    public void testObserverEventListener() throws APIManagementException {

        EventLogger observer = Mockito.mock(EventLogger.class);

        APIPublisherImpl apiPub = new APIPublisherImpl(user, null, null, null, null, null, null, null);
        apiPub.registerObserver(observer);

        Event event = Event.APP_CREATION;
        String username = user;
        Map<String, String> metaData = new HashMap<>();
        ZonedDateTime eventTime = ZonedDateTime.now(ZoneOffset.UTC);
        apiPub.notifyObservers(event, username, eventTime, metaData);

        Mockito.verify(observer, Mockito.times(1)).captureEvent(event, username, eventTime, metaData);

    }

    @Test(description = "Add api from definition")
    public void testAddApiFromDefinition() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        String def = SampleTestObjectCreator.apiDefinition;
        InputStream apiDefinition = new ByteArrayInputStream(def.getBytes());
        apiPublisher.addApiFromDefinition(apiDefinition);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Couldn't retrieve swagger definition for api when getting api gateway config",
            expectedExceptions = APIManagementException.class)
    public void testGetApiGatewayConfigException() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null, null);
        Mockito.when(apiDAO.getGatewayConfig(API_ID))
                .thenThrow(new APIMgtDAOException("Error generating swagger from gateway config " + API_ID));
        apiPublisher.getApiGatewayConfig(API_ID);
    }

    @Test(description = "Error while getting swagger resource from url when adding api from swagger resource",
            expectedExceptions = APIManagementException.class)
    public void testAddApiFromDefinitionErrorGettingSwaggerResource()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addApiFromDefinition("http://test");
        Mockito.verify(apiLifecycleManager, Mockito.times(0)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Malformed url when adding api from swagger resource",
            expectedExceptions = APIManagementException.class)
    public void testAddApiFromDefinitionMalformedUrl() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null,
                null);
        apiPublisher.addApiFromDefinition("http/test");
        Mockito.verify(apiLifecycleManager, Mockito.times(0)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

}
