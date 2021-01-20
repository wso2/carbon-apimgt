/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.keymgt.listeners;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.keymgt.SubscriptionDataHolder;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.ServerStartupObserver;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

import java.util.HashMap;

public class ServerStartupListener extends AbstractAxis2ConfigurationContextObserver implements ServerStartupObserver {
    private static final Log log = LogFactory.getLog(ServerStartupListener.class);

    @Override
    public void completingServerStartup() {

    }

    @Override
    public void completedServerStartup() {
        //load subscription data to memory
        SubscriptionDataHolder.getInstance().registerTenantSubscriptionStore(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        createReservedUser();
    }

    @Override
    public void createdConfigurationContext(ConfigurationContext configContext) {
        //load subscription data to memory
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        log.info("Initializing ServerStartupListener for SubscriptionStore for the tenant domain : " + tenantDomain);
        SubscriptionDataHolder.getInstance().registerTenantSubscriptionStore(tenantDomain);
    }

    @Override
    public void terminatedConfigurationContext(ConfigurationContext configCtx) {
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        SubscriptionDataHolder.getInstance().unregisterTenantSubscriptionStore(tenantDomain);
    }

    /**
     * Creates a reserved user to be used in cross tenant subscription scenarios, so that the tenant admin is
     * not exposed in JWT tokens generated. This logic will be run to add this user to the super tenant if it
     * is not existing. This value can be changed from a config as well.
     */
    public void createReservedUser() {
        APIManagerConfiguration config = getAPIManagerConfiguration();
        String username = APIConstants.DEFAULT_RESERVED_USERNAME;
        if (config != null) {
            String usernameConfig = config.getFirstProperty(APIConstants.KEY_MANAGER_RESERVED_USER);
            if (StringUtils.isNotBlank(usernameConfig)) {
                username = usernameConfig;
            }
        }
        try {
            RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
            int tenantId = getTenantId();
            if (realmService != null && tenantId != MultitenantConstants.INVALID_TENANT_ID) {
                UserStoreManager userStoreManager =
                        (UserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager();
                boolean isReservedUserCreated = userStoreManager.isExistingUser(username);
                if (!isReservedUserCreated) {
                    userStoreManager.addUser(username, APIConstants.DEFAULT_RESERVED_USER_PASSWORD,
                            new String[]{APIConstants.EVERYONE_ROLE},
                            new HashMap<>(), username, false);
                }
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while getting the realm configuration, User store properties might not be " +
                    "returned", e);
        }
    }

    /**
     * Retrieves the tenant id from the Thread Local Carbon Context
     *
     * @return tenant id
     */
    int getTenantId() {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * Returns API manager configurations.
     * @return APIManagerConfiguration object
     */
    private APIManagerConfiguration getAPIManagerConfiguration() {
        return ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
    }
}
