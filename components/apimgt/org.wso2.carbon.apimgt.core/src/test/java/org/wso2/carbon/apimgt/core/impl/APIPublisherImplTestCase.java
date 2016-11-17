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

import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleObjectCreator;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.util.APIConstants;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;

public class APIPublisherImplTestCase {
    public static final String uuid = "7a2298c4-c905-403f-8fac-38c73301631f";
    public static final String user = "admin";

    @Test(description = "Test add api")
    void addApi() throws APIManagementException, SQLException, LifecycleException {
/**
 * this test method verify the API Add with correct API object get invoked correctly
 */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIConstants.API_LIFECYCLE, user)).thenReturn(new
                LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with duplicate context", expectedExceptions = APIManagementException.class)
    void addApiWithDuplicateContext() throws APIManagementException, SQLException, LifecycleException {
        /**
         * This method check by adding duplicate api context
         */
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIConstants.API_LIFECYCLE, user)).thenReturn(new
                LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("/sample/v1")).thenReturn(true);
        Mockito.when(apiDAO.isAPINameExists("Sample")).thenReturn(false);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with duplicate name", expectedExceptions = APIManagementException.class)
    void addApiWithDuplicateName() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIConstants.API_LIFECYCLE, user)).thenReturn(new
                LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("/sample/v1")).thenReturn(false);
        Mockito.when(apiDAO.isAPINameExists("Sample")).thenReturn(true);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIConstants.API_LIFECYCLE, user);
    }

    @Test(description = "Test add api with API Lifecycle failed", expectedExceptions = {LifecycleException.class,
            APIManagementException.class})
    void addAPILifecycleFailure() throws LifecycleException, APIManagementException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle(APIConstants.API_LIFECYCLE, user)).thenThrow(new
                LifecycleException
                ("Couldn't add api lifecycle"));
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle(APIConstants.API_LIFECYCLE, user);
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
    }

    @Test(description = "Get API with valid APIID")
    void getApi() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(new API.APIBuilder(user, "1" +
                ".0.0", "Calculator").build());
        apiPublisher.getAPIbyUUID(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
    }

    @Test(description = "Delete API with zero Subscriptions")
    void deleteApiWithZeroSubscriptions() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionCountByAPI(uuid))
                .thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, apiSubscriptionDAO,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(new API.APIBuilder(user, "1" +
                ".0.0", "Calculator").lifecycleInstanceId(uuid).build());
        apiPublisher.deleteAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI(uuid);
    }

    @Test(description = "Delete API with Subscriptions", expectedExceptions = ApiDeleteFailureException.class)
    void deleteApiWithSubscriptions() throws SQLException, LifecycleException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(SampleObjectCreator
                .getMockAPIObject().lifecycleInstanceId(uuid).build());
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionCountByAPI(uuid))
                .thenReturn(2L);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, apiSubscriptionDAO,
                apiLifecycleManager);

        apiPublisher.deleteAPI(uuid);
    }

    @Test(description = "Test UpdateAPI with Status unchanged")
    void updateAPIWithStatusUnchanged() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.updateAPI(uuid, new API.APIBuilder(user, "Sample",
                "1.0.0").build())).thenReturn(new API.APIBuilder(user, "Sample", "1.0.0").build());
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn
                (SampleObjectCreator.getMockAPIObject().lifeCycleStatus("CREATED").build());
        apiPublisher.updateAPI(SampleObjectCreator.getMockAPIObject().lifeCycleStatus("CREATED").id
                (uuid));
        Mockito.verify(apiDAO,Mockito.times(1)).getAPI(uuid);
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid,
                SampleObjectCreator.getMockAPIObject().lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with Status unchanged", expectedExceptions = APIManagementException.class)
    void updateAPIWithStatusChanged() throws SQLException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.updateAPI(uuid, new API.APIBuilder(user, "Sample",
                "1.0.0").build())).thenReturn(new API.APIBuilder(user, "Sample", "1.0.0").build());
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(new API.APIBuilder(user, "1" +
                ".0.0", "Calculator").lifecycleInstanceId(uuid).lifeCycleStatus
                ("CREATED").build());
        apiPublisher.updateAPI(SampleObjectCreator.getMockAPIObject().lifeCycleStatus("PUBLISH"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI(uuid, new API.APIBuilder
                (user, "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Update api status")
    void updateAPIStatus() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        API mockAPI = new API.APIBuilder(user, "Sample", "1.0.0")
                .lifecycleInstanceId(uuid).id
                        (uuid).build();
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(mockAPI);
        LifecycleState lifecycleState = SampleObjectCreator.getMockLifecycleStateObject();
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                uuid, user, mockAPI)).thenReturn
                (lifecycleState);
        lifecycleState.setState("PUBLISH");
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", new HashMap<>());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).executeLifecycleEvent("PUBLISH",
                uuid, user, mockAPI);
    }

    @Test(description = "Update api status", expectedExceptions = {APIManagementException.class})
    void updateAPIStatusWhileAPINotAvailable() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                uuid, user, SampleObjectCreator.getMockAPIObject().id
                        (uuid).build()))
                .thenReturn
                        (SampleObjectCreator.getMockLifecycleStateObject());
        Mockito.doThrow(new SQLException("Couldn't change the status of api ID ")).when(apiDAO)
                .changeLifeCycleStatus(uuid, "PUBLISH");
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", Collections.emptyMap());
    }


    @Test(description = "Update api status", expectedExceptions = {SQLException.class, APIManagementException.class})
    void updateAPIStatusWhileGettingDBFailure() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiDAO.getAPI(uuid)).thenThrow(new SQLException("Couldn't " +
                "Create connection"));
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                uuid, user, SampleObjectCreator.getMockAPIObject().id
                        (uuid).build()))
                .thenReturn
                        (SampleObjectCreator.getMockLifecycleStateObject());
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.updateAPIStatus(uuid, "PUBLISH", Collections.emptyMap());
    }

    @Test(description = "Create new  API version with valid APIID")
    void CreateNewAPIVersion() throws SQLException, APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(SampleObjectCreator
                .getMockAPIObject().build());
        Mockito.when(apiLifecycleManager.addLifecycle(APIConstants.API_LIFECYCLE, user)).thenReturn(new
                LifecycleState());
        String newUUid = apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI(uuid);
        Assert.assertNotEquals(uuid, newUUid);
    }

    @Test(description = "Create new  API version with invalid APIID", expectedExceptions =
            APIMgtResourceNotFoundException.class)
    void CreateNewAPIVersionWithInvalidUUID() throws SQLException, APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(null);
        apiPublisher.createNewAPIVersion(uuid, "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed", expectedExceptions
            = {LifecycleException.class, APIManagementException.class})
    void CreateNewAPIVersionAndCheckNewApiLifecycleAddFailure() throws SQLException, APIManagementException,
            LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenReturn(SampleObjectCreator
                .getMockAPIObject().build());
        Mockito.when(apiLifecycleManager.addLifecycle(APIConstants.API_LIFECYCLE, user)).thenThrow(new
                LifecycleException(""));
        apiPublisher.createNewAPIVersion(uuid, "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(0)).addAPI(SampleObjectCreator.getMockAPIObject().build());
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed", expectedExceptions
            = {SQLException.class, APIManagementException.class})
    void CreateNewAPIVersionAndGetAPIByUuidFailure() throws SQLException, APIManagementException,
            LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI(uuid)).thenThrow(new SQLException(""));
        apiPublisher.createNewAPIVersion(uuid, "2.0.0");
    }


    @Test(description = "Check if api exist with valid uuid")
    void CheckIfAPIExistForValidUuid() throws SQLException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null);
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(SampleObjectCreator
                .getMockApiSummaryObject());
        Assert.assertTrue(apiPublisher.checkIfAPIExists(uuid));
    }

    @Test(description = "Check if api exist with invalid uuid")
    void CheckIfAPIExistForInValidUuid() throws SQLException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null);
        Mockito.when(apiDAO.getAPISummary(uuid)).thenReturn(null);
        Assert.assertFalse(apiPublisher.checkIfAPIExists(uuid));
    }

    @Test(description = "Check if api exist with invalid uuid", expectedExceptions = {SQLException.class,
            APIManagementException.class})
    void CheckIfAPIExistWhileGettingJDBCConnectionFailure() throws SQLException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl(user, apiDAO, null, null, null);
        Mockito.when(apiDAO.getAPISummary(uuid)).thenThrow(new SQLException(""));
        apiPublisher.checkIfAPIExists(uuid);
    }
}