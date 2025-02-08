package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.io.FileUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.RestApiAdminUtils;
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
    public Response updateOrgThemeStatus(String id, ContentPublishStatusDTO contentPublishStatusDTO, MessageContext messageContext)
        throws APIManagementException {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            String action = contentPublishStatusDTO.getACTION().value();
            APIAdminImpl apiAdmin = new APIAdminImpl();
            apiAdmin.updateOrgThemeStatusAsPublishedOrUnpublished(tenantDomain, action);
            return Response.status(Response.Status.OK).entity("Status updated successfully").build();
    }
}
