/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantThemeApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class TenantThemeApiServiceImpl implements TenantThemeApiService {

    private static final Log log = LogFactory.getLog(TenantThemeApiServiceImpl.class);

    /**
     * Import a Tenant Theme for a particular tenant by uploading an archive file.
     *
     * @param fileInputStream content relevant to the tenant theme
     * @param fileDetail      file details as Attachment
     * @param messageContext
     * @return Theme import response
     * @throws APIManagementException if an error occurs when importing a tenant theme
     */
    @Override
    public Response importTenantTheme(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            String errorMessage = "Super Tenant " + MultitenantConstants.SUPER_TENANT_DOMAIN_NAME +
                    " is not allowed to import a tenant theme";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_IMPORT_NOT_ALLOWED,
                            MultitenantConstants.SUPER_TENANT_DOMAIN_NAME));
        }

        try {
            RestApiAdminUtils.importTenantTheme(fileInputStream, tenantDomain);
            return Response.status(Response.Status.OK).entity("Theme imported successfully").build();
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), e,
                    ExceptionCodes.from(ExceptionCodes.TENANT_THEME_IMPORT_FAILED, tenantDomain, e.getMessage()));
        }
    }

    /**
     * Export a Tenant Theme of a particular tenant as an archive file.
     *
     * @param messageContext
     * @return Theme export response
     * @throws APIManagementException if an error occurs when importing a tenant theme
     */
    @Override
    public Response exportTenantTheme(MessageContext messageContext) throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        APIAdminImpl apiAdmin = new APIAdminImpl();
        if (!apiAdmin.isTenantThemeExist(tenantId)) {
            RestApiUtil.handleResourceNotFoundError(
                    "Tenant Theme for tenant " + tenantDomain + " does not exist.", log);
        }

        InputStream tenantTheme = apiAdmin.getTenantTheme(tenantId);
        if (tenantTheme == null) {
            RestApiUtil.handleResourceNotFoundError(
                    "Tenant Theme for tenant " + tenantDomain + " does not exist.", log);
        }
        // The theme content is already held in memory, so it is streamed to the client directly rather than
        // being staged in a temporary file first
        return Response.ok(tenantTheme, MediaType.APPLICATION_OCTET_STREAM)
                .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                        + tenantDomain + APIConstants.ZIP_FILE_EXTENSION + "\"").build();
    }
}
