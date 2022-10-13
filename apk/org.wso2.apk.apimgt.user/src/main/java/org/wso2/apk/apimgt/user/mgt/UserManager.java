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

package org.wso2.apk.apimgt.user.mgt;

import org.wso2.apk.apimgt.user.exceptions.UserException;

import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

public interface UserManager {

    boolean isTenantAvailable(String tenantDomain) throws UserException;

    int getTenantId(String tenantDomain) throws UserException;

    void denyRole(int tenantId, String role, String resourcePath, String action) throws UserException;

    String[] getRoleListOfUser(int tenantId, String username) throws UserException;

    String[] getRoleListOfUser(String username) throws UserException;

    String addDomainToName(String username, String domainName);

    String getTenantDomainByTenantId(int tenantId) throws UserException;

    boolean isRoleAuthorized(int tenantId, String registryAnonnymousRoleName, String imageLocation, String get) throws UserException;

    void authorizeRole(int tenantId, String registryAnonnymousRoleName, String imageLocation, String get) throws UserException;

    String getAdminUsername() throws UserException;

    String getAdminUsername(int tenantId) throws UserException;

    String getUserClaimValue(int tenantId, String tenantAwareUsername, String claimUri, String profileName) throws UserException;

    boolean isExistingUser(int tenantId, String username) throws UserException;

    String extractDomainFromName(String userId);

    boolean isExistingRole(int tenantId, String role) throws UserException;

    void updateRoleListOfUser(int tenantId, String username, String[] deletedRoles, String[] newRoles) throws UserException;

    void deleteUser(int tenantId, String userName) throws UserException;

    String[] getUserList(int tenantId, String claimUri, String claimValue, String profile) throws UserException;

    boolean isUserAuthorized(int tenantId, String username, String permission, String action) throws UserException;

    String getAdminPassword(int tenantId) throws UserException;

    boolean isUserInRole(int tenantId, String username, String roleName) throws UserException;

    String[] getAllowedRolesForResource(int tenantId, String resourcePath, String action) throws UserException;

    Set<String> getTenantDomainsByState(String state) throws UserException;

    String getDomainFromThreadLocal();

    boolean authenticate(String username, String password) throws UserException;

    String getProperty(int tenantId, String propertyName) throws UserException;

    void changePasswordByUser(String username, String currentPassword, String newPassword) throws UserException;

    void createRole(String roleName, Map<String, String> permissions, int tenantId) throws UserException;

    String getPropertyFromFile(String propertyName) throws UserException;

    SortedMap<String, String> getClaims(String username, int tenantId, String dialectURI) throws UserException;

    String getClaimDisplayName(String claimURI, String username, int tenantId) throws UserException;

    String getTenantDomain(String username) throws UserException;

    String getTenantAwareUsername(String username) throws UserException;
}
