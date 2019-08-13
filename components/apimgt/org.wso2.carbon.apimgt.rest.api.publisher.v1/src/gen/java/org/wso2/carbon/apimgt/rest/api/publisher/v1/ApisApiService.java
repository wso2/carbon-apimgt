package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevenueDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePathListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApisApiService {
      public Response apisApiIdDelete(String apiId, String ifMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId, InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdGraphqlSchemaGet(String apiId, String accept, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdGraphqlSchemaPut(String apiId, String schemaDefinition, String ifMatch, MessageContext messageContext);
      public Response apisApiIdLifecycleHistoryGet(String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdLifecycleStateGet(String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdLifecycleStatePendingTasksDelete(String apiId, MessageContext messageContext);
      public Response apisApiIdMediationPoliciesGet(String apiId, Integer limit, Integer offset, String query, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdMediationPoliciesMediationPolicyIdDelete(String apiId, String mediationPolicyId, String ifMatch, MessageContext messageContext);
      public Response apisApiIdMediationPoliciesMediationPolicyIdGet(String apiId, String mediationPolicyId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdMediationPoliciesMediationPolicyIdPut(String apiId, String mediationPolicyId, MediationDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdMediationPoliciesPost(MediationDTO body, String apiId, String ifMatch, MessageContext messageContext);
      public Response apisApiIdMonetizationGet(String apiId, MessageContext messageContext);
      public Response apisApiIdMonetizePost(String apiId, APIMonetizationInfoDTO body, MessageContext messageContext);
      public Response apisApiIdPut(String apiId, APIDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdResourcePathsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdResourcePoliciesGet(String apiId, String sequenceType, String resourcePath, String verb, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdResourcePoliciesResourcePolicyIdGet(String apiId, String resourcePolicyId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdResourcePoliciesResourcePolicyIdPut(String apiId, String resourcePolicyId, ResourcePolicyInfoDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdRevenueGet(String apiId, MessageContext messageContext);
      public Response apisApiIdScopesGet(String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdScopesNameDelete(String apiId, String name, String ifMatch, MessageContext messageContext);
      public Response apisApiIdScopesNameGet(String apiId, String name, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdScopesNamePut(String apiId, String name, ScopeDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdScopesPost(String apiId, ScopeDTO body, String ifMatch, MessageContext messageContext);
      public Response apisApiIdSubscriptionPoliciesGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdSwaggerGet(String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String ifMatch, MessageContext messageContext);
      public Response apisApiIdThumbnailGet(String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdWsdlGet(String apiId, String ifNoneMatch, MessageContext messageContext);
      public Response apisApiIdWsdlPut(String apiId, InputStream fileInputStream, Attachment fileDetail, String ifMatch, MessageContext messageContext);
      public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist, String ifMatch, MessageContext messageContext);
      public Response apisCopyApiPost(String newVersion, String apiId, Boolean defaultVersion, MessageContext messageContext);
      public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, Boolean expand, String accept, String tenantDomain, MessageContext messageContext);
      public Response apisHead(String query, String ifNoneMatch, MessageContext messageContext);
      public Response apisImportGraphQLSchemaPost(String type, InputStream fileInputStream, Attachment fileDetail, String additionalProperties, String ifMatch, MessageContext messageContext);
      public Response apisPost(APIDTO body, MessageContext messageContext);
      public Response apisValidateGraphqlSchemaPost(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext);
      public Response importOpenAPIDefinition(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext);
      public Response importWSDLDefinition(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, String implementationType, MessageContext messageContext);
      public Response updateAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail, String ifMatch, MessageContext messageContext);
      public Response validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail, Boolean returnContent, MessageContext messageContext);
      public Response validateWSDLDefinition(String url, InputStream fileInputStream, Attachment fileDetail, Boolean returnContent, MessageContext messageContext);
}
