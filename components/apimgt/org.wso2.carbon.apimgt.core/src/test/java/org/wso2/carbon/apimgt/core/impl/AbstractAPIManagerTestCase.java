/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.core.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.DocumentInfo;

import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractAPIManagerTestCase {

    private static final String USER_NAME = "username";
    private static final String API_VERSION = "1.0.0";
    private static final String PROVIDER_NAME = "provider";
    private static final String API_NAME = "api_name";
    private static final String APP_NAME = "app_name";
    public static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";

    @Test
    public void testSearchAPIByUUID() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null, null,
                null);
        API apiFromDAO = new API.APIBuilder(PROVIDER_NAME, API_NAME, API_VERSION).build();
        when(apiDAO.getAPI(UUID)).thenReturn(apiFromDAO);
        API api = apiStore.getAPIbyUUID(UUID);
        Assert.assertEquals(api.getName(), API_NAME);
        verify(apiDAO, atLeastOnce()).getAPI(UUID);
    }

    @Test(description = "Retrieve an application by uuid")
    public void testGetApplicationByUuid() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null,
                null, null);
        Application applicationFromDAO = new Application(APP_NAME, USER_NAME);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        Application application = apiStore.getApplication(UUID, USER_NAME, null);
        Assert.assertNotNull(application);
        verify(applicationDAO, times(1)).getApplication(UUID);
    }

    @Test(description = "Retrieve documentation summary given the id")
    public void testGetDocumentationSummary() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null, null,
                null);
        DocumentInfo documentInfoMock = SampleTestObjectCreator.getMockDocumentInfoObject(UUID);
        when(apiDAO.getDocumentInfo(UUID)).thenReturn(documentInfoMock);
        DocumentInfo documentInfo = apiStore.getDocumentationSummary(UUID);
        Assert.assertNotNull(documentInfo);
        verify(apiDAO, times(1)).getDocumentInfo(UUID);
    }

    @Test(description = "Retrieve list of documentations")
    public void testAllDocumentation() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null, null,
                null);
        List<DocumentInfo> documentInfoMockList = SampleTestObjectCreator.getMockDocumentInfoObjectsList();
        when(apiDAO.getDocumentsInfoList(UUID)).thenReturn(documentInfoMockList);
        List<DocumentInfo> documentInfoList = apiStore.getAllDocumentation(UUID, 1, 10);
        Assert.assertNotNull(documentInfoList);
        verify(apiDAO, times(1)).getDocumentsInfoList(UUID);
    }

    /**
     * Test cases for exceptions
     */

    @Test(description = "Exception when retrieving an application by uuid",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetApplicationByUuidException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null, null, null, null,
                null, null);
        doThrow(new APIMgtDAOException("Error occurred while retrieving application")).when(applicationDAO)
                .getApplication(UUID);
        apiStore.getApplication(UUID, USER_NAME, null);
    }

    @Test(description = "Exception when retrieving documentation summary given the id",
            expectedExceptions = APIMgtDAOException.class)
    public void testGetDocumentationSummaryException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null, null,
                null);
        when(apiDAO.getDocumentInfo(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving documents"));
        apiStore.getDocumentationSummary(UUID);
    }

    @Test(description = "Exception when retrieving list of documentations",
            expectedExceptions = APIMgtDAOException.class)
    public void testAllDocumentationException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null, null,
                null);
        when(apiDAO.getDocumentsInfoList(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving documents"));
        apiStore.getAllDocumentation(UUID, 1, 10);
    }

    @Test(description = "Exception when getting API by UUID", expectedExceptions = APIMgtDAOException.class)
    public void testSearchAPIByUUIDException() throws APIManagementException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null, null, null, null, null,
                null);
        when(apiDAO.getAPI(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while retrieving API with id " + UUID));
        apiStore.getAPIbyUUID(UUID);
    }

}
