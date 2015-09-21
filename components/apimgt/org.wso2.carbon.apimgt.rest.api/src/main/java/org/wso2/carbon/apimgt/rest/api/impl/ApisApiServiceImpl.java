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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.factory.KeyManagerHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.apimgt.rest.api.impl.MappingUtil.fromAPItoDTO;
import static org.wso2.carbon.apimgt.rest.api.impl.MappingUtil.fromDTOtoAPI;

public class ApisApiServiceImpl extends ApisApiService {

    APIProvider provider ;

    @Override
    public Response apisGet(String limit,String offset,String query,String type,String sort,String accept,String ifNoneMatch){
        List<org.wso2.carbon.apimgt.api.model.API> apis;
        List<APIDTO> list = new ArrayList<APIDTO>();
        try {
            provider = APIManagerFactory.getInstance().getAPIProvider("admin");
            apis = provider.searchAPIs(query,type, "admin");
            for (org.wso2.carbon.apimgt.api.model.API temp : apis) {
                list.add(fromAPItoDTO(temp));
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
        return Response.ok().entity(list).build();
    }
    @Override
    public Response apisPost(APIDTO body,String contentType){

        boolean isTenantFlowStarted = false;
        try {
            org.wso2.carbon.apimgt.api.model.API apiToAdd = fromDTOtoAPI(body);
             String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
             if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                 isTenantFlowStarted = true;
                 PrivilegedCarbonContext.startTenantFlow();
                 PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
             }

             provider.addAPI(apiToAdd);

             //how to add thumbnail
             //publish to external stores
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisChangeLifecyclePost(String newState,String publishToGateway,String resubscription,String apiId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisCopyApiPost(String newVersion,String apiId){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince){
        //validate API id (provider's tenant = carbon context.tenant domain)
        String[] apiIdDetails = apiId.split("-");
        String apiName = apiIdDetails[0];
        String version = apiIdDetails[1];
        String providerName = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        boolean isTenantFlowStarted = false;
        APIDTO apiToReturn = new APIDTO();
        try {

            APIIdentifier apiIdentifier = new APIIdentifier(providerNameEmailReplaced, apiName, version);
            APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(providerName);
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();

            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
            }
            org.wso2.carbon.apimgt.api.model.API api = apiProvider.getAPI(apiIdentifier);
            if (api != null) {

                apiToReturn = fromAPItoDTO(api);

            } else {
                //log the error
                return Response.status(Response.Status.NOT_FOUND).entity("Cannot find the requested API- " + apiName +
                        "-" + version).type(MediaType.APPLICATION_JSON).build();
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return Response.ok().entity(apiToReturn).build();
    }
    @Override
    public Response apisApiIdPut(String apiId,APIDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        boolean isTenantFlowStarted = false;
        try {
            org.wso2.carbon.apimgt.api.model.API apiToAdd = fromDTOtoAPI(body);

            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)){
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
            }
            provider.updateAPI(apiToAdd);
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } catch (FaultGatewaysException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince){
        String[] apiIdDetails = apiId.split("-");
        String apiName = apiIdDetails[0];
        String version = apiIdDetails[1];
        String providerName = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        APIIdentifier apiIdentifier = new APIIdentifier(providerNameEmailReplaced, apiName, version);
        boolean isTenantFlowStarted = false;
        try{
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            if(tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
            }
            provider.deleteAPI(apiIdentifier);
            KeyManager keyManager = KeyManagerHolder.getKeyManagerInstance();

            if (apiId.toString() != null) {
                keyManager.deleteRegisteredResourceByAPIId(apiId.toString());
            }

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsPost(String apiId,DocumentDTO body,String contentType){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdEnvironmentsGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response apisApiIdExternalStoresGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch){
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
