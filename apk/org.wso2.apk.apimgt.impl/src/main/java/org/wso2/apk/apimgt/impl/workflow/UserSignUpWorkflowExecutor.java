/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.apk.apimgt.impl.workflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.user.mgt.internal.UserManagerHolder;

import java.util.List;

public abstract class UserSignUpWorkflowExecutor extends WorkflowExecutor {

    private static final Log log = LogFactory.getLog(UserSignUpWorkflowExecutor.class);

    /**
     * Method updates Roles users with list of roles
     *
     * @param userName
     * @param tenantDomain
     * @param roleList
     * @throws Exception
     */
    protected static void updateRolesOfUser(String userName, List<String> roleList, String tenantDomain)
            throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Adding roles to " + userName + "in " + tenantDomain + " Domain");
        }
        int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);

        if (UserManagerHolder.getUserManager().isExistingUser(tenantId, userName)) {
            // check whether given roles exist
            for (String role : roleList) {
                if (!UserManagerHolder.getUserManager().isExistingRole(tenantId, role)) {
                    log.error("Could not find role " + role + " in the user store");
                    throw new Exception("Could not find role " + role + " in the user store");
                }
            }
            UserManagerHolder.getUserManager().updateRoleListOfUser(tenantId, userName, null,
                    roleList.toArray(new String[0]));
        } else {
            log.error("User does not exist. Unable to approve user " + userName);
        }

    }

    /**
     * Method to delete a user
     *
     * @param tenantDomain
     * @param userName
     * @throws Exception
     */
    protected static void deleteUser(String tenantDomain, String userName) throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Remove the rejected user :" + userName);
        }
        int tenantId = UserManagerHolder.getUserManager().getTenantId(tenantDomain);
        UserManagerHolder.getUserManager().deleteUser(tenantId, userName);
    }

}
