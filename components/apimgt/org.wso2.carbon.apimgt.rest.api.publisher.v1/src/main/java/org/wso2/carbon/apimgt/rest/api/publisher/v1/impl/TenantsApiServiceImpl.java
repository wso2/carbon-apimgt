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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.TenantsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.TenantsApiServiceCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TenantListDTO;

import javax.ws.rs.core.Response;

/**
 * This is the rest api implementation class for tenant related operations
 */
public class TenantsApiServiceImpl implements TenantsApiService {

    /**
     * Check whether the given role exists in the system
     *
     * @param tenantDomain Tenant domain
     * @return 200 if the given tenant exists
     */
    @Override
    public Response getTenantExistence(String tenantDomain, MessageContext messageContext) throws APIManagementException {

        TenantsApiServiceCommonImpl.getTenantExistence(tenantDomain);
        return Response.status(Response.Status.OK).build();
    }

    /**
     * This is used to get the tenants using its state
     *
     * @param state          Tenant state either active or inactive
     * @param limit          Maximum number returned at once
     * @param offset         Starting index
     * @param messageContext
     * @return List of tenant domains
     */
    @Override
    public Response getTenantsByState(String state, Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {

        TenantListDTO tenantList = TenantsApiServiceCommonImpl.getTenantsByState(state, limit, offset);
        return Response.ok().entity(tenantList).build();
    }
}
