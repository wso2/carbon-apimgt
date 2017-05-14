/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * You can implement users and roles related functionality using this interface.
 */
public interface IdentityProvider extends KeyManager {

    /**
     * Get the role list of a user.
     *
     * @param userId User Id
     * @return a list of roles
     * @throws IdentityProviderException if error occurred while getting roles of the user
     */
    public List<String> getRolesOfUser(String userId) throws IdentityProviderException;

    /**
     * Validate role.
     *
     * @param roleName Role name
     * @return true if role is valid, else false.
     * @throws IdentityProviderException if error occurred while validation the role
     */
    public boolean isValidRole(String roleName) throws IdentityProviderException;

    /**
     * Register a new user to the system
     *
     * @param user User to be registered
     * @throws IdentityProviderException if error occurred while registering the user
     */
    public void registerUser(User user) throws IdentityProviderException;

}

