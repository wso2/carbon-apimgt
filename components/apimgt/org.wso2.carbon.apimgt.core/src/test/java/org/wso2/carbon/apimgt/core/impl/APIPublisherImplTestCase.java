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
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.Subscription;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

public class APIPublisherImplTestCase {
    private static final String user = "admin";
    private static final String TIER = "Gold";

    @BeforeClass
    void init() {
        File temp = Files.createTempDir();
        temp.deleteOnExit();
        System.setProperty("gwHome", temp.getAbsolutePath());
        //Set the resource path, where contain composer test JS
        System.setProperty("carbon.home", new File("src/test/resources").getAbsolutePath());
    }

    @Test(description = "Test add api")
    void addApi() throws APIManagementException, LifecycleException {
        /**
         * this test method verify the API Add with correct API object get invoked correctly
         */
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with duplicate context", expectedExceptions = APIManagementException.class)
    void addApiWithDuplicateContext() throws APIManagementException, LifecycleException {
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
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with duplicate name", expectedExceptions = APIManagementException.class)
    void addApiWithDuplicateName() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("weather")).thenReturn(false);
        Mockito.when(apiDAO.isAPINameExists("WeatherAPI", user)).thenReturn(true);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with API Lifecycle failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    void addAPILifecycleFailure() throws LifecycleException, APIManagementException {
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenThrow(new LifecycleException("Couldn't add api lifecycle"));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
    }

    @Test(description = "Get API with valid APIID")
    void getApi() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.getAPIbyUUID(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
    }

    @Test(description = "Delete API with zero Subscriptions")
    void deleteApiWithZeroSubscriptions() throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.deleteAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(lifecycleId);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI(uuid);
    }

