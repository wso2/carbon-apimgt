package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SharedScopeUsageDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ScopesApi.*;


public interface ScopesApiService {
      public Response addSharedScope(ScopeDTO body, MessageContext messageContext) throws APIManagementException;
      public Response deleteSharedScope(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getSharedScope(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getSharedScopeUsages(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getSharedScopes(Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response updateSharedScope(String scopeId, ScopeDTO body, MessageContext messageContext) throws APIManagementException;
      public Response validateScope(String scopeId, MessageContext messageContext) throws APIManagementException;
}
