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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * Used to retrieve and update tenant-config in deployment.
 */
public class TenantConfigApiServiceImpl implements TenantConfigApiService {

    public Response exportTenantConfig(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantConfig = apiAdmin.getTenantConfig(RestApiCommonUtil.getLoggedInUserTenantDomain());
        return Response.ok().entity(tenantConfig)
                .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
    }

    public Response updateTenantConfig(String body, MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        apiAdmin.updateTenantConfig(RestApiCommonUtil.getLoggedInUserTenantDomain(), body);
        return Response.ok().entity(body)
                .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
    }
}
