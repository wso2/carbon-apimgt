/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.policy.APIPolicy;
import org.wso2.carbon.apimgt.api.model.policy.PolicyConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromOpenAPISpec;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListPaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WsdlDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.MediationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

/**
 * This is the service implementation class for Publisher API related operations
 */
public class ApisApiServiceImpl extends ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    /**
     * Retrieves APIs qualifying under given search condition
     *
     * @param limit       maximum number of APIs returns
     * @param offset      starting index
     * @param query       search condition
     * @param accept      Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     */
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch, Boolean expand) {
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            Map<String, Object> result = apiProvider.searchPaginatedAPIs(newSearchQuery, tenantDomain,
                                        offset, limit, false);
            Set<API> apis = (Set<API>) result.get("apis");
            allMatchedApis.addAll(apis);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, expand);
            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, allMatchedApis.size());

            //Add pagination section in the response
            Object totalLength = result.get("length");
            Integer length = 0;
            if(totalLength != null) {
                length = (Integer) totalLength;
            }
            APIListPaginationDTO paginationDTO = new APIListPaginationDTO();
            paginationDTO.setOffset(offset);
            paginationDTO.setLimit(limit);
            paginationDTO.setTotal(length);
            apiListDTO.setPagination(paginationDTO);

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

    /**
     * Create new API
     *
     * @param body        DTO model of new API to be created
     * @param contentType content type of the payload
     * @return created API
     */
    @Override
    public Response apisPost(APIDetailedDTO body, String contentType){
        URI createdApiUri;
        APIDetailedDTO createdApiDTO;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();
            boolean isWSAPI = APIDetailedDTO.TypeEnum.WS == body.getType();

            // validate web socket api endpoint configurations
            if (isWSAPI) {
                if (!RestApiPublisherUtils.isValidWSAPI(body)) {
                    RestApiUtil.handleBadRequest("Endpoint URLs should be valid web socket URLs", log);
                }
            } else {
                if (body.getApiDefinition() == null) {
                    RestApiUtil.handleBadRequest("Parameter: \"apiDefinition\" cannot be null", log);
                }
            }

            String apiSecurity = body.getApiSecurity();
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured() && apiSecurity != null && apiSecurity
                    .contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                RestApiUtil.handleBadRequest("Mutual SSL Based authentication is not supported in this server", log);
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
            if (body.getContext().endsWith("/")) {
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

            List<String> tiersFromDTO = body.getTiers();
            //If tiers are not defined, the api should be a PROTOTYPED one,
            if (!APIConstants.PROTOTYPED.equals(body.getStatus()) &&
                    (tiersFromDTO == null || tiersFromDTO.isEmpty())) {
                RestApiUtil.handleBadRequest("No tier defined for the API", log);
            }
            //check whether the added API's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                RestApiUtil.handleBadRequest(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid", log);
            }
            APIPolicy apiPolicy = apiProvider.getAPIPolicy(username, body.getApiLevelPolicy());
            if (apiPolicy == null && body.getApiLevelPolicy() != null) {
                RestApiUtil.handleBadRequest(
                        "Specified policy " + body.getApiLevelPolicy() + " is invalid", log);
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
            apiToAdd = assignLabelsToDTO(body,apiToAdd);

            //adding the api
            apiProvider.addAPI(apiToAdd);
            if (!isWSAPI) {
                apiProvider.saveSwagger20Definition(apiToAdd.getId(), body.getApiDefinition());
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
        } catch (JSONException e) {
            String errorMessage = "Error while validating endpoint configurations : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + "-" + body.getEndpointConfig();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Changes lifecycle state of an API
     *
     * @param apiId              API identifier
     * @param action             Action to promote or demote the API state
     * @param lifecycleChecklist a checklist specifing additional boolean parameters
     * @param ifMatch            If-Match header value
     * @param ifUnmodifiedSince  If-Unmodified-Since header value
     * @return 200 response if successful
     */
    @Override
    public Response apisChangeLifecyclePost(String action, String apiId, String lifecycleChecklist,
                                            String ifMatch, String ifUnmodifiedSince) {

        //pre-processing
        String[] checkListItems = lifecycleChecklist != null ? lifecycleChecklist.split(",") : new String[0];

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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
            apiProvider.changeLifeCycleStatus(apiIdentifier, action);
            return Response.ok().build();
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

    /**
     * Copy API and create a new version of the API
     *
     * @param apiId      API Identifier
     * @param newVersion new version of the API to be created
     * @return API new version
     */
    @Override
    public Response apisCopyApiPost(String newVersion, String apiId) {
        URI newVersionedApiUri;
        APIDetailedDTO newVersionedApi;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            API api = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = api.getId();

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
     * Get API of given ID
     *
     * @param apiId           API ID
     * @param accept          accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return API of the given ID
     */
    @Override
    public Response apisApiIdGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince) {
        APIDetailedDTO apiToReturn;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            API api = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            apiToReturn = APIMappingUtil.fromAPItoDTO(api);
            return Response.ok().entity(apiToReturn).build();
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
     * Returns list of API specific Mediation policies
     *
     * @param apiId       uuid of the api
     * @param limit       maximum number of mediation returns
     * @param offset      starting index
     * @param query       search condition
     * @param accept      accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return Matched API specific mediation policies for given search condition
     */
    @Override
    public Response apisApiIdPoliciesMediationGet(String apiId, Integer limit, Integer offset,
                                                  String query, String accept, String ifNoneMatch) {
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

    /**
     * Deletes an existing API specific mediation policy
     *
     * @param apiId             API uuid
     * @param mediationPolicyId Uuid of mediation policy resource
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 response if deleted successfully
     */
    @Override
    public Response apisApiIdPoliciesMediationMediationPolicyIdDelete(String apiId,
                                                                      String mediationPolicyId,
                                                                      String ifMatch,
                                                                      String ifUnmodifiedSince) {
        APIIdentifier apiIdentifier;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            boolean deletionStatus = apiProvider.deleteApiSpecificMediationPolicy(apiResourcePath,
                    mediationPolicyId);
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
        }
        return null;
    }

    /**
     * Returns a specific mediation policy by identifier that is belong to the given API identifier
     *
     * @param apiId             API uuid
     * @param mediationPolicyId mediation policy uuid
     * @param accept            Accept header value
     * @param ifNoneMatch       If-None-Match header value
     * @param ifModifiedSince   If-Modified-Since header value
     * @return returns the matched mediation
     */
    @Override
    public Response apisApiIdPoliciesMediationMediationPolicyIdGet(String apiId,
                                                                   String mediationPolicyId,
                                                                   String accept, String ifNoneMatch,
                                                                   String ifModifiedSince) {
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
            Mediation mediation = apiProvider.getApiSpecificMediationPolicy(apiResourcePath,
                    mediationPolicyId);
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
     * @param apiId             API identifier
     * @param mediationPolicyId uuid of mediation policy
     * @param body              content to update
     * @param contentType       Content-Type header
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated mediation DTO as response
     */
    @Override
    public Response apisApiIdPoliciesMediationMediationPolicyIdPut(String apiId,
                                                                   String mediationPolicyId,
                                                                   MediationDTO body,
                                                                   String contentType,
                                                                   String ifMatch,
                                                                   String ifUnmodifiedSince) {
        InputStream contentStream = null;
        APIIdentifier apiIdentifier;
        Mediation updatedMediation;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting the api base path out of apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Getting resource correspond to the given uuid
            Resource mediationResource = apiProvider.getApiSpecificMediationResourceFromUuid
                    (mediationPolicyId, apiResourcePath);
            if (mediationResource != null) {
                //extracting already existing name of the mediation policy
                String contentString = IOUtils.toString(mediationResource.getContentStream(),
                        RegistryConstants.DEFAULT_CHARSET_ENCODING);
                //Get policy name from the mediation config
                OMElement omElement = AXIOMUtil.stringToOM(contentString);
                OMAttribute attribute = omElement.getAttribute(new QName
                        (PolicyConstants.MEDIATION_NAME_ATTRIBUTE));
                String existingMediationPolicyName = attribute.getAttributeValue();

                //replacing the name of the body with existing name
                body.setName(existingMediationPolicyName);

                //Getting mediation policy config to update
                contentStream = new ByteArrayInputStream(body.getConfig().getBytes
                        (StandardCharsets.UTF_8));
                ResourceFile contentFile = new ResourceFile(contentStream, contentType);

                //Getting path to the existing resource
                String resourcePath = mediationResource.getPath();

                //Updating the existing mediation policy
                String updatedPolicyUrl = apiProvider.addResourceFile(resourcePath, contentFile);
                if (StringUtils.isNotBlank(updatedPolicyUrl)) {
                    String uuid = apiProvider.getCreatedResourceUuid(resourcePath);
                    //Getting the updated mediation policy
                    updatedMediation = apiProvider.getApiSpecificMediationPolicy
                            (apiResourcePath, uuid);
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
                    "mediation policy " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (IOException e) {
            String errorMessage = " Error occurred while converting content stream in to string";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (XMLStreamException e) {
            String errorMessage = " Error occurred while getting omelement out of content " +
                    "of mediation policy";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (RegistryException e) {
            String errorMessage = " Error while getting content stream of the requested mediation" +
                    " policy";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
        return null;
    }

    /**
     * Add a API specific mediation policy
     *
     * @param body              Mediation DTO as request body
     * @param apiId             api identifier
     * @param contentType       Content-Type header
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return created mediation DTO as response
     */
    @Override
    public Response apisApiIdPoliciesMediationPost(MediationDTO body, String apiId, String contentType,
                                                   String ifMatch, String ifUnmodifiedSince) {
        APIIdentifier apiIdentifier;
        InputStream contentStream = null;
        Mediation createdMediation;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String content = body.getConfig();
            //Getting the mediation policy config from body to create resource file
            contentStream = new ByteArrayInputStream(content.getBytes
                    (StandardCharsets.UTF_8));
            ResourceFile contentFile = new ResourceFile(contentStream, contentType);
            //Extracting the file name specified in the config
            String fileName = this.getMediationNameFromConfig(content);
            String apiResourcePath = APIUtil.getAPIPath(apiIdentifier);
            //Getting registry Api base path out of apiResourcePath
            apiResourcePath = apiResourcePath.substring(0, apiResourcePath.lastIndexOf("/"));
            //Constructing mediation resource path
            String mediationResourcePath = apiResourcePath + RegistryConstants.PATH_SEPARATOR +
                    body.getType() + RegistryConstants.PATH_SEPARATOR + fileName;
            if (apiProvider.checkIfResourceExists(mediationResourcePath)) {
                RestApiUtil.handleConflict("Mediation policy already " +
                        "exists in the given resource path, cannot create new", log);
            }
            //Adding api specific mediation policy
            String mediationPolicyUrl = apiProvider.addResourceFile(mediationResourcePath, contentFile);
            if (StringUtils.isNotBlank(mediationPolicyUrl)) {
                //Getting the uuid of created mediation policy
                String uuid = apiProvider.getCreatedResourceUuid(mediationResourcePath);
                //Getting created Api specific mediation policy
                createdMediation = apiProvider.getApiSpecificMediationPolicy
                        (apiResourcePath, uuid);
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
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding mediation policay for the API " + apiId, e, log);
            } else {
                String errorMessage = "Error while adding the mediation policy : " + body.getName() +
                        "of API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while getting location header for created " +
                    "mediation policy " + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
        return null;
    }

    /**
     * Update API of given ID
     *
     * @param apiId             API ID
     * @param body              Updated API details
     * @param contentType       Request content type
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated API
     */
    @Override
    public Response apisApiIdPut(String apiId, APIDetailedDTO body, String contentType, String ifMatch,
                                 String ifUnmodifiedSince) {
        APIDetailedDTO updatedApiDTO;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            API apiInfo = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = apiInfo.getId();
            boolean isWSAPI = APIConstants.APIType.WS == APIConstants.APIType.valueOf(apiInfo.getType());

            //Overriding some properties:
            body.setName(apiIdentifier.getApiName());
            body.setVersion(apiIdentifier.getVersion());
            body.setProvider(apiIdentifier.getProviderName());
            body.setContext(apiInfo.getContextTemplate());
            body.setStatus(apiInfo.getStatus());
            body.setType(APIDetailedDTO.TypeEnum.valueOf(apiInfo.getType()));
            //Since there is separate API to change the thumbnail, set the existing thumbnail URL
            //If user needs to remove the thumbnail url, this will give the flexibility to do it via an empty string value
            String thumbnailUrl = body.getThumbnailUri();
            if (!StringUtils.isWhitespace(thumbnailUrl)) {
                body.setThumbnailUri(apiInfo.getThumbnailUrl());
            }

            // Validate API Security
            String apiSecurity = body.getApiSecurity();
            if (!apiProvider.isClientCertificateBasedAuthenticationConfigured() && apiSecurity != null && apiSecurity
                    .contains(APIConstants.API_SECURITY_MUTUAL_SSL)) {
                RestApiUtil.handleBadRequest("Mutual SSL based authentication is not supported in this server.", log);
            }
            //validation for tiers
            List<String> tiersFromDTO = body.getTiers();
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

            //attach micro-geteway labels
            apiToUpdate = assignLabelsToDTO(body,apiToUpdate);

            apiProvider.updateAPI(apiToUpdate);

            if (!isWSAPI) {
                apiProvider.saveSwagger20Definition(apiToUpdate.getId(), body.getApiDefinition());
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


    /**
     * This method is used to assign micro gateway labels to the DTO
     *
     * @param apiDTO API DTO
     * @param api the API object
     * @return the API object with labels
     */
    private API assignLabelsToDTO(APIDetailedDTO apiDTO, API api) {

        if (apiDTO.getLabels() != null) {
            List<LabelDTO> dtoLabels = apiDTO.getLabels();
            List<Label> labelList = new ArrayList<>();
            for (LabelDTO labelDTO : dtoLabels) {
                Label label = new Label();
                label.setName(labelDTO.getName());
                label.setDescription(labelDTO.getDescription());
                labelList.add(label);
            }
            api.setGatewayLabels(labelList);
        }
        return api;
    }

    /**
     * Delete API
     *
     * @param apiId             API Id
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Status of API Deletion
     */
    @Override
    public Response apisApiIdDelete(String apiId, String ifMatch, String ifUnmodifiedSince) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            //check if the API has subscriptions
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
     * Returns all the documents of the given API identifier that matches to the search condition
     *
     * @param apiId       API identifier
     * @param limit       max number of records returned
     * @param offset      starting index
     * @param accept      Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched documents as a list if DocumentDTOs
     */
    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String accept,
                                          String ifNoneMatch) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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
     * @param contentType Content-Type header
     * @return created document DTO as response
     */
    @Override
    public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            String documentName = body.getName();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
            }
            String sourceUrl = body.getSourceUrl();
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
            }
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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
     * Returns a specific document by identifier that is belong to the given API identifier
     *
     * @param apiId           API identifier
     * @param documentId      document identifier
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return returns the matched document
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String accept, String ifNoneMatch,
                                                    String ifModifiedSince) {
        Documentation documentation;
        try {
            RestApiPublisherUtils.checkUserAccessAllowedForAPI(apiId);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
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
     * @param apiId             API identifier
     * @param documentId        document identifier
     * @param body              updated document DTO
     * @param contentType       Content-Type header
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated document DTO as response
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body,
                                                    String contentType, String ifMatch, String ifUnmodifiedSince) {
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
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
                return null;
            }
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                return null;
            }

            //overriding some properties
            body.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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
     * Deletes an existing document of an API
     *
     * @param apiId             API identifier
     * @param documentId        document identifier
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return 200 response if deleted successfully
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId, String documentId, String ifMatch,
                                                       String ifUnmodifiedSince) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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

    /**
     * Retrieves the content of a document
     *
     * @param apiId           API identifier
     * @param documentId      document identifier
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Content of the document/ either inline/file or source url as a redirection
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String accept,
                                                           String ifNoneMatch, String ifModifiedSince) {

        Documentation documentation;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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
            } else if (documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)) {
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
     * @param contentType       content type of the payload
     * @param inputStream       file input stream
     * @param fileDetail        file details as Attachment
     * @param inlineContent     inline content for the document
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated document as DTO
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdContentPost(String apiId, String documentId,
                                                            String contentType, InputStream inputStream, Attachment fileDetail, String inlineContent, String ifMatch,
                                                            String ifUnmodifiedSince) {

        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            API api = APIMappingUtil.getAPIInfoFromApiIdOrUUID(apiId, tenantDomain);
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
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not INLINE", log);
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
     * Retrieves the swagger document of an API
     *
     * @param apiId           API identifier
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Swagger document of the API
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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
     * Retrieves the thumbnail image of an API specified by API identifier
     *
     * @param apiId           API Id
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Thumbnail image of the API
     */
    @Override
    public Response apisApiIdThumbnailGet(String apiId, String accept, String ifNoneMatch,
                                          String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
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

    /**
     * Uploads a thumbnail image into an API
     *
     * @param apiId             API Id
     * @param fileInputStream   input stream of thumbnail image
     * @param fileDetail        file details as Attachment
     * @param contentType       content type of the payload
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return metadata of the uploaded thumbnail image
     */
    @Override
    public Response apisApiIdThumbnailPost(String apiId, InputStream fileInputStream, Attachment fileDetail,
                                           String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            String fileName = fileDetail.getDataHandler().getName();
            String fileContentType = URLConnection.guessContentTypeFromName(fileName);
            if (StringUtils.isBlank(fileContentType)) {
                fileContentType = fileDetail.getContentType().toString();
            }
            //this will fail if user does not have access to the API or the API does not exist
            API api = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            ResourceFile apiImage = new ResourceFile(fileInputStream, fileContentType);
            String thumbPath = APIUtil.getIconPath(api.getId());
            String thumbnailUrl = apiProvider.addResourceFile(thumbPath, apiImage);
            api.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, api.getId().getProviderName()));
            APIUtil.setResourcePermissions(api.getId().getProviderName(), null, null, thumbPath);

            //Creating URI templates due to available uri templates in returned api object only kept single template
            //for multiple http methods
            String apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(api.getId());
            if (!StringUtils.isEmpty(apiSwaggerDefinition)) {
                APIDefinition apiDefinitionFromOpenAPISpec = new APIDefinitionFromOpenAPISpec();
                Set<URITemplate> uriTemplates = apiDefinitionFromOpenAPISpec.getURITemplates(api, apiSwaggerDefinition);
                api.setUriTemplates(uriTemplates);

                // scopes
                Set<Scope> scopes = apiDefinitionFromOpenAPISpec.getScopes(apiSwaggerDefinition);
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

    /**
     * Returns the wsdl correspond to the API specified by the identifier
     *
     * @param apiId           API id/uuid
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return ok with requested wsdl content , else null
     */
    @Override
    public Response apisApiIdWsdlGet(String apiId, String accept, String ifNoneMatch,
                                     String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            String wsdlContent = apiProvider.getWsdl(apiIdentifier);
            WsdlDTO dto = new WsdlDTO();
            dto.setWsdlDefinition(wsdlContent);
            dto.setName(apiIdentifier.getProviderName() + "--" + apiIdentifier.getApiName() +
                    apiIdentifier.getVersion() + ".wsdl");
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving wsdl of API: " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while retrieving wsdl of API: " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * 
     * @param apiId API Id
     * @param body WSDL DTO
     * @param contentType content type of the payload
     * @param ifMatch If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return added wsdl 
     */
    @Override
    public Response apisApiIdWsdlPost(String apiId, WsdlDTO body, String contentType, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId,
                    tenantDomain);
            String resourcePath = apiIdentifier.getProviderName() + APIConstants.WSDL_PROVIDER_SEPERATOR +
                    apiIdentifier.getApiName() + apiIdentifier.getVersion() +
                    APIConstants.WSDL_FILE_EXTENSION;
            resourcePath = APIConstants.API_WSDL_RESOURCE_LOCATION + resourcePath;
            if (apiProvider.checkIfResourceExists(resourcePath)) {
                RestApiUtil.handleConflict("wsdl resource already exists for the API " + apiId, log);
            }
            apiProvider.uploadWsdl(resourcePath, body.getWsdlDefinition());

            WsdlDTO wsdlDTO = new WsdlDTO();
            wsdlDTO.setWsdlDefinition(apiProvider.getWsdl(apiIdentifier));
            wsdlDTO.setName(apiIdentifier.getProviderName() + "--" + apiIdentifier.getApiName() +
                    apiIdentifier.getVersion() + ".wsdl");
            return Response.ok().entity(wsdlDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while uploading wsdl for API: " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while uploading wsdl of API : " + apiId;
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
     * @param contentType       content type of the payload
     * @param ifMatch           If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated swagger document of the API
     */
    @Override
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String contentType, String ifMatch,
                                        String ifUnmodifiedSince) {
        try {
            APIDefinition apiDefinitionFromOpenAPISpec = new APIDefinitionFromOpenAPISpec();
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            API existingAPI = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            Set<URITemplate> uriTemplates = apiDefinitionFromOpenAPISpec.getURITemplates(existingAPI, apiDefinition);
            Set<Scope> scopes = apiDefinitionFromOpenAPISpec.getScopes(apiDefinition);
            existingAPI.setUriTemplates(uriTemplates);
            existingAPI.setScopes(scopes);

            //Update API is called to update URITemplates and scopes of the API
            apiProvider.updateAPI(existingAPI);
            apiProvider.saveSwagger20Definition(existingAPI.getId(), apiDefinition);
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
            log.error("Error occurred while converting the mediation config string to json", e);
        } catch (ParseException e) {
            log.error("Error occurred while parsing config json string in to json object", e);
        }
        return null;
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
