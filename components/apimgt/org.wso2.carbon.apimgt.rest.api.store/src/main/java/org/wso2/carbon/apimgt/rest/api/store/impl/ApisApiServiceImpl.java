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

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.rest.api.store.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.util.exception.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class ApisApiServiceImpl extends ApisApiService {

    @Override
    public Response apisGet(Integer limit,Integer offset,String query,String type,String sort,String accept,String ifNoneMatch){
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
    public Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String query,String accept,String ifNoneMatch) {
        List<DocumentDTO> list = new ArrayList<DocumentDTO>();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            List<Documentation> docs = apiProvider.getAllDocumentation(APIMappingUtil.getAPIIdentifierFromApiIdOrUUID(apiId, tenantDomain));
            for (Documentation temp : docs) {
                list.add(APIMappingUtil.fromDocumentationtoDTO(temp));
            }
        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        }
        return Response.ok().entity(list).build();
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

}
