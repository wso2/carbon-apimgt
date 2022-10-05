/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.amazonaws.SdkClientException;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.ApisApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.OperationPoliciesApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIExternalStoreListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevenueDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AsyncAPISpecificationValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AuditReportDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CertificateInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ClientCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MockResponsePayloadListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePathListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TopicListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);
    public static final String FILE_DETAILS_MISSING = "File Details Missing";

    @Override
    public Response getAllAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String xWSO2Tenant,
                               String query, String ifNoneMatch, String accept,
                               MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        Object apiListDTO = ApisApiCommonImpl.getAllAPIs(limit, offset, sortBy, sortOrder, query, organization);

        if (APIConstants.APPLICATION_GZIP.equals(accept)) {
            File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
            return Response.ok().entity(zippedResponse)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment").
                    header("Content-Encoding", "gzip").build();
        } else {
            return Response.ok().entity(apiListDTO).build();
        }
    }

    @Override
    public Response createAPI(APIDTO apiDTO, String oasVersion, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO createdApiDTO = ApisApiCommonImpl.createAPI(apiDTO, oasVersion, organization);
        try {
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDTO.getProvider() + "-" +
                    apiDTO.getName() + "-" + apiDTO.getVersion();
            throw new APIManagementException(errorMessage, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public Response getAPI(String apiId, String xWSO2Tenant, String ifNoneMatch,
                           MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO apiToReturn = ApisApiCommonImpl.getAPI(apiId, organization);
        return Response.ok().entity(apiToReturn).build();
    }

    @Override
    public Response addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo, MessageContext
            messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        CommentDTO commentDTO = ApisApiCommonImpl.addCommentToAPI(apiId, postRequestBodyDTO, replyTo, organization);

        String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentDTO.getId();
        try {
            URI uri = new URI(uriString);
            return Response.created(uri).entity(commentDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public Response getAllCommentsOfAPI(String apiId, String xWSO2Tenant, Integer limit, Integer offset, Boolean
            includeCommenterInfo, MessageContext messageContext) throws APIManagementException {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        CommentListDTO commentDTO = ApisApiCommonImpl.getAllCommentsOfAPI(apiId, limit, offset, includeCommenterInfo,
                requestedTenantDomain);
        String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                RestApiConstants.RESOURCE_PATH_COMMENTS;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return Response.ok(uri).entity(commentDTO).build();
    }

    @Override
    public Response getCommentOfAPI(String commentId, String apiId, String xWSO2Tenant, String ifNoneMatch, Boolean
            includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext) throws
            APIManagementException {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        CommentDTO commentDTO = ApisApiCommonImpl.getCommentOfAPI(commentId, apiId, includeCommenterInfo,
                replyLimit, replyOffset, requestedTenantDomain);
        String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS
                + "/" + commentId;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return Response.ok(uri).entity(commentDTO).build();
    }

    @Override
    public Response getRepliesOfComment(String commentId, String apiId, String xWSO2Tenant, Integer limit, Integer
            offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws
            APIManagementException {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        CommentListDTO commentDTO = ApisApiCommonImpl.getRepliesOfComment(commentId, apiId, limit, offset,
                includeCommenterInfo, requestedTenantDomain);
        String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                RestApiConstants.RESOURCE_PATH_COMMENTS;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId,
                    ExceptionCodes.INTERNAL_ERROR);
        }
        return Response.ok(uri).entity(commentDTO).build();
    }

    @Override
    public Response editCommentOfAPI(String commentId, String apiId, PatchRequestBodyDTO patchRequestBodyDTO,
                                     MessageContext messageContext) throws APIManagementException {

        CommentDTO commentDTO = ApisApiCommonImpl.editCommentOfAPI(commentId, apiId, patchRequestBodyDTO);
        if (commentDTO == null) {
            return Response.ok().build();
        }

        String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + RestApiConstants.RESOURCE_PATH_COMMENTS
                + "/" + commentId;

        try {
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId,
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public Response deleteComment(String commentId, String apiId, String ifMatch, MessageContext messageContext) throws
            APIManagementException {

        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);

        JSONObject deleteConfirmationJSON = ApisApiCommonImpl.deleteComment(commentId, apiId, tokenScopes);
        return Response.ok(deleteConfirmationJSON).type(MediaType.APPLICATION_JSON).build();
    }

    /**
     * Get complexity details of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with complexity details of the GraphQL API
     */

    @Override
    public Response getGraphQLPolicyComplexityOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO =
                ApisApiCommonImpl.getGraphQLPolicyComplexityOfAPI(apiId, organization);
        return Response.ok().entity(graphQLQueryComplexityInfoDTO).build();
    }

    /**
     * Update complexity details of a given API
     *
     * @param apiId          apiId
     * @param body           GraphQLQueryComplexityInfo DTO as request body
     * @param messageContext message context
     * @return Response
     */

    @Override
    public Response updateGraphQLPolicyComplexityOfAPI(String apiId, GraphQLQueryComplexityInfoDTO body,
                                                       MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ApisApiCommonImpl.updateGraphQLPolicyComplexityOfAPI(apiId, body, organization, tokenScopes);
        return Response.ok().build();
    }

    @Override
    public Response updateTopics(String apiId, TopicListDTO topicListDTO, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ApisApiCommonImpl.updateTopics(apiId, topicListDTO, organization, tokenScopes);
        return Response.ok().build();
    }

    /**
     * Get GraphQL Schema of given API
     *
     * @param apiId          apiId
     * @param accept
     * @param ifNoneMatch    If--Match header value
     * @param messageContext message context
     * @return Response with GraphQL Schema
     */
    @Override
    public Response getAPIGraphQLSchema(String apiId, String accept, String ifNoneMatch,
                                        MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        GraphQLSchemaDTO dto = ApisApiCommonImpl.getAPIGraphQLSchema(apiId, organization);
        return Response.ok().entity(dto).build();
    }

    /**
     * Update GraphQL Schema
     *
     * @param apiId            api Id
     * @param schemaDefinition graphQL schema definition
     * @param ifMatch
     * @param messageContext
     * @return
     */
    @Override
    public Response updateAPIGraphQLSchema(String apiId, String schemaDefinition, String ifMatch,
                                           MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        APIDTO modifiedAPI = ApisApiCommonImpl.updateAPIGraphQLSchema(apiId, schemaDefinition, organization, tokenScopes);
        return Response.ok().entity(modifiedAPI.getOperations()).build();
    }

    @Override
    public Response updateAPI(String apiId, APIDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        APIDTO apiDTO = ApisApiCommonImpl.updateAPI(apiId, body, tokenScopes, organization);
        return Response.ok().entity(apiDTO).build();
    }

    /**
     * When the API is Published or Deprecated, only the users with scope "apim:api_import_export", "apim:api_publish", "apim:admin" will be allowed for
     * updating/deleting APIs or its sub-resources.
     *
     * @param status Status of the API which is currently created (current state)
     * @throws APIManagementException if update is not allowed
     */
    private void validateAPIOperationsPerLC(String status) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;
        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);

        for (String scope : tokenScopes) {
            if (RestApiConstants.PUBLISHER_SCOPE.equals(scope)
                    || RestApiConstants.API_IMPORT_EXPORT_SCOPE.equals(scope)
                    || RestApiConstants.API_MANAGE_SCOPE.equals(scope)
                    || RestApiConstants.ADMIN_SCOPE.equals(scope)) {
                updatePermittedForPublishedDeprecated = true;
                break;
            }
        }
        if (!updatePermittedForPublishedDeprecated && (
                APIConstants.PUBLISHED.equals(status)
                        || APIConstants.DEPRECATED.equals(status))) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.API_UPDATE_FORBIDDEN_PER_LC, status));
        }
    }

    /**
     * Get all types and fields of the GraphQL Schema of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with all the types and fields found within the schema definition
     */
    @Override
    public Response getGraphQLPolicyComplexityTypesOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        GraphQLSchemaTypeListDTO graphQLSchemaTypeListDTO =
                ApisApiCommonImpl.getGraphQLPolicyComplexityTypesOfAPI(apiId, organization);
        return Response.ok().entity(graphQLSchemaTypeListDTO).build();
    }

    // AWS Lambda: rest api operation to get ARNs
    @Override
    public Response getAmazonResourceNamesOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {

        JSONObject arns = new JSONObject();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            arns = ApisApiCommonImpl.getAmazonResourceNamesOfAPI(apiId, organization);
            return Response.ok().entity(arns.toString()).build();
        } catch (SdkClientException e) {
            if (e.getCause() instanceof UnknownHostException) {
                arns.put("error", "No internet connection to connect the given access method.");
                log.error("No internet connection to connect the given access method of API : " + apiId, e);
                return Response.serverError().entity(arns.toString()).build();
            } else {
                arns.put("error", "Unable to access Lambda functions under the given access method.");
                log.error("Unable to access Lambda functions under the given access method of API : " + apiId, e);
                return Response.serverError().entity(arns.toString()).build();
            }
        }
    }

    /**
     * Method to retrieve Security Audit Report
     *
     * @param apiId          API ID of the API
     * @param accept         Accept header string
     * @param messageContext Message Context string
     * @return Response object of Security Audit
     */
    @Override
    public Response getAuditReportOfAPI(String apiId, String accept, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        AuditReportDTO auditReportDTO = ApisApiCommonImpl.getAuditReportOfAPI(apiId, organization);
        return Response.ok().entity(auditReportDTO).build();
    }

    @Override
    public Response getAPIClientCertificateContentByAlias(String apiId, String alias, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String certFileName = alias + ".crt";
        Object certificate = ApisApiCommonImpl.getAPIClientCertificateContentByAlias(apiId, alias, organization);
        Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
        responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                RestApiConstants.CONTENT_DISPOSITION_ATTACHMENT_FILENAME + "\"" + certFileName + "\"");
        responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
        return responseBuilder.build();
    }

    @Override
    public Response deleteAPIClientCertificateByAlias(String alias, String apiId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ApisApiCommonImpl.deleteAPIClientCertificateByAlias(alias, apiId, organization, tokenScopes);
        return Response.ok().entity("The certificate for alias '" + alias + "' deleted successfully.").build();
    }

    @Override
    public Response getAPIClientCertificateByAlias(String alias, String apiId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        CertificateInfoDTO certificateInfo = ApisApiCommonImpl.getAPIClientCertificateByAlias(alias, apiId, organization);
        return Response.ok().entity(certificateInfo).build();
    }

    @Override
    public Response updateAPIClientCertificateByAlias(String alias, String apiId,
                                                      InputStream certificateInputStream,
                                                      Attachment certificateDetail, String tier,
                                                      MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);

        ContentDisposition contentDisposition;
        String fileName;
        String base64EncodedCert = null;
        if (certificateDetail != null) {
            contentDisposition = certificateDetail.getContentDisposition();
            fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isNotBlank(fileName)) {
                base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            }
        }
        if (StringUtils.isEmpty(base64EncodedCert) && StringUtils.isEmpty(tier)) {
            return Response.ok().entity("Client Certificate is not updated for alias " + alias).build();
        }

        ClientCertMetadataDTO clientCertMetadataDTO = ApisApiCommonImpl.updateAPIClientCertificateByAlias(alias, apiId,
                tier, organization, base64EncodedCert);
        try {
            URI updatedCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + "?alias=" + alias);
            return Response.ok(updatedCertUri).entity(clientCertMetadataDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while generating the resource location URI for alias '"
                    + alias + "'", ExceptionCodes.INTERNAL_ERROR);
        }
    }

    @Override
    public Response getAPIClientCertificates(String apiId, Integer limit, Integer offset, String alias,
                                             MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ClientCertificatesDTO certificatesDTO = ApisApiCommonImpl.getAPIClientCertificates(apiId, limit, offset, alias, organization);
        return Response.status(Response.Status.OK).entity(certificatesDTO).build();
    }

    @Override
    public Response addAPIClientCertificate(String apiId, InputStream certificateInputStream,
                                            Attachment certificateDetail, String alias, String tier,
                                            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String fileName = null;
        if (certificateDetail != null) {
            fileName = certificateDetail.getContentDisposition()
                    .getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
        }
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);

        ClientCertMetadataDTO certificateDTO =
                ApisApiCommonImpl.addAPIClientCertificate(apiId, certificateInputStream, alias, tier, organization,
                        fileName, tokenScopes);
        try {
            URI createdCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + "?alias=" + alias);
            return Response.created(createdCertUri).entity(certificateDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while generating the resource location URI for alias '"
                    + alias + "'", ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Delete API
     *
     * @param apiId   API Id
     * @param ifMatch If-Match header value
     * @return Status of API Deletion
     * @throws APIManagementException when API delete operation fails
     */
    @Override
    public Response deleteAPI(String apiId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);

        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        // Delete the API
        ApisApiCommonImpl.deleteAPI(apiId, organization);

        return Response.ok().build();
    }

    /**
     * Retrieves the content of a document
     *
     * @param apiId       API identifier
     * @param documentId  document identifier
     * @param ifNoneMatch If-None-Match header value
     * @return Content of the document/ either inline/file or source url as a redirection
     */
    @Override
    public Response getAPIDocumentContentByDocumentId(String apiId, String documentId,
                                                      String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            DocumentationContent docContent = ApisApiCommonImpl.getAPIDocumentContentByDocumentId(apiId, documentId,
                    organization);

            // gets the content depending on the type of the document
            if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.FILE)) {
                String contentType = docContent.getResourceFile().getContentType();
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = docContent.getResourceFile().getName();
                return Response.ok(docContent.getResourceFile().getContent())
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .build();
            } else if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.INLINE)
                    || docContent.getSourceType().equals(DocumentationContent.ContentSourceType.MARKDOWN)) {
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
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
        return null;
    }

    /**
     * Add content to a document. Content can be inline or File
     *
     * @param apiId         API identifier
     * @param documentId    document identifier
     * @param inputStream   file input stream
     * @param fileDetail    file details as Attachment
     * @param inlineContent inline content for the document
     * @param ifMatch       If-match header value
     * @return updated document as DTO
     */
    @Override
    public Response addAPIDocumentContent(String apiId, String documentId, String ifMatch,
                                          InputStream inputStream, Attachment fileDetail, String inlineContent, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String fileName = null;
        String mediaType = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            mediaType = fileDetail.getHeader(RestApiConstants.HEADER_CONTENT_TYPE);
        }

        DocumentDTO documentDTO = ApisApiCommonImpl.addAPIDocumentContent(apiId, documentId, inputStream, inlineContent,
                organization, fileName, mediaType);

        String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENT_CONTENT.replace(RestApiConstants.APIID_PARAM, apiId)
                .replace(RestApiConstants.DOCUMENTID_PARAM, documentId);
        try {
            URI uri = new URI(uriString);
            return Response.created(uri).entity(documentDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving document content location : " + documentId;
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * Deletes an existing document of an API
     *
     * @param apiId      API identifier
     * @param documentId document identifier
     * @param ifMatch    If-match header value
     * @return 200 response if deleted successfully
     */
    @Override
    public Response deleteAPIDocument(String apiId, String documentId, String ifMatch,
                                      MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        ApisApiCommonImpl.deleteAPIDocument(apiId, documentId, organization);
        return Response.ok().build();
    }

    @Override
    public Response getAPIDocumentByDocumentId(String apiId, String documentId, String ifNoneMatch,
                                               MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        DocumentDTO documentDTO = ApisApiCommonImpl.getAPIDocumentByDocumentId(apiId, documentId, organization);
        return Response.ok().entity(documentDTO).build();
    }

    /**
     * Updates an existing document of an API
     *
     * @param apiId      API identifier
     * @param documentId document identifier
     * @param body       updated document DTO
     * @param ifMatch    If-match header value
     * @return updated document DTO as response
     */
    @Override
    public Response updateAPIDocument(String apiId, String documentId, DocumentDTO body,
                                      String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        DocumentDTO documentDTO = ApisApiCommonImpl.updateAPIDocument(apiId, documentId, body, organization);

        return Response.ok().entity(documentDTO).build();
    }

    /**
     * Returns all the documents of the given API identifier that matches to the search condition
     *
     * @param apiId       API identifier
     * @param limit       max number of records returned
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return matched documents as a list if DocumentDTOs
     */
    @Override
    public Response getAPIDocuments(String apiId, Integer limit, Integer offset, String ifNoneMatch,
                                    MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        DocumentListDTO documentListDTO = ApisApiCommonImpl.getAPIDocuments(apiId, limit, offset, organization);
        return Response.ok().entity(documentListDTO).build();
    }

    /**
     * Add a documentation to an API
     *
     * @param apiId api identifier
     * @param body  Documentation DTO as request body
     * @return created document DTO as response
     */
    @Override
    public Response addAPIDocument(String apiId, DocumentDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        DocumentDTO newDocumentDTO = ApisApiCommonImpl.addAPIDocument(apiId, body, organization);
        String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                .replace(RestApiConstants.APIID_PARAM, apiId)
                .replace(RestApiConstants.DOCUMENTID_PARAM, newDocumentDTO.getDocumentId());

        try {
            URI uri = new URI(uriString);
            return Response.created(uri).entity(newDocumentDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving location for document " + body.getName() + " of API " + apiId;
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get external store list which the given API is already published to.
     *
     * @param apiId          API Identifier
     * @param ifNoneMatch    If-None-Match header value
     * @param messageContext CXF Message Context
     * @return External Store list of published API
     */
    @Override
    public Response getAllPublishedExternalStoresByAPI(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        APIExternalStoreListDTO apiExternalStoreListDTO = ApisApiCommonImpl.getAllPublishedExternalStoresByAPI(apiId);
        return Response.ok().entity(apiExternalStoreListDTO).build();
    }

    /**
     * Gets generated scripts
     *
     * @param apiId          API Id
     * @param ifNoneMatch    If-None-Match header value
     * @param messageContext message context
     * @return list of policies of generated sample payload
     * @throws APIManagementException
     */
    @Override
    public Response getGeneratedMockScriptsOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        MockResponsePayloadListDTO responsePayloadListDTO = ApisApiCommonImpl.getGeneratedMockScriptsOfAPI(apiId,
                organization);
        return Response.ok().entity(responsePayloadListDTO).build();
    }

    /**
     * Retrieves the WSDL meta information of the given API. The API must be a SOAP API.
     *
     * @param apiId          Id of the API
     * @param messageContext CXF Message Context
     * @return WSDL meta information of the API
     * @throws APIManagementException when error occurred while retrieving API WSDL meta info.
     *                                eg: when API doesn't exist, API exists but it is not a SOAP API.
     */
    @Override
    public Response getWSDLInfoOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        WSDLInfoDTO wsdlInfoDTO = ApisApiCommonImpl.getWSDLInfoOfAPI(apiId, organization);
        return Response.ok().entity(wsdlInfoDTO).build();
    }

    /**
     * Retrieves API Lifecycle history information
     *
     * @param apiId       API Id
     * @param ifNoneMatch If-None-Match header value
     * @return API Lifecycle history information
     */
    @Override
    public Response getAPILifecycleHistory(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LifecycleHistoryDTO lifecycleHistory = ApisApiCommonImpl.getAPILifecycleHistory(apiId, organization);
        return Response.ok().entity(lifecycleHistory).build();
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId       API Id
     * @param ifNoneMatch If-None-Match header value
     * @return API Lifecycle state information
     */
    @Override
    public Response getAPILifecycleState(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LifecycleStateDTO lifecycleStateDTO = ApisApiCommonImpl.getAPILifecycleState(apiId, organization);
        return Response.ok().entity(lifecycleStateDTO).build();
    }

    @Override
    public Response deleteAPILifecycleStatePendingTasks(String apiId, MessageContext messageContext)
            throws APIManagementException {

        ApisApiCommonImpl.deleteAPILifecycleStatePendingTasks(apiId);
        return Response.ok().build();
    }

    /**
     * Get API monetization status and monetized tier to billing plan mapping
     *
     * @param apiId          API ID
     * @param messageContext message context
     * @return API monetization status and monetized tier to billing plan mapping
     */
    @Override
    public Response getAPIMonetization(String apiId, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIMonetizationInfoDTO monetizationInfoDTO = ApisApiCommonImpl.getAPIMonetization(apiId, organization);
        return Response.ok().entity(monetizationInfoDTO).build();
    }

    /**
     * Monetize (enable or disable) for a given API
     *
     * @param apiId          API ID
     * @param body           request body
     * @param messageContext message context
     * @return monetizationDTO
     */
    @Override
    public Response addAPIMonetization(String apiId, APIMonetizationInfoDTO body, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        Map<String, String> monetizationProperties = body.getProperties();
        if (MapUtils.isNotEmpty(monetizationProperties)) {
            PublisherCommonUtils.validateMonetizationProperties(monetizationProperties);
        }
        APIMonetizationInfoDTO apiMonetizationInfo = ApisApiCommonImpl.addAPIMonetization(apiId, body, organization);
        return Response.ok().entity(apiMonetizationInfo).build();
    }

    /**
     * Add an API specific operation policy
     *
     * @param apiId                                  UUID of the API
     * @param policySpecFileInputStream              Input stream of the policy specification file
     * @param policySpecFileDetail                   Operation policy specification
     * @param synapsePolicyDefinitionFileInputStream Input stream of the synapse policy definition file
     * @param synapsePolicyDefinitionFileDetail      Synapse definition of the operation policy
     * @param ccPolicyDefinitionFileInputStream      Input stream of the choreo connect policy definition file
     * @param ccPolicyDefinitionFileDetail           Choreo connect definition of the operation policy
     * @param messageContext                         message context
     * @return Added operation policy DTO as response
     * @throws APIManagementException when adding a new API specific operation policy fails
     */
    @Override
    public Response addAPISpecificOperationPolicy(String apiId, InputStream policySpecFileInputStream,
                                                  Attachment policySpecFileDetail,
                                                  InputStream synapsePolicyDefinitionFileInputStream,
                                                  Attachment synapsePolicyDefinitionFileDetail,
                                                  InputStream ccPolicyDefinitionFileInputStream,
                                                  Attachment ccPolicyDefinitionFileDetail,
                                                  MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        if (policySpecFileInputStream == null) {
            throw new APIManagementException(ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);
        String jsonContent = PublisherCommonUtils.readInputStream(policySpecFileInputStream);
        String fileName = policySpecFileDetail.getDataHandler().getName();
        String fileContentType = URLConnection.guessContentTypeFromName(fileName);
        if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
            fileContentType = policySpecFileDetail.getContentType().toString();
        }

        OperationPolicyDefinition synapseDefinition;
        OperationPolicyDefinition ccPolicyDefinition;
        OperationPolicySpecification policySpecification =
                OperationPoliciesApiCommonImpl.getPolicySpecification(fileContentType, jsonContent);

        OperationPolicyData operationPolicyData = OperationPoliciesApiCommonImpl
                .prepareOperationPolicyData(policySpecification, organization, apiId);

        if (synapsePolicyDefinitionFileInputStream != null) {
            String synapsePolicyDefinition =
                    PublisherCommonUtils.readInputStream(synapsePolicyDefinitionFileInputStream);
            synapseDefinition = new OperationPolicyDefinition();
            OperationPoliciesApiCommonImpl.preparePolicyDefinition(operationPolicyData, synapseDefinition,
                    synapsePolicyDefinition, OperationPolicyDefinition.GatewayType.Synapse);
        }
        if (ccPolicyDefinitionFileInputStream != null) {
            String choreoConnectPolicyDefinition =
                    PublisherCommonUtils.readInputStream(ccPolicyDefinitionFileInputStream);
            ccPolicyDefinition = new OperationPolicyDefinition();
            OperationPoliciesApiCommonImpl.preparePolicyDefinition(operationPolicyData, ccPolicyDefinition,
                    choreoConnectPolicyDefinition, OperationPolicyDefinition.GatewayType.ChoreoConnect);
        }

        OperationPolicyDataDTO operationPolicyDataDTO = ApisApiCommonImpl
                .addAPISpecificOperationPolicy(policySpecification, operationPolicyData, apiId, organization);
        try {
            URI createdPolicyUri = new URI(RestApiConstants.REST_API_PUBLISHER_VERSION
                    + RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + "/"
                    + RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES + "/" + operationPolicyDataDTO.getId());
            return Response.created(createdPolicyUri).entity(operationPolicyDataDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("An error has occurred while adding an API specific operation policy",
                    ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get the list of all API specific operation policies for a given API
     *
     * @param apiId          API UUID
     * @param limit          max number of records returned
     * @param offset         starting index
     * @param messageContext message context
     * @return A list of operation policies available for the API
     */
    @Override
    public Response getAllAPISpecificOperationPolicies(String apiId, Integer limit, Integer offset, String query,
                                                       MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OperationPolicyDataListDTO policyListDTO = ApisApiCommonImpl.getAllAPISpecificOperationPolicies(apiId, limit,
                offset, organization);
        return Response.ok().entity(policyListDTO).build();
    }

    /**
     * Get the API specific operation policy specification by providing the policy ID
     *
     * @param apiId             API UUID
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return Operation policy DTO as response
     */
    @Override
    public Response getOperationPolicyForAPIByPolicyId(String apiId, String operationPolicyId,
                                                       MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OperationPolicyDataDTO policyDataDTO = ApisApiCommonImpl.getOperationPolicyForAPIByPolicyId(apiId,
                operationPolicyId, organization);
        return Response.ok().entity(policyDataDTO).build();
    }

    /**
     * Download the operation policy specification and definition for a given API specific policy
     *
     * @param apiId             API UUID
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response getAPISpecificOperationPolicyContentByPolicyId(String apiId, String operationPolicyId,
                                                                   MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OperationPolicyData policyData = ApisApiCommonImpl.getAPISpecificOperationPolicyContentByPolicyId(apiId,
                operationPolicyId, organization);
        File file = PublisherCommonUtils.exportOperationPolicyData(policyData, ExportFormat.YAML.name());
        return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").build();
    }

    /**
     * Delete API specific operation policy by providing the policy ID
     *
     * @param apiId             API UUID
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response deleteAPISpecificOperationPolicyByPolicyId(String apiId, String operationPolicyId,
                                                               MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApisApiCommonImpl.deleteAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization);
        return Response.ok().build();
    }

    /**
     * Publish API to given external stores.
     *
     * @param apiId            API Id
     * @param externalStoreIds External Store Ids
     * @param ifMatch          If-match header value
     * @param messageContext   CXF Message Context
     * @return Response of published external store list
     */
    @Override
    public Response publishAPIToExternalStores(String apiId, String externalStoreIds, String ifMatch,
                                               MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIExternalStoreListDTO apiExternalStoreListDTO =
                ApisApiCommonImpl.publishAPIToExternalStores(apiId, externalStoreIds, organization);
        return Response.ok().entity(apiExternalStoreListDTO).build();
    }

    /**
     * Get the resource policies(inflow/outflow).
     *
     * @param apiId        API ID
     * @param sequenceType sequence type('in' or 'out')
     * @param resourcePath api resource path
     * @param verb         http verb
     * @param ifNoneMatch  If-None-Match header value
     * @return json response of the resource policies according to the resource path
     */
    @Override
    public Response getAPIResourcePolicies(String apiId, String sequenceType, String resourcePath,
                                           String verb, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ResourcePolicyListDTO resourcePolicyListDTO = ApisApiCommonImpl
                .getAPIResourcePolicies(apiId, organization, sequenceType, resourcePath, verb);
        return Response.ok().entity(resourcePolicyListDTO).build();
    }

    /**
     * Get the resource policy given the resource id.
     *
     * @param apiId            API ID
     * @param resourcePolicyId resource policy id
     * @param ifNoneMatch      If-None-Match header value
     * @return json response of the resource policy for the resource id given
     */
    @Override
    public Response getAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
                                                     String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ResourcePolicyInfoDTO resourcePolicyInfoDTO =
                ApisApiCommonImpl.getAPIResourcePoliciesByPolicyId(apiId, resourcePolicyId, organization);
        return Response.ok().entity(resourcePolicyInfoDTO).build();
    }

    /**
     * Update the resource policies(inflow/outflow) given the resource id.
     *
     * @param apiId            API ID
     * @param resourcePolicyId resource policy id
     * @param body             resource policy content
     * @param ifMatch          If-Match header value
     * @return json response of the updated sequence content
     */
    @Override
    public Response updateAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
                                                        ResourcePolicyInfoDTO body, String ifMatch,
                                                        MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        ResourcePolicyInfoDTO resourcePolicyInfoDTO = ApisApiCommonImpl
                .updateAPIResourcePoliciesByPolicyId(apiId, organization, resourcePolicyId, body.getContent());
        return Response.ok().entity(resourcePolicyInfoDTO).build();
    }

    /**
     * Get total revenue for a given API from all its' subscriptions
     *
     * @param apiId          API ID
     * @param messageContext message context
     * @return revenue data for a given API
     */
    @Override
    public Response getAPIRevenue(String apiId, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIRevenueDTO apiRevenueDTO = ApisApiCommonImpl.getAPIRevenue(apiId, organization);
        return Response.ok().entity(apiRevenueDTO).build();
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId       API identifier
     * @param ifNoneMatch If-None-Match header value
     * @return Swagger document of the API
     */
    @Override
    public Response getAPISwagger(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        String updatedDefinition = ApisApiCommonImpl.getAPISwagger(apiId, organization);
        return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                "attachment; filename=\"" + "swagger.json" + "\"").build();
    }

    /**
     * Updates the swagger definition of an existing API
     *
     * @param apiId           API identifier
     * @param apiDefinition   Swagger definition
     * @param url             Swagger definition URL
     * @param fileInputStream Swagger definition input file content
     * @param fileDetail      file meta information as Attachment
     * @param ifMatch         If-match header value
     * @return updated swagger document of the API
     */
    @Override
    public Response updateAPISwagger(String apiId, String ifMatch, String apiDefinition, String url,
                                     InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().getStatus());
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }

        String updatedSwagger = ApisApiCommonImpl.updateAPISwagger(apiId, apiDefinition, url, fileInputStream,
                organization, fileName);
        return Response.ok().entity(updatedSwagger).build();
    }

    /**
     * Retrieves the thumbnail image of an API specified by API identifier
     *
     * @param apiId          API Id
     * @param ifNoneMatch    If-None-Match header value
     * @param messageContext If-Modified-Since header value
     * @return Thumbnail image of the API
     */
    @Override
    public Response getAPIThumbnail(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ResourceFile thumbnailResource = ApisApiCommonImpl.getAPIThumbnail(apiId, apiProvider, organization);

        if (thumbnailResource != null) {
            return Response
                    .ok(thumbnailResource.getContent(), MediaType.valueOf(thumbnailResource.getContentType()))
                    .build();
        } else {
            return Response.noContent().build();
        }
    }

    @Override
    public Response updateAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail,
                                       String ifMatch, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String fileName = fileDetail.getDataHandler().getName();
        String fileDetailContentType = fileDetail.getContentType().toString();

        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        FileInfoDTO infoDTO = ApisApiCommonImpl.updateAPIThumbnail(apiId, fileInputStream, organization, fileName,
                fileDetailContentType);
        String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, apiId);
        infoDTO.setRelativePath(uriString);
        try {
            URI uri = new URI(uriString);
            return Response.created(uri).entity(infoDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while updating thumbnail of API: " + apiId;
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
    }

    @Override
    public Response validateAPI(String query, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApisApiCommonImpl.validateAPI(query, organization);
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response validateDocument(String apiId, String name, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ApisApiCommonImpl.validateDocument(apiId, name, organization);
        return Response.status(Response.Status.OK).build();
    }

    @Override
    public Response validateEndpoint(String endpointUrl, String apiId, MessageContext messageContext)
            throws APIManagementException {

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO =
                ApisApiCommonImpl.validateEndpoint(endpointUrl);
        return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
    }

    @Override
    public Response getAPIResourcePaths(String apiId, Integer limit, Integer offset, String ifNoneMatch,
                                        MessageContext messageContext) throws APIManagementException {

        ResourcePathListDTO dto = ApisApiCommonImpl.getAPIResourcePaths(apiId, limit, offset);
        return Response.ok().entity(dto).build();
    }

    /**
     * Validate API Definition and retrieve as the response
     *
     * @param url                 URL of the OpenAPI definition
     * @param fileInputStream     InputStream for the provided file
     * @param fileDetail          File meta-data
     * @param returnContent       Whether to return the definition content
     * @param inlineApiDefinition Swagger API definition String
     * @param messageContext      CXF message context
     * @return API Definition validation response
     */
    @Override
    public Response validateOpenAPIDefinition(Boolean returnContent, String url, InputStream fileInputStream,
                                              Attachment fileDetail, String inlineApiDefinition,
                                              MessageContext messageContext)
            throws APIManagementException {

        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }
        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                ApisApiCommonImpl.validateOpenAPIDefinition(returnContent, url, fileInputStream, inlineApiDefinition,
                        fileName);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Importing an OpenAPI definition and create an API
     *
     * @param fileInputStream      InputStream for the provided file
     * @param fileDetail           File meta-data
     * @param url                  URL of the OpenAPI definition
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param inlineApiDefinition  Swagger API definition String
     * @param messageContext       CXF message context
     * @return API Import using OpenAPI definition response
     * @throws APIManagementException when error occurs while importing the OpenAPI definition
     */
    @Override
    public Response importOpenAPIDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                            String additionalProperties, String inlineApiDefinition,
                                            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        String fileName = null;
        if (fileDetail != null ) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }

        APIDTO createdApiDTO = ApisApiCommonImpl.importOpenAPIDefinition(fileInputStream, url, additionalProperties,
                inlineApiDefinition, organization, fileName);
        try {
            // This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + createdApiDTO.getProvider() + "-" +
                    createdApiDTO.getName() + "-" + createdApiDTO.getVersion();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Validate a provided WSDL definition via a URL or a file/zip
     *
     * @param url             WSDL URL
     * @param fileInputStream file/zip input stream
     * @param fileDetail      file/zip details
     * @param messageContext  messageContext object
     * @return WSDL validation response
     * @throws APIManagementException when error occurred during validation
     */
    @Override
    public Response validateWSDLDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
                                           MessageContext messageContext) throws APIManagementException {

        String fileName = null;
        if (fileDetail != null ) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }
        WSDLValidationResponseDTO validationResponseDTO = ApisApiCommonImpl.validateWSDLDefinition(url, fileInputStream,
                fileName);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Import a WSDL file/url or an archive and create an API. The API can be a SOAP or REST depending on the
     * provided implementationType.
     *
     * @param fileInputStream      file input stream
     * @param fileDetail           file details
     * @param url                  WSDL url
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param implementationType   SOAP or SOAPTOREST
     * @return Created API's payload
     * @throws APIManagementException when error occurred during the operation
     */
    @Override
    public Response importWSDLDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                         String additionalProperties, String implementationType, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        String fileName = null;
        String fileContentType = null;
        if (fileDetail != null) {
            fileContentType = fileDetail.getContentType().toString();
            fileName = fileDetail.getContentDisposition().getFilename();
        }

        APIDTO createdApiDTO = ApisApiCommonImpl.importWSDLDefinition(fileInputStream, fileName, fileContentType,
                url, additionalProperties, implementationType, organization);
        try {
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                    "Error occurred while importing WSDL"));
        }
    }

    /**
     * Retrieve the WSDL of an API
     *
     * @param apiId       UUID of the API
     * @param ifNoneMatch If-None-Match header value
     * @return the WSDL of the API (can be a file or zip archive)
     * @throws APIManagementException when error occurred while trying to retrieve the WSDL
     */
    @Override
    public Response getWSDLOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        ResourceFile resource = ApisApiCommonImpl.getWSDLOfAPI(apiId, organization);
        return RestApiUtil.getResponseFromResourceFile(resource.getName(), resource);
    }

    /**
     * Update the WSDL of an API
     *
     * @param apiId           UUID of the API
     * @param fileInputStream file data as input stream
     * @param fileDetail      file details
     * @param url             URL of the WSDL
     * @return 200 OK response if the operation is successful. 400 if the provided inputs are invalid. 500 if a server
     * error occurred.
     * @throws APIManagementException when error occurred while trying to retrieve the WSDL
     */
    @Override
    public Response updateWSDLOfAPI(String apiId, String ifMatch, InputStream fileInputStream, Attachment fileDetail,
                                    String url, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String fileName = null;
        String contentType = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
            contentType = fileDetail.getContentType().toString();
        }

        ApisApiCommonImpl.updateWSDLOfAPI(apiId, fileInputStream, fileName, contentType, url, organization);
        return Response.ok().build();
    }

    @Override
    public Response changeAPILifecycle(String action, String apiId, String lifecycleChecklist,
                                       String ifMatch, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        WorkflowResponseDTO workflowResponseDTO = ApisApiCommonImpl.changeAPILifecycle(action, apiId,
                lifecycleChecklist, organization);
        return Response.ok().entity(workflowResponseDTO).build();
    }

    @Override
    public Response createNewAPIVersion(String newVersion, String apiId, Boolean defaultVersion,
                                        String serviceVersion, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO newVersionedApi = ApisApiCommonImpl.createNewAPIVersion(newVersion, apiId, defaultVersion, serviceVersion,
                organization);
        try {
            //This URI used to set the location header of the POST response
            URI newVersionedApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + newVersionedApi.getId());
            return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location of " + apiId;
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                    errorMessage));
        }
    }

    /**
     * Exports an API from API Manager for a given API using the ApiId. ID. Meta information, API icon, documentation,
     * WSDL and sequences are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API.
     *
     * @param apiId          UUID of an API
     * @param name           Name of the API that needs to be exported
     * @param version        Version of the API that needs to be exported
     * @param providerName   Provider name of the API that needs to be exported
     * @param format         Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API status on export
     * @return
     */
    @Override
    public Response exportAPI(String apiId, String name, String version, String revisionNum,
                              String providerName, String format, Boolean preserveStatus,
                              Boolean exportLatestRevision, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //If not specified status is preserved by default
        preserveStatus = preserveStatus == null || preserveStatus;

        // Default export format is YAML
        ExportFormat exportFormat = StringUtils.isNotEmpty(format) ?
                ExportFormat.valueOf(format.toUpperCase()) :
                ExportFormat.YAML;
        ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
        File file;
        try {
            file = importExportAPI
                    .exportAPI(apiId, name, version, revisionNum, providerName, preserveStatus, exportFormat,
                            Boolean.TRUE, Boolean.FALSE, exportLatestRevision, StringUtils.EMPTY, organization);
        } catch (APIImportExportException e) {
            throw new APIManagementException("Error while exporting " + RestApiConstants.RESOURCE_API, e);
        }
        return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getName() + "\"").build();
    }

    @Override
    public Response generateInternalAPIKey(String apiId, MessageContext messageContext) throws APIManagementException {

        APIKeyDTO apiKeyDTO = ApisApiCommonImpl.generateInternalAPIKey(apiId);
        return Response.ok().entity(apiKeyDTO).build();
    }

    /**
     * Import a GraphQL Schema
     *
     * @param type                 APIType
     * @param fileInputStream      input file
     * @param fileDetail           file Detail
     * @param additionalProperties api object as string format
     * @param ifMatch              If--Match header value
     * @param messageContext       messageContext
     * @return Response with GraphQL API
     */
    @Override
    public Response importGraphQLSchema(String ifMatch, String type, InputStream fileInputStream,
                                        Attachment fileDetail, String additionalProperties, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO createdApiDTO = ApisApiCommonImpl.importGraphQLSchema(fileInputStream, additionalProperties, organization);
        try {
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + createdApiDTO.getProvider() + "-"
                    + createdApiDTO.getName() + "-" + createdApiDTO.getVersion();
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * Import an API by uploading an archive file. All relevant API data will be included upon the creation of
     * the API. Depending on the choice of the user, provider of the imported API will be preserved or modified.
     *
     * @param fileInputStream  Input stream from the REST request
     * @param fileDetail       File details as Attachment
     * @param preserveProvider User choice to keep or replace the API provider
     * @param overwrite        Whether to update the API or not. This is used when updating already existing APIs.
     * @return API import response
     * @throws APIManagementException when error occurred while trying to import the API
     */
    @Override
    public Response importAPI(InputStream fileInputStream, Attachment fileDetail,
                              Boolean preserveProvider, Boolean rotateRevision, Boolean overwrite,
                              MessageContext messageContext) throws APIManagementException {
        // Check whether to update. If not specified, default value is false.
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ApisApiCommonImpl.importAPI(fileInputStream, preserveProvider, rotateRevision, overwrite, organization, tokenScopes);
        return Response.status(Response.Status.OK).entity("API imported successfully.").build();
    }

    /**
     * Validate graphQL Schema
     *
     * @param fileInputStream input file
     * @param fileDetail      file Detail
     * @param messageContext  messageContext
     * @return Validation response
     */
    @Override
    public Response validateGraphQLSchema(InputStream fileInputStream, Attachment fileDetail,
                                          MessageContext messageContext) {

        String filename = fileDetail.getContentDisposition().getFilename();
        GraphQLValidationResponseDTO validationResponse =
                ApisApiCommonImpl.validateGraphQLSchema(fileInputStream, filename);
        return Response.ok().entity(validationResponse).build();
    }

    /**
     * Generates Mock response examples for Inline prototyping
     * of a swagger
     *
     * @param apiId          API Id
     * @param ifNoneMatch    If-None-Match header value
     * @param messageContext message context
     * @return apiDefinition
     * @throws APIManagementException
     */
    @Override
    public Response generateMockScripts(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String apiDefinition = ApisApiCommonImpl.generateMockScripts(apiId, organization);
        return Response.ok().entity(apiDefinition).build();
    }

    @Override
    public Response getAPISubscriptionPolicies(String apiId, String ifNoneMatch, String xWSO2Tenant,
                                               MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        List<Tier> apiThrottlingPolicies = ApisApiCommonImpl.getAPISubscriptionPolicies(apiId, organization);
        return Response.ok().entity(apiThrottlingPolicies).build();
    }

    /**
     * Retrieve available revisions of an API
     *
     * @param apiId          UUID of the API
     * @param query          Search query string
     * @param messageContext message context object
     * @return response containing list of API revisions
     */
    @Override
    public Response getAPIRevisions(String apiId, String query, MessageContext messageContext) throws APIManagementException {

        APIRevisionListDTO apiRevisionListDTO = ApisApiCommonImpl.getAPIRevisions(apiId, query);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    /**
     * Create a new API revision
     *
     * @param apiId          UUID of the API
     * @param apIRevisionDTO API object that needs to be added
     * @param messageContext message context object
     * @return response containing newly created APIRevision object
     */
    @Override
    public Response createAPIRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        APIRevisionDTO createdApiRevisionDTO = ApisApiCommonImpl.createAPIRevision(apiId, apIRevisionDTO, organization);

        try {
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving created revision API location for API : "
                    + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * Retrieve a revision of an API
     *
     * @param apiId          UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext message context object
     * @return response containing APIRevision object
     */
    @Override
    public Response getAPIRevision(String apiId, String revisionId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    /**
     * Delete a revision of an API
     *
     * @param apiId          UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext message context object
     * @return response with 204 status code and no content
     */
    @Override
    public Response deleteAPIRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        APIRevisionListDTO apiRevisionListDTO = ApisApiCommonImpl.deleteAPIRevision(apiId, revisionId, organization);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    /**
     * Deploy a revision
     *
     * @param apiId          UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext message context object
     * @return response with 200 status code
     */
    @Override
    public Response deployAPIRevision(String apiId, String revisionId,
                                      List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                      MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS =
                ApisApiCommonImpl.deployAPIRevision(apiId, revisionId, apIRevisionDeploymentDTOList, organization);
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Get revision deployment list
     *
     * @param apiId          UUID of the API
     * @param messageContext message context object
     * @return response with 200 status code
     */
    @Override
    public Response getAPIRevisionDeployments(String apiId, MessageContext messageContext) throws APIManagementException {

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = ApisApiCommonImpl.getAPIRevisionDeployments(apiId);
        return Response.ok().entity(apiRevisionDeploymentDTOS).build();
    }

    @Override
    public Response undeployAPIRevision(String apiId, String revisionId, String revisionNum, Boolean allEnvironments,
                                        List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                        MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = ApisApiCommonImpl.undeployAPIRevision(apiId,
                revisionId, revisionNum, allEnvironments, apIRevisionDeploymentDTOList, organization);
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Restore a revision to the working copy of the API
     *
     * @param apiId          UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext message context object
     * @return response with 200 status code
     */
    @Override
    public Response restoreAPIRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        APIDTO apiToReturn = ApisApiCommonImpl.restoreAPIRevision(apiId, revisionId, organization);
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    /**
     * Validate AsyncAPI Specification and retrieve as the response
     *
     * @param url             URL of the AsyncAPI Specification
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail      File meta-data
     * @param returnContent   Whether to return the definition content
     * @param messageContext  CXF message context
     * @return AsyncAPI Specification Validation response
     */
    @Override
    public Response validateAsyncAPISpecification(Boolean returnContent, String url, InputStream fileInputStream,
                                                  Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {

        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }
        AsyncAPISpecificationValidationResponseDTO validationResponseDTO =
                ApisApiCommonImpl.validateAsyncAPISpecification(returnContent, url, fileInputStream, fileName);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Importing and AsyncAPI Specification and create and API
     *
     * @param fileInputStream      InputStream for the provided file
     * @param fileDetail           File meta-data
     * @param url                  URL of the AsyncAPI Specification
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param messageContext       CXF message context
     * @return API import using AsyncAPI specification response
     */
    @Override
    public Response importAsyncAPISpecification(InputStream fileInputStream, Attachment fileDetail, String url,
                                                String additionalProperties, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }

        APIDTO createdAPIDTO = ApisApiCommonImpl.importAsyncAPISpecification(fileInputStream, url, additionalProperties,
                organization, fileName);

        try {
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdAPIDTO.getId());
            return Response.created(createdApiUri).entity(createdAPIDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + createdAPIDTO.getProvider() + "-" +
                    createdAPIDTO.getName() + "-" + createdAPIDTO.getVersion();
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    @Override
    public Response getAsyncAPIDefinition(String apiId, String ifNoneMatch, MessageContext messageContext) throws
            APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String updatedDefinition = ApisApiCommonImpl.getAsyncAPIDefinition(apiId, organization);
        return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                "attachment; fileNme=\"" + "asyncapi.json" + "\"").build();
    }

    @Override
    public Response updateAsyncAPIDefinition(String apiId, String ifMatch, String apiDefinition, String url,
                                             InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());
        String fileName = null;
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }

        String updatedAsyncAPIDefinition = ApisApiCommonImpl.updateAsyncAPIDefinition(apiId, apiDefinition, url,
                fileInputStream, organization, fileName);
        return Response.ok().entity(updatedAsyncAPIDefinition).build();
    }

    @Override
    public Response importServiceFromCatalog(String serviceKey, APIDTO apiDto, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO createdApiDTO = ApisApiCommonImpl.importServiceFromCatalog(serviceKey, apiDto, organization);
        try {
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();

        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDto.getName() + "-"
                    + apiDto.getVersion();
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    @Override
    public Response reimportServiceFromCatalog(String apiId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        APIDTO apiDTO = ApisApiCommonImpl.reimportServiceFromCatalog(apiId, organization);
        return Response.ok().entity(apiDTO).build();
    }

    @Override
    public Response updateAPIDeployment(String apiId, String deploymentId, APIRevisionDeploymentDTO
            apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException {

        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        APIRevisionDeploymentDTO apiRevisionDeploymentDTO =
                ApisApiCommonImpl.updateAPIDeployment(apiId, deploymentId, apIRevisionDeploymentDTO);

        Response.Status status = Response.Status.OK;
        return Response.status(status).entity(apiRevisionDeploymentDTO).build();
    }

    @Override
    public Response getEnvironmentSpecificAPIProperties(String apiId, String envId, MessageContext messageContext)
            throws APIManagementException {

        // validate environment UUID
        RestApiUtil.getValidatedOrganization(messageContext);
        String jsonContent = ApisApiCommonImpl.getEnvironmentSpecificAPIProperties(apiId, envId);

        return Response.ok().entity(jsonContent).build();
    }

    @Override
    public Response updateEnvironmentSpecificAPIProperties(String apiId, String envId, Map<String, String> requestBody,
                                                           MessageContext messageContext) throws APIManagementException {

        // validate environment UUID
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String jsonContent =
                ApisApiCommonImpl.updateEnvironmentSpecificAPIProperties(apiId, envId, requestBody, organization);

        return Response.ok().entity(jsonContent).build();
    }
}
