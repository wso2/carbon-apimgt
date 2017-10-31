/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.keymgt.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.util.APIKeyMgtDataHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityRuntimeException;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This web-service can only be used by super-tenant admins. This will be useful to do the UserManagement tasks in
 * other tenants, by using the super-tenant credentials.
 */
public class MultiTenantUserAdminService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(MultiTenantUserAdminService.class);

    /**
     * To get the role list of a particular user.
     *
     * @param userName UserName of the user to get the role list. This should be fully qualified username, including
     *                 the tenant domain
     * @return the role list of the user.
     * @throws APIKeyMgtException API Key Management Exception.
     */
    @SuppressWarnings("unused")
    public String[] getUserRoleList(String userName) throws APIKeyMgtException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (log.isDebugEnabled()) {
            log.debug("MultiTenantUserAdminService getUserRoleList request received from the tenant " + tenantId);
        }
        if (tenantId != MultitenantConstants.SUPER_TENANT_ID) {
            if (log.isDebugEnabled()) {
                log.debug("MultiTenantUserAdminService is a privileged admin service and can only be used by "
                        + "super-tenant users.");
            }
            return new String[0];
        } else {
            try {
                int userTenantId = IdentityTenantUtil.getTenantIdOfUser(userName);
                UserStoreManager userStoreManager = APIKeyMgtDataHolder.getRealmService()
                        .getTenantUserRealm(userTenantId).getUserStoreManager();
                return userStoreManager.getRoleListOfUser(MultitenantUtils.getTenantAwareUsername(userName));
            } catch (UserStoreException e) {
                String errorMessage = "UserStore exception while trying the get the user role list of the " + userName;
                log.error(errorMessage, e);
                throw new APIKeyMgtException(errorMessage, e);
            } catch (IdentityRuntimeException e) {
                String errorMessage = "UserStore exception while trying the get the user role list of the " + userName;
                log.error(errorMessage, e);
                throw new APIKeyMgtException("User " + userName + " is from a invalid domain", e);
            }
        }
    }
}
