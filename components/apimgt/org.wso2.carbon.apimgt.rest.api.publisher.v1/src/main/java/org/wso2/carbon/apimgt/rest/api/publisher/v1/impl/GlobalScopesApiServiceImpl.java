package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class GlobalScopesApiServiceImpl implements GlobalScopesApiService {
      public Response addGlobalScope(ScopeDTO body, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response deleteGlobalScope(String scopeId, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response getGlobalScope(String scopeId, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response getGlobalScopes(MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
      public Response updateGlobalScope(String scopeId, ScopeDTO body, MessageContext messageContext) {
      // do some magic!
      return Response.ok().entity("magic!").build();
  }
}
