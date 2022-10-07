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
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.TenantInfoDTO;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Base64;

public class TenantInfoCommonImpl {

    private static final Log log = LogFactory.getLog(TenantInfoCommonImpl.class);

    private TenantInfoCommonImpl() {
    }

    /**
     * Get tenant info by username
     *
     * @param username Username
     * @return Tenant info
     * @throws APIManagementException When an internal error occurs
     */
    public static TenantInfoDTO getTenantInfoByUsername(String username) throws APIManagementException {
        TenantInfoDTO tenantInfoDTO = new TenantInfoDTO();
        String decodedUserName;
        try {
            decodedUserName = new String(Base64.getDecoder().decode(username));
        } catch (IllegalArgumentException e) {
            log.warn("Could not decode the username. Using original username");
            decodedUserName = username;
        }
        if (!APIUtil.isUserExist(decodedUserName)) {
            throw new APIManagementException("Requested user " + decodedUserName + " does not exist",
                    ExceptionCodes.USER_DOES_NOT_EXIST);
        }

        String tenantDomain = MultitenantUtils.getTenantDomain(decodedUserName);
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        tenantInfoDTO.setTenantDomain(tenantDomain);
        tenantInfoDTO.setTenantId(tenantId);
        tenantInfoDTO.setUsername(decodedUserName);
        return tenantInfoDTO;
    }
}
