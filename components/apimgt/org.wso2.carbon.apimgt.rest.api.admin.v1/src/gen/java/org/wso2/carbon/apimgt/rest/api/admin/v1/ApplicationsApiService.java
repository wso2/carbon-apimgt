package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApplicationsApiService {
      public Response applicationsApplicationIdChangeOwnerPost(String owner, String applicationId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdGet(String applicationId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsGet(String user, Integer limit, Integer offset, String accept, String ifNoneMatch, String name, String tenantDomain, MessageContext messageContext) throws APIManagementException;
}
