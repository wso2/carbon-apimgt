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

import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
import org.wso2.carbon.apimgt.core.models.APISummary;
import org.wso2.carbon.apimgt.core.models.APISummaryResults;

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
            List<APISummary> apiSummaryList = new ArrayList<APISummary>();
            apiSummaryList.add(new APISummary.Builder("p1", "n1", "v1").build());
            apiSummaryList.add(new APISummary.Builder("p2", "n2", "v2").build());
            apiSummaryList.add(new APISummary.Builder("p3", "n3", "v3").build());
            
            APISummaryResults apimResultsFromDAO = new APISummaryResults.Builder(apiSummaryList, true, 1).build();
            when(apiDAO.searchAPIsForRoles("", 1, 2, new ArrayList<>())).thenReturn(apimResultsFromDAO);
            
            APISummaryResults apis = apiStore.searchAPIs("", 1, 2);
            Assert.assertEquals(apis.getApiSummaryList().size(), 3);
                        
            verify(apiDAO, atLeastOnce()).searchAPIsForRoles("", 1, 2, new ArrayList<>());
            
        } catch (APIManagementException | SQLException e) {
            Assert.fail(e.getMessage());
        }

    }
    
    @Test
    public void testSearchAPIByUUID() {        
        
        try {
            API apiFromDAO = new API.APIBuilder("provider1", "TestAPIByUUID", "1.0.0").build();
            when(apiDAO.getAPI("1234")).thenReturn(apiFromDAO);
            
            API api = apiStore.getAPIbyUUID("1234");
            Assert.assertEquals(api.getName(), "TestAPIByUUID");
            verify(apiDAO, atLeastOnce()).getAPI("1234");
        } catch (APIManagementException | SQLException e) {
            Assert.fail(e.getMessage());            
        }
    }
    
    @Test
    public void testGetDocument() {
        try {
            InputStream docContent = apiStore.getDocumentationContent("");
        } catch (APIManagementException e) {
            Assert.fail(e.getMessage());
        }
    }
    
    @Test
    public void testGetApplicationByUUID () {
        
    }
    
    @Test
    public void testAddApplication () {

    }

}
