package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIKeyRevokeRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyMappingRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeyReGenerateResponseDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApplicationsApiService {
      public Response applicationsApplicationIdApiKeysKeyTypeGeneratePost(String applicationId, String keyType, APIKeyGenerateRequestDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdApiKeysKeyTypeRevokePost(String applicationId, String keyType, APIKeyRevokeRequestDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdGet(String applicationId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdKeysGet(String applicationId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdKeysKeyTypeCleanUpPost(String applicationId, String keyType, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(String applicationId, String keyType, ApplicationTokenGenerateRequestDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdKeysKeyTypeGet(String applicationId, String keyType, String groupId, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdKeysKeyTypePut(String applicationId, String keyType, ApplicationKeyDTO body, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(String applicationId, String keyType, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdMapKeysPost(String applicationId, ApplicationKeyMappingRequestDTO body, MessageContext messageContext) throws APIManagementException;
      public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsGet(String groupId, String query, String sortBy, String sortOrder, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response applicationsPost(ApplicationDTO body, MessageContext messageContext) throws APIManagementException;
}
