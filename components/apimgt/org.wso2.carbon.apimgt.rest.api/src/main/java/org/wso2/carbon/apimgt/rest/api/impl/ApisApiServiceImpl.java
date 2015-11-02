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

package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.rest.api.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.APIMappingUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


public class ApisApiServiceImpl extends ApisApiService {

    @Override
    public Response apisGet(String limit,String offset,String query,String type,String sort,String accept,String ifNoneMatch){
        List<API> apis;
        APIListDTO apiListDTO;
        boolean isTenantFlowStarted = false;

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

            //We should send null as the provider, Otherwise serchAPIs will return all APIs of the provider 
            // instead of looking at type and query
            apis = apiProvider.searchAPIs(query, type, null);
            apiListDTO = APIMappingUtil.fromAPIListToDTO(apis);
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
    @Override
    public Response apisChangeLifecyclePost(String newState,String publishToGateway,String resubscription,String apiId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisCopyApiPost(String newVersion,String apiId){
        boolean isTenantFlowStarted = false;
        URI newVersionedApiUri = null;
        APIDTO newVersionedApi = null;

        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromApiId(apiId);
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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
        APIDTO apiToReturn = new APIDTO();
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
    public Response apisApiIdDocumentsGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch){
        List<DocumentDTO> list = new ArrayList<DocumentDTO>();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            List<Documentation> docs = apiProvider.getAllDocumentation(APIMappingUtil.getAPIIdentifierFromApiId(apiId));
            for (org.wso2.carbon.apimgt.api.model.Documentation temp : docs) {
                list.add(APIMappingUtil.fromDocumentationtoDTO(temp));
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
        return Response.ok().entity(list).build();
    }

    @Override
    public Response apisApiIdDocumentsPost(String apiId,DocumentDTO body,String contentType){
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Documentation doc = APIMappingUtil.fromDTOtoDocumentation(body);
            apiProvider.addDocumentation(APIMappingUtil.getAPIIdentifierFromApiId(apiId),doc);
            return Response.status(Response.Status.CREATED).header("Location", "/apis/" + apiId + "/documents/" + doc.getId()).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince){
        Documentation doc;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            doc = apiProvider.getDocumentation(documentId);
            if(null != doc){
                return Response.ok().entity(doc).build();
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
            Documentation doc = APIMappingUtil.fromDTOtoDocumentation(body);
            apiProvider.updateDocumentation(APIMappingUtil.getAPIIdentifierFromApiId(apiId), doc);
            return Response.ok().entity(APIMappingUtil.fromDocumentationtoDTO(doc)).build();
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }

    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince){
        Documentation doc;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            doc = apiProvider.getDocumentation(documentId);
            if(null == doc){
                throw new NotFoundException();
            }
            apiProvider.removeDocumentation(APIMappingUtil.getAPIIdentifierFromApiId(apiId), documentId);
            return Response.ok().build();

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
    }
}
