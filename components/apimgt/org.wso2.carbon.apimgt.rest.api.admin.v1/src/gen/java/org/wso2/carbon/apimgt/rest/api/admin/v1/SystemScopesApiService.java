package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface SystemScopesApiService {
      public Response systemScopesGet(MessageContext messageContext) throws APIManagementException;
      public Response systemScopesScopeNameGet(String scopeName, String username, MessageContext messageContext) throws APIManagementException;
      public Response updateRolesForScope(ScopeListDTO body, MessageContext messageContext) throws APIManagementException;
}
