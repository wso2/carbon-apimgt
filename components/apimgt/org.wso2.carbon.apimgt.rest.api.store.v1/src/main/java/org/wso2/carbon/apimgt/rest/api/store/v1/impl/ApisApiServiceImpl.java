/*
 * Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.impl.ApisServiceImpl;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApisApiService;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIListDTO apiListDTO = ApisServiceImpl.getAPIList(limit, offset, query, organization);
        return Response.ok().entity(apiListDTO).build();
    }

    @Override
    public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        return Response.ok().entity(ApisServiceImpl.getAPIByAPIId(apiId, organization)).build();
    }

    /**
     * Get complexity details of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with complexity details of the GraphQL API
     */
    @Override
    public Response apisApiIdGraphqlPoliciesComplexityGet(String apiId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO = ApisServiceImpl.getGraphqlPoliciesComplexity(
                apiId, organization);
        return Response.ok().entity(graphQLQueryComplexityInfoDTO).build();
    }

    @Override
    public Response apisApiIdGraphqlPoliciesComplexityTypesGet(String apiId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        GraphQLSchemaTypeListDTO graphQLSchemaTypeListDTO = ApisServiceImpl.getGraphqlPoliciesComplexityTypes(
                apiId, organization);
        return Response.ok().entity(graphQLSchemaTypeListDTO).build();
    }

    @Override
    public Response apisApiIdGraphqlSchemaGet(String apiId, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String graphQLSchema = ApisServiceImpl.getGraphqlSchemaDefinition(apiId, organization);
        return Response.ok().entity(graphQLSchema).build();
    }

    @Override
    public Response addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo,
            MessageContext messageContext) throws APIManagementException {
        URI uri;
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        CommentDTO commentDTO = ApisServiceImpl.addCommentToAPI(apiId, postRequestBodyDTO, replyTo,
                organization);

        if (commentDTO != null) {
            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentDTO.getId();
            try {
                uri = new URI(uriString);
            } catch (URISyntaxException e) {
                throw new APIManagementException("Error while retrieving comment content location for API " + apiId);
            }
            return Response.created(uri).entity(commentDTO).build();
        }
        return null;
    }

    @Override
    public Response getAllCommentsOfAPI(String apiId, String xWSO2Tenant, Integer limit, Integer offset,
            Boolean includeCommenterInfo, MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            CommentListDTO commentDTO = ApisServiceImpl.getAllCommentsOfAPI(apiId, organization, limit, offset,
                    includeCommenterInfo);
            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getCommentOfAPI(String commentId, String apiId, String xWSO2Tenant, String ifNoneMatch,
            Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            CommentDTO commentDTO = ApisServiceImpl.getCommentOfAPI(commentId, apiId, organization,
                    includeCommenterInfo, replyLimit, replyOffset);
            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getRepliesOfComment(String commentId, String apiId, String xWSO2Tenant, Integer limit,
            Integer offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            CommentListDTO commentDTO = ApisServiceImpl.getRepliesOfComment(commentId, apiId, limit,
                    organization, offset, includeCommenterInfo);
            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response editCommentOfAPI(String commentId, String apiId, PatchRequestBodyDTO patchRequestBodyDTO,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            CommentDTO commentDTO = ApisServiceImpl.editCommentOfAPI(commentId, apiId, organization,
                    patchRequestBodyDTO.getCategory(), patchRequestBodyDTO.getContent());

            if (commentDTO != null) {
                String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
                URI uri = new URI(uriString);
                return Response.ok(uri).entity(commentDTO).build();
            } else {
                return Response.ok().build();
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response deleteComment(String commentId, String apiId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        JSONObject obj = ApisServiceImpl.deleteComment(commentId, apiId, organization, tokenScopes);
        return Response.ok(obj).type(MediaType.APPLICATION_JSON).build();
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            DocumentationContent docContent = ApisServiceImpl.getDocumentContent(apiId, documentId,
                    organization);
            // gets the content depending on the type of the document
            if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.FILE)) {
                String contentType = docContent.getResourceFile().getContentType();
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = docContent.getResourceFile().getName();
                String contentDisposition = RestApiConstants.FILE_HEADER_CONTENT_DISPOSITION + name + "\"";
                return Response.ok(docContent.getResourceFile().getContent())
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, contentDisposition).build();
            } else if (docContent.getSourceType()
                    .equals(DocumentationContent.ContentSourceType.INLINE) || docContent.getSourceType()
                    .equals(DocumentationContent.ContentSourceType.MARKDOWN)) {
                String content = docContent.getTextContent();
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE)
                        .build();
            } else if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.URL)) {
                String sourceUrl = docContent.getTextContent();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String xWSO2Tenant,
            String ifModifiedSince, MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        DocumentDTO documentDTO = ApisServiceImpl.getDocumentation(apiId, documentId, organization);
        return Response.ok().entity(documentDTO).build();
    }

    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        DocumentListDTO documentListDTO = ApisServiceImpl.getDocumentationList(apiId, offset, limit,
                organization);
        return Response.ok().entity(documentListDTO).build();
    }

    @Override
    public Response apisApiIdRatingsGet(String id, Integer limit, Integer offset, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        RatingListDTO ratingListDTO = ApisServiceImpl.getAPIRating(id, limit, offset, organization);
        return Response.ok().entity(ratingListDTO).build();
    }

    /**
     * Rest api implementation to downloading the client sdk for given api in given sdk language.
     *
     * @param apiId          : The id of the api.
     * @param language       : Preferred sdk language.
     * @param messageContext : messageContext
     * @return : The sdk as a zip archive.
     */
    @Override
    public Response apisApiIdSdksLanguageGet(String apiId, String language, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        if (StringUtils.isEmpty(apiId) || StringUtils.isEmpty(language)) {
            String message = "Error generating the SDK. API id or language should not be empty";
            throw new APIManagementException(message, ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        Map<String, String> sdkArtifacts = ApisServiceImpl.getSdkArtifacts(apiId, language, organization);
        File sdkFile = new File(sdkArtifacts.get("zipFilePath"));
        return Response.ok(sdkFile, MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .header("Content-Disposition", "attachment; filename=\"" + sdkArtifacts.get("zipFileName") + "\"")
                .build();
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId           API identifier
     * @param environmentName name of the gateway environment
     * @param ifNoneMatch     If-None-Match header value
     * @param xWSO2Tenant     requested tenant domain for cross tenant invocations
     * @param messageContext  CXF message context
     * @return Swagger document of the API for the given cluster or gateway environment
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String environmentName, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String apiSwagger = ApisServiceImpl.getOpenAPIDefinitionForEnvironment(apiId, environmentName,
                organization);
        return Response.ok().entity(apiSwagger)
                .header("Content-Disposition", "attachment; filename=\"" + "swagger.json" + "\"").build();
    }

    @Override
    public Response apisApiIdThumbnailGet(String apiId, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ResourceFile thumbnailResource = ApisServiceImpl.getThumbnail(apiId, organization);

        if (thumbnailResource != null) {
            return Response.ok(thumbnailResource.getContent(), MediaType.valueOf(thumbnailResource.getContentType()))
                    .build();
        } else {
            return Response.noContent().build();
        }
    }

    @Override
    public Response apisApiIdTopicsGet(String apiId, String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {
        TopicListDTO topicListDTO = null;
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        if (StringUtils.isNotEmpty(apiId)) {
            topicListDTO = ApisServiceImpl.getAPITopicList(apiId, organization);
        } else {
            throw new APIManagementException(ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        return Response.ok().entity(topicListDTO).build();
    }

    @Override
    public Response apisApiIdUserRatingPut(String id, RatingDTO body, String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        RatingDTO ratingDTO = ApisServiceImpl.updateUserRating(id, body, organization);
        return Response.ok().entity(ratingDTO).build();
    }

    @Override
    public Response apisApiIdUserRatingGet(String id, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        RatingDTO ratingDTO = ApisServiceImpl.getUserRating(id, organization);
        return Response.ok().entity(ratingDTO).build();
    }

    @Override
    public Response apisApiIdUserRatingDelete(String apiId, String xWSO2Tenant, String ifMatch,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApisServiceImpl.deleteAPIUserRating(apiId, organization);
        return Response.ok().build();
    }

    @Override
    public Response getWSDLOfAPI(String apiId, String environmentName, String ifNoneMatch, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        API api = ApisServiceImpl.getAPI(apiId, organization);
        ResourceFile wsdl = ApisServiceImpl.getWSDLOfAPI(api, environmentName, organization);
        return RestApiUtil.getResponseFromResourceFile(api.getId().toString(), wsdl);
    }

    @Override
    public Response apisApiIdSubscriptionPoliciesGet(String apiId, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        List<Tier> tierList = ApisServiceImpl.getSubscriptionPolicies(apiId, organization);
        return Response.ok().entity(tierList).build();
    }
}
