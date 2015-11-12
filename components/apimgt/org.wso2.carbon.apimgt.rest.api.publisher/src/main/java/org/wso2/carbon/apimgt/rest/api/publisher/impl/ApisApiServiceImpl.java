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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
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
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/** This is the service implementation class for Publisher API related operations 
 * 
 */
public class ApisApiServiceImpl extends ApisApiService {

    /** Retrieves APIs qualifying under given search condition 
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
        boolean isTenantFlowStarted = false;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            /*String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
               // isTenantFlowStarted = true;
               // PrivilegedCarbonContext.startTenantFlow();
               // PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
               // PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }*/

            //We should send null as the provider, Otherwise searchAPIs will return all APIs of the provider
            // instead of looking at type and query
            allMatchedApis = apiProvider.searchAPIs(query, type, null);
            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, offset, limit);
            APIMappingUtil.setPaginationParams(apiListDTO, query, type, offset, limit, allMatchedApis.size());
            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } /*finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }     */
    }
    @Override
    public Response apisPost(APIDTO body,String contentType){

        boolean isTenantFlowStarted = false;
        URI createdApiUri = null;
        APIDTO  createdApiDTO = null;
        try {
            API apiToAdd = APIMappingUtil.fromDTOtoAPI(body);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
           /* if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }*/
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
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } catch (URISyntaxException e) {
            throw new InternalServerErrorException(e);
        } /*finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }   */
        return Response.created(createdApiUri).entity(createdApiDTO).build();
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
    
    @Override
    public Response apisCopyApiPost(String apiId, String newVersion){
        boolean isTenantFlowStarted = false;
        URI newVersionedApiUri = null;
        APIDTO newVersionedApi = null;

        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
           /* String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }*/

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
            } else {
                throw new NotFoundException();
            }

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } catch (DuplicateAPIException e) {
            throw new InternalServerErrorException(e);
        } catch (URISyntaxException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
    }
    @Override
    public Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince){
        APIDTO apiToReturn;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            /*String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            } */
            API api;
            if (RestApiUtil.isUUID(apiId)) {
                api = apiProvider.getAPIbyUUID(apiId);
            } else {
                APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
                api = apiProvider.getAPI(apiIdentifier);
            }

            if (api != null) {
                apiToReturn = APIMappingUtil.fromAPItoDTO(api);
            } else {
                throw new NotFoundException();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } /*finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }   */
        return Response.ok().entity(apiToReturn).build();
    }
    @Override
    public Response apisApiIdPut(String apiId,APIDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){

        boolean isTenantFlowStarted = false;
        APIDTO updatedApiDTO = null;
        try {
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            body.setName(apiIdentifier.getApiName());
            body.setVersion(apiIdentifier.getVersion());
            body.setProvider(apiIdentifier.getProviderName());
            API apiToUpdate = APIMappingUtil.fromDTOtoAPI(body);

            /*String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }*/
            apiProvider.updateAPI(apiToUpdate);
            updatedApiDTO = APIMappingUtil.fromAPItoDTO(apiProvider.getAPI(apiIdentifier));
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } catch (FaultGatewaysException e) {
            throw new InternalServerErrorException(e);
        } /*finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }   */
        return Response.ok().entity(updatedApiDTO).build();
    }
    @Override
    public Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince){
        boolean isTenantFlowStarted = false;
        try{
            String username = RestApiUtil.getLoggedInUsername();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiUtil.getProvider(username);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain);
            /*String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }*/
            apiProvider.deleteAPI(apiIdentifier);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();

            if (apiId != null) {
                keyManager.deleteRegisteredResourceByAPIId(apiId);
            }

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } /*finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }   */
        return Response.ok().build();
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
            documentation = apiProvider.getDocumentation(documentId);
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
            documentation = apiProvider.getDocumentation(documentId);
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

            doc = apiProvider.getDocumentation(documentId);
            if(null == doc){
                throw new NotFoundException();
            }

            apiProvider.removeDocumentation(apiIdentifier, documentId);
            return Response.ok().build();

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
