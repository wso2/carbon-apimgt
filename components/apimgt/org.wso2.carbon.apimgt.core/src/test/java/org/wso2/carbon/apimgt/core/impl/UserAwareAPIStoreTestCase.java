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

import org.mockito.Mockito;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.WorkflowDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.core.models.Application;
import org.wso2.carbon.apimgt.core.models.WorkflowConfig;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ApplicationStatus;
import org.wso2.carbon.apimgt.core.workflow.WorkflowExtensionsConfigBuilder;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;

import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Test class for UserAwareAPIStore
 */
public class UserAwareAPIStoreTestCase {

    private static final String USER_NAME = "username";
    private static final String ANONYMOUS_USER = "anonymous";
    private static final String APP_NAME = "appname";
    public static final String UUID = "7a2298c4-c905-403f-8fac-38c73301631f";

    @BeforeTest
    public void setup() throws Exception {
        WorkflowExtensionsConfigBuilder.build(new ConfigProvider() {

            @Override
            public <T> T getConfigurationObject(Class<T> configClass) throws CarbonConfigurationException {
                T workflowConfig = (T) new WorkflowConfig();
                return workflowConfig;
            }

            @Override
            public Map getConfigurationMap(String namespace) throws CarbonConfigurationException {
                // TODO Auto-generated method stub
                return null;
            }
        });

        ConfigProvider configProvider = Mockito.mock(ConfigProvider.class);
        ServiceReferenceHolder.getInstance().setConfigProvider(configProvider);
    }
    @Test(description = "Delete application")
    public void testDeleteApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APISubscriptionDAO subscriptionDAO = mock(APISubscriptionDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIGateway apiGateway = mock(APIGateway.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO, subscriptionDAO, workflowDAO, apiGateway);
        Application applicationFromDAO = SampleTestObjectCreator.createDefaultApplication();
        applicationFromDAO.setId(UUID);
        applicationFromDAO.setCreatedUser(USER_NAME);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        apiStore.deleteApplication(UUID);
        verify(applicationDAO, times(1)).deleteApplication(UUID);
    }

    @Test(description = "Try delete null application", expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testDeleteApplicationNull() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIGateway apiGateway = mock(APIGateway.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO, apiGateway);
        apiStore.deleteApplication(UUID);
    }

    @Test(description = "Try delete application by anonymous or different user",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testDeleteApplicationAnonymousUser() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, null);
        applicationFromDAO.setCreatedUser(ANONYMOUS_USER);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        apiStore.deleteApplication(UUID);
    }

    @Test(description = "Update application")
    public void testUpdateApplication() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        WorkflowDAO workflowDAO = mock(WorkflowDAO.class);
        APIGateway apiGateway = mock(APIGateway.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO, workflowDAO, apiGateway);
        Application applicationFromDAO = new Application(APP_NAME, null);
        applicationFromDAO.setId(UUID);
        applicationFromDAO.setCreatedUser(USER_NAME);
        applicationFromDAO.setStatus(ApplicationStatus.APPLICATION_APPROVED);        
        //updated app
        Application newApplication = applicationFromDAO;
        newApplication.setDescription("update description");  
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        apiStore.updateApplication(UUID, newApplication);
        verify(applicationDAO, times(1)).updateApplication(UUID, newApplication);
    }

    @Test(description = "Try update null application", expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testUpdateApplicationNull() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO);
        apiStore.updateApplication(UUID, null);
    }

    @Test(description = "Try update application by anonymous or different user",
            expectedExceptions = APIMgtResourceNotFoundException.class)
    public void testUpdateApplicationAnonymousUser() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, null);
        applicationFromDAO.setCreatedUser(ANONYMOUS_USER);
        Application newApplication = new Application("NEW_APP", null);
        when(applicationDAO.getApplication(UUID)).thenReturn(applicationFromDAO);
        apiStore.updateApplication(UUID, newApplication);
    }

    /**
     * Tests for exceptions
     */

    @Test(description = "Exception when deleting application", expectedExceptions = APIMgtDAOException.class)
    public void testDeleteApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, null);
        applicationFromDAO.setCreatedUser(USER_NAME);
        when(applicationDAO.getApplication(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while deleting application - " + UUID));
        apiStore.deleteApplication(UUID);
    }

    @Test(description = "Exception when updating application", expectedExceptions = APIMgtDAOException.class)
    public void testUpdateApplicationException() throws APIManagementException {
        ApplicationDAO applicationDAO = mock(ApplicationDAO.class);
        APIStore apiStore = getUserAwareAPIStore(applicationDAO);
        Application applicationFromDAO = new Application(APP_NAME, null);
        Application newApplication = new Application("NEW_APP", null);
        applicationFromDAO.setCreatedUser(USER_NAME);
        when(applicationDAO.getApplication(UUID))
                .thenThrow(new APIMgtDAOException("Error occurred while updating application - " + UUID));
        apiStore.updateApplication(UUID, newApplication);
    }

    private UserAwareAPIStore getUserAwareAPIStore(ApplicationDAO applicationDAO, APISubscriptionDAO
            apiSubscriptionDAO, WorkflowDAO workflowDAO, APIGateway apiGateway) {
        return new UserAwareAPIStore(USER_NAME, null, null, applicationDAO, apiSubscriptionDAO, null, null, null,
                workflowDAO, null, apiGateway);
    }

    private UserAwareAPIStore getUserAwareAPIStore(ApplicationDAO applicationDAO, WorkflowDAO workflowDAO, APIGateway
            apiGateway) {
        return new UserAwareAPIStore(USER_NAME, null, null, applicationDAO, null, null, null, null,
                workflowDAO, null, apiGateway);
    }
    private UserAwareAPIStore getUserAwareAPIStore(ApplicationDAO applicationDAO, APIGateway apiGateway) {
        return new UserAwareAPIStore(USER_NAME, null, null, applicationDAO, null, null, null, null,
                null, null, apiGateway);
    }
    private UserAwareAPIStore getUserAwareAPIStore(ApplicationDAO applicationDAO) {
        return new UserAwareAPIStore(USER_NAME, null, null, applicationDAO, null, null, null, null,
                null, null, null);
    }
}
