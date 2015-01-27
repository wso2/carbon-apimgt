/**
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.impl.utils;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;

/**
 * This class utilizes an in-JVM OSGi API to retrieve all required authorization decisions corresponding to all methods
 * defined within AuthorizationManagerClient.
 */
public class StandaloneAuthorizationManagerClient implements AuthorizationManagerClient {

    private AuthorizationManager authorizationManager;
    private UserStoreManager userStoreManager;

    public StandaloneAuthorizationManagerClient() {
        try {
            this.userStoreManager = ServiceReferenceHolder.getUserRealm().getUserStoreManager();
            this.authorizationManager = ServiceReferenceHolder.getUserRealm().getAuthorizationManager();
        } catch (UserStoreException e) {
            throw new IllegalStateException("Error occurred while initializing Standalone " +
                    "Authorization Manager Client", e);
        }
    }

    @Override
    public boolean isUserAuthorized(String user, String permission) throws APIManagementException {
        try {
            return authorizationManager.isUserAuthorized(user, permission, CarbonConstants.UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error occurred while checking user permissions", e);
        }
    }

    @Override
    public String[] getRolesOfUser(String user) throws APIManagementException {
        try {
            return userStoreManager.getRoleListOfUser(user);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error occurred while retrieving the role list of user '" +
                    user + "'", e);
        }
    }

    @Override
    public String[] getRoleNames() throws APIManagementException {
        try {
            return userStoreManager.getRoleNames();
        } catch (UserStoreException e) {
            throw new APIManagementException("Error occurred while retrieving the list of role names", e);
        }
    }

}
