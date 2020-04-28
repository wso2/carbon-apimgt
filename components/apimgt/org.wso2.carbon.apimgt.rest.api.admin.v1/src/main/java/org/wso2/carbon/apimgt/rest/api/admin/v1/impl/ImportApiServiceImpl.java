package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.APIInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ImportApiServiceImpl implements ImportApiService {
  public Response importApiPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider, Boolean overwrite, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
  public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail, Boolean preserveOwner, Boolean skipSubscriptions, String appOwner, Boolean skipApplicationKeys, Boolean update, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
}
