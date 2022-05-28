package org.wso2.carbon.apimgt.internal.service;

import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ScopesListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ScopesApiService {
      public Response scopesGet(String xWSO2Tenant, String scopeKey, MessageContext messageContext) throws APIManagementException;
}
