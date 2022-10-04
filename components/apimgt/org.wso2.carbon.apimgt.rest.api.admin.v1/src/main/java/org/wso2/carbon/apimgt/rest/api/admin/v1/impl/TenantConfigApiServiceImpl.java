/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantConfigApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.TenantConfigCommonImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.ws.rs.core.Response;

/**
 * Used to retrieve and update tenant-config in deployment.
 */
public class TenantConfigApiServiceImpl implements TenantConfigApiService {

    @Override
    public Response exportTenantConfig(MessageContext messageContext) throws APIManagementException {
        String tenantConfig = TenantConfigCommonImpl.exportTenantConfig();
        return Response.ok().entity(tenantConfig)
                .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
    }

    @Override
    public Response updateTenantConfig(String body, MessageContext messageContext) throws APIManagementException {
        TenantConfigCommonImpl.updateTenantConfig(body);
        return Response.ok().entity(body)
                .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
    }
}
