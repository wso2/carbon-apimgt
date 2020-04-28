package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class LabelsApiServiceImpl implements LabelsApiService {
  public Response labelsGet(MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
  public Response labelsLabelIdDelete(String labelId, String ifMatch, String ifUnmodifiedSince, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
  public Response labelsLabelIdPut(String labelId, LabelDTO body, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
  public Response labelsPost(LabelDTO body, MessageContext messageContext) {
      // remove errorObject and add implementation code!
      ErrorDTO errorObject = new ErrorDTO();
      Response.Status status  = Response.Status.NOT_IMPLEMENTED;
      errorObject.setCode((long) status.getStatusCode());
      errorObject.setMessage(status.toString());
      errorObject.setDescription("The requested resource has not been implemented");
      return Response.status(status).entity(errorObject).build();
  }
}
