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
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface KeyManagersApiService {
      public Response addNewKeyManager(KeyManagerDTO keyManagerDTO, MessageContext messageContext) throws APIManagementException;
      public Response getAllKeyManagers(MessageContext messageContext) throws APIManagementException;
      public Response getKeyManagerConfiguration(String keyManagerId, MessageContext messageContext) throws APIManagementException;
      public Response getWellKnownInfoKeyManager(String url, String type, MessageContext messageContext) throws APIManagementException;
      public Response removeKeyManager(String keyManagerId, MessageContext messageContext) throws APIManagementException;
      public Response updateKeyManager(String keyManagerId, KeyManagerDTO keyManagerDTO, MessageContext messageContext) throws APIManagementException;
}
