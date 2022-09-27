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

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantThemeApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.TenantThemeCommonImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;

public class TenantThemeApiServiceImpl implements TenantThemeApiService {

    /**
     * Import a Tenant Theme for a particular tenant by uploading an archive file.
     *
     * @param fileInputStream content relevant to the tenant theme
     * @param fileDetail      file details as Attachment
     * @param messageContext  cxf message context
     * @return Theme import response
     * @throws APIManagementException if an error occurs when importing a tenant theme
     */
    @Override
    public Response importTenantTheme(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {
        TenantThemeCommonImpl.importTenantTheme(fileInputStream);
        return Response.status(Response.Status.OK).entity("Theme imported successfully").build();
    }

    /**
     * Export a Tenant Theme of a particular tenant as an archive file.
     *
     * @param messageContext cxf message context
     * @return Theme export response
     * @throws APIManagementException if an error occurs when importing a tenant theme
     */
    @Override
    public Response exportTenantTheme(MessageContext messageContext) throws APIManagementException {
        File tenantThemeArchive = TenantThemeCommonImpl.exportTenantTheme();
        return Response.ok(tenantThemeArchive, MediaType.APPLICATION_OCTET_STREAM)
                .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                        + tenantThemeArchive.getName() + "\"").build();
    }
}
