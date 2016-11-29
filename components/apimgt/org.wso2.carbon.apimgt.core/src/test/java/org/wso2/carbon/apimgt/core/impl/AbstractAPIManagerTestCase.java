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
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractAPIManagerTestCase {

    private static final String USER_NAME = "username";
    private static final String API_VERSION = "1.0.0";
    private static final String PROVIDER_NAME = "provider";
    private static final String API_NAME = "provider";
    private static final String API_ID = "provider";
    private static final String APP_NAME = "appname";
    public static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";

    @Test public void testSearchAPIByUUID() {
        ApiDAO apiDAO = mock(ApiDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, apiDAO, null, null, null);
        API apiFromDAO = new API.APIBuilder(PROVIDER_NAME, API_NAME, API_VERSION).buildApi();
        try {
            when(apiDAO.getAPI(API_ID)).thenReturn(apiFromDAO);
            API api = apiStore.getAPIbyUUID(API_ID);
            Assert.assertEquals(api.getName(), API_NAME);
            verify(apiDAO, atLeastOnce()).getAPI(API_ID);
        } catch (APIManagementException  e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test(description = "Retrieve an application by uuid")
    public void testGetApplicationByUuid() {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        AbstractAPIManager apiStore = new APIStoreImpl(USER_NAME, null, applicationDAO, null, null);
        Application applicationFromDAO = new Application(APP_NAME, USER_NAME);
        try {
            when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
            Application application = apiStore.getApplication(UUID, USER_NAME, null);

            Assert.assertNotNull(application);
            verify(applicationDAO, times(1)).getApplication(UUID);
        } catch (APIManagementException e) {
            Assert.assertTrue(false);
        }
    }

}
