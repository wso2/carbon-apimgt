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

    private static final String USER_NAME = "username";
    private static final String API_VERSION = "1.0.0";
    private static final String PROVIDER_NAME = "provider";
    private static final String API_NAME = "provider";
    private static final String API_ID = "provider";

    @Test
    public void testSearchAPIByUUID() {
        ApiDAO apiDAO = mock(ApiDAO.class);
        APIStore apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null);
        try {
            API apiFromDAO = new API.APIBuilder(PROVIDER_NAME, API_NAME, API_VERSION).build();
            when(apiDAO.getAPI(API_ID)).thenReturn(apiFromDAO);

            API api = apiStore.getAPIbyUUID(API_ID);
            Assert.assertEquals(api.getName(), API_NAME);
            verify(apiDAO, atLeastOnce()).getAPI(API_ID);
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
