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
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.MonetizationException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Monetization;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.ResourcePath;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.SwaggerData;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.Policy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromOpenAPISpec;
import org.wso2.carbon.apimgt.impl.definitions.OAS2Parser;
import org.wso2.carbon.apimgt.impl.definitions.OAS3Parser;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.wsdl.SequenceGenerator;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.impl.wsdl.util.SOAPOperationBindingUtils;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.ExternalStoreMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Optional;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query,
            String ifNoneMatch, Boolean expand, String accept ,String tenantDomain, MessageContext messageContext) {

        List<API> allMatchedApis = new ArrayList<>();
        APIListDTO apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        expand = (expand != null && expand) ? true : false;
        try {
            String newSearchQuery = APIUtil.constructNewSearchQuery(query);

            //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
            if (newSearchQuery.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=")) {
                newSearchQuery = newSearchQuery
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + "=", APIConstants.NAME_TYPE_PREFIX + "=");
            }

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            // We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            String username = RestApiUtil.getLoggedInUsername();
            tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
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
            String username = RestApiUtil.getLoggedInUsername();
            boolean isGraphQL = APIDTO.TypeEnum.GRAPHQL == body.getType();
            boolean isWSAPI = APIDTO.TypeEnum.WS == body.getType();
            boolean isSoapToRestConvertedApi = APIDTO.TypeEnum.SOAPTOREST == body.getType();

            // validate web socket api endpoint configurations
            if (isWSAPI && !RestApiPublisherUtils.isValidWSAPI(body)) {
                RestApiUtil.handleBadRequest("Endpoint URLs should be valid web socket URLs", log);
            }

            API apiToAdd = prepareToCreateAPIByDTO(body);
            //adding the api
            apiProvider.addAPI(apiToAdd);

            if (isSoapToRestConvertedApi) {
                if (StringUtils.isNotBlank(apiToAdd.getWsdlUrl())) {
                    String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(body.getWsdlUri());
                    apiProvider.saveSwaggerDefinition(apiToAdd, swaggerStr);
                    SequenceGenerator.generateSequencesFromSwagger(swaggerStr, new Gson().toJson(body));
                } else {
                    String errorMessage =
                            "Error while generating the swagger since the wsdl url is null for: " + body.getProvider()
                                    + "-" + body.getName() + "-" + body.getVersion();
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            } else if (!isWSAPI) {
                APIDefinition oasParser;
                if(RestApiConstants.OAS_VERSION_2.equalsIgnoreCase(oasVersion)) {
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
        }
        return null;
    }

    /**
     *  Prepares the API Model object to be created using the DTO object
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
        assignLabelsToDTO(body,apiToAdd);

        // set default API Level Policy
        if (StringUtils.isBlank(apiToAdd.getApiLevelPolicy())) {
            Policy[] apiPolicies = apiProvider.getPolicies(username, PolicyConstants.POLICY_LEVEL_API);
            if (apiPolicies.length > 0) {
                for (Policy policy : apiPolicies) {
                    if (policy.getPolicyName().equals(APIConstants.UNLIMITED_TIER)) {
                        apiToAdd.setApiLevelPolicy(APIConstants.UNLIMITED_TIER);
                        break;
                    }
                }
                if (StringUtils.isBlank(apiToAdd.getApiLevelPolicy())) {
                    apiToAdd.setApiLevelPolicy(apiPolicies[0].getPolicyName());
                }
            }
        }

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
     * @param ifNoneMatch If--Match header value
     * @param messageContext message context
     * @return Response with GraphQL Schema
     */
    @Override
    public Response apisApiIdGraphqlSchemaGet(String apiId, String accept, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId,
                    tenantDomain);
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
     * @param apiId api Id
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
            boolean isWSAPI = originalAPI.getType() != null && APIConstants.APITransportType.WS == APIConstants.APITransportType
                    .valueOf(originalAPI.getType());

            org.wso2.carbon.apimgt.rest.api.util.annotations.Scope[] apiDtoClassAnnotatedScopes =
                    APIDTO.class.getAnnotationsByType(org.wso2.carbon.apimgt.rest.api.util.annotations.Scope.class);
            boolean hasClassLevelScope = checkClassScopeAnnotation(apiDtoClassAnnotatedScopes, tokenScopes);

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

            // Validate API Security
            List<String> apiSecurity = body.getSecurityScheme();
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured() && apiSecurity != null && apiSecurity
                    .contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                RestApiUtil.handleBadRequest("Mutual SSL based authentication is not supported in this server.", log);
            }
            //validation for tiers
            List<String> tiersFromDTO = body.getPolicies();
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                RestApiUtil.handleBadRequest("No tier defined for the API", log);
            }
            //check whether the added API's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                RestApiUtil.handleBadRequest(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
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
            API apiToUpdate = APIMappingUtil.fromDTOtoAPI(body, apiIdentifier.getProviderName());
            apiToUpdate.setThumbnailUrl(originalAPI.getThumbnailUrl());

            //attach micro-geteway labels
            assignLabelsToDTO(body, apiToUpdate);
            apiProvider.manageAPI(apiToUpdate);

            if (!isWSAPI) {
                String oldDefinition = apiProvider.getOpenAPIDefinition(apiIdentifier);
                Optional<APIDefinition> definitionOptional = OASParserUtil.getOASParser(oldDefinition);
                if(!definitionOptional.isPresent()) {
                    RestApiUtil.handleInternalServerError("Error occurred while getting swagger parser.", log);
                    return null;
                }
                APIDefinition apiDefinition = definitionOptional.get();
                SwaggerData swaggerData = new SwaggerData(apiToUpdate);
                String newDefinition = apiDefinition.generateAPIDefinition(swaggerData, oldDefinition,
                        true);
                apiProvider.saveSwagger20Definition(apiToUpdate.getId(), newDefinition);
            }
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
                String errorMessage = "Error while updating API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
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

            // Get configuration file and retrieve API token
            APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                    .getAPIManagerConfigurationService().getAPIManagerConfiguration();
            String apiToken = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_API_TOKEN);

            // Retrieve the uuid from the database
            String uuid = ApiMgtDAO.getInstance().getAuditApiId(apiIdentifier);

            if (uuid != null) {
                // PUT Request - Sync API Definition with Audit API

                // Set the property to be attached in the body of the request
                // Attach API Definition to property called specfile to be sent in the request
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("{\n");
                stringBuilder.append("  \"specfile\":   \"").append(Base64Utils.encode(apiDefinition.getBytes("UTF-8"))).append("\" \n");
                stringBuilder.append("}");

                // Logic for HTTP Request
                String putUrl = "https://platform.42crunch.com/api/v1/apis/" + uuid;
                org.apache.axis2.util.URL updateApiUrl = new org.apache.axis2.util.URL(putUrl);
                try (CloseableHttpClient httpClient = (CloseableHttpClient) APIUtil.getHttpClient(updateApiUrl.getPort(), updateApiUrl.getProtocol())) {
                    HttpPut httpPut = new HttpPut(putUrl);

                    // Set the header properties of the request
                    httpPut.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    httpPut.setHeader(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                    httpPut.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);
                    httpPut.setEntity(new StringEntity(stringBuilder.toString()));

                    // Code block for processing the response
                    try (CloseableHttpResponse response = httpClient.execute(httpPut)) {
                        if (isDebugEnabled) {
                            log.debug("HTTP status " + response.getStatusLine().getStatusCode());
                        }
                        if (!(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)) {
                            throw new APIManagementException("Error while sending data to " + putUrl +
                                    ". Found http status " + response.getStatusLine());
                        }
                    } finally {
                        httpPut.releaseConnection();
                    }
                }
            } else {
                // POST Request - Create new Audit API

                HttpURLConnection httpConn;
                OutputStream outputStream;
                PrintWriter writer;
                String postUrl = "https://platform.42crunch.com/api/v1/apis";
                String collectionId = config.getFirstProperty(APIConstants.API_SECURITY_AUDIT_CID);

                URL url = new URL (postUrl);
                httpConn = (HttpURLConnection) url.openConnection();
                httpConn.setUseCaches(false);
                httpConn.setDoOutput(true); // indicates POST method
                httpConn.setDoInput(true);
                httpConn.setRequestProperty(APIConstants.HEADER_CONTENT_TYPE,
                        "multipart/form-data; boundary=" + APIConstants.MULTIPART_FORM_BOUNDARY);
                httpConn.setRequestProperty(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                httpConn.setRequestProperty(APIConstants.HEADER_API_TOKEN, apiToken);
                outputStream = httpConn.getOutputStream();
                writer = new PrintWriter(new OutputStreamWriter(outputStream, "UTF-8"),
                        true);

                // Name property
                writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append("Content-Disposition: form-data; name=\"name\"")
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(apiIdentifier.getApiName())
                        .append(APIConstants.MULTIPART_LINE_FEED);
                writer.flush();

                // Specfile property
                writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append("Content-Disposition: form-data; name=\"specfile\"; filename=\"swagger.json\"")
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(APIConstants.HEADER_CONTENT_TYPE + ": " + APIConstants.APPLICATION_JSON_MEDIA_TYPE)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(apiDefinition)
                        .append(APIConstants.MULTIPART_LINE_FEED);
                writer.flush();

                // CollectionID property
                writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append("Content-Disposition: form-data; name=\"cid\"")
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(APIConstants.MULTIPART_LINE_FEED)
                        .append(collectionId)
                        .append(APIConstants.MULTIPART_LINE_FEED);
                writer.flush();

                writer.append("--" + APIConstants.MULTIPART_FORM_BOUNDARY + "--").append(APIConstants.MULTIPART_LINE_FEED);
                writer.close();

                // Checks server's status code first
                int status = httpConn.getResponseCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    if(isDebugEnabled) {
                        log.debug("HTTP status " + status);
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            httpConn.getInputStream(), "UTF-8"));
                    String inputLine;
                    StringBuilder responseString = new StringBuilder();

                    while((inputLine = reader.readLine()) != null) {
                        responseString.append(inputLine);
                    }
                    reader.close();
                    httpConn.disconnect();
                    JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                    uuid = (String) ((JSONObject) responseJson.get("desc")).get("id");
                    ApiMgtDAO.getInstance().addAuditApiMapping(apiIdentifier, uuid);
                }
            }

            // GET Request - Retrieve the API Security Audit Report for the Audit API
            // Logic for the HTTP request
            String getUrl = "https://platform.42crunch.com/api/v1/apis/" + uuid + "/assessmentreport";
            org.apache.axis2.util.URL getReportUrl = new org.apache.axis2.util.URL(getUrl);
            try (CloseableHttpClient getHttpClient = (CloseableHttpClient) APIUtil.getHttpClient(getReportUrl.getPort(), getReportUrl.getProtocol())) {
                HttpGet httpGet = new HttpGet(getUrl);

                // Set the header properties of the request
                httpGet.setHeader(APIConstants.HEADER_ACCEPT, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                httpGet.setHeader(APIConstants.HEADER_API_TOKEN, apiToken);

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
                        JSONObject responseJson = (JSONObject) new JSONParser().parse(responseString.toString());
                        String report = responseJson.get("data").toString();
                        String grade = (String) ((JSONObject) ((JSONObject) responseJson.get("attr")).get("data")).get("grade");
                        Integer numErrors = Integer.valueOf((String) ((JSONObject) ((JSONObject) responseJson.get("attr")).get("data")).get("numErrors"));
                        String decodedReport = new String(Base64Utils.decode(report), "UTF-8");
                        AuditReportDTO auditReportDTO = new AuditReportDTO();
                        auditReportDTO.setReport(decodedReport);
                        auditReportDTO.setGrade(grade);
                        auditReportDTO.setNumErrors(numErrors);
                        return Response.ok().entity(auditReportDTO).build();
                    }
                }
            }
        } catch (IOException e) {
            log.error("Error occurred while getting HttpClient instance");
        } catch (ParseException e) {
            log.error("API Definition String could not be parsed into JSONObject.");
        } catch (APIManagementException e) {
            String errorMessage = "Error while Auditing API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Check whether the token has APIDTO class level Scope annotation
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

            //deletes the API
            apiProvider.deleteAPI(apiIdentifier);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            keyManager.deleteRegisteredResourceByAPIId(apiId);
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
     * Retrieves the content of a document
     *
     * @param apiId           API identifier
     * @param documentId      document identifier
     * @param ifNoneMatch     If-None-Match header value
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
     * @param apiId             API identifier
     * @param documentId        document identifier
     * @param inputStream       file input stream
     * @param fileDetail        file details as Attachment
     * @param inlineContent     inline content for the document
     * @param ifMatch           If-match header value
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
            }  else {
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
     * @param apiId             API identifier
     * @param documentId        document identifier
     * @param ifMatch           If-match header value
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
            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
     * @param apiId             API identifier
     * @param documentId        document identifier
     * @param body              updated document DTO
     * @param ifMatch           If-match header value
     * @return updated document DTO as response
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body,
            String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
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
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
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
     * @param apiId       api identifier
     * @param body        Documentation DTO as request body
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
        ExternalStoreListDTO externalStoreListDTO =
                ExternalStoreMappingUtil.fromExternalStoreCollectionToDTO(publishedStores);
        return Response.ok().entity(externalStoreListDTO).build();
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
            return APIMappingUtil.fromLifecycleModelToDTO(apiLCData);
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
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdMediationPoliciesGet(String apiId, Integer limit, Integer offset, String query,
            String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdMediationPoliciesMediationPolicyIdDelete(String apiId, String mediationPolicyId,
            String ifMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdMediationPoliciesMediationPolicyIdGet(String apiId, String mediationPolicyId,
            String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdMediationPoliciesMediationPolicyIdPut(String apiId, String mediationPolicyId,
            MediationDTO body, String ifMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdMediationPoliciesPost(MediationDTO body, String apiId, String ifMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
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
            RestApiUtil.handleInternalServerError(errorMessage, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to fetch monetized plans of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, log);
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
            apiProvider.configureMonetizationInAPIArtifact(api);
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            HashMap monetizationDataMap = new Gson().fromJson(api.getMonetizationProperties().toString(), HashMap.class);
            boolean isMonetizationStateChangeSuccessful = false;
            if (MapUtils.isEmpty(monetizationDataMap)) {
                String errorMessage = "Monetization data map is empty for API ID " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, log);
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
    public Response publishAPIToExternalStores(String apiId, List<String> externalStoreIds, String ifMatch,
                                                         MessageContext messageContext) throws APIManagementException {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        API api = null;
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
        if (apiProvider.publishToExternalAPIStores(api, externalStoreIds)) {
            Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(api.getId());
            ExternalStoreListDTO externalStoreListDTO =
                    ExternalStoreMappingUtil.fromExternalStoreCollectionToDTO(publishedStores);
            return Response.ok().entity(externalStoreListDTO).build();
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
            RestApiUtil.handleInternalServerError(errorMessage, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to get current revenue data for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, log);
        }
        return null;
    }

    @Override
    public Response apisApiIdScopesGet(String apiId, String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdScopesNameDelete(String apiId, String name, String ifMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdScopesNameGet(String apiId, String name, String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdScopesNamePut(String apiId, String name, ScopeDTO body, String ifMatch,
            MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdScopesPost(String apiId, ScopeDTO body, String ifMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
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
            return Response.ok().entity(apiSwagger).build();
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
     * @param ifMatch           If-match header value
     * @return updated swagger document of the API
     */
    @Override
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String ifMatch, MessageContext messageContext) {
        try {
            APIDefinitionValidationResponse response = OASParserUtil
                    .validateAPIDefinition(apiDefinition, true);
            if (!response.isValid()) {
                RestApiUtil.handleBadRequest(response.getErrorItems(), log);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            API existingAPI = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            APIDefinition oasParser = response.getParser();
            Set<URITemplate> uriTemplates = null;
            try {
                SwaggerData swaggerData = new SwaggerData(existingAPI);
                uriTemplates = oasParser.getURITemplates(swaggerData, response.getJsonContent());
            } catch (APIManagementException e) {
                // catch APIManagementException inside again to capture validation error
                RestApiUtil.handleBadRequest(e.getMessage(), log);
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

            existingAPI.setUriTemplates(uriTemplates);
            existingAPI.setScopes(scopes);

            //Update API is called to update URITemplates and scopes of the API
            apiProvider.updateAPI(existingAPI);
            SwaggerData swaggerData = new SwaggerData(existingAPI);
            String updatedApiDefinition = oasParser.populateCustomManagementInfo(apiDefinition, swaggerData);
            apiProvider.saveSwagger20Definition(existingAPI.getId(), updatedApiDefinition);
            //retrieves the updated swagger definition
            String apiSwagger = apiProvider.getOpenAPIDefinition(existingAPI.getId());
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating swagger definition of API: " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
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
            String thumbnailUrl = apiProvider.addResourceFile(thumbPath, apiImage);
            api.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, api.getId().getProviderName()));
            APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, thumbPath);

            //Creating URI templates due to available uri templates in returned api object only kept single template
            //for multiple http methods
            String apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(api.getId());
            if (!org.apache.commons.lang3.StringUtils.isEmpty(apiSwaggerDefinition)) {
                Optional<APIDefinition> definitionOptional = OASParserUtil.getOASParser(apiSwaggerDefinition);
                if(!definitionOptional.isPresent()) {
                    RestApiUtil.handleInternalServerError("Error occurred while getting swagger parser.", log);
                    return null;
                }
                APIDefinition apiDefinition = definitionOptional.get();
                SwaggerData swaggerData = new SwaggerData(api);
                Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(swaggerData, apiSwaggerDefinition);
                api.setUriTemplates(uriTemplates);

                // scopes
                Set<Scope> scopes = apiDefinition.getScopes(apiSwaggerDefinition);
                api.setScopes(scopes);
            }

            apiProvider.updateAPI(api);

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
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving thumbnail location of API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (FaultGatewaysException e) {
            //This is logged and process is continued because icon is optional for an API
            log.error("Failed to update API after adding icon. ", e);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return null;
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
            validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, returnContent);
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
            validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, true);
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

            String definitionToAdd;
            boolean syncOperations = apiDTOFromProperties.getOperations().size() > 0;
            // Rearrange paths according to the API payload and save the OpenAPI definition

            APIDefinition apiDefinition = validationResponse.getParser();
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            definitionToAdd = apiDefinition.generateAPIDefinition(swaggerData,
                    validationResponse.getJsonContent(), syncOperations);

            Set<URITemplate> uriTemplates = apiDefinition.getURITemplates(swaggerData, definitionToAdd);
            Set<Scope> scopes = apiDefinition.getScopes(definitionToAdd);
            apiToAdd.setUriTemplates(uriTemplates);
            apiToAdd.setScopes(scopes);

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
        handleInvalidParams(fileInputStream, url);
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
        return Response.ok().entity(responseDTO).build();
    }

    @Override
    public Response importWSDLDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                         String additionalProperties, String implementationType, MessageContext messageContext) {
        try {

            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            APIDTO additionalPropertiesAPI = null;
            APIDTO createdApiDTO;
            URI createdApiUri;

            // Minimum requirement name, version, context and endpointConfig.
            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
            additionalPropertiesAPI.setProvider(RestApiUtil.getLoggedInUsername());
            additionalPropertiesAPI.setType(APIDTO.TypeEnum.SOAPTOREST);
            API apiToAdd = prepareToCreateAPIByDTO(additionalPropertiesAPI);
            //adding the api
            apiProvider.addAPI(apiToAdd);

            boolean isSoapToRestConvertedApi = APIDTO.TypeEnum.SOAPTOREST.equals(implementationType);
            // TODO: First-cut only support URL SOAPToREST remove this todo if it's not
            if (isSoapToRestConvertedApi && StringUtils.isNotBlank(url)) {
                if (StringUtils.isNotBlank(url)) {
                    String swaggerStr = SOAPOperationBindingUtils.getSoapOperationMapping(url);
                    apiProvider.saveSwagger20Definition(apiToAdd.getId(), swaggerStr);
                    SequenceGenerator.generateSequencesFromSwagger(swaggerStr, new Gson().toJson(additionalPropertiesAPI));
                } else {
                    String errorMessage =
                            "Error while generating the swagger since the wsdl url is null for: " + apiProvider;
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            }
            APIIdentifier createdApiId = apiToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException | IOException | URISyntaxException e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    @Override
    public Response apisApiIdWsdlGet(String apiId, String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
    }

    @Override
    public Response apisApiIdWsdlPut(String apiId, InputStream fileInputStream, Attachment fileDetail,
            String ifMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
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

    @Override
    public Response apisHead(String query, String ifNoneMatch, MessageContext messageContext) {
        // do some magic!
        return Response.ok().entity("magic!").build();
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
            APIDefinitionFromOpenAPISpec apiDefinitionUsingOASParser = new APIDefinitionFromOpenAPISpec();
            SwaggerData swaggerData = new SwaggerData(apiToAdd);
            String apiDefinition = apiDefinitionUsingOASParser.generateAPIDefinition(swaggerData);
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
            String errorMessage = "Error while retrieving API location : " + additionalPropertiesAPI.getProvider() + "-" +
                additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
    } catch (IOException e) {
            String errorMessage = "Error while retrieving content from file : " + additionalPropertiesAPI.getProvider()
                    + "-" + additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion()
                    + "-" /*+ body.getEndpointConfig()*/;
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
        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();

        try {
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
                List<APIOperationsDTO> operationArray = extractGraphQLOperationList(schema);
                graphQLInfo.setOperations(operationArray);
                GraphQLSchemaDTO schemaObj = new GraphQLSchemaDTO();
                schemaObj.setSchemaDefinition(schema);
                graphQLInfo.setGraphQLSchema(schemaObj);
                validationResponse.setGraphQLInfo(graphQLInfo);
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
     * Extract GraphQL Operations from given schema
     * @param schema graphQL Schema
     * @return the arrayList of APIOperationsDTO
     */
    private List<APIOperationsDTO> extractGraphQLOperationList(String schema) {
        List<APIOperationsDTO> operationArray = new ArrayList<>();
        SchemaParser schemaParser = new SchemaParser();
        TypeDefinitionRegistry typeRegistry = schemaParser.parse(schema);
        Map<java.lang.String, graphql.language.TypeDefinition> operationList = typeRegistry.types();
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
     * Validate the provided OpenAPI definition (via file or url) and return the validation response DTO
     *
     * @param url OpenAPI definition url
     * @param fileInputStream file as input stream
     * @param returnContent whether to return the content of the definition in the response DTO
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     *  of type OpenAPIDefinitionValidationResponseDTO for the REST API. A value with key 'model' will have the
     *  validation response of type APIDefinitionValidationResponse coming from the impl level.
     */
    private Map validateOpenAPIDefinition(String url, InputStream fileInputStream, Boolean returnContent)
            throws APIManagementException {
        handleInvalidParams(fileInputStream, url);
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
    private void handleInvalidParams(InputStream fileInputStream, String url) {

        String msg = "";
        if (url == null && fileInputStream == null) {
            msg = "Either 'file' or 'url' should be specified";
        }

        if (fileInputStream != null && url != null) {
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
     * @param api the API object
     * @return the API object with labels
     */
    private API assignLabelsToDTO(APIDTO apiDTO, API api) {

        if (apiDTO.getLabels() != null) {
            List<LabelDTO> dtoLabels = apiDTO.getLabels();
            List<Label> labelList = new ArrayList<>();
            for (LabelDTO labelDTO : dtoLabels) {
                Label label = new Label();
                label.setName(labelDTO.getName());
//                label.setDescription(labelDTO.getDescription()); todo add description
                labelList.add(label);
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
}
