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
package org.wso2.carbon.apimgt.keymgt.listeners;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIAuthenticationAdminClient;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowException;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutor;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowExecutorFactory;
import org.wso2.carbon.apimgt.keymgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.listener.IdentityOathEventListener;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserCoreConstants;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.util.UserCoreUtil;
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

    /**
     * Deleting user from the identity database prerequisites. Remove pending aprroval requests for the user and remove
     * the gateway key chache.
     */
    @Override
    public boolean doPreDeleteUser(String username, UserStoreManager userStoreManager) {

        boolean isTenantFlowStarted = false;
        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));

            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                PrivilegedCarbonContext.startTenantFlow();
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Tenant tenant = APIKeyMgtDataHolder.getRealmService().getTenantManager().getTenant(tenantId);
            if(tenant == null && MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                tenant = new org.wso2.carbon.user.core.tenant.Tenant();
                tenant.setDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
                tenant.setId(MultitenantConstants.SUPER_TENANT_ID);
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

            WorkflowExecutor userSignupWFExecutor = WorkflowExecutorFactory.getInstance().
                    getWorkflowExecutor(WorkflowConstants.WF_TYPE_AM_USER_SIGNUP);
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
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return !isEnable() || removeGatewayKeyCache(username, userStoreManager);
    }

    @Override
    public boolean doPreUpdateRoleListOfUser(String username, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) {

        return !isEnable() || removeGatewayKeyCache(username, userStoreManager);
    }

    @Override
    public boolean doPreUpdateUserListOfRole(String username, String[] deletedRoles, String[] newRoles,
            UserStoreManager userStoreManager) {

        return !isEnable() || removeGatewayKeyCache(username, userStoreManager);
    }

    private boolean removeGatewayKeyCache(String username, UserStoreManager userStoreManager) {

        String userStoreDomain = UserCoreUtil.getDomainName(userStoreManager.getRealmConfiguration());
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();

        username = UserCoreUtil.addDomainToName(username, userStoreDomain);
        username = UserCoreUtil.addTenantDomainToEntry(username, tenantDomain);

        //If the username is not case sensitive
        if (!IdentityUtil.isUserStoreInUsernameCaseSensitive(username)) {
            username = username.toLowerCase();
        }

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfigurationService().getAPIManagerConfiguration();

        if (config.getApiGatewayEnvironments().size() <= 0) {
            return true;
        }

        ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
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

        Map<String, Environment> gatewayEnvs = config.getApiGatewayEnvironments();

        for (Environment environment : gatewayEnvs.values()) {
            if (log.isDebugEnabled()) {
                log.debug("Going to remove tokens from the cache of the Gateway '" + environment.getName() + "'");
            }
            try {
                APIAuthenticationAdminClient client = new APIAuthenticationAdminClient(environment);
                client.invalidateCachedTokens(activeTokens);

                log.debug("Removed cached tokens of the Gateway.");
            } catch (AxisFault axisFault) {
                //log and continue invalidating caches of other Gateways (if any).
                log.error("Error occurred while invalidating the Gateway Token Cache of Gateway '" +
                        environment.getName() + "'", axisFault);
            }
        }

        return true;
    }

}
