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
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleObjectCreator;
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ApiDeleteFailureException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;

public class APIPublisherImplTestCase {


    @Test(description = "Test add api")
    void addApi() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(new LifecycleState());
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle("API_LIFECYCLE", "admin");
    }

    @Test(description = "Test add api with duplicate name", expectedExceptions = APIManagementException.class)
    void addApiWithDuplicateContext() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("/sample/v1")).thenReturn(true);
        Mockito.when(apiDAO.isAPINameExists("Sample")).thenReturn(false);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle("API_LIFECYCLE", "admin");
    }

    @Test(description = "Test add api with duplicate name", expectedExceptions = APIManagementException.class)
    void addApiWithDuplicateName() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(new LifecycleState());
        Mockito.when(apiDAO.isAPIContextExists("/sample/v1")).thenReturn(false);
        Mockito.when(apiDAO.isAPINameExists("Sample")).thenReturn(true);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle("API_LIFECYCLE", "admin");
    }

    @Test(description = "Test add api with API add failed", expectedExceptions = {APIMgtDAOException.class,
            APIManagementException.class})
    void addAPIDaoFailureTest() throws LifecycleException, APIManagementException, APIMgtDAOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(SampleObjectCreator
                .getMockLifecycleStateObject());
        Mockito.doThrow(new APIMgtDAOException("abc")).when(apiDAO).addAPI(SampleObjectCreator.getMockAPIObject().build
                ());
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject().id("7a2298c4-c905-403f-8fac-38c73301631f"));
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle("API_LIFECYCLE", "admin");
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(SampleObjectCreator
                .getMockLifecycleStateObject().getLifecycleId());

    }

    @Test(description = "Test add api with API Lifecycle failed", expectedExceptions = {LifecycleException.class,
            APIManagementException.class})
    void addAPILifecycleFailure() throws LifecycleException, APIManagementException, APIMgtDAOException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenThrow(new LifecycleException
                ("Couldn't add api lifecycle"));
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.addAPI(SampleObjectCreator.getMockAPIObject());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).addLifecycle("API_LIFECYCLE", "admin");
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(SampleObjectCreator.getMockAPIObject().build());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle(SampleObjectCreator
                .getMockLifecycleStateObject().getLifecycleId());

    }

    @Test(description = "Get API with valid APIID")
    void getApi() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(new API.APIBuilder("admin", "1" +
                ".0.0", "Calculator").build());
        apiPublisher.getAPIbyUUID("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test(description = "Delete API with zero Subscriptions")
    void deleteApiWithZeroSubscriptions() throws APIManagementException, APIMgtDAOException, LifecycleException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionCountByAPI("7a2298c4-c905-403f-8fac-38c73301631f"))
                .thenReturn(0L);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, apiSubscriptionDAO,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(new API.APIBuilder("admin", "1" +
                ".0.0", "Calculator").lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").build());
        apiPublisher.deleteAPI("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test(description = "Delete API with Subscriptions", expectedExceptions = ApiDeleteFailureException.class)
    void deleteApiWithSubscriptions() throws APIMgtDAOException, LifecycleException, APIManagementException, SQLException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APISubscriptionDAO apiSubscriptionDAO = Mockito.mock(APISubscriptionDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(SampleObjectCreator
                .getMockAPIObject().lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").build());
        Mockito.when(apiSubscriptionDAO.getAPISubscriptionCountByAPI("7a2298c4-c905-403f-8fac-38c73301631f"))
                .thenReturn(2L);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, apiSubscriptionDAO,
                apiLifecycleManager);

        apiPublisher.deleteAPI("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test(description = "Test UpdateAPI with Status unchanged")
    void updateAPIWithStatusUnchanged() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.updateAPI("7a2298c4-c905-403f-8fac-38c73301631f", new API.APIBuilder("admin", "Sample",
                "1.0.0").build())).thenReturn(new API.APIBuilder("admin", "Sample", "1.0.0").build());
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn
                (SampleObjectCreator.getMockAPIObject().lifeCycleStatus("CREATED").build());
        apiPublisher.updateAPI(SampleObjectCreator.getMockAPIObject().lifeCycleStatus("CREATED").id
                ("7a2298c4-c905-403f-8fac-38c73301631f"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI("7a2298c4-c905-403f-8fac-38c73301631f",
                SampleObjectCreator.getMockAPIObject().lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Test UpdateAPI with Status unchanged", expectedExceptions = APIManagementException.class)
    void updateAPIWithStatusChanged() throws APIMgtDAOException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.updateAPI("7a2298c4-c905-403f-8fac-38c73301631f", new API.APIBuilder("admin", "Sample",
                "1.0.0").build())).thenReturn(new API.APIBuilder("admin", "Sample", "1.0.0").build());
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(new API.APIBuilder("admin", "1" +
                ".0.0", "Calculator").lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").lifeCycleStatus
                ("CREATED").build());
        apiPublisher.updateAPI(SampleObjectCreator.getMockAPIObject().lifeCycleStatus("PUBLISH"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI("7a2298c4-c905-403f-8fac-38c73301631f", new API.APIBuilder
                ("admin", "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }

    @Test(description = "Update api status")
    void updateAPIStatus() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        API mockAPI = new API.APIBuilder("admin", "Sample", "1.0.0")
                .lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").id
                        ("7a2298c4-c905-403f-8fac-38c73301631f").build();
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(mockAPI);
        LifecycleState lifecycleState = SampleObjectCreator.getMockLifecycleStateObject();
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                "7a2298c4-c905-403f-8fac-38c73301631f", "admin", mockAPI)).thenReturn
                (lifecycleState);
        lifecycleState.setState("PUBLISH");
        apiPublisher.updateAPIStatus("7a2298c4-c905-403f-8fac-38c73301631f", "PUBLISH",new HashMap<>());
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).executeLifecycleEvent("PUBLISH",
                "7a2298c4-c905-403f-8fac-38c73301631f", "admin", mockAPI);
    }

    @Test(description = "Update api status", expectedExceptions = {APIManagementException.class})
    void updateAPIStatusWhileAPINotAvailable() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(null);
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                "7a2298c4-c905-403f-8fac-38c73301631f", "admin", SampleObjectCreator.getMockAPIObject().id
                        ("7a2298c4-c905-403f-8fac-38c73301631f").build()))
                .thenReturn
                        (SampleObjectCreator.getMockLifecycleStateObject());
        Mockito.doThrow(new APIMgtDAOException("Couldn't change the status of api ID ")).when(apiDAO)
                .changeLifeCycleStatus("7a2298c4-c905-403f-8fac-38c73301631f", "PUBLISH");
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.updateAPIStatus("7a2298c4-c905-403f-8fac-38c73301631f", "PUBLISH",Collections.emptyMap());
    }


    @Test(description = "Update api status", expectedExceptions = {APIMgtDAOException.class,APIManagementException.class})
    void updateAPIStatusWhileGettingDBFailure() throws APIManagementException, APIMgtDAOException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenThrow(new APIMgtDAOException("Couldn't " +
                "Create connection"));
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                "7a2298c4-c905-403f-8fac-38c73301631f", "admin", SampleObjectCreator.getMockAPIObject().id
                        ("7a2298c4-c905-403f-8fac-38c73301631f").build()))
                .thenReturn
                        (SampleObjectCreator.getMockLifecycleStateObject());
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        apiPublisher.updateAPIStatus("7a2298c4-c905-403f-8fac-38c73301631f", "PUBLISH",Collections.emptyMap());
    }

    @Test(description = "Create new  API version with valid APIID")
    void CreateNewAPIVersion() throws APIMgtDAOException, APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(SampleObjectCreator
                .getMockAPIObject().build());
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(new LifecycleState());
        apiPublisher.createNewAPIVersion("7a2298c4-c905-403f-8fac-38c73301631f", "2.0.0");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test(description = "Create new  API version with invalid APIID", expectedExceptions =
            APIMgtResourceNotFoundException.class)
    void CreateNewAPIVersionWithInvalidUUID() throws APIMgtDAOException, APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(null);
        apiPublisher.createNewAPIVersion("7a2298c4-c905-403f-8fac-38c73301631f", "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API add get failed", expectedExceptions
            = {APIMgtDAOException.class, APIManagementException.class})
    void CreateNewAPIVersionAndCheckNewAPIAddFailure() throws APIMgtDAOException, APIManagementException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(SampleObjectCreator
                .getMockAPIObject().build());
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(SampleObjectCreator
                .getMockLifecycleStateObject());
        Mockito.doThrow(new APIMgtDAOException("Connection failure")).when(apiDAO).addAPI(SampleObjectCreator
                .getMockAPIObject().version("2.0.0").build());
        apiPublisher.createNewAPIVersion("7a2298c4-c905-403f-8fac-38c73301631f", "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed", expectedExceptions
            = {LifecycleException.class, APIManagementException.class})
    void CreateNewAPIVersionAndCheckNewApiLifecycleAddFailure() throws APIMgtDAOException, APIManagementException,
            LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(SampleObjectCreator
                .getMockAPIObject().build());
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenThrow(new LifecycleException(""));
        apiPublisher.createNewAPIVersion("7a2298c4-c905-403f-8fac-38c73301631f", "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed", expectedExceptions
            = {APIMgtDAOException.class, APIManagementException.class})
    void CreateNewAPIVersionAndGetAPIByUuidFailure() throws APIMgtDAOException, APIManagementException,
            LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.createNewAPIVersion("7a2298c4-c905-403f-8fac-38c73301631f", "2.0.0");
    }

    @Test(description = "Create new  API version with APIID and new API lifecycle add get failed", expectedExceptions
            = {APIManagementException.class})
    void CreateNewAPIVersionAndCheckNewApiAddfailureWithDisassociationFailure() throws APIMgtDAOException,
            APIManagementException,
            LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(SampleObjectCreator
                .getMockAPIObject().build());
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "admin")).thenReturn(SampleObjectCreator
                .getMockLifecycleStateObject());
        Mockito.doThrow(new APIMgtDAOException("Connection failure")).when(apiDAO).addAPI(SampleObjectCreator
                .getMockAPIObject().version("2.0.0").build());
        Mockito.doThrow(new LifecycleException("")).when(apiLifecycleManager).removeLifecycle
                ("7a2298c4-c905-403f-8fac-38c73301631f");
        apiPublisher.createNewAPIVersion("7a2298c4-c905-403f-8fac-38c73301631f", "2.0.0");
    }

    @Test(description = "Check if api exist with valid uuid")
    void CheckIfAPIExistForValidUuid() throws APIMgtDAOException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, null);
        Mockito.when(apiDAO.getAPISummary("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(SampleObjectCreator
                .getMockApiSummaryObject());
        apiPublisher.checkIfAPIExists("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test(description = "Check if api exist with invalid uuid")
    void CheckIfAPIExistForInValidUuid() throws APIMgtDAOException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, null);
        Mockito.when(apiDAO.getAPISummary("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(null);
        apiPublisher.checkIfAPIExists("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test(description = "Check if api exist with invalid uuid", expectedExceptions = {APIMgtDAOException.class,
            APIManagementException.class})
    void CheckIfAPIExistWhileGettingJDBCConnectionFailure() throws APIMgtDAOException, APIManagementException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("admin", apiDAO, null, null, null);
        Mockito.when(apiDAO.getAPISummary("7a2298c4-c905-403f-8fac-38c73301631f")).thenThrow(new APIMgtDAOException(""));
        apiPublisher.checkIfAPIExists("7a2298c4-c905-403f-8fac-38c73301631f");
    }
}