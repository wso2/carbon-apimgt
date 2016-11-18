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

import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.util.APIUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test class for APIStore
 *
 */
@PrepareForTest(APIUtils.class)
public class APIStoreImplTestCase {

    private static final String USER_NAME = "username";
    private static final String APP_NAME = "appname";
    private static final String USER_ID = "userid";
    private static final String GROUP_ID = "groupdid";
    private static final String STATUS_CREATED = "CREATED";
    private static final String STATUS_PUBLISHED = "PUBLISHED";

    @Test(description = "Search APIs")
    public void searchAPIs() {
        try {
            ApiDAO apiDAO = mock(ApiDAO.class);
            APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null);
            List<API> apimResultsFromDAO = new ArrayList<>();
            when(apiDAO.searchAPIs("")).thenReturn(apimResultsFromDAO);
            List<API> apis = apiStore.searchAPIs("", 1, 2);
            Assert.assertNotNull(apis);
            verify(apiDAO, atLeastOnce()).searchAPIs("");
        } catch (APIManagementException | APIMgtDAOException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test(description = "Search API", expectedExceptions = APIManagementException.class)
    public void searchAPIsWithException() throws Exception {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null);
        PowerMockito.mockStatic(APIUtils.class); // TODO
        when(apiDAO.searchAPIs("select *")).thenThrow(APIMgtDAOException.class);
        //doThrow(new Exception()).when(APIUtils).logAndThrowException(null, null, null)).
        apiStore.searchAPIs("select *", 1, 2);
    }
    
    
    @Test(description = "Retrieve an API by status")
    public void getAPIsByStatus() throws APIManagementException, APIMgtDAOException {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null);
        List<API> expectedAPIs = new ArrayList<API>();
        when(apiDAO.getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED ))).thenReturn(expectedAPIs);

        List<API> actualAPIs = apiStore.getAllAPIsByStatus(1, 2, new String[] { STATUS_CREATED, STATUS_PUBLISHED });
        Assert.assertNotNull(actualAPIs);
        verify(apiDAO, times(1)).getAPIsByStatus(Arrays.asList(STATUS_CREATED, STATUS_PUBLISHED ));
    }

    @Test(description = "Retrieve an application by name")
    public void testGetApplicationByName () {
        try {
            ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
            APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null);
            Application applicationFromDAO = new Application(APP_NAME, null);
            when(applicationDAO.getApplicationByName(USER_ID, APP_NAME, GROUP_ID))
                    .thenReturn(applicationFromDAO);

            Application application = apiStore.getApplicationsByName(USER_ID, APP_NAME, GROUP_ID);
            Assert.assertNotNull(application);
            verify(applicationDAO, times(1)).getApplicationByName(USER_ID, APP_NAME, GROUP_ID);
        } catch (APIManagementException | SQLException e) {
            Assert.assertTrue(false);
        }
    }


//    @Test(description = "Add an application")
    public void testAddApplication(){
        try {
            ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
            APIStore apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null);
            Application application = new Application(APP_NAME, USER_NAME);
            when(applicationDAO.isApplicationNameExists(APP_NAME)).thenReturn(false);
            String applicationUuid = apiStore.addApplication(application);
            Assert.assertNotNull(applicationUuid);
            verify(applicationDAO, times(1)).addApplication(application);
        } catch (APIManagementException | SQLException e) {
            Assert.assertTrue(false);
        }
    }

}
