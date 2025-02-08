package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import java.io.InputStream;
import javax.ws.rs.core.Response;




public class OrgThemesApiServiceImpl implements OrgThemesApiService {

    @Override
    public Response deleteOrgTheme(String id, MessageContext messageContext) throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        RestApiAdminUtils.deleteOrgTheme(id, tenantDomain);
        return Response.status(Response.Status.OK).entity("Theme deleted successfully").build();
    }

    @Override
    public Response getOrgThemeContent(String id, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    @Override
    public Response getOrgThemes(Boolean publish, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    @Override
    public Response importOrgTheme(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        RestApiAdminUtils.importDraftedOrgTheme(fileInputStream, tenantDomain);
        return Response.status(Response.Status.OK).entity("Theme imported successfully").build();
    }

    @Override
    public Response updateOrgThemeStatus(String id, ContentPublishStatusDTO contentPublishStatusDTO, MessageContext messageContext)
        throws APIManagementException {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            String action = contentPublishStatusDTO.getACTION().value();
            RestApiAdminUtils.updateOrgThemeStatusAsPublishedOrUnpublished(action, tenantDomain);
            return Response.status(Response.Status.OK).entity("Status updated successfully").build();
    }
}
