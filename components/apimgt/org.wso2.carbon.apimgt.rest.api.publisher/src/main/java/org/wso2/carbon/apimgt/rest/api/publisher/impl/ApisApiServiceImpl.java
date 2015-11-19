/*
 *  Copyright WSO2 Inc.
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
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
     * @param type value for the search condition
     * @param sort sort parameter
     * @param accept Accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return matched APIs for the given search condition
     */
    @Override
    public Response apisGet(Integer limit, Integer offset, String query, String type, String sort, String accept,
            String ifNoneMatch) {
        List<API> allMatchedApis;
        APIListDTO apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            //if query parameter is not specified, This will search by name
            String searchType = "Name";
            String searchContent = "";
            if (query != null) {
                String[] querySplit = query.split(":");
                if (querySplit.length == 2 && StringUtils.isNotBlank(querySplit[0]) && StringUtils
                        .isNotBlank(querySplit[1])) {
                    searchType = querySplit[0];
                    searchContent = querySplit[1];
                } else if (querySplit.length == 1) {
                    searchContent = query; 
                } else {
                    throw RestApiUtil.buildBadRequestException("Provided query parameter is invalid");
                }
            }

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            allMatchedApis = apiProvider.searchAPIs(searchContent, searchType, null);
            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, offset, limit);
            APIMappingUtil.setPaginationParams(apiListDTO, query, type, offset, limit, allMatchedApis.size());
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

            //todo: this can be moved to validation layer
            if (apiProvider.isDuplicateContextTemplate(body.getContext())) {
                throw RestApiUtil.buildBadRequestException(
                        "Error occurred while adding the API. A duplicate API context already exists for " + body
                                .getContext());
            }

            List<String> tiersFromDTO = body.getTiers();
            if (tiersFromDTO == null || tiersFromDTO.isEmpty()) {
                throw RestApiUtil.buildBadRequestException("No tier defined for the API");
            }

            //check whether the added API's tiers are all valid
            List<String> tierNames = body.getTiers();
            Set<Tier> definedTiers = apiProvider.getTiers();
            for (String tierName : tierNames) {
                boolean isTierValid = false;
                for (Tier definedTier : definedTiers) {
                    if (tierName.equals(definedTier.getName())) {
                        isTierValid = true;
                        break;
                    }
                }

                if (!isTierValid) {
                    throw RestApiUtil.buildBadRequestException("Specified tier " + tierName + " does not exist");
                }
            }

            API apiToAdd = APIMappingUtil.fromDTOtoAPI(body, username);

            if (apiProvider.isAPIAvailable(apiToAdd.getId())) {
                throw RestApiUtil.buildBadRequestException(
                        "Error occurred while adding the API. A duplicate API already exists for " + apiToAdd.getId()
                                .getApiName() + "-" + apiToAdd.getId().getVersion());
            }

            //Overriding some properties:
            //only allow CREATED as the stating state for the new api
            apiToAdd.setStatus(APIStatus.CREATED);

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
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" +
                   createdApiId.getProviderName() + "-" + createdApiId.getApiName() + "-" + createdApiId.getVersion());
            //how to add thumbnail
            //publish to external stores
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
    public Response apisChangeLifecyclePost(String apiId, String action, String lifecycleChecklist,
            String ifMatch, String ifUnmodifiedSince) {

        //pre-processing
        String[] checkListItems = lifecycleChecklist != null ? lifecycleChecklist.split(",") : new String[0];

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

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
            throw new InternalServerErrorException(e);
        }
    }

    /**
     * Copy API and create a new version of the API
     *
     * @param apiId API Identifier
     * @param newVersion new version of the API to be created
     * @return API new version
     */
    @Override
    public Response apisCopyApiPost(String apiId, String newVersion){
        URI newVersionedApiUri = null;
        APIDTO newVersionedApi = null;

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            API api = apiProvider.getAPI(apiIdentifier);
            if (api != null) {
                apiProvider.createNewAPIVersion(api, newVersion);
                //get newly created API to return as response
                APIIdentifier apiNewVersionedIdentifier =
                    new APIIdentifier(apiIdentifier.getProviderName(), apiIdentifier.getApiName(), newVersion);
                newVersionedApi = APIMappingUtil.fromAPItoDTO(apiProvider.getAPI(apiNewVersionedIdentifier));
                //This URI used to set the location header of the POST response
                newVersionedApiUri =
                        new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + apiIdentifier.getProviderName() + "-" +
                                apiIdentifier.getApiName() + "-" + apiIdentifier.getVersion());
                return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
            } else {
                throw RestApiUtil.buildNotFoundException(RestApiConstants.RESOURCE_API, apiId);
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error while copying API : " + apiId;
            handleException(errorMessage, e);
        } catch (DuplicateAPIException e) {
            String errorMessage = "Requested new version " + newVersion + "of API " + apiId + "already exist";
            handleException(errorMessage, e);
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
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            API api;
            if (RestApiUtil.isUUID(apiId)) {
                api = apiProvider.getAPIbyUUID(apiId, tenantDomain);
            } else {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
                api = apiProvider.getAPI(apiIdentifier);
            }

            if (api != null) {
                apiToReturn = APIMappingUtil.fromAPItoDTO(api);
                return Response.ok().entity(apiToReturn).build();
            } else {
                String errorMessage =  apiId + " does not exist";
                log.error(errorMessage);
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            handleException(errorMessage, e);
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

        APIDTO updatedApiDTO = null;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            body.setName(apiIdentifier.getApiName());
            body.setVersion(apiIdentifier.getVersion());
            body.setProvider(apiIdentifier.getProviderName());
            API apiToUpdate = APIMappingUtil.fromDTOtoAPI(body, username);

            apiProvider.updateAPI(apiToUpdate);
            updatedApiDTO = APIMappingUtil.fromAPItoDTO(apiProvider.getAPI(apiIdentifier));
            return Response.ok().entity(updatedApiDTO).build();
        } catch (APIManagementException | FaultGatewaysException e) {
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
        try{
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            apiProvider.deleteAPI(apiIdentifier);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();
            
            keyManager.deleteRegisteredResourceByAPIId(apiId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting API : " + apiId;
            handleException(errorMessage, e);
        }
        return null;
    }
    @Override
    public Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String query,String accept,String ifNoneMatch){
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        if (query == null) {
            query = "";
        }

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiIdentifier);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, query, apiId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response apisApiIdDocumentsPost(String apiId, DocumentDTO body, String contentType) {
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            apiProvider.addDocumentation(apiIdentifier, documentation);
            return Response.status(Response.Status.CREATED)
                    .header("Location", "/apis/" + apiId + "/documents/" + documentation.getId()).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince){
        Documentation documentation;
        try {
            RestApiPublisherUtils.checkUserAccessAllowedForAPI(apiId);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            if(null != documentation){
                DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
                return Response.ok().entity(documentDTO).build();
            }
            else{
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            apiProvider.updateDocumentation(apiIdentifier, documentation);
            //retrieve the updated documentation
            documentation = apiProvider.getDocumentation(documentId, tenantDomain);
            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(documentation)).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince){
        Documentation doc;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);

            doc = apiProvider.getDocumentation(documentId, tenantDomain);
            if(null == doc){
                throw new NotFoundException();
            }

            apiProvider.removeDocumentation(apiIdentifier, documentId);
            return Response.ok().build();

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    private void handleException(String msg, Throwable t) throws InternalServerErrorException {
        log.error(msg, t);
        throw new InternalServerErrorException(t);
    }
}
