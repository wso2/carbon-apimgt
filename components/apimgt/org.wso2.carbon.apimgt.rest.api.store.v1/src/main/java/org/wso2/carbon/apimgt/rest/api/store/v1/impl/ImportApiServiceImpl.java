package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ImportApiServiceImpl implements ImportApiService {
      public Response importApplicationsPost(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response importApplicationsPut(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
