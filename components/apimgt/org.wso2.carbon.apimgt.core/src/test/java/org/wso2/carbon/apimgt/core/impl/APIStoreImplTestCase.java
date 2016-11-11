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

/**
 * Test class for APIStore
 *
 */
@PrepareForTest
public class APIStoreImplTestCase {

    private ApiDAO apiDAO = mock(ApiDAO.class);
    private ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
    private APISubscriptionDAO subscriptionDAO = mock(APISubscriptionDAO.class);

    private APIStore apiStore = new APIStoreImpl("username", apiDAO, applicationDAO, subscriptionDAO);

    @Test
    public void testSearchAPIs() {
        try {
            //PowerMockito.mockStatic(DAOFactory.class);
            // apiDAO.searchAPIsForRoles(searchString, offset, limit, roles);
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

}
