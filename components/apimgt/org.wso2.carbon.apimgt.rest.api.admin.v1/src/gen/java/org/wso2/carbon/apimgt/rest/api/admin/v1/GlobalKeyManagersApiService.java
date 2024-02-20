package org.wso2.carbon.apimgt.rest.api.admin.v1;

import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface GlobalKeyManagersApiService {
      public Response globalKeyManagersGet(MessageContext messageContext) throws APIManagementException;
      public Response globalKeyManagersKeyManagerIdDelete(String keyManagerId, MessageContext messageContext) throws APIManagementException;
      public Response globalKeyManagersKeyManagerIdGet(String keyManagerId, MessageContext messageContext) throws APIManagementException;
      public Response globalKeyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO keyManagerDTO, MessageContext messageContext) throws APIManagementException;
      public Response globalKeyManagersPost(KeyManagerDTO keyManagerDTO, MessageContext messageContext) throws APIManagementException;
}
