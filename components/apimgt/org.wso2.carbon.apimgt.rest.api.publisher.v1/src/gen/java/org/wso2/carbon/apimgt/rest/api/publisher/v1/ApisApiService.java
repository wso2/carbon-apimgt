package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIExternalStoreListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevenueDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AuditReportDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertificatesDTO;
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
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface ApisApiService {
      public Response apisApiIdAmznResourceNamesGet(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdAuditapiGet(String apiId, String accept, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdClientCertificatesAliasContentGet(String apiId, String alias, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdClientCertificatesAliasDelete(String alias, String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdClientCertificatesAliasGet(String alias, String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdClientCertificatesAliasPut(String alias, String apiId, InputStream certificateInputStream, Attachment certificateDetail, String tier, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdClientCertificatesGet(String apiId, Integer limit, Integer offset, String alias, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdClientCertificatesPost(InputStream certificateInputStream, Attachment certificateDetail, String alias, String apiId, String tier, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDelete(String apiId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId, InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlSchemaGet(String apiId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdGraphqlSchemaPut(String apiId, String schemaDefinition, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdLifecycleHistoryGet(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdLifecycleStateGet(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdLifecycleStatePendingTasksDelete(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMediationPoliciesGet(String apiId, Integer limit, Integer offset, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMediationPoliciesMediationPolicyIdContentGet(String apiId, String mediationPolicyId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMediationPoliciesMediationPolicyIdContentPut(String type, String apiId, String mediationPolicyId, InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMediationPoliciesMediationPolicyIdDelete(String apiId, String mediationPolicyId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMediationPoliciesMediationPolicyIdGet(String apiId, String mediationPolicyId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMediationPoliciesPost(String type, String apiId, InputStream mediationPolicyFileInputStream, Attachment mediationPolicyFileDetail, String inlineContent, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMonetizationGet(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdMonetizePost(String apiId, APIMonetizationInfoDTO body, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdPut(String apiId, APIDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdResourcePathsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdResourcePoliciesGet(String apiId, String sequenceType, String resourcePath, String verb, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdResourcePoliciesResourcePolicyIdGet(String apiId, String resourcePolicyId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdResourcePoliciesResourcePolicyIdPut(String apiId, String resourcePolicyId, ResourcePolicyInfoDTO body, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdRevenueGet(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSubscriptionPoliciesGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSwaggerGet(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String url, InputStream fileInputStream, Attachment fileDetail, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisApiIdThumbnailGet(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisCopyApiPost(String newVersion, String apiId, Boolean defaultVersion, MessageContext messageContext) throws APIManagementException;
      public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, Boolean expand, String accept, MessageContext messageContext) throws APIManagementException;
      public Response apisImportGraphqlSchemaPost(String type, InputStream fileInputStream, Attachment fileDetail, String additionalProperties, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response apisPost(APIDTO body, String openAPIVersion, MessageContext messageContext) throws APIManagementException;
      public Response apisValidateGraphqlSchemaPost(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException;
      public Response getAllPublishedExternalStoresByAPI(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getWSDLOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response importOpenAPIDefinition(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext) throws APIManagementException;
      public Response importWSDLDefinition(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, String implementationType, MessageContext messageContext) throws APIManagementException;
      public Response publishAPIToExternalStores(String apiId, String externalStoreIds, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateWSDLOfAPI(String apiId, InputStream fileInputStream, Attachment fileDetail, String url, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response validateAPI(String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response validateDocument(String apiId, String name, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response validateEndpoint(String endpointUrl, String apiId, MessageContext messageContext) throws APIManagementException;
      public Response validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail, Boolean returnContent, MessageContext messageContext) throws APIManagementException;
      public Response validateWSDLDefinition(String url, InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException;
}
