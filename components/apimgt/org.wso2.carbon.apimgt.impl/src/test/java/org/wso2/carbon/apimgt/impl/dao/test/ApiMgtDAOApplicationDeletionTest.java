/*
 * Copyright (c) 2024, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.dao.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for ApiMgtDAO.deleteApplication() focusing on key manager interaction
 * during application deletion. These tests specifically target the scenario described
 * in issue #4866 where deleting an application with generated keys causes an ERROR log
 * due to a race condition between OAuth app deletion and service provider cleanup.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyManagerHolder.class, APIMgtDBUtil.class, ApiMgtDAO.class,
        ServiceReferenceHolder.class, APIUtil.class})
public class ApiMgtDAOApplicationDeletionTest {

    private ApiMgtDAO apiMgtDAO;
    private KeyManager keyManager;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @Before
    public void setUp() throws Exception {
        keyManager = Mockito.mock(KeyManager.class);

        // Mock ServiceReferenceHolder and APIManagerConfiguration (needed by ApiMgtDAO constructor)
        PowerMockito.mockStatic(ServiceReferenceHolder.class);
        ServiceReferenceHolder serviceReferenceHolder = Mockito.mock(ServiceReferenceHolder.class);
        PowerMockito.when(ServiceReferenceHolder.getInstance()).thenReturn(serviceReferenceHolder);
        APIManagerConfigurationService configService = Mockito.mock(APIManagerConfigurationService.class);
        APIManagerConfiguration config = Mockito.mock(APIManagerConfiguration.class);
        Mockito.when(serviceReferenceHolder.getAPIManagerConfigurationService()).thenReturn(configService);
        Mockito.when(configService.getAPIManagerConfiguration()).thenReturn(config);

        // Mock APIUtil (needed by ApiMgtDAO constructor)
        PowerMockito.mockStatic(APIUtil.class);
        PowerMockito.when(APIUtil.isMultiGroupAppSharingEnabled()).thenReturn(false);

        // Mock static classes
        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(APIMgtDBUtil.class);

        // Mock database connection and statements
        connection = Mockito.mock(Connection.class);
        preparedStatement = Mockito.mock(PreparedStatement.class);
        resultSet = Mockito.mock(ResultSet.class);

        PowerMockito.when(APIMgtDBUtil.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery()).thenReturn(resultSet);

        // Reset the singleton and create a fresh instance with mocked dependencies
        java.lang.reflect.Field instanceField = ApiMgtDAO.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, null);

        apiMgtDAO = ApiMgtDAO.getInstance();
    }

    /**
     * Test that deleteApplication calls keyManager.deleteApplication() for non-MAPPED keys.
     * This is the normal flow where the app has CREATED-mode OAuth keys.
     */
    @Test
    public void testDeleteApplicationCallsKeyManagerDeleteForCreatedKeys() throws Exception {
        Application application = createTestApplication();

        // First resultSet call for subscriptions query - no subscriptions
        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // one consumer key found
                .thenReturn(false); // no more consumer keys

        // Consumer key result set columns
        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn("testConsumerKey123");
        Mockito.when(resultSet.getString("NAME")).thenReturn("Default");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Default"))
                .thenReturn(keyManager);

        apiMgtDAO.deleteApplication(application);

        // Verify keyManager.deleteApplication was called with the consumer key
        verify(keyManager, times(1)).deleteApplication("testConsumerKey123");
        // Verify keyManager.deleteMappedApplication was also called
        verify(keyManager, times(1)).deleteMappedApplication("testConsumerKey123");
    }

    /**
     * Test that deleteApplication does NOT call keyManager.deleteApplication() for MAPPED keys.
     * MAPPED keys are created externally and should not be deleted by APIM.
     */
    @Test
    public void testDeleteApplicationSkipsKeyManagerDeleteForMappedKeys() throws Exception {
        Application application = createTestApplication();

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // one consumer key found
                .thenReturn(false); // no more consumer keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn("mappedConsumerKey");
        Mockito.when(resultSet.getString("NAME")).thenReturn("Default");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.MAPPED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Default"))
                .thenReturn(keyManager);

        apiMgtDAO.deleteApplication(application);

        // deleteApplication should NOT be called for MAPPED mode
        verify(keyManager, never()).deleteApplication(anyString());
        // deleteMappedApplication should still be called
        verify(keyManager, times(1)).deleteMappedApplication("mappedConsumerKey");
    }

    /**
     * Test that deleteApplication handles APIManagementException from keyManager.deleteApplication()
     * gracefully without propagating the exception. This simulates the scenario from issue #4866
     * where the IS OAuthApplicationMgtListener throws an error because the OAuth app was already
     * deleted before the service provider cleanup runs.
     */
    @Test
    public void testDeleteApplicationHandlesKeyManagerDeleteError() throws Exception {
        Application application = createTestApplication();

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // one consumer key found
                .thenReturn(false); // no more consumer keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn("errorConsumerKey");
        Mockito.when(resultSet.getString("NAME")).thenReturn("Default");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Default"))
                .thenReturn(keyManager);

        // Simulate the error that occurs when IS tries to look up the already-deleted OAuth app
        Mockito.doThrow(new APIManagementException("Error while Deleting Client Application"))
                .when(keyManager).deleteApplication("errorConsumerKey");

        // The deletion should complete without throwing an exception
        // because deleteApplication catches APIManagementException from keyManager
        apiMgtDAO.deleteApplication(application);

        // Verify that the key manager delete was attempted
        verify(keyManager, times(1)).deleteApplication("errorConsumerKey");
        // Verify the transaction was committed despite the key manager error
        verify(connection, times(1)).commit();
    }

    /**
     * Test that deleteApplication handles the case where keyManager.deleteMappedApplication()
     * throws an error. Both deleteMappedApplication and deleteApplication errors should be
     * handled gracefully.
     */
    @Test
    public void testDeleteApplicationHandlesMappedApplicationDeleteError() throws Exception {
        Application application = createTestApplication();

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // one consumer key found
                .thenReturn(false); // no more consumer keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn("errorMappedKey");
        Mockito.when(resultSet.getString("NAME")).thenReturn("Default");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Default"))
                .thenReturn(keyManager);

        Mockito.doThrow(new APIManagementException("Error deleting mapped application"))
                .when(keyManager).deleteMappedApplication("errorMappedKey");

        // Should not propagate the exception
        apiMgtDAO.deleteApplication(application);

        verify(keyManager, times(1)).deleteMappedApplication("errorMappedKey");
        verify(connection, times(1)).commit();
    }

    /**
     * Test deletion of application with multiple consumer keys across different key managers.
     * Ensures all keys are processed even if one key manager fails.
     */
    @Test
    public void testDeleteApplicationWithMultipleKeysAndPartialFailure() throws Exception {
        Application application = createTestApplication();
        KeyManager keyManager2 = Mockito.mock(KeyManager.class);

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // first consumer key
                .thenReturn(true)   // second consumer key
                .thenReturn(false); // no more consumer keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY))
                .thenReturn("consumerKey1")
                .thenReturn("consumerKey2");
        Mockito.when(resultSet.getString("NAME"))
                .thenReturn("DefaultKM")
                .thenReturn("ExternalKM");
        Mockito.when(resultSet.getString("ORGANIZATION"))
                .thenReturn("carbon.super")
                .thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE"))
                .thenReturn(APIConstants.OAuthAppMode.CREATED.name())
                .thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "DefaultKM"))
                .thenReturn(keyManager);
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "ExternalKM"))
                .thenReturn(keyManager2);

        // First key manager throws error (simulating the race condition from issue #4866)
        Mockito.doThrow(new APIManagementException("application.not.found"))
                .when(keyManager).deleteApplication("consumerKey1");

        apiMgtDAO.deleteApplication(application);

        // Both key managers should have been called despite the first one failing
        verify(keyManager, times(1)).deleteApplication("consumerKey1");
        verify(keyManager2, times(1)).deleteApplication("consumerKey2");
        verify(connection, times(1)).commit();
    }

    /**
     * Test deletion of application when key manager instance is null.
     * This can happen when a key manager is removed but app keys still reference it.
     */
    @Test
    public void testDeleteApplicationWithNullKeyManager() throws Exception {
        Application application = createTestApplication();

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // one consumer key found
                .thenReturn(false); // no more consumer keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn("orphanedKey");
        Mockito.when(resultSet.getString("NAME")).thenReturn("RemovedKM");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        // Key manager not found - returns null
        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "RemovedKM"))
                .thenReturn(null);

        // Should complete without NullPointerException
        apiMgtDAO.deleteApplication(application);

        verify(connection, times(1)).commit();
    }

    /**
     * Test deletion of application with no consumer keys (app without generated keys).
     */
    @Test
    public void testDeleteApplicationWithNoConsumerKeys() throws Exception {
        Application application = createTestApplication();

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(false); // no consumer keys

        apiMgtDAO.deleteApplication(application);

        // No key manager calls should be made
        verify(keyManager, never()).deleteApplication(anyString());
        verify(keyManager, never()).deleteMappedApplication(anyString());
        verify(connection, times(1)).commit();
    }

    /**
     * Test deletion of application where consumer key is null in the database.
     */
    @Test
    public void testDeleteApplicationWithNullConsumerKey() throws Exception {
        Application application = createTestApplication();

        Mockito.when(resultSet.next())
                .thenReturn(false)  // no subscriptions
                .thenReturn(true)   // one row found
                .thenReturn(false); // no more rows

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn(null);

        apiMgtDAO.deleteApplication(application);

        // No key manager calls should be made for null consumer key
        verify(keyManager, never()).deleteApplication(anyString());
        verify(connection, times(1)).commit();
    }

    private Application createTestApplication() {
        Subscriber subscriber = new Subscriber("testUser");
        subscriber.setTenantId(-1234);
        Application application = new Application("TestDeleteApp", subscriber);
        application.setId(1);
        application.setUUID("test-uuid-1234");
        return application;
    }
}
