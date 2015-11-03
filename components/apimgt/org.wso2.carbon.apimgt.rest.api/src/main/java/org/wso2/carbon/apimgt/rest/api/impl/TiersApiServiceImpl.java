/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.dto.TierPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.utils.mappings.TierMappingUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TiersApiServiceImpl extends TiersApiService {
    @Override
    public Response tiersGet(String accept,String ifNoneMatch){
        boolean isTenantFlowStarted = false;
        List<TierDTO> tierDTOs = new ArrayList<>();
        try {
            String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            Set<Tier> tiers = apiProvider.getTiers() ;

            for (Tier tier : tiers) {
                tierDTOs.add(TierMappingUtil.fromTiertoDTO(tier));
            }

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().entity(tierDTOs).build();
    }

    @Override public Response tiersPost(TierDTO body, String contentType) {
        boolean isTenantFlowStarted = false;
        URI createdApiUri = null;
        TierDTO  createdTierDTO = null;
        try {
            String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            apiProvider.addTier(TierMappingUtil.fromDTOtoTier(body));

            //apiProvider.getTier(name) is required
            //assign it to createdTierDTO

            createdApiUri = new URI(body.getName());

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } catch (URISyntaxException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.created(createdApiUri).entity(createdTierDTO).build();
    }

    @Override public Response tiersTierNameGet(String tierName, String accept, String ifNoneMatch,
            String ifModifiedSince){
        //backend method requires
        return Response.ok().entity(null).build();
    }

    @Override public Response tiersTierNamePut(String tierName, TierDTO body, String contentType, String ifMatch,
            String ifUnmodifiedSince) {
        boolean isTenantFlowStarted = false;
        TierDTO  updatedTierDTO = null;
        try {
            String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

            apiProvider.updateTier(TierMappingUtil.fromDTOtoTier(body));

            //apiProvider.getTier(name) is required
            //assign it to updatedTierDTO

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().entity(updatedTierDTO).build();
    }

    @Override public Response tiersTierNameDelete(String tierName, String ifMatch, String ifUnmodifiedSince){
        boolean isTenantFlowStarted = false;
        try {
            String tenantDomain =  CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            String userName = CarbonContext.getThreadLocalCarbonContext().getUsername();
            if (tenantDomain != null && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                isTenantFlowStarted = true;
                PrivilegedCarbonContext.startTenantFlow();
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain, true);
                PrivilegedCarbonContext.getThreadLocalCarbonContext().setUsername(userName);
            }
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            //getTierbyname
            apiProvider.removeTier(new Tier("sample"));//can we have a remove tier by name method?

        } catch (APIManagementException e) {
            throw new InternalServerErrorException(e);
        } finally {
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
        return Response.ok().build();
    }

    @Override
    public Response tiersTierNameUpdatePermissionPost(String tierName,
                                                      TierPermissionDTO permissions,
                                                      String contentType, String ifMatch,
                                                      String ifUnmodifiedSince) {
        return Response.ok().build();
    }
}
