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

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.PaginationDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.TenantDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.TenantListDTO;
import org.wso2.apk.apimgt.user.exceptions.UserException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.wso2.apk.apimgt.impl.APIConstants.TENANT_STATE_ACTIVE;
import static org.wso2.apk.apimgt.impl.APIConstants.TENANT_STATE_INACTIVE;

/**
 * Utility class for operations related to TenantApiService
 */
public class TenantsApiServiceCommonImpl {

    private TenantsApiServiceCommonImpl() {
        //To hide the default constructor
    }

    /**
     * Checks whether the tenant exists
     *
     * @param tenantDomain Tenant domain
     * @throws APIManagementException Error occurred while checking the tenant existence
     */
    public static void getTenantExistence(String tenantDomain) throws APIManagementException {

        if (tenantDomain == null) {
            throw new APIManagementException(ExceptionCodes.TENANT_NOT_FOUND);
        }
        boolean isTenantExist;
        try {
            isTenantExist = APIUtil.isTenantAvailable(tenantDomain);
        } catch (UserException e) {
            throw new APIManagementException("Error while getting checking if tenant exists", e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Error while getting checking if tenant exists"));
        }
        if (!isTenantExist) {
            throw new APIManagementException(ExceptionCodes.TENANT_NOT_FOUND);
        }
    }

    public static TenantListDTO getTenantsByState(String state, Integer limit, Integer offset)
            throws APIManagementException {

        List<TenantDTO> tenantDTOList = new ArrayList<>();
        Integer paginationLimit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        Integer paginationOffset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setOffset(paginationOffset);
        paginationDTO.setLimit(paginationLimit);

        if (!state.equalsIgnoreCase(TENANT_STATE_ACTIVE) && !state.equalsIgnoreCase(TENANT_STATE_INACTIVE)) {
            throw new APIManagementException(ExceptionCodes.INVALID_TENANT_STATE);
        }

        String status = TENANT_STATE_ACTIVE.equalsIgnoreCase(state) ? TENANT_STATE_ACTIVE : TENANT_STATE_INACTIVE;
        Set<String> tenantDomains;
        try {
            tenantDomains = APIUtil.getTenantDomainsByState(state);
        } catch (UserException e) {
            throw new APIManagementException("Error while getting active tenant domains", e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Error while getting active tenant domains"));
        }

        for (String domain : tenantDomains) {
            TenantDTO tenantDTO = new TenantDTO();
            tenantDTO.setDomain(domain);
            tenantDTO.setStatus(status);
            tenantDTOList.add(tenantDTO);
        }

        TenantListDTO tenantList = new TenantListDTO();
        tenantList.count(tenantDTOList.size());
        tenantList.setList(tenantDTOList);
        paginationDTO.setTotal(tenantDTOList.size());
        tenantList.setPagination(paginationDTO);
        return tenantList;
    }
}
