/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONArray;
import org.json.JSONTokener;
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
import org.wso2.carbon.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.dto.EnvironmentPropertiesDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIInfo;
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
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
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
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.restapi.Constants;
import org.wso2.carbon.apimgt.impl.restapi.PublisherUtils;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExternalStoreMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.GraphqlQueryAnalysisMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.OperationPolicyMappingUtil;
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
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorListItemDTO;
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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.wso2.carbon.apimgt.api.ExceptionCodes.API_VERSION_ALREADY_EXISTS;

/**
 * Util class for ApisApiService related operations
 */
public class ApisApiCommonImpl {

    public static final String MESSAGE = "message";
    public static final String ERROR_WHILE_UPDATING_API = "Error while updating API : ";

    private ApisApiCommonImpl() {

    }

    private static final Log log = LogFactory.getLog(ApisApiCommonImpl.class);
    private static final String HTTP_STATUS_LOG = "HTTP status ";
    private static final String AUDIT_ERROR = "Error while parsing the audit response";

    public static Object getAllAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String query,
                                    String organization) throws APIManagementException {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        sortBy = sortBy != null ? sortBy : RestApiConstants.DEFAULT_SORT_CRITERION;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;

        //revert content search back to normal search by name to avoid doc result complexity and to comply with
        // REST api practices
        if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
            query = query
                    .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                            APIConstants.NAME_TYPE_PREFIX + ":");
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        Map<String, Object> result;

        result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, sortBy, sortOrder);

        Set<API> apis = (Set<API>) result.get("apis");
        allMatchedApis.addAll(apis);

        apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);

        //Add pagination section in the response
        Object totalLength = result.get("length");
        int length = 0;
        if (totalLength != null) {
            length = (Integer) totalLength;
        }

        APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
        return apiListDTO;
    }

    public static APIDTO createAPI(APIDTO body, String oasVersion, String organization) throws APIManagementException {

        API createdApi = PublisherCommonUtils
                .addAPIWithGeneratedSwaggerDefinition(body, oasVersion, RestApiCommonUtil.getLoggedInUsername(),
                        organization);
        return APIMappingUtil.fromAPItoDTO(createdApi);
    }

    public static APIDTO getAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        return getAPIByID(apiId, apiProvider, organization);
    }

    public static CommentDTO addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo,
                                             String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        Comment comment = createComment(postRequestBodyDTO.getContent(), postRequestBodyDTO.getCategory(),
                replyTo, username, apiId);
        String createdCommentId = apiProvider.addComment(apiId, comment, username);
        Comment createdComment = apiProvider.getComment(apiTypeWrapper, createdCommentId, 0, 0);
        return CommentMappingUtil.fromCommentToDTO(createdComment);
    }

    public static CommentListDTO getAllCommentsOfAPI(String apiId, Integer limit, Integer offset,
                                                     Boolean includeCommenterInfo, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        String parentCommentID = null;
        CommentList comments = apiProvider.getComments(apiTypeWrapper, parentCommentID, limit, offset);
        return CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);
    }

    public static CommentDTO getCommentOfAPI(String commentId, String apiId, Boolean includeCommenterInfo,
                                             Integer replyLimit, Integer replyOffset, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);

        CommentDTO commentDTO;
        if (Boolean.TRUE.equals(includeCommenterInfo)) {
            Map<String, Map<String, String>> userClaimsMap = CommentMappingUtil
                    .retrieveUserClaims(comment.getUser(), new HashMap<>());
            commentDTO = CommentMappingUtil.fromCommentToDTOWithUserInfo(comment, userClaimsMap);
        } else {
            commentDTO = CommentMappingUtil.fromCommentToDTO(comment);
        }
        return commentDTO;
    }

    public static CommentDTO editCommentOfAPI(String commentId, String apiId, PatchRequestBodyDTO patchRequestBodyDTO)
            throws APIManagementException {

        CommentDTO commentDTO = null;
        String username = RestApiCommonUtil.getLoggedInUsername();
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
        ApisApiCommonImpl.checkCommentOwner(comment, username);

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
        if (commentEdited && apiProvider.editComment(apiTypeWrapper, commentId, comment)) {
            Comment editedComment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            commentDTO = CommentMappingUtil.fromCommentToDTO(editedComment);
        }
        return commentDTO;
    }

    public static CommentListDTO getRepliesOfComment(String commentId, String apiId, Integer limit, Integer offset,
                                                     Boolean includeCommenterInfo, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        CommentList comments = apiProvider.getComments(apiTypeWrapper, commentId, limit, offset);
        return CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);
    }

    /**
     * @param content  Comment content
     * @param category Category
     * @param replyTo  Parent comment ID
     * @param username User commenting
     * @param apiId    API UUID
     * @return Comment
     */
    public static Comment createComment(String content, String category, String replyTo, String username,
                                        String apiId) {

        Comment comment = new Comment();
        comment.setText(content);
        comment.setCategory(category);
        comment.setParentCommentID(replyTo);
        comment.setEntryPoint("PUBLISHER");
        comment.setUser(username);
        comment.setApiId(apiId);
        return comment;
    }

    public static void checkCommentOwner(Comment comment, String username) throws APIManagementException {

        if (!comment.getUser().equals(username)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.COMMENT_NO_PERMISSION, username, comment.getId()));
        }
    }

    public static JSONObject deleteComment(String commentId, String apiId, String[] tokenScopes)
            throws APIManagementException {

        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String username = RestApiCommonUtil.getLoggedInUsername();

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);

        if (Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE) || comment.getUser().equals(username)) {
            if (apiProvider.deleteComment(apiTypeWrapper, commentId)) {
                JSONObject obj = new JSONObject();
                obj.put("id", commentId);
                obj.put(MESSAGE, "The comment has been deleted");
                return obj;
            } else {
                throw new APIManagementException(ExceptionCodes.METHOD_NOT_ALLOWED);
            }
        } else {
            throw new APIManagementException(ExceptionCodes.AUTH_GENERAL_ERROR);
        }
    }

    public static GraphQLQueryComplexityInfoDTO getGraphQLPolicyComplexityOfAPI(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.GRAPHQL_API, api.getType());

        String currentApiUuid;
        // Resolve whether an API or a corresponding revision
        APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            currentApiUuid = apiRevision.getApiUUID();
        } else {
            currentApiUuid = apiId;
        }
        GraphqlComplexityInfo graphqlComplexityInfo = apiProvider.getComplexityDetails(currentApiUuid);
        return GraphqlQueryAnalysisMappingUtil.fromGraphqlComplexityInfotoDTO(graphqlComplexityInfo);
    }

    public static void updateGraphQLPolicyComplexityOfAPI(String apiId, GraphQLQueryComplexityInfoDTO body,
                                                          String organization, String[] tokenScopes)
            throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        //validate if api exists
        APIInfo apiInfo = RestApiCommonUtil.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString(), tokenScopes);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
        String schema = apiProvider.getGraphqlSchemaDefinition(apiId, organization);

        GraphqlComplexityInfo graphqlComplexityInfo =
                GraphqlQueryAnalysisMappingUtil.fromDTOtoValidatedGraphqlComplexityInfo(body, schema);
        RestApiCommonUtil.checkAPIType(APIConstants.GRAPHQL_API, existingAPI.getType());

        apiProvider.addOrUpdateComplexityDetails(apiId, graphqlComplexityInfo);
    }

    public static void updateTopics(String apiId, TopicListDTO topicListDTO, String organization, String[] tokenScopes)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);
        API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
        API updatedAPI = apiProvider.getAPIbyUUID(apiId, organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(updatedAPI.getStatus(), tokenScopes);

        Set<URITemplate> uriTemplates = updatedAPI.getUriTemplates();
        uriTemplates.clear();

        for (TopicDTO topicDTO : topicListDTO.getList()) {
            uriTemplates.add(createUriTemplate(topicDTO.getName(), topicDTO.getMode()));
        }
        updatedAPI.setUriTemplates(uriTemplates);
        updatedAPI.setOrganization(organization);
        try {
            apiProvider.updateAPI(updatedAPI, existingAPI);
        } catch (FaultGatewaysException e) {
            String errorMessage = ERROR_WHILE_UPDATING_API + apiId;
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * @param name Name of URI Template
     * @param verb HTTP verb
     * @return URITemplate
     */
    public static URITemplate createUriTemplate(String name, String verb) {

        URITemplate uriTemplate = new URITemplate();
        uriTemplate.setUriTemplate(name);
        uriTemplate.setHTTPVerb(verb.toUpperCase());
        uriTemplate.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
        uriTemplate.setThrottlingTier(APIConstants.UNLIMITED_TIER);

        return uriTemplate;
    }

    public static GraphQLSchemaDTO getAPIGraphQLSchema(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        APIIdentifier apiIdentifier;
        if (apiProvider.checkAPIUUIDIsARevisionUUID(apiId) != null) {
            apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId, organization).getId();
        } else {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        }
        String schemaContent = apiProvider.getGraphqlSchemaDefinition(apiId, organization);
        GraphQLSchemaDTO dto = new GraphQLSchemaDTO();
        dto.setSchemaDefinition(schemaContent);
        dto.setName(apiIdentifier.getProviderName() + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                apiIdentifier.getApiName() + apiIdentifier.getVersion() + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION);
        return dto;
    }

    public static APIDTO updateAPIGraphQLSchema(String apiId, String schemaDefinition, String organization,
                                                String[] tokenScopes)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
        originalAPI.setOrganization(organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(originalAPI.getStatus(), tokenScopes);
        try {
            PublisherCommonUtils.addGraphQLSchema(originalAPI, schemaDefinition, apiProvider);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while uploading schema of the API: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return APIMappingUtil.fromAPItoDTO(originalAPI);
    }

    public static APIDTO updateAPI(String apiId, APIDTO body, String[] tokenScopes, String organization)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        boolean isWSAPI = APIDTO.TypeEnum.WS.equals(body.getType());

        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);

        // validate web socket api endpoint configurations
        if (isWSAPI && !PublisherCommonUtils.isValidWSAPI(body)) {
            throw new APIManagementException("Endpoint URLs should be valid web socket URLs",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        // validate sandbox and production endpoints
        if (!PublisherCommonUtils.validateEndpoints(body)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
        originalAPI.setOrganization(organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(originalAPI.getStatus(), tokenScopes);
        API updatedApi;
        try {
            updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes);
        } catch (FaultGatewaysException e) {
            String errorMessage = ERROR_WHILE_UPDATING_API + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return APIMappingUtil.fromAPItoDTO(updatedApi);
    }

    private static void validateAPIOperationsPerLC(String status, String[] tokenScopes) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;

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

    public static GraphQLSchemaTypeListDTO getGraphQLPolicyComplexityTypesOfAPI(String apiId, String organization)
            throws APIManagementException {

        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.GRAPHQL_API, api.getType());
        String schemaContent = apiProvider.getGraphqlSchemaDefinition(apiId, organization);
        List<GraphqlSchemaType> typeList = graphql.extractGraphQLTypeList(schemaContent);
        return GraphqlQueryAnalysisMappingUtil.fromGraphqlSchemaTypeListtoDTO(typeList);
    }

    /**
     * @param apiId        API ID
     * @param organization Organization
     * @return JSONObject with arns
     * @throws SdkClientException     if AWSLambda SDK throws an error
     * @throws APIManagementException
     */
    public static JSONObject getAmazonResourceNamesOfAPI(String apiId, String organization)
            throws SdkClientException, APIManagementException {

        JSONObject arns = new JSONObject();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        String endpointConfigString = api.getEndpointConfig();
        if (StringUtils.isNotEmpty(endpointConfigString)) {
            JSONParser jsonParser = new JSONParser();
            JSONObject endpointConfig;
            try {
                endpointConfig = (JSONObject) jsonParser.parse(endpointConfigString);
            } catch (ParseException e) {
                throw new APIManagementException("Error while parsing endpoint config",
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
            if (endpointConfig != null
                    && endpointConfig.containsKey(APIConstants.AMZN_ACCESS_KEY)
                    && endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)
                    && endpointConfig.containsKey(APIConstants.AMZN_REGION)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_ARN)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_SESSION_NAME)
                    && endpointConfig.containsKey(APIConstants.AMZN_ROLE_REGION)) {
                String accessKey = (String) endpointConfig.get(APIConstants.AMZN_ACCESS_KEY);
                String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                String region = (String) endpointConfig.get(APIConstants.AMZN_REGION);
                String roleArn = (String) endpointConfig.get(APIConstants.AMZN_ROLE_ARN);
                String roleSessionName = (String) endpointConfig.get(APIConstants.AMZN_ROLE_SESSION_NAME);
                String roleRegion = (String) endpointConfig.get(APIConstants.AMZN_ROLE_REGION);
                try {
                    AWSLambda awsLambdaClient = getAWSLambdaClient(accessKey, secretKey, region,
                            roleArn, roleSessionName, roleRegion);
                    if (awsLambdaClient == null) {
                        return (JSONObject) Collections.emptyMap();
                    }
                    ListFunctionsResult listFunctionsResult = awsLambdaClient.listFunctions();
                    List<FunctionConfiguration> functionConfigurations = listFunctionsResult.getFunctions();
                    arns.put("count", functionConfigurations.size());
                    JSONArray list = new JSONArray();
                    for (FunctionConfiguration functionConfiguration : functionConfigurations) {
                        list.put(functionConfiguration.getFunctionArn());
                    }
                    arns.put("list", list);
                    return arns;
                } catch (CryptoException e) {
                    throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.ENDPOINT_CRYPTO_ERROR,
                            "Error while decrypting AWS Lambda secret key"));
                }
            }
        }
        return (JSONObject) Collections.emptyMap();
    }

    /**
     * @param accessKey       AWS access key
     * @param secretKey       AWS secret key
     * @param region          AWS region
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     * @throws CryptoException when decoding secrets fail
     */
    private static AWSLambda getAWSLambdaClient(String accessKey, String secretKey, String region,
                                                String roleArn, String roleSessionName, String roleRegion)
            throws CryptoException {

        AWSLambda awsLambdaClient;
        if (StringUtils.isEmpty(accessKey) && StringUtils.isEmpty(secretKey)) {
            awsLambdaClient = getARNsWithIAMRole(roleArn, roleSessionName, roleRegion);
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(accessKey) && StringUtils.isNotEmpty(secretKey) &&
                StringUtils.isNotEmpty(region)) {
            awsLambdaClient = getARNsWithStoredCredentials(accessKey, secretKey, region,
                    roleArn, roleSessionName, roleRegion);
            return awsLambdaClient;
        } else {
            log.error("Missing AWS Credentials");
            return null;
        }
    }

    /**
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     */
    private static AWSLambda getARNsWithIAMRole(String roleArn, String roleSessionName, String roleRegion) {

        AWSLambda awsLambdaClient;
        if (log.isDebugEnabled()) {
            log.debug("Using temporary credentials supplied by the IAM role attached to AWS " +
                    "instance");
        }
        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                && StringUtils.isEmpty(roleRegion)) {
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                    .build();
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                && StringUtils.isNotEmpty(roleRegion)) {
            String stsRegion = String.valueOf(Regions.getCurrentRegion());
            AWSSecurityTokenService awsSTSClient;
            if (StringUtils.isEmpty(stsRegion)) {
                awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                        .build();
            } else {
                awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                        .withCredentials(DefaultAWSCredentialsProviderChain.getInstance())
                        .withEndpointConfiguration(new EndpointConfiguration("https://sts."
                                + stsRegion + ".amazonaws.com", stsRegion))
                        .build();
            }
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
            Credentials sessionCredentials = assumeRoleResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(roleRegion)
                    .build();
            return awsLambdaClient;
        } else {
            log.error("Missing AWS STS configurations");
            return null;
        }
    }

    /**
     * @param accessKey       AWS access key
     * @param secretKey       AWS secret key
     * @param region          AWS region
     * @param roleArn         AWS role ARN
     * @param roleSessionName AWS role session name
     * @param roleRegion      AWS role region
     * @return AWS Lambda Client
     * @throws CryptoException when decoding secrets fail
     */
    private static AWSLambda getARNsWithStoredCredentials(String accessKey, String secretKey, String region,
                                                          String roleArn, String roleSessionName, String roleRegion)
            throws CryptoException {

        AWSLambda awsLambdaClient;
        if (log.isDebugEnabled()) {
            log.debug("Using user given stored credentials");
        }
        if (secretKey.length() == APIConstants.AWS_ENCRYPTED_SECRET_KEY_LENGTH) {
            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
            secretKey = new String(cryptoUtil.base64DecodeAndDecrypt(secretKey),
                    StandardCharsets.UTF_8);
        }
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        if (StringUtils.isEmpty(roleArn) && StringUtils.isEmpty(roleSessionName)
                && StringUtils.isEmpty(roleRegion)) {
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withRegion(region)
                    .build();
            return awsLambdaClient;
        } else if (StringUtils.isNotEmpty(roleArn) && StringUtils.isNotEmpty(roleSessionName)
                && StringUtils.isNotEmpty(roleRegion)) {
            AWSSecurityTokenService awsSTSClient = AWSSecurityTokenServiceClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                    .withEndpointConfiguration(new EndpointConfiguration("https://sts."
                            + region + ".amazonaws.com", region))
                    .build();
            AssumeRoleRequest roleRequest = new AssumeRoleRequest()
                    .withRoleArn(roleArn)
                    .withRoleSessionName(roleSessionName);
            AssumeRoleResult assumeRoleResult = awsSTSClient.assumeRole(roleRequest);
            Credentials sessionCredentials = assumeRoleResult.getCredentials();
            BasicSessionCredentials basicSessionCredentials = new BasicSessionCredentials(
                    sessionCredentials.getAccessKeyId(),
                    sessionCredentials.getSecretAccessKey(),
                    sessionCredentials.getSessionToken());
            awsLambdaClient = AWSLambdaClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(basicSessionCredentials))
                    .withRegion(roleRegion)
                    .build();
            return awsLambdaClient;
        } else {
            log.error("Missing AWS STS configurations");
            return null;
        }
    }

    public static AuditReportDTO getAuditReportOfAPI(String apiId, String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        // Get configuration file, retrieve API token and collection id
        JSONObject securityAuditPropertyObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
        JSONObject responseJson = getAuditReport(api, securityAuditPropertyObject, apiDefinition, organization);
        AuditReportDTO auditReportDTO = new AuditReportDTO();
        auditReportDTO.setReport((String) responseJson.get("decodedReport"));
        auditReportDTO.setGrade((String) responseJson.get("grade"));
        auditReportDTO.setNumErrors((Integer) responseJson.get("numErrors"));
        auditReportDTO.setExternalApiId((String) responseJson.get("auditUuid"));
        return auditReportDTO;
    }

    public static Object getAPIClientCertificateContentByAlias(String apiId, String alias, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                apiTypeWrapper, organization);
        return CertificateRestApiUtils.getDecodedCertificate(clientCertificateDTO.getCertificate());
    }

    public static void deleteAPIClientCertificateByAlias(String alias, String apiId, String organization,
                                                         String[] tokenScopes)
            throws APIManagementException {
        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        apiTypeWrapper.setOrganization(organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiTypeWrapper.getStatus(), tokenScopes);

        CertificateRestApiUtils.preValidateClientCertificate(alias, apiTypeWrapper, organization);
        int responseCode = apiProvider
                .deleteClientCertificate(RestApiCommonUtil.getLoggedInUsername(), apiTypeWrapper, alias);
        if (responseCode == ResponseCode.SUCCESS.getResponseCode()) {

            if (log.isDebugEnabled()) {
                log.debug(String.format("The client certificate which belongs to tenant : %s represented by the "
                        + "alias : %s is deleted successfully", organization, alias));
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(String.format("Failed to delete the client certificate which belongs to tenant : %s "
                        + "represented by the alias : %s.", organization, alias));
            }
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.DELETE_CERT, alias));
        }
    }

    public static CertificateInfoDTO getAPIClientCertificateByAlias(String alias, String apiId, String organization)
            throws APIManagementException {

        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                apiTypeWrapper, organization);
        CertificateInformationDTO certificateInformationDTO = certificateMgtUtils
                .getCertificateInfo(clientCertificateDTO.getCertificate());
        if (certificateInformationDTO != null) {
            return CertificateMappingUtil.fromCertificateInformationToDTO(certificateInformationDTO);
        } else {
            throw new APIManagementException("Certificate is empty for alias " + alias,
                    ExceptionCodes.from(ExceptionCodes.CERT_NOT_FOUND, alias));
        }
    }

    public static ClientCertMetadataDTO updateAPIClientCertificateByAlias(String alias, String apiId, String tier,
                                                                          String organization, String base64EncodedCert)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        apiTypeWrapper.setOrganization(organization);

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                apiTypeWrapper, organization);
        apiProvider.updateClientCertificate(base64EncodedCert, alias, apiTypeWrapper, tier, tenantId, organization);
        ClientCertMetadataDTO clientCertMetadataDTO = new ClientCertMetadataDTO();
        clientCertMetadataDTO.setAlias(alias);
        clientCertMetadataDTO.setApiId(apiTypeWrapper.getUuid());
        clientCertMetadataDTO.setTier(clientCertificateDTO.getTierName());
        return clientCertMetadataDTO;
    }

    public static ClientCertificatesDTO getAPIClientCertificates(String apiId, Integer limit, Integer offset,
                                                                 String alias, String organization)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<ClientCertificateDTO> certificates = new ArrayList<>();
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "apiId", apiId);

        int tenantId = APIUtil.getInternalOrganizationId(organization);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        int totalCount = apiProvider.getClientCertificateCount(tenantId);
        if (totalCount > 0) {
            APIIdentifier apiIdentifier = null;
            if (StringUtils.isNotEmpty(apiId)) {
                API api = apiProvider.getAPIbyUUID(apiId, organization);
                apiIdentifier = api.getId();
            }
            certificates = apiProvider.searchClientCertificates(tenantId, alias, apiIdentifier, organization);
        }

        ClientCertificatesDTO certificatesDTO = CertificateRestApiUtils
                .getPaginatedClientCertificates(certificates, limit, offset, query);
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(totalCount);
        certificatesDTO.setPagination(paginationDTO);
        return certificatesDTO;
    }

    public static ClientCertMetadataDTO addAPIClientCertificate(String apiId, InputStream certificateInputStream,
                                                                String alias, String tier, String organization,
                                                                String fileName, String[] tokenScopes)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(apiId)) {
            throw new APIManagementException("The alias and/ or apiId should not be empty",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (StringUtils.isBlank(fileName)) {
            throw new APIManagementException("Certificate addition failed. "
                    + "Proper Certificate file should be provided", ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);

        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        apiTypeWrapper.setOrganization(organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiTypeWrapper.getStatus(), tokenScopes);

        String userName = RestApiCommonUtil.getLoggedInUsername();
        String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
        int responseCode = apiProvider
                .addClientCertificate(userName, apiTypeWrapper, base64EncodedCert, alias, tier, organization);
        if (log.isDebugEnabled()) {
            log.debug(String.format("Add certificate operation response code : %d", responseCode));
        }
        ClientCertMetadataDTO certificateDTO = new ClientCertMetadataDTO();
        certificateDTO.setAlias(alias);
        certificateDTO.setApiId(apiId);
        certificateDTO.setTier(tier);
        return certificateDTO;
    }

    /**
     * @param api                         API
     * @param securityAuditPropertyObject audit security properties
     * @param apiDefinition               API definition
     * @param organization                user organization
     * @return JSONObject containing audit response
     * @throws APIManagementException when there's an unexpected response
     * @throws IOException            when http client fails
     */
    public static JSONObject getAuditReport(API api, JSONObject securityAuditPropertyObject,
                                            String apiDefinition, String organization)
            throws APIManagementException {

        boolean isDebugEnabled = log.isDebugEnabled();
        APIIdentifier apiIdentifier = api.getId();
        String apiToken = (String) securityAuditPropertyObject.get("apiToken");
        String collectionId = (String) securityAuditPropertyObject.get("collectionId");
        String baseUrl = (String) securityAuditPropertyObject.get("baseUrl");

        if (baseUrl == null) {
            baseUrl = APIConstants.BASE_AUDIT_URL;
        }
        // Retrieve the uuid from the database
        String auditUuid = ApiMgtDAO.getInstance().getAuditApiId(api.getUuid());
        if (auditUuid != null) {
            updateAuditApi(apiDefinition, apiToken, auditUuid, baseUrl, isDebugEnabled);
        } else {
            auditUuid = createAuditApi(collectionId, apiToken, apiIdentifier, apiDefinition, baseUrl,
                    isDebugEnabled, organization);
        }
        String getUrl = baseUrl + "/" + auditUuid + APIConstants.ASSESSMENT_REPORT;

        try {
            URL getReportUrl = new URL(getUrl);
            CloseableHttpClient getHttpClient = (CloseableHttpClient) APIUtil
                    .getHttpClient(getReportUrl.getPort(), getReportUrl.getProtocol());
            HttpGet httpGet = new HttpGet(getUrl);
            // Set the header properties of the request
            httpGet.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
            httpGet.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
            httpGet.setHeader(APIConstants.HEADER_USER_AGENT, APIConstants.USER_AGENT_APIM);
            // Code block for the processing of the response
            try (CloseableHttpResponse response = getHttpClient.execute(httpGet)) {
                if (isDebugEnabled) {
                    log.debug(HTTP_STATUS_LOG + response.getStatusLine().getStatusCode());
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
                    JSONObject output = new JSONObject();
                    output.put("decodedReport", decodedReport);
                    output.put("grade", grade);
                    output.put("numErrors", numErrors);
                    output.put("auditUuid", auditUuid);
                    return output;
                }
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        } catch (ParseException e) {
            throw new APIManagementException(AUDIT_ERROR, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return (JSONObject) Collections.emptyMap();
    }

    /**
     * Update API Definition before retrieving Security Audit Report
     *
     * @param apiDefinition  API Definition of API
     * @param apiToken       API Token to access Security Audit
     * @param auditUuid      Respective UUID of API in Security Audit
     * @param baseUrl        Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @throws APIManagementException In the event of unexpected response
     */
    private static void updateAuditApi(String apiDefinition, String apiToken, String auditUuid, String baseUrl,
                                       boolean isDebugEnabled)
            throws APIManagementException {
        // Set the property to be attached in the body of the request
        // Attach API Definition to property called specfile to be sent in the request
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("specfile", Base64Utils.encode(apiDefinition.getBytes(StandardCharsets.UTF_8)));
        // Logic for HTTP Request
        String putUrl = baseUrl + "/" + auditUuid;
        try {
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
                        log.debug(HTTP_STATUS_LOG + response.getStatusLine().getStatusCode());
                    }
                    if ((response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)) {
                        throw new APIManagementException(
                                "Error while sending data to the API Security Audit Feature. Found http status " +
                                        response.getStatusLine(),
                                ExceptionCodes.from(ExceptionCodes.AUDIT_SEND_FAILED,
                                        String.valueOf(response.getStatusLine())));
                    }
                } finally {
                    httpPut.releaseConnection();
                }
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        }

    }

    /**
     * Send API Definition to Security Audit for the first time
     *
     * @param collectionId   Collection ID in which the Definition should be sent to
     * @param apiToken       API Token to access Security Audit
     * @param apiIdentifier  API Identifier object
     * @param apiDefinition  API Definition of API
     * @param baseUrl        Base URL to communicate with Security Audit
     * @param isDebugEnabled Boolean whether debug is enabled
     * @param organization   Organization
     * @return String UUID of API in Security Audit
     * @throws APIManagementException In the event of unexpected response
     */
    private static String createAuditApi(String collectionId, String apiToken, APIIdentifier apiIdentifier,
                                         String apiDefinition, String baseUrl, boolean isDebugEnabled,
                                         String organization)
            throws APIManagementException {

        HttpURLConnection httpConn;
        OutputStream outputStream;
        String auditUuid = null;
        try {
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
            writeAuditResponse(outputStream, apiIdentifier, apiDefinition, collectionId);
            // Checks server's status code first
            int status = httpConn.getResponseCode();
            if (status == HttpURLConnection.HTTP_OK) {
                if (isDebugEnabled) {
                    log.debug(HTTP_STATUS_LOG + status);
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
                ApiMgtDAO.getInstance().addAuditApiMapping(apiIdentifier, auditUuid, organization);
            } else {
                handleAuditCreateError(httpConn);
            }
        } catch (IOException e) {
            throw new APIManagementException(e.getMessage(), ExceptionCodes.INTERNAL_ERROR);
        } catch (ParseException e) {
            throw new APIManagementException(AUDIT_ERROR, ExceptionCodes.JSON_PARSE_ERROR);
        }
        return auditUuid;
    }

    /**
     * @param outputStream  HTTP output stream
     * @param apiIdentifier API Identifier object
     * @param apiDefinition API Definition of API
     * @param collectionId  Collection ID in which the Definition should be sent to
     */
    private static void writeAuditResponse(OutputStream outputStream, APIIdentifier apiIdentifier,
                                           String apiDefinition, String collectionId) {

        PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), true);
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
    }

    private static void handleAuditCreateError(HttpURLConnection httpConn)
            throws IOException, ParseException, APIManagementException {

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
            if (responseJson.containsKey(MESSAGE)) {
                errorMessage = (String) responseJson.get(MESSAGE);
            }
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + errorMessage,
                    ExceptionCodes.AUDIT_RETRIEVE_FAILED);
        } else {
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + httpConn.getResponseMessage(),
                    ExceptionCodes.AUDIT_RETRIEVE_FAILED);
        }
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @throws APIManagementException when prerequisites for API delete are not met
     */
    public static void deleteAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //check if the API has subscriptions
        List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiId, organization);
        if (apiUsages != null && !apiUsages.isEmpty()) {
            throw new APIManagementException("Cannot remove the API "
                    + apiId + " as active subscriptions exist", ExceptionCodes.API_DELETE_FAILED_SUBSCRIPTIONS);
        }
        List<APIResource> usedProductResources = apiProvider.getUsedProductResources(apiId);

        if (!usedProductResources.isEmpty()) {
            throw new APIManagementException("Cannot remove the API because following resource paths " +
                    usedProductResources.toString() + " are used by one or more API Products",
                    ExceptionCodes.API_DELETE_API_PRODUCT_USED_RESOURCES);
        }

        // Delete the API
        apiProvider.deleteAPI(apiId, organization);
    }

    public static DocumentDTO addAPIDocumentContent(String apiId, String documentId, InputStream inputStream,
                                                    String inlineContent, String organization, String fileName,
                                                    String mediaType) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (inputStream != null && inlineContent != null) {
            throw new APIManagementException("Only one of 'file' and 'inlineContent' should be specified",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }

        //retrieves the document and send 404 if not found
        Documentation documentation = apiProvider.getDocumentation(apiId, documentId, organization);

        //add content depending on the availability of either input stream or inline content
        if (inputStream != null) {
            if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                throw new APIManagementException("Source type of document " + documentId + " is not FILE",
                        ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA);
            }
            if (APIUtil.isSupportedFileType(fileName)) {
                PublisherCommonUtils.attachFileToDocument(apiId, documentation, inputStream, fileName, mediaType,
                        organization);
            } else {
                throw new APIManagementException("Unsupported extension type of document file: " + fileName,
                        ExceptionCodes.UNSUPPORTED_DOC_EXTENSION);
            }
        } else if (inlineContent != null) {
            if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE) &&
                    !documentation.getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                throw new APIManagementException("Source type of document " + documentId + " is not INLINE " +
                        "or MARKDOWN", ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA);
            }
            PublisherCommonUtils
                    .addDocumentationContent(documentation, apiProvider, apiId, documentId, organization,
                            inlineContent);
        } else {
            throw new APIManagementException("Either 'file' or 'inlineContent' should be specified",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        //retrieving the updated doc and the URI
        Documentation updatedDoc = apiProvider.getDocumentation(apiId, documentId, organization);
        return DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc);
    }

    public static DocumentationContent getAPIDocumentContentByDocumentId(String apiId, String documentId,
                                                                         String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        return apiProvider.getDocumentationContent(apiId, documentId, organization);
    }

    public static void deleteAPIDocument(String apiId, String documentId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.getDocumentation(apiId, documentId, organization);
        apiProvider.removeDocumentation(apiId, documentId, organization);
    }

    public static DocumentDTO getAPIDocumentByDocumentId(String apiId, String documentId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Documentation documentation = apiProvider.getDocumentation(apiId, documentId, organization);

        return DocumentationMappingUtil.fromDocumentationToDTO(documentation);
    }

    public static DocumentDTO updateAPIDocument(String apiId, String documentId, DocumentDTO body, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        String sourceUrl = body.getSourceUrl();
        Documentation oldDocument = apiProvider.getDocumentation(apiId, documentId, organization);

        if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
            //check otherTypeName for not null if doc type is OTHER
            throw new APIManagementException("otherTypeName cannot be empty if type is OTHER.",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                (StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
            throw new APIManagementException("Invalid document sourceUrl Format",
                    ExceptionCodes.from(ExceptionCodes.DOCUMENT_INVALID_SOURCE_TYPE, documentId));
        }

        //overriding some properties
        body.setName(oldDocument.getName());

        Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
        newDocumentation.setFilePath(oldDocument.getFilePath());
        newDocumentation.setId(documentId);
        newDocumentation = apiProvider.updateDocumentation(apiId, newDocumentation, organization);

        return DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation);
    }

    public static DocumentListDTO getAPIDocuments(String apiId, Integer limit, Integer offset, String organization)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist

        List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiId, organization);
        DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                offset, limit);
        DocumentationMappingUtil
                .setPaginationParams(documentListDTO, apiId, offset, limit, allDocumentation.size());
        return documentListDTO;
    }

    public static DocumentDTO addAPIDocument(String apiId, DocumentDTO body, String organization)
            throws APIManagementException {

        Documentation documentation = PublisherCommonUtils.addDocumentationToAPI(body, apiId, organization);
        return DocumentationMappingUtil.fromDocumentationToDTO(documentation);
    }

    public static APIExternalStoreListDTO getAllPublishedExternalStoresByAPI(String apiId) throws
            APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(apiId);
        return ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
    }

    public static WSDLInfoDTO getWSDLInfoOfAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        WSDLInfoDTO wsdlInfoDTO = APIMappingUtil.getWsdlInfoDTO(api);
        if (wsdlInfoDTO == null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.NO_WSDL_AVAILABLE_FOR_API,
                            api.getId().getApiName(), api.getId().getVersion()));
        }
        return wsdlInfoDTO;
    }

    public static LifecycleHistoryDTO getAPILifecycleHistory(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api;
        APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
        if (apiRevision != null && apiRevision.getApiUUID() != null) {
            api = apiProvider.getAPIbyUUID(apiRevision.getApiUUID(), organization);
        } else {
            api = apiProvider.getAPIbyUUID(apiId, organization);
        }
        return PublisherCommonUtils.getLifecycleHistoryDTO(api.getUuid(), apiProvider);
    }

    public static LifecycleStateDTO getAPILifecycleState(String apiId, String organization)
            throws APIManagementException {

        return getLifecycleState(apiId, organization);
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId        API Id
     * @param organization organization
     * @return API Lifecycle state information
     */
    private static LifecycleStateDTO getLifecycleState(String apiId, String organization)
            throws APIManagementException {

        APIIdentifier apiIdentifier;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (apiProvider.checkAPIUUIDIsARevisionUUID(apiId) != null) {
            apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId, organization).getId();
        } else {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        }
        return PublisherCommonUtils.getLifecycleStateInformation(apiIdentifier, organization);
    }

    public static void deleteAPILifecycleStatePendingTasks(String apiId) throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        apiProvider.deleteWorkflowTask(apiIdentifierFromTable);
    }

    public static FileInfoDTO updateAPIThumbnail(String apiId, InputStream fileInputStream, String organization,
                                                 String fileName, String fileDetailContentType)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String extension = FilenameUtils.getExtension(fileName);
        if (!RestApiConstants.ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension.toLowerCase())) {
            String errorMessage = "Unsupported Thumbnail File Extension. Supported extensions are .jpg, .png, "
                    + ".jpeg, .svg, and .gif";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        String fileContentType = URLConnection.guessContentTypeFromName(fileName);
        if (StringUtils.isBlank(fileContentType)) {
            fileContentType = fileDetailContentType;
        }
        PublisherCommonUtils.updateThumbnail(fileInputStream, fileContentType, apiProvider, apiId, organization);
        FileInfoDTO infoDTO = new FileInfoDTO();
        infoDTO.setMediaType(fileContentType);
        return infoDTO;
    }

    public static String getAPISwagger(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return RestApiCommonUtil.retrieveSwaggerDefinition(apiId, api, apiProvider);
    }

    public static String updateAPISwagger(String apiId, String apiDefinition, String url, InputStream fileInputStream,
                                          String organization, String fileName) throws APIManagementException {

        String updatedSwagger;
        //Handle URL and file based definition imports
        if (url != null || fileInputStream != null) {
            // Validate and retrieve the OpenAPI definition
            Map<String, Object> validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileName,
                    null, true, false);
            APIDefinitionValidationResponse validationResponse =
                    (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
            if (!validationResponse.isValid()) {
                throw new APIManagementException(ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
            }
            updatedSwagger = PublisherCommonUtils.updateSwagger(apiId, validationResponse, false, organization);
        } else {
            updatedSwagger = updateSwagger(apiId, apiDefinition, organization);
        }
        return updatedSwagger;
    }

    /**
     * update swagger definition of the given api. The swagger will be validated before updating.
     *
     * @param apiId         API Id
     * @param apiDefinition swagger definition
     * @param organization  Organization Identifier
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     */
    public static String updateSwagger(String apiId, String apiDefinition, String organization)
            throws APIManagementException {

        APIDefinitionValidationResponse response = OASParserUtil
                .validateAPIDefinition(apiDefinition, true);
        if (!response.isValid()) {
            String errorDescription = RestApiCommonUtil.getErrorDescriptionFromErrorHandlers(response.getErrorItems());
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION_WITH_CUSTOM_MESSAGE, errorDescription));
        }
        return PublisherCommonUtils.updateSwagger(apiId, response, false, organization);
    }

    public static ResourceFile getAPIThumbnail(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        //this will fail if user does not have access to the API or the API does not exist
        RestApiCommonUtil.validateAPIExistence(apiId);
        return apiProvider.getIcon(apiId, organization);
    }

    /**
     * Send HTTP HEAD request to test the endpoint url
     *
     * @param urlVal url for which the HEAD request is sent
     * @return APIEndpointValidationDTO Response DTO containing validity information of the HEAD request made
     * to test the endpoint url
     */
    public static ApiEndpointValidationResponseDTO validateEndpoint(String urlVal)
            throws APIManagementException {

        URL url;
        try {
            url = new URL(urlVal);
        } catch (MalformedURLException e) {
            throw new APIManagementException("URL is malformed",
                    e, ExceptionCodes.from(ExceptionCodes.URI_PARSE_ERROR, "Malformed url"));
        }
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
        }

        APIEndpointValidationDTO apiEndpointValidationDTO = new APIEndpointValidationDTO();
        org.apache.http.client.HttpClient client = APIUtil.getHttpClient(urlVal);
        HttpHead method = new HttpHead(urlVal);

        try {
            HttpResponse response = client.execute(method);
            apiEndpointValidationDTO.setStatusCode(response.getStatusLine().getStatusCode());
            apiEndpointValidationDTO.setStatusMessage(
                    HttpStatus.getStatusText(response.getStatusLine().getStatusCode()));
        } catch (UnknownHostException e) {
            log.error("UnknownHostException occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationDTO.setError("Unknown Host");
        } catch (IOException e) {
            log.error("Error occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationDTO.setError("Connection error");
        } finally {
            method.releaseConnection();
        }

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        apiEndpointValidationResponseDTO.setError("");
        apiEndpointValidationResponseDTO = APIMappingUtil.fromEndpointValidationToDTO(apiEndpointValidationDTO);
        return apiEndpointValidationResponseDTO;
    }

    public static ResourcePathListDTO getAPIResourcePaths(String apiId, Integer limit, Integer offset)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        List<ResourcePath> apiResourcePaths = apiProvider.getResourcePathsOfAPI(apiIdentifier);

        ResourcePathListDTO dto = APIMappingUtil.fromResourcePathListToDTO(apiResourcePaths, limit, offset);
        APIMappingUtil.setPaginationParamsForAPIResourcePathList(dto, offset, limit, apiResourcePaths.size());
        return dto;
    }

    public static OpenAPIDefinitionValidationResponseDTO validateOpenAPIDefinition(Boolean returnContent, String url,
                                                                                   InputStream fileInputStream,
                                                                                   String inlineApiDefinition,
                                                                                   String fileName)
            throws APIManagementException {
        // Validate and retrieve the OpenAPI definition
        Map<String, Object> validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileName,
                inlineApiDefinition, returnContent, false);

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        if (!validationResponseDTO.isIsValid()) {
            List<ErrorListItemDTO> errors = validationResponseDTO.getErrors();
            for (ErrorListItemDTO error : errors) {
                log.error("Error while parsing OpenAPI definition. Error code: " + error.getCode() + ". Error: "
                        + error.getDescription());
            }
        }
        return validationResponseDTO;
    }

    public static WSDLValidationResponseDTO validateWSDLDefinition(String url, InputStream fileInputStream,
                                                                   String fileName) throws APIManagementException {

        Map<String, Object> validationResponseMap = validateWSDL(url, fileInputStream, fileName, false);
        return (WSDLValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
    }

    public static Map<String, Object> validateOpenAPIDefinition(String url, InputStream inputStream, String fileName,
                                                                String apiDefinition, Boolean returnContent,
                                                                Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(inputStream, fileName, url, apiDefinition, isServiceAPI);

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (inputStream != null) {
            try {
                if (fileName != null) {
                    if (fileName.endsWith(".zip")) {
                        validationResponse =
                                OASParserUtil.extractAndValidateOpenAPIArchive(inputStream, returnContent);
                    } else {
                        String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                }
            } catch (IOException e) {
                throw new APIManagementException("Error while processing the file input",
                        e, ExceptionCodes.from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION));
            }
        } else if (apiDefinition != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(apiDefinition, returnContent);
        }

        OpenAPIDefinitionValidationResponseDTO responseDTO =
                APIMappingUtil.getOpenAPIDefinitionValidationResponseFromModel(validationResponse, returnContent);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * @param url           URL of the OpenAPI definition
     * @param inputStream   OpenAPI definition file
     * @param apiDefinition OpenAPI definition
     * @param fileName      Filename of the definition file
     * @param returnContent Whether to return json or not
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException when file parsing fails
     */
    public static APIDefinitionValidationResponse validateOpenAPIDefinition(String url, InputStream inputStream,
                                                                            String apiDefinition, String fileName,
                                                                            boolean returnContent)
            throws APIManagementException {

        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (inputStream != null) {
            try {
                if (fileName != null) {
                    if (fileName.endsWith(".zip")) {
                        validationResponse =
                                OASParserUtil.extractAndValidateOpenAPIArchive(inputStream, returnContent);
                    } else {
                        String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                    }
                } else {
                    String openAPIContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                    validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
                }
            } catch (IOException e) {
                throw new APIManagementException("Error while processing the file input",
                        e, ExceptionCodes.from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION));
            }
        } else if (apiDefinition != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(apiDefinition, returnContent);
        }

        return validationResponse;
    }

    public static APIDTO importWSDLDefinition(InputStream fileInputStream, String fileName, String fileContentType,
                                              String url, String additionalProperties, String implementationType,
                                              String organization) throws APIManagementException {

        WSDLValidationResponse validationResponse = validateWSDLAndReset(fileInputStream, fileName, url);

        if (StringUtils.isEmpty(implementationType)) {
            implementationType = APIDTO.TypeEnum.SOAP.toString();
        }

        boolean isSoapToRestConvertedAPI = APIDTO.TypeEnum.SOAPTOREST.toString().equals(implementationType);
        boolean isSoapAPI = APIDTO.TypeEnum.SOAP.toString().equals(implementationType);

        APIDTO additionalPropertiesAPI = null;
        APIDTO createdApiDTO;

        try {

            // Minimum requirement name, version, context and endpointConfig.
            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
        } catch (IOException e) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                    "Error occurred while importing WSDL"));
        }
        String username = RestApiCommonUtil.getLoggedInUsername();
        additionalPropertiesAPI.setProvider(username);
        additionalPropertiesAPI.setType(APIDTO.TypeEnum.fromValue(implementationType));
        API apiToAdd = PublisherCommonUtils
                .prepareToCreateAPIByDTO(additionalPropertiesAPI, RestApiCommonUtil.getLoggedInUserProvider(),
                        username, organization);
        apiToAdd.setWsdlUrl(url);
        API createdApi;
        if (isSoapAPI) {
            createdApi = importSOAPAPI(validationResponse.getWsdlProcessor().getWSDL(), fileContentType, url,
                    apiToAdd, organization, null);
        } else if (isSoapToRestConvertedAPI) {
            String wsdlArchiveExtractedPath = null;
            if (validationResponse.getWsdlArchiveInfo() != null) {
                wsdlArchiveExtractedPath = validationResponse.getWsdlArchiveInfo().getLocation()
                        + File.separator + APIConstants.API_WSDL_EXTRACTED_DIRECTORY;
            }
            createdApi = importSOAPToRESTAPI(validationResponse.getWsdlProcessor().getWSDL(), fileName, url,
                    wsdlArchiveExtractedPath, apiToAdd, organization);
        } else {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE,
                            "Invalid implementationType parameter"));
        }
        return APIMappingUtil.fromAPItoDTO(createdApi);
    }

    private static API importSOAPAPI(InputStream fileInputStream, String fileContentType, String url, API apiToAdd,
                                     String organization, ServiceEntry service) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //adding the api
        apiProvider.addAPI(apiToAdd);

        if (StringUtils.isNotBlank(url)) {
            apiToAdd.setWsdlUrl(url);
            apiProvider.addWSDLResource(apiToAdd.getUuid(), null, url, organization);
        } else if (fileContentType != null && fileInputStream != null) {
            PublisherCommonUtils
                    .addWsdl(fileContentType, fileInputStream, apiToAdd, apiProvider,
                            organization);
        } else if (service != null && fileInputStream == null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE,
                            "Error while importing WSDL to create a SOAP API"));
        } else if (service != null) {
            PublisherCommonUtils.addWsdl(RestApiConstants.APPLICATION_OCTET_STREAM,
                    fileInputStream, apiToAdd, apiProvider, organization);
        }

        //add the generated swagger definition to SOAP
        final String soapOperation = getSOAPOperation();
        String apiDefinition = ApisApiCommonImpl.generateSOAPAPIDefinition(apiToAdd, soapOperation);
        apiProvider.saveSwaggerDefinition(apiToAdd, apiDefinition, organization);
        //Retrieve the newly added API to send in the response payload
        return apiProvider.getAPIbyUUID(apiToAdd.getUuid(), organization);
    }

    /**
     * Import an API from WSDL as a SOAP-to-REST API
     *
     * @param fileInputStream          file data as input stream
     * @param fileName                 File name
     * @param url                      URL of the WSDL
     * @param wsdlArchiveExtractedPath Extraction path
     * @param apiToAdd                 API object to be added to the system (which is not added yet)
     * @param organization             Organization Identifier
     * @return API added api
     * @throws APIManagementException
     */
    private static API importSOAPToRESTAPI(InputStream fileInputStream, String fileName, String url,
                                           String wsdlArchiveExtractedPath, API apiToAdd, String organization)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //adding the api
            API createdApi = apiProvider.addAPI(apiToAdd);

            String swaggerStr = ApisApiCommonImpl.getSwaggerString(fileInputStream, url, wsdlArchiveExtractedPath,
                    fileName);
            String updatedSwagger = updateSwagger(createdApi.getUUID(), swaggerStr, organization);
            return PublisherCommonUtils
                    .updateAPIBySettingGenerateSequencesFromSwagger(updatedSwagger, createdApi, apiProvider,
                            organization);
        } catch (FaultGatewaysException | IOException e) {
            String errorMessage = "Error while importing WSDL to create a SOAP-to-REST API";
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * Validates the provided WSDL and reset the streams as required
     *
     * @param fileInputStream file input stream
     * @param fileName        File name
     * @param url             WSDL url
     * @return WSDL validation response
     * @throws APIManagementException when error occurred during the operation
     */
    private static WSDLValidationResponse validateWSDLAndReset(InputStream fileInputStream, String fileName, String url)
            throws APIManagementException {

        Map<String, Object> validationResponseMap = validateWSDL(url, fileInputStream, fileName, false);
        WSDLValidationResponse validationResponse =
                (WSDLValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (validationResponse.getWsdlInfo() == null) {
            // Validation failure
            throw new APIManagementException(validationResponse.getError());
        }
        return validationResponse;
    }

    public static APIDTO importOpenAPIDefinition(InputStream fileInputStream, String url, String additionalProperties,
                                                 String inlineApiDefinition, String organization, String fileName)
            throws APIManagementException {

        // validate 'additionalProperties' json
        if (StringUtils.isBlank(additionalProperties)) {
            throw new APIManagementException("'additionalProperties' is required and should not be null",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        // Convert the 'additionalProperties' json into an APIDTO object
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
        } catch (IOException e) {
            throw new APIManagementException("Error while parsing 'additionalProperties'",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }

        // validate sandbox and production endpoints
        if (!PublisherCommonUtils.validateEndpoints(apiDTOFromProperties)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        LinkedHashMap<?, ?> endpointConfig = (LinkedHashMap) apiDTOFromProperties.getEndpointConfig();

        // OAuth 2.0 backend protection: API Key and API Secret encryption
        PublisherCommonUtils
                .encryptEndpointSecurityOAuthCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                        StringUtils.EMPTY, StringUtils.EMPTY, apiDTOFromProperties);

        // Import the API and Definition
        return importOpenAPIDefinition(fileInputStream, url, inlineApiDefinition, apiDTOFromProperties, fileName,
                null, organization);
    }

    private static APIDTO importOpenAPIDefinition(InputStream definition, String definitionUrl, String inlineDefinition,
                                                  APIDTO apiDTOFromProperties, String fileName, ServiceEntry service,
                                                  String organization) throws APIManagementException {
        // Validate and retrieve the OpenAPI definition
        boolean isServiceAPI = service != null;

        Map<String, Object> validationResponseMap = validateOpenAPIDefinition(definitionUrl, definition, fileName,
                inlineDefinition, true, isServiceAPI);

        API addedAPI = ApisApiCommonImpl.importOpenAPIDefinition(apiDTOFromProperties, service, organization,
                isServiceAPI, validationResponseMap);
        return APIMappingUtil.fromAPItoDTO(addedAPI);
    }

    public static ResourceFile getWSDLOfAPI(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist
        return apiProvider.getWSDL(apiId, organization);
    }

    public static void updateWSDLOfAPI(String apiId, InputStream fileInputStream, String fileName, String contentType,
                                       String url, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        WSDLValidationResponse validationResponse = validateWSDLAndReset(fileInputStream, fileName, url);
        if (StringUtils.isNotBlank(url)) {
            apiProvider.addWSDLResource(apiId, null, url, organization);
        } else {
            ByteArrayInputStream wsdl = validationResponse.getWsdlProcessor().getWSDL();
            ResourceFile wsdlResource = ApisApiCommonImpl.getWSDLResource(wsdl, contentType);
            apiProvider.addWSDLResource(apiId, wsdlResource, null, organization);
        }
    }

    public static WorkflowResponseDTO changeAPILifecycle(String action, String apiId, String lifecycleChecklist,
                                                         String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiWrapper = new ApiTypeWrapper(apiProvider.getAPIbyUUID(apiId, organization));
        APIStateChangeResponse stateChangeResponse = PublisherCommonUtils.changeApiOrApiProductLifecycle(action,
                apiWrapper, lifecycleChecklist, organization);

        //returns the current lifecycle state
        LifecycleStateDTO stateDTO = getLifecycleState(apiId, organization);

        return APIMappingUtil.toWorkflowResponseDTO(stateDTO, stateChangeResponse);
    }

    public static APIDTO createNewAPIVersion(String newVersion, String apiId, Boolean defaultVersion,
                                             String serviceVersion, String organization) throws APIManagementException {

        APIDTO newVersionedApi = new APIDTO();
        ServiceEntry service = new ServiceEntry();

        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifierFromTable == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        int tenantId = APIUtil.getInternalOrganizationId(organization);
        API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
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
            service = serviceCatalog.getServiceByNameAndVersion(serviceName, serviceVersion, tenantId);
            if (service == null) {
                throw new APIManagementException("No matching service version found",
                        ExceptionCodes.SERVICE_VERSION_NOT_FOUND);
            }
        }
        if (StringUtils.isNotEmpty(serviceVersion) && !serviceVersion
                .equals(existingAPI.getServiceInfo("version"))) {
            APIDTO apidto = createAPIDTO(existingAPI, newVersion);
            if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) || ServiceEntry
                    .DefinitionType.OAS3.equals(service.getDefinitionType())) {
                newVersionedApi = importOpenAPIDefinition(service.getEndpointDef(), null, null, apidto,
                        null, service, organization);
            } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                newVersionedApi = importAsyncAPISpecification(service.getEndpointDef(), null, apidto,
                        null, service, organization);
            }
        } else {
            API versionedAPI = apiProvider.createNewAPIVersion(apiId, newVersion, defaultVersion, organization);
            if (APIConstants.API_TYPE_SOAPTOREST.equals(versionedAPI.getType())) {
                updateSwagger(versionedAPI.getUuid(), versionedAPI.getSwaggerDefinition(), organization);
            }
            newVersionedApi = APIMappingUtil.fromAPItoDTO(versionedAPI);
        }
        return newVersionedApi;
    }

    public static APIDTO importAsyncAPISpecification(InputStream fileInputStream, String url,
                                                     String additionalProperties, String organization, String fileName)
            throws APIManagementException {
        // validate 'additionalProperties' json
        if (StringUtils.isBlank(additionalProperties)) {
            String errorMessage = "'additionalProperties' is required and should not be null";
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
        }

        // Convert the 'additionalProperties' json into an APIDTO object
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
            if (apiDTOFromProperties.getType() == null) {
                String errorMessage = "Required property protocol is not specified for the Async API";
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            }
        } catch (IOException e) {
            String errorMessage = "Error while parsing 'additionalProperties'";
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }

        // validate whether ASYNC APIs created without advertise only enabled
        if (APIDTO.TypeEnum.ASYNC.equals(apiDTOFromProperties.getType()) &&
                (apiDTOFromProperties.getAdvertiseInfo() == null ||
                        !apiDTOFromProperties.getAdvertiseInfo().isAdvertised())) {
            String errorMessage = "ASYNC type APIs only can be created as third party APIs";
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
        }

        //validate websocket url and change transport types
        if (PublisherCommonUtils.isValidWSAPI(apiDTOFromProperties)) {
            ArrayList<String> websocketTransports = new ArrayList<>();
            websocketTransports.add(APIConstants.WS_PROTOCOL);
            websocketTransports.add(APIConstants.WSS_PROTOCOL);
            apiDTOFromProperties.setTransport(websocketTransports);
        }
        return importAsyncAPISpecification(fileInputStream, url, apiDTOFromProperties, fileName,
                null, organization);
    }

    private static APIDTO importAsyncAPISpecification(InputStream definition, String definitionUrl,
                                                      APIDTO apiDTOFromProperties, String fileName,
                                                      ServiceEntry service, String organization)
            throws APIManagementException {
        //validate and retrieve the AsyncAPI specification
        Map<String, Object> validationResponseMap = null;
        boolean isServiceAPI = false;

        if (service != null) {
            isServiceAPI = true;
        }
        validationResponseMap = validateAsyncAPISpecification(definitionUrl, definition, fileName, true,
                isServiceAPI);

        AsyncAPISpecificationValidationResponseDTO validationResponseDTO =
                (AsyncAPISpecificationValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            ErrorDTO errorDTO = APIMappingUtil.getErrorDTOFromErrorListItems(validationResponseDTO.getErrors());
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE,
                            errorDTO.getMessage()));
        }
        //Import the API and Definition

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = PublisherCommonUtils.importAsyncAPIWithDefinition(validationResponse, isServiceAPI,
                apiDTOFromProperties, service, organization, apiProvider);
        return APIMappingUtil.fromAPItoDTO(api);
    }

    public static AsyncAPISpecificationValidationResponseDTO validateAsyncAPISpecification(Boolean returnContent,
                                                                                           String url,
                                                                                           InputStream fileInputStream,
                                                                                           String fileName)
            throws APIManagementException {
        //validate and retrieve the AsyncAPI specification
        Map<String, Object> validationResponseMap = validateAsyncAPISpecification(url, fileInputStream, fileName,
                returnContent, false);
        return (AsyncAPISpecificationValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
    }

    /**
     * Validate the provided AsyncAPI specification (via file or url) and return a Map with the validation response
     * information
     *
     * @param url             AsyncAPI specification url
     * @param fileInputStream file as input stream
     * @param returnContent   whether to return the content of the definition in the response DTO
     * @param isServiceAPI    whether the request is to create API from a service in Service Catalog
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     * of type AsyncAPISpecificationValidationResponseDTO for the REST API. A value with the key 'model' will have the
     * validation response of type APIDefinitionValidationResponse coming from the impl level
     */
    private static Map<String, Object> validateAsyncAPISpecification(String url, InputStream fileInputStream,
                                                                     String fileName, Boolean returnContent,
                                                                     Boolean isServiceAPI)
            throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileName, url, null, isServiceAPI);

        AsyncAPISpecificationValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        if (url != null) {
            //validate URL
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(url, returnContent);
        } else if (fileInputStream != null) {
            //validate file
            if (fileName == null) {
                fileName = StringUtils.EMPTY;
            }
            String schemaToBeValidated = ApisApiCommonImpl.getSchemaToBeValidated(fileInputStream, isServiceAPI,
                    fileName);
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecification(schemaToBeValidated, returnContent);
        }

        responseDTO = APIMappingUtil.getAsyncAPISpecificationValidationResponseFromModel(validationResponse,
                returnContent);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    public static APIKeyDTO generateInternalAPIKey(String apiId) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
        String token = apiProvider.generateApiKey(apiId);
        APIKeyDTO apiKeyDTO = new APIKeyDTO();
        apiKeyDTO.setApikey(token);
        apiKeyDTO.setValidityTime(60 * 1000);
        return apiKeyDTO;
    }

    public static APIDTO importGraphQLSchema(InputStream fileInputStream, String additionalProperties,
                                             String organization) throws APIManagementException {

        APIDTO additionalPropertiesAPI = null;
        String schema = "";

        try {
            if (fileInputStream == null || StringUtils.isBlank(additionalProperties)) {
                String errorMessage = "GraphQL schema and api details cannot be empty.";
                throw new APIManagementException(
                        ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
            } else {
                schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            }

            if (!StringUtils.isBlank(additionalProperties) && !StringUtils.isBlank(schema) && log.isDebugEnabled()) {
                log.debug("Deseriallizing additionalProperties: " + additionalProperties + "/n"
                        + "importing schema: " + schema);
            }

            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
        } catch (IOException e) {
            String errorMessage = "Error while retrieving content from file";
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        additionalPropertiesAPI.setType(APIDTO.TypeEnum.GRAPHQL);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(additionalPropertiesAPI, apiProvider,
                RestApiCommonUtil.getLoggedInUsername(), organization);

        //Save swagger definition of graphQL
        String apiDefinition = getApiDefinition(apiToAdd);
        apiToAdd.setSwaggerDefinition(apiDefinition);

        //adding the api
        API createdApi = apiProvider.addAPI(apiToAdd);

        apiProvider.saveGraphqlSchemaDefinition(createdApi.getUuid(), schema, organization);

        return APIMappingUtil.fromAPItoDTO(createdApi);
    }

    public static void importAPI(InputStream fileInputStream, Boolean preserveProvider, Boolean rotateRevision,
                                 Boolean overwrite, String organization, String[] tokenScopes)
            throws APIManagementException {

        overwrite = overwrite != null && overwrite;

        // Check if the URL parameter value is specified, otherwise the default value is true.
        preserveProvider = preserveProvider == null || preserveProvider;

        ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
        importExportAPI.importAPI(fileInputStream, preserveProvider, rotateRevision, overwrite, tokenScopes,
                organization);
    }

    public static GraphQLValidationResponseDTO validateGraphQLSchema(InputStream fileInputStream, String filename) {

        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();

        try {
            String schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            validationResponse = PublisherCommonUtils.validateGraphQLSchema(filename, schema);
        } catch (IOException | APIManagementException e) {
            validationResponse.setIsValid(false);
            validationResponse.setErrorMessage(e.getMessage());
        }
        return validationResponse;
    }

    public static String generateMockScripts(String apiId, String organization) throws APIManagementException {

        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifierFromTable == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);

        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        apiDefinition = String.valueOf(OASParserUtil.generateExamples(apiDefinition).get(APIConstants.SWAGGER));
        apiProvider.saveSwaggerDefinition(originalAPI, apiDefinition, organization);
        return apiDefinition;
    }

    public static List<Tier> getAPISubscriptionPolicies(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIDTO apiInfo = getAPIByID(apiId, apiProvider, organization);
        List<Tier> availableThrottlingPolicyList = ThrottlingPoliciesApiCommonImpl
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(),
                        true);

        List<String> apiPolicies = apiInfo.getPolicies();
        return filterAPIThrottlingPolicies(apiPolicies, availableThrottlingPolicyList);
    }

    public static APIRevisionListDTO getAPIRevisions(String apiId, String query) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIRevisionListDTO apiRevisionListDTO;
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        List<APIRevision> apiRevisionsList = filterAPIRevisionsByDeploymentStatus(query, apiRevisions);
        apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
        return apiRevisionListDTO;
    }

    public static APIRevisionDTO createAPIRevision(String apiId, APIRevisionDTO apIRevisionDTO, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //validate whether the API is advertise only
        APIDTO apiDto = getAPIByID(apiId, apiProvider, organization);
        if (apiDto.getAdvertiseInfo() != null && apiDto.getAdvertiseInfo().isAdvertised()) {
            throw new APIManagementException("Creating API Revisions is not supported for third party APIs: "
                    + apiId, ExceptionCodes.INTERNAL_ERROR);
        }

        APIRevision apiRevision = new APIRevision();
        apiRevision.setApiUUID(apiId);
        apiRevision.setDescription(apIRevisionDTO.getDescription());
        //adding the api revision
        String revisionId = apiProvider.addAPIRevision(apiRevision, organization);

        //Retrieve the newly added APIRevision to send in the response payload
        APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
        return APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
    }

    public static APIRevisionListDTO deleteAPIRevision(String apiId, String revisionId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.deleteAPIRevision(apiId, revisionId, organization);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        return APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
    }

    public static List<APIRevisionDeploymentDTO> deployAPIRevision(String apiId, String revisionId,
                                                                   List<APIRevisionDeploymentDTO> revisionDeployments,
                                                                   String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //validate whether the API is advertise only
        APIDTO apiDto = getAPIByID(apiId, apiProvider, organization);
        if (apiDto.getAdvertiseInfo() != null && Boolean.TRUE.equals(apiDto.getAdvertiseInfo().isAdvertised())) {
            String errorMessage = "Deploying API Revisions is not supported for third party APIs: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }

        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : revisionDeployments) {
            String environment = apiRevisionDeploymentDTO.getName();
            Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
            String vhost = apiRevisionDeploymentDTO.getVhost();
            APIRevisionDeployment apiRevisionDeployment = mapAPIRevisionDeploymentWithValidation(revisionId,
                    environments, environment, displayOnDevportal, vhost, true);
            apiRevisionDeployments.add(apiRevisionDeployment);
        }
        apiProvider.deployAPIRevision(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionsDeploymentList(apiId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return apiRevisionDeploymentDTOS;
    }

    public static List<APIRevisionDeploymentDTO> getAPIRevisionDeployments(String apiId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = apiProvider.getAPIRevisionsDeploymentList(apiId);

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return apiRevisionDeploymentDTOS;
    }

    public static List<APIRevisionDeploymentDTO> undeployAPIRevision(String apiId, String revisionId,
                                                                     String revisionNum, Boolean allEnvironments,
                                                                     List<APIRevisionDeploymentDTO> revisionDeployments,
                                                                     String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        if (revisionId == null && revisionNum != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNum, apiId);
            if (revisionId == null) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED));
            }
        }

        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        if (Boolean.TRUE.equals(allEnvironments)) {
            apiRevisionDeployments = apiProvider.getAPIRevisionDeploymentList(revisionId);
        } else {
            for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : revisionDeployments) {
                Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
                String vhost = apiRevisionDeploymentDTO.getVhost();
                String environment = apiRevisionDeploymentDTO.getName();
                APIRevisionDeployment apiRevisionDeployment = mapAPIRevisionDeploymentWithValidation(revisionId,
                        environments, environment, displayOnDevportal, vhost, false);
                apiRevisionDeployments.add(apiRevisionDeployment);
            }
        }
        apiProvider.undeployAPIRevisionDeployment(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse =
                apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return apiRevisionDeploymentDTOS;
    }

    public static APIDTO restoreAPIRevision(String apiId, String revisionId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.restoreAPIRevision(apiId, revisionId, organization);
        return getAPIByID(apiId, apiProvider, organization);
    }

    public static String getAsyncAPIDefinition(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return RestApiCommonUtil.retrieveAsyncAPIDefinition(api, apiProvider);
    }

    public static String updateAsyncAPIDefinition(String apiId, String apiDefinition, String url,
                                                  InputStream fileInputStream, String organization, String fileName)
            throws APIManagementException {

        String updatedAsyncAPIDefinition;
        try {
            //Handle URL and file based definition imports
            if (url != null || fileInputStream != null) {
                //Validate and retrieve the AsyncAPI definition
                Map<String, Object> validationResponseMap = validateAsyncAPISpecification(url, fileInputStream,
                        fileName, true, false);
                APIDefinitionValidationResponse validationResponse =
                        (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
                if (!validationResponse.isValid()) {
                    throw new APIManagementException(ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
                }
                updatedAsyncAPIDefinition = PublisherCommonUtils.updateAsyncAPIDefinition(apiId, validationResponse,
                        organization);
            } else {
                APIDefinitionValidationResponse response = AsyncApiParserUtil
                        .validateAsyncAPISpecification(apiDefinition, true);
                if (!response.isValid()) {
                    throw new APIManagementException(ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
                }
                updatedAsyncAPIDefinition = PublisherCommonUtils.updateAsyncAPIDefinition(apiId, response,
                        organization);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = ERROR_WHILE_UPDATING_API + apiId;
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return updatedAsyncAPIDefinition;
    }

    public static APIDTO importServiceFromCatalog(String serviceKey, APIDTO apiDto, String organization)
            throws APIManagementException {

        if (StringUtils.isEmpty(serviceKey)) {
            throw new APIManagementException("Required parameter serviceKey is missing",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }

        String username = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(username);
        ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
        ServiceEntry service = serviceCatalog.getServiceByKey(serviceKey, tenantId);
        if (service == null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.RESOURCE_NOT_FOUND_WITH_TYPE_AND_ID, "Service", serviceKey));
        }
        APIDTO createdApiDTO = null;
        if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
            createdApiDTO = importOpenAPIDefinition(service.getEndpointDef(), null, null, apiDto, null, service,
                    organization);
        } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
            createdApiDTO = importAsyncAPISpecification(service.getEndpointDef(), null, apiDto, null, service,
                    organization);
        } else if (ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())) {
            apiDto.setProvider(RestApiCommonUtil.getLoggedInUsername());
            apiDto.setType(APIDTO.TypeEnum.fromValue("SOAP"));
            API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDto,
                    RestApiCommonUtil.getLoggedInUserProvider(), username, organization);
            apiToAdd.setServiceInfo("key", service.getServiceKey());
            apiToAdd.setServiceInfo("md5", service.getMd5());
            apiToAdd.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service
                    .getServiceUrl(), null));
            API api = importSOAPAPI(service.getEndpointDef(), null, null,
                    apiToAdd, organization, service);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(api);
        }
        if (createdApiDTO == null) {
            String errorMessage = "Unsupported definition type provided. Cannot create API " +
                    "using the service type " + service.getDefinitionType().name();
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
        }
        return createdApiDTO;
    }

    public static APIDTO reimportServiceFromCatalog(String apiId, String organization) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(username);

        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
        String serviceKey = apiProvider.retrieveServiceKeyByApiId(originalAPI.getId().getId(), tenantId);
        ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
        ServiceEntry service = serviceCatalog.getServiceByKey(serviceKey, tenantId);
        JSONObject serviceInfo = getServiceInfo(service);
        api.setServiceInfo(serviceInfo);

        Map<String, Object> validationResponseMap = new HashMap<>();
        if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
            validationResponseMap = validateOpenAPIDefinition(null, service.getEndpointDef(), null, null,
                    true, true);
        } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
            validationResponseMap = validateAsyncAPISpecification(null, service.getEndpointDef(),
                    null, true, true);
        } else if (!ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())) {
            String errorMessage = "Unsupported definition type provided. Cannot re-import service to " +
                    "API using the service type " + service.getDefinitionType();
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INVALID_PARAMETERS_PROVIDED_WITH_MESSAGE, errorMessage));
        }
        APIDefinitionValidationResponse validationAPIResponse = null;
        if (ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())) {
            PublisherCommonUtils.addWsdl(RestApiConstants.APPLICATION_OCTET_STREAM,
                    service.getEndpointDef(), api, apiProvider, organization);
        } else {
            validationAPIResponse =
                    (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
            if (!validationAPIResponse.isValid()) {
                throw new APIManagementException(ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
            }
        }
        String protocol = (validationAPIResponse != null ? validationAPIResponse.getProtocol() : "");
        if (!APIConstants.API_TYPE_WEBSUB.equalsIgnoreCase(protocol)) {
            api.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service.getServiceUrl(),
                    protocol));
        }
        API updatedApi;
        try {
            updatedApi = apiProvider.updateAPI(api, originalAPI);
            if (validationAPIResponse != null) {
                PublisherCommonUtils.updateAPIDefinition(apiId, validationAPIResponse, service, organization);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = ERROR_WHILE_UPDATING_API + apiId;
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        return APIMappingUtil.fromAPItoDTO(updatedApi);
    }

    public static APIRevisionDeploymentDTO updateAPIDeployment(String apiId, String deploymentId,
                                                               APIRevisionDeploymentDTO apIRevisionDeploymentDTO)
            throws APIManagementException {

        String revisionId = apIRevisionDeploymentDTO.getRevisionUuid();
        String vhost = apIRevisionDeploymentDTO.getVhost();
        Boolean displayOnDevportal = apIRevisionDeploymentDTO.isDisplayOnDevportal();
        APIRevisionDeployment apiRevisionDeploymentsResponse = updateApiRevisionDeployment(apiId, deploymentId,
                revisionId, vhost, displayOnDevportal);
        return APIMappingUtil.
                fromAPIRevisionDeploymenttoDTO(apiRevisionDeploymentsResponse);
    }

    public static String getEnvironmentSpecificAPIProperties(String apiId, String envId)
            throws APIManagementException {

        // validate api UUID
        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // get properties
        EnvironmentPropertiesDTO properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }

    public static String updateEnvironmentSpecificAPIProperties(String apiId, String envId, Map<String,
            String> requestBody, String organization) throws APIManagementException {

        // validate api UUID
        RestApiCommonUtil.validateAPIExistence(apiId);
        validateEnvironment(organization, envId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        // adding properties
        EnvironmentPropertiesDTO properties = ApisApiCommonImpl.generateEnvironmentPropertiesDTO(requestBody);
        apiProvider.addEnvironmentSpecificAPIProperties(apiId, envId, properties);

        // get properties
        properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }

    /**
     * @param apiToAdd           API which will be added
     * @param apiProvider        API Provider Impl
     * @param service            ServiceCatalog service
     * @param validationResponse OpenAPI defnition validation response
     * @param isServiceAPI       whether the API is created from a service
     * @param syncOperations     sync all API operations
     * @throws APIManagementException when scope validation or OpenAPI parsing fails
     */
    public static API importAPIDefinition(API apiToAdd, APIProvider apiProvider, String organization,
                                          ServiceEntry service, APIDefinitionValidationResponse validationResponse,
                                          boolean isServiceAPI, boolean syncOperations)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        if (isServiceAPI) {
            apiToAdd.setServiceInfo("key", service.getServiceKey());
            apiToAdd.setServiceInfo("md5", service.getMd5());
            apiToAdd.setEndpointConfig(RestApiCommonUtil.constructEndpointConfigForService(service
                    .getServiceUrl(), null));
        }
        APIDefinition apiDefinition = validationResponse.getParser();
        SwaggerData swaggerData;
        String definitionToAdd = validationResponse.getJsonContent();
        if (syncOperations) {
            RestApiCommonUtil.validateScopes(apiToAdd, apiProvider, username);
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
            RestApiCommonUtil.validateScopes(apiToAdd, apiProvider, username);
            swaggerData = new SwaggerData(apiToAdd);
            definitionToAdd = apiDefinition
                    .populateCustomManagementInfo(validationResponse.getJsonContent(), swaggerData);
        }

        // adding the definition
        apiToAdd.setSwaggerDefinition(definitionToAdd);

        API addedAPI = apiProvider.addAPI(apiToAdd);
        // retrieving the added API for returning as the response
        // this would provide the updated templates
        addedAPI = apiProvider.getAPIbyUUID(addedAPI.getUuid(), organization);

        return addedAPI;
    }

    /**
     * @param api           API
     * @param soapOperation SOAP Operation
     * @return SOAP API Definition
     * @throws APIManagementException if an error occurred while parsing string to JSON Object
     */
    public static String generateSOAPAPIDefinition(API api, String soapOperation) throws APIManagementException {

        APIDefinition oasParser = new OAS2Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        String apiDefinition = oasParser.generateAPIDefinition(swaggerData);
        JSONParser jsonParser = new JSONParser();
        JSONObject apiJson;
        JSONObject paths;
        try {
            apiJson = (JSONObject) jsonParser.parse(apiDefinition);
            paths = (JSONObject) jsonParser.parse(soapOperation);
            apiJson.replace("paths", paths);
            return apiJson.toJSONString();
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the api definition.";
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * @param fileInputStream          File input stream for the WSDL file
     * @param url                      URL
     * @param wsdlArchiveExtractedPath Path to WSDL extracted directory
     * @param filename                 File Name
     * @return Swagger string
     * @throws APIManagementException If the WSDL file not supported
     * @throws IOException            If error occurred in converting InputStream to a byte array
     */
    public static String getSwaggerString(InputStream fileInputStream, String url, String wsdlArchiveExtractedPath,
                                          String filename) throws APIManagementException, IOException {

        String swaggerStr = "";
        if (StringUtils.isNotBlank(url)) {
            swaggerStr = SOAPOperationBindingUtils.getSoapOperationMappingForUrl(url);
        } else if (fileInputStream != null) {
            if (filename.endsWith(".zip")) {
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlArchiveExtractedPath);
            } else if (filename.endsWith(".wsdl")) {
                byte[] wsdlContent = APIUtil.toByteArray(fileInputStream);
                swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(wsdlContent);
            } else {
                throw new APIManagementException(ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
            }
        }
        return swaggerStr;
    }

    /**
     * @param wsdlInputStream WSDL file input stream
     * @param contentType     content type of the wsdl
     * @return Resource file WSDL
     */
    public static ResourceFile getWSDLResource(InputStream wsdlInputStream, String contentType) {

        ResourceFile wsdlResource;
        if (APIConstants.APPLICATION_ZIP.equals(contentType) ||
                APIConstants.APPLICATION_X_ZIP_COMPRESSED.equals(contentType)) {
            wsdlResource = new ResourceFile(wsdlInputStream, APIConstants.APPLICATION_ZIP);
        } else {
            wsdlResource = new ResourceFile(wsdlInputStream, contentType);
        }
        return wsdlResource;
    }

    /**
     * @param api API
     * @return API definition
     * @throws APIManagementException If any error occurred in generating API definition from swagger data
     */
    private static String getApiDefinition(API api) throws APIManagementException {

        APIDefinition parser = new OAS3Parser();
        SwaggerData swaggerData = new SwaggerData(api);
        return parser.generateAPIDefinition(swaggerData);
    }

    /**
     * @param apiPolicies                   Policy names applied to the API
     * @param availableThrottlingPolicyList All available policies
     * @return Filtered API policy list which are applied to the API
     */
    public static List<Tier> filterAPIThrottlingPolicies(List<String> apiPolicies,
                                                         List<Tier> availableThrottlingPolicyList) {

        List<Tier> apiThrottlingPolicies = new ArrayList<>();
        if (apiPolicies != null && !apiPolicies.isEmpty()) {
            for (Tier tier : availableThrottlingPolicyList) {
                if (apiPolicies.contains(tier.getName())) {
                    apiThrottlingPolicies.add(tier);
                }
            }
        }
        return apiThrottlingPolicies;
    }

    /**
     * @param deploymentStatus Deployment status [deployed:true / deployed:false]
     * @param apiRevisions     API revisions list
     * @return Filtered API revisions according to the deploymentStatus
     */
    public static List<APIRevision> filterAPIRevisionsByDeploymentStatus(String deploymentStatus,
                                                                         List<APIRevision> apiRevisions) {

        if ("deployed:true".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
            return apiDeployedRevisions;
        } else if ("deployed:false".equalsIgnoreCase(deploymentStatus)) {
            List<APIRevision> apiNotDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : apiRevisions) {
                if (apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiNotDeployedRevisions.add(apiRevision);
                }
            }
            return apiNotDeployedRevisions;
        }
        return apiRevisions;
    }

    /**
     * @param revisionId         Revision ID
     * @param environments       Environments of the organization
     * @param environment        Selected environment
     * @param displayOnDevportal Enable display on Developer Portal
     * @param vhost              Virtual Host of the revision deployment
     * @param mandatoryVHOST     Is vhost mandatory in this validation
     * @return Created {@link APIRevisionDeployment} after validations
     * @throws APIManagementException if any validation fails
     */
    public static APIRevisionDeployment mapAPIRevisionDeploymentWithValidation(String revisionId,
                                                                               Map<String, Environment> environments,
                                                                               String environment,
                                                                               Boolean displayOnDevportal,
                                                                               String vhost, boolean mandatoryVHOST)
            throws APIManagementException {

        if (environments.get(environment) == null) {
            final String errorMessage = "Gateway environment not found: " + environment;
            throw new APIManagementException(errorMessage, ExceptionCodes.from(
                    ExceptionCodes.INVALID_GATEWAY_ENVIRONMENT, String.format("name '%s'", environment)));

        }
        if (mandatoryVHOST && StringUtils.isEmpty(vhost)) {
            // vhost is only required when deploying a revision, not required when un-deploying a revision
            // since the same scheme 'APIRevisionDeployment' is used for deploy and undeploy, handle it here.
            throw new APIManagementException("Required field 'vhost' not found in deployment",
                    ExceptionCodes.GATEWAY_ENVIRONMENT_VHOST_NOT_PROVIDED);
        }
        return mapApiRevisionDeployment(revisionId, vhost, displayOnDevportal, environment);
    }

    public static APIRevisionDeployment updateApiRevisionDeployment(String apiId, String deploymentId,
                                                                    String revisionId, String vhost,
                                                                    Boolean displayOnDevportal)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String decodedDeploymentName = ApisApiCommonImpl.getDecodedDeploymentName(deploymentId);
        APIRevisionDeployment apiRevisionDeployment = ApisApiCommonImpl.mapApiRevisionDeployment(revisionId, vhost,
                displayOnDevportal, decodedDeploymentName);
        apiProvider.updateAPIDisplayOnDevportal(apiId, revisionId, apiRevisionDeployment);
        return apiProvider.getAPIRevisionDeployment(decodedDeploymentName, revisionId);
    }

    /**
     * @param revisionId         Revision ID
     * @param vhost              Virtual Host
     * @param displayOnDevportal Enable displaying on Developer Portal
     * @param deployment         Deployment
     * @return Mapped {@link APIRevisionDeployment}
     */
    public static APIRevisionDeployment mapApiRevisionDeployment(String revisionId, String vhost,
                                                                 Boolean displayOnDevportal, String deployment) {

        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revisionId);
        apiRevisionDeployment.setDeployment(deployment);
        apiRevisionDeployment.setVhost(vhost);
        apiRevisionDeployment.setDisplayOnDevportal(displayOnDevportal);
        return apiRevisionDeployment;
    }

    /**
     * @param deploymentId Deployment ID
     * @return Deployment name decoded from the deploymentId
     * @throws APIMgtResourceNotFoundException If invalid or null deploymentId
     */
    public static String getDecodedDeploymentName(String deploymentId) throws APIMgtResourceNotFoundException {

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
        return decodedDeploymentName;
    }

    /**
     * @param fileInputStream API spec file input stream
     * @param isServiceAPI    Is service API
     * @param fileName        File name
     * @return Schema
     * @throws APIManagementException if error while reading the spec file contents
     */
    public static String getSchemaToBeValidated(InputStream fileInputStream, Boolean isServiceAPI, String fileName)
            throws APIManagementException {

        String schemaToBeValidated = null;
        if (Boolean.TRUE.equals(isServiceAPI) || fileName.endsWith(APIConstants.YAML_FILE_EXTENSION) || fileName
                .endsWith(APIConstants.YML_FILE_EXTENSION)) {
            //convert .yml or .yaml to JSON for validation
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            try {
                Object obj = yamlReader.readValue(fileInputStream, Object.class);
                ObjectMapper jsonWriter = new ObjectMapper();
                schemaToBeValidated = jsonWriter.writeValueAsString(obj);
            } catch (IOException e) {
                throw new APIManagementException("Error while reading file content", e,
                        ExceptionCodes.ERROR_READING_ASYNCAPI_SPECIFICATION);
            }
        } else if (fileName.endsWith(APIConstants.JSON_FILE_EXTENSION)) {
            //continue with .json
            JSONTokener jsonDataFile = new JSONTokener(fileInputStream);
            schemaToBeValidated = new org.json.JSONObject(jsonDataFile).toString();
        }
        return schemaToBeValidated;
    }

    /**
     * @param environmentPropertiesMap Environment Properties Map
     * @return {@link EnvironmentPropertiesDTO} mapped from the properties in the environmentPropertiesMap
     * @throws APIManagementException If error converting environmentPropertiesMap to {@link EnvironmentPropertiesDTO}
     */
    public static EnvironmentPropertiesDTO generateEnvironmentPropertiesDTO(
            Map<String, String> environmentPropertiesMap)
            throws APIManagementException {

        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.convertValue(environmentPropertiesMap, new TypeReference<EnvironmentPropertiesDTO>() {
            });
        } catch (IllegalArgumentException e) {
            String errorMessage = "Possible keys are productionEndpoint,sandboxEndpoint";
            throw new APIManagementException(e.getMessage(),
                    ExceptionCodes.from(ExceptionCodes.INVALID_ENV_API_PROP_CONFIG, errorMessage));
        }
    }

    /**
     * @param service service entry
     * @return service info JSON Object
     */
    public static JSONObject getServiceInfo(ServiceEntry service) {

        JSONObject serviceInfo = new JSONObject();
        serviceInfo.put("name", service.getName());
        serviceInfo.put("version", service.getVersion());
        serviceInfo.put("key", service.getServiceKey());
        serviceInfo.put("md5", service.getMd5());
        return serviceInfo;
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A list of API resource mediation policies with mock scripts
     * @throws APIManagementException when an internal errors occurs
     */
    public static MockResponsePayloadListDTO getGeneratedMockScriptsOfAPI(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        Map<String, Object> examples = OASParserUtil.generateExamples(apiDefinition);

        List<APIResourceMediationPolicy> policiesList =
                (List<APIResourceMediationPolicy>) examples.get(APIConstants.MOCK_GEN_POLICY_LIST);
        return APIMappingUtil.fromMockPayloadsToListDTO(policiesList);
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return Monetized policies to plan mapping
     * @throws APIManagementException when an internal error occurs
     */
    public static APIMonetizationInfoDTO getAPIMonetization(String apiId, String organization)
            throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when retrieving monetized plans.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        String uuid = RestApiCommonUtil.getAPIUUID(apiId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        Map<String, String> monetizedPoliciesToPlanMapping;
        try {
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            monetizedPoliciesToPlanMapping = monetizationImplementation.getMonetizedPoliciesToPlanMapping(api);
        } catch (MonetizationException e) {
            throw new APIManagementException("Error occurred while getting the Monetization mappings for API "
                    + api.getId().getApiName(), e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE,
                            "Error occurred while getting the Monetization mappings for API"));
        }
        return APIMappingUtil.getMonetizedTiersDTO(uuid, organization, monetizedPoliciesToPlanMapping);
    }

    public static APIMonetizationInfoDTO addAPIMonetization(String apiId, APIMonetizationInfoDTO body,
                                                            String organization)
            throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when configuring monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        RestApiCommonUtil.validateAPIExistence(apiId);

        boolean monetizationEnabled = body.isEnabled();
        Map<String, String> monetizationProperties = body.getProperties();
        //set the monetization status
        addAPIMonetization(apiId, organization, monetizationEnabled, monetizationProperties);
        return APIMappingUtil.getMonetizationInfoDTO(apiId, organization);
    }

    /**
     * @param apiId                  API UUID
     * @param organization           Tenant organization
     * @param monetizationEnabled    Whether to enable or disable monetization
     * @param monetizationProperties Monetization properties map
     * @return true if monetization state change is successful
     * @throws APIManagementException when a monetization related error occurs
     */
    private static boolean addAPIMonetization(String apiId, String organization,
                                              boolean monetizationEnabled, Map<String, String> monetizationProperties)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
            String errorMessage = "API " + api.getId().getApiName() +
                    " should be in published state to configure monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_API_STATE_MONETIZATION);
        }
        //set the monetization status
        api.setMonetizationEnabled(monetizationEnabled);
        //clear the existing properties related to monetization
        api.getMonetizationProperties().clear();
        for (Map.Entry<String, String> currentEntry : monetizationProperties.entrySet()) {
            api.addMonetizationProperty(currentEntry.getKey(), currentEntry.getValue());
        }

        Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
        HashMap<String, String> monetizationDataMap = new Gson().fromJson(api.getMonetizationProperties().toString(),
                HashMap.class);
        if (MapUtils.isEmpty(monetizationDataMap)) {
            String errorMessage = "Monetization is not configured. Monetization data is empty for "
                    + api.getId().getApiName();
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        boolean isMonetizationStateChangeSuccessful = false;
        try {
            if (monetizationEnabled) {
                isMonetizationStateChangeSuccessful = monetizationImplementation.enableMonetization
                        (organization, api, monetizationDataMap);
            } else {
                isMonetizationStateChangeSuccessful = monetizationImplementation.disableMonetization
                        (organization, api, monetizationDataMap);
            }
        } catch (MonetizationException e) {
            String errorMessage = "Error while changing monetization status for API ID : " + apiId;
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
        if (isMonetizationStateChangeSuccessful) {
            apiProvider.configureMonetizationInAPIArtifact(api);
            return true;
        } else {
            throw new APIManagementException("Unable to change monetization status for API : " + apiId,
                    ExceptionCodes.from(ExceptionCodes.MONETIZATION_STATE_CHANGE_FAILED,
                            String.valueOf(monetizationEnabled)));
        }
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @return A map of revenue details
     * @throws APIManagementException when retrieving monetization details fail
     */
    public static APIRevenueDTO getAPIRevenue(String apiId, String organization) throws APIManagementException {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when getting revenue details.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
            String errorMessage = "API " + api.getId().getApiName() +
                    " should be in published state to configure monetization.";
            throw new APIManagementException(errorMessage, ExceptionCodes.INVALID_API_STATE_MONETIZATION);
        }

        try {
            Map<String, String> revenueUsageData = monetizationImplementation.getTotalRevenue(api, apiProvider);
            APIRevenueDTO apiRevenueDTO = new APIRevenueDTO();
            apiRevenueDTO.setProperties(revenueUsageData);
            return apiRevenueDTO;
        } catch (MonetizationException e) {
            String errorMessage = "Error while getting revenue information for API ID : " + apiId;
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * @param policySpecification Operation policy specification
     * @param operationPolicyData Operation policy metadata
     * @param apiId               API UUID
     * @param organization        Tenant organization
     * @return Created policy ID
     * @throws APIManagementException when adding an operation policy fails
     */
    public static OperationPolicyDataDTO addAPISpecificOperationPolicy(OperationPolicySpecification policySpecification,
                                                                       OperationPolicyData operationPolicyData,
                                                                       String apiId, String organization)
            throws APIManagementException {

        String policyId;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        OperationPolicyData existingPolicy =
                apiProvider.getAPISpecificOperationPolicyByPolicyName(policySpecification.getName(),
                        policySpecification.getVersion(), apiId, null, organization, false);
        if (existingPolicy == null) {
            policyId = apiProvider.addAPISpecificOperationPolicy(apiId, operationPolicyData, organization);
            if (log.isDebugEnabled()) {
                log.debug("An API specific operation policy has been added for the API " + apiId);
            }
        } else {
            throw new APIManagementException("An API specific operation policy found for the same name.",
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_ALREADY_EXISTS,
                            policySpecification.getName(), policySpecification.getVersion()));
        }
        operationPolicyData.setPolicyId(policyId);
        return OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(operationPolicyData);
    }

    public static OperationPolicyDataListDTO getAllAPISpecificOperationPolicies(String apiId, Integer limit,
                                                                                Integer offset, String organization)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        // Lightweight API specific operation policy includes the policy ID and the policy specification.
        // Since policy definition is bit bulky, we don't query the definition unnecessarily.
        List<OperationPolicyData> sharedOperationPolicyLIst = apiProvider
                .getAllAPISpecificOperationPolicies(apiId, organization);
        return OperationPolicyMappingUtil.fromOperationPolicyDataListToDTO(sharedOperationPolicyLIst, offset, limit);
    }

    public static OperationPolicyDataDTO getOperationPolicyForAPIByPolicyId(String apiId, String operationPolicyId,
                                                                            String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate whether api exists or not
        RestApiCommonUtil.validateAPIExistence(apiId);

        OperationPolicyData existingPolicy = apiProvider
                .getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, false);
        if (existingPolicy == null) {
            throw new APIManagementException(getOperationPolicyRetrieveErrorMessage(apiId, operationPolicyId),
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
        }

        return OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(existingPolicy);
    }

    public static OperationPolicyData getAPISpecificOperationPolicyContentByPolicyId(String apiId, String
            operationPolicyId, String organization) throws APIManagementException {

        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        OperationPolicyData policyData = apiProvider
                .getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, true);
        if (policyData == null) {
            throw new APIMgtResourceNotFoundException(getOperationPolicyRetrieveErrorMessage(apiId, operationPolicyId),
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
        }
        return policyData;
    }

    /**
     * @param operationPolicyId Operation policy ID
     * @param apiId             API UUID
     * @param organization      Tenant organization
     * @throws APIManagementException when deleting API specific operation policy fails
     */
    public static void deleteAPISpecificOperationPolicyByPolicyId(String operationPolicyId, String apiId,
                                                                  String organization)
            throws APIManagementException {

        //validate if api exists
        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        OperationPolicyData existingPolicy = apiProvider
                .getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, false);
        if (existingPolicy != null) {
            apiProvider.deleteOperationPolicyById(operationPolicyId, organization);

            if (log.isDebugEnabled()) {
                log.debug("The operation policy " + operationPolicyId + " has been deleted from the the API "
                        + apiId);
            }
        } else {
            throw new APIManagementException(getOperationPolicyRetrieveErrorMessage(apiId, operationPolicyId),
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
        }
    }

    public static APIExternalStoreListDTO publishAPIToExternalStores(String apiId, String externalStoreIds,
                                                                     String organization)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<String> externalStoreIdList = Arrays.asList(externalStoreIds.split("\\s*,\\s*"));
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        if (!apiProvider.publishToExternalAPIStores(api, externalStoreIdList)) {
            throw new APIManagementException(ExceptionCodes.INTERNAL_ERROR);
        }
        Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(api.getUuid());
        return ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
    }

    /**
     * @param apiId        API UUID
     * @param organization Tenant organization
     * @param sequenceType Sequence type
     * @param resourcePath Resource path
     * @param verb         HTTP verb
     * @return Resource policy
     * @throws APIManagementException when getting resource policy fails
     */
    public static ResourcePolicyListDTO getAPIResourcePolicies(String apiId, String organization,
                                                               String sequenceType, String resourcePath, String verb)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(sequenceType) || !(Constants.IN_SEQUENCE.equals(sequenceType)
                || Constants.OUT_SEQUENCE.equals(sequenceType))) {
            throw new APIManagementException("Sequence type should be either of the values from 'in' or 'out'",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        String resourcePolicy = SequenceUtils.getRestToSoapConvertedSequence(api, sequenceType);

        if (StringUtils.isNotEmpty(resourcePath) && StringUtils.isNotEmpty(verb)) {
            try {
                JSONObject sequenceObj = (JSONObject) new JSONParser().parse(resourcePolicy);
                JSONObject resultJson = new JSONObject();
                String key = resourcePath + "_" + verb;
                JSONObject sequenceContent = (JSONObject) sequenceObj.get(key);
                if (sequenceContent == null) {
                    String errorMessage = "Cannot find any resource policy for Resource path : " + resourcePath +
                            " with type: " + verb;
                    throw new APIManagementException(errorMessage, ExceptionCodes.RESOURCE_NOT_FOUND);
                }
                resultJson.put(key, sequenceObj.get(key));
                resourcePolicy = resultJson.toJSONString();
            } catch (ParseException e) {
                throw new APIManagementException("Error while retrieving the resource policies for the API : " + apiId,
                        ExceptionCodes.JSON_PARSE_ERROR);
            }
        } else if (StringUtils.isEmpty(resourcePath)) {
            throw new APIManagementException("Resource path cannot be empty for the defined verb: " + verb,
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        } else if (StringUtils.isEmpty(verb)) {
            throw new APIManagementException("HTTP verb cannot be empty for the defined resource path: " + resourcePath,
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }

        return APIMappingUtil.fromResourcePolicyStrToDTO(resourcePolicy);
    }

    public static ResourcePolicyInfoDTO getAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
                                                                         String organization)
            throws APIManagementException {

        APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = provider.getLightweightAPIByUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(resourcePolicyId)) {
            String errorMessage = "Resource id should not be empty to update a resource policy.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        String policyContent = SequenceUtils.getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
        return APIMappingUtil.fromResourcePolicyStrToInfoDTO(policyContent);
    }

    /**
     * @param apiId            API UUID
     * @param organization     Tenant organization
     * @param resourcePolicyId Resource policy ID
     * @param xmlContent       Policy xml content
     * @return Updates resource policy
     * @throws APIManagementException when updating a resource policy fails
     */
    public static ResourcePolicyInfoDTO updateAPIResourcePoliciesByPolicyId(String apiId, String organization,
                                                                            String resourcePolicyId, String xmlContent)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        RestApiCommonUtil.checkAPIType(APIConstants.API_TYPE_SOAPTOREST, api.getType());
        if (StringUtils.isEmpty(resourcePolicyId)) {
            String errorMessage = "Resource id should not be empty to update a resource policy.";
            throw new APIManagementException(errorMessage, ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        boolean isValidSchema = PublisherUtils.validateXMLSchema(xmlContent);
        String updatedPolicyContent = "";
        if (isValidSchema) {
            List<SOAPToRestSequence> sequence = api.getSoapToRestSequences();
            for (SOAPToRestSequence soapToRestSequence : sequence) {
                if (soapToRestSequence.getUuid().equals(resourcePolicyId)) {
                    soapToRestSequence.setContent(xmlContent);
                    break;
                }
            }
            API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
            try {
                apiProvider.updateAPI(api, originalAPI);
            } catch (FaultGatewaysException e) {
                String errorMessage = "Error while updating the API with resource policies";
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
            }
            SequenceUtils.updateResourcePolicyFromRegistryResourceId(api.getId(), resourcePolicyId, xmlContent);
            updatedPolicyContent = SequenceUtils.getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
        }
        return APIMappingUtil.fromResourcePolicyStrToInfoDTO(updatedPolicyContent);
    }

    /**
     * @param query        Search query
     * @param organization Tenant organization
     * @throws APIManagementException when validating API existence fails
     */
    public static void validateAPI(String query, String organization) throws APIManagementException {

        if (StringUtils.isEmpty(query)) {
            throw new APIManagementException("The query should not be empty", ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        boolean isSearchArtifactExists;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (query.contains(":")) {
            String[] queryTokens = query.split(":");
            switch (queryTokens[0]) {
                case "name":
                    isSearchArtifactExists = apiProvider.isApiNameExist(queryTokens[1], organization) ||
                            apiProvider.isApiNameWithDifferentCaseExist(queryTokens[1], organization);
                    break;
                case "context":
                default: // API version validation.
                    isSearchArtifactExists = apiProvider.isContextExist(queryTokens[1], organization);
                    break;
            }

        } else { // consider the query as api name
            isSearchArtifactExists =
                    apiProvider.isApiNameExist(query, organization) ||
                            apiProvider.isApiNameWithDifferentCaseExist(query, organization);

        }
        if (!isSearchArtifactExists) {
            throw new APIManagementException(ExceptionCodes.RESOURCE_NOT_FOUND);
        }
    }

    public static void validateDocument(String apiId, String name, String organization) throws APIManagementException {

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(apiId)) {
            throw new APIManagementException("API Id and/ or document name should not be empty",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        boolean documentationExist = apiProvider.isDocumentationExist(apiId, name, organization);
        if (!documentationExist) {
            throw new APIManagementException(ExceptionCodes.RESOURCE_NOT_FOUND);
        }
    }

    public static String getEnvironmentProperties(String apiId, String envId, String organization)
            throws APIManagementException {

        validateEnvironment(organization, envId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // get properties
        EnvironmentPropertiesDTO properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }

    public static String updateEnvironmentProperties(String apiId, String envId, Map<String, String> requestBody,
                                                     String organization)
            throws APIManagementException {

        validateEnvironment(organization, envId);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        // adding properties
        EnvironmentPropertiesDTO properties = ApisApiCommonImpl.generateEnvironmentPropertiesDTO(requestBody);
        apiProvider.addEnvironmentSpecificAPIProperties(apiId, envId, properties);

        // get properties
        properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        return new Gson().toJson(properties);
    }

    public static void validateEnvironment(String organization, String envId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // if apiProvider.getEnvironment(organization, envId) return null, it will throw an exception
        apiProvider.getEnvironment(organization, envId);
    }

    private static Map<String, Object> validateWSDL(String url, InputStream fileInputStream, String fileName,
                                                    Boolean isServiceAPI)
            throws APIManagementException {

        handleInvalidParams(fileInputStream, fileName, url, null, isServiceAPI);
        WSDLValidationResponseDTO responseDTO;
        WSDLValidationResponse validationResponse = new WSDLValidationResponse();

        if (url != null) {
            try {
                URL wsdlUrl = new URL(url);
                validationResponse = APIMWSDLReader.validateWSDLUrl(wsdlUrl);
            } catch (MalformedURLException e) {
                throw new APIManagementException(ExceptionCodes.MALFORMED_URL);
            }
        } else if (fileInputStream != null && !isServiceAPI) {

            try {
                if (fileName.endsWith(".zip")) {
                    validationResponse = APIMWSDLReader.extractAndValidateWSDLArchive(fileInputStream);
                } else if (fileName.endsWith(".wsdl")) {
                    validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
                } else {
                    String errorMessage = "Unsupported extension type of file: " + fileName;
                    throw new APIManagementException(errorMessage, ExceptionCodes.UNSUPPORTED_WSDL_FILE_EXTENSION);
                }
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL from file:" + fileName;
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));

            }
        } else if (fileInputStream != null) {
            try {
                validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL definition input stream";
                throw new APIManagementException(errorMessage,
                        ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
            }
        }

        responseDTO = APIMappingUtil.fromWSDLValidationResponseToDTO(validationResponse);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);

        return response;
    }

    private static void handleInvalidParams(InputStream fileInputStream, String fileName, String url,
                                            String apiDefinition, Boolean isServiceAPI) throws APIManagementException {

        String msg = "";
        boolean isFileSpecified = (fileInputStream != null && fileName != null)
                || (fileInputStream != null && isServiceAPI);
        if (url == null && !isFileSpecified && apiDefinition == null) {
            msg = "One out of 'file' or 'url' or 'inline definition' should be specified";
        }

        boolean isMultipleSpecificationGiven = (isFileSpecified && url != null) || (isFileSpecified &&
                apiDefinition != null) || (apiDefinition != null && url != null);
        if (isMultipleSpecificationGiven) {
            msg = "Only one of 'file', 'url', and 'inline definition' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, msg));
        }
    }

    public static API importOpenAPIDefinition(APIDTO apiDTOFromProperties, ServiceEntry service, String organization,
                                              boolean isServiceAPI, Map<String, Object> validationResponseMap)
            throws APIManagementException {

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            String errorDescription = RestApiCommonUtil
                    .getErrorDescriptionFromErrorHandlers(validationResponse.getErrorItems());
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.OPENAPI_PARSE_EXCEPTION_WITH_CUSTOM_MESSAGE, errorDescription));
        }

        // Only HTTP or WEBHOOK type APIs should be allowed
        if (!(APIDTO.TypeEnum.HTTP.equals(apiDTOFromProperties.getType())
                || APIDTO.TypeEnum.WEBHOOK.equals(apiDTOFromProperties.getType()))) {
            throw new APIManagementException("The API's type is not supported when importing an OpenAPI definition",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }
        // Import the API and Definition
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // Add description from definition if it is not defined by user
        if (validationResponseDTO.getInfo().getDescription() != null
                && apiDTOFromProperties.getDescription() == null) {
            apiDTOFromProperties.setDescription(validationResponse.getInfo().getDescription());
        }
        if (isServiceAPI) {
            apiDTOFromProperties.setType(PublisherCommonUtils.getAPIType(service.getDefinitionType(), null));
        }
        API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDTOFromProperties, apiProvider,
                RestApiCommonUtil.getLoggedInUsername(), organization);
        boolean syncOperations = !apiDTOFromProperties.getOperations().isEmpty();
        return importAPIDefinition(apiToAdd, apiProvider, organization,
                service, validationResponse, isServiceAPI, syncOperations);
    }

    private static APIDTO getAPIByID(String apiId, APIProvider apiProvider, String organization)
            throws APIManagementException {

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        return APIMappingUtil.fromAPItoDTO(api, apiProvider);
    }

    private static APIDTO createAPIDTO(API existingAPI, String newVersion) {

        APIDTO apidto = new APIDTO();
        apidto.setName(existingAPI.getId().getApiName());
        apidto.setContext(existingAPI.getContextTemplate());
        apidto.setVersion(newVersion);
        return apidto;
    }

    private static String getSOAPOperation() {

        return "{\"/*\":{\"post\":{\"parameters\":[{\"schema\":{\"type\":\"string\"},\"description\":\"SOAP request.\","
                + "\"name\":\"SOAP Request\",\"required\":true,\"in\":\"body\"},"
                + "{\"description\":\"SOAPAction header for soap 1.1\",\"name\":\"SOAPAction\",\"type\":\"string\","
                + "\"required\":false,\"in\":\"header\"}],\"responses\":{\"200\":{\"description\":\"OK\"}}," +
                "\"security\":[{\"default\":[]}],\"consumes\":[\"text/xml\",\"application/soap+xml\"]}}}";
    }

    private static String getOperationPolicyRetrieveErrorMessage(String apiId, String operationPolicyId) {

        return "Couldn't retrieve an existing operation policy with ID: "
                + operationPolicyId + " for API " + apiId;
    }
}
