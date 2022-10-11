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

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.CustomUrlInfoDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.CustomUrlInfoDevPortalDTO;
import org.wso2.carbon.apimgt.user.exceptions.UserException;

import java.util.Map;

public class CustomUrlsCommonImpl {

    private CustomUrlsCommonImpl() {
    }

    /**
     * Get custom urls of a tenant
     *
     * @param tenantDomain Tenant domain
     * @return Custom URL details
     * @throws APIManagementException When an internal error occurs
     */
    public static CustomUrlInfoDTO getCustomUrlInfoByTenantDomain(String tenantDomain) throws APIManagementException {
        try {
            boolean isTenantAvailable = APIUtil.isTenantAvailable(tenantDomain);
            if (!isTenantAvailable) {
                throw new APIManagementException(ExceptionCodes.INVALID_TENANT); // tenant does not exist
            }
            CustomUrlInfoDTO customUrlInfoDTO = new CustomUrlInfoDTO();
            boolean perTenantServiceProviderEnabled = APIUtil.isPerTenantServiceProviderEnabled(tenantDomain);
            if (perTenantServiceProviderEnabled) {
                Map<Object, Object> tenantBasedStoreDomainMapping = APIUtil.getTenantBasedStoreDomainMapping(tenantDomain);
                if (tenantBasedStoreDomainMapping != null) {
                    CustomUrlInfoDevPortalDTO customUrlInfoDevPortalDTO = new CustomUrlInfoDevPortalDTO();
                    customUrlInfoDevPortalDTO.setUrl((String) tenantBasedStoreDomainMapping.get("customUrl"));
                    customUrlInfoDTO.setDevPortal(customUrlInfoDevPortalDTO);
                }
            }
            customUrlInfoDTO.setTenantAdminUsername(APIUtil.getTenantAdminUserName(tenantDomain));
            customUrlInfoDTO.setEnabled(perTenantServiceProviderEnabled);
            customUrlInfoDTO.setTenantDomain(tenantDomain);
            return customUrlInfoDTO;
        } catch (UserException | APIManagementException e) {
            throw new APIManagementException("Error while retrieving custom url info for tenant : " + tenantDomain, e,
                    ExceptionCodes.from(ExceptionCodes.ERROR_GETTING_CUSTOM_URLS, tenantDomain));
        }
    }
}
