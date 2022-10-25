/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.user.mgt.impl;

import org.wso2.apk.apimgt.user.exceptions.UserException;
import org.wso2.apk.apimgt.user.mgt.UserManager;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public class APKUserManagerImpl implements UserManager {

    @Override
    public boolean isTenantAvailable(String tenantDomain) throws UserException {
        return false;
    }

    @Override
    public int getTenantId(String tenantDomain) throws UserException {
        return 0;
    }

    @Override
    public void denyRole(int tenantId, String role, String resourcePath, String action) throws UserException {

    }

    @Override
    public String[] getRoleListOfUser(int tenantId, String username) throws UserException {
        return new String[0];
    }

    @Override
    public String[] getRoleListOfUser(String username) throws UserException {
        return new String[0];
    }

    @Override
    public String addDomainToName(String username, String domainName) {
        return null;
    }

    @Override
    public String getTenantDomainByTenantId(int tenantId) throws UserException {
        return null;
    }

    @Override
    public boolean isRoleAuthorized(int tenantId, String registryAnonnymousRoleName, String imageLocation, String get) throws UserException {
        return false;
    }

    @Override
    public void authorizeRole(int tenantId, String registryAnonnymousRoleName, String imageLocation, String get) throws UserException {

    }

    @Override
    public String getAdminUsername() throws UserException {
        return null;
    }

    @Override
    public String getAdminUsername(int tenantId) throws UserException {
        return null;
    }

    @Override
    public String getUserClaimValue(int tenantId, String tenantAwareUsername, String claimUri, String profileName) throws UserException {
        return null;
    }

    @Override
    public boolean isExistingUser(int tenantId, String username) throws UserException {
        return false;
    }

    @Override
    public String extractDomainFromName(String userId) {
        return "carbon.super";
    }

    @Override
    public boolean isExistingRole(int tenantId, String role) throws UserException {
        return false;
    }

    @Override
    public void updateRoleListOfUser(int tenantId, String username, String[] deletedRoles, String[] newRoles) throws UserException {

    }

    @Override
    public void deleteUser(int tenantId, String userName) throws UserException {

    }

    @Override
    public String[] getUserList(int tenantId, String claimUri, String claimValue, String profile) throws UserException {
        return new String[0];
    }

    @Override
    public boolean isUserAuthorized(int tenantId, String username, String permission, String action) throws UserException {
        return false;
    }

    @Override
    public String getAdminPassword(int tenantId) throws UserException {
        return null;
    }

    @Override
    public boolean isUserInRole(int tenantId, String username, String roleName) throws UserException {
        return false;
    }

    @Override
    public String[] getAllowedRolesForResource(int tenantId, String resourcePath, String action) throws UserException {
        return new String[0];
    }

    @Override
    public Set<String> getTenantDomainsByState(String state) throws UserException {
        return null;
    }

    @Override
    public String getDomainFromThreadLocal() {
        return null;
    }

    @Override
    public boolean authenticate(String username, String password) throws UserException {
        return false;
    }

    @Override
    public String getProperty(int tenantId, String propertyName) throws UserException {
        return null;
    }

    @Override
    public void changePasswordByUser(String username, String currentPassword, String newPassword) throws UserException {

    }

    @Override
    public void createRole(String roleName, Map<String, String> permissions, int tenantId) throws UserException {

    }

    @Override
    public String getPropertyFromFile(String propertyName) throws UserException {
        return null;
    }

    @Override
    public SortedMap<String, String> getClaims(String username, int tenantId, String dialectURI) throws UserException {
        return null;
    }

    @Override
    public String getClaimDisplayName(String claimURI, String username, int tenantId) throws UserException {
        return null;
    }

    @Override
    public String getTenantDomain(String username) throws UserException {
        return "carbon.super";
    }

    @Override
    public String getTenantAwareUsername(String username) throws UserException {
        return "carbon.super";
    }
}
