package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import org.wso2.carbon.apimgt.api.APIManagementException;


import java.io.InputStream;

import javax.ws.rs.core.Response;


public interface TenantThemeApiService {
      public Response exportTenantTheme(MessageContext messageContext) throws APIManagementException;
      public Response importTenantTheme(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException;
}
