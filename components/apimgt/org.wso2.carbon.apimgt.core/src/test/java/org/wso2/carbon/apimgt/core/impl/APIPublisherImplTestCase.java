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
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.*;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleEventManager;
import org.wso2.carbon.lcm.core.impl.LifecycleState;
import org.wso2.carbon.lcm.sql.beans.LifecycleHistoryBean;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.mock;

public class APIPublisherImplTestCase {
    private static final String user = "admin";
    private static final String TIER = "Gold";
    private static final String API_ID = "apiId";
    private static final String DOC_ID = "docId";
    private static final String SUB_ID = "subId";

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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        API.APIBuilder apiBuilder = SampleTestObjectCreator.createDefaultAPI();
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIMgtConstants.API_LIFECYCLE, user))
                .thenThrow(new LifecycleException("Couldn't add api lifecycle"));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        apiPublisher.addAPI(apiBuilder);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIMgtConstants.API_LIFECYCLE, user);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(apiBuilder.build());
    }

    @Test(description = "Get API with valid APIID")
    void getApi() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.getAPIbyUUID(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
    }

    @Test(description = "Delete API with zero Subscriptions")
    void deleteApiWithZeroSubscriptions() throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        apiPublisher.deleteAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(lifecycleId);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI(uuid);
    }

    @Test(description = "Error occurred while deleting API with zero subscriptions",
            expectedExceptions = APIMgtDAOException.class)
    void deleteApiWithZeroSubscriptionsException() throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new APIMgtDAOException("Error occurred while deleting the API with id" + uuid)).when(apiDAO)
                .deleteAPI(uuid);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Error occurred while disassociating the API with Lifecycle when deleting the API",
            expectedExceptions = APIMgtDAOException.class)
    void deleteApiWithZeroSubscriptionsLifeCycleException()
            throws APIManagementException, LifecycleException, SQLException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiSubscriptionDAO.getSubscriptionCountByAPI(uuid)).thenReturn(0L);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO, null,
                apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Error occurred while Disassociating the API with Lifecycle id " + uuid))
                .when(apiLifecycleManager).removeLifecycle(lifecycleId);
        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Delete API with Subscriptions", expectedExceptions = ApiDeleteFailureException.class)
    void deleteApiWithSubscriptions() throws LifecycleException, APIManagementException, SQLException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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

    @Test(description = "Test UpdateAPI with Status changed", expectedExceptions = APIManagementException.class)
    void updateAPIWithStatusChanged() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.lifeCycleStatus("PUBLISHED"));
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with context changed", expectedExceptions = APIManagementException.class)
    void updateAPIWithContextChange() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.context("test"));
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with api name changed", expectedExceptions = APIManagementException.class)
    void updateAPIWithNameChange() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.name("testApi"));
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with version changed", expectedExceptions = APIManagementException.class)
    void updateAPIWithVersionChange() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.version("1.1.0"));
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with provider changed", expectedExceptions = APIManagementException.class)
    void updateAPIWithProviderChange() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null, null, apiLifecycleManager, null);
        API.APIBuilder api = SampleTestObjectCreator.createDefaultAPI();
        String uuid = api.getId();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(api.build());
        apiPublisher.updateAPI(api.provider("testProvider"));
        Mockito.verify(apiDAO, Mockito.times(1))
                .updateAPI(uuid, new API.APIBuilder(user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Update api status")
    void updateAPIStatus() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = mock(APISubscriptionDAO.class);
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        Mockito.when(apiDAO.getAPI("xxxxxx")).thenReturn(null);
        apiPublisher.createNewAPIVersion("xxxxxx", "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed",
            expectedExceptions = { LifecycleException.class, APIManagementException.class })
    void CreateNewAPIVersionAndCheckNewApiLifecycleAddFailure() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);

        Mockito.when(apiDAO.getAPI("yyyyy")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.createNewAPIVersion("yyyyy", "2.0.0");
    }

    @Test(description = "Check if api exist with valid uuid")
    void CheckIfAPIExistForValidUuid() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("zzzzz")).thenReturn(SampleTestObjectCreator.getMockApiSummaryObject());
        Assert.assertTrue(apiPublisher.checkIfAPIExists("zzzzz"));
    }

    @Test(description = "Check if api exist with invalid uuid")
    void CheckIfAPIExistForInValidUuid() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("aaaaa")).thenReturn(null);
        Assert.assertFalse(apiPublisher.checkIfAPIExists("aaaaa"));
    }

    @Test(description = "Check if api exist with invalid uuid",
            expectedExceptions = { APIMgtDAOException.class, APIManagementException.class })
    void CheckIfAPIExistWhileGettingJDBCConnectionFailure() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary("bbbbb")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.checkIfAPIExists("bbbbb");
    }

    @Test(description = "Add Documentation Info")
    void addDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentInfo(API_ID, documentInfo);
    }

    @Test(description = "Unable to add documentation info", expectedExceptions = APIMgtDAOException.class)
    void unableToAddDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation")).when(apiDAO)
                .addDocumentInfo(API_ID, documentInfo);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Parse exception when adding documentation info", expectedExceptions = APIMgtDAOException.class)
    void addDocumentationInfoJsonParseException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id("")
                .permission("data").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.addDocumentationInfo(API_ID, documentInfo);
    }

    @Test(description = "Remove Documentation Info")
    void removeDocumentationInfo() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.removeDocumentation(DOC_ID);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteDocument(DOC_ID);
    }

    @Test(description = "Exception when removing Documentation Info", expectedExceptions = APIMgtDAOException.class)
    void removeDocumentationInfoException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation with file")).when(apiDAO)
                .deleteDocument(DOC_ID);
        apiPublisher.removeDocumentation(DOC_ID);
    }

    @Test(description = "Upload Documentation File")
    void uploadDocumentationFile() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.uploadDocumentationFile(DOC_ID, null, "testDoc");
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentFileContent(DOC_ID, null, "testDoc", user);
    }

    @Test(description = "Exception when uploading Documentation File",
            expectedExceptions = APIManagementException.class)
    void uploadDocumentationFileException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation with file")).when(apiDAO)
                .addDocumentFileContent(DOC_ID, null, "testDoc", user);
        apiPublisher.uploadDocumentationFile(DOC_ID, null, "testDoc");
    }

    @Test(description = "Add documentation inline content")
    void addDocumentationContent() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.addDocumentationContent(DOC_ID, "Inline document content");
        Mockito.verify(apiDAO, Mockito.times(1)).addDocumentInlineContent(DOC_ID, "Inline document content", user);
    }

    @Test(description = "Update Documentation Info")
    void updateDocumentation() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
        Mockito.verify(apiDAO, Mockito.times(1)).updateDocumentInfo(API_ID, documentInfo, user);
    }

    @Test(description = "Unable to update documentation info", expectedExceptions = APIMgtDAOException.class)
    void unableToUpdateDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id(DOC_ID)
                .permission("[{\"groupId\": \"testGroup\",\"permission\":[\"READ\",\"UPDATE\",\"DELETE\"]}]").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("Unable to add documentation")).when(apiDAO)
                .updateDocumentInfo(API_ID, documentInfo, user);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Parse exception when updating documentation info",
            expectedExceptions = APIMgtDAOException.class)
    void updateDocumentationInfoJsonParseException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        DocumentInfo documentInfo = new DocumentInfo.Builder().fileName("sample_doc.pdf").name("howto_guide").id("")
                .permission("data").build();
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.updateDocumentation(API_ID, documentInfo);
    }

    @Test(description = "Exception when updating api status", expectedExceptions = APIManagementException.class)
    void updateAPIStatusException() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
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

    @Test(description = "Get lifecycle events list of an API")
    void getLifeCycleEvents() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        List<LifecycleHistoryBean> lifecycleHistoryBeanList = new ArrayList<>();
        LifecycleHistoryBean bean = new LifecycleHistoryBean();
        bean.setPreviousState("CREATED");
        bean.setPostState("DEPRECATED");
        bean.setUser(user);
        lifecycleHistoryBeanList.add(bean);
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doReturn(lifecycleHistoryBeanList).when(apiLifecycleManager).getLifecycleHistory(lifecycleId);
        apiPublisher.getLifeCycleEvents(uuid);
    }

    @Test(description = "Exception finding APISummary Resource when getting lifecycle events list of an API",
            expectedExceptions = APIMgtDAOException.class)
    void getLifeCycleEventsExceptionFindingApiSummaryResource() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        Mockito.when(apiDAO.getAPISummary(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't find APISummary Resource for ID " + API_ID));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.getLifeCycleEvents(API_ID);
    }

    @Test(description = "Exception finding API LifeCycle History when getting lifecycle events list of an API",
            expectedExceptions = APIMgtDAOException.class)
    void getLifeCycleEventsExceptionFindingAPILifeCycleHistory() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Couldn't find APILifecycle History for ID " + uuid))
                .when(apiLifecycleManager).getLifecycleHistory(lifecycleId);
        apiPublisher.getLifeCycleEvents(uuid);
    }

    @Test(description = "Get api lifecycle data")
    void getAPILifeCycleData() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        LifecycleState bean = new LifecycleState();
        bean.setState("CREATED");
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doReturn(bean).when(apiLifecycleManager).getCurrentLifecycleState(lifecycleId);
        apiPublisher.getAPILifeCycleData(uuid);
    }

    @Test(description = "Get api lifecycle data for a null api",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    void getAPILifeCycleDataForNullAPI() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary(API_ID)).thenReturn(null);
        apiPublisher.getAPILifeCycleData(API_ID);
    }

    @Test(description = "Could not retrieve api summary when Getting api lifecycle data",
            expectedExceptions = APIMgtDAOException.class)
    void getAPILifeCycleDataExceptionWhenRetrievingAPISummary() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getAPISummary(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve API Summary for " + API_ID));
        apiPublisher.getAPILifeCycleData(API_ID);
    }

    @Test(description = "Could not retrieve api lifecycle when Getting api lifecycle data",
            expectedExceptions = APIMgtDAOException.class)
    void getAPILifeCycleDataExceptionWhenRetrievingAPILifeCycle() throws APIManagementException, LifecycleException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, apiLifecycleManager, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        String lifecycleId = api.getLifecycleInstanceId();
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(api);
        Mockito.doThrow(new LifecycleException("Couldn't retrieve API Lifecycle for " + uuid)).when(apiLifecycleManager)
                .getCurrentLifecycleState(lifecycleId);
        apiPublisher.getAPILifeCycleData(uuid);
    }

    @Test(description = "Save thumbnail image for API")
    void saveThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        apiPublisher.saveThumbnailImage(API_ID, null, "jpeg");
    }

    @Test(description = "Get thumbnail image for API")
    void getThumbnailImage() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        String uuid = api.getId();
        Mockito.when(apiDAO.getImage(uuid)).thenReturn(null);
        apiPublisher.getThumbnailImage(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getImage(uuid);
    }

    @Test(description = "Exception when getting thumbnail image for API", expectedExceptions = APIMgtDAOException.class)
    void getThumbnailImageException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null, null, null);
        Mockito.when(apiDAO.getImage(API_ID))
                .thenThrow(new APIMgtDAOException("Couldn't retrieve thumbnail for api " + API_ID));
        apiPublisher.getThumbnailImage(API_ID);
    }

    @Test(description = "Retrieving all labels")
    public void getAllLabels() throws APIManagementException {
        LabelDAO labelDAO = mock(LabelDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, null, null, labelDAO);
        Mockito.when(labelDAO.getLabels()).thenReturn(new ArrayList<Label>());
        apiPublisher.getAllLabels();
        Mockito.verify(labelDAO, Mockito.times(1)).getLabels();
    }

    @Test(description = "Exception when retrieving all labels", expectedExceptions = APIManagementException.class)
    void getAllLabelsException() throws APIManagementException {
        LabelDAO labelDAO = mock(LabelDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, null, null, null, labelDAO);
        Mockito.when(labelDAO.getLabels()).thenThrow(new APIMgtDAOException("Error occurred while retrieving labels"));
        apiPublisher.getAllLabels();
    }

    @Test(description = "Update subscription status")
    public void updateSubscriptionStatus() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null);
        apiPublisher.updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1))
                .updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Error when updating subscription status", expectedExceptions = APIManagementException.class)
    public void updateSubscriptionStatusException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("", new Throwable())).when(apiSubscriptionDAO)
                .updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
        apiPublisher.updateSubscriptionStatus(SUB_ID, APIMgtConstants.SubscriptionStatus.ACTIVE);
    }

    @Test(description = "Update subscription policy")
    public void updateSubscriptionPolicy() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null);
        apiPublisher.updateSubscriptionPolicy(SUB_ID, "test policy");
        Mockito.verify(apiSubscriptionDAO, Mockito.times(1)).updateSubscriptionPolicy(SUB_ID, "test policy");
    }

    @Test(description = "Error when updating subscription policy", expectedExceptions = APIManagementException.class)
    public void updateSubscriptionPolicyException() throws APIManagementException {
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, null, null, apiSubscriptionDAO, null, null, null);
        Mockito.doThrow(new APIMgtDAOException("", new Throwable())).when(apiSubscriptionDAO)
                .updateSubscriptionPolicy(SUB_ID, "test policy");
        apiPublisher.updateSubscriptionPolicy(SUB_ID, "test policy");
    }

}