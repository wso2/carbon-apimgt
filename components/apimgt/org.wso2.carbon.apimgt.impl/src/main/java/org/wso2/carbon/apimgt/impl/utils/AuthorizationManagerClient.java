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

import org.wso2.carbon.apimgt.api.APIManagementException;

/**
 * This interface abstracts out different AuthorizationManagerClient implementations available within the component
 * such as RemoteAuthorizationManagerClient which invokes an external authorization endpoint to retrieve authorization
 * decisions in a distributed setup as well as StandaloneAuthorizationManagerClient which invokes an in-JVM OSGi
 * service to perform the same.
 */
public interface AuthorizationManagerClient {

    /**
     * Checks if a user is authorized to perform a particular task by comparing a given permission against the user
     * who's performing the corresponding task.
     *
     * @param user          Current user who's attempting a particular task
     * @param permission    Permission associated with a given task.
     * @return              Boolean indicating whether the user is permitted to perform the task being attempted
     * @throws APIManagementException   If any unexpected error occurred while performing the permission check
     */
    boolean isUserAuthorized(String user, String permission) throws APIManagementException;

    /**
     * Retrieves the list of roles assigned to a particular user.
     *
     * @param user  Current user
     * @return      List of roles assigned to a the given user
     * @throws APIManagementException   If any unexpected error occurred while retrieving the list of roles
     */
    String[] getRolesOfUser(String user) throws APIManagementException;

    /**
     * Retrieves the list of applicable role names
     * @return  List of role names
     * @throws APIManagementException   If any unexpected error occurred while retrieving the list of role names
     */
    String[] getRoleNames() throws APIManagementException;

}
