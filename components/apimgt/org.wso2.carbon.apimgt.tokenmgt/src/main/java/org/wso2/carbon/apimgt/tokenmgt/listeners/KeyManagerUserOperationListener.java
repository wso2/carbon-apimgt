/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.tokenmgt.listeners;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.tokenmgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.tokenmgt.util.TokenMgtDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.listener.IdentityOathEventListener;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.Set;

/**
 * This listener class will execute upon user deletion, role update of user and user update of role operations. It is
 * intended to remove the cached access tokens in the API Gateway belonging to the relevant user when one of the above
 * operations occur.
 */
public class KeyManagerUserOperationListener extends IdentityOathEventListener {

    private static final Log log = LogFactory.getLog(KeyManagerUserOperationListener.class);

    /**
     * Bundle execution order id.
     */
    @Override
    public int getExecutionOrderId() {
        /**
         * This class is intended to remove the active access tokens from the API Gateway cache. Upon user removal, the
         * IdentityOathEventListener class revokes the user's active access tokens. We need this listener class to
         * execute before the IdentityOathEventListener so that it can retrieve the user's active access tokens before
         * IdentityOathEventListener updates their status to 'REVOKED'.
         */
        return getOrderId() - 1;
    }

    @Override
    public boolean doPostUpdateCredential(String userName, Object credential, UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        clearGatewayUsernameCache(userName);
        return super.doPostUpdateCredential(userName, credential, userStoreManager);
    }

    @Override
    public boolean doPostUpdateCredentialByAdmin(String userName, Object credential,
                                                 UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        clearGatewayUsernameCache(userName);
        return super.doPostUpdateCredentialByAdmin(userName, credential, userStoreManager);
    }

