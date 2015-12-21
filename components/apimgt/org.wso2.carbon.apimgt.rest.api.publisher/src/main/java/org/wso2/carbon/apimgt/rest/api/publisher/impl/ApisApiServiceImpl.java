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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the service implementation class for Publisher API related operations
 */
public class ApisApiServiceImpl extends ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    /** 
     * Retrieves APIs qualifying under given search condition 
     * 
     * @param limit maximum number of APIs returns
     * @param offset starting index
     * @param query search condition
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     */
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch) {
        List<API> allMatchedApis;
        APIListDTO apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        query = query == null ? "" : query;

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //if query parameter is not specified, This will search by name
            String searchType = APIConstants.API_NAME;
            String searchContent = "";
            if (!StringUtils.isBlank(query)) {
                String[] querySplit = query.split(":");
                if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                        .isNotBlank(querySplit[1])) {
                    searchType = querySplit[0];
                    searchContent = querySplit[1];
                } else if (querySplit.length == 1) {
                    searchContent = query; 
                } else {
                    throw RestApiUtil.buildBadRequestException("Provided query parameter '" + query + "' is invalid");
                }
            }

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            allMatchedApis = apiProvider.searchAPIs(searchContent, searchType, null);
            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, offset, limit);
            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, allMatchedApis.size());
            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Create new API
     *
     * @param body DTO model of new API to be created
     * @param contentType content type of the payload
     * @return created API
     */
    @Override
    public Response apisPost(APIDTO body,String contentType){

        URI createdApiUri;
        APIDTO  createdApiDTO;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String username = RestApiUtil.getLoggedInUsername();

            if (body.getContext().endsWith("/")) {
                throw RestApiUtil.buildBadRequestException("Context cannot end with '/' character");
            }

            if (apiProvider.isDuplicateContextTemplate(body.getContext())) {
                throw RestApiUtil.buildConflictException(
                        "Error occurred while adding the API. A duplicate API context already exists for " + body
                                .getContext());
            }

            List<String> tiersFromDTO = body.getTiers();
            //If tiers are not defined, the api should be a PROTOTYPED one,
            if (!APIStatus.PROTOTYPED.toString().equals(body.getStatus()) && 
                    (tiersFromDTO == null || tiersFromDTO.isEmpty())) {
                throw RestApiUtil.buildBadRequestException("No tier defined for the API");
            }

            //check whether the added API's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                throw RestApiUtil.buildBadRequestException(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid");
            }

            API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, username);

            if (apiProvider.isAPIAvailable(apiToAdd.getId())) {
                throw RestApiUtil.buildConflictException(
                        "Error occurred while adding the API. A duplicate API already exists for " + apiToAdd.getId()
                                .getApiName() + "-" + apiToAdd.getId().getVersion());
            }

            //Overriding some properties:
            //only allow CREATED as the stating state for the new api if not status is PROTOTYPED
            if (!APIStatus.PROTOTYPED.equals(apiToAdd.getStatus())) {
                apiToAdd.setStatus(APIStatus.CREATED);
            }

            //we are setting the api owner as the logged in user until we support checking admin privileges and assigning
            //  the owner as a different user
            apiToAdd.setApiOwner(username);


            //adding the api
            apiProvider.addAPI(apiToAdd);
            apiProvider.saveSwagger20Definition(apiToAdd.getId(), body.getApiDefinition());
            APIIdentifier createdApiId = apiToAdd.getId();
            //Retrieve the newly added API to send in the response payload
            API createdApi = apiProvider.getAPI(createdApiId);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + body.getProvider() + "-" +
                                  body.getName() + "-" + body.getVersion();
            handleException(errorMessage, e);
        }
        catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + body.getProvider() + "-" +
                                  body.getName() + "-" + body.getVersion();
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Changes lifecycle state of an API
     *  
     * @param apiId API identifier
     * @param action Action to promote or demote the API state
     * @param lifecycleChecklist a checklist specifing additional boolean parameters
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
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
                throw RestApiUtil.buildBadRequestException(
                        "Action '" + action + "' is not allowed. Allowed actions are " + Arrays
                                .toString(nextAllowedStates));
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                handleException("Error while updating lifecycle of API " + apiId, e);
            }
        }
        return null;
    }

    /**
     * Copy API and create a new version of the API
     *
     * @param apiId API Identifier
     * @param newVersion new version of the API to be created
     * @return API new version
     */
    @Override
    public Response apisCopyApiPost(String newVersion, String apiId){
        URI newVersionedApiUri;
        APIDTO newVersionedApi;

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
                throw RestApiUtil.buildConflictException(errorMessage);
            } else if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while copying API : " + apiId;
                handleException(errorMessage, e);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location of " + apiId;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Get API of given ID
     *
     * @param apiId  API ID
     * @param accept accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return API of the given ID
     */
    @Override
    public Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince){
        APIDTO apiToReturn;
        try {
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            API api = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);

            apiToReturn = APIMappingUtil.fromAPItoDTO(api);
            return Response.ok().entity(apiToReturn).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    /**
     * Update API of given ID
     *
     * @param apiId API ID
     * @param body  Updated API details
     * @param contentType Request content type
     * @param ifMatch If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Updated API
     */
    @Override
    public Response apisApiIdPut(String apiId,APIDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        APIDTO updatedApiDTO;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            API apiInfo = APIMappingUtil.getAPIFromApiIdOrUUID(apiId, tenantDomain);
            APIIdentifier apiIdentifier = apiInfo.getId();

            //Overriding some properties:
            body.setName(apiIdentifier.getApiName());
            body.setVersion(apiIdentifier.getVersion());
            body.setProvider(apiIdentifier.getProviderName());
            body.setContext(apiInfo.getContextTemplate());
            body.setStatus(apiInfo.getStatus().getStatus());

            List<String> tiersFromDTO = body.getTiers();
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                throw RestApiUtil.buildBadRequestException("No tier defined for the API");
            }

            //check whether the added API's tiers are all valid
            Set<Tier> definedTiers = apiProvider.getTiers();
            List<String> invalidTiers = RestApiUtil.getInvalidTierNames(definedTiers, tiersFromDTO);
            if (invalidTiers.size() > 0) {
                throw RestApiUtil.buildBadRequestException(
                        "Specified tier(s) " + Arrays.toString(invalidTiers.toArray()) + " are invalid");
            }

            API apiToUpdate = APIMappingUtil.fromDTOtoAPI(body, apiIdentifier.getProviderName());

            apiProvider.updateAPI(apiToUpdate);
            API updatedApi = apiProvider.getAPI(apiIdentifier);
            updatedApiDTO = APIMappingUtil.fromAPItoDTO(updatedApi);
            return Response.ok().entity(updatedApiDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while updating API : " + apiId;
                handleException(errorMessage, e);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Delete API
     *
     * @param apiId API Id
     * @param ifMatch If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return Status of API Deletion
     */
    @Override
    public Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince){
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            apiProvider.deleteAPI(apiIdentifier);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            
            keyManager.deleteRegisteredResourceByAPIId(apiId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    /**
     *  Returns all the documents of the given API identifier that matches to the search condition
     *
     * @param apiId API identifier
     * @param limit max number of records returned
     * @param offset starting index
     * @param accept Accept header value
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String msg = "Error while retrieving documents of API " + apiId;
                handleException(msg, e);
            }
        }
        return null;
    }

    /**
     * Add a documentation to an API
     * 
     * @param apiId api identifier
     * @param body Documentation DTO as request body
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
                throw RestApiUtil.buildBadRequestException("otherTypeName cannot be empty if type is OTHER.");
            }

            String sourceUrl = body.getSourceUrl();
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                throw RestApiUtil.buildBadRequestException("Invalid document sourceUrl Format");
            }

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            if (apiProvider.isDocumentationExist(apiIdentifier, documentName)) {
                String errorMessage = "Requested document '" + documentName + "' already exists";
                throw RestApiUtil.buildConflictException(errorMessage);
            }

            apiProvider.addDocumentation(apiIdentifier, documentation);
            String newDocumentId = documentation.getId();

            //retrieve the newly added document
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while adding the document for API : " + apiId;
                handleException(errorMessage, e);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving location for document " + body.getName() + " of API " + apiId;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Returns a specific document by identifier that is belong to the given API identifier
     *
     * @param apiId API identifier
     * @param documentId document identifier
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
            }

            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            return Response.ok().entity(documentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    /**
     * Updates an existing document of an API
     * 
     * @param apiId API identifier
     * @param documentId document identifier
     * @param body updated document DTO
     * @param contentType Content-Type header
     * @param ifMatch If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated document DTO as response
     */
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId, String documentId, DocumentDTO body,
            String contentType, String ifMatch, String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            Documentation oldDocument = apiProvider.getDocumentation(documentId, tenantDomain);
            if (oldDocument == null) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
            }

            if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                throw RestApiUtil.buildBadRequestException("otherTypeName cannot be empty if type is OTHER.");
            }

            String sourceUrl = body.getSourceUrl();
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (StringUtils.isBlank(sourceUrl) || !RestApiUtil.isURL(sourceUrl))) {
                throw RestApiUtil.buildBadRequestException("Invalid document sourceUrl Format");
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while updating the document " + documentId + " for API : " + apiId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    /**
     * Deletes an existing document of an API
     * 
     * @param apiId API identifier
     * @param documentId document identifier
     * @param ifMatch If-match header value
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
            }

            apiProvider.removeDocumentation(apiIdentifier, documentId);
            return Response.ok().build();

        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    /**
     * Retrieves the content of a document
     *
     * @param apiId API identifier
     * @param documentId document identifier
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
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
            APIIdentifier apiIdentifier  = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            if (documentation == null) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
            }

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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the API " + apiId;
                handleException(errorMessage, e);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Add content to a document. Content can be inline or File
     * 
     * @param apiId API identifier
     * @param documentId document identifier
     * @param contentType content type of the payload
     * @param inputStream file input stream
     * @param fileDetail file details as Attachment
     * @param inlineContent inline content for the document
     * @param ifMatch If-match header value
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
                throw RestApiUtil
                        .buildBadRequestException("Only one of 'file' and 'inlineContent' should be specified");
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            if (documentation == null) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
            }

            //add content depending on the availability of either input stream or inline content
            if (inputStream != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    throw RestApiUtil
                            .buildBadRequestException("Source type of document " + documentId + " is not FILE");
                }
                RestApiPublisherUtils.attachFileToDocument(apiId, documentation, inputStream, fileDetail);
            } else if (inlineContent != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE)) {
                    throw RestApiUtil
                            .buildBadRequestException("Source type of document " + documentId + " is not INLINE");
                }
                apiProvider.addDocumentationContent(api, documentation.getName(), inlineContent);
            } else {
                throw RestApiUtil.buildBadRequestException("Either 'file' or 'inlineContent' should be specified");
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
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                handleException("Failed to add content to the document " + documentId, e);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving document content location : " + documentId;
            handleException(errorMessage, e);
        }
        return null;
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId API identifier
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @return Swagger document of the API
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier  = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            String apiSwagger = apiProvider.getSwagger20Definition(apiIdentifier);
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    /**
     * Updates the swagger definition of an existing API
     * 
     * @param apiId API identifier
     * @param apiDefinition Swagger definition
     * @param contentType content type of the payload
     * @param ifMatch If-match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @return updated swagger document of the API
     */
    @Override
    public Response apisApiIdSwaggerPut(String apiId, String apiDefinition, String contentType, String ifMatch,
            String ifUnmodifiedSince) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier  = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            apiProvider.saveSwagger20Definition(apiIdentifier, apiDefinition);

            //retrieves the updated swagger definition
            String apiSwagger = apiProvider.getSwagger20Definition(apiIdentifier);
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                handleException(errorMessage, e);
            }
        }
        return null;
    }

    private void handleException(String msg, Throwable t) throws InternalServerErrorException {
        log.error(msg, t);
        throw new InternalServerErrorException(t);
    }
}
