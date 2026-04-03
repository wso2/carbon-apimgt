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

package org.wso2.carbon.apimgt.impl;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.WorkflowResponse;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Subscriber;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.ApplicationWorkflowDTO;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.ApplicationDeletionSimpleWorkflowExecutor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Integration-style test that exercises the full application deletion flow:
 * WorkflowExecutor -> ApiMgtDAO.deleteApplication() -> KeyManager.deleteApplication()
 *
 * This test specifically reproduces the scenario from issue #4866:
 * 1. An application is created with generated PRODUCTION keys
 * 2. The application is deleted
 * 3. During deletion, keyManager.deleteApplication(consumerKey) is called which
 *    triggers the IS DCR endpoint to delete the OAuth app
 * 4. The IS OAuthApplicationMgtListener then tries to look up the already-deleted
 *    OAuth app and logs an ERROR
 *
 * The test verifies that:
 * - The deletion flow completes successfully even when the key manager operation fails
 * - All key manager cleanup operations are attempted
 * - The database transaction is committed
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({KeyManagerHolder.class, APIMgtDBUtil.class, ApiMgtDAO.class,
        ServiceReferenceHolder.class, APIUtil.class})
public class ApplicationDeletionWithKeysIntegrationTest {

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

        PowerMockito.mockStatic(KeyManagerHolder.class);
        PowerMockito.mockStatic(APIMgtDBUtil.class);

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
     * Full flow test: Simulates creating an app with PRODUCTION keys, then deleting it.
     * The keyManager.deleteApplication() call triggers the IS DCR delete which causes
     * the OAuthApplicationMgtListener race condition from issue #4866.
     *
     * Expected: The deletion completes successfully. The error from the key manager is
     * caught and logged (not propagated), and the DB cleanup proceeds normally.
     */
    @Test
    public void testDeleteApplicationWithProductionKeysTriggersKeyManagerDelete() throws Exception {
        String consumerKey = "bhihzmGIO6tkwwJfVfB4Ej2D4UUa";

        Application application = new Application("TestDeleteApp", new Subscriber("admin"));
        application.setId(42);
        application.setUUID("app-uuid-prod-keys");

        // Simulate: no subscriptions, one PRODUCTION consumer key
        Mockito.when(resultSet.next())
                .thenReturn(false)   // no subscriptions
                .thenReturn(true)    // one consumer key (PRODUCTION)
                .thenReturn(false);  // no more keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn(consumerKey);
        Mockito.when(resultSet.getString("NAME")).thenReturn("Resident Key Manager");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Resident Key Manager"))
                .thenReturn(keyManager);

        // Execute the full deletion flow through the workflow executor
        ApplicationWorkflowDTO workflowDTO = new ApplicationWorkflowDTO();
        workflowDTO.setApplication(application);
        workflowDTO.setWorkflowReference(String.valueOf(application.getId()));

        // The ApiMgtDAO singleton was initialized in setUp() with mocked dependencies.
        // The workflow executor calls ApiMgtDAO.getInstance() which returns our instance.
        ApplicationDeletionSimpleWorkflowExecutor executor = new ApplicationDeletionSimpleWorkflowExecutor();
        WorkflowResponse response = executor.execute(workflowDTO);

        Assert.assertNotNull(response);

        // Verify the key manager was called to delete the OAuth application
        verify(keyManager, times(1)).deleteApplication(consumerKey);
        // Verify mapped application cleanup was also called
        verify(keyManager, times(1)).deleteMappedApplication(consumerKey);
        // Verify the transaction committed
        verify(connection, times(1)).commit();
    }

