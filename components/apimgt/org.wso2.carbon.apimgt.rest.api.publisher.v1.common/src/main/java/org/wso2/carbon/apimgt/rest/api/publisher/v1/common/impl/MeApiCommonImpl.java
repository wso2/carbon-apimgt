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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.user.exceptions.UserException;

import java.util.Base64;

/**
 * Utility class for operations related to MeApiService
 */
public class MeApiCommonImpl {

    private static final Log log = LogFactory.getLog(MeApiCommonImpl.class);

    private MeApiCommonImpl() {

    }

    /**
     * @param roleId Base64 URL encoded form of role name -Base64URLEncode{user-store-name/role-name}
     * @throws APIManagementException when UserStoreException is caught
     */
    public static void validateUserRole(String roleId) throws APIManagementException {

        if (roleId == null) {
            throw new APIManagementException(ExceptionCodes.ROLE_ID_EMPTY);
        }
        String userName = RestApiCommonUtil.getLoggedInUsername();

        String roleName = new String(Base64.getUrlDecoder().decode(roleId));
        log.debug("Checking whether user :" + userName.replaceAll("[\n\r\t]", "_") + " has role : "
                + roleName.replaceAll("[\n\r\t]", "_"));
        try {
            boolean isUserInRole = APIUtil.checkIfUserInRole(userName, roleName);
            if (!isUserInRole) {
                throw new APIManagementException(ExceptionCodes.ROLE_DOES_NOT_EXIST);
            }
        } catch (UserException e) {
            throw new APIManagementException("Error while validating user role", e,
                    ExceptionCodes.from(ExceptionCodes.USERSTORE_INITIALIZATION_FAILED));
        }
    }
}
