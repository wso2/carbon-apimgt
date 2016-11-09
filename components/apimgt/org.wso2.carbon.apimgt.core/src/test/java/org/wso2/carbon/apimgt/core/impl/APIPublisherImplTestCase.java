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
import org.wso2.carbon.apimgt.core.api.APILifecycleManager;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.impl.LifecycleState;

import java.sql.SQLException;

public class APIPublisherImplTestCase {


    @Test
    void addApi() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiLifecycleManager.addLifecycle("API_LIFECYCLE", "")).thenReturn(new LifecycleState());
        apiPublisher.addAPI(new API.APIBuilder("admin", "Sample", "1.0.0"));
        Mockito.verify(apiDAO, Mockito.times(1)).addAPI(new API.APIBuilder("admin", "Sample", "1.0.0").build());
    }

    @Test
    void getApi() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(new API.APIBuilder("admin", "1" +
                ".0.0", "Calculator").build());
        apiPublisher.getAPIbyUUID("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test
    void deleteApi() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(new API.APIBuilder("admin", "1" +
                ".0.0", "Calculator").lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").build());
        apiPublisher.deleteAPI("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiDAO, Mockito.times(1)).getAPI("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiLifecycleManager, Mockito.times(1)).removeLifecycle("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.verify(apiDAO, Mockito.times(1)).deleteAPI("7a2298c4-c905-403f-8fac-38c73301631f");
    }

    @Test
    void updateAPI() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        Mockito.when(apiDAO.updateAPI("7a2298c4-c905-403f-8fac-38c73301631f", new API.APIBuilder("admin", "Sample",
                "1.0.0").build())).thenReturn(new API.APIBuilder("admin", "Sample", "1.0.0").build());
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(new API.APIBuilder("admin", "1" +
                ".0.0", "Calculator").lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").lifeCycleStatus
                ("CREATED").build());
        apiPublisher.updateAPI(new API.APIBuilder("admin", "Sample", "1.0.0").id
                ("7a2298c4-c905-403f-8fac-38c73301631f").lifeCycleStatus("CREATED"));
        Mockito.verify(apiDAO, Mockito.times(1)).updateAPI("7a2298c4-c905-403f-8fac-38c73301631f", new API.APIBuilder
                ("admin", "Sample", "1.0.0").lifeCycleStatus("CREATED").build());
    }
    @Test
    void updateAPIStatus() throws APIManagementException, SQLException, LifecycleException {
        ApiDAO apiDAO = Mockito.mock(ApiDAO.class);
        APILifecycleManager apiLifecycleManager = Mockito.mock(APILifecycleManager.class);
        APIPublisherImpl apiPublisher = new APIPublisherImpl("", apiDAO, null, null,
                apiLifecycleManager);
        API mockAPI = new API.APIBuilder("admin", "Sample", "1.0.0")
                .lifecycleInstanceId("7a2298c4-c905-403f-8fac-38c73301631f").id("7a2298c4-c905-403f-8fac-38c73301631f").build();
        Mockito.when(apiDAO.getAPI("7a2298c4-c905-403f-8fac-38c73301631f")).thenReturn(mockAPI);
        LifecycleState lifecycleState = new LifecycleState();
        lifecycleState.setLcName("API_LIFECYCLE");
        lifecycleState.setLifecycleId("7a2298c4-c905-403f-8fac-38c73301631f");
        Mockito.when(apiLifecycleManager.executeLifecycleEvent("PUBLISH",
                "7a2298c4-c905-403f-8fac-38c73301631f","",mockAPI)).thenReturn
                (lifecycleState);
        lifecycleState.setState("PUBLISH");

        apiPublisher.updateAPIStatus("7a2298c4-c905-403f-8fac-38c73301631f","PUBLISH",false,false);
        Mockito.verify(apiDAO, Mockito.times(1)).changeLifeCycleStatus("7a2298c4-c905-403f-8fac-38c73301631f",
                "PUBLISH",false,false);
        Mockito.verify(apiLifecycleManager,Mockito.times(1)).executeLifecycleEvent("PUBLISH",
                "7a2298c4-c905-403f-8fac-38c73301631f","",mockAPI);
    }
}