    @Override
    public boolean doPostUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles,
                                              UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        clearGatewayUsernameCache(userName);
        return super.doPostUpdateRoleListOfUser(userName, deletedRoles, newRoles, userStoreManager);
    }

    @Override
    public boolean doPostUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                              UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        clearGatewayUsernameCache((String[]) ArrayUtils.addAll(newUsers, deletedUsers));
        return super.doPostUpdateUserListOfRole(roleName, deletedUsers, newUsers, userStoreManager);
    }

    @Override
    public boolean doPostAddUser(String userName, Object credential, String[] roleList, Map<String, String> claims,
                                 String profile, UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        clearGatewayUsernameCache(userName);
        return super.doPostAddUser(userName, credential, roleList, claims, profile, userStoreManager);
    }

    @Override
    public boolean doPostDeleteUser(String userName, UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        clearGatewayUsernameCache(userName);
        return super.doPostDeleteUser(userName, userStoreManager);
    }

    /**
     * Deleting user from the identity database prerequisites. Remove pending approval requests for the user and remove
     * the gateway key cache.
     */
    @Override
    public boolean doPreDeleteUser(String username, UserStoreManager userStoreManager) {

        boolean isTenantFlowStarted = false;
        ApiMgtDAO apiMgtDAO = getDAOInstance();
        try {
            String tenantDomain = getTenantDomain();
            int tenantId = getTenantId();
            Tenant tenant = getTenant(tenantId);
            if (tenant == null && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                tenant = new org.wso2.carbon.user.core.tenant.Tenant();
                tenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                tenant.setId(MultitenantConstants.SUPER_TENANT_ID);
            } else {
                // Add tenant domain to the username if user is not from super tenant domain.
                // When adding a user, tenant domain is appended to workflow reference only if that user
                // is from a different tenant domain(not carbon.super).
                username = UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);
            }
            Map<String, String> userStoreProperties = userStoreManager.getProperties(tenant);
            String userDomain = userStoreProperties.get(UserCoreConstants.RealmConfig.PROPERTY_DOMAIN_NAME);

            // IdentityUtil.addDomaintoName adds domain to the name only if domain name is not PRIMARY
            // therefore domain name should be manually added to the username if domain is PRIMARY
            if (UserCoreConstants.PRIMARY_DEFAULT_DOMAIN_NAME.equals(userDomain)) {
                username = userDomain.toUpperCase() + UserCoreConstants.DOMAIN_SEPARATOR + username;
            } else {
                username = IdentityUtil.addDomainToName(username, userDomain);
            }

            WorkflowExecutor userSignupWFExecutor = getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
            String workflowExtRef = apiMgtDAO.getExternalWorkflowReferenceForUserSignup(username);
            userSignupWFExecutor.cleanUpPendingTask(workflowExtRef);
        } catch (WorkflowException e) {
            // exception is not thrown to the caller since this is a event Identity(IS) listener
            log.error("Error while cleaning up workflow task for the user: " + username, e);
        } catch (APIManagementException e) {
            // exception is not thrown to the caller since this is a event Identity(IS) listener
            log.error("Error while cleaning up workflow task for the user: " + username, e);
        } catch (UserStoreException e) {
            // exception is not thrown to the caller since this is a event Identity(IS) listener
            log.error("Error while cleaning up workflow task for the user: " + username, e);
        }
        APIUtil.clearRoleCache(getUserName(username, userStoreManager));
        return !isEnable() || removeGatewayKeyCache(username, userStoreManager);
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String username, String[] deletedRoles, String[] newRoles,
                                             UserStoreManager userStoreManager) {
        if (!isEnable()) {
            return true;
        }
        if (ArrayUtils.isNotEmpty(deletedRoles)) {
            removeGatewayKeyCache(username, userStoreManager);
        }
        APIUtil.clearRoleCache(getUserName(username, userStoreManager));
        return true;
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers,
                                             UserStoreManager userStoreManager) {

        boolean isRemoveGatewayKeyCache = invalidateMultipleCacheKeys(deletedUsers, userStoreManager, true);
        isRemoveGatewayKeyCache = invalidateMultipleCacheKeys(newUsers, userStoreManager, isRemoveGatewayKeyCache);
        return !isEnable() || isRemoveGatewayKeyCache;
    }

    private boolean removeGatewayKeyCache(String username, UserStoreManager userStoreManager) {

        username = getUserName(username, userStoreManager);

        try {
            if (APIUtil.getEnvironments().size() <= 0) {
                return true;
            }
        } catch (APIManagementException e) {
            log.error("Error while reading configured gateway environments", e);
            return false;
        }

        ApiMgtDAO apiMgtDAO = getDAOInstance();
        Set<String> activeTokens;

        try {
            activeTokens = apiMgtDAO.getActiveAccessTokensOfUser(username);
        } catch (APIManagementException e) {
            log.error("Error while getting active access tokens of user " + username, e);
            return false;
        }

        if (activeTokens == null || activeTokens.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No active tokens found for the user " + username);
            }
            return true;
        }

        if (log.isDebugEnabled()) {
            log.debug("Found " + activeTokens.size() + " active tokens of the user " + username);
        }

        APIAuthenticationAdminClient client = getApiAuthenticationAdminClient();
        client.invalidateCachedTokens(activeTokens);

        return true;
    }

    /**
     * Removes a given username that is cached on the API Gateway.
     *
     * @param username The username to be cleared from the cache
     */
    private void clearGatewayUsernameCache(String username) {

        APIAuthenticationAdminClient client = getApiAuthenticationAdminClient();
        client.invalidateCachedUsername(getEndUserName(username));

    }

    /**
     * Removes a given list of username that is cached on the API Gateway.
     *
     * @param usernameList The list of username to be cleared from the cache
     */
    private void clearGatewayUsernameCache(String[] usernameList) {

        APIAuthenticationAdminClient client = getApiAuthenticationAdminClient();
        for (int i = 0; i < usernameList.length; i++) {
            usernameList[i] = getEndUserName(usernameList[i]);
        }
        client.invalidateCachedUsernames(usernameList);

        log.debug("Removed cached usernames of the Gateway.");

    }

    private String getEndUserName(String username) {

        return username + "@" + getTenantDomain();
    }

    protected APIAuthenticationAdminClient getApiAuthenticationAdminClient() {

        return new APIAuthenticationAdminClient();
    }

    protected APIManagerConfiguration getApiManagerConfiguration() {

        return ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }

    protected boolean isUserStoreInUsernameCaseSensitive(String username) {

        return IdentityUtil.isUserStoreInUsernameCaseSensitive(username);
    }

    protected String getUserStoreDomainName(UserStoreManager userStoreManager) {

        return UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
    }

    protected WorkflowExecutor getWorkflowExecutor(String workflowType) throws WorkflowException {

        return WorkflowExecutorFactory.getInstance().getWorkflowExecutor(workflowType);
    }

    protected Tenant getTenant(int tenantId) throws UserStoreException {

        return TokenMgtDataHolder.getRealmService().getTenantManager().getTenant(tenantId);
    }

    protected int getTenantId() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    protected String getTenantDomain() {

        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
    }

    protected ApiMgtDAO getDAOInstance() {

        return ApiMgtDAO.getInstance();
    }

    /**
     * To get the fully qualified username with the user store domain.
     *
     * @param username         Name of the User.
     * @param userStoreManager User store manager, which the user is belong to.
     * @return fully qualified username.
     */
    private String getUserName(String username, UserStoreManager userStoreManager) {

        String userStoreDomain = getUserStoreDomainName(userStoreManager);
        String tenantDomain = getTenantDomain();

        username = UserCoreUtil.addDomainToName(username, userStoreDomain);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(tenantDomain) ||
                (!MultitenantUtils.isEmailUserName() &&
                username.indexOf(APIConstants.EMAIL_DOMAIN_SEPARATOR) > 0)) {
            username = UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);
        }

        //If the username is not case sensitive
        if (!isUserStoreInUsernameCaseSensitive(username)) {
            username = username.toLowerCase();
        }
        return username;
    }

    /**
     * To invalidate multiple cache entries when there is an update/
     *
     * @param users               User list that was updated.
     * @param userStoreManager    User Store Manager which the User is belongs to.
     * @param removedGatewayCache current status of removing gateway cache.
     * @return removedGatewayCache result.
     */
    private boolean invalidateMultipleCacheKeys(String[] users, UserStoreManager userStoreManager, boolean
            removedGatewayCache) {

        for (String username : users) {
            username = getUserName(username, userStoreManager);
            APIUtil.clearRoleCache(username);
            if (isEnable()) {
                removedGatewayCache = removedGatewayCache && removeGatewayKeyCache(username, userStoreManager);
            }
        }
        return removedGatewayCache;
    }
    @Override
    public boolean doPostSetUserClaimValue(String userName, UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        boolean isAccountLocked = isAccountLock(userName, userStoreManager);
        if (isAccountLocked) {
            removeGatewayKeyCache(userName, userStoreManager);
        }
        return super.doPostSetUserClaimValue(userName, userStoreManager);
    }

    @Override
    public boolean doPostSetUserClaimValues(String userName, Map<String, String> claims, String profileName,
                                            UserStoreManager userStoreManager)
            throws org.wso2.carbon.user.core.UserStoreException {

        boolean isAccountLocked = isAccountLock(userName, userStoreManager);
        if (isAccountLocked) {
            removeGatewayKeyCache(userName, userStoreManager);
        }
        return super.doPostSetUserClaimValues(userName, claims, profileName, userStoreManager);
    }

    private boolean isAccountLock(String userName, UserStoreManager userStoreManager) {

        String accountLockedClaim;
        try {
            Map<String, String> values = userStoreManager.getUserClaimValues(userName, new String[]{
                    APIConstants.ACCOUNT_LOCKED_CLAIM}, UserCoreConstants.DEFAULT_PROFILE);
            accountLockedClaim = values.get(APIConstants.ACCOUNT_LOCKED_CLAIM);
            return Boolean.parseBoolean(accountLockedClaim);
        } catch (UserStoreException e) {
            log.error("Error occurred while retrieving " + APIConstants.ACCOUNT_LOCKED_CLAIM
                    + " claim value", e);
        }
        return false;
    }
}
