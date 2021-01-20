/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.TenantsApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.APIConstants.TENANT_STATE_ACTIVE;
import static org.wso2.carbon.apimgt.impl.APIConstants.TENANT_STATE_INACTIVE;

/**
 * This is the rest api implementation class for tenant related operations
 */
public class TenantsApiServiceImpl implements TenantsApiService {

    private static final Log log = LogFactory.getLog(TenantsApiServiceImpl.class);

    /**
     * This is used to get the tenants using its state
     * @param state Tenant state either active or inactive
     * @param limit  Maximum number returned at once
     * @param offset Starting index
     * @param messageContext
     * @return List of tenant domains
     */
    @Override
    public Response tenantsGet(String state, Integer limit, Integer offset, MessageContext messageContext) {
        try {
            List<TenantDTO> tenantDTOList = new ArrayList<>();
            Integer paginationLimit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            Integer paginationOffset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
            PaginationDTO paginationDTO = new PaginationDTO();
            paginationDTO.setOffset(paginationOffset);
            paginationDTO.setLimit(paginationLimit);

            if (!state.equalsIgnoreCase(TENANT_STATE_ACTIVE) && !state.equalsIgnoreCase(TENANT_STATE_INACTIVE)) {
                RestApiUtil.handleBadRequest("Invalid tenant state", log);
            }

            String status = TENANT_STATE_ACTIVE.equalsIgnoreCase(state) ? TENANT_STATE_ACTIVE : TENANT_STATE_INACTIVE;
            Set<String> tenantDomains = APIUtil.getTenantDomainsByState(state);
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
            return Response.ok().entity(tenantList).build();
        } catch (UserStoreException e) {
            RestApiUtil.handleInternalServerError("Error while getting active tenant domains", e, log);
        }
        return null;
    }
}
