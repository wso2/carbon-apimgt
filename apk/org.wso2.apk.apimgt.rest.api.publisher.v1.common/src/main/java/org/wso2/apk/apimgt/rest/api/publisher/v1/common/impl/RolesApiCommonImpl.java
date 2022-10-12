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

package org.wso2.apk.apimgt.rest.api.publisher.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.Base64;

/**
 * Utility class for operations related to RoleAPIService
 */
public class RolesApiCommonImpl {

    private static final Log log = LogFactory.getLog(RolesApiCommonImpl.class);

    private RolesApiCommonImpl() {
        //To hide the default constructor
    }

    /**
     * Checks whether the role exists
     *
     * @param roleId Role ID
     * @throws APIManagementException If the roleId is invalid or the role does not exist
     */
    public static void validateSystemRole(String roleId) throws APIManagementException {

        if (roleId == null) {
            throw new APIManagementException(ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        String roleName = new String(Base64.getUrlDecoder().decode(roleId));
        log.debug("Checking whether the role : " + roleName + "exists");
        boolean roleNameExist = APIUtil.isRoleNameExist(username, roleName);
        if (!roleNameExist) {
            throw new APIManagementException(ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
    }
}
