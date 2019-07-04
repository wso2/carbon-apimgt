package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

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
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApplicationsApiService {
      public Response applicationsApplicationIdDelete(String applicationId, String ifMatch, MessageContext messageContext);
      public Response applicationsApplicationIdGenerateKeysPost(String applicationId, ApplicationKeyGenerateRequestDTO body, MessageContext messageContext);
      public Response applicationsApplicationIdGet(String applicationId, String ifNoneMatch, MessageContext messageContext);
      public Response applicationsApplicationIdKeysGet(String applicationId, MessageContext messageContext);
      public Response applicationsApplicationIdKeysKeyTypeGenerateTokenPost(String applicationId, String keyType, ApplicationTokenGenerateRequestDTO body, String ifMatch, MessageContext messageContext);
      public Response applicationsApplicationIdKeysKeyTypeGet(String applicationId, String keyType, String groupId, MessageContext messageContext);
      public Response applicationsApplicationIdKeysKeyTypePut(String applicationId, String keyType, ApplicationKeyDTO body, MessageContext messageContext);
      public Response applicationsApplicationIdKeysKeyTypeRegenerateSecretPost(String applicationId, String keyType, MessageContext messageContext);
      public Response applicationsApplicationIdMapKeysPost(String applicationId, ApplicationKeyMappingRequestDTO body, MessageContext messageContext);
      public Response applicationsApplicationIdPut(String applicationId, ApplicationDTO body, String ifMatch, MessageContext messageContext);
      public Response applicationsApplicationIdScopesGet(String applicationId, Boolean filterByUserRoles, String ifNoneMatch, MessageContext messageContext);
      public Response applicationsGet(String groupId, String query, String sortBy, String sortOrder, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext);
      public Response applicationsPost(ApplicationDTO body, MessageContext messageContext);
}
