/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *
 */

package org.wso2.carbon.apimgt.keymgt.listeners;

import org.apache.axis2.AxisFault;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * KeyManagerUserOperationListener Test Case
 */
public class KeyManagerUserOperationListenerTestCase {


    private UserStoreManager userStoreManager;
    private ApiMgtDAO apiMgtDAO;
    private Tenant tenant;
    private String username;
    private WorkflowExecutor workflowExecutor;
    private APIManagerConfiguration config;
    private APIAuthenticationAdminClient apiAuthenticationAdminClient;
    private String tenantedUsername;
    private Map<String, Environment> environmentMap;

    @Before
    public void init() {
        userStoreManager = Mockito.mock(UserStoreManager.class);
        apiMgtDAO = Mockito.mock(ApiMgtDAO.class);
        workflowExecutor = Mockito.mock(WorkflowExecutor.class);
        config = Mockito.mock(APIManagerConfiguration.class);
        apiAuthenticationAdminClient = Mockito.mock(APIAuthenticationAdminClient.class);
        tenant = new Tenant();
        username = "testuser";
        tenantedUsername = username + "@" + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        environmentMap = new HashMap<String, Environment>();
    }

    @Test
    public void testDoPreDeleteSuperTenantUser() throws APIManagementException {

        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config);
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteTenantUser() throws APIManagementException {
        tenant.setId(1);
        tenant.setDomain("wso2.com");
        WorkflowExecutor workflowExecutor = Mockito.mock(WorkflowExecutor.class);
        APIManagerConfiguration apiManagerConfiguration = Mockito.mock(APIManagerConfiguration.class);

        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, tenant, apiManagerConfiguration);
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteUserInPrimaryUserStore() throws APIManagementException, org.wso2.carbon.user.api
            .UserStoreException {
        tenant.setId(MultitenantConstants.SUPER_TENANT_ID);
        tenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        Map<String, String> userStoreProperties = new HashMap<String, String>();
        userStoreProperties.put(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME, UserCoreConstants
                .PRIMARY_DEFAULT_DOMAIN_NAME);
        Mockito.when(userStoreManager.getProperties(tenant)).thenReturn(userStoreProperties);
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, tenant, config);
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteUserWhenHandlerIsDisabled() throws APIManagementException, org
            .wso2.carbon.user.api.UserStoreException {

        apiAuthenticationAdminClient = Mockito.mock(APIAuthenticationAdminClient.class);
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config, apiAuthenticationAdminClient, false);
        Environment environment = new Environment();
        environmentMap.put("hybrid", environment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environmentMap);
        //Throws APIMgtException while retrieving access tokens
        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getActiveAccessTokensOfUser(tenantedUsername);
        //Should always continue when the handler is disabled, even though Gateway cache update fails
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteUserWhenGatewayEnvironmentsAreNotAvailable() throws APIManagementException {

        apiAuthenticationAdminClient = Mockito.mock(APIAuthenticationAdminClient.class);
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config, apiAuthenticationAdminClient, true);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environmentMap);
        //Throws APIMgtException while retrieving access tokens
        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getActiveAccessTokensOfUser(tenantedUsername);
        //Should always return true when the gateway environments are not available,
        //before throwing APIManagementException while retrieving accessTokens
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteUserWhenActiveAccessTokenAreNotAvailable() throws APIManagementException {

        apiAuthenticationAdminClient = Mockito.mock(APIAuthenticationAdminClient.class);
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config, apiAuthenticationAdminClient, true);
        Environment environment = new Environment();
        environmentMap.put("hybrid", environment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environmentMap);
        Mockito.when(apiMgtDAO.getActiveAccessTokensOfUser(tenantedUsername)).thenReturn(null);
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteUserWithAccessTokenRetrievalFailure() throws APIManagementException {
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config, apiAuthenticationAdminClient, true);
        Environment environment = new Environment();
        environmentMap.put("hybrid", environment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environmentMap);
        //Throws APIMgtException while retrieving access tokens
        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getActiveAccessTokensOfUser(tenantedUsername);
        Assert.assertFalse(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));
    }

    @Test
    public void testDoPreDeleteUserWithTokenCacheInvalidationAxisFault() throws AxisFault, APIManagementException {
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config, apiAuthenticationAdminClient, true);
        Environment environment = new Environment();
        environmentMap.put("hybrid", environment);
        Mockito.when(config.getApiGatewayEnvironments()).thenReturn(environmentMap);

        Set<String> activeTokens = new HashSet<String>();
        activeTokens.add(UUID.randomUUID().toString());

        Mockito.when(apiMgtDAO.getActiveAccessTokensOfUser(tenantedUsername)).thenReturn(activeTokens);

        //Test AxisFault while invalidating Cached Tokens via Admin Client
        Mockito.doThrow(AxisFault.class).when(apiAuthenticationAdminClient).invalidateCachedTokens(activeTokens);
        Assert.assertTrue(keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager));

    }

    @Test
    public void testDoPreDeleteUserWithAPIManagementExceptionWhileRetrievingWFReferences() throws
            APIManagementException {

        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config);
        Mockito.doThrow(APIManagementException.class).when(apiMgtDAO).getExternalWorkflowReferenceForUserSignup
                (username);
        keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager);
    }

    @Test
    public void testDoPreDeleteUserWithUserStoreExceptionWhileRetrievingUserStoreProperties() throws org.wso2.carbon
            .user.api.UserStoreException {
        Tenant tenant = new Tenant();
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, tenant, config);
        Mockito.doThrow(UserStoreException.class).when(userStoreManager).getProperties(tenant);
        keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager);
    }


    @Test
    public void testDoPreDeleteWithUserWorkflowExceptionWhileCleaningUpPendingWFTasks() throws APIManagementException,
            WorkflowException {
        String workflowExtRef = "";
        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config);
        Mockito.when(apiMgtDAO.getExternalWorkflowReferenceForUserSignup(username)).thenReturn(workflowExtRef);
        Mockito.doThrow(WorkflowException.class).when(workflowExecutor).cleanUpPendingTask(workflowExtRef);
        keyManagerUserOperationListener.doPreDeleteUser(username, userStoreManager);
    }

    @Test
    public void testDoPreUpdateRoleListOfUser() {

        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config);
        String[] deletedRoles = {"testRole1"};
        String[] newRoles = {"testRole2"};
        Assert.assertTrue(keyManagerUserOperationListener.doPreUpdateRoleListOfUser(username, deletedRoles, newRoles,
                userStoreManager));
    }

    @Test
    public void testDoPreUpdateUserListOfRole() {

        KeyManagerUserOperationListener keyManagerUserOperationListener = new KeyManagerUserOperationListenerWrapper
                (apiMgtDAO, workflowExecutor, null, config);
        String[] deletedRoles = {"testRole1"};
        String[] newRoles = {"testRole2"};
        Assert.assertTrue(keyManagerUserOperationListener.doPreUpdateUserListOfRole(username, deletedRoles, newRoles,
                userStoreManager));
    }

}
