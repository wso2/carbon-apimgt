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

package org.wso2.carbon.apimgt.impl.restapi.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Base64;

public class MeApiServiceImplUtils {

    private static final Log log = LogFactory.getLog(MeApiServiceImplUtils.class);

    private MeApiServiceImplUtils() {
    }

    /**
     * @param roleId   Base64 URL encoded form of role name -Base64URLEncode{user-store-name/role-name}
     * @param userName username of user
     * @return whether the user has the role
     * @throws APIManagementException when UserStoreException is caught
     */
    public static boolean checkUserInRole(String roleId, String userName) throws APIManagementException {
        boolean isUserInRole = false;
        String roleName = new String(Base64.getUrlDecoder().decode(roleId));
        log.debug("Checking whether user :" + userName + " has role : " + roleName);
        try {
            isUserInRole = APIUtil.checkIfUserInRole(userName, roleName);
            return isUserInRole;
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while validating use role",
                    ExceptionCodes.from(ExceptionCodes.USERSTORE_INITIALIZATION_FAILED));
        }
    }
}
