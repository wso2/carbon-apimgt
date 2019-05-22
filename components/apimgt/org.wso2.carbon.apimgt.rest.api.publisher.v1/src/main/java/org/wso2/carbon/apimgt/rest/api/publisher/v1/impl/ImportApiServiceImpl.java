package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ImportApiServiceImpl implements ImportApiService {
      public Response importApisPost(InputStream fileInputStream, Attachment fileDetail, String provider, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response importApisPut(InputStream fileInputStream, Attachment fileDetail, String provider, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