    /**
     * Simulates the exact error from issue #4866: The key manager's deleteApplication
     * succeeds on the DCR side, but the subsequent service provider deletion triggers
     * OAuthApplicationMgtListener which fails because the OAuth app is already gone.
     *
     * In practice, this error manifests as an APIManagementException wrapping the
     * IS-side error. The APIM code catches this at ApiMgtDAO line 4894-4896.
     *
     * This test verifies that even when deleteApplication() on the key manager fails,
     * the overall application deletion still completes successfully.
     */
    @Test
    public void testDeleteApplicationCompletesWhenKeyManagerThrowsRaceConditionError() throws Exception {
        String consumerKey = "raceConditionConsumerKey";

        Application application = new Application("AppWithRaceCondition", new Subscriber("admin"));
        application.setId(99);
        application.setUUID("race-condition-uuid");

        Mockito.when(resultSet.next())
                .thenReturn(false)
                .thenReturn(true)
                .thenReturn(false);

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn(consumerKey);
        Mockito.when(resultSet.getString("NAME")).thenReturn("Resident Key Manager");
        Mockito.when(resultSet.getString("ORGANIZATION")).thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE")).thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Resident Key Manager"))
                .thenReturn(keyManager);

        // Simulate the race condition: deleteApplication throws because the IS-side
        // OAuthApplicationMgtListener can't find the already-deleted OAuth app
        Mockito.doThrow(new APIManagementException(
                "Error while Deleting Client Application - application.not.found"))
                .when(keyManager).deleteApplication(consumerKey);

        // The deletion should still complete without exception
        apiMgtDAO.deleteApplication(application);

        verify(keyManager, times(1)).deleteApplication(consumerKey);
        verify(connection, times(1)).commit();
    }

    /**
     * Test deletion of an app that has both PRODUCTION and SANDBOX keys.
     * Both sets of keys should be cleaned up, and if one fails the other should still proceed.
     */
    @Test
    public void testDeleteApplicationWithBothProductionAndSandboxKeys() throws Exception {
        String prodConsumerKey = "prodKey123";
        String sandboxConsumerKey = "sandboxKey456";

        Application application = new Application("AppWithBothKeys", new Subscriber("admin"));
        application.setId(55);
        application.setUUID("both-keys-uuid");

        Mockito.when(resultSet.next())
                .thenReturn(false)   // no subscriptions
                .thenReturn(true)    // PRODUCTION key
                .thenReturn(true)    // SANDBOX key
                .thenReturn(false);  // no more keys

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY))
                .thenReturn(prodConsumerKey)
                .thenReturn(sandboxConsumerKey);
        Mockito.when(resultSet.getString("NAME"))
                .thenReturn("Resident Key Manager")
                .thenReturn("Resident Key Manager");
        Mockito.when(resultSet.getString("ORGANIZATION"))
                .thenReturn("carbon.super")
                .thenReturn("carbon.super");
        Mockito.when(resultSet.getString("CREATE_MODE"))
                .thenReturn(APIConstants.OAuthAppMode.CREATED.name())
                .thenReturn(APIConstants.OAuthAppMode.CREATED.name());

        PowerMockito.when(KeyManagerHolder.getKeyManagerInstance("carbon.super", "Resident Key Manager"))
                .thenReturn(keyManager);

        // PRODUCTION key deletion fails (race condition), SANDBOX should still proceed
        Mockito.doThrow(new APIManagementException("application.not.found"))
                .when(keyManager).deleteApplication(prodConsumerKey);

        apiMgtDAO.deleteApplication(application);

        // Both keys should have been attempted
        verify(keyManager, times(1)).deleteApplication(prodConsumerKey);
        verify(keyManager, times(1)).deleteApplication(sandboxConsumerKey);
        verify(keyManager, times(1)).deleteMappedApplication(prodConsumerKey);
        verify(keyManager, times(1)).deleteMappedApplication(sandboxConsumerKey);
        verify(connection, times(1)).commit();
    }

    /**
     * Test deletion of an application that has keys that were already revoked/removed.
     * The key manager should not be called for null consumer keys.
     */
    @Test
    public void testDeleteApplicationWithRevokedKeys() throws Exception {
        Application application = new Application("AppWithRevokedKeys", new Subscriber("admin"));
        application.setId(77);
        application.setUUID("revoked-keys-uuid");

        Mockito.when(resultSet.next())
                .thenReturn(false)   // no subscriptions
                .thenReturn(true)    // one row with null consumer key
                .thenReturn(false);

        Mockito.when(resultSet.getString(APIConstants.FIELD_CONSUMER_KEY)).thenReturn(null);

        apiMgtDAO.deleteApplication(application);

        verify(keyManager, never()).deleteApplication(anyString());
        verify(keyManager, never()).deleteMappedApplication(anyString());
        verify(connection, times(1)).commit();
    }
}
