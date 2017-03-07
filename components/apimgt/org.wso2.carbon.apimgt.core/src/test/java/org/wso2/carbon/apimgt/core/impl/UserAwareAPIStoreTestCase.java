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

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.Application;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Test class for UserAwareAPIStore
 */
public class UserAwareAPIStoreTestCase {

    private static final String USER_NAME = "username";
    private static final String APP_NAME = "appname";
    public static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";

    @Test
    public void testDeleteApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new UserAwareAPIStore(USER_NAME, null, applicationDAO, null, null, null, null);
        Application applicationFromDAO = new Application(APP_NAME, null);
        applicationFromDAO.setCreatedUser(USER_NAME);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        apiStore.deleteApplication(UUID);
        verify(applicationDAO, times(1)).deleteApplication(UUID);
    }

    @Test
    public void testUpdateApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = new UserAwareAPIStore(USER_NAME, null, applicationDAO, null, null, null, null);
        Application applicationFromDAO = new Application(APP_NAME, null);
        Application newApplication = new Application("NEW_APP", null);
        applicationFromDAO.setCreatedUser(USER_NAME);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        apiStore.updateApplication(UUID,newApplication);
        verify(applicationDAO, times(1)).updateApplication(UUID,newApplication);
    }
}
