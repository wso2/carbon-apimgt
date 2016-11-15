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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;

import java.io.InputStream;
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
import org.wso2.carbon.apimgt.core.models.APIResults;
import org.wso2.carbon.apimgt.core.models.Application;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
            List<API> apiSummaryList = new ArrayList<API>();
            apiSummaryList.add(new API.APIBuilder("p1", "n1", "v1").build());
            apiSummaryList.add(new API.APIBuilder("p2", "n2", "v2").build());
            apiSummaryList.add(new API.APIBuilder("p3", "n3", "v3").build());
            
            APIResults apimResultsFromDAO = new APIResults.Builder(apiSummaryList, true, 1).build();
            when(apiDAO.searchAPIsForRoles("", 1, 2, new ArrayList<>())).thenReturn(apimResultsFromDAO);
            
            APIResults apis = apiStore.searchAPIs("", 1, 2);
            Assert.assertEquals(apis.getApiSummaryList().size(), 3);
                        
            verify(apiDAO, atLeastOnce()).searchAPIsForRoles("", 1, 2, new ArrayList<>());
            
        } catch (APIManagementException | SQLException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test(expectedExceptions = APIManagementException.class)
    public void searchAPIsWithException() throws Exception {    
            PowerMockito.mockStatic(APIUtils.class); // TODO
            when(apiDAO.searchAPIsForRoles("select *", 1, 2, new ArrayList<>())).thenThrow(SQLException.class);
            //doThrow(new Exception()).when(APIUtils).logAndThrowException(null, null, null)).
            apiStore.searchAPIs("select *", 1, 2);                    
    }
    
    
    @Test
    public void getAPIsByStatus() throws APIManagementException, SQLException {
        
        List<API> apiSummaryList = new ArrayList<API>();
        apiSummaryList.add(new API.APIBuilder("p1", "n1", "v1").build());
        apiSummaryList.add(new API.APIBuilder("p2", "n2", "v2").build());
        apiSummaryList.add(new API.APIBuilder("p3", "n3", "v3").build());
        
        APIResults expectedAPIs = new APIResults.Builder(apiSummaryList, true, 1).build();
        when(apiDAO.getAPIsByStatus(1, 2, Arrays.asList("CREATED", "APUBLISHED"))).thenReturn(expectedAPIs);
        
        APIResults actualAPIs = apiStore.getAllAPIsByStatus(1, 2, new String[] {"CREATED", "APUBLISHED"});
        Assert.assertNotNull(actualAPIs);
        Assert.assertEquals(actualAPIs.getApiSummaryList().size(), expectedAPIs.getApiSummaryList().size());
        verify(apiDAO, times(1)).getAPIsByStatus(1, 2, Arrays.asList("CREATED", "APUBLISHED"));
    }

    @Test
    public void testGetApplicationByName () {
        try {
            Application applicationFromDAO = new Application("username", null);
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

}
