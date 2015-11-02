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

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.util.exception.InternalServerErrorException;
import org.wso2.carbon.apimgt.rest.api.store.utils.mappings.TierMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.Response;
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

    @Override
    public Response tiersTierNameGet(String tierName, String accept, String ifNoneMatch,
            String ifModifiedSince){
        //backend method requires
        return Response.ok().entity(null).build();
    }

}
