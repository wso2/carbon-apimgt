package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface McpServersApiService {
      public Response addMCPServerDocument(String apiId, DocumentDTO documentDTO, MessageContext messageContext) throws APIManagementException;
      public Response addMCPServerDocumentContent(String apiId, String documentId, String ifMatch, InputStream fileInputStream, Attachment fileDetail, String inlineContent, MessageContext messageContext) throws APIManagementException;
      public Response changeMCPServerLifecycle(String action, String apiId, String lifecycleChecklist, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response createMCPServer(APIDTO APIDTO, String openAPIVersion, MessageContext messageContext) throws APIManagementException;
      public Response createMCPServerRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext) throws APIManagementException;
      public Response createNewMCPServerVersion(String newVersion, String apiId, Boolean defaultVersion, String serviceVersion, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServer(String apiId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServerDocument(String apiId, String documentId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response deployMCPServerRevision(String apiId, String revisionId, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response exportMCPServer(String apiId, String name, String version, String revisionNumber, String providerName, String format, Boolean preserveStatus, Boolean latestRevision, String gatewayEnvironment, Boolean preserveCredentials, MessageContext messageContext) throws APIManagementException;
      public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, String accept, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServer(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocument(String apiId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocumentContent(String apiId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocuments(String apiId, Integer limit, Integer offset, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerEndpoint(String apiId, String endpointId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerEndpoints(String apiId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerLifecycleHistory(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerLifecycleState(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevisions(String apiId, String query, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerSubscriptionPolicies(String apiId, String xWSO2Tenant, String ifNoneMatch, Boolean isAiApi, String organizationID, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerSwagger(String apiId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response importMCPServer(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider, Boolean rotateRevision, Boolean overwrite, Boolean preservePortalConfigurations, Boolean dryRun, String accept, MessageContext messageContext) throws APIManagementException;
      public Response importMCPServerDefinition(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext) throws APIManagementException;
      public Response restoreMCPServerRevision(String apiId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response undeployMCPServerRevision(String apiId, String revisionId, String revisionNumber, Boolean allEnvironments, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServer(String apiId, APIDTO APIDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServerDocument(String apiId, String documentId, DocumentDTO documentDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServerEndpoint(String apiId, String endpointId, BackendEndpointDTO backendEndpointDTO, MessageContext messageContext) throws APIManagementException;
      public Response validateMCPServer(String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response validateMCPServerEndpoint(String endpointUrl, String apiId, MessageContext messageContext) throws APIManagementException;
      public Response validateOpenAPIDefinitionOfMCPServer(Boolean returnContent, String url, InputStream fileInputStream, Attachment fileDetail, String inlineAPIDefinition, MessageContext messageContext) throws APIManagementException;
}
