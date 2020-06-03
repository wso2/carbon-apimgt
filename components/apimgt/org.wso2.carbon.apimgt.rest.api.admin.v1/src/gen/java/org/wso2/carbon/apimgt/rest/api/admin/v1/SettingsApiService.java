package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RoleAliasDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RoleAliasListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SettingsDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface SettingsApiService {
      public Response settingsGet(MessageContext messageContext) throws APIManagementException;
      public Response settingsRoleAliasesGet(MessageContext messageContext) throws APIManagementException;
      public Response settingsRoleAliasesPost(MessageContext messageContext) throws APIManagementException;
      public Response settingsRoleAliasesRoleAliasDelete(String roleAlias, MessageContext messageContext) throws APIManagementException;
      public Response settingsScopesGet(MessageContext messageContext) throws APIManagementException;
      public Response settingsScopesScopeGet(String username, String scope, MessageContext messageContext) throws APIManagementException;
}