    @Test(description = "Delete API with Subscriptions", expectedExceptions = ApiDeleteFailureException.class)
    void deleteApiWithSubscriptions() throws LifecycleException, APIManagementException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        API api = apiBuilder.build();
        String uuid = apiBuilder.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(2L);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null);

        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Test UpdateAPI with Status unchanged")
    void updateAPIWithStatusUnchanged() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.lifeCycleStatus("CREATED").build());
        Mockito.when(apiDAO.isAPIContextExists(api.getContext())).thenReturn(true);

        String bal = "package passthroughservice.samples;\n" + "\n" + "import ballerina.net.http;\n"
                + "@http:BasePath (\"/passthrough\")\n" + "service passthrough {\n" + "\n" + "    @http:GET\n"
                + "    resource passthrough (message m) {\n"
                + "        http:ClientConnector nyseEP = create http:ClientConnector(\"http://localhost:9090\");\n"
                + "        message response = http:ClientConnector.get(nyseEP, \"/nyseStock\", m);\n"
                + "        reply response;\n" + "\n" + "    }\n" + "\n" + "}";
        Mockito.when(apiDAO.getGatewayConfig(uuid)).thenReturn(bal);
        apiPublisher.updateAPI(api.lifeCycleStatus("CREATED").id(uuid));
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(0)).isAPIContextExists(api.getContext());
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid, api.lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with Status unchanged", expectedExceptions = APIManagementException.class)
    void updateAPIWithStatusChanged() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.lifeCycleStatus("PUBLISHED"));
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Update api status")
    void updateAPIStatus() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);

        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("CREATED", "PUBLISH", lifecycleId, user, api))
                .thenReturn(lifecycleState);
        lifecycleState.setState("PUBLISH");
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", new HashMap<>());
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent("CREATED", "PUBLISH", lifecycleId, user, api);
    }

    @Test(description = "Update api status", expectedExceptions = { APIManagementException.class })
    void updateAPIStatusWhileAPINotAvailable() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("CREATED", "PUBLISH", uuid, user, api))
                .thenReturn(SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId));
        Mockito.doThrow(new APIMgtDAOException("Couldn't change the status of api ID ")).when(apiDAO)
                .changeLifeCycleStatus(uuid, "PUBLISH");
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", Collections.emptyMap());
    }

    @Test(description = "Update api status",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    void updateAPIStatusWhileGettingDBFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenThrow(new APIMgtDAOException("Couldn't Create connection"));
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("CREATED", "PUBLISH", uuid, user, api))
                .thenReturn(SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", Collections.emptyMap());
    }

    @Test(description = "Update api status with deprecating previous versions and not require re-subscriptions")
    void updateAPIStatusDeprecatePreviousVersionsAndNotRequireReSubscription()
            throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, applicationDAO, apiSubscriptionDAO, null,
                apiLifecycleManager, null);
        API previousApi = SampleTestObjectCreator.createDefaultAPI().build();
        String previousApiUUID = previousApi.getId();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        api.setCopiedFromApiId(previousApiUUID);
        String lifecycleId = api.getLifecycleInstanceId();
        String lifecycleIdPrevious = previousApi.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        LifecycleState lifecycleState = SampleTestObjectCreator.getMockLifecycleStateObject(lifecycleId);
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("CREATED", "PUBLISH", lifecycleId, user, api))
                .thenReturn(lifecycleState);
        lifecycleState.setState("PUBLISH");
        Mockito.when(apiDAO.getAPI(previousApiUUID)).thenReturn(previousApi);
        LifecycleState previousLifecycleState = SampleTestObjectCreator
                .getMockLifecycleStateObject(lifecycleIdPrevious);
        Mockito.when(apiLifecycleManager.getCurrentLifecycleState(lifecycleIdPrevious))
                .thenReturn(previousLifecycleState);
        Mockito.when(apiLifecycleManager
                .executeLifecycleEvent("PUBLISH", "DEPRECATED", lifecycleIdPrevious, user, previousApi))
                .thenReturn(previousLifecycleState);
        previousLifecycleState.setState("DEPRECATED");
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put("Deprecate old versions after publish the API", true);

        Application application = SampleTestObjectCreator.createDefaultApplication();

        List<Subscription> subscriptions = new ArrayList<>();
        Subscription subscription = new Subscription(previousApiUUID, application, previousApi, TIER);
        subscriptions.add(subscription);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionsByAPI(previousApiUUID)).thenReturn(subscriptions);
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .executeLifecycleEvent("CREATED", "PUBLISH", lifecycleId, user, api);
    }

    @Test(description = "Update checklist item")
    void updateCheckListItem() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put("Deprecate old versions after publish the API", true);
        apiPublisher.updateCheckListItem(uuid, "CREATED", checklist);
        Mockito.verify(apiLifecycleManager, Mockito.times(1))
                .checkListItemEvent(lifecycleId, "CREATED", "Deprecate old versions after publish the API", true);
    }

    @Test(description = "Create new  API version with valid APIID")
    void CreateNewAPIVersion() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
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
    void CreateNewAPIVersionWithInvalidUUID() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI("xxxxxx")).thenReturn(null);
        apiPublisher.createNewAPIVersion("xxxxxx", "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    void CreateNewAPIVersionAndCheckNewApiLifecycleAddFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenThrow(new LifecycleException(""));
        apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(api);
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    void CreateNewAPIVersionAndGetAPIByUuidFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);

        Mockito.when(apiDAO.getAPI("yyyyy")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.createNewAPIVersion("yyyyy", "2.0.0");
    }

    @Test(description = "Check if api exist with valid uuid")
    void CheckIfAPIExistForValidUuid() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("zzzzz")).thenReturn(SampleTestObjectCreator.getMockApiSummaryObject());
        Assert.assertTrue(apiPublisher.checkIfAPIExists("zzzzz"));
    }

    @Test(description = "Check if api exist with invalid uuid")
    void CheckIfAPIExistForInValidUuid() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("aaaaa")).thenReturn(null);
        Assert.assertFalse(apiPublisher.checkIfAPIExists("aaaaa"));
    }

    @Test(description = "Check if api exist with invalid uuid",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    void CheckIfAPIExistWhileGettingJDBCConnectionFailure() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("bbbbb")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.checkIfAPIExists("bbbbb");
    }

    @Test(description = "AddDocumentationInfo",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    void addDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("bbbbb")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.checkIfAPIExists("bbbbb");
    }

    @Test(description = "Exception when updating api status", expectedExceptions = APIManagementException.class)
    void updateAPIStatusException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("CREATED", "PUBLISH", lifecycleId, user, api))
                .thenThrow(new LifecycleException("Couldn't change the status of api ID " + uuid));
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", new HashMap<>());
    }

    @Test(description = "Exception when updating checklist item", expectedExceptions = APIManagementException.class)
    void updateCheckListItemException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.when(apiLifecycleManager
                .checkListItemEvent(lifecycleId, "CREATED", "Deprecate old versions after publish the API", true))
                .thenThrow(new LifecycleException("Couldn't get the lifecycle status of api ID " + uuid));
        Map<String, Boolean> checklist = new HashMap<>();
        checklist.put("Deprecate old versions after publish the API", true);
        apiPublisher.updateCheckListItem(uuid, "CREATED", checklist);
    }
}