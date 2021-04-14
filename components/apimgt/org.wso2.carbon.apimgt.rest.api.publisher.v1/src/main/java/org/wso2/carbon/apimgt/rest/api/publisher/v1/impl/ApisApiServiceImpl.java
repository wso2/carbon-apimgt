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
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.Identifier;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.ResourcePath;
import org.wso2.carbon.apimgt.api.model.SOAPToRestSequence;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParser;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionStringComparator;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.impl.wsdl.SequenceGenerator;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExternalStoreMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.GraphqlQueryAnalysisMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.MediationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIExternalStoreListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
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
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePathListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TopicDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.TopicListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.CarbonUtils;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.api.ExceptionCodes.API_VERSION_ALREADY_EXISTS;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);
    private static final String API_PRODUCT_TYPE = "APIPRODUCT";

    @Override
    public Response getAllAPIs(Integer limit, Integer offset, String xWSO2Tenant, String query,
                            String ifNoneMatch, Boolean expand, String accept, MessageContext messageContext) {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        expand = expand != null && expand;
        try {

            //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":", APIConstants.NAME_TYPE_PREFIX + ":");
            }

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            boolean migrationMode = Boolean.getBoolean(RestApiConstants.MIGRATION_MODE);

            /*if (migrationMode) { // migration flow
                if (!StringUtils.isEmpty(targetTenantDomain)) {
                    tenantDomain = targetTenantDomain;
                }
                RestApiUtil.handleMigrationSpecificPermissionViolations(tenantDomain, username);
            }*/
            Map<String, Object> result;

            result = apiProvider.searchPaginatedAPIs(query, tenantDomain, offset, limit);

            Set<API> apis = (Set<API>) result.get("apis");
            allMatchedApis.addAll(apis);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, expand);

            //Add pagination section in the response
            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }

            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);

            if (APIConstants.APPLICATION_GZIP.equals(accept)) {
                try {
                    File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
                    return Response.ok().entity(zippedResponse)
                            .header("Content-Disposition", "attachment").
                                    header("Content-Encoding", "gzip").build();
                } catch (APIManagementException e) {
                    RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
                }
            } else {
                return Response.ok().entity(apiListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response createAPI(APIDTO body, String oasVersion, MessageContext messageContext)
            throws APIManagementException{
        URI createdApiUri;
        APIDTO createdApiDTO;
        try {
            API createdApi = PublisherCommonUtils
                    .addAPIWithGeneratedSwaggerDefinition(body, oasVersion, RestApiCommonUtil.getLoggedInUsername());
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getAPI(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIDTO apiToReturn = getAPIByID(apiId, apiProvider);
        return Response.ok().entity(apiToReturn).build();
    }

    @Override
    public Response addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo, MessageContext
            messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            Identifier identifier;
            if (apiTypeWrapper.isAPIProduct()) {
                identifier = apiTypeWrapper.getApiProduct().getId();
            } else {
                identifier = apiTypeWrapper.getApi().getId();
            }
            Comment comment = new Comment();
            comment.setText(postRequestBodyDTO.getContent());
            comment.setCategory(postRequestBodyDTO.getCategory());
            comment.setParentCommentID(replyTo);
            comment.setEntryPoint("PUBLISHER");
            comment.setUser(username);
            comment.setApiId(apiId);
            String createdCommentId = apiProvider.addComment(identifier, comment, username);
            Comment createdComment = apiProvider.getComment(apiTypeWrapper, createdCommentId, 0, 0);
            CommentDTO commentDTO = CommentMappingUtil.fromCommentToDTO(createdComment);

            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + createdCommentId;
            URI uri = new URI(uriString);
            return Response.created(uri).entity(commentDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to add comment to the API " + apiId, e, log);
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId);
        }
        return null;
    }

    @Override
    public Response getAllCommentsOfAPI(String apiId, String xWSO2Tenant, Integer limit, Integer offset, Boolean
            includeCommenterInfo, MessageContext messageContext) throws APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            String parentCommentID = null;
            CommentList comments = apiProvider.getComments(apiTypeWrapper, parentCommentID, limit, offset);
            CommentListDTO commentDTO = CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to get comments of API " + apiId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getCommentOfAPI(String commentId, String apiId, String xWSO2Tenant, String ifNoneMatch, Boolean
            includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext) throws
            APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);

            if (comment != null) {
                CommentDTO commentDTO;
                if (includeCommenterInfo) {
                    Map<String, Map<String, String>> userClaimsMap = CommentMappingUtil
                            .retrieveUserClaims(comment.getUser(), new HashMap<>());
                    commentDTO = CommentMappingUtil.fromCommentToDTOWithUserInfo(comment, userClaimsMap);
                } else {
                    commentDTO = CommentMappingUtil.fromCommentToDTO(comment);
                }
                String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                        RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
                URI uri = new URI(uriString);
                return Response.ok(uri).entity(commentDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving comment for API : " + apiId + "with comment ID " + commentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getRepliesOfComment(String commentId, String apiId, String xWSO2Tenant, Integer limit, Integer
            offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws
            APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            CommentList comments = apiProvider.getComments(apiTypeWrapper, commentId, limit, offset);
            CommentListDTO commentDTO = CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to get comments of API " + apiId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response editCommentOfAPI(String commentId, String apiId, PatchRequestBodyDTO patchRequestBodyDTO,
                                     MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                if (comment.getUser().equals(username)) {
                    boolean commentEdited = false;
                    if (patchRequestBodyDTO.getCategory() != null && !(patchRequestBodyDTO.getCategory().equals(comment
                            .getCategory()))) {
                        comment.setCategory(patchRequestBodyDTO.getCategory());
                        commentEdited = true;
                    }
                    if (patchRequestBodyDTO.getContent() != null && !(patchRequestBodyDTO.getContent().equals(comment
                            .getText()))) {
                        comment.setText(patchRequestBodyDTO.getContent());
                        commentEdited = true;
                    }
                    if (commentEdited) {
                        if (apiProvider.editComment(apiTypeWrapper, commentId, comment)) {
                            Comment editedComment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
                            CommentDTO commentDTO = CommentMappingUtil.fromCommentToDTO(editedComment);

                            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                                    RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
                            URI uri = new URI(uriString);
                            return Response.ok(uri).entity(commentDTO).build();
                        }
                    } else {
                        return Response.ok().build();
                    }
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_COMMENTS, String.valueOf(commentId)
                            , log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response deleteComment(String commentId, String apiId, String ifMatch, MessageContext messageContext) throws
            APIManagementException {
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
                if (Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE) || comment.getUser().equals(username)) {
                    if (apiProvider.deleteComment(apiTypeWrapper, commentId)) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", commentId);
                        obj.put("message", "The comment has been deleted");
                        return Response.ok(obj).type(MediaType.APPLICATION_JSON).build();
                    } else {
                        return Response.status(405, "Method Not Allowed").type(MediaType
                                .APPLICATION_JSON).build();
                    }
                } else {
                    return Response.status(403, "Forbidden").type(MediaType.APPLICATION_JSON).build();
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while deleting comment " + commentId + "for API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
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
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId,tenantDomain).getId();
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                GraphqlComplexityInfo graphqlComplexityInfo = apiProvider.getComplexityDetails(apiIdentifier);
                GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO =
                        GraphqlQueryAnalysisMappingUtil.fromGraphqlComplexityInfotoDTO(graphqlComplexityInfo);
                return Response.ok().entity(graphQLQueryComplexityInfoDTO).build();
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving complexity details of API : " + apiId, e, log);
            } else {
                String msg = "Error while retrieving complexity details of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
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
        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            String schema = apiProvider.getGraphqlSchema(apiInfo.toAPIIdentifier());

            GraphqlComplexityInfo graphqlComplexityInfo =
                    GraphqlQueryAnalysisMappingUtil.fromDTOtoValidatedGraphqlComplexityInfo(body, schema);
            if (APIConstants.GRAPHQL_API.equals(existingAPI.getType())) {
                apiProvider.addOrUpdateComplexityDetails(apiInfo.toAPIIdentifier(), graphqlComplexityInfo);
                return Response.ok().build();
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating complexity details of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating complexity details of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateTopics(String apiId, TopicListDTO topicListDTO, String ifMatch, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        //validate if api exists
        validateAPIExistence(apiId);

        API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(existingAPI.getStatus());

        Set<URITemplate> uriTemplates = existingAPI.getUriTemplates();
        uriTemplates.clear();

        for (TopicDTO topicDTO : topicListDTO.getList()) {
            URITemplate uriTemplate = new URITemplate();
            uriTemplate.setUriTemplate(topicDTO.getName());
            uriTemplate.setHTTPVerb(topicDTO.getMode().toUpperCase());
            // TODO: Get these from proper locations
            uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
            uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);
            uriTemplates.add(uriTemplate);
        }
        existingAPI.setUriTemplates(uriTemplates);

        // TODO: Add scopes

        try {
            apiProvider.updateAPI(existingAPI);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
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
                                        MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId,tenantDomain).getId();
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            String schemaContent = apiProvider.getGraphqlSchema(apiIdentifier);
            GraphQLSchemaDTO dto = new GraphQLSchemaDTO();
            dto.setSchemaDefinition(schemaContent);
            dto.setName(apiIdentifier.getProviderName() + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                    apiIdentifier.getApiName() + apiIdentifier.getVersion() + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving schema of API: " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while retrieving schema of API: " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
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
                                           MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //validate if api exists
            validateAPIExistence(apiId);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(originalAPI.getStatus());
            PublisherCommonUtils.addGraphQLSchema(originalAPI, schemaDefinition, apiProvider);
            APIDTO modifiedAPI = APIMappingUtil.fromAPItoDTO(originalAPI);
            return Response.ok().entity(modifiedAPI.getOperations()).build();
        } catch (APIManagementException | FaultGatewaysException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving schema of API: " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while uploading schema of the API: " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPI(String apiId, APIDTO body, String ifMatch, MessageContext messageContext) {
        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            //validate if api exists
            validateAPIExistence(apiId);
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(originalAPI.getStatus());
            API updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes);
            return Response.ok().entity(APIMappingUtil.fromAPItoDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while updating API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the API : " + apiId + " - " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
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
     * Validate whether the given API with UUID exists in the DB
     *
     * @param apiId API UUID
     * @return API details
     * @throws APIManagementException if the API doesn't exists in the DB
     */
    private APIInfo validateAPIExistence(String apiId) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIInfo apiInfo = apiProvider.getAPIInfoByUUID(apiId);
        if (apiInfo == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        return apiInfo;
    }

    /**
     * Get all types and fields of the GraphQL Schema of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with all the types and fields found within the schema definition
     */
    @Override public Response getGraphQLPolicyComplexityTypesOfAPI(String apiId, MessageContext messageContext) {
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId,tenantDomain).getId();
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                String schemaContent = apiProvider.getGraphqlSchema(apiIdentifier);
                List<GraphqlSchemaType> typeList = graphql.extractGraphQLTypeList(schemaContent);
                GraphQLSchemaTypeListDTO graphQLSchemaTypeListDTO =
                        GraphqlQueryAnalysisMappingUtil.fromGraphqlSchemaTypeListtoDTO(typeList);
                return Response.ok().entity(graphQLSchemaTypeListDTO).build();
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving types and fields of API : " + apiId, e, log);
            } else {
                String msg = "Error while retrieving types and fields of the schema of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    // AWS Lambda: rest api operation to get ARNs
    @Override
    public Response getAmazonResourceNamesOfAPI(String apiId, MessageContext messageContext) {
        JSONObject arns = new JSONObject();
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            String endpointConfigString = api.getEndpointConfig();
            if (!StringUtils.isEmpty(endpointConfigString)) {
                JSONParser jsonParser = new JSONParser();
                JSONObject endpointConfig = (JSONObject) jsonParser.parse(endpointConfigString);
                if (endpointConfig != null) {
                    if (endpointConfig.containsKey(APIConstants.AMZN_ACCESS_KEY) &&
                            endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY) &&
                                endpointConfig.containsKey(APIConstants.AMZN_REGION)) {
                        String accessKey = (String) endpointConfig.get(APIConstants.AMZN_ACCESS_KEY);
                        String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                        String region = (String) endpointConfig.get(APIConstants.AMZN_REGION);
                        AWSCredentialsProvider credentialsProvider;
                        if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey) &&
                            StringUtils.isEmpty(region)) {
                            credentialsProvider = InstanceProfileCredentialsProvider.getInstance();
                        } else if (!StringUtils.isEmpty(accessKey) && !StringUtils.isEmpty(secretKey) &&
                                    !StringUtils.isEmpty(region)) {
                            if (secretKey.length() == APIConstants.AWS_ENCRYPTED_SECRET_KEY_LENGTH) {
                                CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                                secretKey = new String(cryptoUtil.base64DecodeAndDecrypt(secretKey),
                                        APIConstants.DigestAuthConstants.CHARSET);
                            }
                            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
                            credentialsProvider = new AWSStaticCredentialsProvider(awsCredentials);
                        } else {
                            log.error("Missing AWS Credentials");
                            return null;
                        }
                        AWSLambda awsLambda = AWSLambdaClientBuilder.standard()
                                .withCredentials(credentialsProvider)
                                .withRegion(region)
                                .build();
                        ListFunctionsResult listFunctionsResult = awsLambda.listFunctions();
                        List<FunctionConfiguration> functionConfigurations = listFunctionsResult.getFunctions();
                        arns.put("count", functionConfigurations.size());
                        JSONArray list = new JSONArray();
                        for (FunctionConfiguration functionConfiguration : functionConfigurations) {
                            list.put(functionConfiguration.getFunctionArn());
                        }
                        arns.put("list", list);
                        return Response.ok().entity(arns.toString()).build();
                    }
                }
            }
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
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of the API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException | UnsupportedEncodingException e) {
            String errorMessage = "Error while decrypting the secret key of the API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Method to retrieve Security Audit Report
     * @param apiId API ID of the API
     * @param accept Accept header string
     * @param messageContext Message Context string
     * @return Response object of Security Audit
     */
    @Override
    public Response getAuditReportOfAPI(String apiId, String accept, MessageContext messageContext) {
        boolean isDebugEnabled = log.isDebugEnabled();
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = api.getId();
            String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier, tenantDomain);
            // Get configuration file, retrieve API token and collection id
            JSONObject securityAuditPropertyObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
            String apiToken = (String) securityAuditPropertyObject.get("apiToken");
            String collectionId = (String) securityAuditPropertyObject.get("collectionId");
            String baseUrl = (String) securityAuditPropertyObject.get("baseUrl");

            if (baseUrl == null) {
                baseUrl = APIConstants.BASE_AUDIT_URL;
            }
            // Retrieve the uuid from the database
            String auditUuid = ApiMgtDAO.getInstance().getAuditApiId(apiIdentifier);
            if (auditUuid != null) {
                updateAuditApi(apiDefinition, apiToken, auditUuid, baseUrl, isDebugEnabled);
            } else {
                auditUuid = createAuditApi(collectionId, apiToken, apiIdentifier, apiDefinition, baseUrl,
                        isDebugEnabled);
            }
            // Logic for the HTTP request
            String getUrl = baseUrl + "/" + auditUuid + APIConstants.ASSESSMENT_REPORT;
            URL getReportUrl = new URL(getUrl);
            try (CloseableHttpClient getHttpClient = (CloseableHttpClient) APIUtil
                    .getHttpClient(getReportUrl.getPort(), getReportUrl.getProtocol())) {
                HttpGet httpGet = new HttpGet(getUrl);
                // Set the header properties of the request
                httpGet.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                httpGet.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
                httpGet.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
                // Code block for the processing of the response
                try (CloseableHttpResponse response = getHttpClient.execute(httpGet)) {
                    if (isDebugEnabled) {
                        log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                    }
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                        String inputLine;
                        StringBuilder responseString = new StringBuilder();

                        while ((inputLine = reader.readLine()) != null) {
                            responseString.append(inputLine);
                        }
                        reader.close();
                        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                        String report = responseJson.get(APIConstants.DATA).toString();
                        String grade = (String) ((JSONObject) ((JSONObject) responseJson.get(APIConstants.ATTR))
                                .get(APIConstants.DATA)).get(APIConstants.GRADE);
                        Integer numErrors = Integer.valueOf(
                                (String) ((JSONObject) ((JSONObject) responseJson.get(APIConstants.ATTR))
                                        .get(APIConstants.DATA)).get(APIConstants.NUM_ERRORS));
                        String decodedReport = new String(Base64Utils.decode(report), StandardCharsets.UTF_8);
                        AuditReportDTO auditReportDTO = new AuditReportDTO();
                        auditReportDTO.setReport(decodedReport);
                        auditReportDTO.setGrade(grade);
                        auditReportDTO.setNumErrors(numErrors);
                        auditReportDTO.setExternalApiId(auditUuid);
                        return Response.ok().entity(auditReportDTO).build();
                    }
                }
            }
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error occurred while getting "
                    + "HttpClient instance", e, log);
        } catch (ParseException e) {
            RestApiUtil.handleInternalServerError("API Definition String "
                    + "could not be parsed into JSONObject.", e, log);
        } catch (APIManagementException e) {
            String errorMessage = "Error while Auditing API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Update API Definition before retrieving Security Audit Report
     * @param apiDefinition API Definition of API
     * @param apiToken API Token to access Security Audit
     * @param auditUuid Respective UUID of API in Security Audit
     * @param baseUrl Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @throws IOException In the event of any problems with the request
     * @throws APIManagementException In the event of unexpected response
     */
    private void updateAuditApi(String apiDefinition, String apiToken, String auditUuid, String baseUrl,
                                boolean isDebugEnabled)
            throws IOException, APIManagementException {
        // Set the property to be attached in the body of the request
        // Attach API Definition to property called specfile to be sent in the request
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("specfile", Base64Utils.encode(apiDefinition.getBytes(StandardCharsets.UTF_8)));
        // Logic for HTTP Request
        String putUrl = baseUrl + "/" + auditUuid;
        URL updateApiUrl = new URL(putUrl);
        try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil
                .getHttpClient(updateApiUrl.getPort(), updateApiUrl.getProtocol())) {
            HttpPut httpPut = new HttpPut(putUrl);
            // Set the header properties of the request
            httpPut.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpPut.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpPut.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
            httpPut.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
            httpPut.setEntity(new StringEntity(jsonBody.toJSONString()));
            // Code block for processing the response
            try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                if (isDebugEnabled) {
                    log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                }
                if (!(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)) {
                    throw new APIManagementException(
                            "Error while sending data to the API Security Audit Feature. Found http status " +
                                    response.getStatusLine());
                }
            } finally {
                httpPut.releaseConnection();
            }
        }
    }

    /**
     * Send API Definition to Security Audit for the first time
     * @param collectionId Collection ID in which the Definition should be sent to
     * @param apiToken API Token to access Security Audit
     * @param apiIdentifier API Identifier object
     * @param apiDefinition API Definition of API
     * @param baseUrl Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @return String UUID of API in Security Audit
     * @throws IOException In the event of any problems in the request
     * @throws APIManagementException In the event of unexpected response
     * @throws ParseException In the event of any parse errors from the response
     */
    private String createAuditApi(String collectionId, String apiToken, APIIdentifier apiIdentifier,
                                  String apiDefinition, String baseUrl, boolean isDebugEnabled)
            throws IOException, APIManagementException, ParseException {
        HttpURLConnection httpConn;
        OutputStream outputStream;
        PrintWriter writer;
        String auditUuid = null;
        URL url = new URL(baseUrl);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty(APIConstants.HEADER_CONTENT_TYPE,
                APIConstants.MULTIPART_CONTENT_TYPE + APIConstants.MULTIPART_FORM_BOUNDARY);
        httpConn.setRequestProperty(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
        httpConn.setRequestProperty(APIConstants.HEADER_API_TOKEN, apiToken);
        httpConn.setRequestProperty(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
        // Name property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"name\"")
                .append(APIConstants.MULTIPART_LINE_FEED).append(APIConstants.MULTIPART_LINE_FEED)
                .append(apiIdentifier.getApiName()).append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        // Specfile property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"specfile\"; filename=\"swagger.json\"")
                .append(APIConstants.MULTIPART_LINE_FEED)
                .append(APIConstants.HEADER_CONTENT_TYPE + ": " + APIConstants.APPLICATION_JSON_MEDIA_TYPE)
                .append(APIConstants.MULTIPART_LINE_FEED).append(APIConstants.MULTIPART_LINE_FEED)
                .append(apiDefinition).append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        // CollectionID property
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY).append(APIConstants.MULTIPART_LINE_FEED)
                .append("Content-Disposition: form-data; name=\"cid\"").append(APIConstants.MULTIPART_LINE_FEED)
                .append(APIConstants.MULTIPART_LINE_FEED).append(collectionId)
                .append(APIConstants.MULTIPART_LINE_FEED);
        writer.flush();
        writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY + "--")
                .append(APIConstants.MULTIPART_LINE_FEED);
        writer.close();
        // Checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            if (isDebugEnabled) {
                log.debug("HTTP status " + status);
            }
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(httpConn.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder responseString = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                responseString.append(inputLine);
            }
            reader.close();
            httpConn.disconnect();
            JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
            auditUuid = (String) ((JSONObject) responseJson.get(APIConstants.DESC)).get(APIConstants.ID);
            ApiMgtDAO.getInstance().addAuditApiMapping(apiIdentifier, auditUuid);
        } else {
            if (httpConn.getErrorStream() != null) {
                BufferedReader reader =
                        new BufferedReader(new InputStreamReader(httpConn.getErrorStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder responseString = new StringBuilder();

                while ((inputLine = reader.readLine()) != null) {
                    responseString.append(inputLine);
                }
                reader.close();
                httpConn.disconnect();
                JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                String errorMessage = httpConn.getResponseMessage();
                if (responseJson.containsKey("message")) {
                    errorMessage = (String) responseJson.get("message");
                }
                throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                        httpConn.getResponseCode() + " - " + errorMessage);
            } else {
                throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                        httpConn.getResponseCode() + " - " + httpConn.getResponseMessage());
            }
        }
        return auditUuid;
    }

    @Override
    public Response getAPIClientCertificateContentByAlias(String apiId, String alias,
                                                               MessageContext messageContext) {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String certFileName = alias + ".crt";
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    api.getId());
            if (clientCertificateDTO != null) {
                Object certificate = CertificateRestApiUtils
                        .getDecodedCertificate(clientCertificateDTO.getCertificate());
                Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + certFileName + "\"");
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                return responseBuilder.build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving the client certificate with alias " + alias + " for the tenant "
                            + tenantDomain, e, log);
        }
        return null;
    }

    @Override
    public Response deleteAPIClientCertificateByAlias(String alias, String apiId,
                                                           MessageContext messageContext) {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            //validate if api exists
            validateAPIExistence(apiId);

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, RestApiCommonUtil.getLoggedInUserTenantDomain());
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(api.getStatus());

            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    api.getId());
            int responseCode = apiProvider
                    .deleteClientCertificate(
                            RestApiCommonUtil.getLoggedInUsername(), clientCertificateDTO.getApiIdentifier(),
                            alias);
            if (responseCode == ResponseCode.SUCCESS.getResponseCode()) {
                //Handle api product case.
                if (API_PRODUCT_TYPE.equals(api.getType())) {
                    APIIdentifier apiIdentifier = api.getId();
                    APIProductIdentifier apiProductIdentifier =
                            new APIProductIdentifier(apiIdentifier.getProviderName(), apiIdentifier.getApiName(),
                                    apiIdentifier.getVersion());
                    APIProduct apiProduct = apiProvider.getAPIProduct(apiProductIdentifier);
                    apiProvider.updateAPIProduct(apiProduct);
                } else {
                    apiProvider.updateAPI(api);
                }
                if (log.isDebugEnabled()) {
                    log.debug(String.format("The client certificate which belongs to tenant : %s represented by the "
                            + "alias : %s is deleted successfully", tenantDomain, alias));
                }
                return Response.ok().entity("The certificate for alias '" + alias + "' deleted successfully.").build();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Failed to delete the client certificate which belongs to tenant : %s "
                            + "represented by the alias : %s.", tenantDomain, alias));
                }
                RestApiUtil.handleInternalServerError(
                        "Error while deleting the client certificate for alias '" + alias + "'.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while deleting the client certificate with alias " + alias + " for the tenant "
                            + tenantDomain, e, log);
        } catch (FaultGatewaysException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while publishing the certificate change to gateways for the alias " + alias, e, log);
        }
        return null;
    }

    @Override
    public Response getAPIClientCertificateByAlias(String alias, String apiId,
                                                        MessageContext messageContext) {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    api.getId());
            CertificateInformationDTO certificateInformationDTO = certificateMgtUtils
                    .getCertificateInfo(clientCertificateDTO.getCertificate());
            if (certificateInformationDTO != null) {
                CertificateInfoDTO certificateInfoDTO = CertificateMappingUtil
                        .fromCertificateInformationToDTO(certificateInformationDTO);
                return Response.ok().entity(certificateInfoDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError("Certificate is empty for alias " + alias, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving the client certificate with alias " + alias + " for the tenant "
                            + tenantDomain, e, log);
        }
        return null;
    }

    @Override
    public Response updateAPIClientCertificateByAlias(String alias, String apiId,
                                                      InputStream certificateInputStream,
                                                      Attachment certificateDetail, String tier,
                                                      MessageContext messageContext) {
        try {
            //validate if api exists
            validateAPIExistence(apiId);

            ContentDisposition contentDisposition;
            String fileName;
            String base64EncodedCert = null;
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, RestApiCommonUtil.getLoggedInUserTenantDomain());
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(api.getStatus());

            String userName = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(userName);
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    api.getId());
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
            int responseCode = apiProvider
                    .updateClientCertificate(base64EncodedCert, alias, clientCertificateDTO.getApiIdentifier(), tier,
                            tenantId);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                //Handle api product case.
                if (API_PRODUCT_TYPE.equals(api.getType())) {
                    APIIdentifier apiIdentifier = api.getId();
                    APIProductIdentifier apiProductIdentifier =
                            new APIProductIdentifier(apiIdentifier.getProviderName(), apiIdentifier.getApiName(),
                                    apiIdentifier.getVersion());
                    APIProduct apiProduct = apiProvider.getAPIProduct(apiProductIdentifier);
                    apiProvider.updateAPIProduct(apiProduct);
                } else {
                    apiProvider.updateAPI(api);
                }
                ClientCertMetadataDTO clientCertMetadataDTO = new ClientCertMetadataDTO();
                clientCertMetadataDTO.setAlias(alias);
                clientCertMetadataDTO.setApiId(api.getUUID());
                clientCertMetadataDTO.setTier(clientCertificateDTO.getTierName());
                URI updatedCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + "?alias=" + alias);

                return Response.ok(updatedCertUri).entity(clientCertMetadataDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError(
                        "Error while updating the client certificate for the alias " + alias + " due to an internal "
                                + "server error", log);
            } else if (ResponseCode.CERTIFICATE_NOT_FOUND.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceNotFoundError("", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest(
                        "Error while updating the client certificate for the alias " + alias + " Certificate Expired.",
                        log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while updating the client certificate for the alias " + alias + " due to an internal "
                            + "server error", e, log);
        } catch (IOException e) {
            RestApiUtil
                    .handleInternalServerError("Error while encoding client certificate for the alias " + alias, e,
                            log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while generating the resource location URI for alias '" + alias + "'", e, log);
        } catch (FaultGatewaysException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while publishing the certificate change to gateways for the alias " + alias, e, log);
        }
        return null;
    }

    @Override
    public Response getAPIClientCertificates(String apiId, Integer limit, Integer offset, String alias,
                                                   MessageContext messageContext) {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<ClientCertificateDTO> certificates = new ArrayList<>();
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "apiId", apiId);

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            int totalCount = apiProvider.getClientCertificateCount(tenantId);
            if (totalCount > 0) {
                APIIdentifier apiIdentifier = null;
                if (StringUtils.isNotEmpty(apiId)) {
                    API api = apiProvider.getAPIbyUUID(apiId, RestApiCommonUtil.getLoggedInUserTenantDomain());
                    apiIdentifier = api.getId();
                }
                certificates = apiProvider.searchClientCertificates(tenantId, alias, apiIdentifier);
            }

            ClientCertificatesDTO certificatesDTO = CertificateRestApiUtils
                    .getPaginatedClientCertificates(certificates, limit, offset, query);
            APIListDTO apiListDTO = new APIListDTO();
            PaginationDTO paginationDTO = new PaginationDTO();
            paginationDTO.setLimit(limit);
            paginationDTO.setOffset(offset);
            paginationDTO.setTotal(totalCount);
            certificatesDTO.setPagination(paginationDTO);
            return Response.status(Response.Status.OK).entity(certificatesDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the client certificates.", e, log);
        }
        return null;
    }

    @Override
        public Response addAPIClientCertificate(String apiId, InputStream certificateInputStream,
                                                Attachment certificateDetail, String alias, String tier,
                                                MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(apiId)) {
                RestApiUtil.handleBadRequest("The alias and/ or apiId should not be empty", log);
            }
            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest(
                        "Certificate addition failed. Proper Certificate file should be provided", log);
            }
            //validate if api exists
            validateAPIExistence(apiId);

            API api = apiProvider.getAPIbyUUID(apiId, RestApiCommonUtil.getLoggedInUserTenantDomain());
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(api.getStatus());

            String userName = RestApiCommonUtil.getLoggedInUsername();
            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = apiProvider.addClientCertificate(userName, api.getId(), base64EncodedCert, alias, tier);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Add certificate operation response code : %d", responseCode));
            }
            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                //Handle api product case.
                if (API_PRODUCT_TYPE.equals(api.getType())) {
                    APIIdentifier apiIdentifier = api.getId();
                    APIProductIdentifier apiProductIdentifier =
                            new APIProductIdentifier(apiIdentifier.getProviderName(), apiIdentifier.getApiName(),
                                    apiIdentifier.getVersion());
                    APIProduct apiProduct = apiProvider.getAPIProduct(apiProductIdentifier);
                    apiProvider.updateAPIProduct(apiProduct);
                } else {
                    apiProvider.updateAPI(api);
                }
                ClientCertMetadataDTO certificateDTO = new ClientCertMetadataDTO();
                certificateDTO.setAlias(alias);
                certificateDTO.setApiId(apiId);
                certificateDTO.setTier(tier);
                URI createdCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + "?alias=" + alias);
                return Response.created(createdCertUri).entity(certificateDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError(
                        "Internal server error while adding the client certificate to " + "API " + apiId, log);
            } else if (ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceAlreadyExistsError(
                        "The alias '" + alias + "' already exists in the trust store.", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest(
                        "Error while adding the certificate to the API " + apiId + ". " + "Certificate Expired.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "APIManagement exception while adding the certificate to the API " + apiId + " due to an internal "
                            + "server error", e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError(
                    "IOException while generating the encoded certificate for the API " + apiId, e, log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while generating the resource location URI for alias '" + alias + "'", e, log);
        } catch (FaultGatewaysException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while publishing the certificate change to gateways for the alias " + alias, e, log);
        }
        return null;
    }

    /**
     * Delete API
     *
     * @param apiId   API Id
     * @param ifMatch If-Match header value
     * @return Status of API Deletion
     */
    @Override
    public Response deleteAPI(String apiId, String ifMatch, MessageContext messageContext) {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            //check if the API has subscriptions
            //Todo : need to optimize this check. This method seems too costly to check if subscription exists
            List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(api.getId());
            if (apiUsages != null && apiUsages.size() > 0) {
                RestApiUtil.handleConflict("Cannot remove the API " + apiId + " as active subscriptions exist", log);
            }

            List<APIResource> usedProductResources = apiProvider.getUsedProductResources(api.getId());

            if (!usedProductResources.isEmpty()) {
                RestApiUtil.handleConflict("Cannot remove the API because following resource paths " +
                        usedProductResources.toString() + " are used by one or more API Products", log);
            }

            // check user has publisher role and API is in published or deprecated state
            String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                    .get(RestApiConstants.USER_REST_API_SCOPES);
            if (!ArrayUtils.contains(tokenScopes, RestApiConstants.PUBLISHER_SCOPE) && (
                    APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus()) || APIConstants.DEPRECATED
                            .equalsIgnoreCase(api.getStatus()))) {
                RestApiUtil.handleAuthorizationFailure(username + " cannot remove the API", log);
            }

            //deletes the API
            apiProvider.deleteAPI(api);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Get resources of an API that are reused by API Products
     *
     * @param api API
     * @return List of resources reused by API Products
     */
    private List<APIResource> getUsedProductResources(API api) {
        List<APIResource> usedProductResources = new ArrayList<>();
        Set<URITemplate> uriTemplates = api.getUriTemplates();

        for (URITemplate uriTemplate : uriTemplates) {
            // If existing URITemplate is used by any API Products
            if (!uriTemplate.retrieveUsedByProducts().isEmpty()) {
                APIResource apiResource = new APIResource(uriTemplate.getHTTPVerb(), uriTemplate.getUriTemplate());
                usedProductResources.add(apiResource);
            }
        }

        return usedProductResources;
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
                                                           String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            DocumentationContent docContent = apiProvider.getDocumentationContent(apiId, documentId, tenantDomain);
            if (docContent == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

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
        } catch (APIManagementException e) {
            // Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
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
                InputStream inputStream, Attachment fileDetail, String inlineContent,
                                                            MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            if (inputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(apiId, documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

            //add content depending on the availability of either input stream or inline content
            if (inputStream != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not FILE", log);
                }
                RestApiPublisherUtils.attachFileToDocument(apiId, documentation, inputStream, fileDetail);
            } else if (inlineContent != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE) &&
                        !documentation.getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not INLINE " +
                            "or MARKDOWN", log);
                }
                PublisherCommonUtils
                        .addDocumentationContent(documentation, apiProvider, apiId, documentId, tenantDomain,
                                inlineContent);
            } else {
                RestApiUtil.handleBadRequest("Either 'file' or 'inlineContent' should be specified", log);
            }

            //retrieving the updated doc and the URI
            Documentation updatedDoc = apiProvider.getDocumentation(apiId, documentId, tenantDomain);
            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENT_CONTENT
                    .replace(RestApiConstants.APIID_PARAM, apiId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentId);
            URI uri = new URI(uriString);
            return Response.created(uri).entity(documentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding content to the document: " + documentId + " of API "
                                + apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving document content location : " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
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
                                                       MessageContext messageContext) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            documentation = apiProvider.getDocumentation(apiId, documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }
            apiProvider.removeDocumentation(apiId, documentId, tenantDomain);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while deleting : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getAPIDocumentByDocumentId(String apiId, String documentId, String ifNoneMatch,
                                                    MessageContext messageContext) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            documentation = apiProvider.getDocumentation(apiId, documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }

            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            return Response.ok().entity(documentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
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
                                                    String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String sourceUrl = body.getSourceUrl();
            Documentation oldDocument = apiProvider.getDocumentation(apiId, documentId, tenantDomain);

            //validation checks for existence of the document
            if (body.getType() == null) {
                throw new BadRequestException();
            }
            if (oldDocument == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && org.apache.commons.lang3.StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
                return null;
            }
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                return null;
            }

            //overriding some properties
            body.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            newDocumentation.setFilePath(oldDocument.getFilePath());
            newDocumentation.setId(documentId);
            newDocumentation = apiProvider.updateDocumentation(apiId, newDocumentation, tenantDomain);

            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating document : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the document " + documentId + " for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
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
                                          MessageContext messageContext) {
        // do some magic!
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            //List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiIdentifier);
            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiId, tenantDomain);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, apiId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving documents of API : " + apiId, e, log);
            } else {
                String msg = "Error while retrieving documents of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    /**
     * Add a documentation to an API
     *
     * @param apiId api identifier
     * @param body  Documentation DTO as request body
     * @return created document DTO as response
     */
    @Override
    public Response addAPIDocument(String apiId, DocumentDTO body, String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            Documentation documentation = PublisherCommonUtils.addDocumentationToAPI(body, apiId);
            DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                    .replace(RestApiConstants.APIID_PARAM, apiId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentation.getId());
            URI uri = new URI(uriString);
            return Response.created(uri).entity(newDocumentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while adding documents of API : " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while adding the document for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving location for document " + body.getName() + " of API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get external store list which the given API is already published to.
     * @param apiId API Identifier
     * @param ifNoneMatch If-None-Match header value
     * @param messageContext CXF Message Context
     * @return External Store list of published API
     */
    @Override
    public Response getAllPublishedExternalStoresByAPI(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        APIIdentifier apiIdentifier = null;
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        try {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while getting API: " + apiId;
                log.error(errorMessage, e);
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }

        Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(apiIdentifier);
        APIExternalStoreListDTO apiExternalStoreListDTO =
                ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
        return Response.ok().entity(apiExternalStoreListDTO).build();
    }

    /**
     * Gets generated scripts
     *
     * @param apiId  API Id
     * @param ifNoneMatch If-None-Match header value
     * @param messageContext message context
     * @return list of policies of generated sample payload
     * @throws APIManagementException
     */
    @Override
    public Response getGeneratedMockScriptsOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        APIIdentifier apiIdentifier = originalAPI.getId();
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier, tenantDomain);
        Map<String, Object> examples = OASParserUtil.generateExamples(apiDefinition);
        List<APIResourceMediationPolicy> policies = (List<APIResourceMediationPolicy>) examples.get(APIConstants.MOCK_GEN_POLICY_LIST);
        return Response.ok().entity(APIMappingUtil.fromMockPayloadsToListDTO(policies)).build();
    }

    /**
     * Retrieves the WSDL meta information of the given API. The API must be a SOAP API.
     *
     * @param apiId Id of the API
     * @param messageContext CXF Message Context
     * @return WSDL meta information of the API
     * @throws APIManagementException when error occurred while retrieving API WSDL meta info.
     *  eg: when API doesn't exist, API exists but it is not a SOAP API.
     */
    @Override
    public Response getWSDLInfoOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, tenantDomain);
        WSDLInfoDTO wsdlInfoDTO = APIMappingUtil.getWsdlInfoDTO(api);
        if (wsdlInfoDTO == null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.NO_WSDL_AVAILABLE_FOR_API,
                            api.getId().getApiName(), api.getId().getVersion()));
        } else {
            return Response.ok().entity(wsdlInfoDTO).build();
        }
    }

    /**
     * Retrieves API Lifecycle history information
     *
     * @param apiId API Id
     * @param ifNoneMatch If-None-Match header value
     * @return API Lifecycle history information
     */
    @Override
    public Response getAPILifecycleHistory(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier;
            APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            List<LifeCycleEvent> lifeCycleEvents = apiProvider.getLifeCycleEvents(apiIdentifier);
            LifecycleHistoryDTO historyDTO = APIMappingUtil.fromLifecycleHistoryModelToDTO(lifeCycleEvents);
            return Response.ok().entity(historyDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId API Id
     * @param ifNoneMatch If-None-Match header value
     * @return API Lifecycle state information
     */
    @Override
    public Response getAPILifecycleState(String apiId, String ifNoneMatch, MessageContext messageContext) {
        LifecycleStateDTO lifecycleStateDTO = getLifecycleState(apiId);
        return Response.ok().entity(lifecycleStateDTO).build();
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId API Id
     * @return API Lifecycle state information
     */
    private LifecycleStateDTO getLifecycleState(String apiId) {
        return getLifecycleState(null, apiId);
    }

    private LifecycleStateDTO getLifecycleState(APIIdentifier identifier, String apiId) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier;
            if (identifier == null) {
                if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                    apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId,tenantDomain).getId();
                } else {
                    apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
                }
            } else {
                apiIdentifier = identifier;
            }
            Map<String, Object> apiLCData = apiProvider.getAPILifeCycleData(apiId, tenantDomain);
            if (apiLCData == null) {
                String errorMessage = "Error while getting lifecycle state for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, log);
            }

            boolean apiOlderVersionExist = false;
            // check whether other versions of the current API exists
            APIVersionStringComparator comparator = new APIVersionStringComparator();
            Set<String> versions = apiProvider.getAPIVersions(
                    APIUtil.replaceEmailDomain(apiIdentifier.getProviderName()), apiIdentifier.getName());

            for (String tempVersion : versions) {
                if (comparator.compare(tempVersion, apiIdentifier.getVersion()) < 0) {
                    apiOlderVersionExist = true;
                    break;
                }
            }

            return APIMappingUtil.fromLifecycleModelToDTO(apiLCData, apiOlderVersionExist);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response deleteAPILifecycleStatePendingTasks(String apiId, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            apiProvider.deleteWorkflowTask(apiIdentifier);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting task ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getAllAPIMediationPolicies(String apiId, Integer limit, Integer offset, String query,
            String ifNoneMatch, MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        APIIdentifier apiIdentifier;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
            //        tenantDomain);
            //Getting list of API specific mediation policies
            List<Mediation> mediationList =
                    apiProvider.getAllApiSpecificMediationPolicies(apiId, tenantDomain);
            //Converting list of mediation policies to DTO
            MediationListDTO mediationListDTO =
                    MediationMappingUtil.fromMediationListToDTO(mediationList, offset, limit);
            return Response.ok().entity(mediationListDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving mediation policies of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving all api specific mediation policies" +
                        " of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response deleteAPIMediationPolicyByPolicyId(String apiId, String mediationPolicyId,
            String ifMatch, MessageContext messageContext) {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            //validate if api exists
            validateAPIExistence(apiId);

            API api = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(api.getStatus());

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out apiResourcePath
            //apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Getting specified mediation policy
            Mediation mediation =
                    apiProvider.getApiSpecificMediationPolicyByPolicyId(apiId, mediationPolicyId, tenantDomain);
            if (mediation != null) {
                if (isAPIModified(api, mediation)) {
                    API oldAPI = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain); // TODO do a deep copy
                    apiProvider.updateAPI(oldAPI, api);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }

            apiProvider.deleteApiSpecificMediationPolicy(apiId, mediationPolicyId, tenantDomain);

        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while deleting mediation policies of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API specific mediation policy : " +
                        mediationPolicyId + "of API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }

        return null;
    }

    /**
     * Returns a specific mediation policy by identifier that is belong to the given API identifier
     *
     * @param apiId             API uuid
     * @param mediationPolicyId mediation policy uuid
     * @param ifNoneMatch       If-None-Match header value
     * @return returns the matched mediation
     */
    @Override
    public Response getAPIMediationPolicyByPolicyId(String apiId, String mediationPolicyId,
            String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //Getting specified mediation policy
            Mediation mediation =
                    apiProvider.getApiSpecificMediationPolicyByPolicyId(apiId, mediationPolicyId, tenantDomain);
            if (mediation != null) {
                MediationDTO mediationDTO =
                        MediationMappingUtil.fromMediationToDTO(mediation);
                return Response.ok().entity(mediationDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while getting mediation policy with uuid " + mediationPolicyId
                                + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while getting mediation policy with uuid "
                        + mediationPolicyId + " of API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Updates an existing API specific mediation policy
     *
     * @param type             type of the mediation policy(in/out/fault)
     * @param apiId             API identifier
     * @param mediationPolicyId uuid of mediation policy
     * @param fileInputStream   input stream of mediation policy
     * @param fileDetail      mediation policy file
     * @param inlineContent   mediation policy content
     * @param ifMatch           If-match header value
     * @return updated mediation DTO as response
     */

    @Override
    public Response updateAPIMediationPolicyContentByPolicyId(String apiId, String mediationPolicyId,
                                String type, String ifMatch, InputStream fileInputStream, Attachment fileDetail,
                                                          String inlineContent, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            Mediation mediationResource = apiProvider
                    .getApiSpecificMediationPolicyByPolicyId(apiId, mediationPolicyId, tenantDomain);
            if (mediationResource != null) {

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(fileInputStream, outputStream);
                byte[] sequenceBytes = outputStream.toByteArray();
                InputStream inSequenceStream = new ByteArrayInputStream(sequenceBytes);
                String content = IOUtils.toString(inSequenceStream, StandardCharsets.UTF_8.name());
                OMElement seqElement = APIUtil.buildOMElement(new ByteArrayInputStream(sequenceBytes));
                String localName = seqElement.getLocalName();

                Mediation returnedPolicy;
                if (APIConstants.MEDIATION_SEQUENCE_ELEM.equals(localName)) {
                    Mediation mediationPolicy = new Mediation();
                    mediationPolicy.setConfig(content);
                    mediationPolicy.setName(localName);
                    mediationPolicy.setType(type);
                    mediationPolicy.setUuid(mediationPolicyId);
                    //Adding api specific mediation policy
                    returnedPolicy  = apiProvider.updateApiSpecificMediationPolicyContent(apiId, mediationPolicy, tenantDomain);
                } else {
                    throw new APIManagementException("Sequence is malformed");
                }

                if (returnedPolicy != null) {
                    String uuid = returnedPolicy.getUuid();
                    String uriString = RestApiConstants.RESOURCE_PATH_API_MEDIATION
                            .replace(RestApiConstants.APIID_PARAM, apiId) + "/" + uuid;
                    URI uri = new URI(uriString);
                    MediationDTO updatedMediationDTO =
                            MediationMappingUtil.fromMediationToDTO(returnedPolicy);

                    return Response.ok(uri).entity(updatedMediationDTO).build();
                }

            } else {
                //If registry resource not exists
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating the mediation policy with uuid " + mediationPolicyId
                                + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error occurred while updating the mediation policy with uuid " +
                        mediationPolicyId + " of API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while getting location header for uploaded " +
                    "mediation policy " + mediationPolicyId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding mediation policy", e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return null;
    }

    /**
     * Retrieve a API specific mediation policy content
     *
     * @param apiId             API identifier
     * @param mediationPolicyId uuid of mediation policy
     * @param ifNoneMatch       If-None-Match header value
     * @return updated mediation DTO as response
     */
    @Override
    public Response getAPIMediationPolicyContentByPolicyId(String apiId, String mediationPolicyId, String ifNoneMatch,
                                                           MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            // Getting resource correspond to the given uuid
            Mediation mediationResource = apiProvider.getApiSpecificMediationPolicyByPolicyId(apiId, mediationPolicyId,
                    tenantDomain);
            if (mediationResource == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MEDIATION_POLICY, mediationPolicyId,
                        log);
                return null;
            }
            Object fileDataStream = new ByteArrayInputStream(mediationResource.getConfig().getBytes());
            String name = mediationResource.getName();
            return Response.ok(fileDataStream)
                    .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_XML)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                    .build();
        } catch (APIManagementException e) {
            // Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + mediationPolicyId + " of API " + apiId,
                        e, log);
            } else {
                String errorMessage = "Error while retrieving document " + mediationPolicyId + " of the API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Add a API specific mediation policy
     *
     * @param type            Type of the mediation policy
     * @param apiId           API identifier
     * @param fileInputStream input stream of mediation policy
     * @param fileDetail      mediation policy file
     * @param inlineContent   mediation policy content
     * @param ifMatch         If-match header value
     * @return updated mediation DTO as response
     */
    @Override
    public Response addAPIMediationPolicy(String apiId, String type, String ifMatch, InputStream
            fileInputStream, Attachment fileDetail, String inlineContent, MessageContext messageContext)
            throws APIManagementException {

        String fileName = "";
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            if (fileInputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            if (!StringUtils.isEmpty(type)) {
                type.toLowerCase();
            } else {
                type = "in";
            }

            Mediation returnedPolicy = null;
            if (fileInputStream != null) {
                fileName = fileDetail.getDataHandler().getName();

                String fileContentType = URLConnection.guessContentTypeFromName(fileName);

                if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                    fileContentType = fileDetail.getContentType().toString();
                }
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(fileInputStream, outputStream);
                byte[] sequenceBytes = outputStream.toByteArray();
                InputStream inSequenceStream = new ByteArrayInputStream(sequenceBytes);
                String content = IOUtils.toString(inSequenceStream, StandardCharsets.UTF_8.name());
                returnedPolicy = PublisherCommonUtils
                        .addMediationPolicyFromFile(content, type, apiProvider, apiId, tenantDomain, null,
                                Boolean.TRUE);
            }
            if (inlineContent != null) {
                //Extracting the file name specified in the config
                fileName = this.getMediationNameFromConfig(inlineContent);
                Mediation mediationPolicy = new Mediation();
                mediationPolicy.setConfig(inlineContent);
                mediationPolicy.setName(fileName.replace(APIConstants.MEDIATION_CONFIG_EXT, ""));
                mediationPolicy.setType(type);
                returnedPolicy  = apiProvider.addApiSpecificMediationPolicy(apiId, mediationPolicy, tenantDomain);
            }

            if (returnedPolicy != null) {
                String uriString = RestApiConstants.RESOURCE_PATH_API_MEDIATION
                        .replace(RestApiConstants.APIID_PARAM, apiId)  + "/" + returnedPolicy.getUuid();
                URI uri = new URI(uriString);
                MediationDTO createdPolicy =
                        MediationMappingUtil.fromMediationToDTO(returnedPolicy);
                return Response.created(uri).entity(createdPolicy).build();
            }

        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) { //this is due to access control restriction.
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding mediation policy for the API " + apiId, e, log);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while getting location header for created " +
                    "mediation policy " + fileName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding mediation policy", e, log);
        }
        return null;
    }

    /**
     * Get API monetization status and monetized tier to billing plan mapping
     *
     * @param apiId API ID
     * @param messageContext message context
     * @return API monetization status and monetized tier to billing plan mapping
     */
    @Override
    public Response getAPIMonetization(String apiId, MessageContext messageContext) {

        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null when retrieving monetized plans.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier;
            APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiRevision.getApiUUID());
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            API api = apiProvider.getAPI(apiIdentifier);
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            Map<String, String> monetizedPoliciesToPlanMapping = monetizationImplementation.
                    getMonetizedPoliciesToPlanMapping(api);
            APIMonetizationInfoDTO monetizationInfoDTO = APIMappingUtil.getMonetizedTiersDTO
                    (apiIdentifier, monetizedPoliciesToPlanMapping);
            return Response.ok().entity(monetizationInfoDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Failed to retrieve monetized plans for API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to fetch monetized plans of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.serverError().build();
    }

    /**
     * Monetize (enable or disable) for a given API
     *
     * @param apiId API ID
     * @param body request body
     * @param messageContext message context
     * @return monetizationDTO
     */
    @Override
    public Response addAPIMonetization(String apiId, APIMonetizationInfoDTO body, MessageContext messageContext) {
        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null when configuring monetization.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            API api = apiProvider.getAPI(apiIdentifier);
            if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
                String errorMessage = "API " + apiIdentifier.getApiName() +
                        " should be in published state to configure monetization.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            //set the monetization status
            boolean monetizationEnabled = body.isEnabled();
            api.setMonetizationStatus(monetizationEnabled);
            //clear the existing properties related to monetization
            api.getMonetizationProperties().clear();
            Map<String, String> monetizationProperties = body.getProperties();
            if (MapUtils.isNotEmpty(monetizationProperties)) {
                String errorMessage = RestApiPublisherUtils.validateMonetizationProperties(monetizationProperties);
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                for (Map.Entry<String, String> currentEntry : monetizationProperties.entrySet()) {
                    api.addMonetizationProperty(currentEntry.getKey(), currentEntry.getValue());
                }
            }
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            HashMap monetizationDataMap = new Gson().fromJson(api.getMonetizationProperties().toString(), HashMap.class);
            boolean isMonetizationStateChangeSuccessful = false;
            if (MapUtils.isEmpty(monetizationDataMap)) {
                String errorMessage = "Monetization is not configured. Monetization data is empty for "
                        + apiIdentifier.getApiName();
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            try {
                if (monetizationEnabled) {
                    isMonetizationStateChangeSuccessful = monetizationImplementation.enableMonetization
                            (tenantDomain, api, monetizationDataMap);
                } else {
                    isMonetizationStateChangeSuccessful = monetizationImplementation.disableMonetization
                            (tenantDomain, api, monetizationDataMap);
                }
            } catch (MonetizationException e) {
                String errorMessage = "Error while changing monetization status for API ID : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
            if (isMonetizationStateChangeSuccessful) {
                apiProvider.configureMonetizationInAPIArtifact(api);
                APIMonetizationInfoDTO monetizationInfoDTO = APIMappingUtil.getMonetizationInfoDTO(apiIdentifier);
                return Response.ok().entity(monetizationInfoDTO).build();
            } else {
                String errorMessage = "Unable to change monetization status for API : " + apiId;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while configuring monetization for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.serverError().build();
    }

    /**
     * Publish API to given external stores.
     *
     * @param apiId API Id
     * @param externalStoreIds  External Store Ids
     * @param ifMatch   If-match header value
     * @param messageContext CXF Message Context
     * @return Response of published external store list
     */
    @Override
    public Response publishAPIToExternalStores(String apiId, String externalStoreIds, String ifMatch,
                                                         MessageContext messageContext) throws APIManagementException {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = null;
        List<String> externalStoreIdList = Arrays.asList(externalStoreIds.split("\\s*,\\s*"));
        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while getting API: " + apiId;
                log.error(errorMessage, e);
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        if (apiProvider.publishToExternalAPIStores(api, externalStoreIdList)) {
            Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(api.getId());
            APIExternalStoreListDTO apiExternalStoreListDTO =
                    ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
            return Response.ok().entity(apiExternalStoreListDTO).build();
        }
        return Response.serverError().build();
    }

    /**
     * Get the resource policies(inflow/outflow).
     *
     * @param apiId           API ID
     * @param sequenceType    sequence type('in' or 'out')
     * @param resourcePath    api resource path
     * @param verb            http verb
     * @param ifNoneMatch     If-None-Match header value
     * @return json response of the resource policies according to the resource path
     */
    @Override
    public Response getAPIResourcePolicies(String apiId, String sequenceType, String resourcePath,
            String verb, String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = provider.getLightweightAPIByUUID(apiId, tenantDomain);
            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                if (StringUtils.isEmpty(sequenceType) || !(RestApiConstants.IN_SEQUENCE.equals(sequenceType)
                        || RestApiConstants.OUT_SEQUENCE.equals(sequenceType))) {
                    String errorMessage = "Sequence type should be either of the values from 'in' or 'out'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                String resourcePolicy = SequenceUtils.getRestToSoapConvertedSequence(api, sequenceType);
                if (StringUtils.isEmpty(resourcePath) && StringUtils.isEmpty(verb)) {
                    ResourcePolicyListDTO resourcePolicyListDTO = APIMappingUtil
                            .fromResourcePolicyStrToDTO(resourcePolicy);
                    return Response.ok().entity(resourcePolicyListDTO).build();
                }
                if (StringUtils.isNotEmpty(resourcePath) && StringUtils.isNotEmpty(verb)) {
                    JSONObject sequenceObj = (JSONObject) new JSONParser().parse(resourcePolicy);
                    JSONObject resultJson = new JSONObject();
                    String key = resourcePath + "_" + verb;
                    JSONObject sequenceContent = (JSONObject) sequenceObj.get(key);
                    if (sequenceContent == null) {
                        String errorMessage = "Cannot find any resource policy for Resource path : " + resourcePath +
                                " with type: " + verb;
                        RestApiUtil.handleResourceNotFoundError(errorMessage, log);
                    }
                    resultJson.put(key, sequenceObj.get(key));
                    ResourcePolicyListDTO resourcePolicyListDTO = APIMappingUtil
                            .fromResourcePolicyStrToDTO(resultJson.toJSONString());
                    return Response.ok().entity(resourcePolicyListDTO).build();
                } else if (StringUtils.isEmpty(resourcePath)) {
                    String errorMessage = "Resource path cannot be empty for the defined verb: " + verb;
                    RestApiUtil.handleBadRequest(errorMessage, log);
                } else if (StringUtils.isEmpty(verb)) {
                    String errorMessage = "HTTP verb cannot be empty for the defined resource path: " + resourcePath;
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            } else {
                String errorMessage = "The provided api with id: " + apiId + " is not a soap to rest converted api.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving the resource policies for the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get the resource policy given the resource id.
     *
     * @param apiId           API ID
     * @param resourcePolicyId      resource policy id
     * @param ifNoneMatch     If-None-Match header value
     * @return json response of the resource policy for the resource id given
     */
    @Override
    public Response getAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
            String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = provider.getLightweightAPIByUUID(apiId, tenantDomain);
            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                if (StringUtils.isEmpty(resourcePolicyId)) {
                    String errorMessage = "Resource id should not be empty to update a resource policy.";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                String policyContent = SequenceUtils.getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
                ResourcePolicyInfoDTO resourcePolicyInfoDTO = APIMappingUtil
                        .fromResourcePolicyStrToInfoDTO(policyContent);
                return Response.ok().entity(resourcePolicyInfoDTO).build();
            } else {
                String errorMessage = "The provided api with id: " + apiId + " is not a soap to rest converted api.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Update the resource policies(inflow/outflow) given the resource id.
     *
     * @param apiId  API ID
     * @param resourcePolicyId resource policy id
     * @param body resource policy content
     * @param ifMatch If-Match header value
     * @return json response of the updated sequence content
     */
    @Override
    public Response updateAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
            ResourcePolicyInfoDTO body, String ifMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = provider.getLightweightAPIByUUID(apiId, tenantDomain);
            if (api == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(api.getStatus());

            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                if (StringUtils.isEmpty(resourcePolicyId)) {
                    String errorMessage = "Resource id should not be empty to update a resource policy.";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                boolean isValidSchema = RestApiPublisherUtils.validateXMLSchema(body.getContent());
                if (isValidSchema) {
                    List<SOAPToRestSequence> sequence = api.getSoapToRestSequences();
                    for (SOAPToRestSequence soapToRestSequence : sequence) {
                        if (soapToRestSequence.getUuid().equals(resourcePolicyId)) {
                            soapToRestSequence.setContent(body.getContent());
                            break;
                        }
                    }
                    API originalAPI = provider.getAPIbyUUID(apiId, tenantDomain);
                    provider.updateAPI(api, originalAPI);
                    String updatedPolicyContent = SequenceUtils
                            .getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
                    ResourcePolicyInfoDTO resourcePolicyInfoDTO = APIMappingUtil
                            .fromResourcePolicyStrToInfoDTO(updatedPolicyContent);
                    return Response.ok().entity(resourcePolicyInfoDTO).build();
                } else {
                    String errorMessage =
                            "Error while validating the resource policy xml content for the API : " + apiId;
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            } else {
                String errorMessage = "The provided api with id: " + apiId + " is not a soap to rest converted api.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException | FaultGatewaysException e) {
            String errorMessage = "Error while retrieving the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get total revenue for a given API from all its' subscriptions
     *
     * @param apiId API ID
     * @param messageContext message context
     * @return revenue data for a given API
     */
    @Override
    public Response getAPIRevenue(String apiId, MessageContext messageContext) {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when getting revenue details.";
            RestApiUtil.handleBadRequest(errorMessage, log);
        }
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            API api = apiProvider.getAPI(apiIdentifier);
            if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
                String errorMessage = "API " + apiIdentifier.getApiName() +
                        " should be in published state to get total revenue.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            Map<String, String> revenueUsageData = monetizationImplementation.getTotalRevenue(api, apiProvider);
            APIRevenueDTO apiRevenueDTO = new APIRevenueDTO();
            apiRevenueDTO.setProperties(revenueUsageData);
            return Response.ok().entity(apiRevenueDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Failed to retrieve revenue data for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to get current revenue data for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId           API identifier
     * @param ifNoneMatch     If-None-Match header value
     * @return Swagger document of the API
     */
    @Override
    public Response getAPISwagger(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            String updatedDefinition = RestApiCommonUtil.retrieveSwaggerDefinition(api, apiProvider);
            return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                    "attachment; filename=\"" + "swagger.json" + "\"" ).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving swagger of API : " + apiId,
                                e, log);
            } else {
                String errorMessage = "Error while retrieving swagger of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
    /**
     * Updates the swagger definition of an existing API
     *
     * @param apiId             API identifier
     * @param apiDefinition     Swagger definition
     * @param url               Swagger definition URL
     * @param fileInputStream   Swagger definition input file content
     * @param fileDetail        file meta information as Attachment
     * @param ifMatch           If-match header value
     * @return updated swagger document of the API
     */
    @Override
    public Response updateAPISwagger(String apiId, String ifMatch, String apiDefinition, String url,
                                InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) {
        try {
            String updatedSwagger;
            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().getStatus());

            //Handle URL and file based definition imports
            if(url != null || fileInputStream != null) {
                // Validate and retrieve the OpenAPI definition
                Map validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileDetail, null,
                        true, false);
                APIDefinitionValidationResponse validationResponse =
                        (APIDefinitionValidationResponse) validationResponseMap .get(RestApiConstants.RETURN_MODEL);
                if (!validationResponse.isValid()) {
                    RestApiUtil.handleBadRequest(validationResponse.getErrorItems(), log);
                }
                updatedSwagger = PublisherCommonUtils.updateSwagger(apiId, validationResponse, false);
            } else {
                updatedSwagger = updateSwagger(apiId, apiDefinition);
            }
            return Response.ok().entity(updatedSwagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating swagger definition of API: " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the swagger definition of the API: " + apiId + " - "
                        + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * update swagger definition of the given api. The swagger will be validated before updating.
     *
     * @param apiId API Id
     * @param apiDefinition swagger definition
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    private String updateSwagger(String apiId, String apiDefinition)
            throws APIManagementException, FaultGatewaysException {
        APIDefinitionValidationResponse response = OASParserUtil
                .validateAPIDefinition(apiDefinition, true);
        if (!response.isValid()) {
            RestApiUtil.handleBadRequest(response.getErrorItems(), log);
        }
        return PublisherCommonUtils.updateSwagger(apiId, response, false);
    }

    /**
     * Retrieves the thumbnail image of an API specified by API identifier
     *
     * @param apiId           API Id
     * @param ifNoneMatch     If-None-Match header value
     * @param messageContext If-Modified-Since header value
     * @return Thumbnail image of the API
     */
    @Override
    public Response getAPIThumbnail(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            ResourceFile thumbnailResource = apiProvider.getIcon(apiId, tenantDomain);

            if (thumbnailResource != null) {
                return Response
                        .ok(thumbnailResource.getContent(), MediaType.valueOf(thumbnailResource.getContentType()))
                        .build();
            } else {
                return Response.noContent().build();
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving thumbnail of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail,
            String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            String fileName = fileDetail.getDataHandler().getName();
            String extension = FilenameUtils.getExtension(fileName);
            if (!RestApiConstants.ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension.toLowerCase())) {
                RestApiUtil.handleBadRequest(
                        "Unsupported Thumbnail File Extension. Supported extensions are .jpg, .png, .jpeg .svg "
                                + "and .gif", log);
            }
            String fileContentType = URLConnection.guessContentTypeFromName(fileName);
            if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                fileContentType = fileDetail.getContentType().toString();
            }
            PublisherCommonUtils.updateThumbnail(fileInputStream, fileContentType, apiProvider, apiId, tenantDomain);
            String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, apiId);
            URI uri = new URI(uriString);
            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(fileContentType);
            return Response.created(uri).entity(infoDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while adding thumbnail for API : " + apiId,
                                e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while updating thumbnail of API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return null;
    }

    @Override
    public Response validateAPI(String query, String ifNoneMatch, MessageContext messageContext) {

        boolean isSearchArtifactExists = false;
        if (StringUtils.isEmpty(query)) {
            RestApiUtil.handleBadRequest("The query should not be empty", log);
        }
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            if (query.contains(":")) {
                String[] queryTokens = query.split(":");
                switch (queryTokens[0]) {
                case "name":
                    isSearchArtifactExists = apiProvider.isApiNameExist(queryTokens[1]) ||
                            apiProvider.isApiNameWithDifferentCaseExist(queryTokens[1]);
                    break;
                case "context":
                default: // API version validation.
                    isSearchArtifactExists = apiProvider.isContextExist(queryTokens[1]);
                    break;
                }

            } else { // consider the query as api name
                isSearchArtifactExists =
                        apiProvider.isApiNameExist(query) || apiProvider.isApiNameWithDifferentCaseExist(query);
            }
        } catch(APIManagementException e){
            RestApiUtil.handleInternalServerError("Error while checking the api existence", e, log);
        }
        return isSearchArtifactExists ? Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response validateDocument(String apiId, String name, String ifMatch, MessageContext messageContext) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(apiId)) {
            RestApiUtil.handleBadRequest("API Id and/ or document name should not be empty", log);
        }
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            return apiProvider.isDocumentationExist(apiId, name, tenantDomain) ? Response.status(Response.Status.OK).build() :
                    Response.status(Response.Status.NOT_FOUND).build();

        } catch(APIManagementException e){
            RestApiUtil.handleInternalServerError("Error while checking the api existence", e, log);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response validateEndpoint(String endpointUrl, String apiId, MessageContext messageContext) {

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        apiEndpointValidationResponseDTO.setError("");
        try {
            URL url = new URL(endpointUrl);
            if (url.getProtocol().matches("https")) {
                ServerConfiguration serverConfig = CarbonUtils.getServerConfiguration();
                String trustStorePath = serverConfig.getFirstProperty("Security.TrustStore.Location");
                String trustStorePassword = serverConfig.getFirstProperty("Security.TrustStore.Password");
                System.setProperty("javax.net.ssl.trustStore", trustStorePath);
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);

                String keyStore = serverConfig.getFirstProperty("Security.KeyStore.Location");
                String keyStoreType = serverConfig.getFirstProperty("Security.KeyStore.Type");
                String keyStorePassword = serverConfig.getFirstProperty("Security.KeyStore.Password");
                System.setProperty("javax.net.ssl.keyStoreType", keyStoreType);
                System.setProperty("javax.net.ssl.keyStore", keyStore);
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);

                /* apiId can be used to get the related API's uriTemplates. These uriTemplates can be used to extract
                the API operations and append those operations separately to the API endpoint url. This edited url can
                 be used to test the endpoint, in case their is no valid url for the sole endpoint url provided. */
                apiEndpointValidationResponseDTO = sendHttpHEADRequest(endpointUrl);
                return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
            } else if (url.getProtocol().matches("http")) {
                apiEndpointValidationResponseDTO = sendHttpHEADRequest(endpointUrl);
                return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
            }
        } catch (MalformedURLException e) {
            log.error("Malformed Url error occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationResponseDTO.setError(e.getMessage());
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("Error while testing the validity of API endpoint url " +
                    "existence", e, log);
        }
        return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
    }

    @Override
    public Response getAPIResourcePaths(String apiId, Integer limit, Integer offset, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            List<ResourcePath> apiResourcePaths = apiProvider.getResourcePathsOfAPI(apiIdentifier);

            ResourcePathListDTO dto = APIMappingUtil.fromResourcePathListToDTO(apiResourcePaths, limit, offset);
            APIMappingUtil.setPaginationParamsForAPIResourcePathList(dto, offset, limit, apiResourcePaths.size());
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving resource paths of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving resource paths of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Validate API Definition and retrieve as the response
     *
     * @param url URL of the OpenAPI definition
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param returnContent Whether to return the definition content
     * @param inlineApiDefinition Swagger API definition String
     * @param messageContext CXF message context
     * @return API Definition validation response
     */
    @Override
    public Response validateOpenAPIDefinition(Boolean returnContent, String url, InputStream fileInputStream,
                                              Attachment fileDetail, String inlineApiDefinition,
                                              MessageContext messageContext) {

        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        try {
            validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileDetail, inlineApiDefinition, returnContent, false);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO)validationResponseMap.get(RestApiConstants.RETURN_DTO);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Importing an OpenAPI definition and create an API
     *
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param url URL of the OpenAPI definition
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param inlineApiDefinition Swagger API definition String
     * @param messageContext CXF message context
     * @return API Import using OpenAPI definition response
     */
    @Override
    public Response importOpenAPIDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                            String additionalProperties, String inlineApiDefinition,
                                            MessageContext messageContext) {

        // validate 'additionalProperties' json
        if (StringUtils.isBlank(additionalProperties)) {
            RestApiUtil.handleBadRequest("'additionalProperties' is required and should not be null", log);
        }

        // Convert the 'additionalProperties' json into an APIDTO object
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
        } catch (IOException e) {
            throw RestApiUtil.buildBadRequestException("Error while parsing 'additionalProperties'", e);
        }

        // Import the API and Definition
        try {
            APIDTO createdApiDTO = importOpenAPIDefinition(fileInputStream, url, inlineApiDefinition,
                    apiDTOFromProperties, fileDetail, null);
            if (createdApiDTO != null) {
                // This URI used to set the location header of the POST response
                URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
                return Response.created(createdApiUri).entity(createdApiDTO).build();
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Validate a provided WSDL definition via a URL or a file/zip
     *
     * @param url WSDL URL
     * @param fileInputStream file/zip input stream
     * @param fileDetail file/zip details
     * @param messageContext messageContext object
     * @return WSDL validation response
     * @throws APIManagementException when error occurred during validation
     */
    @Override
    public Response validateWSDLDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
                                           MessageContext messageContext) throws APIManagementException {
        Map validationResponseMap = validateWSDL(url, fileInputStream, fileDetail);

        WSDLValidationResponseDTO validationResponseDTO =
                (WSDLValidationResponseDTO)validationResponseMap.get(RestApiConstants.RETURN_DTO);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Validate the provided input parameters and returns the validation response DTO (for REST API)
     *  and the intermediate model as a Map
     *
     * @param url WSDL url
     * @param fileInputStream file data stream
     * @param fileDetail file details
     * @return the validation response DTO (for REST API) and the intermediate model as a Map
     * @throws APIManagementException if error occurred during validation of the WSDL
     */
    private Map validateWSDL(String url, InputStream fileInputStream, Attachment fileDetail) throws APIManagementException {
        handleInvalidParams(fileInputStream, fileDetail, url, null, false);
        WSDLValidationResponseDTO responseDTO;
        WSDLValidationResponse validationResponse = new WSDLValidationResponse();

        if (url != null) {
            try {
                URL wsdlUrl = new URL(url);
                validationResponse = APIMWSDLReader.validateWSDLUrl(wsdlUrl);
            } catch (MalformedURLException e) {
                RestApiUtil.handleBadRequest("Invalid/Malformed URL : " + url, log);
            }
        } else if (fileInputStream != null) {
            String filename = fileDetail.getContentDisposition().getFilename();
            try {
                if (filename.endsWith(".zip")) {
                    validationResponse =
                            APIMWSDLReader.extractAndValidateWSDLArchive(fileInputStream);
                } else if (filename.endsWith(".wsdl")) {
                    validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
                } else {
                    RestApiUtil.handleBadRequest("Unsupported extension type of file: " + filename, log);
                }
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL from file:" + filename;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }

        responseDTO =
                APIMappingUtil.fromWSDLValidationResponseToDTO(validationResponse);

        Map response = new HashMap();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);

        return response;
    }

    /**
     * Import a WSDL file/url or an archive and create an API. The API can be a SOAP or REST depending on the
     * provided implementationType.
     *
     * @param fileInputStream file input stream
     * @param fileDetail file details
     * @param url WSDL url
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param implementationType SOAP or SOAPTOREST
     * @return Created API's payload
     * @throws APIManagementException when error occurred during the operation
     */
    @Override
    public Response importWSDLDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
            String additionalProperties, String implementationType, MessageContext messageContext)
            throws APIManagementException {
        try {
            WSDLValidationResponse validationResponse = validateWSDLAndReset(fileInputStream, fileDetail, url);

            if (StringUtils.isEmpty(implementationType)) {
                implementationType = APIDTO.TypeEnum.SOAP.toString();
            }

            boolean isSoapToRestConvertedAPI = APIDTO.TypeEnum.SOAPTOREST.toString().equals(implementationType);
            boolean isSoapAPI = APIDTO.TypeEnum.SOAP.toString().equals(implementationType);

            APIDTO additionalPropertiesAPI = null;
            APIDTO createdApiDTO;
            URI createdApiUri;

            // Minimum requirement name, version, context and endpointConfig.
            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
            String username = RestApiCommonUtil.getLoggedInUsername();
            additionalPropertiesAPI.setProvider(username);
            additionalPropertiesAPI.setType(APIDTO.TypeEnum.fromValue(implementationType));
            API apiToAdd = PublisherCommonUtils
                    .prepareToCreateAPIByDTO(additionalPropertiesAPI, RestApiCommonUtil.getLoggedInUserProvider(),
                            username);
            apiToAdd.setWsdlUrl(url);
            API createdApi = null;
            if (isSoapAPI) {
                createdApi = importSOAPAPI(fileInputStream, fileDetail, url, apiToAdd);
            } else if (isSoapToRestConvertedAPI) {
                String wsdlArchiveExtractedPath = null;
                if (validationResponse.getWsdlArchiveInfo() != null) {
                    wsdlArchiveExtractedPath = validationResponse.getWsdlArchiveInfo().getLocation()
                            + File.separator + APIConstants.API_WSDL_EXTRACTED_DIRECTORY;
                }
                createdApi = importSOAPToRESTAPI(fileInputStream, fileDetail, url, wsdlArchiveExtractedPath, apiToAdd);
            } else {
                RestApiUtil.handleBadRequest("Invalid implementationType parameter", log);
            }
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (IOException | URISyntaxException e) {
            RestApiUtil.handleInternalServerError("Error occurred while importing WSDL", e, log);
        }
        return null;
    }

    /**
     * Validates the provided WSDL and reset the streams as required
     *
     * @param fileInputStream file input stream
     * @param fileDetail file details
     * @param url WSDL url
     * @throws APIManagementException when error occurred during the operation
     */
    private WSDLValidationResponse validateWSDLAndReset(InputStream fileInputStream, Attachment fileDetail, String url)
            throws APIManagementException {
        Map validationResponseMap = validateWSDL(url, fileInputStream, fileDetail);
        WSDLValidationResponse validationResponse =
                (WSDLValidationResponse)validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (validationResponse.getWsdlInfo() == null) {
            // Validation failure
            RestApiUtil.handleBadRequest(validationResponse.getError(), log);
        }

        if (fileInputStream != null) {
            if (fileInputStream.markSupported()) {
                // For uploading the WSDL below will require re-reading from the input stream hence resetting
                try {
                    fileInputStream.reset();
                } catch (IOException e) {
                    throw new APIManagementException("Error occurred while trying to reset the content stream of the " +
                            "WSDL", e);
                }
            } else {
                log.warn("Marking is not supported in 'fileInputStream' InputStream type: "
                        + fileInputStream.getClass() + ". Skipping validating WSDL to avoid re-reading from the " +
                        "input stream.");
            }
        }
        return validationResponse;
    }

    /**
     * Import an API from WSDL as a SOAP API
     *
     * @param fileInputStream file data as input stream
     * @param fileDetail file details
     * @param url URL of the WSDL
     * @param apiToAdd API object to be added to the system (which is not added yet)
     * @return API added api
     */
    private API importSOAPAPI(InputStream fileInputStream, Attachment fileDetail, String url, API apiToAdd) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //adding the api
            apiProvider.addAPI(apiToAdd);
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            if (StringUtils.isNotBlank(url)) {
                apiToAdd.setWsdlUrl(url);
                apiProvider.addWSDLResource(apiToAdd.getUuid(), null, url, tenantDomain);
            } else if (fileDetail != null && fileInputStream != null) {
                PublisherCommonUtils
                        .addWsdl(fileDetail.getContentType().toString(), fileInputStream, apiToAdd, apiProvider,
                                tenantDomain);
            }

            //add the generated swagger definition to SOAP
            APIDefinition oasParser = new OAS2Parser();
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = generateSOAPAPIDefinition(oasParser.generateAPIDefinition(swaggerData));
            apiProvider.saveSwaggerDefinition(apiToAdd, apiDefinition, tenantDomain);
            APIIdentifier createdApiId = apiToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);
            return createdApi;
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while importing WSDL to create a SOAP API", e, log);
        }
        return null;
    }

    /**
     * Add soap parameters to the default soap api resource.
     *
     * @param apiDefinition The API definition string.
     * @return Modified api definition.
     * */
    private String generateSOAPAPIDefinition(String apiDefinition) throws APIManagementException {
        JSONParser jsonParser = new JSONParser();
        JSONObject apiJson;
        JSONObject paths;
        try {
            apiJson = (JSONObject) jsonParser.parse(apiDefinition);
            paths = (JSONObject) jsonParser.parse(RestApiPublisherUtils.getSOAPOperation());
            apiJson.replace("paths", paths);
            return apiJson.toJSONString();
        } catch (ParseException e) {
            throw new APIManagementException("Error while parsing the api definition.", e);
        }
    }

    /**
     * Import an API from WSDL as a SOAP-to-REST API
     *
     * @param fileInputStream file data as input stream
     * @param fileDetail file details
     * @param url URL of the WSDL
     * @param apiToAdd API object to be added to the system (which is not added yet)
     * @return API added api
     */
    private API importSOAPToRESTAPI(InputStream fileInputStream, Attachment fileDetail, String url,
            String wsdlArchiveExtractedPath, API apiToAdd) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //adding the api
            API createdApi = apiProvider.addAPI(apiToAdd);

            String swaggerStr = "";
            if (StringUtils.isNotBlank(url)) {
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(url);
            } else if (fileInputStream != null) {
                String filename = fileDetail.getContentDisposition().getFilename();
                if (filename.endsWith(".zip")) {
                    swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlArchiveExtractedPath);;
                } else if (filename.endsWith(".wsdl")) {
                    byte[] wsdlContent = APIUtil.toByteArray(fileInputStream);
                    swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlContent);
                } else {
                    throw new APIManagementException(ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
                }
            }
            String updatedSwagger = updateSwagger(createdApi.getUUID(), swaggerStr);
            return PublisherCommonUtils
                    .updateAPIBySettingGenerateSequencesFromSwagger(updatedSwagger, createdApi, apiProvider,
                            tenantDomain);
        } catch (FaultGatewaysException | IOException e) {
            throw new APIManagementException("Error while importing WSDL to create a SOAP-to-REST API", e);
        }
    }

    /**
     * Retrieve the WSDL of an API
     *
     * @param apiId UUID of the API
     * @param ifNoneMatch If-None-Match header value
     * @return the WSDL of the API (can be a file or zip archive)
     * @throws APIManagementException when error occurred while trying to retrieve the WSDL
     */
    @Override
    public Response getWSDLOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            ResourceFile resource = apiProvider.getWSDL(apiId, tenantDomain);
            return RestApiUtil.getResponseFromResourceFile(resource.getName(), resource);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving wsdl of API: "
                                        + apiId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    /**
     * Update the WSDL of an API
     *
     * @param apiId UUID of the API
     * @param fileInputStream file data as input stream
     * @param fileDetail file details
     * @param url URL of the WSDL
     * @return 200 OK response if the operation is successful. 400 if the provided inputs are invalid. 500 if a server
     *  error occurred.
     * @throws APIManagementException when error occurred while trying to retrieve the WSDL
     */
    @Override
    public Response updateWSDLOfAPI(String apiId, String ifMatch, InputStream fileInputStream, Attachment fileDetail,
                                    String url, MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        //validate if api exists
        APIInfo apiInfo = validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        validateWSDLAndReset(fileInputStream, fileDetail, url);
        if (StringUtils.isNotBlank(url)) {
            apiProvider.addWSDLResource(apiId, null, url, tenantDomain);
        } else {
            ResourceFile wsdlResource;
            if (APIConstants.APPLICATION_ZIP.equals(fileDetail.getContentType().toString()) ||
                    APIConstants.APPLICATION_X_ZIP_COMPRESSED.equals(fileDetail.getContentType().toString())) {
                wsdlResource = new ResourceFile(fileInputStream, APIConstants.APPLICATION_ZIP);
            } else {
                wsdlResource = new ResourceFile(fileInputStream, fileDetail.getContentType().toString());
            }
            apiProvider.addWSDLResource(apiId, wsdlResource, null, tenantDomain);
        }
        return Response.ok().build();
    }

    @Override
    public Response changeAPILifecycle(String action, String apiId, String lifecycleChecklist,
                                            String ifMatch, MessageContext messageContext) {
        //pre-processing
        String[] checkListItems = lifecycleChecklist != null ? lifecycleChecklist.split(",") : new String[0];

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            Map<String, Object> apiLCData = apiProvider.getAPILifeCycleData(apiId, tenantDomain);
            String[] nextAllowedStates = (String[]) apiLCData.get(APIConstants.LC_NEXT_STATES);
            if (!ArrayUtils.contains(nextAllowedStates, action)) {
                RestApiUtil.handleBadRequest(
                        "Action '" + action + "' is not allowed. Allowed actions are " + Arrays
                                .toString(nextAllowedStates), log);
            }

            //check and set lifecycle check list items including "Deprecate Old Versions" and "Require Re-Subscription".
            Map<String, Boolean> lcMap = new HashMap<String, Boolean>();
            for (String checkListItem : checkListItems) {
                String[] attributeValPair = checkListItem.split(":");
                if (attributeValPair.length == 2) {
                    String checkListItemName = attributeValPair[0].trim();
                    boolean checkListItemValue = Boolean.valueOf(attributeValPair[1].trim());
                    lcMap.put(checkListItemName, checkListItemValue);
                    //apiProvider.checkAndChangeAPILCCheckListItem(apiIdentifier, checkListItemName, checkListItemValue);
                }
            }

            //todo: check if API's tiers are properly set before Publishing
            //APIStateChangeResponse stateChangeResponse = apiProvider.changeLifeCycleStatus(apiIdentifier, action.toString());
            APIStateChangeResponse stateChangeResponse = apiProvider.changeLifeCycleStatus(tenantDomain, apiId, action.toString(),
                    lcMap);

            //returns the current lifecycle state
            LifecycleStateDTO stateDTO = getLifecycleState(apiIdentifier, apiId); // todo try to prevent this call

            WorkflowResponseDTO workflowResponseDTO = APIMappingUtil
                    .toWorkflowResponseDTO(stateDTO, stateChangeResponse);
            return Response.ok().entity(workflowResponseDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating the lifecycle of API " + apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while updating lifecycle of API " + apiId, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating the API in Gateway " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response createNewAPIVersion(String newVersion, String apiId, Boolean defaultVersion,
                String serviceVersion, MessageContext messageContext) throws APIManagementException {
        URI newVersionedApiUri;
        APIDTO newVersionedApi = new APIDTO();
        try {
            APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            String username = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(username);
            API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            if (existingAPI == null) {
                throw new APIMgtResourceNotFoundException("API not found for id " + apiId,
                        ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
            }
            if (newVersion.equals(existingAPI.getId().getVersion())) {
                throw new APIMgtResourceAlreadyExistsException("Version " + newVersion + " exists for api "
                        + existingAPI.getId().getApiName(), ExceptionCodes.from(API_VERSION_ALREADY_EXISTS, newVersion,
                            existingAPI.getId().getApiName()));
            }
            if (StringUtils.isNotEmpty(serviceVersion)) {
                String serviceName = existingAPI.getServiceInfo("name");
                ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
                ServiceEntry service = serviceCatalog.getServiceByNameAndVersion(serviceName, serviceVersion, tenantId);
                APIDTO apidto = createAPIDTO(existingAPI, newVersion);
                if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) || ServiceEntry
                        .DefinitionType.OAS3.equals(service.getDefinitionType())) {
                    newVersionedApi = importOpenAPIDefinition(service.getEndpointDef(), null, null, apidto,
                            null, service);
                } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                    newVersionedApi = importAsyncAPISpecification(service.getEndpointDef(), null, apidto,
                            null, service);
                }
            } else {
                API versionedAPI = apiProvider.createNewAPIVersion(apiId, newVersion, defaultVersion, tenantDomain);
                newVersionedApi = APIMappingUtil.fromAPItoDTO(versionedAPI);
            }
            //This URI used to set the location header of the POST response
            newVersionedApiUri =
                    new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + newVersionedApi.getId());
            return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while copying API : " + apiId, e, log);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location of " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
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
    @Override public Response exportAPI(String apiId, String name, String version, String revisionNum,
                                        String providerName, String format, Boolean preserveStatus,
                                        Boolean exportLatestRevision, MessageContext messageContext) {

        //If not specified status is preserved by default
        preserveStatus = preserveStatus == null || preserveStatus;

        // Default export format is YAML
        ExportFormat exportFormat = StringUtils.isNotEmpty(format) ?
                ExportFormat.valueOf(format.toUpperCase()) :
                ExportFormat.YAML;
        try {
            ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
            File file = importExportAPI
                    .exportAPI(apiId, name, version, revisionNum, providerName, preserveStatus, exportFormat,
                            Boolean.TRUE, Boolean.TRUE, exportLatestRevision, StringUtils.EMPTY);
            return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getName() + "\"").build();
        } catch (APIManagementException | APIImportExportException e) {
            RestApiUtil.handleInternalServerError("Error while exporting " + RestApiConstants.RESOURCE_API, e, log);
        }
        return null;
    }

    @Override
    public Response generateInternalAPIKey(String apiId, MessageContext messageContext) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
        String token = apiProvider.generateApiKey(apiId);
        APIKeyDTO apiKeyDTO = new APIKeyDTO();
        apiKeyDTO.setApikey(token);
        apiKeyDTO.setValidityTime(60 * 1000);
        return Response.ok().entity(apiKeyDTO).build();
    }

    /**
     * Import a GraphQL Schema
     * @param type APIType
     * @param fileInputStream input file
     * @param fileDetail file Detail
     * @param additionalProperties api object as string format
     * @param ifMatch If--Match header value
     * @param messageContext messageContext
     * @return Response with GraphQL API
     */
    @Override
    public Response importGraphQLSchema(String ifMatch, String type, InputStream fileInputStream,
                                Attachment fileDetail, String additionalProperties, MessageContext messageContext) {
        APIDTO additionalPropertiesAPI = null;
        String schema = "";

        try {
            if (fileInputStream == null || StringUtils.isBlank(additionalProperties)) {
                String errorMessage = "GraphQL schema and api details cannot be empty.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            } else {
                schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            }

            if (!StringUtils.isBlank(additionalProperties) && !StringUtils.isBlank(schema)) {
                if (log.isDebugEnabled()) {
                    log.debug("Deseriallizing additionalProperties: " + additionalProperties + "/n"
                            + "importing schema: " + schema);
                }
            }

            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
            additionalPropertiesAPI.setType(APIDTO.TypeEnum.GRAPHQL);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(additionalPropertiesAPI, apiProvider,
                    RestApiCommonUtil.getLoggedInUsername());


            //Save swagger definition of graphQL
            APIDefinition parser = new OAS3Parser();
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = parser.generateAPIDefinition(swaggerData);
            apiToAdd.setSwaggerDefinition(apiDefinition);
            //adding the api
            API createdApi = apiProvider.addAPI(apiToAdd);
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

            apiProvider.saveGraphqlSchemaDefinition(createdApi.getUuid(), schema, tenantDomain);

            APIDTO createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);

            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + additionalPropertiesAPI.getProvider() + "-" +
                additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + additionalPropertiesAPI.getProvider() + "-"
                    + additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
    } catch (IOException e) {
            String errorMessage = "Error while retrieving content from file : " + additionalPropertiesAPI.getProvider()
                    + "-" + additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
    }
        return null;
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
    @Override public Response importAPI(InputStream fileInputStream, Attachment fileDetail,
            Boolean preserveProvider, Boolean rotateRevision, Boolean overwrite, MessageContext messageContext) throws APIManagementException {
        // Check whether to update. If not specified, default value is false.
        overwrite = overwrite == null ? false : overwrite;

        // Check if the URL parameter value is specified, otherwise the default value is true.
        preserveProvider = preserveProvider == null || preserveProvider;

        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
        importExportAPI.importAPI(fileInputStream, preserveProvider, rotateRevision, overwrite, tokenScopes);
        return Response.status(Response.Status.OK).entity("API imported successfully.").build();
    }

    /**
     * Validate graphQL Schema
     * @param fileInputStream  input file
     * @param fileDetail file Detail
     * @param messageContext messageContext
     * @return Validation response
     */
    @Override
    public Response validateGraphQLSchema(InputStream fileInputStream, Attachment fileDetail,
                                          MessageContext messageContext) {

        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();
        String filename = fileDetail.getContentDisposition().getFilename();

        try {
            String schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            validationResponse = PublisherCommonUtils.validateGraphQLSchema(filename, schema);
        } catch (IOException | APIManagementException e) {
            validationResponse.setIsValid(false);
            validationResponse.setErrorMessage(e.getMessage());
        }
        return Response.ok().entity(validationResponse).build();
    }

    /**
     * Generates Mock response examples for Inline prototyping
     * of a swagger
     *
     * @param apiId API Id
     * @param ifNoneMatch If-None-Match header value
     * @param messageContext message context
     * @return apiDefinition
     * @throws APIManagementException
     */
    @Override
    public Response generateMockScripts(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifierFromTable == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);

        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, tenantDomain);
        apiDefinition = String.valueOf(OASParserUtil.generateExamples(apiDefinition).get(APIConstants.SWAGGER));
        apiProvider.saveSwaggerDefinition(originalAPI, apiDefinition, tenantDomain);
        return Response.ok().entity(apiDefinition).build();
    }

    @Override
    public Response getAPISubscriptionPolicies(String apiId, String ifNoneMatch, String xWSO2Tenant,
                                                     MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIDTO apiInfo = getAPIByID(apiId, apiProvider);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl()
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(), true);

        if (apiInfo != null ) {
            List<String> apiPolicies = apiInfo.getPolicies();
            if (apiPolicies != null && !apiPolicies.isEmpty()) {
                List<Tier> apiThrottlingPolicies = new ArrayList<>();
                for (Tier tier : availableThrottlingPolicyList) {
                    if (apiPolicies.contains(tier.getName())) {
                        apiThrottlingPolicies.add(tier);
                    }
                }
                return Response.ok().entity(apiThrottlingPolicies).build();
            }
        }
        return null;
    }

    private APIDTO getAPIByID(String apiId, APIProvider apiProvider) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);

            return APIMappingUtil.fromAPItoDTO(api, apiProvider);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the API", e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    private APIDTO createAPIDTO(API existingAPI, String newVersion) {
        APIDTO apidto = new APIDTO();
        apidto.setName(existingAPI.getId().getApiName());
        apidto.setContext(existingAPI.getContext());
        apidto.setVersion(newVersion);
        return apidto;
    }

    /**
     * Validate the provided OpenAPI definition (via file or url) and return a Map with the validation response
     * information.
     *
     * @param url OpenAPI definition url
     * @param fileInputStream file as input stream
     * @param apiDefinition Swagger API definition String
     * @param returnContent whether to return the content of the definition in the response DTO
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     *  of type OpenAPIDefinitionValidationResponseDTO for the REST API. A value with key 'model' will have the
     *  validation response of type APIDefinitionValidationResponse coming from the impl level.
     */
    private Map validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
                                          String apiDefinition, Boolean returnContent, Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileDetail, url, apiDefinition, isServiceAPI);

        OpenAPIDefinitionValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (fileInputStream != null) {
            try {
                if (fileDetail != null) {
                    String filename = fileDetail.getContentDisposition().getFilename();
                    if (filename.endsWith(".zip")) {
                        validationResponse =
                                OASParserUtil.extractAndValidateOpenAPIArchive(fileInputStream, returnContent);
                    } else {
                        String openAPIContent = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
                    validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                }
            }  catch (IOException e) {
                RestApiUtil.handleInternalServerError("Error while reading file content", e, log);
            }
        } else if (apiDefinition != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(apiDefinition, returnContent);
        }
        responseDTO = APIMappingUtil.getOpenAPIDefinitionValidationResponseFromModel(validationResponse,
                returnContent);

        Map response = new HashMap();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * Validate API import definition/validate definition parameters
     *
     * @param fileInputStream file content stream
     * @param url             URL of the definition
     * @param apiDefinition   Swagger API definition String
     */
    private void handleInvalidParams(InputStream fileInputStream, Attachment fileDetail, String url,
                                     String apiDefinition, Boolean isServiceAPI) {

        String msg = "";
        boolean isFileSpecified = (fileInputStream != null && fileDetail != null &&
                fileDetail.getContentDisposition() != null && fileDetail.getContentDisposition().getFilename() != null)
                || (fileInputStream != null && isServiceAPI);
        if (url == null && !isFileSpecified && apiDefinition == null) {
            msg = "One out of 'file' or 'url' or 'inline definition' should be specified";
        }

        boolean isMultipleSpecificationGiven = (isFileSpecified && url != null) | (isFileSpecified &&
                apiDefinition != null) | (apiDefinition != null && url != null);
        if (isMultipleSpecificationGiven) {
            msg = "Only one of 'file', 'url', and 'inline definition' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            RestApiUtil.handleBadRequest(msg, log);
        }
    }

    /**
     * To check whether a particular exception is due to access control restriction.
     *
     * @param e Exception object.
     * @return true if the the exception is caused due to authorization failure.
     */
    private boolean isAuthorizationFailure(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE);
    }

    /***
     * To check if the API is modified or not when the given sequence is in API.
     *
     * @param api
     * @param mediation
     * @return if the API is modified or not
     */
    private boolean isAPIModified(API api, Mediation mediation) {

        if (mediation != null) {
            String sequenceName;
            if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equalsIgnoreCase(mediation.getType())) {
                sequenceName = api.getInSequence();
                if (isSequenceExistsInAPI(sequenceName, mediation)) {
                    api.setInSequence(null);
                    return true;
                }
            } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equalsIgnoreCase(mediation.getType())) {
                sequenceName = api.getOutSequence();
                if (isSequenceExistsInAPI(sequenceName, mediation)) {
                    api.setOutSequence(null);
                    return true;
                }
            } else {
                sequenceName = api.getFaultSequence();
                if (isSequenceExistsInAPI(sequenceName, mediation)) {
                    api.setFaultSequence(null);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isSequenceExistsInAPI(String sequenceName, Mediation mediation) {

        return StringUtils.isNotEmpty(sequenceName) && mediation.getName().equals(sequenceName);
    }

    /**
     * Returns the mediation policy name specify inside mediation config
     *
     * @param config mediation config content
     * @return name of the mediation policy or null
     */
    public String getMediationNameFromConfig(String config) {
        try {
            //convert xml content in to json
            String configInJson = XML.toJSONObject(config).toString();
            JSONParser parser = new JSONParser();
            //Extracting mediation policy name from the json string
            JSONObject jsonObject = (JSONObject) parser.parse(configInJson);
            JSONObject rootObject = (JSONObject) jsonObject.get(APIConstants.MEDIATION_SEQUENCE_ELEM);
            String name = rootObject.get(APIConstants.POLICY_NAME_ELEM).toString();
            return name + APIConstants.MEDIATION_CONFIG_EXT;
        } catch (JSONException e) {
            log.error("JSON Error occurred while converting the mediation config string to json", e);
        } catch (ParseException e) {
            log.error("Parser Error occurred while parsing config json string in to json object", e);
        }
        return null;
    }

    /**
     * Check the existence of the mediation policy
     *
     * @param mediationResourcePath mediation config content
     */
    public void checkMediationPolicy(APIProvider apiProvider, String mediationResourcePath, String name) throws APIManagementException {
        if (apiProvider.checkIfResourceExists(mediationResourcePath)) {
            throw new APIManagementException(ExceptionCodes.MEDIATION_POLICY_API_ALREADY_EXISTS);
        }
        if (StringUtils.isNotBlank(name) && name.length() > APIConstants.MAX_LENGTH_MEDIATION_POLICY_NAME) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.MEDIATION_POLICY_NAME_TOO_LONG,
                    APIConstants.MAX_LENGTH_MEDIATION_POLICY_NAME + ""));
        }
    }

    /**
     * Send HTTP HEAD request to test the endpoint url
     *
     * @param urlVal url for which the HEAD request is sent
     * @return ApiEndpointValidationResponseDTO Response DTO containing validity information of the HEAD request made
     * to test the endpoint url
     */
    public static ApiEndpointValidationResponseDTO sendHttpHEADRequest(String urlVal) {

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        HttpHead head = new HttpHead(urlVal);
        org.apache.commons.httpclient.HttpClient client = new org.apache.commons.httpclient.HttpClient();
        // extract the host name and add the Host http header for sanity
        head.addHeader("Host", urlVal.replaceAll("https?://", "").
                replaceAll("(/.*)?", ""));
        client.getParams().setParameter("http.socket.timeout", 4000);
        client.getParams().setParameter("http.connection.timeout", 4000);
        HttpMethod method = new HeadMethod(urlVal);

        if (System.getProperty(APIConstants.HTTP_PROXY_HOST) != null &&
                System.getProperty(APIConstants.HTTP_PROXY_PORT) != null) {
            log.debug("Proxy configured, hence routing through configured proxy");
            String proxyHost = System.getProperty(APIConstants.HTTP_PROXY_HOST);
            String proxyPort = System.getProperty(APIConstants.HTTP_PROXY_PORT);
            HostConfiguration hostConfiguration = client.getHostConfiguration();
            hostConfiguration.setProxy(proxyHost, Integer.parseInt(proxyPort));
            client.setHostConfiguration(hostConfiguration);
        }
        try {
            int statusCode = client.executeMethod(method);
            apiEndpointValidationResponseDTO.setStatusCode(statusCode);
            apiEndpointValidationResponseDTO.setStatusMessage(HttpStatus.getStatusText(statusCode));
        } catch (UnknownHostException e) {
            log.error("UnknownHostException occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationResponseDTO.setError("Unknown Host");
        } catch (IOException e) {
            log.error("Error occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationResponseDTO.setError("Connection error");
        } finally {
            method.releaseConnection();
        }
        return apiEndpointValidationResponseDTO;
    }

    /**
     * Retrieve available revisions of an API
     *
     * @param apiId UUID of the API
     * @param query Search query string
     * @param messageContext    message context object
     * @return response containing list of API revisions
     */
    @Override
    public Response getAPIRevisions(String apiId, String query, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevisionListDTO apiRevisionListDTO;
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
            if (StringUtils.equalsIgnoreCase(query, "deployed:true")) {
                List<APIRevision> apiDeployedRevisions = new ArrayList<>();
                for (APIRevision apiRevision : apiRevisions) {
                    if (apiRevision.getApiRevisionDeploymentList().size() != 0) {
                        apiDeployedRevisions.add(apiRevision);
                    }
                }
                apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiDeployedRevisions);
            } else {
                apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
            }
            return Response.ok().entity(apiRevisionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding retrieving API Revision for api id : " + apiId + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Create a new API revision
     *
     * @param apiId             UUID of the API
     * @param apIRevisionDTO    API object that needs to be added
     * @param messageContext    message context object
     * @return response containing newly created APIRevision object
     */
    @Override
    public Response createAPIRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            APIRevision apiRevision = new APIRevision();
            apiRevision.setApiUUID(apiId);
            apiRevision.setDescription(apIRevisionDTO.getDescription());
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //adding the api revision
            String revisionId = apiProvider.addAPIRevision(apiRevision, tenantDomain);

            //Retrieve the newly added APIRevision to send in the response payload
            APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
            APIRevisionDTO createdApiRevisionDTO = APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API Revision for API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving created revision API location for API : "
                    + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieve a revision of an API
     *
     * @param apiId             UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext    message context object
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
     * @param apiId             UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext    message context object
     * @return response with 204 status code and no content
     */
    @Override
    public Response deleteAPIRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        //validate if api exists
        APIInfo apiInfo = validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.deleteAPIRevision(apiId, revisionId, tenantDomain);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        APIRevisionListDTO apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    /**
     * Deploy a revision
     *
     * @param apiId             UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext    message context object
     * @return response with 200 status code
     */
    @Override
    public Response deployAPIRevision(String apiId, String revisionId,
                                      List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                      MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        APIInfo apiInfo = validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        Map<String, Environment> environments = APIUtil.getEnvironments();
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
            APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
            apiRevisionDeployment.setRevisionUUID(revisionId);
            String environment = apiRevisionDeploymentDTO.getName();
            if (environments.get(environment) == null) {
                RestApiUtil.handleBadRequest("Gateway environment not found: " + environment, log);
            }
            apiRevisionDeployment.setDeployment(environment);
            apiRevisionDeployment.setVhost(apiRevisionDeploymentDTO.getVhost());
            if (StringUtils.isEmpty(apiRevisionDeploymentDTO.getVhost())) {
                // vhost is only required when deploying an revision, not required when un-deploying a revision
                // since the same scheme 'APIRevisionDeployment' is used for deploy and undeploy, handle it here.
                RestApiUtil.handleBadRequest(
                        "Required field 'vhost' not found in deployment", log
                );
            }
            apiRevisionDeployment.setDisplayOnDevportal(apiRevisionDeploymentDTO.isDisplayOnDevportal());
            apiRevisionDeployments.add(apiRevisionDeployment);
        }
        apiProvider.deployAPIRevision(apiId, revisionId, apiRevisionDeployments);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Get revision deployment list
     *
     * @param apiId             UUID of the API
     * @param messageContext    message context object
     * @return response with 200 status code
     */
    @Override
    public Response getAPIRevisionDeployments(String apiId, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = new ArrayList<>();
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        for (APIRevision apiRevision : apiRevisions) {
            List<APIRevisionDeployment> apiRevisionDeploymentsResponse =
                    apiProvider.getAPIRevisionDeploymentList(apiRevision.getRevisionUUID());
            for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
                apiRevisionDeploymentsList.add(apiRevisionDeployment);
            }
        }
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return Response.ok().entity(apiRevisionDeploymentDTOS).build();
    }

    @Override
    public Response undeployAPIRevision(String apiId, String revisionId, String revisionNum, Boolean allEnvironments,
                                        List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                        MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        APIInfo apiInfo = validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        if (revisionId == null && revisionNum != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNum, apiId);
            if (revisionId == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(null).build();
            }
        }

        Map<String, Environment> environments = APIUtil.getEnvironments();
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        if (allEnvironments) {
            apiRevisionDeployments = apiProvider.getAPIRevisionDeploymentList(revisionId);
        } else {
            for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                apiRevisionDeployment.setRevisionUUID(revisionId);
                String environment = apiRevisionDeploymentDTO.getName();
                if (environments.get(environment) == null) {
                    RestApiUtil.handleBadRequest("Gateway environment not found: " + environment, log);
                }
                apiRevisionDeployment.setDeployment(environment);
                apiRevisionDeployment.setVhost(apiRevisionDeploymentDTO.getVhost());
                apiRevisionDeployment.setDisplayOnDevportal(apiRevisionDeploymentDTO.isDisplayOnDevportal());
                apiRevisionDeployments.add(apiRevisionDeployment);
            }
        }
        apiProvider.undeployAPIRevisionDeployment(apiId, revisionId, apiRevisionDeployments);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Restore a revision to the working copy of the API
     *
     * @param apiId          UUID of the API
     * @param revisionId  Revision ID of the API
     * @param messageContext message context object
     * @return response with 200 status code
     */
    @Override
    public Response restoreAPIRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        //validate if api exists
        APIInfo apiInfo = validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.restoreAPIRevision(apiId, revisionId, tenantDomain);
        APIDTO apiToReturn = getAPIByID(apiId, apiProvider);
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    /**
     * Validate AsyncAPI Specification and retrieve as the response
     *
     * @param url URL of the AsyncAPI Specification
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param returnContent Whether to return the definition content
     * @param messageContext CXF message context
     * @return AsyncAPI Specification Validation response
     */
    @Override
    public Response validateAsyncAPISpecification(Boolean returnContent, String url, InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException {
        //validate and retrieve the AsyncAPI specification
        Map validationResponseMap = null;
        try {
            validationResponseMap = validateAsyncAPISpecification(url, fileInputStream, fileDetail, returnContent,
                    false);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        AsyncAPISpecificationValidationResponseDTO validationResponseDTO =
                (AsyncAPISpecificationValidationResponseDTO)validationResponseMap.get(RestApiConstants.RETURN_DTO);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Validate the provided AsyncAPI specification (via file or url) and return a Map with the validation response
     * information
     *
     * @param url AsyncAPI specification url
     * @param fileInputStream file as input stream
     * @param returnContent whether to return the content of the definition in the response DTO
     * @param isServiceAPI whether the request is to create API from a service in Service Catalog
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     *  of type AsyncAPISpecificationValidationResponseDTO for the REST API. A value with the key 'model' will have the
     *  validation response of type APIDefinitionValidationResponse coming from the impl level
     */
    private Map validateAsyncAPISpecification(String url, InputStream fileInputStream, Attachment fileDetail,
                                          Boolean returnContent, Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileDetail, url, null, isServiceAPI);

        AsyncAPISpecificationValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        String schemaToBeValidated = null;

        if (url != null) {
            //validate URL
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(url, returnContent);
        } else if (fileInputStream != null){
            //validate file
            String fileName = fileDetail != null ? fileDetail.getContentDisposition().getFilename() : StringUtils.EMPTY;
            try {
                if (isServiceAPI || fileName.endsWith(APIConstants.YAML_FILE_EXTENSION) || fileName
                        .endsWith(APIConstants.YML_FILE_EXTENSION)){
                    //convert .yml or .yaml to JSON for validation
                    ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
                    Object obj = yamlReader.readValue(fileInputStream, Object.class);
                    ObjectMapper jsonWriter = new ObjectMapper();
                    schemaToBeValidated = jsonWriter.writeValueAsString(obj);
                } else if (fileName.endsWith(APIConstants.JSON_FILE_EXTENSION)){
                    //continue with .json
                    JSONTokener jsonDataFile = new JSONTokener(fileInputStream);
                    schemaToBeValidated = new org.json.JSONObject(jsonDataFile).toString();
                }
                validationResponse = AsyncApiParserUtil.validateAsyncAPISpecification(schemaToBeValidated, returnContent);
            } catch (IOException e){
                //error while reading the schemas
                RestApiUtil.handleInternalServerError("Error while reading file content", e, log);
            }
        }

        responseDTO = APIMappingUtil.getAsyncAPISpecificationValidationResponseFromModel(validationResponse, returnContent);

        Map response = new HashMap();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * Importing and AsyncAPI Specification and create and API
     *
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param url URL of the AsyncAPI Specification
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param messageContext CXF message context
     * @return API import using AsyncAPI specification response
     */
    @Override
    public Response importAsyncAPISpecification(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext) throws APIManagementException {
        // validate 'additionalProperties' json
        if (StringUtils.isBlank(additionalProperties)) {
            RestApiUtil.handleBadRequest("'additionalProperties' is required and should not be null", log);
        }

        // Convert the 'additionalProperties' json into an APIDTO object
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
            if (apiDTOFromProperties.getType() == null) {
                RestApiUtil.handleBadRequest("Required property protocol is not specified for the Async API", log);
            }
        } catch (IOException e) {
            throw RestApiUtil.buildBadRequestException("Error while parsing 'additionalProperties'", e);
        }

        //validate websocket url and change transport types
        if (PublisherCommonUtils.isValidWSAPI(apiDTOFromProperties)){
            ArrayList<String> websocketTransports = new ArrayList<>();
            websocketTransports.add(APIConstants.WS_PROTOCOL);
            websocketTransports.add(APIConstants.WSS_PROTOCOL);
            apiDTOFromProperties.setTransport(websocketTransports);
        }

        //Only WS type APIs should be allowed
        /*if (!APIDTO.TypeEnum.WS.equals(apiDTOFromProperties.getType())){
            throw RestApiUtil.buildBadRequestException("The API's type should only be WebSocket when "+
                    "importing an AsyncAPI specification");
        }*/

        //Import the API and Definition
        try {
            APIDTO createdAPIDTO = importAsyncAPISpecification(fileInputStream, url, apiDTOFromProperties, fileDetail,
                    null);
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdAPIDTO.getId());
            return Response.created(createdApiUri).entity(createdAPIDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apisApiIdAsyncapiGet(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            //String asyncAPIString = apiProvider.getAsyncAPIDefinition(apiIdentifier);
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            String updatedDefinition = RestApiCommonUtil.retrieveAsyncAPIDefinition(api, apiProvider);
            return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                    "attachment; fileNme=\"" + "asyncapi.json" + "\"").build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant acessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.
                        handleAuthorizationFailure("Authorization failre while retrieving AsyncAPI of API : " + apiId,
                                e, log);
            } else {
                String errorMessage = "Error while retrieving AsyncAPI for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdAsyncapiPut(String apiId, String ifMatch, String apiDefinition, String url, InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException {
        try {
            String updatedAsyncAPIDefinition;

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            //Handle URL and file based definition imports
            if (url != null || fileInputStream != null) {
                //Validate and retrieve the AsyncAPI definition
                Map validationResponseMap = validateAsyncAPISpecification(url, fileInputStream,
                        fileDetail, true, false);
                APIDefinitionValidationResponse validationResponse =
                        (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
                if (!validationResponse.isValid()) {
                    RestApiUtil.handleBadRequest(validationResponse.getErrorItems(), log);
                }
                updatedAsyncAPIDefinition = PublisherCommonUtils.updateAsyncAPIDefinition(apiId, validationResponse);
            } else {
                updatedAsyncAPIDefinition = updateAsyncAPIDefinition(apiId, apiDefinition);
            }
            return Response.ok().entity(updatedAsyncAPIDefinition).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating AsyncAPI definition of API: " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the AsyncAPI definition of the API: " + apiId + " - "
                        + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * update AsyncAPI definition of the given API. The AsyncAPI will be validated before updating.
     *
     * @param apiId API Id
     * @param apiDefinition AsyncAPI definition
     * @return updated AsyncAPI definition
     * @throws APIManagementException when error occurred updating AsyncAPI
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    private String updateAsyncAPIDefinition(String apiId, String apiDefinition)
            throws APIManagementException, FaultGatewaysException {
        APIDefinitionValidationResponse response = AsyncApiParserUtil
                .validateAsyncAPISpecification(apiDefinition, true);
        if (!response.isValid()) {
            RestApiUtil.handleBadRequest(response.getErrorItems(), log);
        }
        return PublisherCommonUtils.updateAsyncAPIDefinition(apiId, response);
    }

    @Override
    public Response importServiceFromCatalog(String serviceKey, APIDTO apiDto, MessageContext messageContext) {
        if (StringUtils.isEmpty(serviceKey)) {
            RestApiUtil.handleBadRequest("Required parameter serviceKey is missing", log);
        }
        try {
            ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
            String username = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(username);
            ServiceEntry service = serviceCatalog.getServiceByKey(serviceKey, tenantId);
            if (service == null) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceKey, log);
            }
            APIDTO createdApiDTO = null;
            if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                    ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
                createdApiDTO = importOpenAPIDefinition(service.getEndpointDef(), null, null, apiDto, null, service);
            } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                createdApiDTO = importAsyncAPISpecification(service.getEndpointDef(), null, apiDto, null, service);
            }
            if (createdApiDTO != null) {
                URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
                return Response.created(createdApiUri).entity(createdApiDTO).build();
            } else {
                RestApiUtil.handleBadRequest("Unsupported definition type provided. Cannot create API " +
                        "using the service type " + service.getDefinitionType().name(), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceKey, e, log);
            } else {
                String errorMessage = "Error while creating API using Service with Id : " + serviceKey
                        + " from Service Catalog";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDto.getName() + "-"
                    + apiDto.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response reimportServiceFromCatalog(String apiId, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantId(username);
        try {

            //validate if api exists
            APIInfo apiInfo = validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            API api = apiProvider.getLightweightAPIByUUID(apiId, tenantDomain);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            String serviceKey = apiProvider.retrieveServiceKeyByApiId(originalAPI.getId().getId(), tenantId);
            ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
            ServiceEntry service = serviceCatalog.getServiceByKey(serviceKey, tenantId);
            JSONObject serviceInfo = new JSONObject();
            serviceInfo.put("name", service.getName());
            serviceInfo.put("version", service.getVersion());
            serviceInfo.put("key", service.getKey());
            serviceInfo.put("md5", service.getMd5());
            api.setServiceInfo(serviceInfo);
            Map validationResponseMap = new HashMap();
            if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                    ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
                validationResponseMap = validateOpenAPIDefinition(null, service.getEndpointDef(), null, null,
                        true, true);
            } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                validationResponseMap = validateAsyncAPISpecification(null, service.getEndpointDef(),
                        null, true, true);
            } else {
                RestApiUtil.handleBadRequest("Unsupported definition type provided. Cannot re-import service to " +
                        "API using the service type " + service.getDefinitionType(), log);
            }
            APIDefinitionValidationResponse validationResponse =
                    (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
            if (!validationResponse.isValid()) {
                RestApiUtil.handleBadRequest(validationResponse.getErrorItems(), log);
            }
            String protocol = validationResponse.getProtocol();
            if (!APIConstants.API_TYPE_WEBSUB.equalsIgnoreCase(protocol)) {
                api.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service.getServiceUrl(),
                        protocol));
            }
            API updatedApi = apiProvider.updateAPI(api, originalAPI);
            PublisherCommonUtils.updateAPIDefinition(apiId, validationResponse, service);
            return Response.ok().entity(APIMappingUtil.fromAPItoDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            if (ExceptionCodes.MISSING_PROTOCOL_IN_ASYNC_API_DEFINITION.getErrorCode() == e.getErrorHandler()
                    .getErrorCode()) {
                RestApiUtil.handleBadRequest("Missing protocol in the Service Definition", log);
            } else if (ExceptionCodes.UNSUPPORTED_PROTOCOL_SPECIFIED_IN_ASYNC_API_DEFINITION.getErrorCode() ==
                    e.getErrorHandler().getErrorCode()) {
                RestApiUtil.handleBadRequest("Unsupported protocol specified in the Service Definition. Protocol " +
                        "should be either sse or websub or ws", log);
            }
            RestApiUtil.handleInternalServerError("Error while retrieving the service key of the service " +
                    "associated with API with id " + apiId, log);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    private APIDTO importOpenAPIDefinition(InputStream definition, String definitionUrl, String inlineDefinition,
                                           APIDTO apiDTOFromProperties, Attachment fileDetail, ServiceEntry service) {
        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        boolean isServiceAPI = false;
        try {
            if (service != null) {
                isServiceAPI = true;
            }
            validationResponseMap = validateOpenAPIDefinition(definitionUrl, definition, fileDetail, inlineDefinition,
                    true, isServiceAPI);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            ErrorDTO errorDTO = APIMappingUtil.getErrorDTOFromErrorListItems(validationResponseDTO.getErrors());
            throw RestApiUtil.buildBadRequestException(errorDTO);
        }

        // Only HTTP type APIs should be allowed
        if (!APIDTO.TypeEnum.HTTP.equals(apiDTOFromProperties.getType())) {
            throw RestApiUtil.buildBadRequestException("The API's type should only be HTTP when " +
                    "importing an OpenAPI definition");
        }
        // Import the API and Definition
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            if (isServiceAPI) {
                apiDTOFromProperties.setType(PublisherCommonUtils.getAPIType(service.getDefinitionType(), null));
            }
            API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDTOFromProperties, apiProvider,
                    RestApiCommonUtil.getLoggedInUsername());
            if (isServiceAPI) {
                apiToAdd.setServiceInfo("key", service.getKey());
                apiToAdd.setServiceInfo("md5", service.getMd5());
                apiToAdd.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service
                        .getServiceUrl(), null));
            }
            boolean syncOperations = apiDTOFromProperties.getOperations().size() > 0;
            // Rearrange paths according to the API payload and save the OpenAPI definition

            APIDefinition apiDefinition = validationResponse.getParser();
            SwaggerData swaggerData;
            String definitionToAdd = validationResponse.getJsonContent();
            if (syncOperations) {
                PublisherCommonUtils.validateScopes(apiToAdd);
                swaggerData = new SwaggerData(apiToAdd);
                definitionToAdd = apiDefinition.populateCustomManagementInfo(definitionToAdd, swaggerData);
            }
            definitionToAdd = OASParserUtil.preProcess(definitionToAdd);
            Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(definitionToAdd);
            Set<Scope> scopes = apiDefinition.getScopes(definitionToAdd);
            apiToAdd.setUriTemplates(uriTemplates);
            apiToAdd.setScopes(scopes);
            //Set extensions from API definition to API object
            apiToAdd = OASParserUtil.setExtensionsToAPI(definitionToAdd, apiToAdd);
            if (!syncOperations) {
                PublisherCommonUtils.validateScopes(apiToAdd);
                swaggerData = new SwaggerData(apiToAdd);
                definitionToAdd = apiDefinition
                        .populateCustomManagementInfo(validationResponse.getJsonContent(), swaggerData);
            }

            // adding the API and definition
            apiToAdd.setSwaggerDefinition(definitionToAdd);
            API addedAPI = apiProvider.addAPI(apiToAdd);
            //apiProvider.saveSwaggerDefinition(apiToAdd, definitionToAdd);

            // retrieving the added API for returning as the response
            // this would provide the updated templates
            addedAPI = apiProvider.getAPIbyUUID(addedAPI.getUuid(), RestApiCommonUtil.getLoggedInUserTenantDomain());
            return APIMappingUtil.fromAPItoDTO(addedAPI);
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    private APIDTO importAsyncAPISpecification(InputStream definition, String definitionUrl, APIDTO apiDTOFromProperties,
                                           Attachment fileDetail, ServiceEntry service) {
        //validate and retrieve the AsyncAPI specification
        Map validationResponseMap = null;
        boolean isServiceAPI = false;
        try {
            if (service != null) {
                isServiceAPI = true;
            }
            validationResponseMap = validateAsyncAPISpecification(definitionUrl, definition, fileDetail, true,
                    isServiceAPI);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        AsyncAPISpecificationValidationResponseDTO validationResponseDTO =
                (AsyncAPISpecificationValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            ErrorDTO errorDTO = APIMappingUtil.getErrorDTOFromErrorListItems(validationResponseDTO.getErrors());
            throw RestApiUtil.buildBadRequestException(errorDTO);
        }
        //Import the API and Definition
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String definitionToAdd = validationResponse.getJsonContent();
            String protocol = validationResponse.getProtocol();
            if (isServiceAPI) {
                apiDTOFromProperties.setType(PublisherCommonUtils.getAPIType(service.getDefinitionType(), protocol));
            }
            API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDTOFromProperties, apiProvider,
                    RestApiCommonUtil.getLoggedInUsername());
            if (isServiceAPI) {
                apiToAdd.setServiceInfo("key", service.getKey());
                apiToAdd.setServiceInfo("md5", service.getMd5());
                if (!APIConstants.API_TYPE_WEBSUB.equals(protocol.toUpperCase())) {
                    apiToAdd.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service
                            .getServiceUrl(), protocol));
                }
            }

            //load topics from AsyncAPI
            apiToAdd.setUriTemplates(new AsyncApiParser().getURITemplates(
                    definitionToAdd, APIConstants.API_TYPE_WS.equals(apiToAdd.getType())));

            apiProvider.addAPI(apiToAdd);
            apiProvider.saveAsyncApiDefinition(apiToAdd, definitionToAdd);
            return APIMappingUtil.fromAPItoDTO(apiProvider.getAPI(apiToAdd.getId()));
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response updateAPIDeployment(String apiId, String deploymentId, APIRevisionDeploymentDTO
            apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        APIInfo apiInfo = validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String revisionId = apIRevisionDeploymentDTO.getRevisionUuid();
        String decodedDeploymentName;
        if (deploymentId != null) {
            try {
                decodedDeploymentName = new String(Base64.getUrlDecoder().decode(deploymentId),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new APIMgtResourceNotFoundException("deployment with " + deploymentId +
                        " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                        deploymentId));
            }
        } else {
            throw new APIMgtResourceNotFoundException("deployment id not found",
                    ExceptionCodes.from(ExceptionCodes.DEPLOYMENT_ID_NOT_FOUND));
        }
        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revisionId);
        apiRevisionDeployment.setDeployment(decodedDeploymentName);
        apiRevisionDeployment.setVhost(apIRevisionDeploymentDTO.getVhost());
        apiRevisionDeployment.setDisplayOnDevportal(apIRevisionDeploymentDTO.isDisplayOnDevportal());
        apiProvider.updateAPIDisplayOnDevportal(apiId, revisionId, apiRevisionDeployment);
        APIRevisionDeployment apiRevisionDeploymentsResponse = apiProvider.
                getAPIRevisionDeployment(decodedDeploymentName, revisionId);
        APIRevisionDeploymentDTO apiRevisionDeploymentDTO = APIMappingUtil.
                fromAPIRevisionDeploymenttoDTO(apiRevisionDeploymentsResponse);
        Response.Status status = Response.Status.OK;

        return Response.status(status).entity(apiRevisionDeploymentDTO).build();
    }
}
