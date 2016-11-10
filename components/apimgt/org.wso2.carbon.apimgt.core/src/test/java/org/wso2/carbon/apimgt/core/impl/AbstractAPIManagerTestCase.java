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
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;

import java.io.InputStream;
import java.sql.SQLException;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractAPIManagerTestCase {
    private ApiDAO apiDAO = mock(ApiDAO.class);
    private ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
    private APISubscriptionDAO subscriptionDAO = mock(APISubscriptionDAO.class);

    private APIStore apiStore = new APIStoreImpl("username", apiDAO, applicationDAO, subscriptionDAO);

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

//    @Test
//    public void testGetDocument() {
//        try {
//            InputStream docContent = apiStore.getDocumentationContent("");
//        } catch (APIManagementException e) {
//            Assert.fail(e.getMessage());
//        }
//    }

}
