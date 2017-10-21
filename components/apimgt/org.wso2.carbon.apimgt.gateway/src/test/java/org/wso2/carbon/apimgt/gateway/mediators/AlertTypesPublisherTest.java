/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.apimgt.gateway.mediators;

import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;

import java.sql.SQLException;


public class AlertTypesPublisherTest {
    @Test
    public void saveAndPublishAlertTypesEvent() throws Exception {
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = false;
        alertTypesPublisher.publisher = apiMgtUsageDataPublisher;
        alertTypesPublisher.saveAndPublishAlertTypesEvent("abc", "abc@de.com", "admin", "subscriber", "aa");
    }

    @Test
    public void saveAndPublishAlertTypesEventFromInitialization() throws Exception {
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO, apiMgtUsageDataPublisher);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = false;
        alertTypesPublisher.saveAndPublishAlertTypesEvent("abc", "abc@de.com", "admin", "publisher", "aa");
    }

    @Test
    public void saveAlertFromAdminDashboard() throws Exception {
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO, apiMgtUsageDataPublisher);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = false;
        alertTypesPublisher.saveAndPublishAlertTypesEvent("abc", "abc@de.com", "admin", "admin-dashboard", "aa");
    }

    @Test(expected = APIManagementException.class)
    public void saveAlertWhileDatabaseConnectionFailed() throws Exception {
        String checkedAlertList = "health-availability";
        String emailList = "abc@de.com";
        String userName = "admin@carbon.super";
        String agent = "publisher";
        String checkedAlertListValues = "true";
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.doThrow(SQLException.class).when(apiMgtDAO).addAlertTypesConfigInfo(userName, emailList,
                checkedAlertList, agent);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO, apiMgtUsageDataPublisher);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = false;
        alertTypesPublisher.saveAndPublishAlertTypesEvent(checkedAlertList, emailList, userName, agent,
                checkedAlertListValues);
    }

    @Test(expected = APIManagementException.class)
    public void saveAlertWhileEventReceiverSkipped() throws Exception {
        String checkedAlertList = "health-availability";
        String emailList = "abc@de.com";
        String userName = "admin@carbon.super";
        String agent = "publisher";
        String checkedAlertListValues = "true";
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.doThrow(SQLException.class).when(apiMgtDAO).addAlertTypesConfigInfo(userName, emailList,
                checkedAlertList, agent);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO, apiMgtUsageDataPublisher);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = true;
        alertTypesPublisher.saveAndPublishAlertTypesEvent(checkedAlertList, emailList, userName, agent,
                checkedAlertListValues);
    }

    @Test
    public void unSubscribe() throws Exception {
        String userName = "admin@carbon.super";
        String agent = "publisher";
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.doNothing().when(apiMgtDAO).unSubscribeAlerts(userName, agent);
        Mockito.doNothing().when(apiMgtDAO).unSubscribeAlerts(userName, "subscriber");
        Mockito.doNothing().when(apiMgtDAO).unSubscribeAlerts(userName, "admin-dashboard");
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO, apiMgtUsageDataPublisher);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = false;
        alertTypesPublisher.unSubscribe(userName, agent);
        alertTypesPublisher.unSubscribe(userName, "subscriber");
        alertTypesPublisher.unSubscribe(userName, "admin-dashboard");
    }

    @Test(expected = APIManagementException.class)
    public void unSubscribeWhileDatabaseFailure() throws Exception {
        String userName = "admin@carbon.super";
        String agent = "publisher";
        ApiMgtDAO apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        Mockito.doThrow(SQLException.class).when(apiMgtDAO).unSubscribeAlerts(userName, agent);
        APIMgtUsageDataPublisher apiMgtUsageDataPublisher = Mockito.mock(APIMgtUsageDataPublisher.class);
        AlertTypesPublisher alertTypesPublisher = new AlertTypePublisherWrapper(apiMgtDAO, apiMgtUsageDataPublisher);
        alertTypesPublisher.enabled = true;
        alertTypesPublisher.skipEventReceiverConnection = false;
        alertTypesPublisher.unSubscribe(userName, agent);
    }


}