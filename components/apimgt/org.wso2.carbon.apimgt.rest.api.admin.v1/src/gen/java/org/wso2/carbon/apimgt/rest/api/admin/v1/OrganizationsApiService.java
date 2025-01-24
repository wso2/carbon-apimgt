package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.OrganizationListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface OrganizationsApiService {
      public Response organizationsGet(MessageContext messageContext) throws APIManagementException;
      public Response organizationsOrganizationIdDelete(String organizationId, MessageContext messageContext) throws APIManagementException;
      public Response organizationsOrganizationIdGet(String organizationId, MessageContext messageContext) throws APIManagementException;
      public Response organizationsOrganizationIdPut(String organizationId, OrganizationDTO organizationDTO, MessageContext messageContext) throws APIManagementException;
      public Response organizationsPost(OrganizationDTO organizationDTO, MessageContext messageContext) throws APIManagementException;
}
