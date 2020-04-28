package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ExportApiServiceImpl implements ExportApiService {
  public Response exportApiGet(String name, String version, String format, String providerName, Boolean preserveStatus, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
  public Response exportApplicationsGet(String appName, String appOwner, Boolean withKeys, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
}
