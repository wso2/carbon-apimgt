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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.powermock.api.mockito.PowerMockito;
//import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

//import org.powermock.api.mockito.PowerMockito;
import org.wso2.carbon.apimgt.core.util.APIUtils;

/**
 * Test class for APIStore
 *
 */
@PrepareForTest(APIUtils.class)
public class APIStoreImplTestCase {

    private ApiDAO apiDAO = mock(ApiDAO.class);
    private ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
    private APISubscriptionDAO subscriptionDAO = mock(APISubscriptionDAO.class);

    private APIStore apiStore = new APIStoreImpl("username", apiDAO, applicationDAO, subscriptionDAO);

    @Test
    public void searchAPIs() {
        try {
            List<API> apimResultsFromDAO = new ArrayList<>();
            when(apiDAO.searchAPIs("")).thenReturn(apimResultsFromDAO);

            List<API> apis = apiStore.searchAPIs("", 1, 2);
            Assert.assertNotNull(apis);
                        
            verify(apiDAO, atLeastOnce()).searchAPIs("");
            
        } catch (APIManagementException | SQLException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test(expectedExceptions = APIManagementException.class)
    public void searchAPIsWithException() throws Exception {    
            PowerMockito.mockStatic(APIUtils.class); // TODO
            when(apiDAO.searchAPIs("select *")).thenThrow(SQLException.class);
            //doThrow(new Exception()).when(APIUtils).logAndThrowException(null, null, null)).
            apiStore.searchAPIs("select *", 1, 2);                    
    }
    
    
    @Test
    public void getAPIsByStatus() throws APIManagementException, SQLException {
        List<API> expectedAPIs = new ArrayList<API>();
        when(apiDAO.getAPIsByStatus(Arrays.asList("CREATED", "APUBLISHED"))).thenReturn(expectedAPIs);

        List<API> actualAPIs = apiStore.getAllAPIsByStatus(1, 2, new String[] {"CREATED", "APUBLISHED"});
        Assert.assertNotNull(actualAPIs);
        verify(apiDAO, times(1)).getAPIsByStatus(Arrays.asList("CREATED", "APUBLISHED"));
    }

    @Test
    public void testGetApplicationByName () {
        try {
            Application applicationFromDAO = new Application("appName", null);
            when(applicationDAO.getApplicationByName("userId","applicationName","groupId")).thenReturn(applicationFromDAO);

            Application application = apiStore.getApplicationsByName("userId","applicationName","groupId");
            Assert.assertNotNull(application);
            verify(applicationDAO, times(1)).getApplicationByName("userId","applicationName","groupId");
        } catch (APIManagementException |SQLException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testIsApplicationExists(){
        try {
            when(applicationDAO.isApplicationExists("applicationName","userId","groupId")).thenReturn(true);

            boolean isApplicationExists = apiStore.isApplicationExists("applicationName","userId","groupId");
            Assert.assertTrue(isApplicationExists);
            verify(applicationDAO, times(1)).isApplicationExists("applicationName","userId","groupId");
        } catch (APIManagementException |SQLException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void testAddApplication(){
        try {
            Application application = new Application("appName", null);
            when(apiStore.isApplicationExists("appName",null,null)).thenReturn(false);
            when(applicationDAO.addApplication(application)).thenReturn("1");

            String applicationUuid = apiStore.addApplication(application);
            Assert.assertNotNull(applicationUuid);
            verify(applicationDAO, times(1)).addApplication(application);
        } catch (APIManagementException |SQLException e) {
            Assert.assertTrue(false);
        }
    }

}
