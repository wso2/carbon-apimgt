package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerProxyRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerValidationRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface McpServersApiService {
      public Response addCommentToMCPServer(String mcpServerId, CommentRequestDTO commentRequestDTO, String replyTo, MessageContext messageContext) throws APIManagementException;
      public Response addMCPServerDocument(String mcpServerId, DocumentDTO documentDTO, MessageContext messageContext) throws APIManagementException;
      public Response addMCPServerDocumentContent(String mcpServerId, String documentId, String ifMatch, InputStream fileInputStream, Attachment fileDetail, String inlineContent, MessageContext messageContext) throws APIManagementException;
      public Response changeMCPServerLifecycle(String action, String mcpServerId, String lifecycleChecklist, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response createMCPServerFromAPI(MCPServerDTO mcPServerDTO, String openAPIVersion, MessageContext messageContext) throws APIManagementException;
      public Response createMCPServerFromOpenAPI(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext) throws APIManagementException;
      public Response createMCPServerProxy(MCPServerProxyRequestDTO mcPServerProxyRequestDTO, MessageContext messageContext) throws APIManagementException;
      public Response createMCPServerRevision(String mcpServerId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext) throws APIManagementException;
      public Response createNewMCPServerVersion(String newVersion, String mcpServerId, Boolean defaultVersion, String serviceVersion, MessageContext messageContext) throws APIManagementException;
      public Response deleteCommentOfMCPServer(String commentId, String mcpServerId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServer(String mcpServerId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServerDocument(String mcpServerId, String documentId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServerRevision(String mcpServerId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response deployMCPServerRevision(String mcpServerId, String revisionId, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response editCommentOfMCPServer(String commentId, String mcpServerId, CommentRequestDTO commentRequestDTO, MessageContext messageContext) throws APIManagementException;
      public Response exportMCPServer(String mcpServerId, String name, String version, String revisionNumber, String providerName, String format, Boolean preserveStatus, Boolean latestRevision, String gatewayEnvironment, Boolean preserveCredentials, MessageContext messageContext) throws APIManagementException;
      public Response generateInternalAPIKeyMCPServer(String mcpServerId, MessageContext messageContext) throws APIManagementException;
      public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, String accept, MessageContext messageContext) throws APIManagementException;
      public Response getCommentOfMCPServer(String commentId, String mcpServerId, String xWSO2Tenant, String ifNoneMatch, Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServer(String mcpServerId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerBackend(String mcpServerId, String backendId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerBackends(String mcpServerId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocument(String mcpServerId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocumentContent(String mcpServerId, String documentId, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocuments(String mcpServerId, Integer limit, Integer offset, String accept, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerLifecycleHistory(String mcpServerId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerLifecycleState(String mcpServerId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevision(String mcpServerId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevisionDeployments(String mcpServerId, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRevisions(String mcpServerId, String query, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerSubscriptionPolicies(String mcpServerId, String xWSO2Tenant, String ifNoneMatch, Boolean isAiApi, String organizationID, MessageContext messageContext) throws APIManagementException;
      public Response getRepliesOfCommentOfMCPServer(String commentId, String mcpServerId, String xWSO2Tenant, Integer limit, Integer offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response importMCPServer(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider, Boolean rotateRevision, Boolean overwrite, Boolean preservePortalConfigurations, Boolean dryRun, String accept, MessageContext messageContext) throws APIManagementException;
      public Response restoreMCPServerRevision(String mcpServerId, String revisionId, MessageContext messageContext) throws APIManagementException;
      public Response undeployMCPServerRevision(String mcpServerId, String revisionId, String revisionNumber, Boolean allEnvironments, List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServer(String mcpServerId, MCPServerDTO mcPServerDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServerBackend(String mcpServerId, String backendId, BackendDTO backendDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServerDeployment(String mcpServerId, String deploymentId, APIRevisionDeploymentDTO apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException;
      public Response updateMCPServerDocument(String mcpServerId, String documentId, DocumentDTO documentDTO, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response validateMCPServer(String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response validateMCPServerEndpoint(String endpointUrl, String apiId, MessageContext messageContext) throws APIManagementException;
      public Response validateOpenAPIDefinitionOfMCPServer(Boolean returnContent, String url, InputStream fileInputStream, Attachment fileDetail, String inlineAPIDefinition, MessageContext messageContext) throws APIManagementException;
      public Response validateThirdPartyMCPServer(MCPServerValidationRequestDTO mcPServerValidationRequestDTO, MessageContext messageContext) throws APIManagementException;
}
