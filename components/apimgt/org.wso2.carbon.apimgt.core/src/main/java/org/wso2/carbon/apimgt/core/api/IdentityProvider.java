/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.IdentityProviderException;
import org.wso2.carbon.apimgt.core.models.User;

import java.util.List;

/**
 * This Interface is providing functionality for identity provider operations.
 */
public interface IdentityProvider extends KeyManager {

    /**
     * Get the user ID of SCIM user.
     *
     * @param userName Username of user
     * @return the ID of the user
     * @throws IdentityProviderException if error occurred while getting user ID of user
     */
    public String getIdOfUser(String userName) throws IdentityProviderException;

    /**
     * Get the user Email of SCIM user.
     *
     * @param userId Email of user
     * @return the Email of the user
     * @throws IdentityProviderException if error occurred while getting user ID of user
     */
    public String getEmailOfUser(String userId) throws IdentityProviderException;

    /**
     * Get the role name list of a user.
     *
     * @param userId User Id
     * @return a list of role names
     * @throws IdentityProviderException if error occurred while getting roles of the user
     */
    public List<String> getRoleNamesOfUser(String userId) throws IdentityProviderException;

    /**
     * Validate role.
     *
     * @param roleName Role name
     * @return true if role is valid, else false.
     * @throws IdentityProviderException if error occurred while validation the role
     */
    public boolean isValidRole(String roleName) throws IdentityProviderException;

    /**
     * Get the role Id list of a user.
     *
     * @param userId User Id
     * @return a list of role Ids
     * @throws IdentityProviderException if error occurred while getting role Ids of the user
     */
    public List<String> getRoleIdsOfUser(String userId) throws IdentityProviderException;

    /**
     * Get role Id of given role.
     *
     * @param roleName Role name
     * @return the roleId if the role is available else null
     * @throws IdentityProviderException if error occurred while getting role Id for role name
     */
    public String getRoleId(String roleName) throws IdentityProviderException;

    /**
     * Get displayName of given role Id.
     *
     * @param roleId Role ID
     * @return the displayName of role if the role is available else null
     * @throws IdentityProviderException if error occurred while getting displayName for roleId
     */
    public String getRoleName(String roleId) throws IdentityProviderException;

    /**
     * Register a new user to the system
     *
     * @param user User to be registered
     * @throws IdentityProviderException if error occurred while registering the user
     */
    public void registerUser(User user) throws IdentityProviderException;

}

