package org.wso2.carbon.apimgt.rest.api.gateway;

import org.wso2.carbon.apimgt.rest.api.gateway.*;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApisApiService {
      public Response apisApiIdGet(String apiId, String tenantDomain, MessageContext messageContext) throws APIManagementException;
      public Response apisGet(String context, String version, String tenantDomain, MessageContext messageContext) throws APIManagementException;
}
