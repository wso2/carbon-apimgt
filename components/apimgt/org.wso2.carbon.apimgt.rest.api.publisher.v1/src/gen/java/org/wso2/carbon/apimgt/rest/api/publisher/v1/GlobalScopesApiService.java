package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface GlobalScopesApiService {
      public Response addGlobalScope(ScopeDTO body, MessageContext messageContext) throws APIManagementException;
      public Response deleteGlobalScope(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getGlobalScope(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getGlobalScopes(MessageContext messageContext) throws APIManagementException;
      public Response updateGlobalScope(String scopeId, ScopeDTO body, MessageContext messageContext) throws APIManagementException;
}
