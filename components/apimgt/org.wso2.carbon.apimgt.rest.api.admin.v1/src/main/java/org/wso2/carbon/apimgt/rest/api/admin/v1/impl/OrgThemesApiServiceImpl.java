/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.*;
import org.wso2.carbon.apimgt.impl.dao.constants.DevPortalConstants;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.admin.v1.OrgThemesApiService;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusResponseDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

public class OrgThemesApiServiceImpl implements OrgThemesApiService {

    @Override
    public Response deleteOrgTheme(String id, MessageContext messageContext) throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIAdminImpl apiAdmin = new APIAdminImpl();
        apiAdmin.deleteOrgTheme(tenantDomain, id);
        return Response.status(Response.Status.OK).entity("Theme deleted successfully").build();
    }

    @Override
    public Response getOrgThemeContent(String id, MessageContext messageContext) throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIAdminImpl apiAdmin = new APIAdminImpl();
        InputStream orgTheme = apiAdmin.getOrgTheme(id, tenantDomain);
        String tempPath =
                System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + "exported-org-themes";
        String tempFile = tenantDomain + APIConstants.ZIP_FILE_EXTENSION;
        File orgThemeArchive = new File(tempPath, tempFile);

        try {
            FileUtils.copyInputStreamToFile(orgTheme, orgThemeArchive);
            return Response.ok(orgThemeArchive, MediaType.APPLICATION_OCTET_STREAM)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                            + orgThemeArchive.getName() + "\"").build();
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), e,
                    ExceptionCodes.from(ExceptionCodes.ORG_THEME_EXPORT_FAILED, tenantDomain, e.getMessage()));
        }
    }

    @Override
    public Response getOrgThemes(Boolean publish, MessageContext messageContext) throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIAdminImpl apiAdmin = new APIAdminImpl();
        List<ContentPublishStatusResponseDTO> responseList = new ArrayList<>();
        Map<String, String> themeMap = apiAdmin.getOrgThemes(tenantDomain);

        String draftedArtifact = themeMap.get("drafted");
        String publishedArtifact = themeMap.get("published");

        if (publish == null) {
            if (draftedArtifact != null) {
                responseList.add(new ContentPublishStatusResponseDTO().id(draftedArtifact).published(false));
            }
            if (publishedArtifact != null) {
                responseList.add(new ContentPublishStatusResponseDTO().id(publishedArtifact).published(true));
            }
        } else if (publish) {
            if (publishedArtifact != null) {
                responseList.add(new ContentPublishStatusResponseDTO().id(publishedArtifact).published(true));
            }
        } else {
            if (draftedArtifact != null) {
                responseList.add(new ContentPublishStatusResponseDTO().id(draftedArtifact).published(false));
            }
        }
        return Response.ok(responseList).build();
    }


    @Override
    public Response importOrgTheme(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIAdminImpl apiAdmin = new APIAdminImpl();
        apiAdmin.importDraftedOrgTheme(tenantDomain, fileInputStream);
        return Response.status(Response.Status.OK).entity("Theme imported successfully").build();
    }

    @Override
    public Response updateOrgThemeStatus(String id, ContentPublishStatusDTO contentPublishStatusDTO,
                                         MessageContext messageContext) throws APIManagementException {
        APIManagerConfiguration apiManagerConfiguration =
                ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService().getAPIManagerConfiguration();
        String portalType = apiManagerConfiguration.getFirstProperty(APIConstants.API_STORE_TYPE);

        if (DevPortalConstants.DEVPORTAL_V2.equals(portalType)) {
            DevPortalHandler devPortalHandler = DevPortalHandlerV2Impl.getInstance();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            String action = contentPublishStatusDTO.getAction().value();
            APIAdminImpl apiAdmin = new APIAdminImpl();
            try (InputStream content = apiAdmin.updateOrgThemeStatus(tenantDomain, action)) {
                if (DevPortalConstants.PUBLISH.equals(action)) {
                    devPortalHandler.publishOrgContent(tenantDomain, content);
                } else if (DevPortalConstants.UNPUBLISH.equals(action)) {
                    devPortalHandler.unpublishOrgContent(tenantDomain);
                }
            } catch (IOException e) {
                throw new APIManagementException("Failed to update API theme status", e);
            }
            return Response.status(Response.Status.OK).entity("Status updated successfully").build();
        } else {
            return Response.status(Response.Status.PRECONDITION_FAILED).entity("Please enable Next Gen Devportal " +
                    "to publish or unpublish").build();
        }
    }
}
