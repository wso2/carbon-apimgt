package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class ClientCertificatesApiServiceImpl implements ClientCertificatesApiService {
      public Response clientCertificatesAliasContentGet(String alias, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response clientCertificatesAliasDelete(String alias, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response clientCertificatesAliasGet(String alias, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response clientCertificatesAliasPut(String alias, InputStream certificateInputStream, Attachment certificateDetail, String tier, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response clientCertificatesGet(Integer limit, Integer offset, String alias, String apiId, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response clientCertificatesPost(InputStream certificateInputStream, Attachment certificateDetail, String alias, String apiId, String tier, SecurityContext securityContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
