/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.api.WorkflowResponse;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.impl.APIStoreImpl;
import org.wso2.carbon.apimgt.core.models.Application;

import java.util.UUID;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DAOFactory.class, Application.class})
public class ApplicationImportExportManagerTestCase {

    private static final Logger log = LoggerFactory.getLogger(ApplicationImportExportManagerTestCase.class);
    APIStore apiStore = Mockito.mock(APIStoreImpl.class);
    private String USER = "admin";
    ApplicationImportExportManager applicationImportExportManager = new ApplicationImportExportManager(apiStore);

    @Test
    public void testGetApplicationDetails() throws Exception {
        printTestMethodName();
        Application testApp = Mockito.mock(Application.class);
        String appId = UUID.randomUUID().toString();
        Mockito.when(apiStore.getApplication(appId, USER)).thenReturn(testApp);
        testApp = applicationImportExportManager.getApplicationDetails(appId, USER);
        Assert.assertNotNull(testApp);
    }

    @Test
    public void testGetApplicationDetailsNotFound() throws Exception {
        printTestMethodName();
        Application testApp = Mockito.mock(Application.class);
        Mockito.when(apiStore.getApplication("", USER)).thenReturn(null);
        testApp = applicationImportExportManager.getApplicationDetails("", USER);
        Assert.assertEquals(testApp, null);
    }

    @Test
    public void testUpdateApplication() throws Exception {
        printTestMethodName();
        Application testApp = Mockito.mock(Application.class);
        PowerMockito.mockStatic(DAOFactory.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PowerMockito.when(DAOFactory.getApplicationDAO()).thenReturn(applicationDAO);
        PowerMockito.when(applicationDAO.isApplicationNameExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiStore.getApplicationByName(testApp.getName(), USER)).thenReturn(testApp);
        WorkflowResponse workflowResponse = Mockito.mock(WorkflowResponse.class);
        Mockito.when(apiStore.updateApplication(testApp.getUuid(), testApp)).thenReturn(workflowResponse);
        Mockito.when(apiStore.getApplication(testApp.getUuid(), USER)).thenReturn(testApp);
        testApp = applicationImportExportManager.updateApplication(testApp, USER);
        Assert.assertNotNull(testApp);
    }

    @Test(expected = APIManagementException.class)
    public void testUpdateApplicationError() throws Exception {
        printTestMethodName();
        Application testApp = Mockito.mock(Application.class);
        PowerMockito.mockStatic(DAOFactory.class);
        ApplicationDAO applicationDAO = Mockito.mock(ApplicationDAO.class);
        PowerMockito.when(DAOFactory.getApplicationDAO()).thenReturn(applicationDAO);
        PowerMockito.when(applicationDAO.isApplicationNameExists(Mockito.anyString())).thenReturn(true);
        Mockito.when(apiStore.getApplicationByName(testApp.getName(), USER)).thenReturn(testApp);
        WorkflowResponse workflowResponse = Mockito.mock(WorkflowResponse.class);
        Mockito.when(apiStore.updateApplication(testApp.getUuid(), testApp)).thenReturn(workflowResponse);
        Mockito.when(apiStore.getApplication(testApp.getUuid(), USER)).thenThrow
                (new APIMgtDAOException("Error occurred while finding application matching the provided name"));
        applicationImportExportManager.updateApplication(testApp, USER);
    }

    private static void printTestMethodName() {
        log.info("------------------ Test method: " + Thread.currentThread().getStackTrace()[2].getMethodName() +
                " ------------------");
    }
}