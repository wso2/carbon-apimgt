package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.Multipart;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public interface McpServersApiService {
      public Response addCommentToMCPServer(String mcpServerId, PostRequestBodyDTO postRequestBodyDTO, String replyTo, MessageContext messageContext) throws APIManagementException;
      public Response addMCPServerRating(String mcpServerId, RatingDTO ratingDTO, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response deleteCommentOfMCPServer(String mcpServerId, String commentId, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response deleteMCPServerRating(String mcpServerId, String xWSO2Tenant, String ifMatch, MessageContext messageContext) throws APIManagementException;
      public Response editCommentOfMCPServer(String mcpServerId, String commentId, PatchRequestBodyDTO patchRequestBodyDTO, MessageContext messageContext) throws APIManagementException;
      public Response getAllCommentsOfMCPServer(String mcpServerId, String xWSO2Tenant, Integer limit, Integer offset, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getCommentOfMCPServer(String mcpServerId, String commentId, String xWSO2Tenant, String ifNoneMatch, Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServer(String mcpServerId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocument(String mcpServerId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocumentContent(String mcpServerId, String documentId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerDocuments(String mcpServerId, Integer limit, Integer offset, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerRating(String mcpServerId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerSDK(String mcpServerId, String language, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerSubscriptionPolicies(String mcpServerId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerSwagger(String mcpServerId, String environmentName, String ifNoneMatch, String xWSO2Tenant, String xWSO2TenantQ, String query, MessageContext messageContext) throws APIManagementException;
      public Response getMCPServerThumbnail(String mcpServerId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException;
      public Response getRepliesOfCommentOfMCPServer(String mcpServerId, String commentId, String xWSO2Tenant, Integer limit, Integer offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException;
      public Response mcpServersMcpServerIdRatingsGet(String mcpServerId, Integer limit, Integer offset, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException;
}
