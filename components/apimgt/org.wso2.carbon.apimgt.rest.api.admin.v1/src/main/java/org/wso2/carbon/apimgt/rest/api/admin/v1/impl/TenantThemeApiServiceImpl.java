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
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantThemeApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.TenantThemeImportManager;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;

import java.io.InputStream;

import javax.ws.rs.core.Response;

public class TenantThemeApiServiceImpl implements TenantThemeApiService {

    private static final Log log = LogFactory.getLog(ImportApiServiceImpl.class);

    /**
     * Import an Tenant Theme for a particular tenant by uploading an archive file.
     *
     * @param fileInputStream content relevant to the tenant theme
     * @param fileDetail      file details as Attachment
     * @param tenantDomain    tenant to which the theme is imported
     * @return Theme import response
     */
    @Override
    public Response tenantThemeImportPost(InputStream fileInputStream, Attachment fileDetail, String tenantDomain,
                                          MessageContext messageContext) {

        try {
            boolean isTenantAvailable = APIUtil.isTenantAvailable(tenantDomain);
            if (!isTenantAvailable) {
                // tenant does not exist
                String errorDescription = "The tenant " + tenantDomain + " does not exist";
                ErrorDTO errorObject = RestApiUtil
                        .getErrorDTO(RestApiConstants.STATUS_NOT_FOUND_MESSAGE_DEFAULT, 404L,
                                errorDescription);
                return Response.ok().entity(errorObject).build();
            }

            TenantThemeImportManager.deployTenantTheme(fileInputStream, tenantDomain);
            return Response.status(Response.Status.OK).entity("Theme imported successfully").build();
        } catch (UserStoreException | APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while importing tenant theme", e, log);
        }
        return null;
    }
}
