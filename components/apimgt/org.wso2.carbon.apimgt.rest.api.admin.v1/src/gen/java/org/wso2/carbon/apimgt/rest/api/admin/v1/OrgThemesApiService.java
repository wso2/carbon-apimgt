package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ContentPublishStatusResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface OrgThemesApiService {
      public Response deleteOrgTheme(String id, MessageContext messageContext) throws APIManagementException;
      public Response getOrgThemeContent(String id, MessageContext messageContext) throws APIManagementException;
      public Response getOrgThemes(Boolean publish, MessageContext messageContext) throws APIManagementException;
      public Response importOrgTheme(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException;
      public Response updateOrgThemeStatus(String id, ContentPublishStatusDTO contentPublishStatusDTO, MessageContext messageContext) throws APIManagementException;
}
