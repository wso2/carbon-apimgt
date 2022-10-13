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

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.Base64;

public class RolesCommonImpl {

    private static final Log log = LogFactory.getLog(RolesCommonImpl.class);

    private RolesCommonImpl() {
    }

    /**
     * Check whether the given role exists in the system
     *
     * @param roleId Base64 URL encoded form of role name - Base64URLEncode{user-store-name/role-name}
     * @return Whether the role exists or not
     */
    public static boolean validateSystemRole(String roleId) {
        boolean isRoleExist = false;
        String username = RestApiCommonUtil.getLoggedInUsername();
        if (roleId != null) {
            String roleName = new String(Base64.getUrlDecoder().decode(roleId));
            if (log.isDebugEnabled()) {
                log.debug("Checking whether the role: " + roleName + " exists");
            }
            isRoleExist = APIUtil.isRoleNameExist(username, roleName);
        }
        return isRoleExist;
    }
}
