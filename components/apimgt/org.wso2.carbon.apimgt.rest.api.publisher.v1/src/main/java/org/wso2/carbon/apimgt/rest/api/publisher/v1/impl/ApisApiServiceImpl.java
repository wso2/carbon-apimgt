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
import com.amazonaws.services.lambda.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.language.TypeDefinition;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.schema.validation.SchemaValidationError;
import graphql.schema.validation.SchemaValidator;
import org.apache.axiom.util.base64.Base64Utils;
import org.apache.axiom.om.OMElement;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIExportUtil;
import org.wso2.carbon.apimgt.impl.utils.APIVersionStringComparator;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.impl.wsdl.SequenceGenerator;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.impl.ExportApiUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Map;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.CertificateRestApiUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.CertificateMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.ExternalStoreMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.MediationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.HeadMethod;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.wso2.carbon.utils.CarbonUtils;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);
    private static final String API_PRODUCT_TYPE = "APIPRODUCT";

    @Override
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query,
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
            String newSearchQuery = APIUtil.constructApisGetQuery(query);

            //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
            if (newSearchQuery.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=")) {
                newSearchQuery = newSearchQuery
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=", APIConstants.NAME_TYPE_PREFIX + "=");
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            boolean migrationMode = Boolean.getBoolean(RestApiConstants.MIGRATION_MODE);

            /*if (migrationMode) { // migration flow
                if (!StringUtils.isEmpty(targetTenantDomain)) {
                    tenantDomain = targetTenantDomain;
                }
                RestApiUtil.handleMigrationSpecificPermissionViolations(tenantDomain, username);
            }*/

            Map<String, Object> result = apiProvider.searchPaginatedAPIs(newSearchQuery, tenantDomain,
                    offset, limit, false);
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
    public Response apisPost(APIDTO body, String oasVersion, MessageContext messageContext) {
        URI createdApiUri;
        APIDTO createdApiDTO;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            boolean isWSAPI = APIDTO.TypeEnum.WS == body.getType();

            // validate web socket api endpoint configurations
            if (isWSAPI && !RestApiPublisherUtils.isValidWSAPI(body)) {
                RestApiUtil.handleBadRequest("Endpoint URLs should be valid web socket URLs", log);
            }

            // AWS Lambda: secret key encryption while creating the API
            if (body.getEndpointConfig() != null) {
                LinkedHashMap endpointConfig = (LinkedHashMap) body.getEndpointConfig();
                if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
                    String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                    if (!StringUtils.isEmpty(secretKey)) {
                        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                        String encryptedSecretKey = cryptoUtil.encryptAndBase64Encode(secretKey.getBytes());
                        endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                        body.setEndpointConfig(endpointConfig);
                    }
                }
            }

            API apiToAdd = prepareToCreateAPIByDTO(body);
            validateScopes(apiToAdd);
            //validate API categories
            List<APICategory> apiCategories = apiToAdd.getApiCategories();
            if (apiCategories != null && apiCategories.size() >0) {
                if (!APIUtil.validateAPICategories(apiCategories, RestApiUtil.getLoggedInUserTenantDomain())) {
                    RestApiUtil.handleBadRequest("Invalid API Category name(s) defined", log);
                }
            }
            //adding the api
            apiProvider.addAPI(apiToAdd);

            if (!isWSAPI) {
                APIDefinition oasParser;
                if (RestApiConstants.OAS_VERSION_2.equalsIgnoreCase(oasVersion)) {
                    oasParser = new OAS2Parser();
                } else {
                    oasParser = new OAS3Parser();
                }
                SwaggerData swaggerData = new SwaggerData(apiToAdd);
                String apiDefinition = oasParser.generateAPIDefinition(swaggerData);
                apiProvider.saveSwaggerDefinition(apiToAdd, apiDefinition);
            }

            APIIdentifier createdApiId = apiToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
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

    /**
     * Prepares the API Model object to be created using the DTO object
     *
     * @param body APIDTO of the API
     * @return API object to be created
     * @throws APIManagementException Error while creating the API
     */
    private API prepareToCreateAPIByDTO(APIDTO body) throws APIManagementException {
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String username = RestApiUtil.getLoggedInUsername();
        List<String> apiSecuritySchemes = body.getSecurityScheme();//todo check list vs string
        if (!apiProvider.isClientCertificateBasedAuthenticationConfigured() && apiSecuritySchemes != null) {
            for (String apiSecurityScheme : apiSecuritySchemes) {
                if (apiSecurityScheme.contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                    RestApiUtil.handleBadRequest("Mutual SSL Based authentication is not supported in this server", log);
                }
            }
        }
        if (body.getAccessControlRoles() != null) {
            String errorMessage = RestApiPublisherUtils.validateUserRoles(body.getAccessControlRoles());

            if (!errorMessage.isEmpty()) {
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        }
        if (body.getAdditionalProperties() != null) {
            String errorMessage = RestApiPublisherUtils
                    .validateAdditionalProperties(body.getAdditionalProperties());
            if (!errorMessage.isEmpty()) {
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        }
        if (body.getContext() == null) {
            RestApiUtil.handleBadRequest("Parameter: \"context\" cannot be null", log);
        } else if (body.getContext().endsWith("/")) {
            RestApiUtil.handleBadRequest("Context cannot end with '/' character", log);
        }
        if (apiProvider.isApiNameWithDifferentCaseExist(body.getName())) {
            RestApiUtil.handleBadRequest("Error occurred while adding API. API with name " + body.getName()
                    + " already exists.", log);
        }
        if (body.getAuthorizationHeader() == null) {
            body.setAuthorizationHeader(APIUtil
                    .getOAuthConfigurationFromAPIMConfig(APIConstants.AUTHORIZATION_HEADER));
        }
        if (body.getAuthorizationHeader() == null) {
            body.setAuthorizationHeader(APIConstants.AUTHORIZATION_HEADER_DEFAULT);
        }

        if (body.getVisibility() == APIDTO.VisibilityEnum.RESTRICTED && body.getVisibleRoles().isEmpty()) {
            RestApiUtil.handleBadRequest("Valid roles should be added under 'visibleRoles' to restrict " +
                    "the visibility", log);
        }
        if (body.getVisibleRoles() != null) {
            String errorMessage = RestApiPublisherUtils.validateRoles(body.getVisibleRoles());
            if (!errorMessage.isEmpty()) {
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        }

        //Get all existing versions of  api been adding
        List<String> apiVersions = apiProvider.getApiVersionsMatchingApiName(body.getName(), username);
        if (apiVersions.size() > 0) {
            //If any previous version exists
            for (String version : apiVersions) {
                if (version.equalsIgnoreCase(body.getVersion())) {
                    //If version already exists
                    if (apiProvider.isDuplicateContextTemplate(body.getContext())) {
                        RestApiUtil.handleResourceAlreadyExistsError("Error occurred while " +
                                "adding the API. A duplicate API already exists for "
                                + body.getName() + "-" + body.getVersion(), log);
                    } else {
                        RestApiUtil.handleBadRequest("Error occurred while adding API. API with name " +
                                body.getName() + " already exists with different " +
                                "context", log);
                    }
                }
            }
        } else {
            //If no any previous version exists
            if (apiProvider.isDuplicateContextTemplate(body.getContext())) {
                RestApiUtil.handleBadRequest("Error occurred while adding the API. A duplicate API context " +
                        "already exists for " + body.getContext(), log);
            }
        }

        //Check if the user has admin permission before applying a different provider than the current user
        String provider = body.getProvider();
        if (!StringUtils.isBlank(provider) && !provider.equals(username)) {
            if (!APIUtil.hasPermission(username, APIConstants.Permissions.APIM_ADMIN)) {
                if (log.isDebugEnabled()) {
                    log.debug("User " + username + " does not have admin permission ("
                            + APIConstants.Permissions.APIM_ADMIN + ") hence provider (" +
                            provider + ") overridden with current user (" + username + ")");
                }
                provider = username;
            } else {
                if (!APIUtil.isUserExist(provider)) {
                    RestApiUtil.handleBadRequest("Specified provider " + provider + " not exist.", log);
                }
            }
        } else {
            //Set username in case provider is null or empty
            provider = username;
        }

        List<String> tiersFromDTO = body.getPolicies();

        //check whether the added API's tiers are all valid
        Set<Tier> definedTiers = apiProvider.getTiers();
        List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
        if (invalidTiers.size() > 0) {
            RestApiUtil.handleBadRequest(
                    "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
        }
        APIPolicy apiPolicy = apiProvider.getAPIPolicy(username, body.getApiThrottlingPolicy());
        if (apiPolicy == null && body.getApiThrottlingPolicy() != null) {
            RestApiUtil.handleBadRequest(
                    "Specified policy " + body.getApiThrottlingPolicy() + " is invalid", log);
        }

        API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, provider);
        //Overriding some properties:
        //only allow CREATED as the stating state for the new api if not status is PROTOTYPED
        if (!APIConstants.PROTOTYPED.equals(apiToAdd.getStatus())) {
            apiToAdd.setStatus(APIConstants.CREATED);
        }
        //we are setting the api owner as the logged in user until we support checking admin privileges and assigning
        //  the owner as a different user
        apiToAdd.setApiOwner(provider);

        //attach micro-geteway labels
        assignLabelsToDTO(body, apiToAdd);

        return apiToAdd;
    }

    @Override
    public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) {
        APIDTO apiToReturn = getAPIByID(apiId);
        return Response.ok().entity(apiToReturn).build();
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
    public Response apisApiIdGraphqlSchemaGet(String apiId, String accept, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
    public Response apisApiIdGraphqlSchemaPut(String apiId, String schemaDefinition, String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId,
                    tenantDomain);

            API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            List<APIOperationsDTO> operationArray = extractGraphQLOperationList(schemaDefinition);
            Set<URITemplate> uriTemplates = APIMappingUtil.getURITemplates(originalAPI, operationArray);
            originalAPI.setUriTemplates(uriTemplates);

            apiProvider.saveGraphqlSchemaDefinition(originalAPI, schemaDefinition);
            apiProvider.updateAPI(originalAPI);
            String schema = apiProvider.getGraphqlSchema(apiIdentifier);
            return Response.ok().entity(schema).build();
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
    public Response apisApiIdPut(String apiId, APIDTO body, String ifMatch, MessageContext messageContext) {
        APIDTO updatedApiDTO;
        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange().get(RestApiConstants.USER_REST_API_SCOPES);
        // Validate if the USER_REST_API_SCOPES is not set in WebAppAuthenticator when scopes are validated
        if (tokenScopes == null) {
            RestApiUtil.handleInternalServerError("Error occurred while updating the  API " + apiId +
                    " as the token information hasn't been correctly set internally", log);
            return null;
        }
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = originalAPI.getId();
            boolean isWSAPI = originalAPI.getType() != null
                    && APIConstants.APITransportType.WS.toString().equals(originalAPI.getType());
            boolean isGraphql = originalAPI.getType() != null
                    && APIConstants.APITransportType.GRAPHQL.toString().equals(originalAPI.getType());

            org.wso2.carbon.apimgt.rest.api.util.annotations.Scope[] apiDtoClassAnnotatedScopes =
                    APIDTO.class.getAnnotationsByType(org.wso2.carbon.apimgt.rest.api.util.annotations.Scope.class);
            boolean hasClassLevelScope = checkClassScopeAnnotation(apiDtoClassAnnotatedScopes, tokenScopes);

            // AWS Lambda: secret key encryption while updating the API
            if (body.getEndpointConfig() != null) {
                LinkedHashMap endpointConfig = (LinkedHashMap) body.getEndpointConfig();
                if (endpointConfig.containsKey(APIConstants.AMZN_SECRET_KEY)) {
                    String secretKey = (String) endpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                    if (!StringUtils.isEmpty(secretKey)) {
                        if (!APIConstants.AWS_SECRET_KEY.equals(secretKey)) {
                            CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                            String encryptedSecretKey = cryptoUtil.encryptAndBase64Encode(secretKey.getBytes());
                            endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                            body.setEndpointConfig(endpointConfig);
                        } else {
                            JSONParser jsonParser = new JSONParser();
                            JSONObject originalEndpointConfig = (JSONObject)
                                    jsonParser.parse(originalAPI.getEndpointConfig());
                            String encryptedSecretKey = (String) originalEndpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                            endpointConfig.put(APIConstants.AMZN_SECRET_KEY, encryptedSecretKey);
                            body.setEndpointConfig(endpointConfig);
                        }
                    }
                }
            }

            if (!hasClassLevelScope) {
                // Validate per-field scopes
                body = getFieldOverriddenAPIDTO(body, originalAPI, tokenScopes);
            }
            //Overriding some properties:
            body.setName(apiIdentifier.getApiName());
            body.setVersion(apiIdentifier.getVersion());
            body.setProvider(apiIdentifier.getProviderName());
            body.setContext(originalAPI.getContextTemplate());
            body.setLifeCycleStatus(originalAPI.getStatus());
            body.setType(APIDTO.TypeEnum.fromValue(originalAPI.getType()));

            List<APIResource> removedProductResources = getRemovedProductResources(body, originalAPI);

            if (!removedProductResources.isEmpty()) {
                RestApiUtil.handleConflict("Cannot remove following resource paths " +
                        removedProductResources.toString() + " because they are used by one or more API Products", log);
            }

            // Validate API Security
            List<String> apiSecurity = body.getSecurityScheme();
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured() && apiSecurity != null && apiSecurity
                    .contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                RestApiUtil.handleBadRequest("Mutual SSL based authentication is not supported in this server.", log);
            }
            //validation for tiers
            List<String> tiersFromDTO = body.getPolicies();
            String originalStatus = originalAPI.getStatus();
            if (apiSecurity.contains(APIConstants.DEFAULT_API_SECURITY_OAUTH2) ||
                    apiSecurity.contains(APIConstants.API_SECURITY_API_KEY)) {
                if (tiersFromDTO == null || tiersFromDTO.isEmpty() &&
                        !(APIConstants.CREATED.equals(originalStatus) ||
                                APIConstants.PROTOTYPED.equals(originalStatus))) {
                    RestApiUtil.handleBadRequest("A tier should be defined " +
                            "if the API is not in CREATED or PROTOTYPED state", log);
                }
            }

            if (tiersFromDTO != null && !tiersFromDTO.isEmpty()) {
                //check whether the added API's tiers are all valid
                Set<Tier> definedTiers = apiProvider.getTiers();
                List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
                if (invalidTiers.size() > 0) {
                    RestApiUtil.handleBadRequest(
                            "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
                }
            }
            if (body.getAccessControlRoles() != null) {
                String errorMessage = RestApiPublisherUtils.validateUserRoles(body.getAccessControlRoles());
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }
            if (body.getVisibleRoles() != null) {
                String errorMessage = RestApiPublisherUtils.validateRoles(body.getVisibleRoles());
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }
            if (body.getAdditionalProperties() != null) {
                String errorMessage = RestApiPublisherUtils.validateAdditionalProperties(body.getAdditionalProperties());
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            }
            // Validate if resources are empty
            if (!isWSAPI && (body.getOperations() == null || body.getOperations().isEmpty())) {
                RestApiUtil.handleBadRequest(ExceptionCodes.NO_RESOURCES_FOUND, log);
            }
            API apiToUpdate = APIMappingUtil.fromDTOtoAPI(body, apiIdentifier.getProviderName());
            if (APIConstants.PUBLIC_STORE_VISIBILITY.equals(apiToUpdate.getVisibility())) {
                apiToUpdate.setVisibleRoles(StringUtils.EMPTY);
            }
            apiToUpdate.setUUID(originalAPI.getUUID());
            validateScopes(apiToUpdate);
            apiToUpdate.setThumbnailUrl(originalAPI.getThumbnailUrl());

            //attach micro-geteway labels
            assignLabelsToDTO(body, apiToUpdate);

            //preserve monetization status in the update flow
            apiProvider.configureMonetizationInAPIArtifact(originalAPI);

            if (!isWSAPI) {
                String oldDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);
                APIDefinition apiDefinition = OASParserUtil.getOASParser(oldDefinition);
                SwaggerData swaggerData = new SwaggerData(apiToUpdate);
                String newDefinition = apiDefinition.generateAPIDefinition(swaggerData, oldDefinition);
                apiProvider.saveSwaggerDefinition(apiToUpdate, newDefinition);
                if (!isGraphql) {
                    apiToUpdate.setUriTemplates(apiDefinition.getURITemplates(newDefinition));
                }
            }
            apiToUpdate.setWsdlUrl(body.getWsdlUrl());

            //validate API categories
            List<APICategory> apiCategories = apiToUpdate.getApiCategories();
            if (apiCategories != null && apiCategories.size() >0) {
                if (!APIUtil.validateAPICategories(apiCategories, RestApiUtil.getLoggedInUserTenantDomain())) {
                    RestApiUtil.handleBadRequest("Invalid API Category name(s) defined", log);
                }
            }

            apiProvider.manageAPI(apiToUpdate);

            API updatedApi = apiProvider.getAPI(apiIdentifier);
            updatedApiDTO = APIMappingUtil.fromAPItoDTO(updatedApi);
            return Response.ok().entity(updatedApiDTO).build();
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

    // AWS Lambda: rest api operation to get ARNs
    @Override
    public Response apisApiIdAmznResourceNamesGet(String apiId, MessageContext messageContext) {
        JSONObject arns = new JSONObject();
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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

    @Override
    public Response apisApiIdAuditapiGet(String apiId, String accept, MessageContext messageContext) {
        boolean isDebugEnabled = log.isDebugEnabled();
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = api.getId();
            String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);
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
                                new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
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
                        String decodedReport = new String(Base64Utils.decode(report), "UTF-8");
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

    private void updateAuditApi(String apiDefinition, String apiToken, String auditUuid, String baseUrl,
                                boolean isDebugEnabled)
            throws IOException, APIManagementException {
        // Set the property to be attached in the body of the request
        // Attach API Definition to property called specfile to be sent in the request
        JSONObject jsonBody = new JSONObject();
        jsonBody.put("specfile", Base64Utils.encode(apiDefinition.getBytes("UTF-8")));
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
        writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"), true);
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
                    new InputStreamReader(httpConn.getInputStream(), "UTF-8"));
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
            throw new APIManagementException(
                    "Error while retrieving data for the API Security Audit Report. Found http status: " +
                            httpConn.getResponseCode() + " - " + httpConn.getResponseMessage());
        }
        return auditUuid;
    }

    /**
     * Finds resources that have been removed in the updated API, that are currently reused by API Products.
     *
     * @param updatedDTO  Updated API
     * @param existingAPI Existing API
     * @return List of removed resources that are reused among API Products
     */
    private List<APIResource> getRemovedProductResources(APIDTO updatedDTO, API existingAPI) {
        List<APIOperationsDTO> updatedOperations = updatedDTO.getOperations();
        Set<URITemplate> existingUriTemplates = existingAPI.getUriTemplates();
        List<APIResource> removedReusedResources = new ArrayList<>();

        for (URITemplate existingUriTemplate : existingUriTemplates) {

            // If existing URITemplate is used by any API Products
            if (!existingUriTemplate.retrieveUsedByProducts().isEmpty()) {
                String existingVerb = existingUriTemplate.getHTTPVerb();
                String existingPath = existingUriTemplate.getUriTemplate();
                boolean isReusedResourceRemoved = true;

                for (APIOperationsDTO updatedOperation : updatedOperations) {
                    String updatedVerb = updatedOperation.getVerb();
                    String updatedPath = updatedOperation.getTarget();

                    //Check if existing reused resource is among updated resources
                    if (existingVerb.equalsIgnoreCase(updatedVerb) &&
                            existingPath.equalsIgnoreCase(updatedPath)) {
                        isReusedResourceRemoved = false;
                        break;
                    }
                }

                // Existing reused resource is not among updated resources
                if (isReusedResourceRemoved) {
                    APIResource removedResource = new APIResource(existingVerb, existingPath);
                    removedReusedResources.add(removedResource);
                }
            }
        }

        return removedReusedResources;
    }

    /**
     * Check whether the token has APIDTO class level Scope annotation
     *
     * @return true if the token has APIDTO class level Scope annotation
     */
    private boolean checkClassScopeAnnotation(org.wso2.carbon.apimgt.rest.api.util.annotations.Scope[] apiDtoClassAnnotatedScopes, String[] tokenScopes) {

        for (org.wso2.carbon.apimgt.rest.api.util.annotations.Scope classAnnotation : apiDtoClassAnnotatedScopes) {
            for (String tokenScope : tokenScopes) {
                if (classAnnotation.name().equals(tokenScope)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the API DTO object in which the API field values are overridden with the user passed new values
     *
     * @throws APIManagementException
     */
    private APIDTO getFieldOverriddenAPIDTO(APIDTO apidto, API originalAPI,
                                            String[] tokenScopes) throws APIManagementException {

        APIDTO originalApiDTO;
        APIDTO updatedAPIDTO;

        try {
            originalApiDTO = APIMappingUtil.fromAPItoDTO(originalAPI);

            Field[] fields = APIDTO.class.getDeclaredFields();
            ObjectMapper mapper = new ObjectMapper();
            String newApiDtoJsonString = mapper.writeValueAsString(apidto);
            JSONParser parser = new JSONParser();
            JSONObject newApiDtoJson = (JSONObject) parser.parse(newApiDtoJsonString);

            String originalApiDtoJsonString = mapper.writeValueAsString(originalApiDTO);
            JSONObject originalApiDtoJson = (JSONObject) parser.parse(originalApiDtoJsonString);

            for (Field field : fields) {
                org.wso2.carbon.apimgt.rest.api.util.annotations.Scope[] fieldAnnotatedScopes =
                        field.getAnnotationsByType(org.wso2.carbon.apimgt.rest.api.util.annotations.Scope.class);
                String originalElementValue = mapper.writeValueAsString(originalApiDtoJson.get(field.getName()));
                String newElementValue = mapper.writeValueAsString(newApiDtoJson.get(field.getName()));

                if (!StringUtils.equals(originalElementValue, newElementValue)) {
                    originalApiDtoJson = overrideDTOValues(originalApiDtoJson, newApiDtoJson, field, tokenScopes,
                            fieldAnnotatedScopes);
                }
            }

            updatedAPIDTO = mapper.readValue(originalApiDtoJson.toJSONString(), APIDTO.class);

        } catch (IOException | ParseException e) {
            String msg = "Error while processing API DTO json strings";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        return updatedAPIDTO;
    }

    /**
     * Override the API DTO field values with the user passed new values considering the field-wise scopes defined as
     * allowed to update in REST API definition yaml
     */
    private JSONObject overrideDTOValues(JSONObject originalApiDtoJson, JSONObject newApiDtoJson, Field field, String[]
            tokenScopes, org.wso2.carbon.apimgt.rest.api.util.annotations.Scope[] fieldAnnotatedScopes) throws
            APIManagementException {
        for (String tokenScope : tokenScopes) {
            for (org.wso2.carbon.apimgt.rest.api.util.annotations.Scope scopeAnt : fieldAnnotatedScopes) {
                if (scopeAnt.name().equals(tokenScope)) {
                    // do the overriding
                    originalApiDtoJson.put(field.getName(), newApiDtoJson.get(field.getName()));
                    return originalApiDtoJson;
                }
            }
        }
        throw new APIManagementException("User is not authorized to update one or more API fields. None of the " +
                "required scopes found in user token to update the field. So the request will be failed.");
    }

    @Override
    public Response apisApiIdClientCertificatesAliasContentGet(String apiId, String alias,
                                                               MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        String certFileName = alias + ".crt";
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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
    public Response apisApiIdClientCertificatesAliasDelete(String alias, String apiId,
                                                           MessageContext messageContext) {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, RestApiUtil.getLoggedInUserTenantDomain());
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    api.getId());
            int responseCode = apiProvider
                    .deleteClientCertificate(RestApiUtil.getLoggedInUsername(), clientCertificateDTO.getApiIdentifier(),
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
    public Response apisApiIdClientCertificatesAliasGet(String alias, String apiId,
                                                        MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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
    public Response apisApiIdClientCertificatesAliasPut(String alias, String apiId,
                                                        InputStream certificateInputStream, Attachment certificateDetail, String tier,
                                                        MessageContext messageContext) {
        try {
            ContentDisposition contentDisposition;
            String fileName;
            String base64EncodedCert = null;
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, RestApiUtil.getLoggedInUserTenantDomain());
            String userName = RestApiUtil.getLoggedInUsername();
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
    public Response apisApiIdClientCertificatesGet(String apiId, Integer limit, Integer offset, String alias,
                                                   MessageContext messageContext) {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<ClientCertificateDTO> certificates = new ArrayList<>();
        String userName = RestApiUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "apiId", apiId);

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                RestApiUtil.handleBadRequest(
                        "The client certificate based authentication is not configured for this " + "server", log);
            }
            int totalCount = apiProvider.getClientCertificateCount(tenantId);
            if (totalCount > 0) {
                APIIdentifier apiIdentifier = null;
                if (StringUtils.isNotEmpty(apiId)) {
                    API api = apiProvider.getAPIbyUUID(apiId, RestApiUtil.getLoggedInUserTenantDomain());
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
    public Response apisApiIdClientCertificatesPost(InputStream certificateInputStream,
                                                    Attachment certificateDetail, String alias, String apiId, String tier, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
            if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(apiId)) {
                RestApiUtil.handleBadRequest("The alias and/ or apiId should not be empty", log);
            }
            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest(
                        "Certificate addition failed. Proper Certificate file should be provided", log);
            }
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured()) {
                RestApiUtil.handleBadRequest(
                        "The client certificate based authentication is not configured for this " + "server", log);
            }
            API api = apiProvider.getAPIbyUUID(apiId, RestApiUtil.getLoggedInUserTenantDomain());
            String userName = RestApiUtil.getLoggedInUsername();
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
    public Response apisApiIdDelete(String apiId, String ifMatch, MessageContext messageContext) {

        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);

            //check if the API has subscriptions
            //Todo : need to optimize this check. This method seems too costly to check if subscription exists
            List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiIdentifier);
            if (apiUsages != null && apiUsages.size() > 0) {
                RestApiUtil.handleConflict("Cannot remove the API " + apiId + " as active subscriptions exist", log);
            }

            API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);

            List<APIResource> usedProductResources = getUsedProductResources(existingAPI);

            if (!usedProductResources.isEmpty()) {
                RestApiUtil.handleConflict("Cannot remove the API because following resource paths " +
                        usedProductResources.toString() + " are used by one or more API Products", log);
            }

            //deletes the API
            apiProvider.deleteAPI(apiIdentifier, apiId);
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
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId,
                                                           String ifNoneMatch, MessageContext messageContext) {
        Documentation documentation;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

            //gets the content depending on the type of the document
            if (documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                String resource = documentation.getFilePath();
                Map<String, Object> docResourceMap = APIUtil.getDocument(username, resource, tenantDomain);
                Object fileDataStream = docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
                Object contentType = docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE);
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = docResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_NAME).toString();
                return Response.ok(fileDataStream)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .build();
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE) || documentation.getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                String content = apiProvider.getDocumentationContent(apiIdentifier, documentation.getName());
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE)
                        .build();
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.URL)) {
                String sourceUrl = documentation.getSourceUrl();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
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
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId,
                                                            InputStream inputStream, Attachment fileDetail, String inlineContent, String ifMatch,
                                                            MessageContext messageContext) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API api = APIMappingUtil.getAPIInfoFromUUID(apiId, tenantDomain);
            if (inputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(documentId, tenantDomain);
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
                apiProvider.addDocumentationContent(api, documentation.getName(), inlineContent);
            } else {
                RestApiUtil.handleBadRequest("Either 'file' or 'inlineContent' should be specified", log);
            }

            //retrieving the updated doc and the URI
            Documentation updatedDoc = apiProvider.getDocumentation(documentId, tenantDomain);
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
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch,
                                                       MessageContext messageContext) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }
            apiProvider.removeDocumentation(apiIdentifier, documentId);
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
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String ifNoneMatch,
                                                    MessageContext messageContext) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if API is not accessible
            APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
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
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body,
                                                    String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            String sourceUrl = body.getSourceUrl();
            Documentation oldDocument = apiProvider.getDocumentation(documentId, tenantDomain);

            //validation checks for existence of the document
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
                    (org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                return null;
            }

            //overriding some properties
            body.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            newDocumentation.setFilePath(oldDocument.getFilePath());
            apiProvider.updateDocumentation(apiIdentifier, newDocumentation);

            //retrieve the updated documentation
            newDocumentation = apiProvider.getDocumentation(documentId, tenantDomain);
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
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch,
                                          MessageContext messageContext) {
        // do some magic!
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiIdentifier);
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
    public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            String documentName = body.getName();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && org.apache.commons.lang3.StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
            }
            String sourceUrl = body.getSourceUrl();
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
            }
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            if (apiProvider.isDocumentationExist(apiIdentifier, documentName)) {
                String errorMessage = "Requested document '" + documentName + "' already exists";
                RestApiUtil.handleResourceAlreadyExistsError(errorMessage, log);
            }
            apiProvider.addDocumentation(apiIdentifier, documentation);

            //retrieve the newly added document
            String newDocumentId = documentation.getId();
            documentation = apiProvider.getDocumentation(newDocumentId, tenantDomain);
            DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                    .replace(RestApiConstants.APIID_PARAM, apiId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, newDocumentId);
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
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

        try {
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        APIIdentifier apiIdentifier = originalAPI.getId();
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);
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
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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
    public Response apisApiIdLifecycleHistoryGet(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
    public Response apisApiIdLifecycleStateGet(String apiId, String ifNoneMatch, MessageContext messageContext) {
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
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            Map<String, Object> apiLCData = apiProvider.getAPILifeCycleData(apiIdentifier);
            if (apiLCData == null) {
                String errorMessage = "Error while getting lifecycle state for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, log);
            }

            boolean apiOlderVersionExist = false;
            // check whether other versions of the current API exists
            APIDTO currentAPI = getAPIByID(apiId);
            APIVersionStringComparator comparator = new APIVersionStringComparator();
            Set<String> versions = apiProvider.getAPIVersions(
                    APIUtil.replaceEmailDomain(currentAPI.getProvider()), currentAPI.getName());

            for (String tempVersion : versions) {
                if (comparator.compare(tempVersion, currentAPI.getVersion()) < 0) {
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
    public Response apisApiIdLifecycleStatePendingTasksDelete(String apiId, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
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
    public Response apisApiIdMediationPoliciesGet(String apiId, Integer limit, Integer offset, String query,
            String ifNoneMatch, MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        APIIdentifier apiIdentifier;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            //Getting list of API specific mediation policies
            List<Mediation> mediationList =
                    apiProvider.getAllApiSpecificMediationPolicies(apiIdentifier);
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
    public Response apisApiIdMediationPoliciesMediationPolicyIdDelete(String apiId, String mediationPolicyId,
            String ifMatch, MessageContext messageContext) {
        APIIdentifier apiIdentifier;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            API api = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Getting specified mediation policy
            Mediation mediation =
                    apiProvider.getApiSpecificMediationPolicy(apiIdentifier, apiResourcePath, mediationPolicyId);
            if (mediation != null) {
                if (isAPIModified(api, mediation)) {
                    apiProvider.updateAPI(api);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_POLICY, mediationPolicyId, log);
            }
            boolean deletionStatus =
                    apiProvider.deleteApiSpecificMediationPolicy(apiIdentifier, apiResourcePath, mediationPolicyId);
            if (deletionStatus) {
                return Response.ok().build();
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
    public Response apisApiIdMediationPoliciesMediationPolicyIdGet(String apiId, String mediationPolicyId,
            String ifNoneMatch, MessageContext messageContext) {
        APIIdentifier apiIdentifier;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out of apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Getting specified mediation policy
            Mediation mediation =
                    apiProvider.getApiSpecificMediationPolicy(apiIdentifier, apiResourcePath, mediationPolicyId);
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
    public Response apisApiIdMediationPoliciesMediationPolicyIdContentPut(String type, String apiId, String mediationPolicyId,
                                                                   InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch, MessageContext messageContext) {

        InputStream contentStream = null;
        APIIdentifier apiIdentifier;
        Mediation updatedMediation;
        String resourcePath = "";
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out of apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Getting resource correspond to the given uuid
            Resource mediationResource = apiProvider
                    .getApiSpecificMediationResourceFromUuid(apiIdentifier, mediationPolicyId, apiResourcePath);
            if (mediationResource != null) {
                ResourceFile contentFile = new ResourceFile(fileInputStream, fileDetail.getContentType().toString());

                //Getting path to the existing resource
                resourcePath = mediationResource.getPath();

                //Updating the existing mediation policy
                String updatedPolicyUrl = apiProvider.addResourceFile(apiIdentifier, resourcePath, contentFile);
                if (StringUtils.isNotBlank(updatedPolicyUrl)) {
                    String uuid = apiProvider.getCreatedResourceUuid(resourcePath);
                    //Getting the updated mediation policy
                    updatedMediation = apiProvider.getApiSpecificMediationPolicy(apiIdentifier, apiResourcePath, uuid);
                    MediationDTO updatedMediationDTO =
                            MediationMappingUtil.fromMediationToDTO(updatedMediation);
                    URI uploadedMediationUri = new URI(updatedPolicyUrl);
                    return Response.ok(uploadedMediationUri).entity(updatedMediationDTO).build();
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
                    "mediation policy " + resourcePath;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(contentStream);
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
    public Response apisApiIdMediationPoliciesMediationPolicyIdContentGet(String apiId, String mediationPolicyId, String ifNoneMatch, MessageContext messageContext) {

        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out of apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Getting resource correspond to the given uuid
            Resource mediationResource = apiProvider
                    .getApiSpecificMediationResourceFromUuid(apiIdentifier, mediationPolicyId, apiResourcePath);
            if (mediationResource == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MEDIATION_POLICY, mediationPolicyId, log);
                return null;
            }

            String resource = mediationResource.getPath();
            resource = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + resource;
            Map<String, Object> mediationPolicyResourceMap = APIUtil.getDocument(username, resource, tenantDomain);
            Object fileDataStream = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
            Object contentType = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE);
            contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
            String name = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_NAME).toString();
            return Response.ok(fileDataStream)
                    .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                    .build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + mediationPolicyId + " of API " + apiId, e, log);
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
    public Response apisApiIdMediationPoliciesPost(String type, String apiId, InputStream fileInputStream, Attachment fileDetail, String inlineContent, String ifMatch, MessageContext messageContext) {

        String fileName = "";
        String mediationPolicyUrl = "";
        String mediationResourcePath = "";
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API api = APIMappingUtil.getAPIInfoFromUUID(apiId, tenantDomain);
            if (fileInputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            if (!StringUtils.isEmpty(type)) {
                type.toLowerCase();
            } else {
                type = "in";
            }

            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting registry Api base path out of apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));

            if (fileInputStream != null) {
                fileName = fileDetail.getDataHandler().getName();
                //Constructing mediation resource path
                mediationResourcePath = apiResourcePath + RegistryConstants.PATH_SEPARATOR +
                        type + RegistryConstants.PATH_SEPARATOR;
                String fileContentType = URLConnection.guessContentTypeFromName(fileName);

                if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                    fileContentType = fileDetail.getContentType().toString();
                }

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                IOUtils.copy(fileInputStream, outputStream);
                byte[] sequenceBytes = outputStream.toByteArray();
                InputStream inSequenceStream = new ByteArrayInputStream(sequenceBytes);
                OMElement seqElement = APIUtil.buildOMElement(new ByteArrayInputStream(sequenceBytes));
                String localName = seqElement.getLocalName();
                fileName = seqElement.getAttributeValue(new QName("name"));
                //Constructing mediation resource path
                mediationResourcePath = mediationResourcePath + fileName;
                checkMediationPolicy(apiProvider, mediationResourcePath);
                if (APIConstants.MEDIATION_SEQUENCE_ELEM.equals(localName)) {
                    ResourceFile contentFile = new ResourceFile(inSequenceStream, fileContentType);
                    //Adding api specific mediation policy
                    mediationPolicyUrl = apiProvider.addResourceFile(apiIdentifier, mediationResourcePath, contentFile);
                } else {
                    throw new APIManagementException("Sequence is malformed");
                }
            }
            if (inlineContent != null) {
                //Extracting the file name specified in the config
                fileName = this.getMediationNameFromConfig(inlineContent);
                //Constructing mediation resource path
                mediationResourcePath = apiResourcePath + RegistryConstants.PATH_SEPARATOR + type +
                        RegistryConstants.PATH_SEPARATOR + fileName;
                checkMediationPolicy(apiProvider,mediationResourcePath);
                InputStream contentStream = new ByteArrayInputStream(inlineContent.getBytes(StandardCharsets.UTF_8));
                String contentType = URLConnection.guessContentTypeFromName(fileName);
                ResourceFile contentFile = new ResourceFile(contentStream, contentType);
                //Adding api specific mediation policy
                mediationPolicyUrl = apiProvider.addResourceFile(apiIdentifier, mediationResourcePath, contentFile);
            }

            if (StringUtils.isNotBlank(mediationPolicyUrl)) {
                //Getting the uuid of created mediation policy
                String uuid = apiProvider.getCreatedResourceUuid(mediationResourcePath);
                //Getting created Api specific mediation policy
                Mediation createdMediation =
                        apiProvider.getApiSpecificMediationPolicy(apiIdentifier, apiResourcePath, uuid);
                MediationDTO createdPolicy =
                        MediationMappingUtil.fromMediationToDTO(createdMediation);
                URI uploadedMediationUri = new URI(mediationPolicyUrl);
                return Response.created(uploadedMediationUri).entity(createdPolicy).build();
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
                String errorMessage = "Error while adding the mediation policy : " + fileName +
                        "of API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while getting location header for created " +
                    "mediation policy " + fileName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while adding mediation policy", e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
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
    public Response apisApiIdMonetizationGet(String apiId, MessageContext messageContext) {

        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null when retrieving monetized plans.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
    public Response apisApiIdMonetizePost(String apiId, APIMonetizationInfoDTO body, MessageContext messageContext) {
        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null when configuring monetization.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        API api = null;
        List<String> externalStoreIdList = Arrays.asList(externalStoreIds.split("\\s*,\\s*"));
        try {
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
    public Response apisApiIdResourcePoliciesGet(String apiId, String sequenceType, String resourcePath,
            String verb, String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            boolean isSoapToRESTApi = SOAPOperationBindingUtils
                    .isSOAPToRESTApi(apiIdentifier.getApiName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
            if (isSoapToRESTApi) {
                if (StringUtils.isEmpty(sequenceType) || !(RestApiConstants.IN_SEQUENCE.equals(sequenceType)
                        || RestApiConstants.OUT_SEQUENCE.equals(sequenceType))) {
                    String errorMessage = "Sequence type should be either of the values from 'in' or 'out'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                String resourcePolicy = SequenceUtils
                        .getRestToSoapConvertedSequence(apiIdentifier.getApiName(), apiIdentifier.getVersion(),
                                apiIdentifier.getProviderName(), sequenceType);
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
    public Response apisApiIdResourcePoliciesResourcePolicyIdGet(String apiId, String resourcePolicyId,
            String ifNoneMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            boolean isSoapToRESTApi = SOAPOperationBindingUtils
                    .isSOAPToRESTApi(apiIdentifier.getApiName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
            if (isSoapToRESTApi) {
                if (StringUtils.isEmpty(resourcePolicyId)) {
                    String errorMessage = "Resource id should not be empty to update a resource policy.";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                String policyContent = SequenceUtils
                        .getResourcePolicyFromRegistryResourceId(apiIdentifier, resourcePolicyId);
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
    public Response apisApiIdResourcePoliciesResourcePolicyIdPut(String apiId, String resourcePolicyId,
            ResourcePolicyInfoDTO body, String ifMatch, MessageContext messageContext) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            boolean isSoapToRESTApi = SOAPOperationBindingUtils
                    .isSOAPToRESTApi(apiIdentifier.getApiName(), apiIdentifier.getVersion(),
                            apiIdentifier.getProviderName());
            if (isSoapToRESTApi) {
                if (StringUtils.isEmpty(resourcePolicyId)) {
                    String errorMessage = "Resource id should not be empty to update a resource policy.";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                boolean isValidSchema = RestApiPublisherUtils.validateXMLSchema(body.getContent());
                if (isValidSchema) {
                    SequenceUtils
                            .updateResourcePolicyFromRegistryResourceId(apiIdentifier, resourcePolicyId, body.getContent());
                    String updatedPolicyContent = SequenceUtils
                            .getResourcePolicyFromRegistryResourceId(apiIdentifier, resourcePolicyId);
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
        } catch (APIManagementException e) {
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
    public Response apisApiIdRevenueGet(String apiId, MessageContext messageContext) {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when getting revenue details.";
            RestApiUtil.handleBadRequest(errorMessage, log);
        }
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
    public Response apisApiIdSwaggerGet(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            String apiSwagger = apiProvider.getOpenAPIDefinition(apiIdentifier);
            APIDefinition parser = OASParserUtil.getOASParser(apiSwagger);
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            String updatedDefinition = parser.getOASDefinitionForPublisher(api, apiSwagger);
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
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String url, InputStream fileInputStream,
            Attachment fileDetail, String ifMatch, MessageContext messageContext) {
        try {
            String updatedSwagger;
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            boolean isSoapToRestConvertedAPI = SOAPOperationBindingUtils.isSOAPToRESTApi(apiIdentifier.getApiName(),
                    apiIdentifier.getVersion(), apiIdentifier.getProviderName());
            //Handle URL and file based definition imports
            if(url != null || fileInputStream != null) {
                // Validate and retrieve the OpenAPI definition
                Map validationResponseMap = validateOpenAPIDefinition(url, fileInputStream,
                        fileDetail, true);
                APIDefinitionValidationResponse validationResponse =
                        (APIDefinitionValidationResponse) validationResponseMap .get(RestApiConstants.RETURN_MODEL);
                if (!validationResponse.isValid()) {
                    RestApiUtil.handleBadRequest(validationResponse.getErrorItems(), log);
                }
                updatedSwagger = updateSwagger(apiId, validationResponse);
            } else {
                updatedSwagger = updateSwagger(apiId, apiDefinition);
            }
            if (isSoapToRestConvertedAPI) {
                SequenceGenerator.generateSequencesFromSwagger(updatedSwagger, apiIdentifier);
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
        return updateSwagger(apiId, response);
    }

    /**
     * update swagger definition of the given api
     *
     * @param apiId API Id
     * @param response response of a swagger definition validation call
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    private String updateSwagger(String apiId, APIDefinitionValidationResponse response)
            throws APIManagementException, FaultGatewaysException {
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        //this will fail if user does not have access to the API or the API does not exist
        API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        APIDefinition oasParser = response.getParser();
        String apiDefinition = response.getJsonContent();
        apiDefinition = OASParserUtil.preProcess(apiDefinition);
        Set<URITemplate> uriTemplates = null;
        try {
            uriTemplates = oasParser.getURITemplates(apiDefinition);
        } catch (APIManagementException e) {
            // catch APIManagementException inside again to capture validation error
            RestApiUtil.handleBadRequest(e.getMessage(), log);
        }
        if(uriTemplates == null || uriTemplates.isEmpty()) {
            RestApiUtil.handleBadRequest(ExceptionCodes.NO_RESOURCES_FOUND, log);
        }
        Set<Scope> scopes = oasParser.getScopes(apiDefinition);
        //validating scope roles
        for (Scope scope : scopes) {
            String roles = scope.getRoles();
            if (roles != null) {
                for (String aRole : roles.split(",")) {
                    boolean isValidRole = APIUtil.isRoleNameExist(RestApiUtil.getLoggedInUsername(), aRole);
                    if (!isValidRole) {
                        String error = "Role '" + aRole + "' Does not exist.";
                        RestApiUtil.handleBadRequest(error, log);
                    }
                }
            }
        }

        List<APIResource> removedProductResources = apiProvider.getRemovedProductResources(uriTemplates, existingAPI);

        if (!removedProductResources.isEmpty()) {
            RestApiUtil.handleConflict("Cannot remove following resource paths " +
                    removedProductResources.toString() + " because they are used by one or more API Products", log);
        }

        existingAPI.setUriTemplates(uriTemplates);
        existingAPI.setScopes(scopes);
        validateScopes(existingAPI);

        //Update API is called to update URITemplates and scopes of the API
        SwaggerData swaggerData = new SwaggerData(existingAPI);
        String updatedApiDefinition = oasParser.populateCustomManagementInfo(apiDefinition, swaggerData);
        apiProvider.saveSwagger20Definition(existingAPI.getId(), updatedApiDefinition);
        apiProvider.updateAPI(existingAPI);
        //retrieves the updated swagger definition
        String apiSwagger = apiProvider.getOpenAPIDefinition(existingAPI.getId());
        return oasParser.getOASDefinitionForPublisher(existingAPI, apiSwagger);
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
    public Response apisApiIdThumbnailGet(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            ResourceFile thumbnailResource = apiProvider.getIcon(apiIdentifier);

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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            String fileName = fileDetail.getDataHandler().getName();
            String fileContentType = URLConnection.guessContentTypeFromName(fileName);
            if (org.apache.commons.lang3.StringUtils.isBlank(fileContentType)) {
                fileContentType = fileDetail.getContentType().toString();
            }
            //this will fail if user does not have access to the API or the API does not exist
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            ResourceFile apiImage = new ResourceFile(fileInputStream, fileContentType);
            String thumbPath = APIUtil.getIconPath(api.getId());
            String thumbnailUrl = apiProvider.addResourceFile(api.getId(), thumbPath, apiImage);
            api.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, api.getId().getProviderName()));
            APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, thumbPath);

            //Creating URI templates due to available uri templates in returned api object only kept single template
            //for multiple http methods
            String apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(api.getId());
            if (!org.apache.commons.lang3.StringUtils.isEmpty(apiSwaggerDefinition)) {
                APIDefinition apiDefinition = OASParserUtil.getOASParser(apiSwaggerDefinition);
                Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(apiSwaggerDefinition);
                api.setUriTemplates(uriTemplates);

                // scopes
                Set<Scope> scopes = apiDefinition.getScopes(apiSwaggerDefinition);
                api.setScopes(scopes);
            }

            apiProvider.manageAPI(api);

            String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL
                    .replace(RestApiConstants.APIID_PARAM, apiId);
            URI uri = new URI(uriString);
            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(apiImage.getContentType());
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
        } catch (URISyntaxException | FaultGatewaysException e) {
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

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
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            return apiProvider.isDocumentationExist(apiIdentifier, name) ? Response.status(Response.Status.OK).build() :
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
    public Response apisApiIdResourcePathsGet(String apiId, Integer limit, Integer offset, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
     * @param messageContext CXF message context
     * @return API Definition validation response
     */
    @Override
    public Response validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
          Boolean returnContent, MessageContext messageContext) {

        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        try {
            validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileDetail, returnContent);
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
     * @param messageContext CXF message context
     * @return API Import using OpenAPI definition response
     */
    @Override
    public Response importOpenAPIDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                            String additionalProperties, MessageContext messageContext) {

        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        try {
            validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileDetail, true);
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

        // Convert the 'additionalProperties' json into an APIDTO object
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
        } catch (IOException e) {
            throw RestApiUtil.buildBadRequestException("Error while parsing 'additionalProperties'", e);
        }

        // Only HTTP type APIs should be allowed
        if (!APIDTO.TypeEnum.HTTP.equals(apiDTOFromProperties.getType())) {
            throw RestApiUtil.buildBadRequestException("The API's type should only be HTTP when " +
                    "importing an OpenAPI definition");
        }

        // Import the API and Definition
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API apiToAdd = prepareToCreateAPIByDTO(apiDTOFromProperties);

            boolean syncOperations = apiDTOFromProperties.getOperations().size() > 0;
            // Rearrange paths according to the API payload and save the OpenAPI definition

            APIDefinition apiDefinition = validationResponse.getParser();
            SwaggerData swaggerData;
            String definitionToAdd = validationResponse.getJsonContent();
            if (syncOperations) {
                validateScopes(apiToAdd);
                swaggerData = new SwaggerData(apiToAdd);
                definitionToAdd = apiDefinition.populateCustomManagementInfo(definitionToAdd, swaggerData);
            }
            definitionToAdd = OASParserUtil.preProcess(definitionToAdd);
            Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(definitionToAdd);
            Set<Scope> scopes = apiDefinition.getScopes(definitionToAdd);
            apiToAdd.setUriTemplates(uriTemplates);
            apiToAdd.setScopes(scopes);
            //Set x-wso2-extensions to API when importing through API publisher
            boolean isBasepathExtractedFromSwagger = false;
            apiToAdd = OASParserUtil.setExtensionsToAPI(definitionToAdd, apiToAdd, isBasepathExtractedFromSwagger);
            if (!syncOperations) {
                validateScopes(apiToAdd);
                swaggerData = new SwaggerData(apiToAdd);
                definitionToAdd = apiDefinition
                        .populateCustomManagementInfo(validationResponse.getJsonContent(), swaggerData);
            }

            // adding the API and definition
            apiProvider.addAPI(apiToAdd);
            apiProvider.saveSwaggerDefinition(apiToAdd, definitionToAdd);

            // retrieving the added API for returning as the response
            API addedAPI = apiProvider.getAPI(apiToAdd.getId());
            APIDTO createdApiDTO = APIMappingUtil.fromAPItoDTO(addedAPI);
            // This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
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
        handleInvalidParams(fileInputStream, fileDetail, url);
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
            additionalPropertiesAPI.setProvider(RestApiUtil.getLoggedInUsername());
            additionalPropertiesAPI.setType(APIDTO.TypeEnum.fromValue(implementationType));
            API apiToAdd = prepareToCreateAPIByDTO(additionalPropertiesAPI);
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            if (StringUtils.isNotBlank(url)) {
                apiToAdd.setWsdlUrl(url);
            } else if (fileDetail != null && fileInputStream != null) {
                ResourceFile wsdlResource = new ResourceFile(fileInputStream,
                        fileDetail.getContentType().toString());
                apiToAdd.setWsdlResource(wsdlResource);
            }

            //adding the api
            apiProvider.addAPI(apiToAdd);

            //add the generated swagger definition to SOAP
            APIDefinition oasParser = new OAS2Parser();
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = generateSOAPAPIDefinition(oasParser.generateAPIDefinition(swaggerData));
            apiProvider.saveSwaggerDefinition(apiToAdd, apiDefinition);
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //adding the api
            apiProvider.addAPI(apiToAdd);

            APIIdentifier createdApiId = apiToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);
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
            SequenceGenerator.generateSequencesFromSwagger(updatedSwagger, apiToAdd.getId());
            return createdApi;
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            ResourceFile getWSDLResponse = apiProvider.getWSDL(apiIdentifier);
            return RestApiUtil.getResponseFromResourceFile(apiIdentifier.toString(), getWSDLResponse);
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
    public Response updateWSDLOfAPI(String apiId, InputStream fileInputStream, Attachment fileDetail, String url,
           String ifMatch, MessageContext messageContext) throws APIManagementException {

        validateWSDLAndReset(fileInputStream, fileDetail, url);
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        if (StringUtils.isNotBlank(url)) {
            api.setWsdlUrl(url);
            api.setWsdlResource(null);
            apiProvider.updateWsdlFromUrl(api);
        } else {
            ResourceFile wsdlResource = new ResourceFile(fileInputStream,
                    fileDetail.getContentType().toString());
            api.setWsdlResource(wsdlResource);
            api.setWsdlUrl(null);
            apiProvider.updateWsdlFromResourceFile(api);
        }
        return Response.ok().build();
    }

    @Override
    public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist,
            String ifMatch, MessageContext messageContext) {
        //pre-processing
        String[] checkListItems = lifecycleChecklist != null ? lifecycleChecklist.split(",") : new String[0];

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            Map<String, Object> apiLCData = apiProvider.getAPILifeCycleData(apiIdentifier);
            String[] nextAllowedStates = (String[]) apiLCData.get(APIConstants.LC_NEXT_STATES);
            if (!ArrayUtils.contains(nextAllowedStates, action)) {
                RestApiUtil.handleBadRequest(
                        "Action '" + action + "' is not allowed. Allowed actions are " + Arrays
                                .toString(nextAllowedStates), log);
            }

            //check and set lifecycle check list items including "Deprecate Old Versions" and "Require Re-Subscription".
            for (String checkListItem : checkListItems) {
                String[] attributeValPair = checkListItem.split(":");
                if (attributeValPair.length == 2) {
                    String checkListItemName = attributeValPair[0].trim();
                    boolean checkListItemValue = Boolean.valueOf(attributeValPair[1].trim());
                    apiProvider.checkAndChangeAPILCCheckListItem(apiIdentifier, checkListItemName, checkListItemValue);
                }
            }

            //todo: check if API's tiers are properly set before Publishing
            APIStateChangeResponse stateChangeResponse = apiProvider.changeLifeCycleStatus(apiIdentifier, action);

            //returns the current lifecycle state
            LifecycleStateDTO stateDTO = getLifecycleState(apiId);;

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
    public Response apisCopyApiPost(String newVersion, String apiId, Boolean defaultVersion,
                                    MessageContext messageContext) {
        URI newVersionedApiUri;
        APIDTO newVersionedApi;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = api.getId();
            if (defaultVersion) {
                api.setAsDefaultVersion(true);
            }
            //creates the new version
            apiProvider.createNewAPIVersion(api, newVersion);

            //get newly created API to return as response
            APIIdentifier apiNewVersionedIdentifier =
                    new APIIdentifier(apiIdentifier.getProviderName(), apiIdentifier.getApiName(), newVersion);
            newVersionedApi = APIMappingUtil.fromAPItoDTO(apiProvider.getAPI(apiNewVersionedIdentifier));
            //This URI used to set the location header of the POST response
            newVersionedApiUri =
                    new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + newVersionedApi.getId());
            return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
        } catch (APIManagementException | DuplicateAPIException e) {
            if (RestApiUtil.isDueToResourceAlreadyExists(e)) {
                String errorMessage = "Requested new version " + newVersion + " of API " + apiId + " already exists";
                RestApiUtil.handleResourceAlreadyExistsError(errorMessage, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while copying API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while copying API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
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
    @Override
    public Response apisExportGet(String apiId, String name, String version, String providerName, String format,
                                  Boolean preserveStatus, MessageContext messageContext)
            throws APIManagementException {
        ExportApiUtil exportApiUtil = new ExportApiUtil();
        if (apiId == null) {

            return exportApiUtil.exportApiOrApiProductByParams(name, version, providerName, format, preserveStatus, RestApiConstants.RESOURCE_API);
        } else {
            try {
                String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
                return exportApiUtil.exportApiById(apiIdentifier, preserveStatus);
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else if (isAuthorizationFailure(e)) {
                    RestApiUtil.handleAuthorizationFailure(
                            "Authorization failure while exporting the  API " + apiId, e, log);
                } else {
                    RestApiUtil.handleInternalServerError("Error while exporting the API " + apiId, e, log);
                }
            }
        }
        return null;
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
    public Response apisImportGraphqlSchemaPost(String type, InputStream fileInputStream, Attachment fileDetail,
                                                String additionalProperties, String ifMatch,
                                                MessageContext messageContext) {
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API apiToAdd = prepareToCreateAPIByDTO(additionalPropertiesAPI);

            //adding the api
            apiProvider.addAPI(apiToAdd);

            //Save swagger definition of graphQL
            APIDefinition parser = new OAS3Parser();
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = parser.generateAPIDefinition(swaggerData);
            apiProvider.saveSwagger20Definition(apiToAdd.getId(), apiDefinition);

            APIIdentifier createdApiId = apiToAdd.getId();
            apiProvider.saveGraphqlSchemaDefinition(apiToAdd, schema);

            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);

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
     * Validate graphQL Schema
     * @param fileInputStream  input file
     * @param fileDetail file Detail
     * @param messageContext messageContext
     * @return Validation response
     */
    @Override
    public Response apisValidateGraphqlSchemaPost(InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) {

        String errorMessage = "";
        String schema;
        TypeDefinitionRegistry typeRegistry;
        Set<SchemaValidationError> validationErrors;
        boolean isValid = false;
        SchemaParser schemaParser = new SchemaParser();
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();
        String filename = fileDetail.getContentDisposition().getFilename();

        try {
            if (filename.endsWith(".graphql") || filename.endsWith(".txt") || filename.endsWith(".sdl")) {
                schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
                if (schema.isEmpty()) {
                    errorMessage = "GraphQL Schema cannot be empty or null to validate it";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                typeRegistry = schemaParser.parse(schema);
                GraphQLSchema graphQLSchema = UnExecutableSchemaGenerator.makeUnExecutableSchema(typeRegistry);
                SchemaValidator schemaValidation = new SchemaValidator();
                validationErrors = schemaValidation.validateSchema(graphQLSchema);

                if (validationErrors.toArray().length > 0) {
                    errorMessage = "InValid Schema";
                } else {
                    isValid = true;
                    validationResponse.setIsValid(isValid);
                    GraphQLValidationResponseGraphQLInfoDTO graphQLInfo = new GraphQLValidationResponseGraphQLInfoDTO();
                    List<URITemplate> operationList = graphql.extractGraphQLOperationList(schema, null);
                    List<APIOperationsDTO> operationArray = APIMappingUtil.fromURITemplateListToOprationList(operationList);
                    graphQLInfo.setOperations(operationArray);
                    GraphQLSchemaDTO schemaObj = new GraphQLSchemaDTO();
                    schemaObj.setSchemaDefinition(schema);
                    graphQLInfo.setGraphQLSchema(schemaObj);
                    validationResponse.setGraphQLInfo(graphQLInfo);
                }
            }
            else {
                RestApiUtil.handleBadRequest("Unsupported extension type of file: " + filename, log);
            }
        } catch (SchemaProblem | IOException e) {
            errorMessage = e.getMessage();
        }

        if(!isValid) {
            validationResponse.setIsValid(isValid);
            validationResponse.setErrorMessage(errorMessage);
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
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
        APIIdentifier apiIdentifier = originalAPI.getId();
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);
        apiDefinition=String.valueOf(OASParserUtil.generateExamples(apiDefinition).get(APIConstants.SWAGGER));
        apiProvider.saveSwaggerDefinition(originalAPI,apiDefinition);
        return Response.ok().entity(apiDefinition).build();
    }

    /**
     * Extract GraphQL Operations from given schema
     * @param schema graphQL Schema
     * @return the arrayList of APIOperationsDTOextractGraphQLOperationList
     *
     */
    public List<APIOperationsDTO> extractGraphQLOperationList(String schema) {
        List<APIOperationsDTO> operationArray = new ArrayList<>();
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        Map<java.lang.String, TypeDefinition> operationList = typeRegistry.types();
        for (Map.Entry<String, TypeDefinition> entry : operationList.entrySet()) {
            if (entry.getValue().getName().equals(APIConstants.GRAPHQL_QUERY) ||
                    entry.getValue().getName().equals(APIConstants.GRAPHQL_MUTATION)
                    || entry.getValue().getName().equals(APIConstants.GRAPHQL_SUBSCRIPTION)) {
                for (FieldDefinition fieldDef : ((ObjectTypeDefinition) entry.getValue()).getFieldDefinitions()) {
                    APIOperationsDTO operation = new APIOperationsDTO();
                    operation.setVerb(entry.getKey());
                    operation.setTarget(fieldDef.getName());
                    operationArray.add(operation);
                }
            }
        }
        return operationArray;
    }

    @Override
    public Response apisApiIdSubscriptionPoliciesGet(String apiId, String ifNoneMatch, String xWSO2Tenant,
                                                     MessageContext messageContext) {
        APIDTO apiInfo = getAPIByID(apiId);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl()
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString());

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

    private APIDTO getAPIByID(String apiId) {
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            return APIMappingUtil.fromAPItoDTO(api);
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

    /**
     * Validate the provided OpenAPI definition (via file or url) and return a Map with the validation response
     * information.
     *
     * @param url OpenAPI definition url
     * @param fileInputStream file as input stream
     * @param returnContent whether to return the content of the definition in the response DTO
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     *  of type OpenAPIDefinitionValidationResponseDTO for the REST API. A value with key 'model' will have the
     *  validation response of type APIDefinitionValidationResponse coming from the impl level.
     */
    private Map validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
           Boolean returnContent) throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileDetail, url);

        OpenAPIDefinitionValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, returnContent);
        } else if (fileInputStream != null) {
            try {
                String openAPIContent = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
                validationResponse = OASParserUtil.validateAPIDefinition(openAPIContent, returnContent);
            } catch (IOException e) {
                RestApiUtil.handleInternalServerError("Error while reading file content", e, log);
            }
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
     */
    private void handleInvalidParams(InputStream fileInputStream, Attachment fileDetail, String url) {

        String msg = "";
        boolean isFileSpecified = fileInputStream != null && fileDetail != null &&
                fileDetail.getContentDisposition() != null && fileDetail.getContentDisposition().getFilename() != null;
        if (url == null && !isFileSpecified) {
            msg = "Either 'file' or 'url' should be specified";
        }

        if (isFileSpecified && url != null) {
            msg = "Only one of 'file' and 'url' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            RestApiUtil.handleBadRequest(msg, log);
        }
    }

    /**
     * This method is used to assign micro gateway labels to the DTO
     *
     * @param apiDTO API DTO
     * @param api    the API object
     * @return the API object with labels
     */
    private API assignLabelsToDTO(APIDTO apiDTO, API api) {

        if (apiDTO.getLabels() != null) {
            List<String> labels = apiDTO.getLabels();
            List<Label> labelList = new ArrayList<>();
            for (String label : labels) {
                Label mgLabel = new Label();
                mgLabel.setName(label);
                labelList.add(mgLabel);
            }
            api.setGatewayLabels(labelList);
        }
        return api;
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
     * @param mediationResourcePath mediation config content
     *
     */
    public void checkMediationPolicy(APIProvider apiProvider,String mediationResourcePath) throws APIManagementException {
        if (apiProvider.checkIfResourceExists(mediationResourcePath)) {
            RestApiUtil.handleConflict("Mediation policy already " +
                    "exists in the given resource path, cannot create new", log);
        }
    }
    /**
     * validate user inout scopes
     *
     * @param api api information
     * @throws APIManagementException throw if validation failure
     */
    private void validateScopes(API api) throws APIManagementException {

        APIIdentifier apiId = api.getId();
        String username = RestApiUtil.getLoggedInUsername();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        APIProvider apiProvider = RestApiUtil.getProvider(username);
        Set<Scope> sharedAPIScopes = new HashSet<>();

        for (Scope scope : api.getScopes()) {
            String scopeName = scope.getKey();
            if (!(APIUtil.isWhiteListedScope(scopeName))) {
                // Check if each scope key is already assigned as a local scope to a different API which is also not a
                // different version of the same API. If true, return error.
                // If false, check if the scope key is already defined as a shared scope. If so, do not honor the
                // other scope attributes (description, role bindings) in the request payload, replace them with
                // already defined values for the existing shared scope.
                if (apiProvider.isScopeKeyAssignedLocally(apiId, scopeName, tenantId)) {
                    RestApiUtil
                            .handleBadRequest("Scope " + scopeName + " is already assigned locally by another "
                                    + "API", log);
                } else if (apiProvider.isSharedScopeNameExists(scopeName, tenantDomain)) {
                    sharedAPIScopes.add(scope);
                    continue;
                }
            }

            //set display name as empty if it is not provided
            if (StringUtils.isBlank(scope.getName())) {
                scope.setName(scopeName);
            }

            //set description as empty if it is not provided
            if (StringUtils.isBlank(scope.getDescription())) {
                scope.setDescription("");
            }
            if (scope.getRoles() != null) {
                for (String aRole : scope.getRoles().split(",")) {
                    boolean isValidRole = APIUtil.isRoleNameExist(username, aRole);
                    if (!isValidRole) {
                        String error = "Role '" + aRole + "' does not exist.";
                        RestApiUtil.handleBadRequest(error, log);
                    }
                }
            }
        }

        apiProvider.validateSharedScopes(sharedAPIScopes, tenantDomain);
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
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,
                    new HttpHost(proxyHost, Integer.parseInt(proxyPort)));
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
}
