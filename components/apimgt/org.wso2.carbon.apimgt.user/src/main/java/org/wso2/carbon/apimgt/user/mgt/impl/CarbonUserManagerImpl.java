/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.user.mgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.user.mgt.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.user.mgt.UserConstants;
import org.wso2.carbon.apimgt.user.mgt.UserManager;
import org.wso2.carbon.apimgt.user.exceptions.UserException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.*;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.config.RealmConfigXMLProcessor;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.user.mgt.UserAdmin;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.*;

public class CarbonUserManagerImpl implements UserManager {

    private static final Log logger = LogFactory.getLog(CarbonUserManagerImpl.class);

    @Override
    public boolean isTenantAvailable(String tenantDomain) throws UserException {
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            if (tenantId == -1) return false;
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().isTenantActive(tenantId);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public int getTenantId(String tenantDomain) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void denyRole(int tenantId, String role, String resourcePath, String action) throws UserException {
        try {
            ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId).
                    getAuthorizationManager().denyRole(role, resourcePath, action);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String[] getRoleListOfUser(int tenantId, String username) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().getRoleListOfUser(username);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String[] getRoleListOfUser(String username) throws UserException {
        try {
            return CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .getRoleListOfUser(username);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String addDomainToName(String username, String domainName) {
        return UserCoreUtil.addDomainToName(username, domainName);
    }

    @Override
    public String getTenantDomainByTenantId(int tenantId) throws UserException {
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
        if (realmService == null) {
            return MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
        }

        try {
            return realmService.getTenantManager().getDomain(tenantId);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isRoleAuthorized(int tenantId, String roleName, String resourceId, String action)
            throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager()
                    .isRoleAuthorized(roleName, resourceId, action);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }

    }

    @Override
    public void authorizeRole(int tenantId, String roleName, String resourceId, String action) throws UserException {
        try {
            ServiceReferenceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(MultitenantConstants.SUPER_TENANT_ID).getAuthorizationManager()
                    .authorizeRole(roleName, resourceId, action);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String getAdminUsername() throws UserException {
        try {
            return CarbonContext.getThreadLocalCarbonContext().getUserRealm().getRealmConfiguration()
                    .getAdminUserName();
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String getAdminUsername(int tenantId) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String getUserClaimValue(int tenantId, String tenantAwareUsername, String claimUri, String profileName)
            throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().getUserClaimValue(tenantAwareUsername, claimUri, profileName);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isExistingUser(int tenantId, String username) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().
                    getTenantUserRealm(tenantId).getUserStoreManager().isExistingUser(username);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String extractDomainFromName(String userId) {
        return UserCoreUtil.extractDomainFromName(userId);
    }

    @Override
    public boolean isExistingRole(int tenantId, String role) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().isExistingRole(role);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void updateRoleListOfUser(int tenantId, String username, String[] deletedRoles, String[] newRoles)
            throws UserException {
        try {
            ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getUserStoreManager().updateRoleListOfUser(username, deletedRoles, newRoles);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteUser(int tenantId, String userName) throws UserException {
        try {
            ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId).getUserStoreManager()
                    .deleteUser(userName);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String[] getUserList(int tenantId, String claimUri, String claimValue, String profile)
            throws UserException {
        try {
            return ((org.wso2.carbon.user.core.UserStoreManager) ServiceReferenceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager()).getUserList(claimUri, claimValue, profile);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserAuthorized(int tenantId, String username, String permission, String action)
            throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getAuthorizationManager().isUserAuthorized(username, permission, action);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String getAdminPassword(int tenantId) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminPassword();
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isUserInRole(int tenantId, String username, String roleName) throws UserException {
        try {
            return ((AbstractUserStoreManager) ServiceReferenceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId).getUserStoreManager()).isUserInRole(username, roleName);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String[] getAllowedRolesForResource(int tenantId, String resourcePath, String action) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getAuthorizationManager().getAllowedRolesForResource(resourcePath, action);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public Set<String> getTenantDomainsByState(String state) throws UserException {
        boolean isActive = state.equalsIgnoreCase(UserConstants.TENANT_STATE_ACTIVE);
        Tenant[] tenants = new Tenant[0];
        try {
            tenants = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().getAllTenants();
        } catch (UserStoreException e) {
            logger.error("Error while fetching all tenants", e);
        }
        if (tenants == null || tenants.length == 0) {
            return Collections.emptySet();
        }
        Set<String> tenantDomains = new HashSet<>();
        for (Tenant tenant : tenants) {
            if (tenant.isActive() == isActive) {
                tenantDomains.add(tenant.getDomain());
            }
        }
        if (!tenantDomains.isEmpty() && isActive) {
            tenantDomains.add(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        }
        return tenantDomains;
    }

    @Override
    public String getDomainFromThreadLocal() {
        return UserCoreUtil.getDomainFromThreadLocal();
    }

    @Override
    public boolean authenticate(String username, String password) throws UserException {
        try {
            return CarbonContext.getThreadLocalCarbonContext().getUserRealm().getUserStoreManager()
                    .authenticate(username, password);
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String getProperty(int tenantId, String propertyName) throws UserException {
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
        if (realmService != null && tenantId != MultitenantConstants.INVALID_TENANT_ID) {
            RealmConfiguration realmConfiguration = null;
            try {
                realmConfiguration = ((UserStoreManager) realmService.getTenantUserRealm(tenantId).getUserStoreManager()).getRealmConfiguration();
            } catch (UserStoreException e) {
                throw new UserException(e.getMessage(), e);
            }
            if (realmConfiguration != null) {
                return realmConfiguration.getUserStoreProperty(propertyName);
            }
        }
        return null;
    }

    @Override
    public void changePasswordByUser(String username, String currentPassword, String newPassword) throws UserException {
        UserAdmin userAdmin = new UserAdmin();
        try {
            userAdmin.changePasswordByUser(username, currentPassword, newPassword);
        } catch (UserAdminException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public void createRole(String roleName, Map<String, String> permissions, int tenantId) throws UserException {
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
        UserRealm realm;
        org.wso2.carbon.user.api.UserRealm tenantRealm;
        UserStoreManager manager;

        try {
            if (tenantId < 0) {
                realm = realmService.getBootstrapRealm();
                manager = realm.getUserStoreManager();
            } else {
                tenantRealm = realmService.getTenantUserRealm(tenantId);
                manager = (UserStoreManager) tenantRealm.getUserStoreManager();
            }
            if (!manager.isExistingRole(roleName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Creating role: " + roleName);
                }
                String tenantAdminName = ServiceReferenceHolder.getInstance().getRealmService()
                        .getTenantUserRealm(tenantId).getRealmConfiguration().getAdminUserName();
                String[] userList = new String[]{tenantAdminName};
                List<Permission> permissionList = new ArrayList<>();
                for (Map.Entry<String, String> entry : permissions.entrySet()) {
                    permissionList.add(new Permission(entry.getKey(), entry.getValue()));
                }
                manager.addRole(roleName, userList, permissionList.toArray(new Permission[permissionList.size()]));
            }
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    @Override
    public String getPropertyFromFile(String propertyName) throws UserException {
        try {
            RealmConfiguration realmConfig = new RealmConfigXMLProcessor().buildRealmConfigurationFromFile();
            if (propertyName.equals(UserConstants.PROP_ADMIN_USERNAME)) {
                return realmConfig.getAdminUserName();
            }
            if (propertyName.equals(UserConstants.PROP_ADMIN_PASSWORD)) {
                return realmConfig.getAdminPassword();
            }
        } catch (org.wso2.carbon.user.core.UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public SortedMap<String, String> getClaims(String username, int tenantId, String dialectURI) throws UserException {
        try {
            ClaimManager claimManager = ServiceReferenceHolder.getInstance().getRealmService()
                    .getTenantUserRealm(tenantId).getClaimManager();
            ClaimMapping[] claims = claimManager.getAllClaimMappings(dialectURI);
            String[] claimURIs = claimMappingtoClaimURIString(claims);
            UserStoreManager userStoreManager = (UserStoreManager) ServiceReferenceHolder.getInstance()
                    .getRealmService().getTenantUserRealm(tenantId).getUserStoreManager();
            String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
            return new TreeMap(userStoreManager.getUserClaimValues(tenantAwareUserName, claimURIs, null));
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }

    private String[] claimMappingtoClaimURIString(ClaimMapping[] claims) {

        String[] temp = new String[claims.length];
        for (int i = 0; i < claims.length; i++) {
            temp[i] = claims[i].getClaim().getClaimUri();

        }
        return temp;
    }

    @Override
    public String getClaimDisplayName(String claimURI, String username, int tenantId) throws UserException {
        try {
            return ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId).getClaimManager()
                    .getClaim(claimURI).getDisplayTag();
        } catch (UserStoreException e) {
            throw new UserException(e.getMessage(), e);
        }
    }
}
